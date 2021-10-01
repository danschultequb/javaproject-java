package qub;

public class JavaClassFile extends File
{
    private Result<DateTime> lastModifiedResult;

    public JavaClassFile(File file)
    {
        super(file.getFileSystem(), file.getPath());
    }

    public static JavaClassFile get(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return new JavaClassFile(file);
    }

    @Override
    public Result<DateTime> getLastModified()
    {
        if (this.lastModifiedResult == null)
        {
            this.lastModifiedResult = super.getLastModified();
        }
        return this.lastModifiedResult;
    }
}
