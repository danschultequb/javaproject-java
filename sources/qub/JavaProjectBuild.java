package qub;

public interface JavaProjectBuild
{
    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("build", JavaProjectBuild::run)
            .setDescription("Build a Java source code project.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        JavaProjectBuild.run(process, action, null, null);
    }

    static void run(DesktopProcess process, CommandLineAction action, JavaProjectFolder providedProjectFolder, File providedLogFile)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to build. Defaults to the current folder.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = JavaProject.addProfilerParameter(parameters, process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final JavaProjectFolder projectFolder = providedProjectFolder != null
                ? providedProjectFolder
                : JavaProjectFolder.get(projectFolderParameter.getValue().await());

            final File logFile = providedLogFile != null
                ? providedLogFile
                : CommandLineLogsAction.getLogFileFromProcess(process);

            try (final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await()))
            {
                final CharacterToByteWriteStream outputStream = logStreams.getOutput();
                final VerboseCharacterToByteWriteStream verboseStream = logStreams.getVerbose();

                verboseStream.writeLine("Parsing " + projectFolder.getProjectJsonFile().await() + "...").await();
                final JavaProjectJSON projectJson = projectFolder.getProjectJson()
                    .catchError(FileNotFoundException.class, () ->
                    {
                        outputStream.writeLine("No project.json file exists in the project folder at " + Strings.escapeAndQuote(projectFolder) + ".").await();
                        process.setExitCode(-1);
                    })
                    .catchError(ParseException.class, (ParseException error) ->
                    {
                        outputStream.writeLine("Invalid project.json file: " + error.getMessage()).await();
                        process.setExitCode(-1);
                    })
                    .await();
                if (projectJson != null)
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder)
                        .catchError(NotFoundException.class, () ->
                        {
                            outputStream.writeLine("No openjdk/jdk project is installed in the qub folder at " + Strings.escapeAndQuote(qubFolder) + ".").await();
                            process.setExitCode(-1);
                        })
                        .await();
                    if (jdkFolder != null)
                    {
                        final VerboseChildProcessRunner childProcessRunner = VerboseChildProcessRunner.create(process, verboseStream);
                        final Javac javac = jdkFolder.getJavac(childProcessRunner).await();

                        verboseStream.writeLine("Discovering dependencies...").await();
                        final Iterable<ProjectSignature> declaredDependencies = projectFolder.getDependencies().await();
                        final Iterable<JavaPublishedProjectFolder> dependencyFolders = projectFolder.getAllDependencyFolders(qubFolder, true)
                            .catchError((Throwable error) ->
                            {
                                final Iterable<Throwable> errors;
                                final String errorString;
                                if (error instanceof ErrorIterable)
                                {
                                    errors = (ErrorIterable)error;
                                    errorString = "Errors";
                                }
                                else
                                {
                                    errors = Iterable.create(error);
                                    errorString = "An error";
                                }

                                outputStream.writeLine(errorString + " occurred while discovering dependencies:").await();
                                int errorNumber = 0;
                                for (final Throwable dependencyError : errors)
                                {
                                    errorNumber++;
                                    final IndentedCharacterWriteStream indentedOutputStream = IndentedCharacterWriteStream.create(outputStream);
                                    final String errorNumberString = errorNumber + ". ";
                                    indentedOutputStream.write(errorNumberString).await();
                                    indentedOutputStream.setCurrentIndent(Strings.repeat(' ', errorNumberString.length()));
                                    indentedOutputStream.writeLine(dependencyError.getMessage()).await();
                                }
                                process.setExitCode(-1);
                            })
                            .await();
                        if (dependencyFolders != null)
                        {
                            final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                            verboseStream.writeLine("Parsing " + projectFolder.getBuildJsonRelativePath().await() + "...").await();
                            final BuildJSON buildJson = projectFolder.getBuildJson()
                                .catchError(FileNotFoundException.class, () -> BuildJSON.create())
                                .await();

                            verboseStream.writeLine("Checking if dependencies have changed since the previous build...").await();
                            boolean modifiedPreviousDependencies = false;
                            for (final ProjectSignature previousDependency : buildJson.getDependencies())
                            {
                                if (!declaredDependencies.contains(previousDependency))
                                {
                                    modifiedPreviousDependencies = true;
                                    break;
                                }
                            }
                            verboseStream.writeLine("  Previous dependencies have " + (modifiedPreviousDependencies ? "" : "not ") + "changed.").await();

                            verboseStream.writeLine("Checking if latest installed JDK has changed since the previous build...").await();
                            final VersionNumber javacVersionNumber = javac.version().await();
                            final boolean javacVersionChanged = !javacVersionNumber.equals(buildJson.getJavacVersion());
                            verboseStream.writeLine("  Installed JDK has " + (javacVersionChanged ? "" : "not ") + "changed.").await();

                            final Iterable<JavaFile> javaFiles = projectFolder.iterateJavaFiles().toList();

                            verboseStream.writeLine("Looking for .java files that have been deleted...").await();
                            final Iterable<JavaFile> deletedJavaFiles = projectFolder.getDeletedJavaFiles().await();
                            for (final JavaFile deletedJavaFile : deletedJavaFiles)
                            {
                                verboseStream.writeLine(deletedJavaFile.relativeTo(projectFolder) + " - Deleted").await();
                            }
                            projectFolder.deleteClassFiles(deletedJavaFiles).await();

                            if (!javaFiles.any())
                            {
                                outputStream.writeLine("No .java files found in " + projectFolder + ".").await();
                                process.setExitCode(-1);
                            }
                            else
                            {
                                verboseStream.writeLine("Looking for .java files to compile...").await();
                                final List<BuildJSONJavaFile> javaFilesToCompile = List.create();
                                final List<BuildJSONJavaFile> unmodifiedJavaFiles = List.create();
                                final List<BuildJSONJavaFile> javaFilesWithNewContent = List.create();
                                for (final JavaFile javaFile : javaFiles)
                                {
                                    final Path javaFileRelativePath = javaFile.relativeTo(projectFolder);
                                    final DateTime javaFileLastModified = javaFile.getLastModified().await();
                                    final BuildJSONJavaFile buildJsonJavaFile = buildJson.getJavaFile(javaFileRelativePath)
                                        .catchError(NotFoundException.class)
                                        .await();
                                    boolean shouldCompileJavaFile = modifiedPreviousDependencies || javacVersionChanged;
                                    boolean hasNewContent = false;
                                    if (buildJsonJavaFile == null)
                                    {
                                        verboseStream.writeLine(javaFileRelativePath + " - New file").await();
                                        hasNewContent = true;
                                        shouldCompileJavaFile = true;
                                    }
                                    else if (!javaFileLastModified.equals(buildJsonJavaFile.getLastModified()))
                                    {
                                        verboseStream.writeLine(javaFileRelativePath + " - Modified").await();
                                        hasNewContent = true;
                                        shouldCompileJavaFile = true;
                                    }

                                    if (shouldCompileJavaFile)
                                    {
                                        final BuildJSONJavaFile newBuildJsonJavaFile = BuildJSONJavaFile.create(javaFileRelativePath)
                                            .setLastModified(javaFileLastModified);

                                        javaFilesToCompile.add(newBuildJsonJavaFile);
                                        if (hasNewContent)
                                        {
                                            javaFilesWithNewContent.add(newBuildJsonJavaFile);
                                        }
                                        else
                                        {
                                            newBuildJsonJavaFile.setDependencies(buildJsonJavaFile.getDependencies());
                                        }
                                    }
                                    else
                                    {
                                        unmodifiedJavaFiles.add(buildJsonJavaFile);
                                    }
                                }

                                verboseStream.writeLine("Update .java file dependencies...").await();
                                if (javaFilesWithNewContent.any())
                                {
                                    final Map<String, Path> typeNamesToPathMap = javaFiles
                                        .toMap(File::getNameWithoutFileExtension,
                                            (JavaFile javaFile) -> javaFile.relativeTo(projectFolder));

                                    for (final BuildJSONJavaFile javaFileToCompile : javaFilesWithNewContent)
                                    {
                                        final Path javaFileRelativePath = javaFileToCompile.getRelativePath();
                                        final Set<String> javaFileWords;
                                        try (final ByteReadStream fileByteReadStream = projectFolder.getFileContentReadStream(javaFileRelativePath).await())
                                        {
                                            final BufferedByteReadStream bufferedFileByteReadStream = BufferedByteReadStream.create(fileByteReadStream);
                                            final CharacterReadStream fileCharacterReadStream = CharacterReadStream.create(bufferedFileByteReadStream);
                                            javaFileWords = Strings.iterateWords(CharacterReadStream.iterate(fileCharacterReadStream)).toSet();
                                        }

                                        final String javaFileTypeName = javaFileRelativePath.getNameWithoutFileExtension();
                                        final List<Path> dependencyPaths = List.create();
                                        for (final String javaFileWord : javaFileWords)
                                        {
                                            if (!javaFileWord.equals(javaFileTypeName))
                                            {
                                                final Path dependencyPath = typeNamesToPathMap.get(javaFileWord).catchError().await();
                                                if (dependencyPath != null)
                                                {
                                                    dependencyPaths.add(dependencyPath);
                                                }
                                            }
                                        }

                                        if (dependencyPaths.any())
                                        {
                                            dependencyPaths.sort(Path::lessThan);
                                            javaFileToCompile.setDependencies(dependencyPaths);
                                        }
                                    }
                                }

                                verboseStream.writeLine("Discovering unmodified .java files that have dependencies that are being compiled or were deleted...").await();
                                if (unmodifiedJavaFiles.any())
                                {
                                    final Iterable<Path> deletedJavaFilePaths = deletedJavaFiles.map((JavaFile deletedJavaFile) -> deletedJavaFile.relativeTo(projectFolder)).toList();
                                    boolean javaFilesToCompileChanged = true;
                                    while (unmodifiedJavaFiles.any() && javaFilesToCompileChanged)
                                    {
                                        javaFilesToCompileChanged = false;

                                        for (final BuildJSONJavaFile unmodifiedJavaFile : unmodifiedJavaFiles.toList())
                                        {
                                            final Iterable<Path> unmodifiedJavaFileDependencyPaths = unmodifiedJavaFile.getDependencies();
                                            if (!Iterable.isNullOrEmpty(unmodifiedJavaFileDependencyPaths))
                                            {
                                                final Iterable<Path> javaFilePathsToCompile = javaFilesToCompile.map(BuildJSONJavaFile::getRelativePath);
                                                boolean shouldCompileJavaFile = false;

                                                final Iterable<Path> unmodifiedJavaFileDependenciesBeingCompiled = unmodifiedJavaFileDependencyPaths.where(javaFilePathsToCompile::contains);
                                                if (!Iterable.isNullOrEmpty(unmodifiedJavaFileDependenciesBeingCompiled))
                                                {
                                                    verboseStream.writeLine(unmodifiedJavaFile.getRelativePath() + " - Dependency file(s) being compiled").await();
                                                    shouldCompileJavaFile = true;
                                                }
                                                else
                                                {
                                                    final Iterable<Path> unmodifiedJavaFileDependenciesThatWereDeleted = unmodifiedJavaFileDependencyPaths.where(deletedJavaFilePaths::contains);
                                                    if (unmodifiedJavaFileDependenciesThatWereDeleted.any())
                                                    {
                                                        verboseStream.writeLine(unmodifiedJavaFile.getRelativePath() + " - Dependency file(s) were deleted").await();
                                                        shouldCompileJavaFile = true;
                                                    }
                                                }

                                                if (shouldCompileJavaFile)
                                                {
                                                    javaFilesToCompile.add(unmodifiedJavaFile);
                                                    unmodifiedJavaFiles.remove(unmodifiedJavaFile);
                                                    unmodifiedJavaFile.setIssues(Iterable.create());
                                                    javaFilesToCompileChanged = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                verboseStream.writeLine("Discovering unmodified .java files that have missing or modified .class files...").await();
                                final Map<Path, JavaClassFile> classFilesMap = projectFolder.iterateClassFiles().toMap(
                                    (JavaClassFile classFile) -> classFile.relativeTo(projectFolder),
                                    (JavaClassFile classFile) -> classFile);
                                for (final BuildJSONJavaFile unmodifiedJavaFile : unmodifiedJavaFiles.toList())
                                {
                                    boolean shouldCompileJavaFile = false;

                                    final Path javaFileRelativePath = unmodifiedJavaFile.getRelativePath();
                                    final Iterable<BuildJSONClassFile> buildJsonJavaFileClassFiles = unmodifiedJavaFile.getClassFiles();
                                    if (!buildJsonJavaFileClassFiles.any())
                                    {
                                        verboseStream.writeLine(javaFileRelativePath + " - Missing class file(s)").await();
                                        shouldCompileJavaFile = true;
                                    }
                                    else
                                    {
                                        for (final BuildJSONClassFile buildJsonJavaFileClassFile : buildJsonJavaFileClassFiles)
                                        {
                                            final Path classFileRelativePath = buildJsonJavaFileClassFile.getRelativePath();
                                            final JavaClassFile javaClassFile = classFilesMap.get(classFileRelativePath).catchError().await();
                                            if (javaClassFile == null ||
                                                !javaClassFile.getLastModified().await().equals(buildJsonJavaFileClassFile.getLastModified()))
                                            {
                                                verboseStream.writeLine(javaFileRelativePath + " - Missing or modified class file(s)").await();
                                                shouldCompileJavaFile = true;
                                                break;
                                            }
                                        }

                                        if (!shouldCompileJavaFile)
                                        {
                                            verboseStream.writeLine(javaFileRelativePath + " - All class files are up to date.").await();
                                        }
                                    }

                                    if (shouldCompileJavaFile)
                                    {
                                        javaFilesToCompile.add(unmodifiedJavaFile);
                                        unmodifiedJavaFiles.remove(unmodifiedJavaFile);
                                        unmodifiedJavaFile.setIssues(Iterable.create());
                                    }
                                }

                                verboseStream.writeLine("Discovering unmodified .java file issues...").await();
                                final List<JavacIssue> unmodifiedWarnings = List.create();
                                final List<JavacIssue> unmodifiedErrors = List.create();
                                final List<JavacIssue> unmodifiedUnrecognizedIssues = List.create();
                                for (final BuildJSONJavaFile unmodifiedJavaFile : unmodifiedJavaFiles.toList())
                                {
                                    boolean hasErrors = false;
                                    for (final JavacIssue issue : unmodifiedJavaFile.getIssues())
                                    {
                                        if (Comparer.equalIgnoreCase("warning", issue.getType()))
                                        {
                                            unmodifiedWarnings.add(issue);
                                        }
                                        else if (Comparer.equalIgnoreCase("error", issue.getType()))
                                        {
                                            hasErrors = true;
                                            unmodifiedErrors.add(issue);
                                        }
                                        else
                                        {
                                            unmodifiedUnrecognizedIssues.add(issue);
                                        }
                                    }

                                    if (hasErrors)
                                    {
                                        verboseStream.writeLine(unmodifiedJavaFile.getRelativePath() + " - Has errors from previous build.").await();
                                        javaFilesToCompile.add(unmodifiedJavaFile);
                                        unmodifiedJavaFiles.remove(unmodifiedJavaFile);
                                    }
                                }

                                final BuildJSON newBuildJson = BuildJSON.create()
                                    .setJavacVersion(javacVersionNumber)
                                    .setProjectJson(projectJson)
                                    .setJavaFiles(unmodifiedJavaFiles)
                                    .setJavaFiles(javaFilesToCompile);
                                final List<JavacIssue> newWarnings = List.create();
                                final List<JavacIssue> newErrors = List.create();
                                final List<JavacIssue> newUnrecognizedIssues = List.create();
                                if (!javaFilesToCompile.any())
                                {
                                    outputStream.writeLine("No .java files need to be compiled.").await();
                                }
                                else
                                {
                                    JavacResult javacResult = null;

                                    final Iterable<Path> javaFileRelativePathsToCompile = javaFilesToCompile
                                        .map(BuildJSONJavaFile::getRelativePath)
                                        .toList();

                                    final List<String> dependencyCompiledSourcesJarFilePaths = List.create();
                                    for (final JavaPublishedProjectFolder dependencyFolder : dependencyFolders)
                                    {
                                        final File dependencySourcesJarFile = dependencyFolder.getCompiledSourcesJarFile().await();
                                        dependencyCompiledSourcesJarFilePaths.add(dependencySourcesJarFile.toString());
                                    }

                                    final Folder outputsSourcesFolder = projectFolder.getOutputsSourcesFolder().await();
                                    final Path sourcesFolderRelativePath = projectFolder.getSourcesFolder().await()
                                        .relativeTo(projectFolder);
                                    final Iterable<String> javaSourceFileRelativePathsToCompile = javaFileRelativePathsToCompile
                                        .where(path -> path.startsWith(sourcesFolderRelativePath))
                                        .map(Path::toString)
                                        .order(Strings::lessThan)
                                        .toList();
                                    final int javaSourceFilesToCompileCount = javaSourceFileRelativePathsToCompile.getCount();
                                    if (javaSourceFilesToCompileCount > 0)
                                    {
                                        outputStream.writeLine("Compiling " + javaSourceFilesToCompileCount + " source file" + (javaSourceFilesToCompileCount == 1 ? "" : "s") + "...").await();
                                        javacResult = javac.compile((JavacParameters javacParameters) ->
                                        {
                                            javacParameters.addDirectory(outputsSourcesFolder);

                                            final List<String> classpath = List.create(outputsSourcesFolder.toString());
                                            classpath.addAll(dependencyCompiledSourcesJarFilePaths);
                                            javacParameters.addClasspath(classpath);

                                            javacParameters.addXLint("all", "-try", "-overrides");
                                            javacParameters.addArguments(javaSourceFileRelativePathsToCompile);
                                        }).await();

                                        process.setExitCode(javacResult.getExitCode());
                                    }

                                    if (process.getExitCode() == 0)
                                    {
                                        final Path testSourcesFolderRelativePath = projectFolder.getTestSourcesFolder().await()
                                            .relativeTo(projectFolder);
                                        final Folder testSourcesOutputFolder = projectFolder.getOutputsTestsFolder().await();
                                        final Iterable<String> javaTestSourceFileRelativePathsToCompile = javaFileRelativePathsToCompile
                                            .where(path -> path.startsWith(testSourcesFolderRelativePath))
                                            .map(Path::toString)
                                            .order(Strings::lessThan)
                                            .toList();
                                        final int javaTestSourceFilesToCompileCount = javaTestSourceFileRelativePathsToCompile.getCount();
                                        if (javaTestSourceFilesToCompileCount > 0)
                                        {
                                            final List<String> dependencyCompiledTestSourcesJarFilePaths = List.create();
                                            for (final JavaPublishedProjectFolder dependencyFolder : dependencyFolders)
                                            {
                                                final File dependencyTestSourcesJarFile = dependencyFolder.getCompiledTestsJarFile().await();
                                                if (dependencyTestSourcesJarFile.exists().await())
                                                {
                                                    dependencyCompiledTestSourcesJarFilePaths.add(dependencyTestSourcesJarFile.toString());
                                                }
                                            }

                                            outputStream.writeLine("Compiling " + javaTestSourceFilesToCompileCount + " test source file" + (javaTestSourceFilesToCompileCount == 1 ? "" : "s") + "...").await();
                                            javacResult = javac.compile((JavacParameters javacParameters) ->
                                            {
                                                javacParameters.addDirectory(testSourcesOutputFolder);

                                                final List<String> classpath = List.create(testSourcesOutputFolder.toString());
                                                if (outputsSourcesFolder.exists().await())
                                                {
                                                    classpath.add(outputsSourcesFolder.toString());
                                                }
                                                classpath.addAll(dependencyCompiledSourcesJarFilePaths);
                                                classpath.addAll(dependencyCompiledTestSourcesJarFilePaths);
                                                javacParameters.addClasspath(classpath);

                                                javacParameters.addXLint("all", "-try", "-overrides");
                                                javacParameters.addArguments(javaTestSourceFileRelativePathsToCompile);
                                            }).await();

                                            process.setExitCode(javacResult.getExitCode());
                                        }
                                    }

                                    if (javacResult != null)
                                    {
                                        verboseStream.writeLine("Adding compilation issues to new build.json...").await();
                                        final MutableMap<Path, List<JavacIssue>> sourceFilePathToIssueMap = Map.create();
                                        for (final JavacIssue issue : javacResult.getIssues())
                                        {
                                            if (Comparer.equalIgnoreCase("warning", issue.getType()))
                                            {
                                                newWarnings.add(issue);
                                            }
                                            else if (Comparer.equalIgnoreCase("error", issue.getType()))
                                            {
                                                newErrors.add(issue);
                                            }
                                            else
                                            {
                                                newUnrecognizedIssues.add(issue);
                                            }

                                            sourceFilePathToIssueMap.getOrSet(issue.getSourceFilePath(), List::create).await()
                                                .add(issue);
                                        }

                                        for (final MapEntry<Path, List<JavacIssue>> entry : sourceFilePathToIssueMap)
                                        {
                                            newBuildJson.getJavaFile(entry.getKey()).await()
                                                .setIssues(entry.getValue());
                                        }
                                    }

                                    verboseStream.writeLine("Associating .class files with original .java files...").await();
                                    final MutableMap<Path, List<BuildJSONClassFile>> sourceFilePathToClassFileMap = Map.create();
                                    for (final File classFile : projectFolder.iterateClassFiles())
                                    {
                                        final Path classFileRelativeToOutputsPath = classFile.relativeTo(outputsFolder);
                                        final List<String> classSourceFileRelativePathSegments = List.create(classFileRelativeToOutputsPath.getSegments());
                                        String typeName = classFile.getNameWithoutFileExtension();
                                        final int dollarSignIndex = typeName.indexOf('$');
                                        final boolean isAnonymousType = (dollarSignIndex >= 0);
                                        if (isAnonymousType)
                                        {
                                            typeName = typeName.substring(0, dollarSignIndex);
                                        }
                                        classSourceFileRelativePathSegments.removeLast();
                                        classSourceFileRelativePathSegments.add(typeName + ".java");

                                        File classSourceFile = projectFolder.getFile(Strings.join('/', classSourceFileRelativePathSegments)).await();
                                        final Path classSourceFileRelativeToProjectFolderPath = classSourceFile.relativeTo(projectFolder);
                                        sourceFilePathToClassFileMap.getOrSet(classSourceFileRelativeToProjectFolderPath, List::create).await()
                                            .add(BuildJSONClassFile.create(classFile.relativeTo(projectFolder), classFile.getLastModified().await()));
                                    }

                                    for (final MapEntry<Path, List<BuildJSONClassFile>> entry : sourceFilePathToClassFileMap)
                                    {
                                        final Path javaFileRelativePath = entry.getKey();
                                        final List<BuildJSONClassFile> classFiles = entry.getValue();
                                        final BuildJSONJavaFile buildJsonJavaFile = newBuildJson.getJavaFile(javaFileRelativePath)
                                            .catchError((Throwable e) -> verboseStream.writeLine(e.getMessage()).await())
                                            .await();
                                        if (buildJsonJavaFile != null)
                                        {
                                            buildJsonJavaFile.setClassFiles(classFiles);
                                        }
                                    }
                                }

                                JavaProjectBuild.writeIssues(outputStream, unmodifiedWarnings, "Unmodified Warning").await();
                                JavaProjectBuild.writeIssues(outputStream, unmodifiedUnrecognizedIssues, "Unmodified Unrecognized Issue").await();

                                JavaProjectBuild.writeIssues(outputStream, newWarnings, "Warning").await();
                                JavaProjectBuild.writeIssues(outputStream, newErrors, "Error").await();
                                JavaProjectBuild.writeIssues(outputStream, newUnrecognizedIssues, "Unrecognized Issue").await();

                                verboseStream.writeLine("Updating " + projectFolder.getBuildJsonRelativePath().await() + "...").await();
                                projectFolder.writeBuildJson(newBuildJson).await();
                            }
                        }
                    }
                }
            }
        }
    }

    static Result<Void> writeIssues(CharacterWriteStream writeStream, Iterable<JavacIssue> issues, String issueType)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(issues, "issues");
        PreCondition.assertNotNullAndNotEmpty(issueType, "issueType");

        return Result.create(() ->
        {
            final int issueCount = issues.getCount();
            if (issueCount >= 1)
            {
                writeStream.writeLine(issueCount + " " + issueType + (issueCount == 1 ? "" : "s") + ":").await();
                final Iterable<JavacIssue> orderedIssues = issues.order((JavacIssue lhs, JavacIssue rhs) -> lhs.getSourceFilePath().toString().compareTo(rhs.getSourceFilePath().toString()) < 0);
                for (final JavacIssue issue : orderedIssues)
                {
                    writeStream.writeLine(issue.getSourceFilePath() + " (Line " + issue.getLineNumber() + "): " + issue.getMessage()).await();
                }
            }
        });
    }
}
