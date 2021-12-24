package qub;

public interface JavaFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaFile.class, () ->
        {
            runner.testGroup("get(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaFile.get(null),
                        new PreConditionFailure("file cannot be null."));
                });

                runner.test("with file that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final File file = process.getCurrentFolder().getFile("testFile").await();
                    final JavaFile javaFile = JavaFile.get(file);
                    test.assertNotNull(javaFile);
                    test.assertEqual(file, javaFile);
                    test.assertThrows(() -> javaFile.getLastModified().await(),
                        new FileNotFoundException(javaFile));
                    test.assertThrows(() -> javaFile.getLastModified().await(),
                        new FileNotFoundException(javaFile));
                });

                runner.test("with file that exists",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final File file = process.getCurrentFolder().createFile("testFile").await();
                    final JavaFile javaFile = JavaFile.get(file);
                    test.assertNotNull(javaFile);
                    test.assertEqual(file, javaFile);
                    test.assertEqual(startTime, javaFile.getLastModified().await());
                    test.assertEqual(startTime, javaFile.getLastModified().await());
                });
            });
        });
    }
}
