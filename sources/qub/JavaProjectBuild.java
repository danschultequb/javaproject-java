package qub;

public interface JavaProjectBuild
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("build", JavaProjectBuild::run)
            .setDescription("Build a Java source code project.");
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        process.getOutputWriteStream().writeLine("Building...").await();
    }
}
