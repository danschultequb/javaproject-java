package qub;

public class JavaProjectFolder extends Folder
{
    private static final String outputsFolderName = "outputs";
    private static final String sourcesFolderName = "sources";
    private static final String testsFolderName = "tests";

    private JavaProjectFolder(Folder folder)
    {
        super(folder.getFileSystem(), folder.getPath());
    }

    public static JavaProjectFolder get(Folder folder)
    {
        PreCondition.assertNotNull(folder, "folder");

        return new JavaProjectFolder(folder);
    }

    public Result<Folder> getOutputsFolder()
    {
        return this.getFolder(JavaProjectFolder.outputsFolderName);
    }

    public Result<Folder> getSourceOutputsFolder()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.getFolder(JavaProjectFolder.sourcesFolderName).await();
        });
    }

    public Result<Folder> getTestOutputsFolder()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.getFolder(JavaProjectFolder.testsFolderName).await();
        });
    }

    public Result<File> getBuildJsonFile()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.getFile("build.json").await();
        });
    }

    public Result<Path> getBuildJsonRelativePath()
    {
        return Result.create(() ->
        {
            final File buildJsonFile = this.getBuildJsonFile().await();
            return buildJsonFile.relativeTo(this);
        });
    }

    public Result<CharacterWriteStream> getBuildJsonFileWriteStream()
    {
        return Result.create(() ->
        {
            final File buildJsonFile = this.getBuildJsonFile().await();
            return CharacterWriteStream.create(ByteWriteStream.buffer(buildJsonFile.getContentsByteWriteStream().await()));
        });
    }

    public Result<Void> writeBuildJson(BuildJSON buildJson)
    {
        PreCondition.assertNotNull(buildJson, "buildJson");

        return Result.create(() ->
        {
            try (final CharacterWriteStream writeStream = this.getBuildJsonFileWriteStream().await())
            {
                buildJson.toString(writeStream, JSONFormat.pretty).await();
            }
        });
    }

    public Result<BuildJSON> getBuildJson()
    {
        return Result.create(() ->
        {
            final File buildJsonFile = this.getBuildJsonFile().await();
            return BuildJSON.parse(buildJsonFile).await();
        });
    }

    public Result<Iterable<JavaClassFile>> getClassFiles()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.iterateFilesRecursively()
                .catchError(NotFoundException.class)
                .where((File file) -> Comparer.equalIgnoreCase(".class", file.getFileExtension()))
                .map(JavaClassFile::get)
                .toList();
        });
    }

    /**
     * Get the .java files that have been deleted since the previous build.
     * @return The .java files that have been deleted since the previous build.
     */
    public Result<Iterable<JavaFile>> getDeletedJavaFiles()
    {
        return Result.create(() ->
        {
            final List<JavaFile> result = List.create();

            final BuildJSON buildJson = this.getBuildJson().catchError().await();
            if (buildJson != null)
            {
                final Iterable<JavaFile> javaFiles = this.getJavaFiles().await().toList();

                final Iterable<BuildJSONJavaFile> buildJsonJavaFiles = buildJson.getJavaFiles();
                for (final BuildJSONJavaFile buildJsonJavaFile : buildJsonJavaFiles)
                {
                    final JavaFile previousJavaFile = JavaFile.get(this.getFile(buildJsonJavaFile.getRelativePath()).await());
                    if (!javaFiles.contains(previousJavaFile))
                    {
                        result.add(previousJavaFile);
                    }
                }
            }

            return result;
        });
    }

    /**
     * Delete the class files that were created from the provided .java files.
     * @param javaFiles The .java files to use to determine which class files to delete.
     * @return The class files that were deleted.
     */
    public Result<Iterable<JavaClassFile>> deleteClassFiles(Iterable<JavaFile> javaFiles)
    {
        PreCondition.assertNotNull(javaFiles, "javaFiles");

        return Result.create(() ->
        {
            final List<JavaClassFile> result = List.create();

            final BuildJSON buildJson = this.getBuildJson()
                .catchError()
                .await();
            if (buildJson != null)
            {
                for (final JavaFile javaFile : javaFiles)
                {
                    final BuildJSONJavaFile buildJsonJavaFile = buildJson.getJavaFile(javaFile.relativeTo(this))
                        .catchError()
                        .await();
                    if (buildJsonJavaFile != null)
                    {
                        final Iterable<BuildJSONClassFile> buildJsonClassFiles = buildJsonJavaFile.getClassFiles();
                        for (final BuildJSONClassFile buildJsonClassFile : buildJsonClassFiles)
                        {
                            final JavaClassFile classFile = JavaClassFile.get(this.getFile(buildJsonClassFile.getRelativePath()).await());
                            classFile.delete()
                                .catchError()
                                .await();
                            result.add(classFile);
                        }
                    }
                }
            }

            return result;
        });
    }

    public Result<File> getProjectJsonFile()
    {
        return this.getFile("project.json");
    }

    private Result<JavaProjectJSON> getProjectJson()
    {
        return Result.create(() ->
        {
            final File projectJsonFile = this.getProjectJsonFile().await();
            return JavaProjectJSON.parse(projectJsonFile).await();
        });
    }

    public Result<JavaProjectJSON> getProjectJson(CharacterWriteStream outputStream)
    {
        PreCondition.assertNotNull(outputStream, "outputStream");

        return Result.create(() ->
        {
            return this.getProjectJson()
                .onError(FileNotFoundException.class, () ->
                {
                    outputStream.writeLine("No project.json file exists in the project folder at " + Strings.escapeAndQuote(this) + ".").await();
                })
                .onError(ParseException.class, (ParseException error) ->
                {
                    outputStream.writeLine("Invalid project.json file: " + error.getMessage()).await();
                })
                .await();
        });
    }

    /**
     * Get the dependencies that are specified in this folder's project.json file.
     * @return The dependencies that are specified in this folder's project.json file.
     */
    public Result<Iterable<ProjectSignature>> getDependencies()
    {
        return Result.create(() ->
        {
            final JavaProjectJSON projectJson = this.getProjectJson().await();
            return projectJson.getDependencies();
        });
    }

    public Result<Folder> getSourcesFolder()
    {
        return this.getFolder(JavaProjectFolder.sourcesFolderName);
    }

    public Result<Folder> getTestsFolder()
    {
        return this.getFolder(JavaProjectFolder.testsFolderName);
    }

    /**
     * Get all files within this project folder that have the .java file extension.
     * @return All files within this project folder that have the .java file extension.
     */
    public Result<Iterable<JavaFile>> getJavaFiles()
    {
        return Result.create(() ->
        {
            return this.iterateFilesRecursively()
                .where((File file) -> Comparer.equalIgnoreCase(".java", file.getFileExtension()))
                .map(JavaFile::get)
                .toList();
        });
    }
}
