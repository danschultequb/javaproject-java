package qub;

public interface JavaProjectDependenciesUpdate
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("update", JavaProjectDependenciesUpdate::run)
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
                final VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();
                final QubFolder qubFolder = process.getQubFolder().await();
                final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());
                if (!projectFolder.exists().await())
                {
                    output.writeLine("The project folder at " + Strings.escapeAndQuote(projectFolder.toString()) + " doesn't exist.").await();
                    process.setExitCode(-1);
                }
                else
                {
                    final JavaProjectJSON projectJson = projectFolder.getProjectJson()
                        .catchError((Throwable error) ->
                        {
                            if (error instanceof FileNotFoundException)
                            {
                                output.writeLine("No project.json file exists in the project folder at " + Strings.escapeAndQuote(projectFolder.toString()) + ".").await();
                                verbose.writeLine(error.toString()).await();
                            }
                            else if (error instanceof ParseException)
                            {
                                output.writeLine("Invalid project.json file: " + error.getMessage()).await();
                                verbose.writeLine(error.toString()).await();
                            }
                            else
                            {
                                output.writeLine(error.toString()).await();
                            }

                            process.setExitCode(-1);
                        })
                        .await();
                    if (projectJson != null)
                    {
                        output.writeLine("Getting dependencies for " + projectFolder.toString() + "...").await();
                        final Iterable<ProjectSignature> currentDependencies = projectJson.getDependencies();
                        final List<ProjectSignature> newDependencies = List.create();
                        if (Iterable.isNullOrEmpty(currentDependencies))
                        {
                            output.writeLine("No dependencies found.").await();
                        }
                        else
                        {
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
                        }

                        final Iterable<ProjectSignature> projectJsonTransitiveDependencies = newDependencies.any()
                            ? projectFolder.getAllDependencies(qubFolder, false).await()
                            : Iterable.create();

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
                                            final String classesUrl = moduleLibrary.getClassesUrls().first().catchError().await();
                                            verbose.writeLine("Found module with classesUrl: " + Strings.escapeAndQuote(classesUrl)).await();
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

                        final Path outputsSourcesRelativePath = projectFolder.getOutputsSourcesFolder().await()
                            .relativeTo(projectFolder);
                        final Path outputsTestsRelativePath = projectFolder.getOutputsTestsFolder().await()
                            .relativeTo(projectFolder);

                        final List<ProjectSignature> runConfigurationDependencies = List.create();
                        final QubProjectVersionFolder qubJavaProjectLatestVersionFolder = qubFolder.getLatestProjectVersionFolder("qub", "javaproject-java")
                            .catchError()
                            .await();
                        if (qubJavaProjectLatestVersionFolder != null)
                        {
                            final JavaPublishedProjectFolder qubJavaProjectPublishedFolder = JavaPublishedProjectFolder.get(qubJavaProjectLatestVersionFolder);
                            runConfigurationDependencies.add(qubJavaProjectPublishedFolder.getProjectSignature().await());
                            runConfigurationDependencies.addAll(
                                JavaProjectFolder.getAllDependencies(qubFolder, qubJavaProjectPublishedFolder.getDependencies().await(), false).await());
                        }
                        for (final ProjectSignature projectJsonTransitiveDependency : projectJsonTransitiveDependencies)
                        {
                            runConfigurationDependencies.removeFirst(projectJsonTransitiveDependency::equalsIgnoreVersion);
                            runConfigurationDependencies.add(projectJsonTransitiveDependency);
                        }

                        final Folder testsFolder = projectFolder.getTestSourcesFolder().await();
                        final Iterable<String> fullTestClassNames = testsFolder.iterateFilesRecursively()
                            .where((File file) -> Comparer.equal(".java", file.getFileExtension()))
                            .map((File testJavaFile) -> JavaFile.getFullTypeName(testsFolder, testJavaFile))
                            .catchError(() -> Iterable.create())
                            .toList();

                        final String testJsonProgramParameter = "--testjson=false";
                        final String projectName = projectFolder.getProject().await();

                        final File intellijWorkspaceFile = projectFolder.getFile(".idea/workspace.xml").await();
                        if (intellijWorkspaceFile.exists().await())
                        {
                            output.writeLine("Updating IntelliJ workspace file...").await();
                            output.indent(() ->
                            {
                                final IntellijWorkspace intellijWorkspace = Result.create(() -> IntellijWorkspace.create(XML.parse(intellijWorkspaceFile).await()))
                                    .catchError(() -> output.writeLine("Invalid Intellij Workspace file: " + intellijWorkspaceFile).await())
                                    .await();

                                final List<String> fullTestClassNamesToAdd = List.create(fullTestClassNames);

                                final CharacterList vmParameters = CharacterList.create();
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

                        final VSCodeWorkspaceFolder vscodeWorkspaceFolder = VSCodeWorkspaceFolder.get(projectFolder);
                        final File vscodeSettingsJsonFile = vscodeWorkspaceFolder.getSettingsJsonFile();
                        if (vscodeSettingsJsonFile.exists().await())
                        {
                            output.writeLine("Updating " + vscodeSettingsJsonFile.relativeTo(vscodeWorkspaceFolder) + "...").await();

                            final VSCodeJavaSettingsJson vscodeSettingsJson = VSCodeJavaSettingsJson.parse(vscodeSettingsJsonFile).catchError().await();
                            if (vscodeSettingsJson != null)
                            {
                                boolean vscodeSettingsJsonChanged = false;

                                final List<String> sourcePaths = vscodeSettingsJson.getJavaProjectSourcePaths().toList();
                                boolean sourcePathsChanged = false;
                                for (final String sourcePath : Iterable.create("sources", "tests"))
                                {
                                    if (!sourcePaths.contains(sourcePath))
                                    {
                                        sourcePathsChanged = true;
                                        sourcePaths.add(sourcePath);
                                    }
                                }
                                if (sourcePathsChanged)
                                {
                                    vscodeSettingsJsonChanged = true;

                                    vscodeSettingsJson.setJavaProjectSourcePaths(sourcePaths);
                                }

                                final Iterable<String> javaProjectReferencedLibraries = vscodeSettingsJson.getJavaProjectReferencedLibraries();
                                final List<String> newJavaProjectReferencedLibraries = List.create();
                                for (final ProjectSignature projectDependency : projectJsonTransitiveDependencies)
                                {
                                    final JavaPublishedProjectFolder projectDependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(projectDependency).await());

                                    final List<File> projectDependencyFiles = List.create(
                                        projectDependencyFolder.getCompiledSourcesJarFile().await(),
                                        projectDependencyFolder.getCompiledTestsJarFile().await());
                                    for (final File projectDependencyFile : projectDependencyFiles)
                                    {
                                        if (projectDependencyFile.exists().await())
                                        {
                                            newJavaProjectReferencedLibraries.add(projectDependencyFile.toString());
                                        }
                                    }
                                }
                                if (!javaProjectReferencedLibraries.equals(newJavaProjectReferencedLibraries))
                                {
                                    vscodeSettingsJsonChanged = true;

                                    vscodeSettingsJson.setJavaProjectReferencedLibraries(newJavaProjectReferencedLibraries);
                                }

                                if (vscodeSettingsJsonChanged)
                                {
                                    vscodeSettingsJsonFile.setContentsAsString(vscodeSettingsJson.toString(JSONFormat.pretty)).await();
                                }
                            }
                        }

                        final File vscodeLaunchJsonFile = vscodeWorkspaceFolder.getLaunchJsonFile();
                        if (vscodeLaunchJsonFile.exists().await())
                        {
                            output.writeLine("Updating " + vscodeLaunchJsonFile.relativeTo(vscodeWorkspaceFolder) + "...").await();

                            final VSCodeLaunchJson launchJson = VSCodeLaunchJson.parse(vscodeLaunchJsonFile).catchError().await();
                            if (launchJson != null)
                            {
                                if (launchJson.getVersion() == null)
                                {
                                    launchJson.setVersion("0.2.0");
                                }

                                final List<VSCodeJavaLaunchConfigurationJson> configurations = List.create();
                                for (final JavaFile testJavaFile : projectFolder.iterateTestJavaFiles())
                                {
                                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();

                                    configuration.setType("java");

                                    configuration.setRequest("launch");

                                    final Path testFileRelativePath = testJavaFile.relativeTo(testsFolder);
                                    configuration.setName(testFileRelativePath.toString());

                                    final List<String> classPaths = List.create(
                                        outputsSourcesRelativePath.toString(),
                                        outputsTestsRelativePath.toString());
                                    for (final ProjectSignature runConfigurationDependency : runConfigurationDependencies)
                                    {
                                        final JavaPublishedProjectFolder dependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(runConfigurationDependency).await());

                                        final File compiledSourcesJarFile = dependencyFolder.getCompiledSourcesJarFile().await();
                                        if (compiledSourcesJarFile.exists().await())
                                        {
                                            classPaths.add(compiledSourcesJarFile.toString());
                                        }

                                        final File compiledTestsJarFile = dependencyFolder.getCompiledTestsJarFile().await();
                                        if (compiledTestsJarFile.exists().await())
                                        {
                                            classPaths.add(compiledTestsJarFile.toString());
                                        }
                                    }
                                    configuration.setClassPaths(classPaths);

                                    configuration.setMainClass(Types.getFullTypeName(JavaProjectTest.class));

                                    configuration.setArgs(Strings.join(' ', Iterable.create(testJsonProgramParameter, "--pattern=" + JavaClassFile.getFullTypeName(testFileRelativePath))));

                                    configurations.add(configuration);
                                }
                                launchJson.setConfigurations(configurations);

                                vscodeWorkspaceFolder.setLaunchJson(launchJson).await();
                            }
                        }
                    }
                }
            }
        }
    }
}
