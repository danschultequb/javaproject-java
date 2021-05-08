package qub;

public interface JavaProjectTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProject.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProject.main(null),
                        new PreConditionFailure("args cannot be null."));
                });
            });

            runner.testGroup("createCommandLineActions(DesktopProcess)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProject.createCommandLineActions(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);
                    test.assertNotNull(actions);
                    test.assertEqual("qub-javaproject", actions.getApplicationName());
                    test.assertEqual("An application used to interact with Java source code projects.", actions.getApplicationDescription());
                    test.assertSame(process, actions.getProcess());
                });
            });

            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProject.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with no arguments",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProject.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject [--action=]<action-name> [--help]",
                            "  An application used to interact with Java source code projects.",
                            "  --action(a): The name of the action to invoke.",
                            "  --help(?):   Show the help message for this application.",
                            "",
                            "Actions:",
                            "  build:         Build a Java source code project.",
                            "  configuration: Open the configuration file for this application.",
                            "  create:        Create a new Java source code project.",
                            "  logs:          Show the logs folder.",
                            "  pack:          Package a Java source code project.",
                            "  publish:       Publish a Java source code project.",
                            "  test:          Run the tests of a Java source code project."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("with --help",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProject.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub-javaproject [--action=]<action-name> [--help]",
                            "  An application used to interact with Java source code projects.",
                            "  --action(a): The name of the action to invoke.",
                            "  --help(?):   Show the help message for this application.",
                            "",
                            "Actions:",
                            "  build:         Build a Java source code project.",
                            "  configuration: Open the configuration file for this application.",
                            "  create:        Create a new Java source code project.",
                            "  logs:          Show the logs folder.",
                            "  pack:          Package a Java source code project.",
                            "  publish:       Publish a Java source code project.",
                            "  test:          Run the tests of a Java source code project."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });

                runner.test("with unrecognized action (spam)",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("spam")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProject.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Unrecognized action: \"spam\"",
                            "",
                            "Usage: qub-javaproject [--action=]<action-name> [--help]",
                            "  An application used to interact with Java source code projects.",
                            "  --action(a): The name of the action to invoke.",
                            "  --help(?):   Show the help message for this application.",
                            "",
                            "Actions:",
                            "  build:         Build a Java source code project.",
                            "  configuration: Open the configuration file for this application.",
                            "  create:        Create a new Java source code project.",
                            "  logs:          Show the logs folder.",
                            "  pack:          Package a Java source code project.",
                            "  publish:       Publish a Java source code project.",
                            "  test:          Run the tests of a Java source code project."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());
                });
            });
        });
    }
}