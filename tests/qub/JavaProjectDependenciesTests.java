package qub;

public interface JavaProjectDependenciesTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectDependencies.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectDependencies.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);
                    final CommandLineAction action = JavaProjectDependencies.addAction(actions);
                    test.assertNotNull(action);
                    test.assertEqual("dependencies", action.getName());
                    test.assertEqual("qub-javaproject dependencies", action.getFullName());
                    test.assertEqual("Perform actions based on a Java project's dependencies.", action.getDescription());
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

                    test.assertThrows(() -> JavaProjectDependencies.run(null, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectDependencies.run(process, null),
                        new PreConditionFailure("action cannot be null."));
                });

                runner.test("with no command line arguments",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesTests.createCommandLineAction(process);
                    
                    JavaProjectDependencies.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject dependencies [--action=]<action-name> [--help]",
                            "  Perform actions based on a Java project's dependencies.",
                            "  --action(a): The name of the action to invoke.",
                            "  --help(?):   Show the help message for this application.",
                            "",
                            "Actions:",
                            "  list:   List the Java project's dependencies.",
                            "  update: Update the Java project's dependencies to the latest versions."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("with " + Strings.escapeAndQuote("-?"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("-?")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = JavaProjectDependenciesTests.createCommandLineAction(process);
                    
                    JavaProjectDependencies.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject dependencies [--action=]<action-name> [--help]",
                            "  Perform actions based on a Java project's dependencies.",
                            "  --action(a): The name of the action to invoke.",
                            "  --help(?):   Show the help message for this application.",
                            "",
                            "Actions:",
                            "  list:   List the Java project's dependencies.",
                            "  update: Update the Java project's dependencies to the latest versions."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });
            });
        });
    }

    public static CommandLineAction createCommandLineAction(FakeDesktopProcess process)
    {
        final CommandLineActions actions = JavaProject.createCommandLineActions(process);
        return JavaProjectDependencies.addAction(actions);
    }

    public static CommandLineActions createCommandLineActions(FakeDesktopProcess process)
    {
        return JavaProjectDependenciesTests.createCommandLineAction(process)
            .createCommandLineActions();
    }
}
