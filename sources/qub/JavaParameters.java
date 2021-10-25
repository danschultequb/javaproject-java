package qub;

public class JavaParameters extends ChildProcessParametersDecorator<JavaParameters>
{
    protected JavaParameters(Path executablePath)
    {
        super(executablePath);
    }

    public static JavaParameters create()
    {
        return JavaParameters.create("java");
    }

    public static JavaParameters create(String executablePath)
    {
        PreCondition.assertNotNullAndNotEmpty(executablePath, "executablePath");

        return JavaParameters.create(Path.parse(executablePath));
    }

    public static JavaParameters create(Path executablePath)
    {
        PreCondition.assertNotNull(executablePath, "executablePath");

        return new JavaParameters(executablePath);
    }

    public static JavaParameters create(File executable)
    {
        PreCondition.assertNotNull(executable, "executable");

        return JavaParameters.create(executable.getPath());
    }

    /**
     * Add a classpath argument to the java process.
     * @param classpath The classpath argument to add to the java process.
     * @return This object for method chaining.
     */
    public JavaParameters addClasspath(String classpath)
    {
        PreCondition.assertNotNullAndNotEmpty(classpath, "classpath");

        return this.addArguments("-classpath", classpath);
    }

    /**
     * Add a classpath argument to the java process.
     * @param classpath The classpath argument to add to the java process.
     * @return This object for method chaining.
     */
    public JavaParameters addClasspath(Iterator<String> classpath)
    {
        PreCondition.assertNotNull(classpath, "classpath");

        return this.addClasspath(Strings.join(';', classpath));
    }

    /**
     * Add a classpath argument to the java process.
     * @param classpath The classpath argument to add to the java process.
     * @return This object for method chaining.
     */
    public JavaParameters addClasspath(Iterable<String> classpath)
    {
        PreCondition.assertNotNull(classpath, "classpath");

        return this.addClasspath(Strings.join(';', classpath));
    }

    /**
     * Add a javaagent argument to the java process.
     * @param javaAgent The javaagent argument.
     * @return This object for method chaining.
     */
    public JavaParameters addJavaAgent(String javaAgent)
    {
        PreCondition.assertNotNullAndNotEmpty(javaAgent, "javaAgent");

        return this.addArguments("-javaagent:" + javaAgent);
    }

    /**
     * Add a version argument to the java process.
     * @return This object for method chaining.
     */
    public JavaParameters addVersion()
    {
        return this.addVersion(JavaVersionDestination.StandardOutput);
    }

    /**
     * Add a version argument to the java process.
     * @param javaVersionDestination The destination stream that the version number will be printed
     *                               to.
     * @return This object for method chaining.
     */
    public JavaParameters addVersion(JavaVersionDestination javaVersionDestination)
    {
        PreCondition.assertNotNull(javaVersionDestination, "javaVersionDestination");

        return this.addArguments(javaVersionDestination == JavaVersionDestination.StandardOutput
            ? "--version"
            : "-version");
    }

    /**
     * Add a show-version argument to the java process.
     * @return This object for method chaining.
     */
    public JavaParameters addShowVersion()
    {
        return this.addShowVersion(JavaVersionDestination.StandardOutput);
    }

    /**
     * Add a show-version argument to the java process.
     * @param javaVersionDestination The destination stream that the version number will be printed
     *                               to.
     * @return This object for method chaining.
     */
    public JavaParameters addShowVersion(JavaVersionDestination javaVersionDestination)
    {
        PreCondition.assertNotNull(javaVersionDestination, "javaVersionDestination");

        return this.addArguments(javaVersionDestination == JavaVersionDestination.StandardOutput
            ? "--show-version"
            : "-showversion");
    }
}
