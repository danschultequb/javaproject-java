package qub;

public interface JavaProjectTest
{
    String patternParameterName = "pattern";
    String coverageParameterName = "coverage";
    String testJsonParameterName = "testjson";
    String logFileParameterName = "logfile";
    String profilerParameterName = "profiler";

    static CommandLineAction addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        return actions.addAction("test", JavaProjectTest::run)
            .setDescription("Run the tests of a Java source code project.");
    }

    static CommandLineParameter<Folder> addProjectFolderParameter(CommandLineParameters parameters, DesktopProcess process)
    {
        return JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that contains a Java project to test. Defaults to the current folder.");
    }

    static CommandLineParameter<PathPattern> addPattern(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.add(JavaProjectTest.patternParameterName, (String argumentValue) ->
            {
                return Result.create(()  ->
                {
                    return Strings.isNullOrEmpty(argumentValue)
                        ? null
                        : PathPattern.parse(argumentValue);
                });
            })
            .setValueName("<test-name-pattern>")
            .setValueRequired(true)
            .setDescription("The pattern to match against tests to determine if they will be run.");
    }

    static CommandLineParameter<Coverage> addCoverage(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.addEnum(JavaProjectTest.coverageParameterName, Coverage.None, Coverage.Sources)
            .setValueRequired(false)
            .setValueName("<None|Sources|Tests|All>")
            .addAlias("c")
            .setDescription("Whether code coverage information will be collected while running tests.");
    }

    static CommandLineParameterBoolean addTestJson(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.addBoolean(JavaProjectTest.testJsonParameterName, true)
            .setDescription("Whether to use a test.json file to cache test results in.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        JavaProjectTest.run(process, action, null, null);
    }

    static void run(DesktopProcess process, CommandLineAction action, JavaProjectFolder providedProjectFolder, File providedLogFile)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectTest.addProjectFolderParameter(parameters, process);
        final CommandLineParameter<PathPattern> patternParameter = JavaProjectTest.addPattern(parameters);
        final CommandLineParameter<Coverage> coverageParameter = JavaProjectTest.addCoverage(parameters);
        final CommandLineParameterBoolean testJsonParameter = JavaProjectTest.addTestJson(parameters);
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

            JavaProjectBuild.run(process, action, projectFolder, logFile);

            if (process.getExitCode() == 0)
            {
                try (final LogStreams logStreamsBeforeTests = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await()))
                {
                    final QubFolder qubFolder = process.getQubFolder().await();

                    final Coverage coverage = coverageParameter.getValue().await();
                    final QubProjectVersionFolder jacocoFolder = coverage == Coverage.None
                        ? null
                        : qubFolder.getLatestProjectVersionFolder("jacoco", "jacococli").await();

                    final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                    final Folder outputsSourcesFolder = projectFolder.getOutputsSourcesFolder().await();
                    final Folder outputsTestsFolder = projectFolder.getOutputsTestsFolder().await();

                    final ChildProcessRunner customChildProcessRunner = CustomChildProcessRunner.create(process.getChildProcessRunner())
                        .setBeforeChildProcessStarted(() -> logStreamsBeforeTests.dispose().await());
                    final VerboseChildProcessRunner childProcessRunnerForTests = VerboseChildProcessRunner.create(customChildProcessRunner, logStreamsBeforeTests.getVerbose());
                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder).await();
                    final Java javaForTests = jdkFolder.getJava(childProcessRunnerForTests).await();

                    process.setExitCode(javaForTests.run((JavaParameters javaParameters) ->
                    {
                        if (jacocoFolder != null)
                        {
                            final File jacocoAgentJarFile = jacocoFolder.getFile("jacocoagent.jar").await();
                            final File coverageExecFile = outputsFolder.getFile("coverage.exec").await();
                            javaParameters.addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile);
                        }

                        final List<FileSystemEntry> classpathEntries = List.create();
                        classpathEntries.addAll(
                            outputsSourcesFolder,
                            outputsTestsFolder);
                        final Iterable<JavaPublishedProjectFolder> dependencyFolders = projectFolder.getAllDependencyFolders(qubFolder, false).await();
                        for (final JavaPublishedProjectFolder dependencyFolder : dependencyFolders)
                        {
                            classpathEntries.addAll(
                                dependencyFolder.getCompiledSourcesJarFile().await(),
                                dependencyFolder.getCompiledTestsJarFile().await());
                        }
                        final String jvmClassPath = process.getJVMClasspath().await();
                        if (!Strings.isNullOrEmpty(jvmClassPath))
                        {
                            final FileSystem fileSystem = process.getFileSystem();
                            final ProjectJSON projectJson = projectFolder.getProjectJson().await();
                            final Path currentFolderPath = process.getCurrentFolderPath();

                            final String[] jvmClassPaths = jvmClassPath.split(";");
                            for (final String jvmClassPathString : jvmClassPaths)
                            {
                                Path jvmClassPathPath = Path.parse(jvmClassPathString);
                                if (!jvmClassPathPath.isRooted())
                                {
                                    jvmClassPathPath = currentFolderPath.resolve(jvmClassPathPath).await();
                                }

                                if (!qubFolder.isAncestorOf(jvmClassPathPath).await())
                                {
                                    if (fileSystem.fileExists(jvmClassPathPath).await())
                                    {
                                        classpathEntries.add(fileSystem.getFile(jvmClassPathPath).await());
                                    }
                                    else if (fileSystem.folderExists(jvmClassPathPath).await())
                                    {
                                        classpathEntries.add(fileSystem.getFolder(jvmClassPathPath).await());
                                    }
                                }
                                else
                                {
                                    final Path relativeJvmClassPath = jvmClassPathPath.relativeTo(qubFolder);
                                    final Indexable<String> segments = relativeJvmClassPath.getSegments();
                                    final String publisher = segments.get(0);
                                    final String project = segments.get(1);
                                    final String version = segments.get(3);
                                    final ProjectSignature jvmProjectSignature = ProjectSignature.create(publisher, project, version);
                                    if (!jvmProjectSignature.equalsIgnoreVersion(projectJson.getPublisher(), projectJson.getProject()))
                                    {
                                        final JavaPublishedProjectFolder jvmProjectFolder = JavaPublishedProjectFolder.getIfExists(qubFolder, jvmProjectSignature).catchError().await();
                                        if (!dependencyFolders.contains(jvmProjectFolder))
                                        {
                                            if (fileSystem.fileExists(jvmClassPathPath).await())
                                            {
                                                classpathEntries.add(fileSystem.getFile(jvmClassPathPath).await());
                                            }
                                            else if (fileSystem.folderExists(jvmClassPathPath).await())
                                            {
                                                classpathEntries.add(fileSystem.getFolder(jvmClassPathPath).await());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        final Iterable<String> classpaths = classpathEntries
                            .where((FileSystemEntry entry) -> entry.exists().await())
                            .map(FileSystemEntry::toString)
                            .toList();
                        javaParameters.addClasspath(classpaths);

                        javaParameters.addArgument(Types.getFullTypeName(JavaProjectTest.class));

                        final boolean verbose = verboseParameter.isVerbose();
                        javaParameters.addArgument("--verbose=" + verbose);

                        final boolean testJson = testJsonParameter.getValue().await();
                        javaParameters.addArgument("--" + JavaProjectTest.testJsonParameterName + "=" + testJson);

                        javaParameters.addArgument("--" + JavaProjectTest.logFileParameterName + "=" + logFile.toString());

                        final PathPattern pattern = patternParameter.getValue().await();
                        if (pattern != null)
                        {
                            javaParameters.addArgument("--" + JavaProjectTest.patternParameterName + "=" + pattern.toString());
                        }

                        javaParameters.addArgument("--" + JavaProject.projectFolderParameterName + "=" + projectFolder.toString());

                        javaParameters.addArgument("--" + JavaProjectTest.coverageParameterName + "=" + coverage.toString());

                        javaParameters.addArgument("--" + JavaProjectTest.profilerParameterName + "=" + Booleans.toString(profilerParameter.getValue().await()));

                        javaParameters.redirectOutputTo(process.getOutputWriteStream());
                        javaParameters.redirectErrorTo(process.getErrorWriteStream());
                        javaParameters.setInputStream(process.getInputReadStream());
                    }).await());

                    if (jacocoFolder != null)
                    {
                        try (final LogStreams logStreamsAfterTests = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await()))
                        {
                            final CharacterToByteWriteStream output = logStreamsAfterTests.getOutput();
                            
                            output.writeLine().await();
                            output.writeLine("Analyzing coverage...").await();

                            final Folder coverageFolder = outputsFolder.getFolder("coverage").await();

                            final VerboseChildProcessRunner childProcessRunnerForCoverage = VerboseChildProcessRunner.create(process, logStreamsAfterTests.getVerbose());
                            final Java javaForCoverage = jdkFolder.getJava(childProcessRunnerForCoverage).await();
                            process.setExitCode(javaForCoverage.run((JavaParameters javaParameters) ->
                            {
                                final File jacococliJarFile = jacocoFolder.getFile("jacococli.jar").await();
                                javaParameters.addArguments("-jar", jacococliJarFile.toString());

                                javaParameters.addArgument("report");

                                final File coverageExecFile = outputsFolder.getFile("coverage.exec").await();
                                javaParameters.addArgument(coverageExecFile.toString());

                                if (coverage == Coverage.Sources || coverage == Coverage.All)
                                {
                                    javaParameters.addArguments("--classfiles", outputsSourcesFolder.toString());
                                    javaParameters.addArguments("--sourcefiles", projectFolder.getSourcesFolder().await().toString());
                                }
                                if (coverage == Coverage.Tests || coverage == Coverage.All)
                                {
                                    javaParameters.addArguments("--classfiles", outputsTestsFolder.toString());
                                    javaParameters.addArguments("--sourcesfiles", projectFolder.getTestSourcesFolder().await().toString());
                                }

                                javaParameters.addArguments("--html", coverageFolder.toString());
                            }).await());

                            if (process.getExitCode() == 0)
                            {
                                final DefaultApplicationLauncher defaultApplicationLauncher = process.getDefaultApplicationLauncher();
                                final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                                defaultApplicationLauncher.openFileWithDefaultApplication(indexHtmlFile).await();
                            }
                        }
                    }
                }
            }
        }
    }

    static void main(String[] args)
    {
        DesktopProcess.run(args, JavaProjectTest::runTests);
    }

    static void runTests(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineParameters parameters = process.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectTest.addProjectFolderParameter(parameters, process);
        final CommandLineParameter<PathPattern> patternParameter = JavaProjectTest.addPattern(parameters);
        final CommandLineParameter<Coverage> coverageParameter = JavaProjectTest.addCoverage(parameters);
        final CommandLineParameterBoolean testJsonParameter = JavaProjectTest.addTestJson(parameters);
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameter<File> logFileParameter = parameters.addFile(JavaProjectTest.logFileParameterName, process);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(process, JavaProjectTest.class);

        profilerParameter.await();

        final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());
        final PathPattern pattern = patternParameter.getValue().await();
        final File logFile = logFileParameter.getValue().await();
        final Coverage coverage = coverageParameter.getValue().await();
        final boolean testJson = testJsonParameter.getValue().await();
        final CharacterToByteWriteStream outputStream = process.getOutputWriteStream();
        final VerboseCharacterToByteWriteStream verboseStream = verboseParameter.getVerboseCharacterToByteWriteStream().await();

        final LogStreams logStreams;
        final CharacterToByteWriteStream output;
        final VerboseCharacterToByteWriteStream verbose;
        if (logFile == null)
        {
            logStreams = null;
            output = outputStream;
            verbose = verboseStream;
        }
        else
        {
            logStreams = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, outputStream, verboseStream);
            output = logStreams.getOutput();
            verbose = logStreams.getVerbose();
        }

        try
        {
            final VersionNumber currentJavaVersion = process.getJavaVersion();
            verbose.writeLine("Current Java version: " + currentJavaVersion.toString()).await();

            final boolean useTestJson = testJson && pattern == null;
            if (testJson && pattern != null)
            {
                verbose.writeLine("Not using existing test.json or writing a new test.json file because a test pattern was specified.").await();
            }

            final TestJSON previousTestJson = useTestJson
                ? projectFolder.getTestJson()
                    .onValue(() -> verbose.writeLine("Successfully parsed test.json file.").await())
                    .catchError(FileNotFoundException.class, () -> verbose.writeLine("No test.json file found.").await())
                    .catchError((Throwable e) -> verbose.writeLine("Invalid test.json file found: " + e.getMessage()).await())
                    .await()
                : null;
            final TestJSON newTestJson = useTestJson
                ? TestJSON.create()
                : null;
            final List<TestJSONClassFile> newTestJsonClassFiles = useTestJson
                ? List.create()
                : null;

            if (useTestJson)
            {
                newTestJson.setJavaVersion(currentJavaVersion);
            }

            final Iterable<JavaClassFile> testClassFiles = projectFolder.iterateTestClassFiles()
                // Ignore anonymous type class files
                .where((JavaClassFile testClassFile) -> !testClassFile.getName().contains("$"))
                .toList();
            final int testClassFilesCount = testClassFiles.getCount();
            verbose.writeLine("Found " + testClassFilesCount + " test class file" + (testClassFilesCount == 1 ? "" : "s") + " to test.").await();

            if (testClassFilesCount == 0)
            {
                output.writeLine("No test classes found.").await();
            }
            else
            {
                final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                final Folder outputsTestsFolder = projectFolder.getOutputsTestsFolder().await();
                final Iterable<JavaClassFile> testClassFilesToRun;

                final TestRunner runner = TestRunner.create(process, pattern);

                final IntegerValue unmodifiedPassedTestCount = IntegerValue.create(0);
                final IntegerValue unmodifiedTestFailureCount = IntegerValue.create(0);
                final IntegerValue unmodifiedSkippedTestCount = IntegerValue.create(0);

                if (!useTestJson ||
                    previousTestJson == null ||
                    !Comparer.equal(previousTestJson.getJavaVersion(), currentJavaVersion) ||
                    coverage != Coverage.None)
                {
                    verbose.writeLine("Running all tests...").await();
                    testClassFilesToRun = testClassFiles;
                }
                else
                {
                    verbose.writeLine("Discovering which test class files to run...").await();
                    final List<JavaClassFile> changedTestClassFiles = List.create();
                    final Iterable<TestJSONClassFile> testJsonClassFiles = previousTestJson.getClassFiles();
                    final Map<Path,TestJSONClassFile> relativePathToTestJsonClassFileMap = testJsonClassFiles
                        .toMap(TestJSONClassFile::getRelativePath,
                               (TestJSONClassFile testJsonClassFile) -> testJsonClassFile);
                    for (final JavaClassFile testClassFile : testClassFiles)
                    {
                        final Path testClassFileRelativePath = testClassFile.relativeTo(outputsFolder);
                        final TestJSONClassFile testJsonClassFile = relativePathToTestJsonClassFileMap.get(testClassFileRelativePath).catchError().await();
                        if (testJsonClassFile == null)
                        {
                            verbose.writeLine("Found new class file to run: " + testClassFileRelativePath.toString()).await();
                            changedTestClassFiles.add(testClassFile);
                        }
                        else if (testClassFile.getLastModified().await().equals(testJsonClassFile.getLastModified()))
                        {
                            final Integer failedTestCount = testJsonClassFile.getFailedTestCount();
                            if (failedTestCount != null && failedTestCount > 0)
                            {
                                verbose.writeLine("Found unmodified class file with errors: " + testClassFileRelativePath.toString() + " (Last modified: " + testClassFile.getLastModified().await().toString() + ")").await();
                                changedTestClassFiles.add(testClassFile);
                            }
                            else
                            {
                                verbose.writeLine("Found unmodified class file with no errors: " + testClassFileRelativePath.toString() + " (Last modified: " + testClassFile.getLastModified().await().toString() + ")").await();
                                newTestJsonClassFiles.add(testJsonClassFile);

                                JavaProjectTest.plusAssign(unmodifiedPassedTestCount, testJsonClassFile.getPassedTestCount());
                                JavaProjectTest.plusAssign(unmodifiedSkippedTestCount, testJsonClassFile.getSkippedTestCount());
                                JavaProjectTest.plusAssign(unmodifiedTestFailureCount, testJsonClassFile.getFailedTestCount());
                            }
                        }
                        else
                        {
                            verbose.writeLine("Found modified class file to run: " + testClassFileRelativePath.toString()).await();
                            changedTestClassFiles.add(testClassFile);
                        }
                    }
                    testClassFilesToRun = changedTestClassFiles;
                }

                final List<TestError> testFailures = List.create();
                final List<Test> skippedTests = List.create();
                final IntegerValue passedTestCount = IntegerValue.create(0);
                final IntegerValue finishedTestCount = IntegerValue.create(0);

                if (!testClassFilesToRun.any())
                {
                    output.writeLine("No tests need to be run.").await();
                }
                else
                {
                    final MutableMap<Path,JavaClassFile> relativePathToTestClassFilesToRunMap = testClassFilesToRun.toMap(
                        (JavaClassFile testClassFileToRun) -> testClassFileToRun.relativeTo(outputsTestsFolder),
                        (JavaClassFile testClassFileToRun) -> testClassFileToRun);

                    final BooleanValue wroteRunningTests = BooleanValue.create(false);
                    final IndentedCharacterWriteStream indentedOutput = IndentedCharacterWriteStream.create(output);
                    final List<TestParent> testParentsWrittenToConsole = List.create();
                    runner.beforeTest((Test test) ->
                    {
                        if (!wroteRunningTests.get())
                        {
                            indentedOutput.writeLine().await();
                            indentedOutput.writeLine("Running tests...").await();
                            wroteRunningTests.set(true);
                        }

                        final Stack<TestParent> testParentsToWrite = Stack.create();
                        TestParent currentTestParent = test.getParent();
                        while (currentTestParent != null && !testParentsWrittenToConsole.contains(currentTestParent))
                        {
                            testParentsToWrite.push(currentTestParent);
                            currentTestParent = currentTestParent.getParent();
                        }

                        while (testParentsToWrite.any())
                        {
                            final TestParent testParentToWrite = testParentsToWrite.pop().await();

                            final String skipMessage = testParentToWrite.getSkipMessage();
                            final String testGroupMessage = testParentToWrite.getName() + (!testParentToWrite.shouldSkip() ? "" : " - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
                            indentedOutput.writeLine(testGroupMessage).await();
                            testParentsWrittenToConsole.addAll(testParentToWrite);
                            indentedOutput.increaseIndent();
                        }

                        indentedOutput.write(test.getName()).await();
                        indentedOutput.increaseIndent();
                    });
                    runner.afterTestSuccess((Test test) ->
                    {
                        passedTestCount.increment();

                        indentedOutput.writeLine(" - Passed").await();
                    });
                    runner.afterTestFailure((Test test, TestError failure) ->
                    {
                        testFailures.add(failure);

                        indentedOutput.writeLine(" - Failed").await();
                        JavaProjectTest.writeFailure(indentedOutput, failure);
                    });
                    runner.afterTestSkipped((Test test) ->
                    {
                        skippedTests.add(test);

                        final String skipMessage = test.getSkipMessage();
                        indentedOutput.writeLine(" - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage)).await();
                    });
                    runner.afterTest((Test test) ->
                    {
                        finishedTestCount.increment();

                        indentedOutput.decreaseIndent();
                    });
                    runner.afterTestGroup((TestGroup testGroup) ->
                    {
                        if (testParentsWrittenToConsole.remove(testGroup))
                        {
                            indentedOutput.decreaseIndent();
                        }
                    });
                    runner.afterTestClass((TestClass testClass) ->
                    {
                        verbose.writeLine("Updating test.json class file for " + testClass.getFullName() + "...").await();
                        final Path testClassRelativePath = JavaClassFile.getRelativePathFromFullTypeName(testClass.getFullName());
                        final JavaClassFile testClassFile = relativePathToTestClassFilesToRunMap.get(testClassRelativePath).catchError().await();
                        newTestJsonClassFiles.add(TestJSONClassFile.create(testClassFile.relativeTo(outputsFolder))
                            .setLastModified(testClassFile.getLastModified().await())
                            .setPassedTestCount(testClass.getPassedTestCount())
                            .setSkippedTestCount(testClass.getSkippedTestCount())
                            .setFailedTestCount(testClass.getFailedTestCount()));

                        if (testParentsWrittenToConsole.remove(testClass))
                        {
                            indentedOutput.decreaseIndent();
                        }
                    });

                    for (final JavaClassFile testClassFile : testClassFilesToRun)
                    {
                        final String fullTypeName = JavaFile.getFullTypeName(outputsTestsFolder, testClassFile);
                        runner.testClass(fullTypeName)
                            .catchError((Throwable e) ->
                            {
                                verbose.writeLine(e.getMessage()).await();
                            })
                            .await();
                    }

                    if (skippedTests.any())
                    {
                        indentedOutput.writeLine().await();
                        indentedOutput.writeLine("Skipped Tests:").await();
                        indentedOutput.increaseIndent();
                        int testSkippedNumber = 1;
                        for (final Test skippedTest : skippedTests)
                        {
                            final String skipMessage = skippedTest.getSkipMessage();
                            indentedOutput.writeLine(testSkippedNumber + ") " + skippedTest.getFullName() + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage)).await();
                            ++testSkippedNumber;
                        }
                        indentedOutput.decreaseIndent();
                    }

                    if (testFailures.any())
                    {
                        indentedOutput.writeLine().await();
                        indentedOutput.writeLine("Test failures:").await();
                        indentedOutput.increaseIndent();

                        int testFailureNumber = 1;
                        for (final TestError failure : testFailures)
                        {
                            if (testFailureNumber > 1)
                            {
                                indentedOutput.writeLine().await();
                            }

                            indentedOutput.writeLine(testFailureNumber + ") " + failure.getTestScope()).await();
                            ++testFailureNumber;
                            indentedOutput.increaseIndent();
                            JavaProjectTest.writeFailure(indentedOutput, failure);
                            indentedOutput.decreaseIndent();
                        }

                        indentedOutput.decreaseIndent();
                    }
                }

                final CharacterTable table = CharacterTable.create();
                if (unmodifiedPassedTestCount.greaterThan(0) || unmodifiedSkippedTestCount.greaterThan(0))
                {
                    table.addRow("Unmodified Tests:", Integers.toString(unmodifiedPassedTestCount.get() + unmodifiedSkippedTestCount.get()));
                    if (unmodifiedPassedTestCount.greaterThan(0))
                    {
                        table.addRow("Unmodified Passed Tests:", unmodifiedPassedTestCount.toString());
                    }
                    if (unmodifiedSkippedTestCount.greaterThan(0))
                    {
                        table.addRow("Unmodified Skipped Tests:", unmodifiedSkippedTestCount.toString());
                    }
                }

                if (finishedTestCount.greaterThan(0))
                {
                    table.addRow("Tests Run:", finishedTestCount.toString());
                    if (passedTestCount.greaterThan(0))
                    {
                        table.addRow("Tests Passed:", passedTestCount.toString());
                    }
                    if (testFailures.any())
                    {
                        table.addRow("Tests Failed:", Integers.toString(testFailures.getCount()));
                    }
                    if (skippedTests.any())
                    {
                        table.addRow("Tests Skipped:", Integers.toString(skippedTests.getCount()));
                    }
                }

                if (!table.getRows().any())
                {
                    output.writeLine("No tests found.").await();
                }
                else
                {
                    output.writeLine().await();
                    table.toString(output, CharacterTableFormat.consise).await();
                    output.writeLine().await();
                }

                process.setExitCode(testFailures.getCount());
            }

            if (useTestJson)
            {
                newTestJson.setClassFiles(newTestJsonClassFiles);

                verbose.writeLine("Updating test.json file...").await();
                projectFolder.writeTestJson(newTestJson).await();
            }
        }
        finally
        {
            if (logStreams != null)
            {
                logStreams.dispose().await();
            }
        }
    }

    static void writeFailure(IndentedCharacterWriteStream writeStream, TestError failure)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(failure, "failure");

        writeStream.increaseIndent();
        JavaProjectTest.writeMessageLines(writeStream, failure);
        JavaProjectTest.writeStackTrace(writeStream, failure);
        writeStream.decreaseIndent();

        final Throwable cause = failure.getCause();
        if (cause != null)
        {
            JavaProjectTest.writeFailureCause(writeStream, cause);
        }
    }

    static void writeMessageLines(IndentedCharacterWriteStream writeStream, TestError failure)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(failure, "failure");

        for (final String messageLine : failure.getMessageLines())
        {
            if (messageLine != null)
            {
                writeStream.writeLine(messageLine).await();
            }
        }
    }

    static void writeMessage(IndentedCharacterWriteStream writeStream, Throwable throwable)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(throwable, "throwable");

        if (throwable instanceof TestError)
        {
            JavaProjectTest.writeMessageLines(writeStream, (TestError)throwable);
        }
        else if (!Strings.isNullOrEmpty(throwable.getMessage()))
        {
            writeStream.writeLine("Message: " + throwable.getMessage()).await();
        }
    }

    static void writeFailureCause(IndentedCharacterWriteStream writeStream, Throwable cause)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(cause, "cause");

        if (cause instanceof ErrorIterable)
        {
            final ErrorIterable errors = (ErrorIterable)cause;

            writeStream.writeLine("Caused by:").await();
            int causeNumber = 0;
            for (final Throwable innerCause : errors)
            {
                ++causeNumber;
                writeStream.write(causeNumber + ") " + innerCause.getClass().getName()).await();

                writeStream.increaseIndent();
                JavaProjectTest.writeMessage(writeStream, innerCause);
                JavaProjectTest.writeStackTrace(writeStream, innerCause);
                writeStream.decreaseIndent();

                final Throwable nextCause = innerCause.getCause();
                if (nextCause != null && nextCause != innerCause)
                {
                    writeStream.increaseIndent();
                    JavaProjectTest.writeFailureCause(writeStream, nextCause);
                    writeStream.decreaseIndent();
                }
            }
        }
        else
        {
            writeStream.writeLine("Caused by: " + cause.getClass().getName()).await();

            writeStream.increaseIndent();
            JavaProjectTest.writeMessage(writeStream, cause);
            JavaProjectTest.writeStackTrace(writeStream, cause);
            writeStream.decreaseIndent();

            final Throwable nextCause = cause.getCause();
            if (nextCause != null && nextCause != cause)
            {
                writeStream.increaseIndent();
                JavaProjectTest.writeFailureCause(writeStream, nextCause);
                writeStream.decreaseIndent();
            }
        }
    }

    /**
     * Write the stack trace of the provided Throwable to the output stream.
     * @param t The Throwable to writeByte the stack trace of.
     */
    static void writeStackTrace(IndentedCharacterWriteStream writeStream, Throwable t)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(t, "t");

        final StackTraceElement[] stackTraceElements = t.getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 0)
        {
            writeStream.writeLine("Stack Trace:").await();
            writeStream.increaseIndent();
            for (StackTraceElement stackTraceElement : stackTraceElements)
            {
                writeStream.writeLine("at " + stackTraceElement.toString()).await();
            }
            writeStream.decreaseIndent();
        }
    }

    static void plusAssign(IntegerValue lhs, Integer rhs)
    {
        PreCondition.assertNotNull(lhs, "lhs");

        if (rhs != null)
        {
            lhs.plusAssign(rhs);
        }
    }
}
