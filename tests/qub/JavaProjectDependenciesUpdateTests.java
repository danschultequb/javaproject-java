package qub;

public interface JavaProjectDependenciesUpdateTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectDependenciesUpdate.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectDependenciesUpdate.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProjectDependenciesUpdateTests.createCommandLineActions(process);

                    final CommandLineAction action = JavaProjectDependenciesUpdate.addAction(actions);
                    test.assertNotNull(action);
                    test.assertEqual("update", action.getName());
                    test.assertEqual("qub-javaproject dependencies update", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Update the Java project's dependencies to the latest versions.", action.getDescription());
                    test.assertSame(action, actions.getAction("update").await());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);
                        test.assertThrows(() -> JavaProjectDependenciesUpdate.run((DesktopProcess)null, action),
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
                        test.assertThrows(() -> JavaProjectDependenciesUpdate.run(process, (CommandLineAction)null),
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
                        final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                        JavaProjectDependenciesUpdate.run(process, action);

                        test.assertLinesEqual(
                            Iterable.create(
                                "Usage: qub-javaproject dependencies update [[--projectFolder=]<projectFolder-value>] [--help] [--verbose] [--profiler]",
                                "  Update the Java project's dependencies to the latest versions.",
                                "  --projectFolder: The folder that contains the Java source code project to update.",
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
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "The project folder at \"/project/folder/\" doesn't exist."),
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
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    projectFolder.create().await();

                    JavaProjectDependenciesUpdate.run(process, action);

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
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.create().await();

                    JavaProjectDependenciesUpdate.run(process, action);

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
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString("[]").await();

                    JavaProjectDependenciesUpdate.run(process, action);

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

                runner.test("with no dependencies and no Intellij module file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        JavaProjectJSON.create()
                            .toString(JSONFormat.pretty))
                        .await();

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /project/folder/...",
                            "No dependencies found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

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

                runner.test("with no dependencies and Intellij module file with no dependencies",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        JavaProjectJSON.create()
                            .toString(JSONFormat.pretty))
                        .await();
                    final File intellijModuleFile = projectFolder.getFile("project.iml").await();
                    intellijModuleFile.setContentsAsString(
                        IntellijModule.create()
                            .addSourceFolder(IntellijSourceFolder.create("file://$MODULE_DIR$/sources"))
                            .toString(XMLFormat.pretty))
                        .await();

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /project/folder/...",
                            "No dependencies found.",
                            "Updating IntelliJ module files..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            intellijModuleFile,
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    test.assertLinesEqual(
                        Iterable.create(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
                            "<module type=\"JAVA_MODULE\" version=\"4\">",
                            "  <component name=\"NewModuleRootManager\">",
                            "    <content url=\"file://$MODULE_DIR$\">",
                            "      <sourceFolder url=\"file://$MODULE_DIR$/sources\"/>",
                            "    </content>",
                            "  </component>",
                            "</module>"),
                        intellijModuleFile.getContentsAsString().await());

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

                runner.test("with no dependencies and Intellij module file with a Qub dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JavaPublishedProjectFolder projectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "20").await());

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        JavaProjectJSON.create()
                            .toString(JSONFormat.pretty))
                        .await();
                    final File intellijModuleFile = projectFolder.getFile("project.iml").await();
                    intellijModuleFile.setContentsAsString(
                        IntellijModule.create()
                            .addSourceFolder(IntellijSourceFolder.create("file://$MODULE_DIR$/sources"))
                            .addModuleLibrary(IntellijModuleLibrary.create()
                                .addSourcesUrl("jar://" + projectVersionFolder.getSourcesJarFile().await().toString())
                                .addSourcesUrl("jar://" + projectVersionFolder.getTestSourcesJarFile().await().toString())
                                .addClassesUrl("jar://" + projectVersionFolder.getCompiledSourcesJarFile().await().toString())
                                .addClassesUrl("jar://" + projectVersionFolder.getCompiledTestsJarFile().await().toString()))
                            .toString(XMLFormat.pretty))
                        .await();

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /project/folder/...",
                            "No dependencies found.",
                            "Updating IntelliJ module files...",
                            "  a/b@20 - Removed"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            intellijModuleFile,
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    test.assertLinesEqual(
                        Iterable.create(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
                            "<module type=\"JAVA_MODULE\" version=\"4\">",
                            "  <component name=\"NewModuleRootManager\">",
                            "    <content url=\"file://$MODULE_DIR$\">",
                            "      <sourceFolder url=\"file://$MODULE_DIR$/sources\"/>",
                            "    </content>",
                            "  </component>",
                            "</module>"),
                        intellijModuleFile.getContentsAsString().await());

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

                runner.test("with no dependencies and Intellij module file with a non-Qub dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final QubFolder qubFolder = process.getQubFolder().await();
                    
                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        JavaProjectJSON.create()
                            .toString(JSONFormat.pretty))
                        .await();
                    final File intellijModuleFile = projectFolder.getFile("project.iml").await();
                    intellijModuleFile.setContentsAsString(
                        IntellijModule.create()
                            .addSourceFolder(IntellijSourceFolder.create("file://$MODULE_DIR$/sources"))
                            .addModuleLibrary(IntellijModuleLibrary.create()
                                .addSourcesUrl("Not a normal Qub dependency"))
                            .toString(XMLFormat.pretty))
                        .await();

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /project/folder/...",
                            "No dependencies found.",
                            "Updating IntelliJ module files..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            intellijModuleFile,
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    test.assertLinesEqual(
                        Iterable.create(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
                            "<module type=\"JAVA_MODULE\" version=\"4\">",
                            "  <component name=\"NewModuleRootManager\">",
                            "    <content url=\"file://$MODULE_DIR$\">",
                            "      <sourceFolder url=\"file://$MODULE_DIR$/sources\"/>",
                            "    </content>",
                            "    <orderEntry type=\"module-library\">",
                            "      <library>",
                            "        <CLASSES/>",
                            "        <JAVADOC/>",
                            "        <SOURCES>",
                            "          <root url=\"Not a normal Qub dependency\"/>",
                            "        </SOURCES>",
                            "      </library>",
                            "    </orderEntry>",
                            "  </component>",
                            "</module>"),
                        intellijModuleFile.getContentsAsString().await());

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

                runner.test("with one Qub dependency and Intellij module file with no dependencies",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/project/folder/")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesUpdateTests.createCommandLineAction(process);

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JavaPublishedProjectFolder projectVersionFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder("a", "b", "20").await());
                    projectVersionFolder.getCompiledSourcesJarFile().await()
                        .create().await();

                    final FileSystem fileSystem = process.getFileSystem();
                    final Folder projectFolder = fileSystem.getFolder("/project/folder/").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        JavaProjectJSON.create()
                            .setDependencies(Iterable.create(projectVersionFolder.getProjectSignature().await()))
                            .toString(JSONFormat.pretty))
                        .await();
                    final File intellijModuleFile = projectFolder.getFile("project.iml").await();
                    intellijModuleFile.setContentsAsString(
                        IntellijModule.create()
                            .addSourceFolder(IntellijSourceFolder.create("file://$MODULE_DIR$/sources"))
                            .toString(XMLFormat.pretty))
                        .await();

                    JavaProjectDependenciesUpdate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /project/folder/...",
                            "Found 1 dependencies:",
                            "  a/b@20 - No updates",
                            "Updating IntelliJ module files...",
                            "  a/b@20 - Added"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            intellijModuleFile,
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    test.assertLinesEqual(
                        Iterable.create(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
                            "<module type=\"JAVA_MODULE\" version=\"4\">",
                            "  <component name=\"NewModuleRootManager\">",
                            "    <content url=\"file://$MODULE_DIR$\">",
                            "      <sourceFolder url=\"file://$MODULE_DIR$/sources\"/>",
                            "    </content>",
                            "    <orderEntry type=\"module-library\">",
                            "      <library>",
                            "        <CLASSES>",
                            "          <root url=\"jar:///qub/a/b/versions/20/b.jar!/\"/>",
                            "          <root url=\"jar:///qub/a/b/versions/20/b.tests.jar!/\"/>",
                            "        </CLASSES>",
                            "        <JAVADOC/>",
                            "        <SOURCES>",
                            "          <root url=\"jar:///qub/a/b/versions/20/b.sources.jar!/\"/>",
                            "          <root url=\"jar:///qub/a/b/versions/20/b.test.sources.jar!/\"/>",
                            "        </SOURCES>",
                            "      </library>",
                            "    </orderEntry>",
                            "  </component>",
                            "</module>"),
                        intellijModuleFile.getContentsAsString().await());

                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final Folder fakeProjectVersionsFolder = fakeProjectFolder.getProjectVersionsFolder().await();
                    final JavaPublishedProjectFolder fakeProjectVersionFolder = JavaPublishedProjectFolder.get(fakeProjectFolder.getProjectVersionFolder("8").await());
                    test.assertEqual(
                        Iterable.create(
                            projectVersionFolder.getPublisherFolder().await(),
                            fakePublisherFolder,
                            projectVersionFolder.getProjectFolder().await(),
                            projectVersionFolder.getProjectVersionsFolder().await(),
                            projectVersionFolder,
                            projectVersionFolder.getCompiledSourcesJarFile().await(),
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

    static CommandLineActions createCommandLineActions(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = JavaProject.createCommandLineActions(process);
        final CommandLineAction dependenciesAction = JavaProjectDependencies.addAction(actions);
        return dependenciesAction.createCommandLineActions();
    }

    static CommandLineAction createCommandLineAction(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = JavaProjectDependenciesUpdateTests.createCommandLineActions(process);
        return JavaProjectDependenciesUpdate.addAction(actions);
    }
}
