package qub;

public interface JavaProjectPackTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectPack.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectPack.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = JavaProject.createCommandLineActions(process);

                    JavaProjectPack.addAction(actions);

                    final CommandLineAction action = actions.getAction("pack").await();
                    test.assertNotNull(action);
                    test.assertEqual("pack", action.getName());
                    test.assertEqual("qub-javaproject pack", action.getFullName());
                    test.assertEqual(Iterable.create(), action.getAliases());
                    test.assertEqual("Package a Java source code project.", action.getDescription());
                });
            });
            
            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectPack.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProjectPack.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Packing..."),
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
