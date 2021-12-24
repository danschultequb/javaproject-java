package qub;

public interface JavaProjectClean
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("clean", JavaProjectClean::run)
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
        final CommandLineParameterProfiler profilerParameter = JavaProject.addProfilerParameter(parameters, process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();

            final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDesktopProcess(process, verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final IndentedCharacterWriteStream output = IndentedCharacterWriteStream.create(logStreams.getOutput());

                output.writeLine("Cleaning...").await();

                if (!projectFolder.exists().await())
                {
                    output.writeLine("The project folder " + projectFolder + " doesn't exist.").await();
                }
                else
                {
                    final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                    if (outputsFolder.exists().await())
                    {
                        output.write("Deleting folder " + outputsFolder + "...").await();
                        outputsFolder.delete()
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
            }
        }
    }
}
