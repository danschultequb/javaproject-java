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

    /**
     * Add the --file argument.
     * @param jarFilePath The path to the jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    public JarParameters addJarFile(String jarFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(jarFilePath, "jarFilePath");

        return this.addJarFile(Path.parse(jarFilePath));
    }

    /**
     * Add the --file argument.
     * @param jarFilePath The path to the jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    public JarParameters addJarFile(Path jarFilePath)
    {
        PreCondition.assertNotNull(jarFilePath, "jarFilePath");

        jarFilePath = this.relativeToWorkingFolderPath(jarFilePath);
        return this.addArgument("--file=" + jarFilePath);
    }

    /**
     * Add the --file argument.
     * @param jarFile The jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    public JarParameters addJarFile(File jarFile)
    {
        PreCondition.assertNotNull(jarFile, "jarFile");

        return this.addJarFile(jarFile.getPath());
    }

    /**
     * Set the main class/entry point type for executable jar file that will be created.
     * @param mainClassFullTypeName The main class/entry point type for the executable jar file
     *                              that will be created.
     * @return This object for method chaining.
     */
    public JarParameters addMainClass(String mainClassFullTypeName)
    {
        PreCondition.assertNotNullAndNotEmpty(mainClassFullTypeName, "mainClassFullTypeName");

        return this.addArgument("--main-class=" + mainClassFullTypeName);
    }

    /**
     * Add the --manifest argument.
     * @param manifestFilePath The path to the manifest file.
     * @return This object for method chaining.
     */
    public JarParameters addManifestFile(String manifestFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(manifestFilePath, "manifestFilePath");

        return this.addManifestFile(Path.parse(manifestFilePath));
    }

    /**
     * Add the --manifest argument.
     * @param manifestFilePath The path to the manifest file.
     * @return This object for method chaining.
     */
    public JarParameters addManifestFile(Path manifestFilePath)
    {
        PreCondition.assertNotNull(manifestFilePath, "manifestFilePath");

        manifestFilePath = this.relativeToWorkingFolderPath(manifestFilePath);
        return this.addArgument("--manifest=" + manifestFilePath);
    }

    /**
     * Add the --manifest argument.
     * @param manifestFile The manifest file.
     * @return This object for method chaining.
     */
    public JarParameters addManifestFile(File manifestFile)
    {
        PreCondition.assertNotNull(manifestFile, "manifestFile");

        return this.addManifestFile(manifestFile.getPath());
    }

    public JarParameters addBaseFolderPath(String baseFolderPath)
    {
        PreCondition.assertNotNullAndNotEmpty(baseFolderPath, "baseFolderPath");

        return this.addBaseFolderPath(Path.parse(baseFolderPath));
    }

    public JarParameters addBaseFolderPath(Path baseFolderPath)
    {
        PreCondition.assertNotNull(baseFolderPath, "baseFolderPath");

        return this.addArguments("-C", baseFolderPath.toString());
    }

    public JarParameters addBaseFolderPath(Folder baseFolder)
    {
        PreCondition.assertNotNull(baseFolder, "baseFolder");

        return this.addBaseFolderPath(baseFolder.getPath());
    }

    public JarParameters addContentPathStrings(Iterable<String> contentPathStrings)
    {
        PreCondition.assertNotNullAndNotEmpty(contentPathStrings, "contentPathStrings");

        return this.addContentPaths(contentPathStrings.map(Path::parse));
    }

    public JarParameters addContentPaths(Iterable<Path> contentPaths)
    {
        PreCondition.assertNotNull(contentPaths, "contentPaths");

        for (final Path contentPath : contentPaths)
        {
            this.addContentPath(contentPath);
        }
        final JarParameters result = this;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public JarParameters addContentEntries(Iterable<FileSystemEntry> contentEntries)
    {
        PreCondition.assertNotNullAndNotEmpty(contentEntries, "contentEntries");

        return this.addContentPaths(contentEntries.map(FileSystemEntry::getPath));
    }

    public JarParameters addContentPath(String contentPath)
    {
        PreCondition.assertNotNullAndNotEmpty(contentPath, "contentPath");

        return this.addContentPath(Path.parse(contentPath));
    }

    public JarParameters addContentPath(Path contentPath)
    {
        PreCondition.assertNotNull(contentPath, "contentPath");

        contentPath = this.relativeToWorkingFolderPath(contentPath);
        return this.addArguments(contentPath.toString());
    }

    public JarParameters addContentPath(FileSystemEntry contentEntry)
    {
        PreCondition.assertNotNull(contentEntry, "contentEntry");

        return this.addContentPath(contentEntry.getPath());
    }

    private Path relativeToWorkingFolderPath(Path path)
    {
        PreCondition.assertNotNull(path, "path");

        Path result = path;
        if (result.isRooted())
        {
            final Path workingFolderPath = this.getWorkingFolderPath();
            if (workingFolderPath != null)
            {
                result = result.relativeTo(workingFolderPath);
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
