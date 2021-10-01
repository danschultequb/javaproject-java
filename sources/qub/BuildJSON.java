package qub;

public class BuildJSON extends JSONObjectWrapperBase
{
    private static final String projectJsonPropertyName = "project.json";
    private static final String javacVersionPropertyName = "javacVersion";
    private static final String javaFilesPropertyName = "javaFiles";

    private BuildJSON(JSONObject json)
    {
        super(json);
    }

    public static BuildJSON create()
    {
        return new BuildJSON(JSONObject.create());
    }

    public static Result<BuildJSON> parse(File parseJSONFile)
    {
        PreCondition.assertNotNull(parseJSONFile, "parseJSONFile");

        return Result.create(() ->
        {
            return BuildJSON.parse(JSON.parseObject(parseJSONFile).await()).await();
        });
    }

    public static Result<BuildJSON> parse(ByteReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return BuildJSON.parse(JSON.parseObject(readStream).await()).await();
        });
    }

    public static Result<BuildJSON> parse(CharacterReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream");

        return Result.create(() ->
        {
            return BuildJSON.parse(JSON.parseObject(readStream).await()).await();
        });
    }

    public static Result<BuildJSON> parse(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "character");

        return Result.create(() ->
        {
            return BuildJSON.parse(JSON.parseObject(characters).await()).await();
        });
    }

    public static Result<BuildJSON> parse(JSONObject json)
    {
        PreCondition.assertNotNull(json, "json");

        return Result.create(() ->
        {
            return new BuildJSON(json);
        });
    }

    public BuildJSON setProjectJson(ProjectJSON projectJson)
    {
        this.toJson().setObjectOrNull(BuildJSON.projectJsonPropertyName, projectJson == null ? null : projectJson.toJson());
        return this;
    }

    public JavaProjectJSON getProjectJson()
    {
        final JSONObject projectJson = this.toJson().getObject(BuildJSON.projectJsonPropertyName)
            .catchError()
            .await();
        return projectJson == null ? null : JavaProjectJSON.create(projectJson);
    }

    public BuildJSON setJavacVersion(String javacVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(javacVersion, "javacVersion");

        this.toJson().setString(BuildJSON.javacVersionPropertyName, javacVersion);
        return this;
    }

    public BuildJSON setJavacVersion(VersionNumber javacVersion)
    {
        PreCondition.assertNotNull(javacVersion, "javacVersion");

        return this.setJavacVersion(javacVersion.toString());
    }

    public VersionNumber getJavacVersion()
    {
        return this.toJson().getString(BuildJSON.javacVersionPropertyName)
            .then((String javacVersionString) -> VersionNumber.parse(javacVersionString).await())
            .catchError()
            .await();
    }

    public Iterable<BuildJSONJavaFile> getJavaFiles()
    {
        final JSONObject sourceFilesJson = this.toJson().getObject(BuildJSON.javaFilesPropertyName)
            .catchError(() -> JSONObject.create())
            .await();
        return sourceFilesJson.getProperties()
            .map((JSONProperty property) -> BuildJSONJavaFile.parse(property).await())
            .toList();
    }

    public BuildJSON setJavaFile(BuildJSONJavaFile javaFile)
    {
        PreCondition.assertNotNull(javaFile, "javaFile");

        this.toJson().getOrCreateObject(BuildJSON.javaFilesPropertyName).await()
            .set(javaFile.toJson());

        return this;
    }

    public BuildJSON setJavaFiles(Iterable<BuildJSONJavaFile> javaFiles)
    {
        PreCondition.assertNotNull(javaFiles, "javaFiles");

        for (final BuildJSONJavaFile javaFile : javaFiles)
        {
            this.setJavaFile(javaFile);
        }

        return this;
    }

    /**
     * Get the BuildJSONSourceFile that matches the provided relative path. The path should be
     * relative to the project folder.
     * @param relativePath The path to the source file. This should be relative to the project
     *                     folder.
     * @return The BuildJSONSourceFile that is associated with the provided relative path.
     */
    public Result<BuildJSONJavaFile> getJavaFile(String relativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(relativePath, "relativePath");

        return this.getJavaFile(Path.parse(relativePath));
    }

    /**
     * Get the BuildJSONSourceFile that matches the provided relative path. The path should be
     * relative to the project folder.
     * @param relativePath The path to the source file. This should be relative to the project
     *                     folder.
     * @return The BuildJSONSourceFile that is associated with the provided relative path.
     */
    public Result<BuildJSONJavaFile> getJavaFile(Path relativePath)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertFalse(relativePath.isRooted(), "relativePath.isRooted()");

        return Result.create(() ->
        {
            BuildJSONJavaFile result = null;
            final Iterable<BuildJSONJavaFile> javaFiles = this.getJavaFiles();
            if (!Iterable.isNullOrEmpty(javaFiles))
            {
                result = javaFiles.first((BuildJSONJavaFile javaFile) -> javaFile.getRelativePath().equals(relativePath));
            }
            if (result == null)
            {
                throw new NotFoundException("No .java file found in the BuildJSON object with the path " + Strings.escapeAndQuote(relativePath.toString()) + ".");
            }
            return result;
        });
    }
}
