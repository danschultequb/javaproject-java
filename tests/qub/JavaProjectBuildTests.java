package qub;

public interface JavaProjectBuildTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectBuild.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectBuild.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);

                    final CommandLineAction action = JavaProjectBuild.addAction(actions);
                    test.assertNotNull(action);
                    test.assertEqual("build", action.getName());
                    test.assertEqual("qub-javaproject build", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Build a Java source code project.", action.getDescription());
                    test.assertSame(action, actions.getAction("build").await());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    test.assertThrows(() -> JavaProjectBuild.run((DesktopProcess)null, action),
                        new PreConditionFailure("process cannot be null."));

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

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectBuild.run(process, (CommandLineAction)null),
                        new PreConditionFailure("action cannot be null."));

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

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject build [[--projectFolder=]<projectFolder-value>] [--help] [--verbose]",
                            "  Build a Java source code project.",
                            "  --projectFolder: The folder that contains a Java project to build. Defaults to the current folder.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs."),
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
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    JavaProjectBuild.run(process, action);

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
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    projectFolder.create().await();

                    JavaProjectBuild.run(process, action);

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
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.create().await();

                    JavaProjectBuild.run(process, action);

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
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString("[]").await();

                    JavaProjectBuild.run(process, action);

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
                    final CommandLineAction action = JavaProjectTests.createAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final ProjectJSON projectJson = ProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    JavaProjectBuild.run(process, action);

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

                    JavaProjectBuild.run(process, action);

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
                    final CommandLineAction action = JavaProjectTests.createAction(process);
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

                    JavaProjectBuild.run(process, action);

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

                runner.test("with one source file and no outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

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

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one source file and empty outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

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
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with two source files and empty outputs folder",
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
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();
                    bJavaFile.setContentsAsString("B.java source code").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                                bClassFile.setContentsAsString("B.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 source files..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
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
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: sources/B.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 2 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with two source file folders and empty outputs folder",
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
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File bJavaFile = testsFolder.getFile("B.java").await();
                    bJavaFile.setContentsAsString("B.java source code").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsTestsFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/tests/", "--class-path", "/project/folder/outputs/tests/;/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "tests/B.java")
                            .setAction(() ->
                            {
                                bClassFile.setContentsAsString("B.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "Compiling 1 test source file..."),
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
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
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
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: tests/B.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "Compiling 1 test source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/tests/ --class-path /project/folder/outputs/tests/;/project/folder/outputs/sources/ -Xlint:all,-try,-overrides tests/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one source file and class file with same age but no build.json file",
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
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));

                    final ManualClock clock = process.getClock();
                    clock.advance(Duration.minutes(1));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());

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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one source file and class file with same age and build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code").await();
                    final BuildJSON buildJson = BuildJSON.create()
                        .setProjectJson(projectJson)
                        .setJavacVersion("17")
                        .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                            .setLastModified(startTime)
                            .setClassFiles(Iterable.create(
                                BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))));
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    buildJsonFile.setContentsAsString(buildJson.toString()).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    clock.advance(Duration.minutes(1));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one source file newer than existing class file and build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code").await();
                    final BuildJSON buildJson = BuildJSON.create()
                        .setProjectJson(projectJson)
                        .setJavacVersion("17")
                        .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                            .setLastModified(startTime)
                            .setClassFiles(Iterable.create(
                                BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))));
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    buildJsonFile.setContentsAsString(buildJson.toString()).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one source file with one error",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction((FakeDesktopProcess childProcess) ->
                            {
                                JavaProjectTests.writeIssues(childProcess.getErrorWriteStream(),
                                    JavacIssue.create()
                                        .setSourceFilePath("sources\\A.java")
                                        .setLineNumber(1)
                                        .setColumnNumber(20)
                                        .setType("error")
                                        .setMessage("This doesn't look right to me."));
                                childProcess.setExitCode(1);
                            }));

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "1 Error:",
                            "sources/A.java (Line 1): This doesn't look right to me."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            projectJsonFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "1 Error:",
                            "sources/A.java (Line 1): This doesn't look right to me.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one unmodified source file with one warning",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source code").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final BuildJSON buildJson = BuildJSON.create()
                        .setProjectJson(projectJson)
                        .setJavacVersion("17")
                        .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                            .setLastModified(startTime)
                            .setClassFiles(Iterable.create(
                                BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))
                            .setIssues(Iterable.create(
                                JavacIssue.create()
                                    .setSourceFilePath(aJavaFile.relativeTo(projectFolder))
                                    .setLineNumber(1)
                                    .setColumnNumber(20)
                                    .setType("WARNING")
                                    .setMessage("Are you sure?"))));
                    buildJsonFile.setContentsAsString(buildJson.toString(JSONFormat.pretty)).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    clock.advance(Duration.minutes(1));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled.",
                            "1 Unmodified Warning:",
                            "sources/A.java (Line 1): Are you sure?"),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "1 Unmodified Warning:",
                            "sources/A.java (Line 1): Are you sure?",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one modified source file with one warning",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();
                    final BuildJSON buildJson = BuildJSON.create()
                        .setProjectJson(projectJson)
                        .setJavacVersion("17")
                        .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                            .setLastModified(startTime)
                            .setClassFiles(Iterable.create(
                                BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))
                            .setIssues(Iterable.create(
                                JavacIssue.create()
                                    .setSourceFilePath(aJavaFile.relativeTo(projectFolder))
                                    .setLineNumber(1)
                                    .setColumnNumber(20)
                                    .setType("WARNING")
                                    .setMessage("Are you sure?"))));
                    buildJsonFile.setContentsAsString(buildJson.toString(JSONFormat.pretty)).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction((FakeDesktopProcess childProcess) ->
                            {
                                JavaProjectTests.writeIssues(childProcess.getErrorWriteStream(),
                                    JavacIssue.create()
                                        .setSourceFilePath("sources\\A.java")
                                        .setLineNumber(1)
                                        .setColumnNumber(20)
                                        .setType("warning")
                                        .setMessage("Are you still sure?"));
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));;

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "1 Warning:",
                            "sources/A.java (Line 1): Are you still sure?"),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "1 Warning:",
                            "sources/A.java (Line 1): Are you still sure?",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with two source files with one error and one warning",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final ManualClock clock = process.getClock();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();
                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File bJavaFile = testsFolder.getFile("B.java").await();
                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction((FakeDesktopProcess childProcess) ->
                            {
                                JavaProjectTests.writeIssues(childProcess.getErrorWriteStream(),
                                    JavacIssue.create()
                                        .setSourceFilePath("sources\\A.java")
                                        .setLineNumber(1)
                                        .setColumnNumber(5)
                                        .setType("error")
                                        .setMessage("Are you sure?"));
                                childProcess.setExitCode(1);
                            }));

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();
                    bJavaFile.setContentsAsString("B.java source code").await();

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file...",
                            "1 Error:",
                            "sources/A.java (Line 1): Are you sure?"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            buildJsonFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: tests/B.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "1 Error:",
                            "sources/A.java (Line 1): Are you sure?",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with multiple source files with errors and warnings",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder testsFolder = projectFolder.createFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    final File cJavaFile = testsFolder.getFile("C.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final Folder outputsTestsFolder = outputsFolder.getFolder(testsFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File aTestsClassFile = outputsTestsFolder.getFile("ATests.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aClassFile.setContentsAsString("A.java byte code").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();
                    bJavaFile.setContentsAsString("B.java source code").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source code").await();
                    cJavaFile.setContentsAsString("C.java source code").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java")
                            .setAction((FakeDesktopProcess childProcess) ->
                            {
                                JavaProjectTests.writeIssues(childProcess.getErrorWriteStream(),
                                    JavacIssue.create()
                                        .setSourceFilePath("sources\\A.java")
                                        .setLineNumber(12)
                                        .setColumnNumber(2)
                                        .setType("error")
                                        .setMessage("Are you sure?"),
                                    JavacIssue.create()
                                        .setSourceFilePath("sources\\B.java")
                                        .setLineNumber(1)
                                        .setColumnNumber(5)
                                        .setType("error")
                                        .setMessage("Are you sure?"));
                                childProcess.setExitCode(2);
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 source files...",
                            "2 Errors:",
                            "sources/A.java (Line 12): Are you sure?",
                            "sources/B.java (Line 1): Are you sure?"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(2, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            outputsFolder,
                            sourcesFolder,
                            testsFolder,
                            projectJsonFile,
                            outputsSourcesFolder,
                            buildJsonFile,
                            aClassFile,
                            aJavaFile,
                            bJavaFile,
                            aTestsJavaFile,
                            cJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: sources/B.java - New file",
                            "VERBOSE: tests/ATests.java - New file",
                            "VERBOSE: tests/C.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 2 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "2 Errors:",
                            "sources/A.java (Line 12): Are you sure?",
                            "sources/B.java (Line 1): Are you sure?",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with two source files newer than their existing class files",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code - 2").await();
                    bJavaFile.setContentsAsString("B.java source code - 2").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 source files..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), bClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: sources/B.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 2 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one modified source file and one unmodified and independent source file",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    bJavaFile.setContentsAsString("B.java source code - 1").await();
                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code - 2").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, bClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 1", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one modified source file and one unmodified and dependent source file",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    bJavaFile.setContentsAsString("B.java source code, Depends on A - 1").await();
                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    aJavaFile.relativeTo(projectFolder))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code - 2").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 source files..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), bClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: sources/B.java - Dependency file(s) being compiled",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 2 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one unmodified source file and another modified and dependant source file",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code - 1").await();
                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    aJavaFile.relativeTo(projectFolder))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    bJavaFile.setContentsAsString("B.java source code, Depends on A - 1").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/B.java")
                            .setAction(() ->
                            {
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), bClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/B.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one deleted source file and another unmodified and dependant source file",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bJavaFile.setContentsAsString("B.java source code, Depends on A - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(aJavaFile.relativeTo(projectFolder))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/B.java")
                            .setAction(() ->
                            {
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            bClassFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Deleted",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: sources/B.java - Dependency file(s) were deleted",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with one new source file and another unmodified and independent source file",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    bJavaFile.setContentsAsString("B.java source code").await();
                    bClassFile.setContentsAsString("B.java byte code").await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    aJavaFile.setContentsAsString("A.java source code").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            bClassFile,
                            aJavaFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code", bClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, bClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - New file",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("N depends on nothing, A depends on B, B depends on C, C.java is modified: A, B, and C should be compiled",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();
                    final File cJavaFile = sourcesFolder.getFile("C.java").await();
                    final File nJavaFile = sourcesFolder.getFile("N.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File cClassFile = outputsSourcesFolder.getFile("C.class").await();
                    final File nClassFile = outputsSourcesFolder.getFile("N.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code, Depends on B").await();
                    bJavaFile.setContentsAsString("B.java source code, Depends on C").await();
                    nJavaFile.setContentsAsString("N.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    cClassFile.setContentsAsString("C.java byte code - 1").await();
                    nClassFile.setContentsAsString("N.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    bJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    cJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(cJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(cClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(nJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(nClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    cJavaFile.setContentsAsString("C.java source code").await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java", "sources/C.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                                cClassFile.setContentsAsString("C.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 3 source files..."),
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
                            aClassFile,
                            bClassFile,
                            cClassFile,
                            nClassFile,
                            aJavaFile,
                            bJavaFile,
                            cJavaFile,
                            nJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), bClassFile.getLastModified().await());
                    test.assertEqual("C.java byte code - 2", cClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), cClassFile.getLastModified().await());
                    test.assertEqual("N.java byte code - 1", nClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, nClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/C.java - Modified",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: sources/B.java - Dependency file(s) being compiled",
                            "VERBOSE: sources/A.java - Dependency file(s) being compiled",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 3 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java sources/C.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("N depends on nothing, A depends on B, B depends on C, C.java is deleted: A and B should be compiled",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();
                    final File cJavaFile = sourcesFolder.getFile("C.java").await();
                    final File nJavaFile = sourcesFolder.getFile("N.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File cClassFile = outputsSourcesFolder.getFile("C.class").await();
                    final File nClassFile = outputsSourcesFolder.getFile("N.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code, Depends on B").await();
                    bJavaFile.setContentsAsString("B.java source code, Depends on C").await();
                    nJavaFile.setContentsAsString("N.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    cClassFile.setContentsAsString("C.java byte code - 1").await();
                    nClassFile.setContentsAsString("N.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    bJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    cJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(cJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(cClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(nJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(nClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java", "sources/B.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                                bClassFile.setContentsAsString("B.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 source files..."),
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
                            aClassFile,
                            bClassFile,
                            nClassFile,
                            aJavaFile,
                            bJavaFile,
                            nJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 2", bClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), bClassFile.getLastModified().await());
                    test.assertEqual("N.java byte code - 1", nClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, nClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/C.java - Deleted",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: sources/B.java - Dependency file(s) were deleted",
                            "VERBOSE: sources/A.java - Dependency file(s) being compiled",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 2 source files...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java sources/B.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("N depends on nothing, A depends on B, B depends on C, C.class is deleted: C should be compiled",
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

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();
                    final File cJavaFile = sourcesFolder.getFile("C.java").await();
                    final File nJavaFile = sourcesFolder.getFile("N.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File bClassFile = outputsSourcesFolder.getFile("B.class").await();
                    final File cClassFile = outputsSourcesFolder.getFile("C.class").await();
                    final File nClassFile = outputsSourcesFolder.getFile("N.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code, Depends on B").await();
                    bJavaFile.setContentsAsString("B.java source code, Depends on C").await();
                    cJavaFile.setContentsAsString("C.java source code").await();
                    nJavaFile.setContentsAsString("N.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();
                    bClassFile.setContentsAsString("B.java byte code - 1").await();
                    nClassFile.setContentsAsString("N.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    bJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime)))
                                .setDependencies(Iterable.create(
                                    cJavaFile.relativeTo(projectFolder))))
                            .setJavaFile(BuildJSONJavaFile.create(cJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(cClassFile.relativeTo(projectFolder), startTime))))
                            .setJavaFile(BuildJSONJavaFile.create(nJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(nClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/C.java")
                            .setAction(() ->
                            {
                                cClassFile.setContentsAsString("C.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            bClassFile,
                            cClassFile,
                            nClassFile,
                            aJavaFile,
                            bJavaFile,
                            cJavaFile,
                            nJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual("B.java byte code - 1", bClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, bClassFile.getLastModified().await());
                    test.assertEqual("C.java byte code - 2", cClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), cClassFile.getLastModified().await());
                    test.assertEqual("N.java byte code - 1", nClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, nClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/C.java - Missing or modified class file(s)",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/C.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("nothing gets compiled when project.json publisher changes",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create()
                                .setPublisher("old-fake-publisher")
                                .setProject("fake-project")
                                .setVersion("fake-version"))
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("nothing gets compiled when project.json project changes",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create()
                                .setPublisher("fake-publisher")
                                .setProject("old-fake-project")
                                .setVersion("fake-version"))
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("nothing gets compiled when project.json version changes",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code - 1").await();

                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create()
                                .setPublisher("fake-publisher")
                                .setProject("fake-project")
                                .setVersion("old-fake-version"))
                            .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                .setLastModified(startTime)
                                .setClassFiles(Iterable.create(
                                    BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                            .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("nothing gets compiled when project.json java dependency is added",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder otherProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());
                    otherProjectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(otherProjectFolder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code").await();

                    buildJsonFile.setContentsAsString(
                            BuildJSON.create()
                                .setJavacVersion("17")
                                .setProjectJson(JavaProjectJSON.create()
                                    .setPublisher("fake-publisher")
                                    .setProject("fake-project")
                                    .setVersion("fake-version"))
                                .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                                .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            aClassFile,
                            buildJsonFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            otherProjectFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder,
                            otherProjectFolder.getProjectFolder().await(),
                            otherProjectFolder.getProjectVersionsFolder().await(),
                            otherProjectFolder,
                            otherProjectFolder.getProjectJsonFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("everything gets compiled when project.json java dependency is removed",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder otherProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());
                    otherProjectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code").await();

                    buildJsonFile.setContentsAsString(
                            BuildJSON.create()
                                .setJavacVersion("17")
                                .setProjectJson(JavaProjectJSON.create()
                                    .setPublisher("fake-publisher")
                                    .setProject("fake-project")
                                    .setVersion("fake-version")
                                    .setDependencies(Iterable.create(otherProjectFolder.getProjectSignature().await())))
                                .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                                .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE:   Previous dependencies have changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has not changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            otherProjectFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder,
                            otherProjectFolder.getProjectFolder().await(),
                            otherProjectFolder.getProjectVersionsFolder().await(),
                            otherProjectFolder,
                            otherProjectFolder.getProjectJsonFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("everything gets compiled when project.json java dependency version is changed",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final QubProjectFolder otherProjectFolder = qubFolder.getProjectFolder("other-publisher", "other-project").await();
                    final JavaPublishedProjectFolder otherProjectVersionFolder1 = JavaPublishedProjectFolder.get(otherProjectFolder.getProjectVersionFolder("1").await());
                    otherProjectVersionFolder1.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();
                    final JavaPublishedProjectFolder otherProjectVersionFolder2 = JavaPublishedProjectFolder.get(otherProjectFolder.getProjectVersionFolder("2").await());
                    otherProjectVersionFolder2.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(otherProjectVersionFolder2.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code").await();

                    buildJsonFile.setContentsAsString(
                            BuildJSON.create()
                                .setJavacVersion("17")
                                .setProjectJson(JavaProjectJSON.create()
                                    .setPublisher("fake-publisher")
                                    .setProject("fake-project")
                                    .setVersion("fake-version")
                                    .setDependencies(Iterable.create(otherProjectVersionFolder1.getProjectSignature().await())))
                                .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime))))
                                .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/;/qub/other-publisher/other-project/versions/2/other-project.jar", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code - 2", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE:   Previous dependencies have changed.",
                            "VERBOSE: Checking if latest installed JDK has changed since the previous build...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac --version",
                            "VERBOSE:   Installed JDK has not changed.",
                            "VERBOSE: Looking for .java files that have been deleted...",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/;/qub/other-publisher/other-project/versions/2/other-project.jar -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            otherProjectFolder.getPublisherFolder().await(),
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder,
                            otherProjectFolder,
                            otherProjectFolder.getProjectVersionsFolder().await(),
                            otherProjectVersionFolder1,
                            otherProjectVersionFolder2,
                            otherProjectVersionFolder1.getProjectJsonFile().await(),
                            otherProjectVersionFolder2.getProjectJsonFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with deleted source file with anonymous classes",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    final File bJavaFile = sourcesFolder.getFile("B.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    final File a1ClassFile = outputsFolder.getFile("A$1.class").await();
                    final File a2ClassFile = outputsFolder.getFile("A$2.class").await();
                    final File bClassFile = outputsFolder.getFile("B.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    bJavaFile.setContentsAsString("B.java source code").await();

                    aClassFile.setContentsAsString("A.java byte code").await();
                    a1ClassFile.setContentsAsString("A.java anonymous class 1 byte code").await();
                    a2ClassFile.setContentsAsString("A.java anonymous class 2 byte code").await();
                    bClassFile.setContentsAsString("B.java byte code").await();

                    buildJsonFile.setContentsAsString(
                            BuildJSON.create()
                                .setJavacVersion("17")
                                .setProjectJson(JavaProjectJSON.create()
                                    .setPublisher("fake-publisher")
                                    .setProject("fake-project")
                                    .setVersion("fake-version"))
                                .setJavaFile(BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), startTime),
                                        BuildJSONClassFile.create(a1ClassFile.relativeTo(projectFolder), startTime),
                                        BuildJSONClassFile.create(a2ClassFile.relativeTo(projectFolder), startTime))))
                                .setJavaFile(BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(bClassFile.relativeTo(projectFolder), startTime))))
                                .toString())
                        .await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "No .java files need to be compiled."),
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
                            bClassFile,
                            buildJsonFile,
                            bJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("B.java byte code", bClassFile.getContentsAsString().await());
                    test.assertEqual(startTime, bClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "VERBOSE: sources/A.java - Deleted",
                            "VERBOSE: Looking for .java files to compile...",
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "No .java files need to be compiled.",
                            "VERBOSE: Updating outputs/build.json..."),
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

                runner.test("with project.json dependency with publisher that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder otherProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(otherProjectFolder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final ManualClock clock = process.getClock();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "An error occurred while discovering dependencies:",
                            "1. No publisher folder named \"other-publisher\" found in the Qub folder (/qub/)."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            projectJsonFile,
                            aJavaFile),
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
                            "An error occurred while discovering dependencies:",
                            "1. No publisher folder named \"other-publisher\" found in the Qub folder (/qub/).",
                            "VERBOSE: qub.NotFoundException: No publisher folder named \"other-publisher\" found in the Qub folder (/qub/)."),
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

                runner.test("with project.json dependency with project that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                        final CommandLineAction action = JavaProjectTests.createAction(process);
                        final QubFolder qubFolder = process.getQubFolder().await();
                        final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                        final File javacFile = jdkFolder.getJavacFile().await();

                        final JavaPublishedProjectFolder otherProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());
                        final QubPublisherFolder otherPublisherFolder = otherProjectFolder.getPublisherFolder().await();
                        otherPublisherFolder.create().await();

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        final JavaProjectJSON projectJson = JavaProjectJSON.create()
                            .setPublisher("fake-publisher")
                            .setProject("fake-project")
                            .setVersion("fake-version")
                            .setDependencies(Iterable.create(otherProjectFolder.getProjectSignature().await()));
                        final File projectJsonFile = projectFolder.getFile("project.json").await();
                        projectJsonFile.setContentsAsString(projectJson.toString()).await();

                        final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                        JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                        JavaProjectBuild.run(process, action);

                        test.assertLinesEqual(
                            Iterable.create(
                                "An error occurred while discovering dependencies:",
                                "1. No project folder named \"other-project\" found in the \"other-publisher\" publisher folder (/qub/other-publisher/)."),
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
                                "An error occurred while discovering dependencies:",
                                "1. No project folder named \"other-project\" found in the \"other-publisher\" publisher folder (/qub/other-publisher/).",
                                "VERBOSE: qub.NotFoundException: No project folder named \"other-project\" found in the \"other-publisher\" publisher folder (/qub/other-publisher/)."),
                            fakeLogFile.getContentsAsString().await());
                        test.assertEqual(
                            Iterable.create(
                                fakePublisherFolder,
                                jdkFolder.getPublisherFolder().await(),
                                otherPublisherFolder,
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

                runner.test("with project.json dependency with version that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                        final CommandLineAction action = JavaProjectTests.createAction(process);
                        final QubFolder qubFolder = process.getQubFolder().await();
                        final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                        final File javacFile = jdkFolder.getJavacFile().await();

                        final JavaPublishedProjectFolder otherProjectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());
                        final QubPublisherFolder otherPublisherFolder = otherProjectVersionFolder.getPublisherFolder().await();
                        final QubProjectFolder otherProjectFolder = otherProjectVersionFolder.getProjectFolder().await();
                        otherProjectFolder.create().await();

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        final JavaProjectJSON projectJson = JavaProjectJSON.create()
                            .setPublisher("fake-publisher")
                            .setProject("fake-project")
                            .setVersion("fake-version")
                            .setDependencies(Iterable.create(otherProjectVersionFolder.getProjectSignature().await()));
                        final File projectJsonFile = projectFolder.getFile("project.json").await();
                        projectJsonFile.setContentsAsString(projectJson.toString()).await();

                        final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                        JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                        JavaProjectBuild.run(process, action);

                        test.assertLinesEqual(
                            Iterable.create(
                                "An error occurred while discovering dependencies:",
                                "1. No version folder named \"other-version\" found in the \"other-publisher/other-project\" project folder (/qub/other-publisher/other-project/)."),
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
                                "An error occurred while discovering dependencies:",
                                "1. No version folder named \"other-version\" found in the \"other-publisher/other-project\" project folder (/qub/other-publisher/other-project/).",
                                "VERBOSE: qub.NotFoundException: No version folder named \"other-version\" found in the \"other-publisher/other-project\" project folder (/qub/other-publisher/other-project/)."),
                            fakeLogFile.getContentsAsString().await());
                        test.assertEqual(
                            Iterable.create(
                                fakePublisherFolder,
                                jdkFolder.getPublisherFolder().await(),
                                otherPublisherFolder,
                                fakeProjectFolder,
                                fakeProjectDataFolder,
                                fakeProjectVersionsFolder,
                                fakeProjectLogsFolder,
                                fakeProjectLogsFolder.getFile("1.log").await(),
                                fakeProjectVersionFolder,
                                fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                                jdkFolder.getProjectFolder().await(),
                                jdkFolder.getProjectVersionsFolder().await(),
                                jdkFolder,
                                otherProjectFolder),
                            qubFolder.iterateEntriesRecursively().toList());
                    });

                runner.test("with project.json dependency with project.json that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder otherProjectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("other-publisher", "other-project", "other-version").await());
                    final QubPublisherFolder otherPublisherFolder = otherProjectVersionFolder.getPublisherFolder().await();
                    final QubProjectFolder otherProjectFolder = otherProjectVersionFolder.getProjectFolder().await();
                    otherProjectVersionFolder.create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(otherProjectVersionFolder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "An error occurred while discovering dependencies:",
                            "1. The file at \"/qub/other-publisher/other-project/versions/other-version/project.json\" doesn't exist."),
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
                            "An error occurred while discovering dependencies:",
                            "1. The file at \"/qub/other-publisher/other-project/versions/other-version/project.json\" doesn't exist.",
                            "VERBOSE: qub.FileNotFoundException: The file at \"/qub/other-publisher/other-project/versions/other-version/project.json\" doesn't exist."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            otherPublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesJarFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder,
                            otherProjectFolder,
                            otherProjectFolder.getProjectVersionsFolder().await(),
                            otherProjectVersionFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with one-hop transitive dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder ab1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "1").await());
                    ab1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();
                    ab1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final JavaPublishedProjectFolder ac1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "c", "1").await());
                    ac1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ab1Folder.getProjectSignature().await()))
                            .toString()).await();
                    ac1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(ac1Folder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/;/qub/a/c/versions/1/c.jar;/qub/a/b/versions/1/b.jar", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/;/qub/a/c/versions/1/c.jar;/qub/a/b/versions/1/b.jar -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            ab1Folder.getPublisherFolder().await(),
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            ab1Folder.getProjectFolder().await(),
                            ac1Folder.getProjectFolder().await(),
                            ab1Folder.getProjectVersionsFolder().await(),
                            ab1Folder,
                            ab1Folder.getCompiledSourcesJarFile().await(),
                            ab1Folder.getProjectJsonFile().await(),
                            ac1Folder.getProjectVersionsFolder().await(),
                            ac1Folder,
                            ac1Folder.getCompiledSourcesJarFile().await(),
                            ac1Folder.getProjectJsonFile().await(),
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

                runner.test("with two-hop transitive dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder ab1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "1").await());
                    ab1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();
                    ab1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final JavaPublishedProjectFolder ac1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "c", "1").await());
                    ac1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ab1Folder.getProjectSignature().await()))
                            .toString()).await();
                    ac1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final JavaPublishedProjectFolder de5Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("d", "e", "5").await());
                    de5Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ac1Folder.getProjectSignature().await()))
                            .toString()).await();
                    de5Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(de5Folder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final ManualClock clock = process.getClock();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/;/qub/d/e/versions/5/e.jar;/qub/a/c/versions/1/c.jar;/qub/a/b/versions/1/b.jar", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java byte code", aClassFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());

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
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/;/qub/d/e/versions/5/e.jar;/qub/a/c/versions/1/c.jar;/qub/a/b/versions/1/b.jar -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            ab1Folder.getPublisherFolder().await(),
                            de5Folder.getPublisherFolder().await(),
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            ab1Folder.getProjectFolder().await(),
                            ac1Folder.getProjectFolder().await(),
                            ab1Folder.getProjectVersionsFolder().await(),
                            ab1Folder,
                            ab1Folder.getCompiledSourcesJarFile().await(),
                            ab1Folder.getProjectJsonFile().await(),
                            ac1Folder.getProjectVersionsFolder().await(),
                            ac1Folder,
                            ac1Folder.getCompiledSourcesJarFile().await(),
                            ac1Folder.getProjectJsonFile().await(),
                            de5Folder.getProjectFolder().await(),
                            de5Folder.getProjectVersionsFolder().await(),
                            de5Folder,
                            de5Folder.getCompiledSourcesJarFile().await(),
                            de5Folder.getProjectJsonFile().await(),
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

                runner.test("with multiple versions of same project dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final JavaPublishedProjectFolder ab1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "1").await());
                    ab1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();
                    ab1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final JavaPublishedProjectFolder ab2Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "2").await());
                    ab2Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();
                    ab2Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final JavaPublishedProjectFolder ac1Folder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "c", "1").await());
                    ac1Folder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ab1Folder.getProjectSignature().await()))
                            .toString()).await();
                    ac1Folder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version")
                        .setDependencies(Iterable.create(
                            ab2Folder.getProjectSignature().await(),
                            ac1Folder.getProjectSignature().await()));
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final ManualClock clock = process.getClock();

                    aJavaFile.setContentsAsString("A.java source code").await();

                    clock.advance(Duration.minutes(1));

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "An error occurred while discovering dependencies:",
                            "1. Found more than one required version for package a/b:",
                            "   1. a/b@2",
                            "   2. a/b@1",
                            "       from a/c@1"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            projectJsonFile,
                            aJavaFile),
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
                            "An error occurred while discovering dependencies:",
                            "1. Found more than one required version for package a/b:",
                            "   1. a/b@2",
                            "   2. a/b@1",
                            "       from a/c@1",
                            "VERBOSE: java.lang.RuntimeException: Found more than one required version for package a/b:",
                            "VERBOSE: 1. a/b@2",
                            "VERBOSE: 2. a/b@1",
                            "VERBOSE:     from a/c@1"),
                        fakeLogFile.getContentsAsString().await());
                    test.assertEqual(
                        Iterable.create(
                            ab1Folder.getPublisherFolder().await(),
                            fakePublisherFolder,
                            jdkFolder.getPublisherFolder().await(),
                            ab1Folder.getProjectFolder().await(),
                            ac1Folder.getProjectFolder().await(),
                            ab1Folder.getProjectVersionsFolder().await(),
                            ab1Folder,
                            ab2Folder,
                            ab1Folder.getCompiledSourcesJarFile().await(),
                            ab1Folder.getProjectJsonFile().await(),
                            ab2Folder.getCompiledSourcesJarFile().await(),
                            ab2Folder.getProjectJsonFile().await(),
                            ac1Folder.getProjectVersionsFolder().await(),
                            ac1Folder,
                            ac1Folder.getCompiledSourcesJarFile().await(),
                            ac1Folder.getProjectJsonFile().await(),
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

                runner.test("with source code file modified after compilation but before build.json file updated",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectTests.createAction(process);
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JDKFolder jdkFolder = JavaProjectTests.getJdkFolder(qubFolder);
                    final File javacFile = jdkFolder.getJavacFile().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setPublisher("fake-publisher")
                        .setProject("fake-project")
                        .setVersion("fake-version");
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    final Folder sourcesFolder = projectFolder.createFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();

                    final Folder outputsFolder = projectFolder.createFolder("outputs").await();
                    final File buildJsonFile = outputsFolder.getFile("build.json").await();

                    final Folder outputsSourcesFolder = outputsFolder.getFolder(sourcesFolder.getName()).await();
                    final File aClassFile = outputsSourcesFolder.getFile("A.class").await();

                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    aJavaFile.setContentsAsString("A.java source code - 1").await();

                    clock.advance(Duration.minutes(1));
                    final DateTime beforeBuild = clock.getCurrentDateTime();

                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    JavaProjectTests.addJavacVersionFakeChildProcessRun(childProcessRunner, javacFile);
                    childProcessRunner.add(
                        FakeChildProcessRun.create(javacFile, "-d", "/project/folder/outputs/sources/", "--class-path", "/project/folder/outputs/sources/", "-Xlint:all,-try,-overrides", "sources/A.java")
                            .setAction(() ->
                            {
                                aClassFile.setContentsAsString("A.java byte code - 1").await();
                                clock.advance(Duration.minutes(1));
                                aJavaFile.setContentsAsString("A.java source code - 2").await();
                            }));

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 source file..."),
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
                            aClassFile,
                            aJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual("A.java source code - 2", aJavaFile.getContentsAsString().await());
                    test.assertEqual(clock.getCurrentDateTime(), aJavaFile.getLastModified().await());
                    test.assertEqual("A.java byte code - 1", aClassFile.getContentsAsString().await());
                    test.assertEqual(beforeBuild, aClassFile.getLastModified().await());
                    test.assertEqual(clock.getCurrentDateTime(), buildJsonFile.getLastModified().await());
                    test.assertEqual(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setProjectJson(JavaProjectJSON.create()
                                .setPublisher("fake-publisher")
                                .setProject("fake-project")
                                .setVersion("fake-version"))
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(startTime)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), beforeBuild)))))
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
                            "VERBOSE: Update .java file dependencies...",
                            "VERBOSE: Discovering unmodified .java files that have dependencies that are being compiled or were deleted...",
                            "VERBOSE: Discovering unmodified .java files that have missing or modified .class files...",
                            "Compiling 1 source file...",
                            "VERBOSE: /qub/openjdk/jdk/versions/17/bin/javac -d /project/folder/outputs/sources/ --class-path /project/folder/outputs/sources/ -Xlint:all,-try,-overrides sources/A.java",
                            "VERBOSE: Adding compilation issues to new build.json...",
                            "VERBOSE: Associating .class files with original .java files...",
                            "VERBOSE: Updating outputs/build.json..."),
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
}
