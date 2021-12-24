package qub;

public class JDKFolder extends QubProjectVersionFolder
{
    private JDKFolder(Folder innerFolder)
    {
        super(innerFolder);
    }

    public static JDKFolder get(Folder innerFolder)
    {
        PreCondition.assertNotNull(innerFolder, "innerFolder");

        return new JDKFolder(innerFolder);
    }

    public static Result<JDKFolder> getLatestVersion(QubFolder qubFolder)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        return Result.create(() ->
        {
            final String jdkPublisherName = "openjdk";
            final String jdkProjectName = "jdk";
            final QubProjectVersionFolder latestJdkFolder = qubFolder.getLatestProjectVersionFolder(jdkPublisherName, jdkProjectName).await();
            return JDKFolder.get(latestJdkFolder);
        });
    }

    public Result<File> getJavacFile()
    {
        return this.getFile("bin/javac");
    }

    public Result<Javac> getJavac(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return this.getJavac(process.getChildProcessRunner());
    }

    public Result<Javac> getJavac(ChildProcessRunner childProcessRunner)
    {
        PreCondition.assertNotNull(childProcessRunner, "childProcessRunner");

        return Result.create(() ->
        {
            return Javac.create(childProcessRunner)
                .setExecutablePath(this.getJavacFile().await());
        });
    }

    public Result<File> getJavaFile()
    {
        return this.getFile("bin/java");
    }

    public Result<Java> getJava(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return this.getJava(process.getChildProcessRunner());
    }

    public Result<Java> getJava(ChildProcessRunner childProcessRunner)
    {
        PreCondition.assertNotNull(childProcessRunner, "childProcessRunner");

        return Result.create(() ->
        {
            return Java.create(childProcessRunner)
                .setExecutablePath(this.getJavaFile().await());
        });
    }
}
