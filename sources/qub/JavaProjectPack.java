package qub;

public interface JavaProjectPack
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("pack", JavaProjectPack::run)
            .setDescription("Package a Java source code project.");
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        process.getOutputWriteStream().writeLine("Packing...").await();
    }
}
