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

            runner.testGroup("getRelativePathFromFullTypeName(String)", () ->
            {
                final Action2<String,Throwable> getRelativePathFromFullTypeNameErrorTest = (String fullTypeName, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullTypeName), (Test test) ->
                    {
                        test.assertThrows(() -> JavaClassFile.getRelativePathFromFullTypeName(fullTypeName),
                            expected);
                    });
                };

                getRelativePathFromFullTypeNameErrorTest.run(null, new PreConditionFailure("fullTypeName cannot be null."));
                getRelativePathFromFullTypeNameErrorTest.run("", new PreConditionFailure("fullTypeName cannot be empty."));

                final Action2<String,Path> getRelativePathFromFullTypeNameTest = (String fullTypeName, Path expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullTypeName), (Test test) ->
                    {
                        test.assertEqual(expected, JavaClassFile.getRelativePathFromFullTypeName(fullTypeName));
                    });
                };

                getRelativePathFromFullTypeNameTest.run("a", Path.parse("a.class"));
                getRelativePathFromFullTypeNameTest.run("a.b", Path.parse("a/b.class"));
                getRelativePathFromFullTypeNameTest.run("a.b.c", Path.parse("a/b/c.class"));
                getRelativePathFromFullTypeNameTest.run("a.b$5", Path.parse("a/b$5.class"));
            });
        });
    }
}
