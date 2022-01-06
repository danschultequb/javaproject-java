package qub;

public interface PackJSONFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(PackJSONFile.class, () ->
        {
            runner.testGroup("create(String,DateTime)", () ->
            {
                final Action3<String,DateTime,Throwable> createErrorTest = (String fileRelativePath, DateTime lastModified, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(fileRelativePath), lastModified), (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.create(fileRelativePath, lastModified),
                            expected);
                    });
                };

                createErrorTest.run(null, DateTime.create(1, 2, 3), new PreConditionFailure("fileRelativePath cannot be null."));
                createErrorTest.run("", DateTime.create(1, 2, 3), new PreConditionFailure("fileRelativePath cannot be empty."));
                createErrorTest.run("/test.txt", DateTime.create(1, 2, 3), new PreConditionFailure("fileRelativePath.isRooted() cannot be true."));
                createErrorTest.run("test.txt", null, new PreConditionFailure("lastModified cannot be null."));

                final Action2<String,DateTime> createTest = (String fileRelativePath, DateTime lastModified) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(fileRelativePath), lastModified), (Test test) ->
                    {
                        final PackJSONFile packJsonFile = PackJSONFile.create(fileRelativePath, lastModified);
                        test.assertNotNull(packJsonFile);
                        test.assertEqual(Path.parse(fileRelativePath), packJsonFile.getRelativePath());
                        test.assertEqual(lastModified, packJsonFile.getLastModified());
                    });
                };

                createTest.run("sources/A.java", DateTime.create(2020, 5, 3));
            });

            runner.testGroup("create(Path,DateTime)", () ->
            {
                final Action3<Path,DateTime,Throwable> createErrorTest = (Path fileRelativePath, DateTime lastModified, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(fileRelativePath), lastModified), (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.create(fileRelativePath, lastModified),
                            expected);
                    });
                };

                createErrorTest.run(null, DateTime.create(1, 2, 3), new PreConditionFailure("fileRelativePath cannot be null."));
                createErrorTest.run(Path.parse("/test.txt"), DateTime.create(1, 2, 3), new PreConditionFailure("fileRelativePath.isRooted() cannot be true."));
                createErrorTest.run(Path.parse("test.txt"), null, new PreConditionFailure("lastModified cannot be null."));

                final Action2<Path,DateTime> createTest = (Path fileRelativePath, DateTime lastModified) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(fileRelativePath), lastModified), (Test test) ->
                    {
                        final PackJSONFile packJsonFile = PackJSONFile.create(fileRelativePath, lastModified);
                        test.assertNotNull(packJsonFile);
                        test.assertEqual(fileRelativePath, packJsonFile.getRelativePath());
                        test.assertEqual(lastModified, packJsonFile.getLastModified());
                    });
                };

                createTest.run(Path.parse("sources/A.java"), DateTime.create(2020, 5, 3));
            });

            runner.testGroup("create(JSONProperty)", () ->
            {
                final Action2<JSONProperty,Throwable> createErrorTest = (JSONProperty jsonProperty, Throwable expected) ->
                {
                    runner.test("with " + jsonProperty, (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.create(jsonProperty),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("json cannot be null."));

                final Action3<JSONProperty,Path,DateTime> createTest = (JSONProperty jsonProperty, Path expectedFileRelativePath, DateTime expectedLastModified) ->
                {
                    runner.test("with " + jsonProperty, (Test test) ->
                    {
                        final PackJSONFile packJsonFile = PackJSONFile.create(jsonProperty);
                        test.assertNotNull(packJsonFile);
                        test.assertEqual(expectedFileRelativePath, packJsonFile.getRelativePath());
                        test.assertEqual(expectedLastModified, packJsonFile.getLastModified());
                    });
                };

                createTest.run(
                    JSONProperty.create("sources/A.java", JSONObject.create()),
                    Path.parse("sources/A.java"),
                    null);
                createTest.run(
                    JSONProperty.create("sources/A.java", ""),
                    Path.parse("sources/A.java"),
                    null);
                createTest.run(
                    JSONProperty.create("sources/A.java", DateTime.create(2020, 5, 3).toString()),
                    Path.parse("sources/A.java"),
                    DateTime.create(2020, 5, 3));
            });
        });
    }
}
