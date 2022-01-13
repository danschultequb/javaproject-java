package qub;

public interface JavaProjectPublish
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("publish", JavaProjectPublish::run)
            .setDescription("Publish a Java source code project.");
    }

    static CommandLineParameter<Folder> addProjectFolderParameter(CommandLineParameters parameters, DesktopProcess process)
    {
        return JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to publish. Defaults to the current folder.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        JavaProjectPublish.run(process, action, null, null);
    }

    static void run(DesktopProcess process, CommandLineAction action, JavaProjectFolder providedProjectFolder, File providedLogFile)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectPublish.addProjectFolderParameter(parameters, process);
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

            JavaProjectPack.run(process, action, projectFolder, logFile);

            if (process.getExitCode() == 0)
            {
                try (final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await()))
                {
                    final CharacterWriteStream output = logStreams.getOutput();
                    final VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final File projectJsonFile = projectFolder.getProjectJsonFile().await();
                    final JavaProjectJSON projectJSON = projectFolder.getProjectJson().await();
                    final String publisher = projectJSON.getPublisher();
                    final String project = projectJSON.getProject();
                    VersionNumber version = projectJSON.getVersion();
                    final QubProjectFolder publishedProjectFolder = qubFolder.getProjectFolder(publisher, project).await();
                    if (version == null || !version.any())
                    {
                        final QubProjectVersionFolder latestVersionFolder = publishedProjectFolder.getLatestProjectVersionFolder().catchError().await();
                        if (latestVersionFolder != null)
                        {
                            final VersionNumber latestVersion = latestVersionFolder.getVersion().catchError().await();
                            if (latestVersion != null && latestVersion.hasMajor())
                            {
                                version = VersionNumber.create().setMajor(latestVersion.getMajor() + 1);
                            }
                        }
                        if (version == null || !version.any())
                        {
                            version = VersionNumber.create().setMajor(1);
                        }
                    }

                    final QubProjectVersionFolder versionFolder = publishedProjectFolder.getProjectVersionFolder(version).await();
                    if (versionFolder.exists().await())
                    {
                        output.writeLine("This package (" + publisher + "/" + project + ":" + version + ") can't be published because a package with that signature already exists.").await();
                        process.setExitCode(1);
                    }
                    else
                    {
                        final File sourcesJarFile = projectFolder.getSourcesJarFile().await();
                        final File testSourcesJarFile = projectFolder.getTestSourcesJarFile().await();
                        final File compiledSourcesJarFile = projectFolder.getCompiledSourcesJarFile().await();
                        final File compiledTestsJarFile = projectFolder.getCompiledTestSourcesJarFile().await();

                        output.writeLine("Publishing " + publisher + "/" + project + "@" + version + "...").await();
                        JavaProjectPublish.publishFile(projectJsonFile, versionFolder, verbose);
                        JavaProjectPublish.publishFile(sourcesJarFile, versionFolder, verbose);
                        JavaProjectPublish.publishFile(testSourcesJarFile, versionFolder, verbose);
                        JavaProjectPublish.publishFile(compiledSourcesJarFile, versionFolder, verbose);
                        JavaProjectPublish.publishFile(compiledTestsJarFile, versionFolder, verbose);

                        final String mainClass = projectJSON.getMainClass();
                        if (!Strings.isNullOrEmpty(mainClass))
                        {
                            String shortcutName = projectJSON.getShortcutName();
                            if (Strings.isNullOrEmpty(shortcutName))
                            {
                                shortcutName = projectJSON.getProject();
                            }

                            final CharacterList classpath = CharacterList.create()
                                .addAll("%~dp0").addAll(versionFolder.getFile(compiledSourcesJarFile.getName()).await().relativeTo(qubFolder).toString());
                            Iterable<JavaPublishedProjectFolder> dependencyFolders = projectJSON.getAllDependencyFolders(qubFolder).await();
                            if (!Iterable.isNullOrEmpty(dependencyFolders))
                            {
                                for (final JavaPublishedProjectFolder dependencyFolder : dependencyFolders)
                                {
                                    final File dependencyCompiledSourcesJarFile = dependencyFolder.getCompiledSourcesJarFile().await();
                                    final Path dependencyCompiledSourcesJarFileRelativePath = dependencyCompiledSourcesJarFile.relativeTo(qubFolder);
                                    classpath.addAll(";%~dp0").addAll(dependencyCompiledSourcesJarFileRelativePath.toString());
                                }
                            }

                            final File shortcutFile = qubFolder.getFile(shortcutName + ".cmd").await();
                            try (final CharacterWriteStream shortcutFileStream = shortcutFile.getContentsCharacterWriteStream().await())
                            {
                                shortcutFileStream.writeLine("@echo OFF").await();
                                shortcutFileStream.writeLine("java -classpath " + classpath + " " + mainClass + " %*").await();
                            }
                        }

                        final List<String> projectsToUpdate = List.create();
                        for (final QubPublisherFolder publisherFolder : qubFolder.iteratePublisherFolders())
                        {
                            for (final QubProjectFolder projectFolder2 : publisherFolder.iterateProjectFolders())
                            {
                                final QubProjectVersionFolder latestVersionFolder = projectFolder2.getLatestProjectVersionFolder().catchError().await();
                                if (latestVersionFolder != null)
                                {
                                    final JavaPublishedProjectFolder javaPublishedProjectFolder = JavaPublishedProjectFolder.get(latestVersionFolder);
                                    final JavaProjectJSON publishedProjectJson = javaPublishedProjectFolder.getProjectJson().catchError().await();
                                    if (publishedProjectJson != null)
                                    {
                                        final Iterable<ProjectSignature> dependencies = publishedProjectJson.getDependencies();
                                        if (!Iterable.isNullOrEmpty(dependencies))
                                        {
                                            final ProjectSignature dependency = dependencies.first((ProjectSignature d) ->
                                                Comparer.equal(d.getPublisher(), publisher) &&
                                                    Comparer.equal(d.getProject(), project));
                                            if (dependency != null)
                                            {
                                                final ProjectSignature publishedProjectSignature = ProjectSignature.create(
                                                    publishedProjectJson.getPublisher(),
                                                    publishedProjectJson.getProject(),
                                                    publishedProjectJson.getVersion());
                                                projectsToUpdate.add(publishedProjectSignature.toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!Iterable.isNullOrEmpty(projectsToUpdate))
                        {
                            output.writeLine("The following projects should be updated to use " + publisher + "/" + project + "@" + version + ":").await();
                            for (final String projectToUpdate : projectsToUpdate)
                            {
                                output.writeLine("  " + projectToUpdate).await();
                            }
                        }
                    }
                }
            }
        }
    }

    static void publishFile(File file, QubProjectVersionFolder publishedFolder, VerboseCharacterToByteWriteStream verbose)
    {
        PreCondition.assertNotNull(file, "file");
        PreCondition.assertNotNull(publishedFolder, "publishedFolder");
        PreCondition.assertNotNull(verbose, "verbose");

        verbose.writeLine("Publishing " + file.toString() + " to " + publishedFolder.toString() + "...").await();
        file.copyToFolder(publishedFolder)
            .catchError(FileNotFoundException.class, () -> verbose.writeLine(file.toString() + " can't be published because it doesn't exist.").await())
            .catchError((Throwable e) -> verbose.writeLine(e.toString()))
            .await();
    }
}
