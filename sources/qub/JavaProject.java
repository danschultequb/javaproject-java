package qub;

public interface JavaProject
{
    String projectFolderParameterName = "projectFolder";

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
            .addAction(JavaProjectClean::addAction)
            .addAction(JavaProjectBuild::addAction)
            .addAction(JavaProjectTest::addAction)
            .addAction(JavaProjectPack::addAction)
            .addAction(JavaProjectPublish::addAction)
            .addAction(CommandLineLogsAction::addAction)
            .addAction(CommandLineConfigurationAction::addAction)
            .run();
    }

    static CommandLineParameter<Folder> addProjectFolderParameter(CommandLineParameters parameters, DesktopProcess process, String parameterDescription)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNullAndNotEmpty(parameterDescription, "parameterDescription");

        return parameters.addPositionalFolder(JavaProject.projectFolderParameterName, process)
            .setDescription(parameterDescription);
    }

    static CommandLineParameterProfiler addProfilerParameter(CommandLineParameters parameters, DesktopProcess process)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");

        return parameters.addProfiler(process, JavaProject.class);
    }

    static String getSourcesJarFileName(String projectName)
    {
        PreCondition.assertNotNullAndNotEmpty(projectName, "projectName");

        return projectName + ".sources.jar";
    }

    static String getTestSourcesJarFileName(String projectName)
    {
        PreCondition.assertNotNullAndNotEmpty(projectName, "projectName");

        return projectName + ".test.sources.jar";
    }

    static String getCompiledSourcesJarFileName(String projectName)
    {
        PreCondition.assertNotNullAndNotEmpty(projectName, "projectName");

        return projectName + ".jar";
    }

    static String getCompiledTestsJarFileName(String projectName)
    {
        PreCondition.assertNotNullAndNotEmpty(projectName, "projectName");

        return projectName + ".tests.jar";
    }
}