package qub;

public class BuildJSONClassFile extends JSONPropertyWrapperBase
{
    private BuildJSONClassFile(JSONProperty innerProperty)
    {
        super(innerProperty);
    }

    public static BuildJSONClassFile create(JSONProperty innerProperty)
    {
        return new BuildJSONClassFile(innerProperty);
    }

    public static BuildJSONClassFile create(Path relativePath, DateTime lastModified)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertNotNull(lastModified, "lastModified");

        return BuildJSONClassFile.create(JSONProperty.create(relativePath.toString(), lastModified.toString()));
    }

    public Path getRelativePath()
    {
        return Path.parse(this.toJson().getName());
    }

    public DateTime getLastModified()
    {
        return this.toJson().getStringValue()
            .then((String lastModifiedString) -> DateTime.parse(lastModifiedString).await())
            .catchError()
            .await();
    }
}
