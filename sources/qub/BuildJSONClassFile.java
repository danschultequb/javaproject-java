package qub;

public class BuildJSONClassFile
{
    private final Path relativePath;
    private final DateTime lastModified;

    private BuildJSONClassFile(Path relativePath, DateTime lastModified)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertFalse(relativePath.isRooted(), "relativePath.isRooted()");
        PreCondition.assertNotNull(lastModified, "lastModified");

        this.relativePath = relativePath;
        this.lastModified = lastModified;
    }

    public static BuildJSONClassFile create(Path relativePath, DateTime lastModified)
    {
        return new BuildJSONClassFile(relativePath, lastModified);
    }

    public Path getRelativePath()
    {
        return this.relativePath;
    }

    public DateTime getLastModified()
    {
        return this.lastModified;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof BuildJSONClassFile && this.equals((BuildJSONClassFile)rhs);
    }

    public boolean equals(BuildJSONClassFile rhs)
    {
        return rhs != null &&
            this.relativePath.equals(rhs.relativePath) &&
            this.lastModified.equals(rhs.lastModified);
    }
}
