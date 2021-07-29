package qub;

public interface JavaProjectBuild
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("build", JavaProjectBuild::run)
            .setDescription("Build a Java source code project.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to build. Defaults to the current folder.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final Folder dataFolder = process.getQubProjectDataFolder().await();
            final Folder projectFolder = projectFolderParameter.getValue().await();

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDataFolder(dataFolder, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final CharacterToByteWriteStream outputStream = logStreams.getOutput();
                final VerboseCharacterToByteWriteStream verboseStream = logStreams.getVerbose();

                try
                {
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    verboseStream.writeLine("Parsing " + projectJsonFile + "...").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile)
                        .onError(FileNotFoundException.class, () ->
                        {
                            outputStream.writeLine("No project.json file exists in the project folder at " + Strings.escapeAndQuote(projectFolder) + ".").await();
                        })
                        .onError(ParseException.class, (ParseException error) ->
                        {
                            outputStream.writeLine("Invalid project.json file: " + error.getMessage()).await();
                        })
                        .await();
                    if (projectJson != null)
                    {
                        final QubFolder qubFolder = process.getQubFolder().await();
                        final String jdkPublisherName = "openjdk";
                        final String jdkProjectName = "jdk";
                        final QubProjectVersionFolder latestJdkFolder = qubFolder.getProjectLatestVersionFolder(jdkPublisherName, jdkProjectName)
                            .onError(NotFoundException.class, () ->
                            {
                                outputStream.writeLine("No openjdk/jdk project is installed in the qub folder at " + Strings.escapeAndQuote(qubFolder) + ".").await();
                            })
                            .await();
                        final Javac javac = Javac.create(process.getChildProcessRunner())
                            .setExecutablePath(latestJdkFolder.getFile("bin/javac.exe").await());

                        final List<QubProjectVersionFolder> dependencyFolders = List.create();
                        final ProjectJSONJava projectJsonJava = projectJson.getJava();
                        final List<ProjectSignature> declaredDependencies = List.create();
                        if (projectJsonJava != null)
                        {
                            declaredDependencies.addAll(projectJsonJava.getDependencies());

                            verboseStream.writeLine("Discovering dependencies...").await();
                            final Map<ProjectSignature, Iterable<ProjectSignature>> dependencyMap = projectJsonJava.getTransitiveDependencyPaths(qubFolder);
                            if (!Iterable.isNullOrEmpty(dependencyMap))
                            {
                                final Iterable<ProjectSignature> allDependencies = dependencyMap.getKeys();
                                final Set<ProjectSignature> errorDependencies = Set.create();
                                for (final ProjectSignature dependency : allDependencies)
                                {
                                    if (!errorDependencies.contains(dependency))
                                    {
                                        final Iterable<ProjectSignature> matchingDependencies = allDependencies.where(dependency::equalsIgnoreVersion).toList();
                                        if (matchingDependencies.getCount() > 1)
                                        {
                                            errorDependencies.addAll(matchingDependencies);
                                            final InMemoryCharacterToByteStream errorMessage = InMemoryCharacterToByteStream.create();
                                            final IndentedCharacterWriteStream indentedErrorMessage = IndentedCharacterWriteStream.create(errorMessage)
                                                .setSingleIndent(" ");
                                            indentedErrorMessage.writeLine("Found more than one required version for package " + dependency.toStringIgnoreVersion() + ":").await();
                                            int number = 0;
                                            for (final ProjectSignature matchingProjectSignature : matchingDependencies)
                                            {
                                                ++number;
                                                final String numberString = number + ". ";
                                                indentedErrorMessage.setCurrentIndent("");
                                                errorMessage.writeLine(numberString + matchingProjectSignature).await();
                                                indentedErrorMessage.setCurrentIndent(Strings.repeat(' ', numberString.length()));
                                                final Iterable<ProjectSignature> path = dependencyMap.get(matchingProjectSignature).await();
                                                for (final ProjectSignature pathProjectSignature : path)
                                                {
                                                    indentedErrorMessage.increaseIndent();
                                                    indentedErrorMessage.writeLine("from " + pathProjectSignature).await();
                                                }
                                            }
                                            throw new RuntimeException(errorMessage.getText().await());
                                        }
                                    }
                                }

                                for (final ProjectSignature dependency : allDependencies)
                                {
                                    final String dependencyPublisher = dependency.getPublisher();
                                    final QubPublisherFolder publisherFolder = qubFolder.getPublisherFolder(dependencyPublisher).await();
                                    if (!publisherFolder.exists().await())
                                    {
                                        throw new NotFoundException("No publisher folder named " + Strings.escapeAndQuote(dependencyPublisher) + " found in the Qub folder (" + qubFolder + ").");
                                    }
                                    else
                                    {
                                        final String dependencyProject = dependency.getProject();
                                        final QubProjectFolder dependencyProjectFolder = publisherFolder.getProjectFolder(dependencyProject).await();
                                        if (!dependencyProjectFolder.exists().await())
                                        {
                                            throw new NotFoundException("No project folder named " + Strings.escapeAndQuote(dependencyProject) + " found in the " + Strings.escapeAndQuote(dependencyPublisher) + " publisher folder (" + publisherFolder + ").");
                                        }
                                        else
                                        {
                                            final VersionNumber dependencyVersion = dependency.getVersion();
                                            final QubProjectVersionFolder dependencyVersionFolder = dependencyProjectFolder.getProjectVersionFolder(dependencyVersion).await();
                                            if (!dependencyVersionFolder.exists().await())
                                            {
                                                throw new NotFoundException("No version folder named " + Strings.escapeAndQuote(dependencyVersion) + " found in the " + Strings.escapeAndQuote(dependency.toStringIgnoreVersion()) + " project folder (" + projectFolder + ").");
                                            }
                                            else
                                            {
                                                dependencyFolders.add(dependencyVersionFolder);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                        final File buildJsonFile = outputsFolder.getFile("build.json").await();
                        verboseStream.writeLine("Parsing " + buildJsonFile.relativeTo(projectFolder) + "...").await();
                        final BuildJSON previousBuildJson = BuildJSON.parse(buildJsonFile)
                            .catchError(FileNotFoundException.class, BuildJSON::create)
                            .await();

                        final ProjectJSON previousProjectJson = previousBuildJson.getProjectJson();
                        final ProjectJSONJava previousProjectJsonJava = previousProjectJson.getJava();

                        verboseStream.writeLine("Checking if dependencies have changed since the previous build...").await();
                        final boolean declaredDependenciesChanged = !declaredDependencies.toSet().equals(previousProjectJsonJava.getDependencies().toSet());
                        verboseStream.writeLine("  Dependencies have " + (declaredDependenciesChanged ? "" : "not ") + "changed.").await();

                        verboseStream.writeLine("Checking if latest installed JDK has changed since the previous build...").await();
                        final VersionNumber javacVersionNumber = javac.version().await();
                        final boolean javacVersionChanged = !javacVersionNumber.equals(previousBuildJson.getJavacVersion());
                        verboseStream.writeLine("  Installed JDK has " + (javacVersionChanged ? "" : "not ") + "changed.").await();

                        final Map<Path,DateTime> sourceFiles = JavaProjectBuild.getJavaFiles(projectFolder);

                        verboseStream.writeLine("Looking for source files that have been deleted...").await();
                        final List<Path> deletedSourceFilePaths = List.create();
                        for (final BuildJSONSourceFile previousBuildJsonSourceFile : previousBuildJson.getSourceFiles())
                        {
                            final Path previousSourceFilePath = previousBuildJsonSourceFile.getRelativePath();
                            if (!sourceFiles.containsKey(previousSourceFilePath))
                            {
                                verboseStream.writeLine(previousSourceFilePath + " - Deleted").await();
                                deletedSourceFilePaths.add(previousSourceFilePath);
                                for (final Path previousSourceClassFilePath : previousBuildJsonSourceFile.getClassFiles().getKeys())
                                {
                                    projectFolder.deleteFile(previousSourceClassFilePath)
                                        .catchError()
                                        .await();
                                }
                            }
                        }

                        final Map<Path,DateTime> classFiles = JavaProjectBuild.getClassFiles(projectFolder);

                        verboseStream.writeLine("Looking for source files to compile...").await();
                        final List<BuildJSONSourceFile> sourceFilesToCompile = List.create();
                        final List<BuildJSONSourceFile> unmodifiedSourceFiles = List.create();
                        final List<BuildJSONSourceFile> sourceFilesWithNewContent = List.create();
                        for (final MapEntry<Path,DateTime> sourceFileEntry : sourceFiles)
                        {
                            final Path relativeSourceFilePath = sourceFileEntry.getKey();
                            final DateTime sourceFileLastModified = sourceFileEntry.getValue();
                            final BuildJSONSourceFile previousBuildJsonSourceFile = previousBuildJson.getSourceFile(relativeSourceFilePath)
                                .catchError(NotFoundException.class)
                                .await();
                            boolean shouldCompileSourceFile = declaredDependenciesChanged || javacVersionChanged;
                            boolean hasNewContent = false;
                            if (previousBuildJsonSourceFile == null)
                            {
                                verboseStream.writeLine(relativeSourceFilePath + " - New file").await();
                                hasNewContent = true;
                                shouldCompileSourceFile = true;
                            }
                            else if (!sourceFileLastModified.equals(previousBuildJsonSourceFile.getLastModified()))
                            {
                                verboseStream.writeLine(relativeSourceFilePath + " - Modified").await();
                                hasNewContent = true;
                                shouldCompileSourceFile = true;
                            }
                            else
                            {
                                final Map<Path,DateTime> previousClassFiles = previousBuildJsonSourceFile.getClassFiles();
                                if (!previousClassFiles.any())
                                {
                                    verboseStream.writeLine(relativeSourceFilePath + " - Missing class file(s)").await();
                                    shouldCompileSourceFile = true;
                                }
                                else if (previousClassFiles
                                    .where((MapEntry<Path,DateTime> sourceFileClassFile) -> !classFiles.contains(sourceFileClassFile))
                                    .any())
                                {
                                    verboseStream.writeLine(relativeSourceFilePath + " - Missing or modified class file(s)").await();
                                    shouldCompileSourceFile = true;
                                }
                            }

                            if (shouldCompileSourceFile)
                            {
                                final BuildJSONSourceFile buildJsonSourceFile = BuildJSONSourceFile.create(relativeSourceFilePath)
                                    .setLastModified(sourceFileLastModified);

                                sourceFilesToCompile.add(buildJsonSourceFile);
                                if (hasNewContent)
                                {
                                    sourceFilesWithNewContent.add(buildJsonSourceFile);
                                }
                                else
                                {
                                    buildJsonSourceFile.addDependencies(previousBuildJsonSourceFile.getDependencies());
                                }
                            }
                            else
                            {
                                unmodifiedSourceFiles.add(previousBuildJsonSourceFile);
                            }
                        }

                        verboseStream.writeLine("Update source file dependencies...").await();
                        if (sourceFilesWithNewContent.any())
                        {
                            final Map<String,Path> typeNamesToPathMap = sourceFiles.getKeys()
                                .toMap(JavaProjectBuild::getFileTypeName, (Path sourceFilePath) -> sourceFilePath);

                            for (final BuildJSONSourceFile sourceFileToCompile : sourceFilesWithNewContent)
                            {
                                final Path sourceFileRelativePath = sourceFileToCompile.getRelativePath();
                                final Set<String> sourceFileWords = Set.create();
                                try (final CharacterToByteReadStream fileStream = projectFolder.getFileContentReadStream(sourceFileRelativePath).await())
                                {
                                    sourceFileWords.addAll(Strings.iterateWords(CharacterReadStream.iterate(fileStream)));
                                }

                                final String sourceFileTypeName = JavaProjectBuild.getFileTypeName(sourceFileRelativePath);
                                for (final String sourceFileWord : sourceFileWords.order(Strings::lessThan))
                                {
                                    if (!sourceFileWord.equals(sourceFileTypeName))
                                    {
                                        final Path dependencyPath = typeNamesToPathMap.get(sourceFileWord).catchError().await();
                                        if (dependencyPath != null)
                                        {
                                            sourceFileToCompile.addDependency(dependencyPath);
                                        }
                                    }
                                }
                            }
                        }

                        verboseStream.writeLine("Discovering unmodified source files that have dependencies that are being compiled or were deleted...").await();
                        if (sourceFilesToCompile.any())
                        {
                            boolean sourceFilesToCompileChanged = true;
                            while (unmodifiedSourceFiles.any() && sourceFilesToCompileChanged)
                            {
                                sourceFilesToCompileChanged = false;

                                for (final BuildJSONSourceFile unmodifiedSourceFile : unmodifiedSourceFiles.toList())
                                {
                                    final Iterable<Path> unmodifiedSourceFileDependencies = unmodifiedSourceFile.getDependencies();
                                    final Iterable<Path> sourceFilePathsToCompile = sourceFilesToCompile.map(BuildJSONSourceFile::getRelativePath);
                                    boolean shouldCompileSourceFile = false;

                                    final Iterable<Path> unmodifiedSourceFileDependenciesBeingCompiled = unmodifiedSourceFileDependencies.where(sourceFilePathsToCompile::contains);
                                    if (unmodifiedSourceFileDependenciesBeingCompiled.any())
                                    {
                                        verboseStream.writeLine(unmodifiedSourceFile.getRelativePath() + " - Dependency file(s) being compiled").await();
                                        shouldCompileSourceFile = true;
                                    }
                                    else
                                    {
                                        final Iterable<Path> unmodifiedSourceFileDependenciesThatWereDeleted = unmodifiedSourceFileDependencies.where(deletedSourceFilePaths::contains);
                                        if (unmodifiedSourceFileDependenciesThatWereDeleted.any())
                                        {
                                            verboseStream.writeLine(unmodifiedSourceFile.getRelativePath() + " - Dependency file(s) were deleted").await();
                                            shouldCompileSourceFile = true;
                                        }
                                    }

                                    if (shouldCompileSourceFile)
                                    {
                                        sourceFilesToCompile.add(unmodifiedSourceFile);
                                        unmodifiedSourceFiles.remove(unmodifiedSourceFile);
                                        sourceFilesToCompileChanged = true;
                                    }
                                }
                            }
                        }

                        final List<JavacIssue> unmodifiedWarnings = List.create();
                        final List<JavacIssue> unmodifiedErrors = List.create();
                        final List<JavacIssue> unmodifiedUnrecognizedIssues = List.create();
                        for (final BuildJSONSourceFile unmodifiedSourceFile : unmodifiedSourceFiles)
                        {
                            for (final JavacIssue issue : unmodifiedSourceFile.getIssues())
                            {
                                if (Comparer.equalIgnoreCase("warning", issue.getType()))
                                {
                                    unmodifiedWarnings.add(issue);
                                }
                                else if (Comparer.equalIgnoreCase("error", issue.getType()))
                                {
                                    unmodifiedErrors.add(issue);
                                }
                                else
                                {
                                    unmodifiedUnrecognizedIssues.add(issue);
                                }
                            }
                        }

                        final BuildJSON newBuildJson = BuildJSON.create()
                            .setJavacVersion(javacVersionNumber)
                            .setProjectJson(projectJson)
                            .setSourceFiles(unmodifiedSourceFiles)
                            .setSourceFiles(sourceFilesToCompile);
                        final List<JavacIssue> newWarnings = List.create();
                        final List<JavacIssue> newErrors = List.create();
                        final List<JavacIssue> newUnrecognizedIssues = List.create();
                        if (!sourceFilesToCompile.any())
                        {
                            outputStream.writeLine("No files need to be compiled.").await();
                        }
                        else
                        {
                            final int sourceFilesToCompileCount = sourceFilesToCompile.getCount();
                            outputStream.writeLine("Compiling " + sourceFilesToCompileCount + " source file" + (sourceFilesToCompileCount == 1 ? "" : "s") + "...").await();
                            final JavacResult javacResult = javac.compile((JavacParameters javacParameters) ->
                            {
                                final Folder sourcesOutputFolder = outputsFolder.getFolder("sources").await();
                                javacParameters.addDirectory(sourcesOutputFolder);

                                javacParameters.addClasspath(sourcesOutputFolder);
                                for (final QubProjectVersionFolder dependencyFolder : dependencyFolders)
                                {
                                    final File compiledSourcesFile = dependencyFolder.getCompiledSourcesFile().await();
                                    javacParameters.addClasspath(compiledSourcesFile);
                                }

                                javacParameters.addXLint();
                                javacParameters.addArguments(sourceFilesToCompile
                                    .map(BuildJSONSourceFile::getRelativePath)
                                    .map(Path::toString));
                            }).await();

                            process.setExitCode(javacResult.getExitCode());

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

                                newBuildJson.getSourceFile(issue.getSourceFilePath()).await()
                                    .addIssue(issue);
                            }
                        }

                        JavaProjectBuild.writeIssues(outputStream, unmodifiedWarnings, "Unmodified Warning").await();
                        JavaProjectBuild.writeIssues(outputStream, unmodifiedErrors, "Unmodified Error").await();
                        JavaProjectBuild.writeIssues(outputStream, unmodifiedUnrecognizedIssues, "Unmodified Unrecognized Issue").await();

                        JavaProjectBuild.writeIssues(outputStream, newWarnings, "Warning").await();
                        JavaProjectBuild.writeIssues(outputStream, newErrors, "Error").await();
                        JavaProjectBuild.writeIssues(outputStream, newUnrecognizedIssues, "Unrecognized Issue").await();

                        verboseStream.writeLine("Updating " + buildJsonFile + "...").await();
                        try (final CharacterWriteStream writeStream = CharacterWriteStream.create(ByteWriteStream.buffer(buildJsonFile.getContentsByteWriteStream().await())))
                        {
                            newBuildJson.toString(writeStream, JSONFormat.pretty).await();
                        }
                    }
                }
                catch (Throwable error)
                {
                    process.setExitCode(-1);
                    verboseStream.write(error.toString()).await();
                }
            }
        }
    }

    static Map<Path,DateTime> getJavaFiles(Folder folder)
    {
        return JavaProjectBuild.getFiles(folder, ".java");
    }

    static Map<Path,DateTime> getClassFiles(Folder folder)
    {
        return JavaProjectBuild.getFiles(folder, ".class");
    }

    static Map<Path,DateTime> getFiles(Folder folder, String fileExtension)
    {
        PreCondition.assertNotNullAndNotEmpty(fileExtension, "fileExtension");
        PreCondition.assertStartsWith(fileExtension, ".", "fileExtension");

        return folder.iterateFilesRecursively()
            .where((File file) -> Comparer.equalIgnoreCase(fileExtension, file.getFileExtension()))
            .toMap((File file) -> file.relativeTo(folder), (File file) -> file.getLastModified().await());
    }

    static String getFileTypeName(Path filePath)
    {
        PreCondition.assertNotNull(filePath, "filePath");

        return filePath.withoutFileExtension().getSegments().last();
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
