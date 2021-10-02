package qub;

public class JavacParameters extends ChildProcessParametersDecorator<JavacParameters>
{
    protected JavacParameters(Path executablePath)
    {
        super(executablePath);
    }

    public static JavacParameters create()
    {
        return JavacParameters.create("javac");
    }

    public static JavacParameters create(String executablePath)
    {
        PreCondition.assertNotNullAndNotEmpty(executablePath, "executablePath");

        return JavacParameters.create(Path.parse(executablePath));
    }

    public static JavacParameters create(Path executablePath)
    {
        PreCondition.assertNotNull(executablePath, "executablePath");

        return new JavacParameters(executablePath);
    }

    public static JavacParameters create(File executable)
    {
        PreCondition.assertNotNull(executable, "executable");

        return JavacParameters.create(executable.getPath());
    }

    /**
     * Add a directory argument that specifies where class files should be generated.
     * @param directory The directory where class files should be generated.
     * @return This object for method chaining.
     */
    public JavacParameters addDirectory(String directory)
    {
        PreCondition.assertNotNullAndNotEmpty(directory, "directory");

        return this.addDirectory(Path.parse(directory));
    }

    /**
     * Add a directory argument that specifies where class files should be generated.
     * @param directory The directory where class files should be generated.
     * @return This object for method chaining.
     */
    public JavacParameters addDirectory(Path directory)
    {
        PreCondition.assertNotNull(directory, "directory");

        return this.addArguments("-d", directory.toString());
    }

    /**
     * Add a directory argument that specifies where class files should be generated.
     * @param directory The directory where class files should be generated.
     * @return This object for method chaining.
     */
    public JavacParameters addDirectory(Folder directory)
    {
        PreCondition.assertNotNull(directory, "directory");

        return this.addDirectory(directory.getPath());
    }

    /**
     * Add an argument that indicates that the compile should output extra messages about what it
     * is doing.
     * @return This object for method chaining.
     */
    public JavacParameters addVerbose()
    {
        return this.addArgument("-verbose");
    }

    /**
     * Add an argument that will cause the compiler to output its version.
     * @return This object for method chaining.
     */
    public JavacParameters addVersion()
    {
        return this.addArgument("--version");
    }

    /**
     * Add a class path argument that will indicate where existing class files are.
     * @param classpath The class path argument that indicates where existing class files are.
     * @return This object for method chaining.
     */
    public JavacParameters addClasspath(String... classpath)
    {
        PreCondition.assertNotNullAndNotEmpty(classpath, "classpaths");

        return this.addClasspath(Iterable.create(classpath));
    }

    /**
     * Add a class path argument that will indicate where existing class files are.
     * @param classpath The class path argument that indicates where existing class files are.
     * @return This object for method chaining.
     */
    public JavacParameters addClasspath(Iterable<String> classpath)
    {
        PreCondition.assertNotNullAndNotEmpty(classpath, "classpath");

        return this.addArguments("--class-path", Strings.join(';', classpath));
    }

    /**
     * Enable or disable specific Xlint warnings. To disable a key, precede it with a '-'
     * character.
     * @param xlintKeys The Xlint keys to enable/disable.
     * @return This object for method chaining.
     */
    public JavacParameters addXLint(String... xlintKeys)
    {
        PreCondition.assertNotNull(xlintKeys, "xlintKeys");

        return this.addXLint(Iterable.create(xlintKeys));
    }

    /**
     * Enable or disable specific Xlint warnings. To disable a key, precede it with a '-'
     * character.
     * @param xlintKeys The Xlint keys to enable/disable.
     * @return This object for method chaining.
     */
    public JavacParameters addXLint(Iterable<String> xlintKeys)
    {
        PreCondition.assertNotNull(xlintKeys, "xlintKeys");
        PreCondition.assertFalse(xlintKeys.contains((String)null), "xlintKeys.contains(null)");

        final CharacterList argument = CharacterList.create().addAll("-Xlint");
        if (xlintKeys.any())
        {
            argument.add(':');
            argument.addAll(xlintKeys.first());
            for (final String xlintKey : xlintKeys.skipFirst())
            {
                argument.add(',');
                argument.addAll(xlintKey);
            }
        }
        return this.addArgument(argument.toString(true));
    }
}
