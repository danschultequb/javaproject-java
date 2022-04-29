package qub;

public interface JavaProjectTestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectTest.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectTest.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);

                    final CommandLineAction addActionResult = JavaProjectTest.addAction(actions);
                    test.assertNotNull(addActionResult);

                    final CommandLineAction action = actions.getAction("test").await();
                    test.assertNotNull(action);
                    test.assertSame(addActionResult, action);
                    test.assertEqual("test", action.getName());
                    test.assertEqual("qub-javaproject test", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Run the tests of a Java source code project.", action.getDescription());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    test.assertThrows(() -> JavaProjectTest.run(null, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectTest.run(process, null),
                        new PreConditionFailure("action cannot be null."));
                });

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject test [[--projectFolder=]<projectFolder-value>] [--pattern=<test-name-pattern>] [--coverage[=<None|Sources|Tests|All>]] [--testjson] [--openCoverageReport] [--help] [--verbose] [--profiler]",
                            "  Run the tests of a Java source code project.",
                            "  --projectFolder:      The folder that contains a Java project to test. Defaults to the current folder.",
                            "  --pattern:            The pattern to match against tests to determine if they will be run.",
                            "  --coverage(c):        Whether code coverage information will be collected while running tests.",
                            "  --testjson:           Whether to use a test.json file to cache test results in.",
                            "  --openCoverageReport: Whether to automatically open the HTML coverage report after the tests complete.",
                            "  --help(?):            Show the help message for this application.",
                            "  --verbose(v):         Whether or not to show verbose logs.",
                            "  --profiler:           Whether or not this application should pause before it is run to allow a profiler to be attached."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder();
                    test.assertEqual(
                        Iterable.create(),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with non-existing project folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No project.json file exists in the project folder at \"/project/folder/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    test.assertFalse(projectFolder.exists().await());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    projectFolder.create().await();

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No project.json file exists in the project folder at \"/project/folder/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with empty project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.create().await();

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Invalid project.json file: Missing object left curly bracket ('{')."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with empty array project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString("[]").await();

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Invalid project.json file: Expected object left curly bracket ('{')."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no openjdk/jdk project installed",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final ProjectJSON projectJson = ProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No openjdk/jdk project is installed in the qub folder at \"/qub/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no sources folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final ProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files found in " + projectFolder + "."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with empty sources folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTestTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files found in " + projectFolder + "."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file and no test files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "No test classes found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create())
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 0 test class files to test.",
                            "No test classes found.",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no source files and one test file that can't be loaded",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code").await();
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 test source file...",
                            "No tests found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aTestsClassFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create())
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "VERBOSE: Could not load a class named \"ATests\".",
                            "No tests found.",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no source files and one test file that doesn't have a test method",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code").await();
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", Object.class);

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 test source file...",
                            "No tests found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aTestsClassFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create("tests/ATests.class")
                                    .setLastModified(DateTime.create(1970, 1, 1))
                                    .setPassedTestCount(0)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "VERBOSE: No static method with the signature java.lang.Object.test(qub.TestRunner) -> ? could be found.",
                            "No tests found.",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no source files and one test file that has a test method with no tests",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code").await();
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 test source file...",
                            "No tests found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aTestsClassFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create("tests/ATests.class")
                                    .setLastModified(DateTime.create(1970, 1, 1))
                                    .setPassedTestCount(0)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "No tests found.",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no source files and one test file that has tests (including test failures)",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code").await();
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();

                    JavaProjectConfiguration.setConfigurationFile(process, JavaProjectConfiguration.create()
                        .setIgnoredStackTraceTypes(Iterable.create(
                            "java.lang.reflect.Method",
                            "java.lang.Thread",
                            "jdk.internal.reflect.NativeMethodAccessorImpl",
                            "jdk.internal.reflect.DelegatingMethodAccessorImpl",
                            "qub.AsyncTask",
                            "qub.BasicTestRunner",
                            "qub.JavaProjectTest",
                            "qub.LazyResult",
                            "qub.Result",
                            "qub.StaticMethod1",
                            "qub.Test",
                            "qub.TestRunner"
                        )))
                        .await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.test("Test outside of test group", (Test test) ->
                                {
                                });

                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });

                                    runner.test("Empty test", (Test test) ->
                                    {
                                    });

                                    runner.testGroup("with no tests", () ->
                                    {
                                        // No output should appear for this test group.
                                    });

                                    runner.testGroup("B", () ->
                                    {
                                        runner.test("Failing test", (Test test) ->
                                        {
                                            test.fail("Intentional failure");
                                        });

                                        runner.test("Passing test after failing test", (Test test) ->
                                        {
                                            test.assertGreaterThan(10, 5);
                                        });

                                        runner.test("Skipped test with no message", runner.skip(), (Test test) ->
                                        {
                                        });

                                        runner.test("Skipped test with message", runner.skip("Why the test should be skipped"), (Test test) ->
                                        {
                                        });
                                    });

                                    runner.testGroup("Group with unexpected error", () ->
                                    {
                                        throw new NullPointerException("Unexpected test group error!");
                                    });

                                    runner.testGroup("Skipped test group with no message", runner.skip(), () ->
                                    {
                                    });
                                    
                                    runner.testGroup("Skipped test group with message", runner.skip("Why the test group should be skipped"), () ->
                                    {
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "Test outside of test group - Passed",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "    Empty test - Passed",
                            "    B",
                            "      Failing test - Failed",
                            "          Intentional failure",
                            "          Stack Trace:",
                            "            at qub.JavaProjectTestTests$2.lambda$test$4(JavaProjectTestTests.java:1092)",
                            "            at qub.JavaProjectTestTests$2.lambda$test$8(JavaProjectTestTests.java:1090)",
                            "            at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1088)",
                            "            at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "            at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "            at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "      Passing test after failing test - Passed",
                            "      Skipped test with no message - Skipped",
                            "      Skipped test with message - Skipped: Why the test should be skipped",
                            "    Group with unexpected error - Failed",
                            "      An unexpected error occurred during \"ATests A Group with unexpected error\".",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Caused by: java.lang.NullPointerException",
                            "      Message: Unexpected test group error!",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$9(JavaProjectTestTests.java:1111)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Skipped test group with no message - Skipped",
                            "    Skipped test group with message - Skipped: Why the test group should be skipped",
                            "",
                            "Skipped Tests:",
                            "  1) ATests A B Skipped test with no message",
                            "  2) ATests A B Skipped test with message: Why the test should be skipped",
                            "",
                            "Test failures:",
                            "  1) ATests A B Failing test",
                            "      Intentional failure",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$4(JavaProjectTestTests.java:1092)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$8(JavaProjectTestTests.java:1090)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1088)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "",
                            "  2) ATests A Group with unexpected error",
                            "      An unexpected error occurred during \"ATests A Group with unexpected error\".",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Caused by: java.lang.NullPointerException",
                            "      Message: Unexpected test group error!",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$9(JavaProjectTestTests.java:1111)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "",
                            "Tests Run:     7",
                            "Tests Passed:  4",
                            "Tests Failed:  2",
                            "Tests Skipped: 2"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(2, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aTestsClassFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create("tests/ATests.class")
                                    .setLastModified(DateTime.create(1970, 1, 1))
                                    .setPassedTestCount(4)
                                    .setSkippedTestCount(2)
                                    .setFailedTestCount(2)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "Test outside of test group - Passed",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "    Empty test - Passed",
                            "    B",
                            "      Failing test - Failed",
                            "          Intentional failure",
                            "          Stack Trace:",
                            "            at qub.JavaProjectTestTests$2.lambda$test$4(JavaProjectTestTests.java:1092)",
                            "            at qub.JavaProjectTestTests$2.lambda$test$8(JavaProjectTestTests.java:1090)",
                            "            at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1088)",
                            "            at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "            at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "            at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "      Passing test after failing test - Passed",
                            "      Skipped test with no message - Skipped",
                            "      Skipped test with message - Skipped: Why the test should be skipped",
                            "    Group with unexpected error - Failed",
                            "      An unexpected error occurred during \"ATests A Group with unexpected error\".",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Caused by: java.lang.NullPointerException",
                            "      Message: Unexpected test group error!",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$9(JavaProjectTestTests.java:1111)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Skipped test group with no message - Skipped",
                            "    Skipped test group with message - Skipped: Why the test group should be skipped",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Skipped Tests:",
                            "  1) ATests A B Skipped test with no message",
                            "  2) ATests A B Skipped test with message: Why the test should be skipped",
                            "",
                            "Test failures:",
                            "  1) ATests A B Failing test",
                            "      Intentional failure",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$4(JavaProjectTestTests.java:1092)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$8(JavaProjectTestTests.java:1090)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1088)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "",
                            "  2) ATests A Group with unexpected error",
                            "      An unexpected error occurred during \"ATests A Group with unexpected error\".",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "    Caused by: java.lang.NullPointerException",
                            "      Message: Unexpected test group error!",
                            "      Stack Trace:",
                            "        at qub.JavaProjectTestTests$2.lambda$test$9(JavaProjectTestTests.java:1111)",
                            "        at qub.JavaProjectTestTests$2.lambda$test$12(JavaProjectTestTests.java:1109)",
                            "        at qub.JavaProjectTestTests$2.test(JavaProjectTestTests.java:1072)",
                            "        at qub.FakeChildProcessRunner.lambda$start$4(FakeChildProcessRunner.java:142)",
                            "        at qub.ParallelAsyncRunner.lambda$schedule$0(ParallelAsyncRunner.java:58)",
                            "",
                            "Tests Run:     7",
                            "Tests Passed:  4",
                            "Tests Failed:  2",
                            "Tests Skipped: 2",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectDataFolder.getFile("configuration.json").await(),
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage with no jacoco/jacococli published project",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner
                        .add(FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }))
                        .add(FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=Sources", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "No project named jacoco/jacococli has been published.",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "No project named jacoco/jacococli has been published.",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=Sources --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final QubProjectVersionFolder jacocoFolder = JavaProjectTestTests.getJacocoFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                    final File coverageXmlFile = outputsFolder.getFile("coverage.xml").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec", "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=Sources", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-jar", "/qub/jacoco/jacococli/versions/8/jacococli.jar", "report", "/project/folder/outputs/coverage.exec", "--classfiles", "/project/folder/outputs/sources/", "--sourcefiles", "/project/folder/sources/", "--html", "/project/folder/outputs/coverage/", "--xml", "/project/folder/outputs/coverage.xml")
                            .setAction(() ->
                            {
                                indexHtmlFile.create().await();
                                coverageXmlFile.create().await();
                            }));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "",
                            "Analyzing coverage..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            coverageFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            coverageXmlFile,
                            testJsonFile,
                            indexHtmlFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=Sources --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file...",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -jar /qub/jacoco/jacococli/versions/8/jacococli.jar report /project/folder/outputs/coverage.exec --classfiles /project/folder/outputs/sources/ --sourcefiles /project/folder/sources/ --html /project/folder/outputs/coverage/ --xml /project/folder/outputs/coverage.xml"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jacocoFolder.getPublisherFolder().await(),
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jacocoFolder.getProjectFolder().await(),
                            jacocoFolder.getProjectVersionsFolder().await(),
                            jacocoFolder,
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage=None",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage=None")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage=Sources",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage=Sources")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final QubProjectVersionFolder jacocoFolder = JavaProjectTestTests.getJacocoFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                    final File coverageXmlFile = outputsFolder.getFile("coverage.xml").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec", "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=Sources", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-jar", "/qub/jacoco/jacococli/versions/8/jacococli.jar", "report", "/project/folder/outputs/coverage.exec", "--classfiles", "/project/folder/outputs/sources/", "--sourcefiles", "/project/folder/sources/", "--html", "/project/folder/outputs/coverage/", "--xml", "/project/folder/outputs/coverage.xml")
                            .setAction(() ->
                            {
                                indexHtmlFile.create().await();
                                coverageXmlFile.create().await();
                            }));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "",
                            "Analyzing coverage..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            coverageFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            coverageXmlFile,
                            testJsonFile,
                            indexHtmlFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=Sources --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file...",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -jar /qub/jacoco/jacococli/versions/8/jacococli.jar report /project/folder/outputs/coverage.exec --classfiles /project/folder/outputs/sources/ --sourcefiles /project/folder/sources/ --html /project/folder/outputs/coverage/ --xml /project/folder/outputs/coverage.xml"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jacocoFolder.getPublisherFolder().await(),
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jacocoFolder.getProjectFolder().await(),
                            jacocoFolder.getProjectVersionsFolder().await(),
                            jacocoFolder,
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage=Tests",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage=Tests")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final QubProjectVersionFolder jacocoFolder = JavaProjectTestTests.getJacocoFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                    final File coverageXmlFile = outputsFolder.getFile("coverage.xml").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec", "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=Tests", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-jar", "/qub/jacoco/jacococli/versions/8/jacococli.jar", "report", "/project/folder/outputs/coverage.exec", "--classfiles", "/project/folder/outputs/tests/", "--sourcefiles", "/project/folder/tests/", "--html", "/project/folder/outputs/coverage/", "--xml", "/project/folder/outputs/coverage.xml")
                            .setAction(() ->
                            {
                                indexHtmlFile.create().await();
                                coverageXmlFile.create().await();
                            }));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "",
                            "Analyzing coverage..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            coverageFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            coverageXmlFile,
                            testJsonFile,
                            indexHtmlFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=Tests --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file...",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -jar /qub/jacoco/jacococli/versions/8/jacococli.jar report /project/folder/outputs/coverage.exec --classfiles /project/folder/outputs/tests/ --sourcefiles /project/folder/tests/ --html /project/folder/outputs/coverage/ --xml /project/folder/outputs/coverage.xml"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jacocoFolder.getPublisherFolder().await(),
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jacocoFolder.getProjectFolder().await(),
                            jacocoFolder.getProjectVersionsFolder().await(),
                            jacocoFolder,
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage=All",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage=All")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final QubProjectVersionFolder jacocoFolder = JavaProjectTestTests.getJacocoFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                    final File coverageXmlFile = outputsFolder.getFile("coverage.xml").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec", "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=All", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-jar", "/qub/jacoco/jacococli/versions/8/jacococli.jar", "report", "/project/folder/outputs/coverage.exec", "--classfiles", "/project/folder/outputs/sources/", "--sourcefiles", "/project/folder/sources/", "--classfiles", "/project/folder/outputs/tests/", "--sourcefiles", "/project/folder/tests/", "--html", "/project/folder/outputs/coverage/", "--xml", "/project/folder/outputs/coverage.xml")
                            .setAction(() ->
                            {
                                indexHtmlFile.create().await();
                                coverageXmlFile.create().await();
                            }));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "",
                            "Analyzing coverage..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            coverageFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            coverageXmlFile,
                            testJsonFile,
                            indexHtmlFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=All --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file...",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -jar /qub/jacoco/jacococli/versions/8/jacococli.jar report /project/folder/outputs/coverage.exec --classfiles /project/folder/outputs/sources/ --sourcefiles /project/folder/sources/ --classfiles /project/folder/outputs/tests/ --sourcefiles /project/folder/tests/ --html /project/folder/outputs/coverage/ --xml /project/folder/outputs/coverage.xml"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jacocoFolder.getPublisherFolder().await(),
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jacocoFolder.getProjectFolder().await(),
                            jacocoFolder.getProjectVersionsFolder().await(),
                            jacocoFolder,
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --coverage --openCoverageReport",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--coverage", "--openCoverageReport")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final QubProjectVersionFolder jacocoFolder = JavaProjectTestTests.getJacocoFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final File indexHtmlFile = coverageFolder.getFile("index.html").await();
                    final File coverageXmlFile = outputsFolder.getFile("coverage.xml").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec", "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=Sources", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-jar", "/qub/jacoco/jacococli/versions/8/jacococli.jar", "report", "/project/folder/outputs/coverage.exec", "--classfiles", "/project/folder/outputs/sources/", "--sourcefiles", "/project/folder/sources/", "--html", "/project/folder/outputs/coverage/", "--xml", "/project/folder/outputs/coverage.xml")
                            .setAction(() ->
                            {
                                indexHtmlFile.create().await();
                                coverageXmlFile.create().await();
                            }));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "",
                            "Analyzing coverage..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            coverageFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            coverageXmlFile,
                            testJsonFile,
                            indexHtmlFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    test.assertEqual(
                        Iterable.create(
                            indexHtmlFile.getPath()),
                        process.getDefaultApplicationLauncher().getPathsOpened());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -javaagent:/qub/jacoco/jacococli/versions/8/jacocoagent.jar=destfile=/project/folder/outputs/coverage.exec -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=Sources --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file...",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -jar /qub/jacoco/jacococli/versions/8/jacococli.jar report /project/folder/outputs/coverage.exec --classfiles /project/folder/outputs/sources/ --sourcefiles /project/folder/sources/ --html /project/folder/outputs/coverage/ --xml /project/folder/outputs/coverage.xml"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jacocoFolder.getPublisherFolder().await(),
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jacocoFolder.getProjectFolder().await(),
                            jacocoFolder.getProjectVersionsFolder().await(),
                            jacocoFolder,
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --testjson",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--testjson")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --testjson=true",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--testjson=true")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: No test.json file found.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "VERBOSE: Updating test.json class file for ATests...",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and --testjson=false",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--testjson=false")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=false", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides,-varargs,-serial,-overloads tests/ATests.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=false --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Running all tests...",
                            "",
                            "Running tests...",
                            "ATests",
                            "  A",
                            "    Passing test - Passed",
                            "",
                            "Tests Run:    1",
                            "Tests Passed: 1"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one source file, one test file that has tests, and test.json file with all tests passed",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/", "--testjson=true")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();

                    final Clock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code, depends on A").await();
                    
                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code").await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java byte code").await();

                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(projectJson)
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty))
                        .await();

                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    testJsonFile.setContentsAsString(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty))
                        .await();
                    
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "tests/ATests.java")
                            .setAction(() ->
                            {
                                aTestsClassFile.setContentsAsString("ATests.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/;/project/folder/outputs/tests/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                            .setAction(JavaProjectTest::runTests));

                    process.getTypeLoader()
                        .addType("ATests", new Object()
                        {
                            @SuppressWarnings("unused")
                            public static void test(TestRunner runner)
                            {
                                runner.testGroup("A", () ->
                                {
                                    runner.test("Passing test", (Test test) ->
                                    {
                                        test.assertNotNull(test);
                                    });
                                });
                            }
                        }.getClass());

                    JavaProjectTest.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled.",
                            "No tests need to be run.",
                            "",
                            "Unmodified Tests:        1",
                            "Unmodified Passed Tests: 1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            testJsonFile,
                            aClassFile,
                            aTestsClassFile,
                            aJavaFile,
                            aTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create())
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))),
                                BuildJSONJavaFile.create(aTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setDependencies(Iterable.create(
                                        aJavaFile.relativeTo(projectFolder)))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aTestsClassFile.relativeTo(projectFolder), startTime)))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create(
                                TestJSONClassFile.create(aTestsClassFile.relativeTo(outputsFolder))
                                    .setLastModified(startTime)
                                    .setPassedTestCount(1)
                                    .setSkippedTestCount(0)
                                    .setFailedTestCount(0)))
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeLogFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing /project/folder/project.json...",
                            "VERBOSE: Discovering dependencies...",
                            "VERBOSE: Parsing outputs/build.json...",
                            "VERBOSE: Checking if dependencies have changed since the previous build...",
                            "VERBOSE:   Previous dependencies have not changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has not changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "VERBOSE: sources/A.java - All class files are up to date.",
                            "VERBOSE: tests/ATests.java - All class files are up to date.",
                            "VERBOSE: Discovering unmodified .java file issues...",
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/java -classpath /project/folder/outputs/sources/;/project/folder/outputs/tests/ qub.JavaProjectTest --verbose=false --testjson=true --logfile=/qub/fake-publisher/fake-project/data/logs/1.log --projectFolder=/project/folder/ --coverage=None --profiler=false",
                            "VERBOSE: Current Java version: fake-java-version",
                            "VERBOSE: Successfully parsed test.json file.",
                            "VERBOSE: Found 1 test class file to test.",
                            "VERBOSE: Discovering which test class files to run...",
                            "VERBOSE: Found unmodified class file with no errors: tests/ATests.class (Last modified: 1970-01-01T00:00Z)",
                            "No tests need to be run.",
                            "",
                            "Unmodified Tests:        1",
                            "Unmodified Passed Tests: 1",
                            "VERBOSE: Updating test.json file..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });
            });
        });
    }

    static CommandLineAction createAction(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = JavaProject.createCommandLineActions(process);
        return JavaProjectTest.addAction(actions);
    }

    public static QubProjectVersionFolder getJacocoFolder(QubFolder qubFolder)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        final QubProjectVersionFolder result = qubFolder.getProjectVersionFolder("jacoco", "jacococli", "8").await();
        result.create().catchError().await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
