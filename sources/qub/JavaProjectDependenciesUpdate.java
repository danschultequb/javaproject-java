package qub;

public interface JavaProjectDependenciesUpdate
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("update", JavaProjectDependenciesUpdate::run)
            .setDescription("Update the Java project's dependencies to the latest versions.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains the Java source code project to update.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = JavaProject.addProfilerParameter(parameters, process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();

            final Folder dataFolder = process.getQubProjectDataFolder().await();

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDataFolder(dataFolder, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final IndentedCharacterWriteStream output = IndentedCharacterWriteStream.create(logStreams.getOutput());
                final QubFolder qubFolder = process.getQubFolder().await();
                final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());

                output.writeLine("Getting dependencies for " + projectFolder.toString() + "...").await();
                final Iterable<ProjectSignature> currentDependencies = JavaProjectDependencies.getDependencies(projectFolder, output);
                if (Iterable.isNullOrEmpty(currentDependencies))
                {
                    output.writeLine("No dependencies found.").await();
                }
                else
                {
                    final List<ProjectSignature> newDependencies = List.create();
                    output.writeLine("Found " + currentDependencies.getCount() + " dependencies:").await();
                    output.indent(() ->
                    {
                        boolean dependenciesChanged = false;
                        for (final ProjectSignature currentDependency : currentDependencies)
                        {
                            output.write(currentDependency.toString()).await();

                            final QubProjectVersionFolder latestDependencyFolder = qubFolder.getLatestProjectVersionFolder(currentDependency.getPublisher(), currentDependency.getProject())
                                .catchError()
                                .await();
                            if (latestDependencyFolder == null)
                            {
                                newDependencies.add(currentDependency);
                                output.writeLine(" - Not Found").await();
                            }
                            else
                            {
                                final VersionNumber latestVersion = latestDependencyFolder.getVersion().await();
                                if (Comparer.equal(currentDependency.getVersion(), latestVersion))
                                {
                                    newDependencies.add(currentDependency);
                                    output.writeLine(" - No updates").await();
                                }
                                else
                                {
                                    final ProjectSignature newDependency = latestDependencyFolder.getProjectSignature().await();
                                    newDependencies.add(newDependency);
                                    dependenciesChanged = true;
                                    output.writeLine(" - Updated to " + newDependency).await();
                                }
                            }
                        }

                        if (dependenciesChanged)
                        {
                            final JavaProjectJSON javaProjectJSON = projectFolder.getProjectJson().await();
                            javaProjectJSON.setDependencies(newDependencies);
                            projectFolder.writeProjectJson(javaProjectJSON).await();
                        }
                    });

                    final Iterable<ProjectSignature> projectJsonTransitiveDependencies = projectFolder.getAllDependencies(qubFolder, false).await();

                    final Iterator<File> intellijProjectFiles = projectFolder.iterateFilesRecursively()
                        .where((File file) -> Comparer.equal(file.getFileExtension(), ".iml"));
                    if (intellijProjectFiles.any())
                    {
                        output.writeLine("Updating IntelliJ module files...").await();
                        output.indent(() ->
                        {
                            for (final File intellijProjectFile : intellijProjectFiles)
                            {
                                final IntellijModule intellijModule = IntellijModule.parse(intellijProjectFile)
                                    .catchError(() -> output.writeLine("Invalid Intellij Module file: " + intellijProjectFile.toString()).await())
                                    .await();
                                if (intellijModule != null)
                                {
                                    final List<ProjectSignature> dependenciesToAddToModule = List.create(projectJsonTransitiveDependencies);
                                    final Iterable<IntellijModuleLibrary> currentModuleLibraries = intellijModule.getModuleLibraries().toList();

                                    intellijModule.clearModuleLibraries();

                                    for (final IntellijModuleLibrary moduleLibrary : currentModuleLibraries)
                                    {
                                        final String classesUrl = moduleLibrary.getClassesUrls().first();
                                        if (Strings.isNullOrEmpty(classesUrl) || !classesUrl.startsWith("jar://"))
                                        {
                                            intellijModule.addModuleLibrary(moduleLibrary);
                                        }
                                        else
                                        {
                                            final int startIndex = "jar://".length();
                                            int endIndex = classesUrl.length();
                                            if (classesUrl.endsWith("!/"))
                                            {
                                                endIndex -= "!/".length();
                                            }
                                            final Path compiledSourcesFilePath = Path.parse(classesUrl.substring(startIndex, endIndex));
                                            if (!qubFolder.isAncestorOf(compiledSourcesFilePath).await())
                                            {
                                                output.writeLine(compiledSourcesFilePath + " - No updates").await();
                                                intellijModule.addModuleLibrary(moduleLibrary);
                                            }
                                            else
                                            {
                                                final Path compiledSourcesRelativeFilePath = compiledSourcesFilePath.relativeTo(qubFolder);
                                                final Indexable<String> segments = compiledSourcesRelativeFilePath.getSegments();
                                                final String publisher = segments.get(0);
                                                final String project = segments.get(1);
                                                String version = segments.get(2);
                                                if (version.equals("versions"))
                                                {
                                                    version = segments.get(3);
                                                }
                                                final ProjectSignature currentQubDependency = ProjectSignature.create(publisher, project, version);

                                                final ProjectSignature newQubDependency = dependenciesToAddToModule.removeFirst(currentQubDependency::equalsIgnoreVersion);
                                                if (newQubDependency == null)
                                                {
                                                    output.writeLine(currentQubDependency + " - Removed").await();
                                                }
                                                else
                                                {
                                                    if (newQubDependency.equals(currentQubDependency))
                                                    {
                                                        output.writeLine(currentQubDependency + " - No updates").await();
                                                    }
                                                    else
                                                    {
                                                        output.writeLine(currentQubDependency + " - Updated to " + newQubDependency).await();
                                                    }

                                                    final JavaPublishedProjectFolder projectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(
                                                        newQubDependency.getPublisher(),
                                                        newQubDependency.getProject(),
                                                        newQubDependency.getVersion()).await());
                                                    intellijModule.addModuleLibrary(IntellijModuleLibrary.create()
                                                        .addClassesUrl("jar://" + projectVersionFolder.getCompiledSourcesJarFile().await().toString() + "!/")
                                                        .addSourcesUrl("jar://" + projectVersionFolder.getSourcesJarFile().await().toString() + "!/")
                                                        .addClassesUrl("jar://" + projectVersionFolder.getCompiledTestsJarFile().await().toString() + "!/")
                                                        .addSourcesUrl("jar://" + projectVersionFolder.getTestSourcesJarFile().await().toString() + "!/"));
                                                }
                                            }
                                        }
                                    }

                                    for (final ProjectSignature dependencyToAddToModule : dependenciesToAddToModule)
                                    {
                                        output.writeLine(dependencyToAddToModule + " - Added").await();

                                        final JavaPublishedProjectFolder projectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(
                                            dependencyToAddToModule.getPublisher(),
                                            dependencyToAddToModule.getProject(),
                                            dependencyToAddToModule.getVersion()).await());
                                        intellijModule.addModuleLibrary(IntellijModuleLibrary.create()
                                            .addClassesUrl("jar://" + projectVersionFolder.getCompiledSourcesJarFile().await().toString() + "!/")
                                            .addSourcesUrl("jar://" + projectVersionFolder.getSourcesJarFile().await().toString() + "!/")
                                            .addClassesUrl("jar://" + projectVersionFolder.getCompiledTestsJarFile().await().toString() + "!/")
                                            .addSourcesUrl("jar://" + projectVersionFolder.getTestSourcesJarFile().await().toString() + "!/"));
                                    }

                                    intellijProjectFile.setContentsAsString(intellijModule.toString(XMLFormat.pretty)).await();
                                }
                            }
                        });
                    }

                    final File intellijWorkspaceFile = projectFolder.getFile(".idea/workspace.xml").await();
                    if (intellijWorkspaceFile.exists().await())
                    {
                        output.writeLine("Updating IntelliJ workspace file...").await();
                        output.indent(() ->
                        {
                            final IntellijWorkspace intellijWorkspace = Result.create(() -> IntellijWorkspace.create(XML.parse(intellijWorkspaceFile).await()))
                                .catchError(() -> output.writeLine("Invalid Intellij Workspace file: " + intellijWorkspaceFile).await())
                                .await();

                            final Folder testsFolder = projectFolder.getTestSourcesFolder().await();
                            final Iterable<String> fullTestClassNames = testsFolder.iterateFilesRecursively()
                                .where((File file) -> Comparer.equal(".java", file.getFileExtension()))
                                .map((File testJavaFile) -> JavaFile.getFullTypeName(testsFolder, testJavaFile))
                                .toList();

                            final List<String> fullTestClassNamesToAdd = List.create(fullTestClassNames);

                            final JavaPublishedProjectFolder qubJavaProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getLatestProjectVersionFolder("qub", "javaproject-java").await());
                            final List<ProjectSignature> runConfigurationDependencies = List.create(qubJavaProjectFolder.getProjectSignature().await())
                                .addAll(JavaProjectFolder.getAllDependencies(qubFolder, qubJavaProjectFolder.getDependencies().await(), false).await());

                            for (final ProjectSignature projectJsonTransitiveDependency : projectJsonTransitiveDependencies)
                            {
                                runConfigurationDependencies.removeFirst(projectJsonTransitiveDependency::equalsIgnoreVersion);
                                runConfigurationDependencies.add(projectJsonTransitiveDependency);
                            }

                            final CharacterList vmParameters = CharacterList.create();
                            final Path outputsSourcesRelativePath = projectFolder.getOutputsSourcesFolder().await()
                                .relativeTo(projectFolder);
                            final Path outputsTestsRelativePath = projectFolder.getOutputsTestsFolder().await()
                                .relativeTo(projectFolder);
                            vmParameters.addAll("-classpath ");
                            vmParameters.addAll("$PROJECT_DIR$/" + outputsSourcesRelativePath.toString());
                            vmParameters.addAll(";");
                            vmParameters.addAll("$PROJECT_DIR$/" + outputsTestsRelativePath.toString());
                            for (final ProjectSignature runConfigurationDependency : runConfigurationDependencies)
                            {
                                final JavaPublishedProjectFolder dependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(runConfigurationDependency).await());

                                final File compiledSourcesJarFile = dependencyFolder.getCompiledSourcesJarFile().await();
                                if (compiledSourcesJarFile.exists().await())
                                {
                                    vmParameters.add(';');
                                    vmParameters.addAll(compiledSourcesJarFile.toString());
                                }

                                final File compiledTestsJarFile = dependencyFolder.getCompiledTestsJarFile().await();
                                if (compiledTestsJarFile.exists().await())
                                {
                                    vmParameters.add(';');
                                    vmParameters.addAll(compiledTestsJarFile.toString());
                                }
                            }
                            final String vmParametersString = vmParameters.toString(true);
                            final String testJsonProgramParameter = "--testjson=false";
                            final String projectName = projectFolder.getProject().await();

                            final List<IntellijWorkspaceRunConfiguration> runConfigurationsToRemove = List.create();
                            for (final IntellijWorkspaceRunConfiguration runConfiguration : intellijWorkspace.getRunConfigurations())
                            {
                                final String runConfigurationName = runConfiguration.getName();
                                if (!fullTestClassNames.contains(runConfigurationName))
                                {
                                    runConfigurationsToRemove.add(runConfiguration);
                                }
                                else
                                {
                                    fullTestClassNamesToAdd.remove(runConfigurationName);

                                    runConfiguration.setType("Application");
                                    runConfiguration.setFactoryName("Application");
                                    runConfiguration.setMainClassFullName(Types.getFullTypeName(JavaProjectTest.class));
                                    runConfiguration.setModuleName(projectName);
                                    runConfiguration.setProgramParameters(Strings.join(' ', Iterable.create(testJsonProgramParameter, "--pattern=" + runConfigurationName)));
                                    runConfiguration.setVmParameters(vmParametersString);
                                }
                            }

                            for (final IntellijWorkspaceRunConfiguration runConfigurationToRemove : runConfigurationsToRemove)
                            {
                                intellijWorkspace.removeRunConfiguration(runConfigurationToRemove);
                            }

                            for (final String fullTestClassNameToAdd : fullTestClassNamesToAdd)
                            {
                                intellijWorkspace.addRunConfiguration(IntellijWorkspaceRunConfiguration.create()
                                    .setName(fullTestClassNameToAdd)
                                    .setType("Application")
                                    .setFactoryName("Application")
                                    .setMainClassFullName(Types.getFullTypeName(JavaProjectTest.class))
                                    .setModuleName(projectName)
                                    .setProgramParameters(Strings.join(' ', Iterable.create(testJsonProgramParameter, "--pattern=" + fullTestClassNameToAdd)))
                                    .setVmParameters(vmParametersString));
                            }

                            intellijWorkspaceFile.setContentsAsString(intellijWorkspace.toString(XMLFormat.pretty)).await();
                        });
                    }
                }
            }
        }
    }
}
