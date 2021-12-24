package qub;

public interface JavaClassFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaClassFile.class, () ->
        {
            runner.testGroup("get(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaClassFile.get(null),
                        new PreConditionFailure("file cannot be null."));
                });

                runner.test("with file that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final File file = process.getCurrentFolder().getFile("testFile").await();
                    final JavaClassFile javaClassFile = JavaClassFile.get(file);
                    test.assertNotNull(javaClassFile);
                    test.assertEqual(file, javaClassFile);
                    test.assertThrows(() -> javaClassFile.getLastModified().await(),
                        new FileNotFoundException(javaClassFile));
                    test.assertThrows(() -> javaClassFile.getLastModified().await(),
                        new FileNotFoundException(javaClassFile));
                });

                runner.test("with file that exists",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final ManualClock clock = process.getClock();
                    final DateTime startTime = clock.getCurrentDateTime();

                    final File file = process.getCurrentFolder().createFile("testFile").await();
                    final JavaClassFile javaClassFile = JavaClassFile.get(file);
                    test.assertNotNull(javaClassFile);
                    test.assertEqual(file, javaClassFile);
                    test.assertEqual(startTime, javaClassFile.getLastModified().await());
                    test.assertEqual(startTime, javaClassFile.getLastModified().await());
                });
            });
        });
    }
}
