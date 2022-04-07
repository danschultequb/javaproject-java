package qub;

public class VSCodeJavaLaunchConfigurationJson extends VSCodeLaunchConfigurationJsonBase<VSCodeJavaLaunchConfigurationJson>
{
    private static final String mainClassPropertyName = "mainClass";
    private static final String argsPropertyName = "args";
    private static final String classPathsPropertyName = "classPaths";
    private static final String consolePropertyName = "console";
    private static final String encodingPropertyName = "encoding";
    private static final String modulePathsPropertyName = "modulePaths";
    private static final String projectNamePropertyName = "projectName";
    private static final String shortenCommandLinePropertyName = "shortenCommandLine";
    private static final String sourcePathsPropertyName = "sourcePaths";
    private static final String stepFiltersPropertyName = "stepFilters";
    private static final String stopOnEntryPropertyName = "stopOnEntry";
    private static final String vmArgsPropertyName = "vmArgs";

    protected VSCodeJavaLaunchConfigurationJson(JSONObject json)
    {
        super(json);
    }

    public static VSCodeJavaLaunchConfigurationJson create()
    {
        return VSCodeJavaLaunchConfigurationJson.create(JSONObject.create());
    }

    public static VSCodeJavaLaunchConfigurationJson create(JSONObject json)
    {
        return new VSCodeJavaLaunchConfigurationJson(json);
    }

    /**
     * Get the fully qualified class name (e.g. [java module name/]com.xyz.MainApp) or the java file
     * path of the program entry.
     */
    public String getMainClass()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.mainClassPropertyName).catchError().await();
    }

    /**
     * Set the fully qualified class name (e.g. [java module name/]com.xyz.MainApp) or the java file
     * path of the program entry.
     * @param mainClass The fully qualified class name or the java file path of the program entry.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setMainClass(String mainClass)
    {
        PreCondition.assertNotNull(mainClass, "mainClass");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.mainClassPropertyName, mainClass);

        return this;
    }

    /**
     * Get the command line arguments passed to the program.
     */
    public String getArgs()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.argsPropertyName).catchError().await();
    }

    /**
     * Set the command line arguments passed to the program.
     * @param args The command line arguments passed to the program.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setArgs(String args)
    {
        PreCondition.assertNotNull(args, "args");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.argsPropertyName, args);

        return this;
    }

    /**
     * Get the classpaths for launching the JVM. If not specified, the debugger will automatically
     * resolve from current project.
     */
    public Iterable<String> getClassPaths()
    {
        return this.toJson().getArray(VSCodeJavaLaunchConfigurationJson.classPathsPropertyName)
            .catchError(() -> JSONArray.create())
            .await()
            .instanceOf(JSONString.class)
            .map(JSONString::getValue)
            .toList();
    }

    /**
     * Set the classpaths for launching the JVM. If not specified, the debugger will automatically
     * resolve from current project.
     * @param classPaths The classpaths for launching the JVM.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setClassPaths(Iterable<String> classPaths)
    {
        PreCondition.assertNotNull(classPaths, "classPaths");

        this.toJson().setArray(VSCodeJavaLaunchConfigurationJson.classPathsPropertyName, JSONArray.create(classPaths.map(JSONString::get)));

        return this;
    }

    /**
     * Get the specified console to launch the program. Possible options are:
     * <ul>
     *   <li><b>externalTerminal</b>: External terminal that can be configured in user settings.</li>
     *   <li><b>integratedTerminal</b>: VS Code integrated terminal.</li>
     *   <li><b>internalConsole</b>: VS Code debug console (input stream not supported).</li>
     * </ul>
     */
    public String getConsole()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.consolePropertyName).catchError().await();
    }

    /**
     * Set the specified console to launch the program. Possible options are:
     * <ul>
     *   <li><b>externalTerminal</b>: External terminal that can be configured in user settings.</li>
     *   <li><b>integratedTerminal</b>: VS Code integrated terminal.</li>
     *   <li><b>internalConsole</b>: VS Code debug console (input stream not supported).</li>
     * </ul>
     * @param console The specified console to launch the program.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setConsole(String console)
    {
        PreCondition.assertNotNull(console, "console");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.consolePropertyName, console);

        return this;
    }

    /**
     * Get the file.encoding setting for the JVM. Possible values can be found in
     * https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html.
     */
    public String getEncoding()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.encodingPropertyName).catchError().await();
    }

    /**
     * Get the file.encoding setting for the JVM. Possible values can be found in
     * https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html.
     * @param encoding The file.encoding setting for the JVM.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setEncoding(String encoding)
    {
        PreCondition.assertNotNull(encoding, "encoding");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.encodingPropertyName, encoding);

        return this;
    }

    /**
     * Get the module paths for launching the JVM. If not specified, the debugger will automatically
     * resolve from current project.
     */
    public Iterable<String> getModulePaths()
    {
        return this.toJson().getArray(VSCodeJavaLaunchConfigurationJson.modulePathsPropertyName)
            .catchError(() -> JSONArray.create())
            .await()
            .instanceOf(JSONString.class)
            .map(JSONString::getValue)
            .toList();
    }

    /**
     * Set the module paths for launching the JVM. If not specified, the debugger will automatically
     * resolve from current project.
     * @param modulePaths The module paths for launching the JVM.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setModulePaths(Iterable<String> modulePaths)
    {
        PreCondition.assertNotNull(modulePaths, "modulePaths");

        this.toJson().setArray(VSCodeJavaLaunchConfigurationJson.modulePathsPropertyName, JSONArray.create(modulePaths.map(JSONString::get)));

        return this;
    }

    /**
     * Get the preferred project in which the debugger searches for classes. There could be
     * duplicated class names in different projects. This setting also works when the debugger looks
     * for the specified main class when launching a program. It is required for expression
     * evaluation.
     */
    public String getProjectName()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.projectNamePropertyName).catchError().await();
    }

    /**
     * Set the preferred project in which the debugger searches for classes. There could be
     * duplicated class names in different projects. This setting also works when the debugger looks
     * for the specified main class when launching a program. It is required for expression
     * evaluation.
     * @param projectName The preferred project in which the debugger searches for classes.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setProjectName(String projectName)
    {
        PreCondition.assertNotNull(projectName, "projectName");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.projectNamePropertyName, projectName);

        return this;
    }

    /**
     * When the project has long classpath or big VM arguments, the command line to launch the
     * program may exceed the maximum command line string limitation allowed by the OS. This
     * configuration item provides multiple approaches to shorten the command line.
     * <ul>
     *   <li><b>argfile</b>: Generate the classpath parameters to a temporary argument file, and
     *     launch the program with the command line 'java &#64;argfile [args]'. This value only
     *     applies to Java 9 and higher.</li>
     *   <li><b>auto</b>: Automatically detect the command line length and determine whether to
     *     shorten the command line via an appropriate approach.</li>
     *   <li><b>jarmanifest</b>: Generate the classpath parameters to a temporary classpath.jar
     *     file, and launch the program with the command line 'java -cp classpath.jar classname
     *     [args]'.</li>
     *   <li><b>none</b>: Launch the program with the standard command line 'java [options]
     *     classname [args]'.</li>
     * </ul>
     */
    public String getShortenCommandLine()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.shortenCommandLinePropertyName).catchError().await();
    }

    /**
     * When the project has long classpath or big VM arguments, the command line to launch the
     * program may exceed the maximum command line string limitation allowed by the OS. This
     * configuration item provides multiple approaches to shorten the command line.
     * <ul>
     *   <li><b>argfile</b>: Generate the classpath parameters to a temporary argument file, and
     *     launch the program with the command line 'java &#64;argfile [args]'. This value only
     *     applies to Java 9 and higher.</li>
     *   <li><b>auto</b>: Automatically detect the command line length and determine whether to
     *     shorten the command line via an appropriate approach.</li>
     *   <li><b>jarmanifest</b>: Generate the classpath parameters to a temporary classpath.jar
     *     file, and launch the program with the command line 'java -cp classpath.jar classname
     *     [args]'.</li>
     *   <li><b>none</b>: Launch the program with the standard command line 'java [options]
     *     classname [args]'.</li>
     * </ul>
     * @param shortenCommandLine The approach to take to shorten the command line.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setShortenCommandLine(String shortenCommandLine)
    {
        PreCondition.assertNotNull(shortenCommandLine, "shortenCommandLine");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.shortenCommandLinePropertyName, shortenCommandLine);

        return this;
    }

    /**
     * Get the extra source directories of the program. The debugger looks for source code from
     * project settings by default. This option allows the debugger to look for source code in extra
     * directories.
     */
    public Iterable<String> getSourcePaths()
    {
        return this.toJson().getArray(VSCodeJavaLaunchConfigurationJson.sourcePathsPropertyName)
            .catchError(() -> JSONArray.create())
            .await()
            .instanceOf(JSONString.class)
            .map(JSONString::getValue)
            .toList();
    }

    /**
     * Set the extra source directories of the program. The debugger looks for source code from
     * project settings by default. This option allows the debugger to look for source code in extra
     * directories.
     * @param sourcePaths The extra source directories of the program.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setSourcePaths(Iterable<String> sourcePaths)
    {
        PreCondition.assertNotNull(sourcePaths, "sourcePaths");

        this.toJson().setArray(VSCodeJavaLaunchConfigurationJson.sourcePathsPropertyName, JSONArray.create(sourcePaths.map(JSONString::get)));

        return this;
    }

    /**
     * Get the specified classes or methods to skip when stepping.
     */
    public VSCodeJavaLaunchConfigurationStepFiltersJson getStepFilters()
    {
        final JSONObject stepFiltersJson = this.toJson().getObject(VSCodeJavaLaunchConfigurationJson.stepFiltersPropertyName).catchError().await();
        return stepFiltersJson == null ? null : VSCodeJavaLaunchConfigurationStepFiltersJson.create(stepFiltersJson);
    }

    /**
     * Set the specified classes or methods to skip when stepping.
     * @param stepFilters The specified classes or methods to skip when stepping.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setStepFilters(VSCodeJavaLaunchConfigurationStepFiltersJson stepFilters)
    {
        PreCondition.assertNotNull(stepFilters, "stepFilters");

        this.toJson().setObject(VSCodeJavaLaunchConfigurationJson.stepFiltersPropertyName, stepFilters.toJson());

        return this;
    }

    /**
     * Get whether to automatically pause the program after launching.
     */
    public Boolean getStopOnEntry()
    {
        return this.toJson().getBoolean(VSCodeJavaLaunchConfigurationJson.stopOnEntryPropertyName).catchError().await();
    }

    /**
     * Set whether to automatically pause the program after launching.
     * @param stopOnEntry Whether to automatically pause the program after launching.
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setStopOnEntry(boolean stopOnEntry)
    {
        this.toJson().setBoolean(VSCodeJavaLaunchConfigurationJson.stopOnEntryPropertyName, stopOnEntry);

        return this;
    }

    /**
     * Get the extra options and system properties for the JVM (e.g. -Xms -Xmx -D=).
     */
    public String getVmArgs()
    {
        return this.toJson().getString(VSCodeJavaLaunchConfigurationJson.vmArgsPropertyName).catchError().await();
    }

    /**
     * Set the extra options and system properties for the JVM (e.g. -Xms -Xmx -D=).
     * @param vmArgs The extra options and system properties for the JVM (e.g. -Xms -Xmx -D=).
     * @return This object for method chaining.
     */
    public VSCodeJavaLaunchConfigurationJson setVmArgs(String vmArgs)
    {
        PreCondition.assertNotNull(vmArgs, "vmArgs");

        this.toJson().setString(VSCodeJavaLaunchConfigurationJson.vmArgsPropertyName, vmArgs);

        return this;
    }
}
