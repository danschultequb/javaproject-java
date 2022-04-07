package qub;

public class VSCodeJavaLaunchConfigurationStepFiltersJson extends JSONObjectWrapperBase
{
    private static final String skipClassesPropertyName = "skipClasses";
    private static final String skipSyntheticsPropertyName = "skipSynthetics";
    private static final String skipStaticInitializersPropertyName = "skipStaticInitializers";
    private static final String skipConstructorsPropertyName = "skipConstructors";

    protected VSCodeJavaLaunchConfigurationStepFiltersJson(JSONObject json)
    {
        super(json);
    }
    
    public static VSCodeJavaLaunchConfigurationStepFiltersJson create()
    {
        return VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create());
    }

    public static VSCodeJavaLaunchConfigurationStepFiltersJson create(JSONObject json)
    {
        return new VSCodeJavaLaunchConfigurationStepFiltersJson(json);
    }

    /**
     * Get the specified classes to skip when stepping. You can use the built-in variables such as
     * '$JDK' and '$Libraries' to skip a group of classes, or add a specific class name expression,
     * e.g. java.*, *.Foo
     * @return
     */
    public Iterable<String> getSkipClasses()
    {
        return this.toJson().getArray(VSCodeJavaLaunchConfigurationStepFiltersJson.skipClassesPropertyName)
            .catchError(() -> JSONArray.create())
            .await()
            .instanceOf(JSONString.class)
            .map(JSONString::getValue)
            .toList();
    }

    /**
     * Set the specified classes to skip when stepping. You can use the built-in variables such as
     * '$JDK' and '$Libraries' to skip a group of classes, or add a specific class name expression,
     * e.g. java.*, *.Foo
     * @param skipClasses The specified classes to skip when stepping.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationStepFiltersJson setSkipClasses(Iterable<String> skipClasses)
    {
        PreCondition.assertNotNull(skipClasses, "skipClasses");

        this.toJson().setArray(VSCodeJavaLaunchConfigurationStepFiltersJson.skipClassesPropertyName, JSONArray.create(skipClasses.map(JSONString::get)));

        return this;
    }

    /**
     * Get whether to skip synthetic methods when stepping.
     */
    public Boolean getSkipSynthetics()
    {
        return this.toJson().getBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipSyntheticsPropertyName).catchError().await();
    }

    /**
     * Set whether to skip synthetic methods when stepping.
     * @param skipSynthetics Whether to skip synthetic methods when stepping.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationStepFiltersJson setSkipSynthetics(boolean skipSynthetics)
    {
        this.toJson().setBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipSyntheticsPropertyName, skipSynthetics);

        return this;
    }

    /**
     * Get whether to skip static initializer methods when stepping.
     */
    public Boolean getSkipStaticInitializers()
    {
        return this.toJson().getBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipStaticInitializersPropertyName).catchError().await();
    }

    /**
     * Set whether to skip static initializer methods when stepping.
     * @param skipStaticInitializers Whether to skip static initializer methods when stepping.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationStepFiltersJson setSkipStaticInitializers(boolean skipStaticInitializers)
    {
        this.toJson().setBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipStaticInitializersPropertyName, skipStaticInitializers);

        return this;
    }

    /**
     * Get whether to skip constructor methods when stepping.
     */
    public Boolean getSkipConstructors()
    {
        return this.toJson().getBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipConstructorsPropertyName).catchError().await();
    }

    /**
     * Set whether to skip constructor methods when stepping.
     * @param skipConstructors Whether to skip constructor methods when stepping.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationStepFiltersJson setSkipConstructors(boolean skipConstructors)
    {
        this.toJson().setBoolean(VSCodeJavaLaunchConfigurationStepFiltersJson.skipConstructorsPropertyName, skipConstructors);

        return this;
    }
}
