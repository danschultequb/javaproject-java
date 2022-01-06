package qub;

public class PackJSON extends JSONObjectWrapperBase
{
    private static final String jarVersionPropertyName = "jarVersion";
    private static final String projectPropertyName = "project";
    private static final String sourceFilesPropertyName = "sourceFiles";
    private static final String sourceOutputFilesPropertyName = "sourceOutputFiles";
    private static final String testSourceFilesPropertyName = "testSourceFiles";
    private static final String testOutputFilesPropertyName = "testOutputFiles";

    protected PackJSON(JSONObject json)
    {
        super(json);
    }

    public static PackJSON create()
    {
        return PackJSON.create(JSONObject.create());
    }

    public static PackJSON create(JSONObject json)
    {
        PreCondition.assertNotNull(json, "json");

        return new PackJSON(json);
    }

    public static Result<PackJSON> parse(File packJsonFile)
    {
        PreCondition.assertNotNull(packJsonFile, "packJsonFile");

        return Result.create(() ->
        {
            return PackJSON.create(JSON.parseObject(packJsonFile).await());
        });
    }

    public static Result<PackJSON> parse(ByteReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return PackJSON.create(JSON.parseObject(readStream).await());
        });
    }

    public static Result<PackJSON> parse(CharacterReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return PackJSON.create(JSON.parseObject(readStream).await());
        });
    }

    public static Result<PackJSON> parse(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            return PackJSON.create(JSON.parseObject(characters).await());
        });
    }

    public PackJSON setJarVersion(VersionNumber jarVersion)
    {
        PreCondition.assertNotNull(jarVersion, "jarVersion");

        return this.setJarVersion(jarVersion.toString());
    }

    public PackJSON setJarVersion(String jarVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(jarVersion, "jarVersion");

        this.toJson().setString(PackJSON.jarVersionPropertyName, jarVersion);

        return this;
    }

    public VersionNumber getJarVersion()
    {
        return this.toJson().getString(PackJSON.jarVersionPropertyName)
            .then((String javaVersionString) -> VersionNumber.parse(javaVersionString).await())
            .catchError()
            .await();
    }

    public PackJSON setProject(String project)
    {
        PreCondition.assertNotNullAndNotEmpty(project, "project");

        this.toJson().setString(PackJSON.projectPropertyName, project);

        return this;
    }

    public String getProject()
    {
        return this.toJson().getString(PackJSON.projectPropertyName)
            .catchError()
            .await();
    }

    public PackJSON setSourceFiles(Iterable<PackJSONFile> sourceFiles)
    {
        PreCondition.assertNotNull(sourceFiles, "sourceFiles");

        this.toJson().setObject(PackJSON.sourceFilesPropertyName, JSONObject.create(sourceFiles.map(PackJSONFile::toJson)));

        return this;
    }

    public Iterable<PackJSONFile> getSourceFiles()
    {
        return this.toJson().getObject(PackJSON.sourceFilesPropertyName)
            .then((JSONObject sourceFilesJson) -> sourceFilesJson.getProperties().map(PackJSONFile::create))
            .catchError(() -> Iterable.create())
            .await();
    }

    public PackJSON setSourceOutputFiles(Iterable<PackJSONFile> sourceOutputFiles)
    {
        PreCondition.assertNotNull(sourceOutputFiles, "sourceOutputFiles");

        this.toJson().setObject(PackJSON.sourceOutputFilesPropertyName, JSONObject.create(sourceOutputFiles.map(PackJSONFile::toJson)));

        return this;
    }

    public Iterable<PackJSONFile> getSourceOutputFiles()
    {
        return this.toJson().getObject(PackJSON.sourceOutputFilesPropertyName)
            .then((JSONObject sourceOutputFilesJson) -> sourceOutputFilesJson.getProperties().map(PackJSONFile::create))
            .catchError(() -> Iterable.create())
            .await();
    }

    public PackJSON setTestSourceFiles(Iterable<PackJSONFile> testSourceFiles)
    {
        PreCondition.assertNotNull(testSourceFiles, "testSourceFiles");

        this.toJson().setObject(PackJSON.testSourceFilesPropertyName, JSONObject.create(testSourceFiles.map(PackJSONFile::toJson)));

        return this;
    }

    public Iterable<PackJSONFile> getTestSourceFiles()
    {
        return this.toJson().getObject(PackJSON.testSourceFilesPropertyName)
            .then((JSONObject testSourceFilesJson) -> testSourceFilesJson.getProperties().map(PackJSONFile::create))
            .catchError(() -> Iterable.create())
            .await();
    }

    public PackJSON setTestOutputFiles(Iterable<PackJSONFile> testOutputFiles)
    {
        PreCondition.assertNotNull(testOutputFiles, "testOutputFiles");

        this.toJson().setObject(PackJSON.testOutputFilesPropertyName, JSONObject.create(testOutputFiles.map(PackJSONFile::toJson)));

        return this;
    }

    public Iterable<PackJSONFile> getTestOutputFiles()
    {
        return this.toJson().getObject(PackJSON.testOutputFilesPropertyName)
            .then((JSONObject testOutputFilesJson) -> testOutputFilesJson.getProperties().map(PackJSONFile::create))
            .catchError(() -> Iterable.create())
            .await();
    }
}
