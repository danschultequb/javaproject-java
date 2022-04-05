package qub;

public interface JavaProjectPackTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectPack.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectPack.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);

                    JavaProjectPack.addAction(actions);

                    final CommandLineAction action = actions.getAction("pack").await();
                    test.assertNotNull(action);
                    test.assertEqual("pack", action.getName());
                    test.assertEqual("qub-javaproject pack", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Package a Java source code project.", action.getDescription());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    test.assertThrows(() -> JavaProjectPack.run(null, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectPack.run(process, null),
                        new PreConditionFailure("action cannot be null."));
                });

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    JavaProjectPack.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject pack [[--projectFolder=]<projectFolder-value>] [--help] [--verbose] [--profiler]",
                            "  Package a Java source code project.",
                            "  --projectFolder: The folder that contains a Java project to package. Defaults to the current folder.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs.",
                            "  --profiler:      Whether or not this application should pause before it is run to allow a profiler to be attached."),
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
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    JavaProjectPack.run(process, action);

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
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    projectFolder.create().await();

                    JavaProjectPack.run(process, action);

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
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.create().await();

                    JavaProjectPack.run(process, action);

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
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString("[]").await();

                    JavaProjectPack.run(process, action);

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
                    final CommandLineAction action = JavaProjectPackTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final ProjectJSON projectJson = ProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    JavaProjectPack.run(process, action);

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

                runner.test("with no source or test files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    
                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectPack.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files found in /project/folder/."),
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
                            "No .java files found in /project/folder/."),
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

                runner.test("with one source file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();
                    final File javaFile = jdkFolder.getJavaFile().await();
                    final File jarFile = jdkFolder.getJarFile().await();

                    final ManualClock clock = process.getClock();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setProject("fake-project-to-pack");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();

                    final Folder outputsFolder = projectFolder.getFolder("outputs").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final File testJsonFile = outputsFolder.getFile("test.json").await();
                    final File packJsonFile = outputsFolder.getFile("pack.json").await();
                    final File sourcesJarFile = outputsFolder.getFile("fake-project-to-pack-sources.jar").await();
                    final File compiledSourcesJarFile = outputsFolder.getFile("fake-project-to-pack.jar").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder("sources").await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides,-varargs,-serial,-overloads", "sources/A.java")
                        .setAction(() ->
                        {
                            clock.advance(Duration.minutes(1)).await();
                            aClassFile.setContentsAsString("A.java bytecode").await();
                        }));
                    childProcessRunner.add(FakeChildProcessRun.create(javaFile, "-classpath", "/project/folder/outputs/sources/", "qub.JavaProjectTest", "--verbose=false", "--testjson=true", "--logfile=/qub/fake-publisher/fake-project/data/logs/1.log", "--projectFolder=/project/folder/", "--coverage=None", "--profiler=false")
                        .setAction((FakeDesktopProcess testProcess) ->
                        {
                            clock.advance(Duration.minutes(1)).await();
                            JavaProjectTest.runTests(testProcess);
                        }));
                    JavaProjectTests.addJarVersionFakeChildProcessRun(childProcessRunner, jarFile);
                    childProcessRunner.add(FakeChildProcessRun.create(jarFile, "--create", "--file=/project/folder/outputs/fake-project-to-pack-sources.jar", "-C", "/project/folder/sources/", ".")
                        .setAction((FakeDesktopProcess testProcess) ->
                        {
                            clock.advance(Duration.minutes(1)).await();
                            sourcesJarFile.setContentsAsString("sources jar file").await();
                        }));
                    childProcessRunner.add(FakeChildProcessRun.create(jarFile, "--create", "--file=/project/folder/outputs/fake-project-to-pack.jar", "-C", "/project/folder/outputs/sources/", ".")
                        .setAction((FakeDesktopProcess testProcess) ->
                        {
                            clock.advance(Duration.minutes(1)).await();
                            compiledSourcesJarFile.setContentsAsString("compiled sources jar file").await();
                        }));

                    JavaProjectPack.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "No test classes found.",
                            "Creating outputs/fake-project-to-pack-sources.jar...",
                            "Creating outputs/fake-project-to-pack.jar..."),
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
                            sourcesJarFile,
                            compiledSourcesJarFile,
                            packJsonFile,
                            testJsonFile,
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(projectJson)
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create("sources/A.java")
                                    .setLastModified(DateTime.create(1970, 1, 1))
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create("outputs/sources/A.class", DateTime.create(1970, 1, 1, 0, 1))))))
                            .toString(JSONFormat.pretty),
                        buildJsonFile.getContentsAsString().await());
                    test.assertEqual(DateTime.create(1970, 1, 1, 0, 1), buildJsonFile.getLastModified().await());
                    test.assertEqual(
                        TestJSON.create()
                            .setJavaVersion("fake-java-version")
                            .setClassFiles(Iterable.create())
                            .toString(JSONFormat.pretty),
                        testJsonFile.getContentsAsString().await());
                    test.assertEqual(DateTime.create(1970, 1, 1, 0, 2), testJsonFile.getLastModified().await());
                    test.assertEqual(
                        PackJSON.create()
                            .setJarVersion("17")
                            .setSourceFiles(Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1970, 1, 1))))
                            .setTestSourceFiles(Iterable.create())
                            .setSourceOutputFiles(Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(1970, 1, 1, 0, 1))))
                            .setTestOutputFiles(Iterable.create())
                            .toString(JSONFormat.pretty),
                        packJsonFile.getContentsAsString().await());
                    test.assertEqual(DateTime.create(1970, 1, 1, 0, 4), packJsonFile.getLastModified().await());
                    test.assertEqual("sources jar file", sourcesJarFile.getContentsAsString().await());
                    test.assertEqual(DateTime.create(1970, 1, 1, 0, 3), sourcesJarFile.getLastModified().await());
                    test.assertEqual("compiled sources jar file", compiledSourcesJarFile.getContentsAsString().await());
                    test.assertEqual(DateTime.create(1970, 1, 1, 0, 4), compiledSourcesJarFile.getLastModified().await());

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
                            "VERBOSE: Updating test.json file...",
                            "VERBOSE: Parsing pack.json file...",
                            "VERBOSE: Getting jar version...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/jar --version",
                            "VERBOSE: Previous jar version number: null",
                            "VERBOSE: Current jar version number:  17",
                            "VERBOSE: Checking if the outputs/fake-project-to-pack-sources.jar needs to be created...",
                            "VERBOSE: Jar version changed.",
                            "Creating outputs/fake-project-to-pack-sources.jar...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/jar --create --file=/project/folder/outputs/fake-project-to-pack-sources.jar -C /project/folder/sources/ .",
                            "VERBOSE: Checking if the outputs/fake-project-to-pack.test-sources.jar needs to be created...",
                            "VERBOSE: No files exist that would go into outputs/fake-project-to-pack.test-sources.jar.",
                            "VERBOSE: Checking if the outputs/fake-project-to-pack.jar needs to be created...",
                            "VERBOSE: Jar version changed.",
                            "Creating outputs/fake-project-to-pack.jar...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/jar --create --file=/project/folder/outputs/fake-project-to-pack.jar -C /project/folder/outputs/sources/ .",
                            "VERBOSE: Checking if the outputs/fake-project-to-pack.tests.jar needs to be created...",
                            "VERBOSE: No files exist that would go into outputs/fake-project-to-pack.tests.jar.",
                            "VERBOSE: Updating outputs/pack.json..."),
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
        return JavaProjectPack.addAction(actions);
    }
}
