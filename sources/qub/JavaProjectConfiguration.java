package qub;

public class JavaProjectConfiguration extends JSONObjectWrapperBase
{
    private static final String configurationFileName = "configuration.json";

    private static final String ignoredStackTraceTypesPropertyName = "ignoredStackTraceTypes";

    private JavaProjectConfiguration(JSONObject json)
    {
        super(json);
    }

    public static JavaProjectConfiguration create()
    {
        return JavaProjectConfiguration.create(JSONObject.create());
    }

    public static JavaProjectConfiguration create(JSONObject json)
    {
        return new JavaProjectConfiguration(json);
    }

    public static Result<File> getConfigurationFile(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return Result.create(() ->
        {
            return process.getQubProjectDataFolder().await()
                .getFile(JavaProjectConfiguration.configurationFileName).await();
        });
    }

    public static Result<Void> setConfigurationFile(DesktopProcess process, JavaProjectConfiguration configuration)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(configuration, "configuration");

        return Result.create(() ->
        {
            final File configurationFile = JavaProjectConfiguration.getConfigurationFile(process).await();
            configurationFile.setContentsAsString(configuration.toString(JSONFormat.pretty)).await();
        });
    }

    public static Result<JavaProjectConfiguration> parse(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return Result.create(() ->
        {
            final File configurationFile = JavaProjectConfiguration.getConfigurationFile(process).await();
            return JavaProjectConfiguration.parse(configurationFile).await();
        });
    }

    public static Result<JavaProjectConfiguration> parse(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return Result.create(() ->
        {
            final JSONObject json = JSON.parseObject(file).await();
            return JavaProjectConfiguration.create(json);
        });
    }

    public Iterable<String> getIgnoredStackTraceTypes()
    {
        return this.toJson().getArray(JavaProjectConfiguration.ignoredStackTraceTypesPropertyName)
            .catchError(() -> JSONArray.create())
            .await()
            .instanceOf(JSONString.class)
            .map(JSONString::getValue)
            .toList();
    }

    public JavaProjectConfiguration setIgnoredStackTraceTypes(Iterable<String> ignoredStackTraceTypes)
    {
        PreCondition.assertNotNull(ignoredStackTraceTypes, "ignoredStackTraceTypes");

        this.toJson().setArray(JavaProjectConfiguration.ignoredStackTraceTypesPropertyName, JSONArray.create(ignoredStackTraceTypes.map(JSONString::get)));

        return this;
    }

    public static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        final String configurationSchemaJsonFilePath = "./configuration.schema.json";

        return CommandLineConfigurationAction.addAction(actions, CommandLineConfigurationActionParameters.create()
            .setConfigurationFileRelativePath(JavaProjectConfiguration.configurationFileName)
            .setConfigurationSchemaFileRelativePath(configurationSchemaJsonFilePath)
            .setDefaultConfiguration(JSONObject.create()
                .setString("$schema", configurationSchemaJsonFilePath)
            )
            .setConfigurationSchema(JSONSchema.create()
                .setSchema("http://json-schema.org/draft-04/schema")
                .setType(JSONSchemaType.Object)
                .addProperty(JSONSchema.schemaPropertyName, JSONSchema.create()
                    .setDescription("The schema that defines how a java project configuration file should be structured.")
                    .setEnum(configurationSchemaJsonFilePath)
                )
                .addProperty(JavaProjectConfiguration.ignoredStackTraceTypesPropertyName, JSONSchema.create()
                    .setDescription("The types whose stack trace elements will be omitted when displaying stack traces.")
                    .setType(JSONSchemaType.Array)
                    .setItems(JSONSchema.create()
                        .setDescription("A type whose stack trace elements will be omitted when displaying stack traces.")
                        .setType(JSONSchemaType.String)
                        .setMinLength(1)
                    )
                )
            ));
    }
}