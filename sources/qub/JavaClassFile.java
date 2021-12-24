package qub;

public class JavaClassFile extends File
{
    private Result<DateTime> lastModifiedResult;

    private JavaClassFile(File file)
    {
        super(file.getFileSystem(), file.getPath());
    }

    public static JavaClassFile get(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return new JavaClassFile(file);
    }

    public static JavaClassFile getFromFullTypeName(Folder outputFolder, String fullTypeName)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNullAndNotEmpty(fullTypeName, "fullTypeName");

        final Path relativeFilePath = JavaClassFile.getRelativePathFromFullTypeName(fullTypeName);
        final File file = outputFolder.getFile(relativeFilePath).await();
        return JavaClassFile.get(file);
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

        return JavaFile.getFullTypeName(relativeFilePath);
    }

    public static String getFullTypeName(Folder baseFolder, File file)
    {
        PreCondition.assertNotNull(baseFolder, "baseFolder");
        PreCondition.assertNotNull(file, "file");
        PreCondition.assertTrue(baseFolder.isAncestorOf(file).await(), "baseFolder.isAncestorOf(file).await()");

        return JavaFile.getFullTypeName(baseFolder, file);
    }

    public static Path getRelativePathFromFullTypeName(String fullTypeName)
    {
        PreCondition.assertNotNullAndNotEmpty(fullTypeName, "fullTypeName");

        final Path result = Path.parse(fullTypeName.replaceAll("\\.", "//") + ".class");

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertFalse(result.isRooted(), "result.isRooted()");

        return result;
    }
}
