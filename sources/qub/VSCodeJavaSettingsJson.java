package qub;

public class VSCodeJavaSettingsJson extends VSCodeSettingsJson
{
    private static final String javaProjectSourcePathsPropertyName = "java.project.sourcePaths";
    private static final String javaProjectReferencedLibrariesPropertyName = "java.project.referencedLibraries";
    private static final String javaFormatSettingsUrlPropertyName = "java.format.settings.url";
    private static final String javaFormatEnabledPropertyName = "java.format.enabled";

    private VSCodeJavaSettingsJson(JSONObject json)
    {
        super(json);
    }

    public static VSCodeJavaSettingsJson create()
    {
        return VSCodeJavaSettingsJson.create(JSONObject.create());
    }

    public static VSCodeJavaSettingsJson create(JSONObject json)
    {
        return new VSCodeJavaSettingsJson(json);
    }

    public static Result<VSCodeJavaSettingsJson> parse(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return Result.create(() ->
        {
            return VSCodeJavaSettingsJson.create(JSON.parseObject(file).await());
        });
    }

    public Iterable<String> getJavaProjectSourcePaths()
    {
        final JSONArray sourcePathsJson = this.toJson().getArray(VSCodeJavaSettingsJson.javaProjectSourcePathsPropertyName).catchError().await();
        return sourcePathsJson == null
            ? Iterable.create()
            : sourcePathsJson
                .instanceOf(JSONString.class)
                .map(JSONString::getValue)
                .toList();
    }

    public VSCodeJavaSettingsJson setJavaProjectSourcePaths(Iterable<String> javaProjectSourcePaths)
    {
        PreCondition.assertNotNull(javaProjectSourcePaths, "javaProjectSourcePaths");

        this.toJson().setArray(VSCodeJavaSettingsJson.javaProjectSourcePathsPropertyName, JSONArray.create(javaProjectSourcePaths.map(JSONString::get)));

        return this;
    }

    public Iterable<String> getJavaProjectReferencedLibraries()
    {
        final JSONArray referencedLibraries = this.toJson().getArray(VSCodeJavaSettingsJson.javaProjectReferencedLibrariesPropertyName).catchError().await();
        return referencedLibraries == null
            ? Iterable.create()
            : referencedLibraries
                .instanceOf(JSONString.class)
                .map(JSONString::getValue)
                .toList();
    }

    public VSCodeJavaSettingsJson setJavaProjectReferencedLibraries(Iterable<String> javaProjectReferencedLibraries)
    {
        PreCondition.assertNotNull(javaProjectReferencedLibraries, "javaProjectReferencedLibraries");

        this.toJson().setArray(VSCodeJavaSettingsJson.javaProjectReferencedLibrariesPropertyName, JSONArray.create(javaProjectReferencedLibraries.map(JSONString::get)));

        return this;
    }

    public String getJavaFormatSettingsUrl()
    {
        return this.toJson().getString(VSCodeJavaSettingsJson.javaFormatSettingsUrlPropertyName).catchError().await();
    }

    public VSCodeJavaSettingsJson setJavaFormatSettingsUrl(String javaFormatSettingsUrl)
    {
        PreCondition.assertNotNull(javaFormatSettingsUrl, "javaFormatSettingsUrl");

        this.toJson().setString(VSCodeJavaSettingsJson.javaFormatSettingsUrlPropertyName, javaFormatSettingsUrl);

        return this;
    }

    public Boolean getJavaFormatEnabled()
    {
        return this.toJson().getBoolean(VSCodeJavaSettingsJson.javaFormatEnabledPropertyName).catchError().await();
    }

    public VSCodeJavaSettingsJson setJavaFormatEnabled(boolean javaFormatEnabled)
    {
        this.toJson().setBoolean(VSCodeJavaSettingsJson.javaFormatEnabledPropertyName, javaFormatEnabled);

        return this;
    }
}
