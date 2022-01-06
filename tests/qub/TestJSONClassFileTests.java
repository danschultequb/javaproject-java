package qub;

public interface TestJSONClassFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(TestJSONClassFile.class, () ->
        {
            runner.testGroup("create(JSONProperty)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSONClassFile.create((JSONProperty)null),
                        new PreConditionFailure("innerProperty cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    test.assertNotNull(classFile);
                    test.assertEqual("a", classFile.getFullTypeName());
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                });
            });

            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String testClassRelativePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(testClassRelativePath), (Test test) ->
                    {
                        test.assertThrows(() -> TestJSONClassFile.create(testClassRelativePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("testClassRelativePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("testClassRelativePath cannot be empty."));
                createErrorTest.run("/rooted/path", new PreConditionFailure("testClassRelativePath.isRooted() cannot be true."));

                final Action1<String> createTest = (String testClassRelativePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(testClassRelativePath), (Test test) ->
                    {
                        final TestJSONClassFile classFile = TestJSONClassFile.create(testClassRelativePath);
                        test.assertNotNull(classFile);
                        test.assertEqual(Path.parse(testClassRelativePath), classFile.getRelativePath());
                        test.assertEqual(JavaClassFile.getFullTypeName(Path.parse(testClassRelativePath)), classFile.getFullTypeName());
                        test.assertNull(classFile.getLastModified());
                        test.assertNull(classFile.getPassedTestCount());
                        test.assertNull(classFile.getSkippedTestCount());
                        test.assertNull(classFile.getFailedTestCount());
                    });
                };

                createTest.run("a/b/c.class");
            });

            runner.testGroup("create(Path)", () ->
            {
                final Action2<Path,Throwable> createErrorTest = (Path testClassRelativePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(testClassRelativePath), (Test test) ->
                    {
                        test.assertThrows(() -> TestJSONClassFile.create(testClassRelativePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("testClassRelativePath cannot be null."));
                createErrorTest.run(Path.parse("/rooted/path"), new PreConditionFailure("testClassRelativePath.isRooted() cannot be true."));

                final Action1<Path> createTest = (Path testClassRelativePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(testClassRelativePath), (Test test) ->
                    {
                        final TestJSONClassFile classFile = TestJSONClassFile.create(testClassRelativePath);
                        test.assertNotNull(classFile);
                        test.assertEqual(testClassRelativePath, classFile.getRelativePath());
                        test.assertEqual(JavaClassFile.getFullTypeName(testClassRelativePath), classFile.getFullTypeName());
                        test.assertNull(classFile.getLastModified());
                        test.assertNull(classFile.getPassedTestCount());
                        test.assertNull(classFile.getSkippedTestCount());
                        test.assertNull(classFile.getFailedTestCount());
                    });
                };

                createTest.run(Path.parse("a/b/c.class"));
            });

            runner.testGroup("getLastModified()", () ->
            {
                final Action2<TestJSONClassFile,DateTime> getLastModifiedTest = (TestJSONClassFile classFile, DateTime expected) ->
                {
                    runner.test("with " + classFile.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, classFile.getLastModified());
                    });
                };

                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", "b")),
                    null);
                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create())),
                    null);
                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNull("lastModified"))),
                    null);
                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("lastModified", ""))),
                    null);
                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("lastModified", "hello"))),
                    null);
                getLastModifiedTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("lastModified", DateTime.create(1, 2, 3).toString()))),
                    DateTime.create(1, 2, 3));
            });

            runner.testGroup("setLastModified(DateTime)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("a");
                    test.assertThrows(() -> classFile.setLastModified(null),
                        new PreConditionFailure("lastModified cannot be null."));
                    test.assertNull(classFile.getLastModified());
                });

                runner.test("with non-object JSON value", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    final TestJSONClassFile setLastModifiedResult = classFile.setLastModified(DateTime.create(1, 2, 3));
                    test.assertSame(classFile, setLastModifiedResult);
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                    test.assertNull(classFile.getLastModified());
                });

                final Action2<TestJSONClassFile,DateTime> setLastModifiedTest = (TestJSONClassFile classFile, DateTime lastModified) ->
                {
                    runner.test("with " + English.andList(classFile, lastModified), (Test test) ->
                    {
                        final TestJSONClassFile setLastModifiedResult = classFile.setLastModified(lastModified);
                        test.assertSame(classFile, setLastModifiedResult);
                        test.assertEqual(lastModified, classFile.getLastModified());
                    });
                };

                setLastModifiedTest.run(
                    TestJSONClassFile.create("a"),
                    DateTime.create(10, 11, 12));
                setLastModifiedTest.run(
                    TestJSONClassFile.create("a")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    DateTime.create(10, 11, 12));
            });

            runner.testGroup("getPassedTestCount()", () ->
            {
                final Action2<TestJSONClassFile,Integer> getPassedTestCount = (TestJSONClassFile classFile, Integer expected) ->
                {
                    runner.test("with " + classFile.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, classFile.getPassedTestCount());
                    });
                };

                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", "b")),
                    null);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create())),
                    null);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNull("passedTestCount"))),
                    null);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("passedTestCount", ""))),
                    null);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("passedTestCount", "hello"))),
                    null);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("passedTestCount", 5))),
                    5);
                getPassedTestCount.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("passedTestCount", 6.0))),
                    null);
            });

            runner.testGroup("setPassedTestCount(int)", () ->
            {
                runner.test("with non-object JSON value", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    final TestJSONClassFile setLastModifiedResult = classFile.setPassedTestCount(5);
                    test.assertSame(classFile, setLastModifiedResult);
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                    test.assertNull(classFile.getPassedTestCount());
                });

                final Action2<TestJSONClassFile,Integer> setPassedTestCountTest = (TestJSONClassFile classFile, Integer passedTestCount) ->
                {
                    runner.test("with " + English.andList(classFile, passedTestCount), (Test test) ->
                    {
                        final TestJSONClassFile setPassedTestCountResult = classFile.setPassedTestCount(passedTestCount);
                        test.assertSame(classFile, setPassedTestCountResult);
                        test.assertEqual(passedTestCount, classFile.getPassedTestCount());
                    });
                };

                setPassedTestCountTest.run(
                    TestJSONClassFile.create("a"),
                    10);
                setPassedTestCountTest.run(
                    TestJSONClassFile.create("a")
                        .setPassedTestCount(15),
                    30);
            });

            runner.testGroup("getSkippedTestCount()", () ->
            {
                final Action2<TestJSONClassFile,Integer> getSkippedTestCountTest = (TestJSONClassFile classFile, Integer expected) ->
                {
                    runner.test("with " + classFile.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, classFile.getSkippedTestCount());
                    });
                };

                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", "b")),
                    null);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create())),
                    null);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNull("skippedTestCount"))),
                    null);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("skippedTestCount", ""))),
                    null);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("skippedTestCount", "hello"))),
                    null);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("skippedTestCount", 5))),
                    5);
                getSkippedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("skippedTestCount", 6.0))),
                    null);
            });

            runner.testGroup("setSkippedTestCount(int)", () ->
            {
                runner.test("with non-object JSON value", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    final TestJSONClassFile setSkippedTestCountResult = classFile.setSkippedTestCount(5);
                    test.assertSame(classFile, setSkippedTestCountResult);
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                    test.assertNull(classFile.getSkippedTestCount());
                });

                final Action2<TestJSONClassFile,Integer> setSkippedTestCountTest = (TestJSONClassFile classFile, Integer skippedTestCount) ->
                {
                    runner.test("with " + English.andList(classFile, skippedTestCount), (Test test) ->
                    {
                        final TestJSONClassFile setSkippedTestCountResult = classFile.setSkippedTestCount(skippedTestCount);
                        test.assertSame(classFile, setSkippedTestCountResult);
                        test.assertEqual(skippedTestCount, classFile.getSkippedTestCount());
                    });
                };

                setSkippedTestCountTest.run(
                    TestJSONClassFile.create("a"),
                    10);
                setSkippedTestCountTest.run(
                    TestJSONClassFile.create("a")
                        .setSkippedTestCount(15),
                    30);
            });

            runner.testGroup("getFailedTestCount()", () ->
            {
                final Action2<TestJSONClassFile,Integer> getFailedTestCountTest = (TestJSONClassFile classFile, Integer expected) ->
                {
                    runner.test("with " + classFile.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, classFile.getFailedTestCount());
                    });
                };

                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", "b")),
                    null);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create())),
                    null);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNull("failedTestCount"))),
                    null);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("failedTestCount", ""))),
                    null);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setString("failedTestCount", "hello"))),
                    null);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("failedTestCount", 5))),
                    5);
                getFailedTestCountTest.run(
                    TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()
                        .setNumber("failedTestCount", 6.0))),
                    null);
            });

            runner.testGroup("setFailedTestCount(int)", () ->
            {
                runner.test("with non-object JSON value", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    final TestJSONClassFile setFailedTestCountResult = classFile.setFailedTestCount(5);
                    test.assertSame(classFile, setFailedTestCountResult);
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                    test.assertNull(classFile.getFailedTestCount());
                });

                final Action2<TestJSONClassFile,Integer> setFailedTestCountTest = (TestJSONClassFile classFile, Integer failedTestCount) ->
                {
                    runner.test("with " + English.andList(classFile, failedTestCount), (Test test) ->
                    {
                        final TestJSONClassFile setFailedTestCountResult = classFile.setFailedTestCount(failedTestCount);
                        test.assertSame(classFile, setFailedTestCountResult);
                        test.assertEqual(failedTestCount, classFile.getFailedTestCount());
                    });
                };

                setFailedTestCountTest.run(
                    TestJSONClassFile.create("a"),
                    10);
                setFailedTestCountTest.run(
                    TestJSONClassFile.create("a")
                        .setFailedTestCount(15),
                    30);
            });
        });
    }
}
