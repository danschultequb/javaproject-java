package qub;

public interface JavaProjectCleanTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectClean.class, () ->
        {
            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    test.assertThrows(() -> JavaProjectClean.run(null, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectClean.run(process, null),
                        new PreConditionFailure("action cannot be null."));
                });

                runner.test("with -?",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("-?")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject clean [[--projectFolder=]<projectFolder-value>] [--help] [--verbose] [--profiler]",
                            "  Clean a Java source code project's build outputs.",
                            "  --projectFolder: The folder that contains a Java project to build. Defaults to the current folder.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs.",
                            "  --profiler:      Whether or not this application should pause before it is run to allow a profiler to be attached."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("with run -?",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("run", "-?")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject clean [[--projectFolder=]<projectFolder-value>] [--help] [--verbose] [--profiler]",
                            "  Clean a Java source code project's build outputs.",
                            "  --projectFolder: The folder that contains a Java project to build. Defaults to the current folder.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs.",
                            "  --profiler:      Whether or not this application should pause before it is run to allow a profiler to be attached."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("when folder to clean doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning...",
                            "The project folder /folder/to/clean/ doesn't exist."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });

                runner.test("when no folders to clean exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    process.getFileSystem().createFolder("/folder/to/clean/").await();

                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });

                runner.test("when no folders to clean exist with verbose",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    process.getFileSystem().createFolder("/folder/to/clean/").await();

                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });

                runner.test("when outputs folder exists",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder folderToClean = process.getFileSystem().getFolder("/folder/to/clean/").await();
                    final Folder outputsFolder = folderToClean.getFolder("outputs").await();
                    outputsFolder.create().await();

                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning...",
                            "Deleting folder " + outputsFolder + "... Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertFalse(outputsFolder.exists().await());
                    test.assertTrue(folderToClean.exists().await());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });

                runner.test("when outputs folder exists with verbose",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder folderToClean = process.getFileSystem().getFolder("/folder/to/clean/").await();
                    final Folder outputsFolder = folderToClean.getFolder("outputs").await();
                    outputsFolder.create().await();

                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning...",
                            "Deleting folder /folder/to/clean/outputs/... Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertFalse(outputsFolder.exists().await());
                    test.assertTrue(folderToClean.exists().await());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });

                runner.test("when outputs folder exists but can't be deleted",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/folder/to/clean/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final InMemoryFileSystem fileSystem = process.getFileSystem();
                    final Folder folderToClean = fileSystem.getFolder("/folder/to/clean/").await();
                    final Folder outputsFolder = folderToClean.getFolder("outputs").await();
                    outputsFolder.create().await();
                    fileSystem.setFolderCanDelete(outputsFolder.getPath(), false).await();

                    final CommandLineAction action = JavaProjectCleanTests.createAction(process);

                    JavaProjectClean.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Cleaning...",
                            "Deleting folder /folder/to/clean/outputs/... Failed.",
                            "  The folder at \"/folder/to/clean/outputs/\" doesn't exist."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                    test.assertTrue(outputsFolder.exists().await());
                    test.assertTrue(process.getQubProjectDataFolder().await().exists().await());
                });
            });
        });
    }

    static CommandLineAction createAction(FakeDesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = JavaProject.createCommandLineActions(process);
        return JavaProjectClean.addAction(actions);
    }
}
