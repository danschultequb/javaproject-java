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

            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectBuild.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    JavaProjectBuild.run(process);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Building..."),
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
