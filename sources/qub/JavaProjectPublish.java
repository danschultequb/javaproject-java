package qub;

public interface JavaProjectPublish
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("publish", JavaProjectPublish::run)
            .setDescription("Publish a Java source code project.");
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        process.getOutputWriteStream().writeLine("Publishing...").await();
    }
}
