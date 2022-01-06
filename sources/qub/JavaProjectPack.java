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
                    final Jar jar = jdkFolder.getJar(process).await();

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

                    verbose.writeLine("Checking if the sources jar file needs to be created...").await();
                    boolean createSourcesJarFile = jarVersionChanged;
                    if (!createSourcesJarFile)
                    {
                        final Map<Path,DateTime> previousSourceFiles = packJson.getSourceFiles()
                            .toMap(PackJSONFile::getRelativePath, PackJSONFile::getLastModified);
                        for (final JavaFile sourceFile : projectFolder.iterateSourceJavaFiles())
                        {
                            final Path relativePath = sourceFile.relativeTo(projectFolder);
                            final DateTime previousLastModified = previousSourceFiles.get(relativePath).catchError().await();
                            if (previousLastModified == null || !previousLastModified.equals(sourceFile.getLastModified().await()))
                            {
                                createSourcesJarFile = true;
                                break;
                            }
                        }
                    }
                    Iterable<PackJSONFile> newPackJsonSourceFiles;
                    if (!createSourcesJarFile)
                    {
                        verbose.writeLine("Source jar file is up to date.").await();
                        newPackJsonSourceFiles = packJson.getSourceFiles();
                    }
                    else
                    {
                        output.writeLine("Creating sources jar file...").await();
                        newPackJsonSourceFiles = projectFolder.iterateSourceJavaFiles()
                            .map((JavaFile javaFile) -> PackJSONFile.create(javaFile.relativeTo(projectFolder), javaFile.getLastModified().await()))
                            .toList();
                    }
                    newPackJson.setSourceFiles(newPackJsonSourceFiles);

                    verbose.writeLine("Checking if the compiled sources jar file needs to be created...").await();

                    verbose.writeLine("Checking if the test sources jar file needs to be created...").await();

                    verbose.writeLine("Checking if the compiled tests jar file needs to be created...").await();

                    verbose.writeLine("Updating " + projectFolder.getPackJsonRelativePath().await() + "...").await();
                    projectFolder.writePackJson(newPackJson).await();
                }
            }
        }
    }
}
