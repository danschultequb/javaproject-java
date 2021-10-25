package qub;

public interface JavaProjectTest
{
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

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectTest.addProjectFolderParameter(parameters, process);
        final CommandLineParameter<String> patternParameter = parameters.addString("pattern")
            .setValueName("<test-name-pattern>")
            .setDescription("The pattern to match against tests to determine if they will be run.");
        final CommandLineParameter<Coverage> coverageParameter = parameters.addEnum("coverage", Coverage.None, Coverage.Sources)
            .setValueRequired(false)
            .setValueName("<None|Sources|Tests|All>")
            .addAlias("c")
            .setDescription("Whether code coverage information will be collected while running tests.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final Folder dataFolder = process.getQubProjectDataFolder().await();
            final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDataFolder(dataFolder, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                JavaProjectBuild.run(process, projectFolder, logStreams);
                if (process.getExitCode() == 0)
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final CharacterWriteStream output = logStreams.getOutput();

                    final JavaProjectJSON projectJson = projectFolder.getProjectJson().await();
                    final Iterable<JavaPublishedProjectFolder> dependencyFolders = projectJson.getAllDependencyFolders(qubFolder).await();

                    final Coverage coverage = coverageParameter.getValue().await();
                    final QubProjectVersionFolder jacocoFolder = (coverage == Coverage.None)
                        ? null
                        : qubFolder.getLatestProjectVersionFolder("jacoco", "jacococli").await();

                    output.writeLine("Running tests...").await();

                    final String pattern = patternParameter.getValue().await();
                }
            }
        }
    }

//    static void main(String[] args)
//    {
//        DesktopProcess.run(args, JavaProjectTest::runTests);
//    }
//
//    static void runTests(DesktopProcess process)
//    {
//        PreCondition.assertNotNull(process, "process");
//
//        final CommandLineParameters parameters = process.createCommandLineParameters();
//        final CommandLineParameter<Folder> projectFolderParameter = JavaProjectTest.addProjectFolderParameter(parameters, process);
//        final CommandLineParameter<PathPattern> patternParameter = parameters.add("pattern", (String argumentValue) ->
//        {
//            return Result.success(Strings.isNullOrEmpty(argumentValue)
//                ? null
//                : PathPattern.parse(argumentValue));
//        });
//        final CommandLineParameter<Coverage> coverageParameter = parameters.addEnum("coverage", Coverage.None, Coverage.Sources);
//        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
//        final CommandLineParameter<File> logFileParameter = parameters.addFile("logFile", process);
//
//        final JavaProjectFolder projectFolder = JavaProjectFolder.get(projectFolderParameter.getValue().await());
//        final PathPattern pattern = patternParameter.getValue().await();
//        final File logFile = logFileParameter.getValue().await();
//        final Coverage coverage = coverageParameter.getValue().await();
//        final CharacterToByteWriteStream outputStream = process.getOutputWriteStream();
//        final VerboseCharacterToByteWriteStream verboseStream = verboseParameter.getVerboseCharacterToByteWriteStream().await();
//
//        final TestRunner runner = TestRunner.create(process, pattern);
//
//        final LogStreams logStreams;
//        final CharacterToByteWriteStream output;
//        final VerboseCharacterToByteWriteStream verbose;
//        if (logFile == null)
//        {
//            logStreams = null;
//            output = outputStream;
//            verbose = verboseStream;
//        }
//        else
//        {
//            logStreams = CommandLineLogsAction.getLogStreamsFromLogFile(logFile, outputStream, verboseStream);
//            output = logStreams.getOutput();
//            verbose = logStreams.getVerbose();
//        }
//
//        int result;
//        try
//        {
//            final TestJSON previousTestJson = projectFolder.getTestJson()
//                .catchError()
//                .await();
//
//            final VersionNumber currentJavaVersion = process.getJavaVersion();
//            final List<TestJSONClassFile> testJSONClassFiles = List.create();
//
//            VersionNumber previousJavaVersion = null;
//
//            if (previousTestJson != null)
//            {
//                verbose.writeLine("Found and parsed test.json file.").await();
//                previousJavaVersion = previousTestJson.getJavaVersion();
//                for (final TestJSONClassFile testJSONClassFile : previousTestJson.getClassFiles())
//                {
//                    fullTypeNameToTestJSONClassFileMap.set(testJSONClassFile.getFullTypeName(), testJSONClassFile);
//                }
//            }
//
//            runner.afterTestClass((TestClass testClass) ->
//            {
//                verbose.writeLine("Updating test.json class file for " + testClass.getFullName() + "...").await();
//                final File testClassFile = QubTestRun.getClassFile(outputsFolder, testClass.getFullName());
//                testJSONClassFiles.addAll(TestJSONClassFile.create(testClassFile.relativeTo(outputsFolder))
//                    .setLastModified(testClassFile.getLastModified().await())
//                    .setPassedTestCount(testClass.getPassedTestCount())
//                    .setSkippedTestCount(testClass.getSkippedTestCount())
//                    .setFailedTestCount(testClass.getFailedTestCount()));
//            });
//
//            for (final String testClassName : fullTypeNames)
//            {
//                boolean runTestClass;
//
//                if (!currentJavaVersion.equals(previousJavaVersion) || coverage != Coverage.None)
//                {
//                    runTestClass = true;
//                }
//                else
//                {
//                    final TestJSONClassFile testJSONClassFile = fullTypeNameToTestJSONClassFileMap.get(testClassName)
//                        .catchError(NotFoundException.class)
//                        .await();
//                    if (testJSONClassFile == null)
//                    {
//                        verbose.writeLine("Found class that didn't exist in previous test run: " + testClassName);
//                        runTestClass = true;
//                    }
//                    else
//                    {
//                        verbose.writeLine("Found class entry for " + testClassName + ". Checking timestamps...").await();
//                        final File testClassFile = outputsFolder.getFile(testJSONClassFile.getRelativePath()).await();
//                        final DateTime testClassFileLastModified = testClassFile.getLastModified().await();
//                        if (!testClassFileLastModified.equals(testJSONClassFile.getLastModified()))
//                        {
//                            verbose.writeLine("Timestamp of " + testClassName + " from the previous run (" + testJSONClassFile.getLastModified() + ") was not the same as the current class file timestamp (" + testClassFileLastModified + "). Running test class tests.").await();
//                            runTestClass = true;
//                        }
//                        else if (testJSONClassFile.getFailedTestCount() > 0)
//                        {
//                            verbose.writeLine("Previous run of " + testClassName + " contained errors. Running test class tests...").await();
//                            runTestClass = true;
//                        }
//                        else
//                        {
//                            verbose.writeLine("Previous run of " + testClassName + " didn't contain errors and the test class hasn't changed since then. Skipping test class tests.").await();
//                            runner.addUnmodifiedPassedTests(testJSONClassFile.getPassedTestCount());
//                            runner.addUnmodifiedSkippedTests(testJSONClassFile.getSkippedTestCount());
//                            testJSONClassFiles.addAll(testJSONClassFile);
//                            runTestClass = false;
//                        }
//                    }
//                }
//
//                if (runTestClass)
//                {
//                    runner.testClass(testClassName)
//                        .catchError((Throwable e) -> verbose.writeLine(e.getMessage()).await())
//                        .await();
//                }
//            }
//
//            if (useTestJson && pattern == null)
//            {
//                final File testJsonFile = outputsFolder.getFile("test.json").await();
//                final TestJSON testJson = TestJSON.create()
//                    .setJavaVersion(currentJavaVersion)
//                    .setClassFiles(testJSONClassFiles);
//                testJsonFile.setContentsAsString(testJson.toString(JSONFormat.pretty)).await();
//            }
//
//            runner.writeLine().await();
//            runner.writeSummary(stopwatch);
//
//            result = runner.getFailedTestCount();
//        }
//        finally
//        {
//            if (logStreams != null)
//            {
//                logStreams.getLogStream().dispose().await();
//            }
//        }
//
//        return result;
//    }
}
