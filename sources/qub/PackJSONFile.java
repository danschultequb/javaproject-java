package qub;

public class PackJSONFile extends JSONPropertyWrapperBase
{
    protected PackJSONFile(JSONProperty innerProperty)
    {
        super(innerProperty);
    }

    public static PackJSONFile create(String fileRelativePath, DateTime lastModified)
    {
        PreCondition.assertNotNullAndNotEmpty(fileRelativePath, "fileRelativePath");
        PreCondition.assertNotNull(lastModified, "lastModified");

        return PackJSONFile.create(Path.parse(fileRelativePath), lastModified);
    }

    public static PackJSONFile create(Path fileRelativePath, DateTime lastModified)
    {
        PreCondition.assertNotNull(fileRelativePath, "fileRelativePath");
        PreCondition.assertFalse(fileRelativePath.isRooted(), "fileRelativePath.isRooted()");
        PreCondition.assertFalse(fileRelativePath.endsWith('/'), "fileRelativePath.endsWith('/')");
        PreCondition.assertFalse(fileRelativePath.endsWith('\\'), "fileRelativePath.endsWith('\\')");
        PreCondition.assertNotNull(lastModified, "lastModified");

        return PackJSONFile.create(JSONProperty.create(fileRelativePath.toString(), lastModified.toString()));
    }

    public static PackJSONFile create(JSONProperty json)
    {
        PreCondition.assertNotNull(json, "json");

        return new PackJSONFile(json);
    }

    public Path getRelativePath()
    {
        return Path.parse(this.toJson().getName());
    }

    public DateTime getLastModified()
    {
        final String lastModifiedString = this.toJson().getStringValue().catchError().await();
        return Strings.isNullOrEmpty(lastModifiedString)
            ? null
            : DateTime.parse(lastModifiedString).catchError().await();
    }
}
