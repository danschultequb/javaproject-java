package qub;

public interface JavaProjectPack
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("pack", JavaProjectPack::run)
            .setDescription("Package a Java source code project.");
    }

    static CommandLineParameter<Folder> addProjectFolderParameter(CommandLineParameters parameters, DesktopProcess process)
    {
        return JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to package. Defaults to the current folder.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        JavaProjectPack.run(process, action, null, null);
    }

    static void run(DesktopProcess process, CommandLineAction action, JavaProjectFolder providedProjectFolder, File providedLogFile)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectPack.addProjectFolderParameter(parameters, process);
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = JavaProject.addProfilerParameter(parameters, process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();
            profilerParameter.removeValue().catchError().await();

            final JavaProjectFolder projectFolder = providedProjectFolder != null
                ? providedProjectFolder
                : JavaProjectFolder.get(projectFolderParameter.getValue().await());

            final File logFile = providedLogFile != null
                ? providedLogFile
                : CommandLineLogsAction.getLogFileFromProcess(process);

            JavaProjectTest.run(process, action, projectFolder, logFile);

            if (process.getExitCode() == 0)
            {
                try (final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await()))
                {
                    final CharacterWriteStream output = logStreams.getOutput();
                    final VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder).await();
                    final VerboseChildProcessRunner childProcessRunner = VerboseChildProcessRunner.create(process, verbose);
                    final Jar jar = jdkFolder.getJar(childProcessRunner).await();

                    verbose.writeLine("Parsing pack.json file...").await();
                    final PackJSON packJson = projectFolder.getPackJson()
                        .catchError(() -> PackJSON.create())
                        .await();
                    final PackJSON newPackJson = PackJSON.create();

                    verbose.writeLine("Getting jar version...").await();
                    final VersionNumber jarVersion = jar.version().await();
                    verbose.writeLine("Previous jar version number: " + packJson.getJarVersion()).await();
                    verbose.writeLine("Current jar version number:  " + jarVersion).await();
                    newPackJson.setJarVersion(jarVersion);

                    final boolean jarVersionChanged = !Comparer.equal(packJson.getJarVersion(), jarVersion);

                    final Function4<File,Folder,String,Iterable<PackJSONFile>,Iterable<PackJSONFile>> createJarFileIfNeeded = (File jarFile, Folder baseFolder, String mainClassFullTypeName, Iterable<PackJSONFile> packJsonFiles) ->
                    {
                        Iterable<PackJSONFile> newPackJsonFiles;
                        final Path jarFileRelativePath = jarFile.relativeTo(projectFolder);

                        verbose.writeLine("Checking if the " + jarFileRelativePath.toString() + " needs to be created...").await();
                        final Iterable<File> baseFolderFiles = baseFolder.iterateFilesRecursively().catchError().toList();
                        if (!baseFolderFiles.any())
                        {
                            verbose.writeLine("No files exist that would go into " + jarFileRelativePath.toString() + ".").await();
                            jarFile.delete().catchError().await();
                            newPackJsonFiles = Iterable.create();
                        }
                        else
                        {
                            boolean createSourcesJarFile = true;
                            if (jarVersionChanged)
                            {
                                verbose.writeLine("Jar version changed.").await();
                            }
                            else if (!jarFile.exists().await())
                            {
                                verbose.writeLine(jarFileRelativePath.toString() + " doesn't exist.").await();
                            }
                            else
                            {
                                createSourcesJarFile = false;

                                final Map<Path, DateTime> previousBaseFolderFiles = packJsonFiles
                                    .toMap(PackJSONFile::getRelativePath, PackJSONFile::getLastModified);
                                for (final File baseFolderFile : baseFolderFiles)
                                {
                                    final Path relativePath = baseFolderFile.relativeTo(projectFolder);
                                    final DateTime previousLastModified = previousBaseFolderFiles.get(relativePath).catchError().await();
                                    if (previousLastModified == null || !previousLastModified.equals(baseFolderFile.getLastModified().await()))
                                    {
                                        verbose.writeLine("Found file(s) that have changed since the last pack occurred.").await();
                                        createSourcesJarFile = true;
                                        break;
                                    }
                                }
                            }

                            if (!createSourcesJarFile)
                            {
                                verbose.writeLine(jarFileRelativePath.toString() + " is up to date.").await();
                                newPackJsonFiles = packJsonFiles;
                            }
                            else
                            {
                                output.writeLine("Creating " + jarFileRelativePath.toString() + "...").await();
                                jar.run((JarParameters jarParameters) ->
                                {
                                    jarParameters.addCreate();
                                    jarParameters.addJarFile(jarFile);
                                    if (!Strings.isNullOrEmpty(mainClassFullTypeName))
                                    {
                                        jarParameters.addMainClass(mainClassFullTypeName);
                                    }
                                    jarParameters.addBaseFolderPath(baseFolder);
                                    jarParameters.addContentPath(".");
                                }).await();
                                newPackJsonFiles = baseFolder.iterateFilesRecursively()
                                    .map((File baseFolderFile) -> PackJSONFile.create(baseFolderFile.relativeTo(projectFolder), baseFolderFile.getLastModified().await()))
                                    .toList();
                            }
                        }

                        return newPackJsonFiles;
                    };


                    newPackJson.setSourceFiles(createJarFileIfNeeded.run(
                        projectFolder.getSourcesJarFile().await(),
                        projectFolder.getSourcesFolder().await(),
                        null,
                        packJson.getSourceFiles()));

                    newPackJson.setTestSourceFiles(createJarFileIfNeeded.run(
                        projectFolder.getTestSourcesJarFile().await(),
                        projectFolder.getTestSourcesFolder().await(),
                        null,
                        packJson.getTestSourceFiles()));

                    final String mainClassFullTypeName = projectFolder.getMainClass().catchError().await();
                    newPackJson.setSourceOutputFiles(createJarFileIfNeeded.run(
                        projectFolder.getCompiledSourcesJarFile().await(),
                        projectFolder.getOutputsSourcesFolder().await(),
                        mainClassFullTypeName,
                        packJson.getSourceOutputFiles()));

                    newPackJson.setTestOutputFiles(createJarFileIfNeeded.run(
                        projectFolder.getCompiledTestSourcesJarFile().await(),
                        projectFolder.getOutputsTestsFolder().await(),
                        null,
                        packJson.getTestOutputFiles()));

                    verbose.writeLine("Updating " + projectFolder.getPackJsonRelativePath().await() + "...").await();
                    projectFolder.writePackJson(newPackJson).await();
                }
            }
        }
    }
}
