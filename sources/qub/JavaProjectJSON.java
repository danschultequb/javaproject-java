package qub;

public class JavaProjectJSON extends ProjectJSONWrapperBase<JavaProjectJSON>
{
    private static final String javaPropertyName = "java";
    private static final String mainClassPropertyName = "mainClass";
    private static final String shortcutNamePropertyName = "shortcutName";
    private static final String dependenciesPropertyName = "dependencies";

    public final static String projectSignaturePublisherPropertyName = "publisher";
    public final static String projectSignatureProjectPropertyName = "project";
    public final static String projectSignatureVersionPropertyName = "version";

    protected JavaProjectJSON(ProjectJSON projectJson)
    {
        super(projectJson);
    }

    /**
     * Create a {@link JavaProjectJSON} object around an empty {@link JSONObject}.
     * @return The {@link JavaProjectJSON} object that wraps an empty {@link JSONObject}.
     */
    static JavaProjectJSON create()
    {
        return JavaProjectJSON.create(JSONObject.create());
    }

    /**
     * Create a {@link JavaProjectJSON} object from the provided {@link JSONObject}.
     * @param json The JSON object to wrap.
     * @return The {@link JavaProjectJSON} object that wraps the provided {@link JSONObject}.
     */
    static JavaProjectJSON create(JSONObject json)
    {
        return JavaProjectJSON.create(ProjectJSON.create(json));
    }

    /**
     * Create a {@link JavaProjectJSON} object from the provided {@link ProjectJSON}.
     * @param projectJson The {@link ProjectJSON} object to wrap.
     * @return The {@link JavaProjectJSON} object that wraps the provided {@link ProjectJSON}.
     */
    static JavaProjectJSON create(ProjectJSON projectJson)
    {
        return new JavaProjectJSON(projectJson);
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided {@link File}.
     * @param projectJsonFile The {@link File} to parse.
     * @return The result of attempting to parse a project.json file.
     */
    static Result<JavaProjectJSON> parse(File projectJsonFile)
    {
        PreCondition.assertNotNull(projectJsonFile, "projectJsonFile");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(projectJsonFile).await());
        });
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided bytes.
     * @param bytes The bytes to parse.
     * @return The result of attempting to parse a {@link JavaProjectJSON} object.
     */
    static Result<ProjectJSON> parse(ByteReadStream bytes)
    {
        PreCondition.assertNotNull(bytes, "bytes");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(bytes).await());
        });
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided characters.
     * @param characters The characters to parse.
     * @return The result of attempting to parse a {@link JavaProjectJSON} object.
     */
    static Result<ProjectJSON> parse(CharacterReadStream characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(characters).await());
        });
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided text.
     * @param text The text to parse.
     * @return The result of attempting to parse a {@link JavaProjectJSON} object.
     */
    static Result<ProjectJSON> parse(String text)
    {
        PreCondition.assertNotNull(text, "text");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(text).await());
        });
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided characters.
     * @param characters The characters to parse.
     * @return The result of attempting to parse a {@link JavaProjectJSON} object.
     */
    static Result<ProjectJSON> parse(Iterable<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(characters).await());
        });
    }

    /**
     * Parse a {@link JavaProjectJSON} object from the provided characters.
     * @param characters The characters to parse.
     * @return The result of attempting to parse a {@link JavaProjectJSON} object.
     */
    static Result<ProjectJSON> parse(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            return JavaProjectJSON.create(ProjectJSON.parse(characters).await());
        });
    }

    private Result<JSONObject> getJava()
    {
        return this.toJson().getObject(JavaProjectJSON.javaPropertyName);
    }

    private Result<JSONObject> getOrCreateJava()
    {
        return this.toJson().getOrCreateObject(JavaProjectJSON.javaPropertyName);
    }

    private String getJavaString(String javaPropertyName)
    {
        PreCondition.assertNotNullAndNotEmpty(javaPropertyName, "javaPropertyName");

        return this.getJava()
            .then((JSONObject java) -> java.getString(javaPropertyName).await())
            .catchError()
            .await();
    }

    private JSONArray getJavaArray(String javaPropertyName)
    {
        PreCondition.assertNotNullAndNotEmpty(javaPropertyName, "javaPropertyName");

        return this.getJava()
            .then((JSONObject java) -> java.getArray(javaPropertyName).await())
            .catchError()
            .await();
    }

    private JavaProjectJSON setJavaString(String javaPropertyName, String javaPropertyValue)
    {
        PreCondition.assertNotNullAndNotEmpty(javaPropertyName, "javaPropertyName");
        PreCondition.assertNotNullAndNotEmpty(javaPropertyValue, "javaPropertyValue");

        this.getOrCreateJava()
            .then((JSONObject java) -> java.setString(javaPropertyName, javaPropertyValue))
            .await();
        return this;
    }

    private JavaProjectJSON setJavaArray(String javaPropertyName, JSONArray javaPropertyValue)
    {
        PreCondition.assertNotNullAndNotEmpty(javaPropertyName, "javaPropertyName");
        PreCondition.assertNotNull(javaPropertyValue, "javaPropertyValue");

        this.getOrCreateJava()
            .then((JSONObject java) -> java.setArray(javaPropertyName, javaPropertyValue))
            .await();
        return this;
    }

    /**
     * Get the name of the type whose main method will be invoked when this project is run.
     * @return The name of the type whose main method will be invoked when this project is run.
     */
    public String getMainClass()
    {
        return this.getJavaString(JavaProjectJSON.mainClassPropertyName);
    }

    /**
     * Set the name of the type whose main method will be invoked when this project is run.
     * @param mainClass The name of the type whose main method will be invoked when this project
     *                  is run.
     * @return This object for method chaining.
     */
    public JavaProjectJSON setMainClass(String mainClass)
    {
        PreCondition.assertNotNullAndNotEmpty(mainClass, "mainClass");

        return this.setJavaString(JavaProjectJSON.mainClassPropertyName, mainClass);
    }

    /**
     * Get the name of the batch file that will be created in the root of the Qub folder.
     * @return The name of the batch file that will be created in the root of the Qub folder.
     */
    public String getShortcutName()
    {
        return this.getJavaString(JavaProjectJSON.shortcutNamePropertyName);
    }

    /**
     * Set the name of the batch file that will be created in the root of the Qub folder.
     * @param shortcutName The name of the batch file that will be created in the root of the Qub
     *                     folder.
     * @return This object for method chaining.
     */
    public JavaProjectJSON setShortcutName(String shortcutName)
    {
        PreCondition.assertNotNullAndNotEmpty(shortcutName, "shortcutName");

        return this.setJavaString(JavaProjectJSON.shortcutNamePropertyName, shortcutName);
    }

    /**
     * Set the dependencies that this project depends on. This project will implicitly also depend
     * on all the projects that these dependencies depend on.
     * @return The dependencies that this project depends on.
     */
    public Iterable<ProjectSignature> getDependencies()
    {
        final List<ProjectSignature> result = List.create();

        final JSONArray dependencies = this.getJavaArray(JavaProjectJSON.dependenciesPropertyName);
        if (dependencies != null)
        {
            for (final JSONObject dependencyJson : dependencies.instanceOf(JSONObject.class))
            {
                final ProjectSignature dependency = JavaProjectJSON.parseProjectSignature(dependencyJson).catchError().await();
                if (dependency != null)
                {
                    result.add(dependency);
                }
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public JavaProjectJSON setDependencies(Iterable<ProjectSignature> dependencies)
    {
        PreCondition.assertNotNull(dependencies, "dependencies");

        return this.setJavaArray(JavaProjectJSON.dependenciesPropertyName, JSONArray.create(dependencies
            .map(JavaProjectJSON::projectSignatureToJson)));
    }

    /**
     * Get the published project folders of the dependencies found in this project's dependency
     * graph.
     * @param qubFolder The {@link QubFolder} to look for dependencies in.
     * @return The published project folders of the dependencies found in this project's dependency
     * graph.
     */
    public Result<Iterable<JavaPublishedProjectFolder>> getAllDependencyFolders(QubFolder qubFolder)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        return qubFolder.getAllDependencyFolders(
            this.getDependencies(),
            (QubProjectVersionFolder projectVersionFolder) ->
            {
                return Result.create(() ->
                {
                    final JavaPublishedProjectFolder javaProjectVersionFolder = JavaPublishedProjectFolder.get(projectVersionFolder);
                    final JavaProjectJSON dependencyProjectJson = javaProjectVersionFolder.getProjectJson().await();
                    return dependencyProjectJson.getDependencies();
                });
            },
            true)
            .then((Iterable<QubProjectVersionFolder> projectVersionFolders) ->
            {
                return projectVersionFolders.map(JavaPublishedProjectFolder::get);
            });
    }

    public static JSONObject projectSignatureToJson(ProjectSignature projectSignature)
    {
        PreCondition.assertNotNull(projectSignature, "projectSignature");

        return JSONObject.create()
            .setString(JavaProjectJSON.projectSignaturePublisherPropertyName, projectSignature.getPublisher())
            .setString(JavaProjectJSON.projectSignatureProjectPropertyName, projectSignature.getProject())
            .setString(JavaProjectJSON.projectSignatureVersionPropertyName, projectSignature.getVersion().toString());
    }

    public static Result<ProjectSignature> parseProjectSignature(JSONObject jsonObject)
    {
        PreCondition.assertNotNull(jsonObject, "jsonObject");

        return Result.create(() ->
        {
            final String publisher = jsonObject.getString(JavaProjectJSON.projectSignaturePublisherPropertyName).await();
            final String project = jsonObject.getString(JavaProjectJSON.projectSignatureProjectPropertyName).await();
            final String version = jsonObject.getString(JavaProjectJSON.projectSignatureVersionPropertyName).await();
            return ProjectSignature.create(publisher, project, version);
        });
    }
}
