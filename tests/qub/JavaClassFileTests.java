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

            runner.testGroup("getFromFullTypeName(Folder,String)", () ->
            {
                runner.test("with null outputFolder", (Test test) ->
                {
                    test.assertThrows(() -> JavaClassFile.getFromFullTypeName(null, "fake.full.typename"),
                        new PreConditionFailure("outputFolder cannot be null."));
                });

                runner.test("with null fullTypeName",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder outputFolder = process.getCurrentFolder().getFolder("fake/output/folder").await();
                    test.assertThrows(() -> JavaClassFile.getFromFullTypeName(outputFolder, null),
                        new PreConditionFailure("fullTypeName cannot be null."));
                });

                runner.test("with empty fullTypeName",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder outputFolder = process.getCurrentFolder().getFolder("fake/output/folder").await();
                    test.assertThrows(() -> JavaClassFile.getFromFullTypeName(outputFolder, ""),
                        new PreConditionFailure("fullTypeName cannot be empty."));
                });

                runner.test("with non-empty fullTypeName",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder outputFolder = process.getCurrentFolder().getFolder("fake/output/folder").await();
                    final JavaClassFile classFile = JavaClassFile.getFromFullTypeName(outputFolder, "fake.full.typename");
                    test.assertNotNull(classFile);
                    test.assertEqual("/fake/output/folder/fake/full/typename.class", classFile.toString());
                });
            });

            runner.testGroup("getFullTypeName(Path)", () ->
            {
                final Action2<Path,Throwable> getFullTypeNameErrorTest = (Path relativeFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativeFilePath), (Test test) ->
                    {
                        test.assertThrows(() -> JavaClassFile.getFullTypeName(relativeFilePath),
                            expected);
                    });
                };

                getFullTypeNameErrorTest.run(null, new PreConditionFailure("relativeFilePath cannot be null."));
                getFullTypeNameErrorTest.run(Path.parse("/test"), new PreConditionFailure("relativeFilePath.isRooted() cannot be true."));

                final Action2<Path,String> getFullTypeNameTest = (Path relativePath, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativePath), (Test test) ->
                    {
                        test.assertEqual(expected, JavaClassFile.getFullTypeName(relativePath));
                    });
                };

                getFullTypeNameTest.run(Path.parse("a/b/c.class"), "a.b.c");
                getFullTypeNameTest.run(Path.parse("a/b/c$5.class"), "a.b.c$5");
            });

            runner.testGroup("getFullTypeName(Folder,File)", () ->
            {
                runner.test("with null baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = null;
                        final File file = process.getCurrentFolder().getFile("fake/base/folder/file").await();

                        test.assertThrows(() -> JavaClassFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("baseFolder cannot be null."));
                    });

                runner.test("with null file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = null;

                        test.assertThrows(() -> JavaClassFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("file cannot be null."));
                    });

                runner.test("with file outside of baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("../other-folder/file").await();

                        test.assertThrows(() -> JavaClassFile.getFullTypeName(baseFolder, file),
                            new PreConditionFailure("baseFolder.isAncestorOf(file).await() cannot be false."));
                    });

                runner.test("with file immediately under baseFolder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("file").await();

                        test.assertEqual("file", JavaClassFile.getFullTypeName(baseFolder, file));
                    });

                runner.test("with file immediately under baseFolder child-folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final Folder baseFolder = process.getCurrentFolder().getFolder("fake/base/folder/").await();
                        final File file = baseFolder.getFile("subfolder/file").await();

                        test.assertEqual("subfolder.file", JavaClassFile.getFullTypeName(baseFolder, file));
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
