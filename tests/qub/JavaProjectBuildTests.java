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

                    JavaProjectBuild.addAction(actions);

                    final CommandLineAction action = actions.getAction("build").await();
                    test.assertNotNull(action);
                    test.assertEqual("build", action.getName());
                    test.assertEqual("qub-javaproject build", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Build a Java source code project.", action.getDescription());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final DesktopProcess nullProcess = null;
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");
                    test.assertThrows(() -> JavaProjectBuild.run(nullProcess, action),
                        new PreConditionFailure("process cannot be null."));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = null;
                    test.assertThrows(() -> JavaProjectBuild.run(process, action),
                        new PreConditionFailure("action cannot be null."));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub fake-project fake-action [[--projectFolder=]<projectFolder-value>] [--help] [--verbose]",
                            "  Fake action description",
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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with non-existing project folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with empty project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no openjdk/jdk project installed",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectVersionsFolder,
                            fakeProjectLogsFolder,
                            fakeProjectLogsFolder.getFile("1.log").await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with no Java source files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectVersionFolder jdkFolder = qubFolder.getProjectVersionFolder("openjdk", "jdk", "16.0.1").await();
                    jdkFolder.create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final ProjectJSON projectJson = ProjectJSON.create();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(projectJson.toString()).await();

                    JavaProjectBuild.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(),
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
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
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
                            fakeProjectVersionFolder.getCompiledSourcesFile().await(),
                            jdkFolder.getProjectFolder().await(),
                            jdkFolder.getProjectVersionsFolder().await(),
                            jdkFolder),
                        qubFolder.iterateEntriesRecursively().toList());
                });
            });
        });
    }
}
