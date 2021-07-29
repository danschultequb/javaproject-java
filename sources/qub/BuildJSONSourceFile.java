package qub;

/**
 * An individual source file referenced within a build.json file.
 */
public class BuildJSONSourceFile
{
    private static final String lastModifiedPropertyName = "lastModified";
    private static final String dependenciesPropertyName = "dependencies";
    private static final String issuesPropertyName = "issues";
    private static final String classFilesPropertyName = "classFiles";

    private final JSONProperty jsonProperty;

    private BuildJSONSourceFile(JSONProperty jsonProperty)
    {
        PreCondition.assertNotNull(jsonProperty, "jsonProperty");
        PreCondition.assertInstanceOf(jsonProperty.getValue(), JSONObject.class, "jsonProperty.getValue()");

        this.jsonProperty = jsonProperty;
    }

    public static BuildJSONSourceFile create(String sourceFileRelativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(sourceFileRelativePath, "sourceFileRelativePath");

        return BuildJSONSourceFile.create(Path.parse(sourceFileRelativePath));
    }

    public static BuildJSONSourceFile create(Path sourceFileRelativePath)
    {
        PreCondition.assertNotNull(sourceFileRelativePath, "sourceFileRelativePath");
        PreCondition.assertFalse(sourceFileRelativePath.isRooted(), "sourceFileRelativePath.isRooted()");

        final JSONProperty json = JSONProperty.create(sourceFileRelativePath.toString(), JSONObject.create());
        return new BuildJSONSourceFile(json);
    }

    /**
     * Parse a BuildJSONSourceFile from the provided JSONProperty.
     * @param sourceFileProperty The JSONProperty to parse a JSONSourceFile from.
     * @return The parsed BuildJSONSourceFile.
     */
    public static Result<BuildJSONSourceFile> parse(JSONProperty sourceFileProperty)
    {
        PreCondition.assertNotNull(sourceFileProperty, "sourceFileProperty");

        return Result.create(() ->
        {
            return new BuildJSONSourceFile(sourceFileProperty);
        });
    }

    /**
     * Get the path to the source file from the project root folder.
     * @return The path to the source file from the project root folder.
     */
    public Path getRelativePath()
    {
        return Path.parse(this.jsonProperty.getName());
    }

    private JSONObject getPropertyValue()
    {
        return this.jsonProperty.getObjectValue().await();
    }

    /**
     * Get the last time the source file was modified.
     * @return The last time the source file was modified.
     */
    public DateTime getLastModified()
    {
        final String lastModifiedString = this.getPropertyValue().getString(BuildJSONSourceFile.lastModifiedPropertyName)
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
    public BuildJSONSourceFile setLastModified(DateTime lastModified)
    {
        PreCondition.assertNotNull(lastModified, "lastModified");

        this.getPropertyValue().setString(BuildJSONSourceFile.lastModifiedPropertyName, lastModified.toString());
        return this;
    }

    /**
     * Get the relative paths to the source files that this source file depends on.
     * @return The relative paths to the source files that this source file depends on.
     */
    public Iterable<Path> getDependencies()
    {
        final JSONArray dependenciesArray = this.getPropertyValue().getArray(BuildJSONSourceFile.dependenciesPropertyName)
            .catchError()
            .await();
        List<Path> result = null;
        if (dependenciesArray != null)
        {
            result = List.create();
            for (final JSONSegment dependencySegment : dependenciesArray)
            {
                final JSONString dependency = (JSONString)dependencySegment;
                final String dependencyPathString = dependency.getValue();
                final Path dependencyPath = Path.parse(dependencyPathString);
                result.add(dependencyPath);
            }
        }
        return result;
    }

    public BuildJSONSourceFile addDependency(Path dependency)
    {
        PreCondition.assertNotNull(dependency, "dependency");
        PreCondition.assertFalse(dependency.isRooted(), "dependency.isRooted()");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray dependencies = propertyValue.getOrCreateArray(BuildJSONSourceFile.dependenciesPropertyName).await();
        dependencies.addString(dependency.normalize().toString());

        return this;
    }

    public BuildJSONSourceFile addDependencies(Iterable<Path> dependencies)
    {
        PreCondition.assertNotNull(dependencies, "dependencies");

        for (final Path dependency : dependencies)
        {
            this.addDependency(dependency);
        }

        return this;
    }

    /**
     * Add the provided issue to the source file.
     * @param issue The issue to add to the source file.
     * @return This object for method chaining.
     */
    public BuildJSONSourceFile addIssue(JavacIssue issue)
    {
        PreCondition.assertNotNull(issue, "issue");

        final JSONObject propertyValue = this.getPropertyValue();
        JSONArray issuesArray = propertyValue.getArray(BuildJSONSourceFile.issuesPropertyName)
            .catchError()
            .await();
        if (issuesArray == null)
        {
            issuesArray = JSONArray.create();
            propertyValue.setArray(BuildJSONSourceFile.issuesPropertyName, issuesArray);
        }
        issuesArray.add(issue.toJson());

        return this;
    }

    public BuildJSONSourceFile setIssues(Iterable<JavacIssue> issues)
    {
        PreCondition.assertNotNull(issues, "issues");

        final JSONObject propertyValue = this.getPropertyValue();
        propertyValue.setArray(BuildJSONSourceFile.issuesPropertyName, JSONArray.create(issues.map(JavacIssue::toJson)));

        return this;
    }

    /**
     * Get the issues that have been added to the source file.
     * @return The issues that have been added to the source file.
     */
    public Iterable<JavacIssue> getIssues()
    {
        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray issuesArray = propertyValue.getArray(BuildJSONSourceFile.issuesPropertyName)
            .catchError()
            .await();
        final Iterable<JavacIssue> result = issuesArray == null
            ? Iterable.create()
            : issuesArray
                .instanceOf(JSONObject.class)
                .map(JavacIssue::create)
                .toList();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public BuildJSONSourceFile addClassFile(Path classFileRelativePath, DateTime classFileLastModified)
    {
        PreCondition.assertNotNull(classFileRelativePath, "classFileRelativePath");
        PreCondition.assertFalse(classFileRelativePath.isRooted(), "classFileRelativePath.isRooted()");
        PreCondition.assertNotNull(classFileLastModified, "classFileLastModified");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONObject classFilesJson = propertyValue.getOrCreateObject(BuildJSONSourceFile.classFilesPropertyName).await();
        classFilesJson.setString(classFileRelativePath.normalize().toString(), classFileLastModified.toString());

        return this;
    }

    public Map<Path,DateTime> getClassFiles()
    {
        final JSONObject propertyValue = this.getPropertyValue();
        final JSONObject classFilesJson = propertyValue.getObject(BuildJSONSourceFile.classFilesPropertyName)
            .catchError(() -> JSONObject.create())
            .await();
        final Iterable<MapEntry<Path,DateTime>> classFileEntries = classFilesJson.getProperties()
            .map((JSONProperty classFileProperty) ->
            {
                final Path classFileRelativePath = Path.parse(classFileProperty.getName());
                final String classFileLastModifiedString = classFileProperty.getStringValue().catchError().await();
                final DateTime classFileLastModified = Strings.isNullOrEmpty(classFileLastModifiedString)
                    ? null
                    : DateTime.parse(classFileLastModifiedString).catchError().await();
                return MapEntry.create(classFileRelativePath, classFileLastModified);
            });
        return Map.create(classFileEntries);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof BuildJSONSourceFile && this.equals((BuildJSONSourceFile)rhs);
    }

    public boolean equals(BuildJSONSourceFile rhs)
    {
        return rhs != null &&
            Comparer.equal(this.jsonProperty, rhs.jsonProperty);
    }

    @Override
    public String toString()
    {
        return this.toString(JSONFormat.consise);
    }

    public String toString(JSONFormat format)
    {
        PreCondition.assertNotNull(format, "format");

        return this.toJson().toString(format);
    }

    public JSONProperty toJson()
    {
        return this.jsonProperty;
    }
}
