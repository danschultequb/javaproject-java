package qub;

public class JDKFolder extends Folder
{
    private final Folder innerFolder;

    private JDKFolder(Folder innerFolder)
    {
        super(innerFolder.getFileSystem(), innerFolder.getPath());

        this.innerFolder = innerFolder;
    }

    public static Result<JDKFolder> get(Folder innerFolder)
    {
        PreCondition.assertNotNull(innerFolder, "innerFolder");

        return Result.success(new JDKFolder(innerFolder));
    }

    public static Result<JDKFolder> getLatestVersion(QubFolder qubFolder, CharacterWriteStream outputStream)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");
        PreCondition.assertNotNull(outputStream, "outputStream");

        return Result.create(() ->
        {
            final String jdkPublisherName = "openjdk";
            final String jdkProjectName = "jdk";
            final QubProjectVersionFolder latestJdkFolder = qubFolder.getProjectLatestVersionFolder(jdkPublisherName, jdkProjectName)
                .onError(NotFoundException.class, () ->
                {
                    outputStream.writeLine("No openjdk/jdk project is installed in the qub folder at " + Strings.escapeAndQuote(qubFolder) + ".").await();
                })
                .await();
            return JDKFolder.get(latestJdkFolder).await();
        });
    }

    public Result<Javac> getJavac(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return Result.create(() ->
        {
            final File javacFile = this.getFile("bin/javac").await();
            return Javac.create(process.getChildProcessRunner())
                .setExecutablePath(javacFile);
        });
    }
}
