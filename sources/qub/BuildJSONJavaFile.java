package qub;

import java.util.Objects;

/**
 * An individual .java file referenced within a build.json file.
 */
public class BuildJSONJavaFile
{
    private static final String lastModifiedPropertyName = "lastModified";
    private static final String dependenciesPropertyName = "dependencies";
    private static final String issuesPropertyName = "issues";
    private static final String classFilesPropertyName = "classFiles";

    private final JSONProperty jsonProperty;

    private BuildJSONJavaFile(JSONProperty jsonProperty)
    {
        PreCondition.assertNotNull(jsonProperty, "jsonProperty");
        PreCondition.assertInstanceOf(jsonProperty.getValue(), JSONObject.class, "jsonProperty.getValue()");

        this.jsonProperty = jsonProperty;
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
        return new BuildJSONJavaFile(json);
    }

    /**
     * Parse a BuildJSONSourceFile from the provided JSONProperty.
     * @param sourceFileProperty The JSONProperty to parse a JSONSourceFile from.
     * @return The parsed BuildJSONSourceFile.
     */
    public static Result<BuildJSONJavaFile> parse(JSONProperty sourceFileProperty)
    {
        PreCondition.assertNotNull(sourceFileProperty, "sourceFileProperty");

        return Result.create(() ->
        {
            return new BuildJSONJavaFile(sourceFileProperty);
        });
    }

    /**
     * Get the path to the .java file from the project root folder.
     * @return The path to the .java file from the project root folder.
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
        final JSONArray dependenciesArray = this.getPropertyValue().getArray(BuildJSONJavaFile.dependenciesPropertyName)
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

    public BuildJSONJavaFile addDependency(Path dependency)
    {
        PreCondition.assertNotNull(dependency, "dependency");
        PreCondition.assertFalse(dependency.isRooted(), "dependency.isRooted()");
        PreCondition.assertEqual(".java", dependency.getFileExtension(), "dependency.getFileExtension()");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONArray dependencies = propertyValue.getOrCreateArray(BuildJSONJavaFile.dependenciesPropertyName).await();
        dependencies.addString(dependency.normalize().toString());

        return this;
    }

    public BuildJSONJavaFile addDependencies(Iterable<Path> dependencies)
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
    public BuildJSONJavaFile addIssue(JavacIssue issue)
    {
        PreCondition.assertNotNull(issue, "issue");

        final JSONObject propertyValue = this.getPropertyValue();
        JSONArray issuesArray = propertyValue.getArray(BuildJSONJavaFile.issuesPropertyName)
            .catchError()
            .await();
        if (issuesArray == null)
        {
            issuesArray = JSONArray.create();
            propertyValue.setArray(BuildJSONJavaFile.issuesPropertyName, issuesArray);
        }
        issuesArray.add(issue.toJson());

        return this;
    }

    public BuildJSONJavaFile setIssues(Iterable<JavacIssue> issues)
    {
        PreCondition.assertNotNull(issues, "issues");

        final JSONObject propertyValue = this.getPropertyValue();
        propertyValue.setArray(BuildJSONJavaFile.issuesPropertyName, JSONArray.create(issues.map(JavacIssue::toJson)));

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
        final Iterable<JavacIssue> result = issuesArray == null
            ? Iterable.create()
            : issuesArray
                .instanceOf(JSONObject.class)
                .map(JavacIssue::create)
                .toList();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public BuildJSONJavaFile addClassFile(Path classFileRelativePath, DateTime classFileLastModified)
    {
        PreCondition.assertNotNull(classFileRelativePath, "classFileRelativePath");
        PreCondition.assertFalse(classFileRelativePath.isRooted(), "classFileRelativePath.isRooted()");
        PreCondition.assertEqual(".class", classFileRelativePath.getFileExtension(), "classFileRelativePath.getFileExtension()");
        PreCondition.assertNotNull(classFileLastModified, "classFileLastModified");

        final JSONObject propertyValue = this.getPropertyValue();
        final JSONObject classFilesJson = propertyValue.getOrCreateObject(BuildJSONJavaFile.classFilesPropertyName).await();
        classFilesJson.setString(classFileRelativePath.normalize().toString(), classFileLastModified.toString());

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
            .where(Objects::nonNull)
            .toList();
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof BuildJSONJavaFile && this.equals((BuildJSONJavaFile)rhs);
    }

    public boolean equals(BuildJSONJavaFile rhs)
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
