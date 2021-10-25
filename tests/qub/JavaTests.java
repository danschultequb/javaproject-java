package qub;

public interface JavaTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(Java.class, () ->
        {
            runner.testGroup("create(ChildProcessRunner)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> Java.create(null),
                        new PreConditionFailure("childProcessRunner cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final FakeChildProcessRunner childProcessRunner = process.getChildProcessRunner();
                    final Java java = Java.create(childProcessRunner);
                    test.assertNotNull(java);
                    test.assertEqual("java", java.getExecutablePath().toString());
                });
            });
        });
    }
}
