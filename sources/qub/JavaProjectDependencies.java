package qub;

public interface JavaProjectDependencies
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("dependencies", JavaProjectDependencies::run)
            .setDescription("Perform actions based on a Java project's dependencies.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        action.createCommandLineActions()
            .addAction(JavaProjectDependencies::addListAction)
            .run();
    }

    static void addListAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("list", JavaProjectDependencies::runList)
            .setDescription("List the Java project's dependencies.");
    }

    static void runList(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that the new Java project will be created in.");
        final CommandLineParameterBoolean recurseParameter = parameters.addBoolean("recurse")
            .setAliases("r")
            .setDescription("Whether the entire dependency tree will be output (true) or just the immediate dependencies (false). Defaults to false.");
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
                final boolean recurse = recurseParameter.getValue().await();

                output.writeLine("Getting dependencies for " + projectFolder.toString() + "...").await();
                final Iterable<ProjectSignature> dependencies = JavaProjectDependencies.getDependencies(projectFolder, output);
                if (Iterable.isNullOrEmpty(dependencies))
                {
                    output.writeLine("No dependencies found.").await();
                }
                else
                {
                    output.writeLine("Found " + dependencies.getCount() + " dependencies:").await();
                    output.indent(() ->
                    {
                        JavaProjectDependencies.writeDependencyTree(qubFolder, dependencies, output, recurse);
                    });
                }
            }
        }
    }

    static Iterable<ProjectSignature> getDependencies(JavaProjectFolder projectFolder, CharacterWriteStream output)
    {
        PreCondition.assertNotNull(projectFolder, "projectFolder");
        PreCondition.assertNotNull(output, "output");

        return projectFolder.getDependencies()
            .catchError((Throwable e) -> output.writeLine(e.getMessage()).await())
            .await();
    }

    static void writeDependencyTree(QubFolder qubFolder, Iterable<ProjectSignature> dependencies, IndentedCharacterWriteStream output, boolean recurse)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");
        PreCondition.assertNotNull(dependencies, "dependencies");
        PreCondition.assertNotNull(output, "output");

        for (final ProjectSignature dependency : dependencies)
        {
            output.writeLine(dependency.toString()).await();
            if (recurse)
            {
                final JavaProjectFolder publishedDependencyFolder = JavaProjectFolder.get(qubFolder.getProjectVersionFolder(dependency).await());
                final Iterable<ProjectSignature> dependencyDependencies = JavaProjectDependencies.getDependencies(publishedDependencyFolder, output);
                output.indent(() ->
                {
                    JavaProjectDependencies.writeDependencyTree(qubFolder, dependencyDependencies, output, recurse);
                });
            }
        }
    }
}
