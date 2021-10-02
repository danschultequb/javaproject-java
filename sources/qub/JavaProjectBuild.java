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

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to build. Defaults to the current folder.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final Folder dataFolder = process.getQubProjectDataFolder().await();
            final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDataFolder(dataFolder, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final CharacterToByteWriteStream outputStream = logStreams.getOutput();
                final VerboseCharacterToByteWriteStream verboseStream = logStreams.getVerbose();

                try
                {
                    verboseStream.writeLine("Parsing " + projectFolder.getProjectJsonFile().await() + "...").await();
                    final JavaProjectJSON projectJson = projectFolder.getProjectJson(outputStream).await();
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder, outputStream).await();
                    final VerboseChildProcessRunner childProcessRunner = VerboseChildProcessRunner.create(process, verboseStream);
                    final Javac javac = jdkFolder.getJavac(childProcessRunner).await();

                    final List<JavaPublishedProjectFolder> dependencyFolders = List.create();
                    final Iterable<ProjectSignature> declaredDependencies = projectJson.getDependencies();
                    if (!Iterable.isNullOrEmpty(declaredDependencies))
                    {
                        verboseStream.writeLine("Discovering dependencies...").await();
                        final MutableMap<ProjectSignature,List<Node1<ProjectSignature>>> dependencyPaths = Map.create();
                        final MutableMap<Tuple2<String,String>,List<VersionNumber>> dependencyVersions = Map.create();
                        final List<Throwable> dependencyErrors = List.create();
                        Traversal.createDepthFirstSearch((TraversalActions<Node1<ProjectSignature>,Void> actions, Node1<ProjectSignature> currentNode) ->
                        {
                            final ProjectSignature dependency = currentNode.getValue();
                            dependencyPaths.getOrSet(dependency, List::create).await()
                                .add(currentNode);
                            final List<VersionNumber> dependencyVersionList = dependencyVersions.getOrSet(Tuple.create(dependency.getPublisher(), dependency.getProject()), List::create).await();
                            if (!dependencyVersionList.contains(dependency.getVersion()))
                            {
                                dependencyVersionList.add(dependency.getVersion());
                            }

                            JavaPublishedProjectFolder.getIfExists(qubFolder, dependency)
                                .then((JavaPublishedProjectFolder dependencyFolder) ->
                                {
                                    dependencyFolders.add(dependencyFolder);

                                    final JavaProjectJSON dependencyProjectJson = dependencyFolder.getProjectJson().await();
                                    final Iterable<ProjectSignature> nextDependencies = dependencyProjectJson.getDependencies();
                                    actions.visitNodes(nextDependencies.map((ProjectSignature nextDependency) -> Node1.create(nextDependency).setNode1(currentNode)));
                                })
                                .catchError(dependencyErrors::add)
                                .await();
                        }).iterate(declaredDependencies.map(Node1::create)).await();

                        final Iterable<MapEntry<Tuple2<String,String>,List<VersionNumber>>> multipleVersionDependencies = dependencyVersions
                            .where((MapEntry<Tuple2<String,String>,List<VersionNumber>> entry) -> entry.getValue().getCount() > 1);
                        if (multipleVersionDependencies.any())
                        {
                            final InMemoryCharacterToByteStream errorMessage = InMemoryCharacterToByteStream.create();
                            final IndentedCharacterWriteStream indentedErrorMessage = IndentedCharacterWriteStream.create(errorMessage)
                                .setSingleIndent(" ");
                            for (final MapEntry<Tuple2<String, String>, List<VersionNumber>> dependency : multipleVersionDependencies)
                            {
                                final String publisher = dependency.getKey().getValue1();
                                final String project = dependency.getKey().getValue2();
                                final Iterable<VersionNumber> versions = dependency.getValue();
                                indentedErrorMessage.writeLine("Found more than one required version for package " + publisher + "/" + project + ":").await();
                                int number = 0;
                                for (final VersionNumber version : versions)
                                {
                                    ++number;
                                    final String numberString = number + ". ";
                                    final ProjectSignature dependencySignature = ProjectSignature.create(publisher, project, version);
                                    indentedErrorMessage.setCurrentIndent("");
                                    indentedErrorMessage.writeLine(numberString + dependencySignature).await();
                                    indentedErrorMessage.setCurrentIndent(Strings.repeat(' ', numberString.length()));

                                    final List<ProjectSignature> dependencyPath = List.create();
                                    Node1<ProjectSignature> dependencyNode = dependencyPaths.get(dependencySignature).await().first();
                                    // Skip the first node because that's the dependency itself (not the path to the dependency).
                                    dependencyNode = dependencyNode.getNode1();
                                    while (dependencyNode != null)
                                    {
                                        dependencyPath.insert(0, dependencyNode.getValue());
                                        dependencyNode = dependencyNode.getNode1();
                                    }

                                    for (final ProjectSignature pathProjectSignature : dependencyPath)
                                    {
                                        indentedErrorMessage.increaseIndent();
                                        indentedErrorMessage.writeLine("from " + pathProjectSignature).await();
                                    }
                                }
                            }
                            dependencyErrors.add(new RuntimeException(errorMessage.getText().await().trim()));
                        }

                        final int dependencyErrorCount = dependencyErrors.getCount();
                        if (dependencyErrorCount >= 1)
                        {
                            final boolean singleError = (dependencyErrorCount == 1);
                            final String errorString = singleError ? "An error" : "Errors";
                            outputStream.writeLine(errorString + " occurred while discovering dependencies:").await();
                            int errorNumber = 0;
                            for (final Throwable dependencyError : dependencyErrors)
                            {
                                errorNumber++;
                                final IndentedCharacterWriteStream indentedOutputStream = IndentedCharacterWriteStream.create(outputStream);
                                final String errorNumberString = errorNumber + ". ";
                                indentedOutputStream.write(errorNumberString).await();
                                indentedOutputStream.setCurrentIndent(Strings.repeat(' ', errorNumberString.length()));
                                indentedOutputStream.writeLine(dependencyError.getMessage()).await();
                            }
                            throw ErrorIterable.create(dependencyErrors);
                        }
                    }

                    final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                    verboseStream.writeLine("Parsing " + projectFolder.getBuildJsonRelativePath().await() + "...").await();
                    final BuildJSON buildJson = projectFolder.getBuildJson()
                        .catchError(FileNotFoundException.class, BuildJSON::create)
                        .await();

                    JavaProjectJSON buildJsonProjectJson = buildJson.getProjectJson();
                    if (buildJsonProjectJson == null)
                    {
                        buildJsonProjectJson = JavaProjectJSON.create();
                    }

                    verboseStream.writeLine("Checking if dependencies have changed since the previous build...").await();
                    boolean modifiedPreviousDependencies = false;
                    for (final ProjectSignature previousDependency : buildJsonProjectJson.getDependencies())
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

                    final Iterable<JavaFile> javaFiles = projectFolder.getJavaFiles().await();

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
                        final Iterable<JavaClassFile> classFiles = projectFolder.getClassFiles().await();

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
                                    Iterable<Path> dependencies = buildJsonJavaFile.getDependencies();
                                    if (dependencies == null)
                                    {
                                        dependencies = Iterable.create();
                                    }
                                    newBuildJsonJavaFile.addDependencies(dependencies);
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
                                try (final CharacterToByteReadStream fileStream = projectFolder.getFileContentReadStream(javaFileRelativePath).await())
                                {
                                    javaFileWords = Strings.iterateWords(CharacterReadStream.iterate(fileStream)).toSet();
                                }

                                final String javaFileTypeName = javaFileRelativePath.getNameWithoutFileExtension();
                                for (final String javaFileWord : javaFileWords.order(Strings::lessThan))
                                {
                                    if (!javaFileWord.equals(javaFileTypeName))
                                    {
                                        final Path dependencyPath = typeNamesToPathMap.get(javaFileWord).catchError().await();
                                        if (dependencyPath != null)
                                        {
                                            javaFileToCompile.addDependency(dependencyPath);
                                        }
                                    }
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
                                            javaFilesToCompileChanged = true;
                                        }
                                    }
                                }
                            }
                        }

                        verboseStream.writeLine("Discovering unmodified .java files that have missing or modified .class files...").await();
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
                                    Path classFileRelativePath = buildJsonJavaFileClassFile.getRelativePath();
                                    final JavaClassFile javaClassFile = classFiles.first((JavaClassFile classFile) -> classFile.relativeTo(projectFolder).equals(classFileRelativePath));
                                    if (javaClassFile == null ||
                                        !javaClassFile.getLastModified().await().equals(buildJsonJavaFileClassFile.getLastModified()))
                                    {
                                        verboseStream.writeLine(javaFileRelativePath + " - Missing or modified class file(s)").await();
                                        shouldCompileJavaFile = true;
                                        break;
                                    }
                                }
                            }

                            if (shouldCompileJavaFile)
                            {
                                javaFilesToCompile.add(unmodifiedJavaFile);
                                unmodifiedJavaFiles.remove(unmodifiedJavaFile);
                            }
                        }

                        final List<JavacIssue> unmodifiedWarnings = List.create();
                        final List<JavacIssue> unmodifiedErrors = List.create();
                        final List<JavacIssue> unmodifiedUnrecognizedIssues = List.create();
                        for (final BuildJSONJavaFile unmodifiedJavaFile : unmodifiedJavaFiles)
                        {
                            for (final JavacIssue issue : unmodifiedJavaFile.getIssues())
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
                            final int javaFilesToCompileCount = javaFilesToCompile.getCount();
                            outputStream.writeLine("Compiling " + javaFilesToCompileCount + " source file" + (javaFilesToCompileCount == 1 ? "" : "s") + "...").await();
                            final JavacResult javacResult = javac.compile((JavacParameters javacParameters) ->
                            {
                                javacParameters.addDirectory(outputsFolder);

                                final List<String> classpath = List.create(outputsFolder.toString());
                                if (dependencyFolders.any())
                                {
                                    classpath.addAll(dependencyFolders.map(folder -> folder.getCompiledSourcesJarFile().await().toString()));
                                }
                                javacParameters.addClasspath(classpath);

                                javacParameters.addXLint("all", "-try", "-overrides");
                                javacParameters.addArguments(javaFilesToCompile
                                    .map(BuildJSONJavaFile::getRelativePath)
                                    .map(Path::toString)
                                    .order(Strings::lessThan));
                            }).await();

                            process.setExitCode(javacResult.getExitCode());

                            verboseStream.writeLine("Adding compilation issues to new build.json...").await();
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

                                newBuildJson.getJavaFile(issue.getSourceFilePath()).await()
                                    .addIssue(issue);
                            }

                            verboseStream.writeLine("Associating .class files with original .java files...").await();
                            for (final File classFile : projectFolder.getClassFiles().await())
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

                                final Path classSourceFileRelativeToSourceFolderPath = Path.parse(Strings.join('/', classSourceFileRelativePathSegments));
                                File classSourceFile = projectFolder.getSourcesFolder().await()
                                    .getFile(classSourceFileRelativeToSourceFolderPath).await();
                                if (!classSourceFile.exists().await())
                                {
                                    classSourceFile = projectFolder.getTestsFolder().await()
                                        .getFile(classSourceFileRelativeToSourceFolderPath).await();
                                }
                                final Path classSourceFileRelativeToProjectFolderPath = classSourceFile.relativeTo(projectFolder);
                                final BuildJSONJavaFile javaFile = newBuildJson.getJavaFile(classSourceFileRelativeToProjectFolderPath).await();
                                javaFile.addClassFile(classFile.relativeTo(projectFolder), classFile.getLastModified().await());
                            }
                        }

                        JavaProjectBuild.writeIssues(outputStream, unmodifiedWarnings, "Unmodified Warning").await();
                        JavaProjectBuild.writeIssues(outputStream, unmodifiedErrors, "Unmodified Error").await();
                        JavaProjectBuild.writeIssues(outputStream, unmodifiedUnrecognizedIssues, "Unmodified Unrecognized Issue").await();

                        JavaProjectBuild.writeIssues(outputStream, newWarnings, "Warning").await();
                        JavaProjectBuild.writeIssues(outputStream, newErrors, "Error").await();
                        JavaProjectBuild.writeIssues(outputStream, newUnrecognizedIssues, "Unrecognized Issue").await();

                        verboseStream.writeLine("Updating " + projectFolder.getBuildJsonRelativePath().await() + "...").await();
                        projectFolder.writeBuildJson(newBuildJson).await();
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
