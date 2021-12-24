package qub;

public class JavaFile extends File
{
    private Result<DateTime> lastModifiedResult;

    private JavaFile(File file)
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

    public static String getFullTypeName(Path relativeFilePath)
    {
        PreCondition.assertNotNull(relativeFilePath, "relativeFilePath");
        PreCondition.assertFalse(relativeFilePath.isRooted(), "relativeFilePath.isRooted()");

        return relativeFilePath.withoutFileExtension().normalize().toString().replaceAll("/", ".");
    }

    public static String getFullTypeName(Folder baseFolder, File file)
    {
        PreCondition.assertNotNull(baseFolder, "baseFolder");
        PreCondition.assertNotNull(file, "file");
        PreCondition.assertTrue(baseFolder.isAncestorOf(file).await(), "baseFolder.isAncestorOf(file).await()");

        final Path relativeFilePath = file.relativeTo(baseFolder);
        return JavaFile.getFullTypeName(relativeFilePath);
    }

    public static Path getRelativePathFromFullTypeName(String fullTypeName)
    {
        PreCondition.assertNotNullAndNotEmpty(fullTypeName, "fullTypeName");

        final Path result = Path.parse(fullTypeName.replaceAll("\\.", "//") + ".java");

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertFalse(result.isRooted(), "result.isRooted()");

        return result;
    }
}
