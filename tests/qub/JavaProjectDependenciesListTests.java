package qub;

public interface JavaProjectDependenciesListTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectDependenciesList.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectDependenciesList.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProjectDependenciesTests.createCommandLineActions(process);
                    final CommandLineAction action = JavaProjectDependenciesList.addAction(actions);
                    test.assertNotNull(action);
                    test.assertEqual("list", action.getName());
                    test.assertEqual("qub-javaproject dependencies list", action.getFullName());
                    test.assertEqual("List the Java project's dependencies.", action.getDescription());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertSame(process, action.getProcess());
                    test.assertSame(action, actions.getAction(action.getName()).await());
                });
            });

            runner.testGroup("run(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});

                    test.assertThrows(() -> JavaProjectDependenciesList.run(null, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectDependenciesList.run(process, null),
                        new PreConditionFailure("action cannot be null."));
                });

                runner.test("with " + Strings.escapeAndQuote("-?"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("-?")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject dependencies list [[--projectFolder=]<projectFolder-value>] [--recurse] [--help] [--verbose] [--profiler]",
                            "  List the Java project's dependencies.",
                            "  --projectFolder: The folder that contains the Java project. This can be either a source code folder or a published folder.",
                            "  --recurse(r):    Whether the entire dependency tree will be output (true) or just the immediate dependencies (false). Defaults to false.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs.",
                            "  --profiler:      Whether or not this application should pause before it is run to allow a profiler to be attached."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("with no project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "The file at \"/project.json\" doesn't exist.",
                            "No dependencies found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with empty project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString("").await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Missing object left curly bracket ('{').",
                            "No dependencies found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with no dependencies",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "No dependencies found."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ProjectSignature.parse("fake-publisher/fake-project@123512345").await()))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency that doesn't exist with --recurse",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--recurse")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ProjectSignature.parse("fake-publisher/fake-project@123512345").await()))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345",
                            "  The file at \"/qub/fake-publisher/fake-project/versions/123512345/project.json\" doesn't exist."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency that exists with --recurse",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--recurse")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final ProjectSignature dependency = ProjectSignature.parse("fake-publisher/fake-project@123512345").await();
                    final JavaPublishedProjectFolder dependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(dependency).await());
                    dependencyFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .toString())
                            .await();

                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ProjectSignature.parse("fake-publisher/fake-project@123512345").await()))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency with sub-dependency that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final ProjectSignature dependency = ProjectSignature.parse("fake-publisher/fake-project@123512345").await();
                    final ProjectSignature subDependency = ProjectSignature.parse("a/b@3").await();
                    final JavaPublishedProjectFolder publishedProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(dependency).await());
                    publishedProjectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                subDependency))
                            .toString())
                            .await();

                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                dependency))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency with sub-dependency that doesn't exist and --recurse",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--recurse")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final ProjectSignature dependency = ProjectSignature.parse("fake-publisher/fake-project@123512345").await();
                    final ProjectSignature subDependency = ProjectSignature.parse("a/b@3").await();
                    final JavaPublishedProjectFolder publishedProjectFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(dependency).await());
                    publishedProjectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                subDependency))
                            .toString())
                            .await();

                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                dependency))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345",
                            "    a/b@3",
                            "    The file at \"/qub/a/b/versions/3/project.json\" doesn't exist."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with one dependency with sub-dependency and --recurse",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--recurse")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final ProjectSignature dependency = ProjectSignature.parse("fake-publisher/fake-project@123512345").await();
                    final ProjectSignature subDependency = ProjectSignature.parse("a/b@3").await();
                    final JavaPublishedProjectFolder dependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(dependency).await());
                    dependencyFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                subDependency))
                            .toString())
                            .await();
                    final JavaPublishedProjectFolder subDependencyFolder = JavaPublishedProjectFolder.get(qubFolder.getProjectVersionFolder(subDependency).await());
                    subDependencyFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create().toString()).await();

                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                dependency))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 1 dependency:",
                            "  fake-publisher/fake-project@123512345",
                            "    a/b@3"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });

                runner.test("with two dependencies that don't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(process.getCurrentFolder());
                    projectFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ProjectSignature.parse("fake-publisher/fake-project@123512345").await(),
                                ProjectSignature.parse("fake-publisher/fake-project2@1234").await()))
                            .toString())
                            .await();

                    final CommandLineAction action = JavaProjectDependenciesListTests.createCommandLineAction(process);
                    
                    JavaProjectDependenciesList.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Getting dependencies for /...",
                            "Found 2 dependencies:",
                            "  fake-publisher/fake-project@123512345",
                            "  fake-publisher/fake-project2@1234"),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });
            });
        });
    }

    public static CommandLineAction createCommandLineAction(FakeDesktopProcess process)
    {
        final CommandLineActions actions = JavaProjectDependenciesTests.createCommandLineActions(process);
        return JavaProjectDependenciesList.addAction(actions);
    }
}
