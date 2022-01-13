package qub;

public interface JavaProjectPublishTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectPublish.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectPublish.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);

                    JavaProjectPublish.addAction(actions);

                    final CommandLineAction action = actions.getAction("publish").await();
                    test.assertNotNull(action);
                    test.assertEqual("publish", action.getName());
                    test.assertEqual("qub-javaproject publish", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Publish a Java source code project.", action.getDescription());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        test.assertThrows(() -> JavaProjectPublish.run(null, action),
                            new PreConditionFailure("process cannot be null."));
                    });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        test.assertThrows(() -> JavaProjectPublish.run(process, null),
                            new PreConditionFailure("action cannot be null."));
                    });

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        JavaProjectPublish.run(process, action);

                        test.assertLinesEqual(
                            Iterable.create(
                                "Usage: qub-javaproject publish [[--projectFolder=]<projectFolder-value>] [--help] [--verbose] [--profiler]",
                                "  Publish a Java source code project.",
                                "  --projectFolder: The folder that contains a Java project to publish. Defaults to the current folder.",
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
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        JavaProjectPublish.run(process, action);

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
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        projectFolder.create().await();

                        JavaProjectPublish.run(process, action);

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
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        final File projectJsonFile = projectFolder.getFile("project.json").await();
                        projectJsonFile.create().await();

                        JavaProjectPublish.run(process, action);

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
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        final File projectJsonFile = projectFolder.getFile("project.json").await();
                        projectJsonFile.setContentsAsString("[]").await();

                        JavaProjectPublish.run(process, action);

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
                        final CommandLineAction action = JavaProjectPublishTests.createAction(process);

                        final FileSystem fileSystem = process.getFileSystem();
                        final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                        final ProjectJSON projectJson = ProjectJSON.create();
                        final File projectJsonFile = projectFolder.getFile("project.json").await();
                        projectJsonFile.setContentsAsString(projectJson.toString()).await();

                        JavaProjectPublish.run(process, action);

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
            });
        });
    }

    static CommandLineAction createAction(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = JavaProject.createCommandLineActions(process);
        return JavaProjectPublish.addAction(actions);
    }
}
