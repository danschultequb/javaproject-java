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

    public Result<Folder> getOutputsSourcesFolder()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            final Folder sourcesFolder = this.getSourcesFolder().await();
            return outputsFolder.getFolder(sourcesFolder.relativeTo(this)).await();
        });
    }

    public Result<Folder> getOutputsTestsFolder()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            final Folder testSourcesFolder = this.getTestSourcesFolder().await();
            return outputsFolder.getFolder(testSourcesFolder.relativeTo(this)).await();
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

    public Result<BuildJSON> getBuildJson()
    {
        return Result.create(() ->
        {
            final File buildJsonFile = this.getBuildJsonFile().await();
            return BuildJSON.parse(buildJsonFile).await();
        });
    }

    public Result<Void> writeBuildJson(BuildJSON buildJson)
    {
        PreCondition.assertNotNull(buildJson, "buildJson");

        return Result.create(() ->
        {
            final File buildJsonFile = this.getBuildJsonFile().await();
            try (final CharacterWriteStream writeStream = CharacterWriteStream.create(ByteWriteStream.buffer(buildJsonFile.getContentsByteWriteStream().await())))
            {
                buildJson.toString(writeStream, JSONFormat.pretty).await();
            }
        });
    }

    public Result<File> getTestJsonFile()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.getFile("test.json").await();
        });
    }

    public Result<Path> getTestJsonRelativePath()
    {
        return Result.create(() ->
        {
            final File testJsonFile = this.getTestJsonFile().await();
            return testJsonFile.relativeTo(this);
        });
    }

    public Result<TestJSON> getTestJson()
    {
        return Result.create(() ->
        {
            final File testJsonFile = this.getTestJsonFile().await();
            return TestJSON.parse(testJsonFile).await();
        });
    }

    public Result<Void> writeTestJson(TestJSON testJson)
    {
        PreCondition.assertNotNull(testJson, "testJson");

        return Result.create(() ->
        {
            final File testJsonFile = this.getTestJsonFile().await();
            try (final CharacterWriteStream writeStream = CharacterWriteStream.create(ByteWriteStream.buffer(testJsonFile.getContentsByteWriteStream().await())))
            {
                testJson.toString(writeStream, JSONFormat.pretty).await();
            }
        });
    }

    public Result<File> getPackJsonFile()
    {
        return Result.create(() ->
        {
            final Folder outputsFolder = this.getOutputsFolder().await();
            return outputsFolder.getFile("pack.json").await();
        });
    }

    public Result<Path> getPackJsonRelativePath()
    {
        return Result.create(() ->
        {
            final File packJsonFile = this.getPackJsonFile().await();
            return packJsonFile.relativeTo(this);
        });
    }

    public Result<PackJSON> getPackJson()
    {
        return Result.create(() ->
        {
            final File packJsonFile = this.getPackJsonFile().await();
            return PackJSON.parse(packJsonFile).await();
        });
    }

    public Result<Void> writePackJson(PackJSON packJson)
    {
        PreCondition.assertNotNull(packJson, "packJson");

        return Result.create(() ->
        {
            final File packJsonFile = this.getPackJsonFile().await();
            try (final CharacterWriteStream writeStream = CharacterWriteStream.create(ByteWriteStream.buffer(packJsonFile.getContentsByteWriteStream().await())))
            {
                packJson.toString(writeStream, JSONFormat.pretty).await();
            }
        });
    }

    public Iterator<JavaClassFile> iterateClassFiles()
    {
        final Folder outputsFolder = this.getOutputsFolder().await();
        return JavaProjectFolder.iterateClassFilesFromOutputsFolder(outputsFolder);
    }

    public Iterator<JavaClassFile> iterateSourceClassFiles()
    {
        final Folder outputsSourcesFolder = this.getOutputsSourcesFolder().await();
        return JavaProjectFolder.iterateClassFilesFromOutputsFolder(outputsSourcesFolder);
    }

    public Iterator<JavaClassFile> iterateTestClassFiles()
    {
        final Folder outputsTestsFolder = this.getOutputsTestsFolder().await();
        return JavaProjectFolder.iterateClassFilesFromOutputsFolder(outputsTestsFolder);
    }

    private static Iterator<JavaClassFile> iterateClassFilesFromOutputsFolder(Folder outputsFolder)
    {
        PreCondition.assertNotNull(outputsFolder, "outputsFolder");

        return outputsFolder.iterateFilesRecursively()
            .catchError(NotFoundException.class)
            .where((File file) -> Comparer.equalIgnoreCase(".class", file.getFileExtension()))
            .map(JavaClassFile::get);
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
                final Iterable<JavaFile> javaFiles = this.iterateJavaFiles().toList();

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

    public Result<JavaProjectJSON> getProjectJson()
    {
        return Result.create(() ->
        {
            final File projectJsonFile = this.getProjectJsonFile().await();
            return JavaProjectJSON.parse(projectJsonFile).await();
        });
    }

    public Result<String> getPublisher()
    {
        return Result.create(() ->
        {
            final JavaProjectJSON projectJson = this.getProjectJson().await();
            return projectJson.getPublisher();
        });
    }

    public Result<String> getProject()
    {
        return Result.create(() ->
        {
            final JavaProjectJSON projectJson = this.getProjectJson().await();
            return projectJson.getProject();
        });
    }

    public Result<VersionNumber> getVersion()
    {
        return Result.create(() ->
        {
            final JavaProjectJSON projectJson = this.getProjectJson().await();
            return projectJson.getVersion();
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

    public Result<Iterable<ProjectSignature>> getAllDependencies(QubFolder qubFolder, boolean validateDependencies)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        return JavaProjectFolder.getAllDependencies(qubFolder, this.getDependencies().await(), validateDependencies);
    }

    public static Result<Iterable<ProjectSignature>> getAllDependencies(QubFolder qubFolder, Iterable<ProjectSignature> projectSignatures, boolean validateDependencies)
    {
        return JavaProjectFolder.getAllDependencyFolders(qubFolder, projectSignatures, validateDependencies)
            .then((Iterable<JavaPublishedProjectFolder> allDependencies) ->
            {
                return allDependencies.map((JavaPublishedProjectFolder dependency) -> dependency.getProjectSignature().await());
            });
    }

    public Result<Iterable<JavaPublishedProjectFolder>> getAllDependencyFolders(QubFolder qubFolder, boolean validateDependencies)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        return JavaProjectFolder.getAllDependencyFolders(qubFolder, this.getDependencies().await(), validateDependencies);
    }

    public static Result<Iterable<JavaPublishedProjectFolder>> getAllDependencyFolders(QubFolder qubFolder, Iterable<ProjectSignature> projectSignatures, boolean validateDependencies)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");
        PreCondition.assertNotNull(projectSignatures, "projectSignatures");

        return Result.create(() ->
        {
            final Iterable<QubProjectVersionFolder> dependencyFolders = qubFolder.getAllDependencyFolders(projectSignatures,
                (QubProjectVersionFolder projectVersionFolder) ->
                {
                    return Result.create(() ->
                    {
                        final JavaPublishedProjectFolder javaPublishedProjectFolder = JavaPublishedProjectFolder.get(projectVersionFolder);
                        return javaPublishedProjectFolder.getDependencies()
                            .convertError(NotFoundException.class, (NotFoundException error) ->
                            {
                                Throwable result;
                                final QubPublisherFolder publisherFolder = projectVersionFolder.getPublisherFolder().await();
                                final QubProjectFolder projectFolder = projectVersionFolder.getProjectFolder().await();
                                if (!publisherFolder.exists().await())
                                {
                                    result = new NotFoundException("No publisher folder named " + Strings.escapeAndQuote(publisherFolder.getPublisherName()) + " found in the Qub folder (" + qubFolder + ").");
                                }
                                else if (!projectFolder.exists().await())
                                {
                                    final String project = projectFolder.getProjectName();
                                    result = new NotFoundException("No project folder named " + Strings.escapeAndQuote(project) + " found in the " + Strings.escapeAndQuote(publisherFolder.getPublisherName()) + " publisher folder (" + publisherFolder + ").");
                                }
                                else if (!projectVersionFolder.exists().await())
                                {

                                    final VersionNumber projectVersion = projectVersionFolder.getVersion().await();
                                    final String projectSignatureWithoutVersion = projectVersionFolder.getProjectSignature().await().toStringIgnoreVersion();
                                    result = new NotFoundException("No version folder named " + Strings.escapeAndQuote(projectVersion) + " found in the " + Strings.escapeAndQuote(projectSignatureWithoutVersion) + " project folder (" + projectFolder + ").");
                                }
                                else
                                {
                                    result = error;
                                }
                                return result;
                            })
                            .await();
                    });
                },
                validateDependencies).await();
            return dependencyFolders.map(JavaPublishedProjectFolder::get);
        });
    }

    public Result<Folder> getSourcesFolder()
    {
        return this.getFolder(JavaProjectFolder.sourcesFolderName);
    }

    public Result<Folder> getTestSourcesFolder()
    {
        return this.getFolder(JavaProjectFolder.testsFolderName);
    }

    /**
     * Get all files within this project folder that have the .java file extension.
     * @return All files within this project folder that have the .java file extension.
     */
    public Iterator<JavaFile> iterateJavaFiles()
    {
        return JavaProjectFolder.iterateJavaFilesFromFolder(this);
    }

    public Iterator<JavaFile> iterateSourceJavaFiles()
    {
        final Folder sourcesFolder = this.getSourcesFolder().await();
        return JavaProjectFolder.iterateJavaFilesFromFolder(sourcesFolder);
    }

    public Iterator<JavaFile> iterateTestJavaFiles()
    {
        final Folder testsFolder = this.getTestSourcesFolder().await();
        return JavaProjectFolder.iterateJavaFilesFromFolder(testsFolder);
    }

    private static Iterator<JavaFile> iterateJavaFilesFromFolder(Folder folder)
    {
        PreCondition.assertNotNull(folder, "folder");

        return folder.iterateFilesRecursively()
            .catchError(NotFoundException.class)
            .where((File file) -> Comparer.equalIgnoreCase(".java", file.getFileExtension()))
            .map(JavaFile::get);
    }
}
