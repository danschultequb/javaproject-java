package qub;

public interface JavacTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(Javac.class, () ->
        {
            runner.test("version()",
                (TestResources resources) -> Tuple.create(resources.getProcess()),
                (Test test, DesktopProcess process) ->
            {
                final Javac javac = Javac.create(process.getChildProcessRunner());
                final VersionNumber versionNumber = javac.version().await();
                test.assertEqual(
                    VersionNumber.create()
                        .setMajor(16)
                        .setMinor(0)
                        .setPatch(1),
                    versionNumber);
            });
        });
    }
}
