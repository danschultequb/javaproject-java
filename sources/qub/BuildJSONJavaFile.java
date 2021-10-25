package qub;

/**
 * An individual .java file referenced within a build.json file.
 */
public class BuildJSONJavaFile extends JSONPropertyWrapperBase
{
    private static final String lastModifiedPropertyName = "lastModified";
    private static final String dependenciesPropertyName = "dependencies";
    private static final String issuesPropertyName = "issues";
    private static final String classFilesPropertyName = "classFiles";

    private BuildJSONJavaFile(JSONProperty jsonProperty)
    {
        super(jsonProperty);
    }

    public static BuildJSONJavaFile create(String sourceFileRelativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(sourceFileRelativePath, "sourceFileRelativePath");

        return BuildJSONJavaFile.create(Path.parse(sourceFileRelativePath));
    }

    public static BuildJSONJavaFile create(Path sourceFileRelativePath)
    {
        PreCondition.assertNotNull(sourceFileRelativePath, "sourceFileRelativePath");
        PreCondition.assertFalse(sourceFileRelativePath.isRooted(), "sourceFileRelativePath.isRooted()");

        final JSONProperty json = JSONProperty.create(sourceFileRelativePath.toString(), JSONObject.create());
        return BuildJSONJavaFile.create(json).await();
    }

    /**
     * Create a {@link BuildJSONJavaFile} from the provided {@link JSONProperty}.
     * @param sourceFileProperty The {@link JSONProperty} to create a {@link BuildJSONJavaFile} from.
     * @return The created {@link BuildJSONJavaFile}.
     */
    public static Result<BuildJSONJavaFile> create(JSONProperty sourceFileProperty)
    {
        PreCondition.assertNotNull(sourceFileProperty, "sourceFileProperty");

        return Result.create(() ->
        {
            final Path sourceFileRelativePath = Path.parse(sourceFileProperty.getName());
            if (sourceFileRelativePath.isRooted())
            {
                throw new java.lang.IllegalArgumentException("sourceFileProperty.getName() cannot be an absolute file path.");
            }

            final JSONSegment propertyValue = sourceFileProperty.getValue();
            if (!Types.instanceOf(propertyValue, JSONObject.class))
            {
                throw new java.lang.IllegalArgumentException("sourceFileProperty.getValue() must be a JSONObject.");
            }

            return new BuildJSONJavaFile(sourceFileProperty);
        });
    }

    /**
     * Get the path to the .java file from the project root folder.
     * @return The path to the .java file from the project root folder.
     */
    public Path getRelativePath()
    {
        return Path.parse(this.toJson().getName());
    }

    private JSONObject getPropertyValue()
    {
        return this.toJson().getObjectValue().await();
    }

    /**
     * Get the last time the .java file was modified.
     * @return The last time the source file was modified.
     */
    public DateTime getLastModified()
    {
        final String lastModifiedString = this.getPropertyValue().getString(BuildJSONJavaFile.lastModifiedPropertyName)
            .catchError()
            .await();
        return Strings.isNullOrEmpty(lastModifiedString)
            ? null
            : DateTime.parse(lastModifiedString).catchError().await();
    }

    /**
     * Set the last time the source file was modified.
     * @param lastModified The last time the source file was modified.
     */
    public BuildJSONJavaFile setLastModified(DateTime lastModified)
    {
        PreCondition.assertNotNull(lastModified, "lastModified");

        this.getPropertyValue().setString(BuildJSONJavaFile.lastModifiedPropertyName, lastModified.toString());
        return this;
    }

    /**
     * Get the relative paths to the source files that this source file depends on.
     * @return The relative paths to the source files that this source file depends on.
     */
    public Iterable<Path> getDependencies()
    {
        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray dependenciesArray = propertyValue.getArray(BuildJSONJavaFile.dependenciesPropertyName)
            .catchError()
            .await();
        return (dependenciesArray == null)
            ? Iterable.create()
            : dependenciesArray
                .instanceOf(JSONString.class)
                .map((JSONString dependency) -> Path.parse(dependency.getValue()))
                .toList();
    }

    public BuildJSONJavaFile setDependencies(Iterable<Path> dependencies)
    {
        PreCondition.assertNotNull(dependencies, "dependencies");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray dependenciesArray = JSONArray.create(dependencies.map((Path dependency) -> JSONString.get(dependency.toString())));
        propertyValue.setArray(BuildJSONJavaFile.dependenciesPropertyName, dependenciesArray);

        return this;
    }

    /**
     * Get the issues that have been added to the source file.
     * @return The issues that have been added to the source file.
     */
    public Iterable<JavacIssue> getIssues()
    {
        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray issuesArray = propertyValue.getArray(BuildJSONJavaFile.issuesPropertyName)
            .catchError()
            .await();
        return (issuesArray == null)
            ? Iterable.create()
            : issuesArray
                .instanceOf(JSONObject.class)
                .map(JavacIssue::create)
                .toList();
    }

    public BuildJSONJavaFile setIssues(Iterable<JavacIssue> issues)
    {
        PreCondition.assertNotNull(issues, "issues");

        final JSONObject propertyValue = this.getPropertyValue();
        propertyValue.setArray(BuildJSONJavaFile.issuesPropertyName, JSONArray.create(issues.map(JavacIssue::toJson)));

        return this;
    }

    public Iterable<BuildJSONClassFile> getClassFiles()
    {
        final JSONObject propertyValue = this.getPropertyValue();
        final JSONObject classFilesJson = propertyValue.getObject(BuildJSONJavaFile.classFilesPropertyName)
            .catchError(() -> JSONObject.create())
            .await();
        return classFilesJson.getProperties()
            .map((JSONProperty classFileProperty) ->
            {
                final Path classFileRelativePath = Path.parse(classFileProperty.getName());
                final String classFileLastModifiedString = classFileProperty.getStringValue().catchError().await();
                final DateTime classFileLastModified = Strings.isNullOrEmpty(classFileLastModifiedString)
                    ? null
                    : DateTime.parse(classFileLastModifiedString).catchError().await();
                return classFileLastModified == null
                    ? null
                    : BuildJSONClassFile.create(classFileRelativePath, classFileLastModified);
            })
            .where(value -> value != null)
            .toList();
    }

    public BuildJSONJavaFile setClassFiles(Iterable<BuildJSONClassFile> classFiles)
    {
        PreCondition.assertNotNull(classFiles, "classFiles");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONObject classFilesJson = propertyValue.getOrCreateObject(BuildJSONJavaFile.classFilesPropertyName).await();
        classFilesJson.setAll(classFiles.map(BuildJSONClassFile::toJson));

        return this;
    }
}
