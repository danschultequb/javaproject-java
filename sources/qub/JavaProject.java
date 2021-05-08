package qub;

import java.awt.*;

public interface JavaProject
{
    static void main(String[] args)
    {
        DesktopProcess.run(args, JavaProject::run);
    }

    static CommandLineActions createCommandLineActions(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return process.createCommandLineActions()
            .setApplicationName("qub-javaproject")
            .setApplicationDescription("An application used to interact with Java source code projects.");
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        JavaProject.createCommandLineActions(process)
            .addAction(JavaProjectCreate::addAction)
            .addAction(JavaProjectBuild::addAction)
            .addAction(JavaProjectTest::addAction)
            .addAction(JavaProjectPack::addAction)
            .addAction(JavaProjectPublish::addAction)
            .addAction(CommandLineLogsAction::addAction)
            .addAction(CommandLineConfigurationAction::addAction)
            .run();
    }
}