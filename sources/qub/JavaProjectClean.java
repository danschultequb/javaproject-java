package qub;

public interface JavaProjectClean
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("clean", JavaProjectPack::run)
            .setDescription("Clean a Java source code project's build outputs.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to build. Defaults to the current folder.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final Folder projectFolder = projectFolderParameter.getValue().await();

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDesktopProcess(process, verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final IndentedCharacterWriteStream output = IndentedCharacterWriteStream.create(logStreams.getOutput());
                final VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();

                output.writeLine("Cleaning...").await();

                if (!projectFolder.exists().await())
                {
                    output.writeLine("The folder " + projectFolder + " doesn't exist.").await();
                }
                else
                {
                    int foldersCleaned = 0;
                    for (final String folderNameToClean : Iterable.create("outputs", "out", "target"))
                    {
                        final Folder folderToDelete = projectFolder.getFolder(folderNameToClean).await();
                        verbose.writeLine("Checking if " + folderToDelete + " exists...").await();
                        if (!folderToDelete.exists().await())
                        {
                            verbose.writeLine("Doesn't exist.").await();
                        }
                        else
                        {
                            ++foldersCleaned;
                            output.write("Deleting folder " + folderToDelete + "...").await();
                            folderToDelete.delete()
                                .then(() ->
                                {
                                    output.writeLine(" Done.").await();
                                })
                                .catchError((Throwable error) ->
                                {
                                    output.writeLine(" Failed.").await();
                                    output.indent(() -> output.writeLine(error.getMessage()).await());
                                })
                                .await();
                        }
                    }

                    if (foldersCleaned == 0)
                    {
                        output.writeLine("Found no folders to delete.").await();
                    }
                }
            }
        }
    }
}
