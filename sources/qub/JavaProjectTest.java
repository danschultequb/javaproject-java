package qub;

public interface JavaProjectTest
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("test", JavaProjectTest::run)
            .setDescription("Run the tests of a Java source code project.");
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        process.getOutputWriteStream().writeLine("Running tests...").await();
    }
}
