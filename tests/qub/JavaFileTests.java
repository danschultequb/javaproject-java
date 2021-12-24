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



            runner.testGroup("getRelativePathFromFullTypeName(String)", () ->
            {
                final Action2<String,Throwable> getRelativePathFromFullTypeNameErrorTest = (String fullTypeName, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullTypeName), (Test test) ->
                    {
                        test.assertThrows(() -> JavaFile.getRelativePathFromFullTypeName(fullTypeName),
                            expected);
                    });
                };

                getRelativePathFromFullTypeNameErrorTest.run(null, new PreConditionFailure("fullTypeName cannot be null."));
                getRelativePathFromFullTypeNameErrorTest.run("", new PreConditionFailure("fullTypeName cannot be empty."));

                final Action2<String,Path> getRelativePathFromFullTypeNameTest = (String fullTypeName, Path expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullTypeName), (Test test) ->
                    {
                        test.assertEqual(expected, JavaFile.getRelativePathFromFullTypeName(fullTypeName));
                    });
                };

                getRelativePathFromFullTypeNameTest.run("a", Path.parse("a.java"));
                getRelativePathFromFullTypeNameTest.run("a.b", Path.parse("a/b.java"));
                getRelativePathFromFullTypeNameTest.run("a.b.c", Path.parse("a/b/c.java"));
                getRelativePathFromFullTypeNameTest.run("a.b$5", Path.parse("a/b.java"));
            });
        });
    }
}
