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
            
            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectPublish.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProjectPublish.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Publishing..."),
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
