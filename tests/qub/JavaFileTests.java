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

            runner.testGroup("getFullTypeName(Path)", () ->
            {
                final Action2<Path,Throwable> getFullTypeNameErrorTest = (Path relativeFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativeFilePath), (Test test) ->
                    {
                        test.assertThrows(() -> JavaFile.getFullTypeName(relativeFilePath),
                            expected);
                    });
                };

                getFullTypeNameErrorTest.run(null, new PreConditionFailure("relativeFilePath cannot be null."));
                getFullTypeNameErrorTest.run(Path.parse("/test"), new PreConditionFailure("relativeFilePath.isRooted() cannot be true."));

                final Action2<Path,String> getFullTypeNameTest = (Path relativePath, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativePath), (Test test) ->
                    {
                        test.assertEqual(expected, JavaFile.getFullTypeName(relativePath));
                    });
                };

                getFullTypeNameTest.run(Path.parse("a/b/c.java"), "a.b.c");
            });

            runner.testGroup("getFullTypeName(Folder,File)", () ->
            {
                runner.test("with null baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = null;
                        final File file = process.getCurrentFolder().getFile("fake/base/folder/file").await();

                        test.assertThrows(() -> JavaFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("baseFolder cannot be null."));
                    });

                runner.test("with null file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = null;

                        test.assertThrows(() -> JavaFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("file cannot be null."));
                    });

                runner.test("with file outside of baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("../other-folder/file").await();

                        test.assertThrows(() -> JavaFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("baseFolder.isAncestorOf(file).await() cannot be false."));
                    });

                runner.test("with file immediately under baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("file").await();

                        test.assertEqual("file", JavaFile.getFullTypeName(baseFolder, file));
                    });

                runner.test("with file immediately under baseFolder child-folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("subfolder/file").await();

                        test.assertEqual("subfolder.file", JavaFile.getFullTypeName(baseFolder, file));
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
