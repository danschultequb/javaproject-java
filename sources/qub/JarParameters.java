package qub;

public class JarParameters extends ChildProcessParametersDecorator<JarParameters>
{
    protected JarParameters(Path executablePath)
    {
        super(executablePath);
    }

    public static JarParameters create()
    {
        return JarParameters.create("jar");
    }

    public static JarParameters create(String executablePath)
    {
        PreCondition.assertNotNullAndNotEmpty(executablePath, "executablePath");

        return JarParameters.create(Path.parse(executablePath));
    }

    public static JarParameters create(Path executablePath)
    {
        PreCondition.assertNotNull(executablePath, "executablePath");

        return new JarParameters(executablePath);
    }

    public static JarParameters create(File executable)
    {
        PreCondition.assertNotNull(executable, "executable");

        return JarParameters.create(executable.getPath());
    }

    /**
     * Add the --version argument.
     * @return This object for method chaining.
     */
    public JarParameters addVersion()
    {
        return this.addArguments("--version");
    }

    /**
     * Add the --create argument.
     * @return This object for method chaining.
     */
    public JarParameters addCreate()
    {
        return this.addArguments("--create");
    }
}
