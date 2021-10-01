package qub;

public interface JavaPublishedProjectFolderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaPublishedProjectFolder.class, () ->
        {
            runner.testGroup("get(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaPublishedProjectFolder.get(null),
                        new PreConditionFailure("innerFolder cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final Folder folder = JavaPublishedProjectFolderTests.getFolder();
                    final JavaPublishedProjectFolder javaFolder = JavaPublishedProjectFolder.get(folder);
                    test.assertEqual(folder, javaFolder);
                    test.assertEqual(javaFolder, folder);
                });
            });
        });
    }

    static Folder getFolder()
    {
        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
        fileSystem.createRoot("/").await();
        return fileSystem.getFolder("/qub/fake-publisher/fake-project/versions/fake-version/").await();
    }
}
