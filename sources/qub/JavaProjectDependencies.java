package qub;

public interface JavaProjectDependencies
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("dependencies", JavaProjectDependencies::run)
            .setDescription("Perform actions based on a Java project's dependencies.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        action.createCommandLineActions()
            .addAction(JavaProjectDependenciesList::addAction)
            .addAction(JavaProjectDependenciesUpdate::addAction)
            .run();
    }
}
