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

                    JavaProjectTest.addAction(actions);

                    final CommandLineAction action = actions.getAction("test").await();
                    test.assertNotNull(action);
                    test.assertEqual("test", action.getName());
                    test.assertEqual("qub-javaproject test", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Run the tests of a Java source code project.", action.getDescription());
                });
            });
            
            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectTest.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProjectTest.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Running tests..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());
                });
            });
        });
    }
}
