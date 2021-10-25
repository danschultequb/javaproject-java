package qub;

public class TestJSON extends JSONObjectWrapperBase
{
    private static final String javaVersionPropertyName = "javaVersion";
    private static final String classFilesPropertyName = "classFiles";

    private TestJSON(JSONObject json)
    {
        super(json);
    }

    public static TestJSON create()
    {
        return new TestJSON(JSONObject.create());
    }

    public static TestJSON create(JSONObject json)
    {
        PreCondition.assertNotNull(json, "json");

        return new TestJSON(json);
    }

    public static Result<TestJSON> parse(File parseJSONFile)
    {
        PreCondition.assertNotNull(parseJSONFile, "parseJSONFile");

        return Result.create(() ->
        {
            return TestJSON.create(JSON.parseObject(parseJSONFile).await());
        });
    }

    public static Result<TestJSON> parse(ByteReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return TestJSON.create(JSON.parseObject(readStream).await());
        });
    }

    public static Result<TestJSON> parse(CharacterReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return TestJSON.create(JSON.parseObject(readStream).await());
        });
    }

    public static Result<TestJSON> parse(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "character");

        return Result.create(() ->
        {
            return TestJSON.create(JSON.parseObject(characters).await());
        });
    }

    public VersionNumber getJavaVersion()
    {
        return this.toJson().getString(TestJSON.javaVersionPropertyName)
            .then((String javaVersionString) -> VersionNumber.parse(javaVersionString).await())
            .catchError()
            .await();
    }

    public TestJSON setJavaVersion(String javaVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(javaVersion, "javaVersion");

        this.toJson().setString(TestJSON.javaVersionPropertyName, javaVersion);

        return this;
    }

    public TestJSON setJavaVersion(VersionNumber javaVersion)
    {
        PreCondition.assertNotNull(javaVersion, "javaVersion");

        return this.setJavaVersion(javaVersion.toString());
    }

    public Iterable<TestJSONClassFile> getClassFiles()
    {
        return this.toJson().getObject(TestJSON.classFilesPropertyName)
            .then((JSONObject classFilesObject) ->
            {
                return classFilesObject.getProperties().map(TestJSONClassFile::create);
            })
            .catchError(() -> Iterable.create())
            .await();
    }

    public TestJSON setClassFiles(Iterable<TestJSONClassFile> classFiles)
    {
        PreCondition.assertNotNull(classFiles, "classFiles");

        this.toJson().setObject(TestJSON.classFilesPropertyName, JSONObject.create(classFiles.map(TestJSONClassFile::toJson)));

        return this;
    }
}
