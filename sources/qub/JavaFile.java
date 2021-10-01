package qub;

public class JavaFile extends File
{
    private Result<DateTime> lastModifiedResult;

    public JavaFile(File file)
    {
        super(file.getFileSystem(), file.getPath());
    }

    public static JavaFile get(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return new JavaFile(file);
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
