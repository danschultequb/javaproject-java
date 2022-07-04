package qub;

public interface JavacIssueTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(JavacIssue.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JavacIssue issue = JavacIssue.create();
                test.assertNotNull(issue);
                test.assertNull(issue.getSourceFilePath());
                test.assertNull(issue.getLineNumber());
                test.assertNull(issue.getColumnNumber());
                test.assertNull(issue.getType());
                test.assertNull(issue.getMessage());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavacIssue.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action6<JSONObject,Path,Integer,Integer,String,String> createTest = (JSONObject json, Path expectedSourceFilePath, Integer expectedLineNumber, Integer expectedColumnNumber, String expectedType, String expectedMessage) ->
                {
                    runner.test("with " + json.toString(), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create(json);
                        test.assertNotNull(issue);
                        test.assertEqual(expectedSourceFilePath, issue.getSourceFilePath());
                        test.assertEqual(expectedLineNumber, issue.getLineNumber());
                        test.assertEqual(expectedColumnNumber, issue.getColumnNumber());
                        test.assertEqual(expectedType, issue.getType());
                        test.assertEqual(expectedMessage, issue.getMessage());
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    null,
                    null,
                    null,
                    null,
                    null);
                createTest.run(
                    JSONObject.create()
                        .setString("a", "b"),
                    null,
                    null,
                    null,
                    null,
                    null);
                createTest.run(
                    JSONObject.create()
                        .setString("sourceFilePath", "b"),
                    Path.parse("b"),
                    null,
                    null,
                    null,
                    null);
                createTest.run(
                    JSONObject.create()
                        .setString("sourceFilePath", "a")
                        .setNumber("lineNumber", 1)
                        .setNumber("columnNumber", 2)
                        .setString("type", "b")
                        .setString("message", "c"),
                    Path.parse("a"),
                    1,
                    2,
                    "b",
                    "c");
            });

            runner.testGroup("parseIssues(Iterator<String>)", () ->
            {
                final Action3<String,Iterator<String>,Throwable> parseIssuesErrorTest = (String testName, Iterator<String> lines, Throwable expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        test.assertThrows(() -> JavacIssue.parseIssues(lines).await(),
                            expected);
                    });
                };

                parseIssuesErrorTest.run("with null",
                    null,
                    new PreConditionFailure("lines cannot be null."));

                final Action3<String,Iterator<String>,Iterable<JavacIssue>> parseIssuesTest = (String testName, Iterator<String> lines, Iterable<JavacIssue> expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final Iterator<JavacIssue> issues = JavacIssue.parseIssues(lines);
                        test.assertNotNull(issues);
                        test.assertEqual(expected, issues.toList());
                    });
                };

                parseIssuesTest.run("with no lines",
                    Iterator.create(),
                    Iterable.create());

                parseIssuesTest.run("with \"class is public, should be declared in a file named\"",
                    Iterator.create(
                        "sources\\Empty.java:1: error: class Class1 is public, should be declared in a file named Class1.java",
                        "public class Class1",
                        "       ^",
                        "1 error"),
                    Iterable.create(
                        JavacIssue.create()
                            .setSourceFilePath("sources/Empty.java")
                            .setLineNumber(1)
                            .setType("error")
                            .setMessage("class Class1 is public, should be declared in a file named Class1.java")
                            .setColumnNumber(8)));

                parseIssuesTest.run("with basic text java file",
                    Iterator.create(
                        "sources\\Empty.java:1: error: class, interface, enum, or record expected",
                        "Hello there. I'm not a java file.",
                        "^",
                        "sources\\Empty.java:1: error: unclosed character literal",
                        "Hello there. I'm not a java file.",
                        "              ^",
                        "2 errors"),
                    Iterable.create(
                        JavacIssue.create()
                            .setSourceFilePath("sources/Empty.java")
                            .setLineNumber(1)
                            .setType("error")
                            .setMessage("class, interface, enum, or record expected")
                            .setColumnNumber(1),
                        JavacIssue.create()
                            .setSourceFilePath("sources/Empty.java")
                            .setLineNumber(1)
                            .setType("error")
                            .setMessage("unclosed character literal")
                            .setColumnNumber(15)));

                parseIssuesTest.run("with generic deprecation issues",
                    Iterator.create(
                        "sources\\qub\\JavaProjectFolder.java:192: warning: [deprecation] <TError>catchError(Class<TError>) in Iterator has been deprecated",
                        "            .catchError(NotFoundException.class)",
                        "            ^",
                        "  where TError,T are type-variables:",
                        "    TError extends Throwable declared in method <TError>catchError(Class<TError>)",
                        "    T extends Object declared in interface Iterator",
                        "sources\\qub\\JavaProjectFolder.java:458: warning: [deprecation] <TError>catchError(Class<TError>) in Iterator has been deprecated",
                        "            .catchError(NotFoundException.class)",
                        "            ^",
                        "  where TError,T are type-variables:",
                        "    TError extends Throwable declared in method <TError>catchError(Class<TError>)",
                        "    T extends Object declared in interface Iterator",
                        "2 warnings"),
                    Iterable.create(
                        JavacIssue.create()
                            .setSourceFilePath("sources/qub/JavaProjectFolder.java")
                            .setLineNumber(192)
                            .setType("warning")
                            .setMessage("[deprecation] <TError>catchError(Class<TError>) in Iterator has been deprecated")
                            .setColumnNumber(13),
                        JavacIssue.create()
                            .setSourceFilePath("sources/qub/JavaProjectFolder.java")
                            .setLineNumber(458)
                            .setType("warning")
                            .setMessage("[deprecation] <TError>catchError(Class<TError>) in Iterator has been deprecated")
                            .setColumnNumber(13)));
            });

            runner.testGroup("setSourceFilePath(String)", () ->
            {
                final Action2<String,Throwable> setSourceFilePathErrorTest = (String sourceFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFilePath), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setSourceFilePath(sourceFilePath),
                            expected);
                        test.assertNull(issue.getSourceFilePath());
                    });
                };

                setSourceFilePathErrorTest.run(null, new PreConditionFailure("sourceFilePath cannot be null."));
                setSourceFilePathErrorTest.run("", new PreConditionFailure("sourceFilePath cannot be empty."));

                final Action1<String> setSourceFilePathTest = (String sourceFilePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFilePath), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setSourceFilePathResult = issue.setSourceFilePath(sourceFilePath);
                        test.assertSame(issue, setSourceFilePathResult);
                        test.assertEqual(Path.parse(sourceFilePath), issue.getSourceFilePath());
                    });
                };

                setSourceFilePathTest.run("a");
                setSourceFilePathTest.run("a/b");
                setSourceFilePathTest.run("a\\b");
                setSourceFilePathTest.run("/a");
                setSourceFilePathTest.run("/a/b");
                setSourceFilePathTest.run("\\a\\b");
            });

            runner.testGroup("setSourceFilePath(Path)", () ->
            {
                final Action2<Path,Throwable> setSourceFilePathErrorTest = (Path sourceFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFilePath), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setSourceFilePath(sourceFilePath),
                            expected);
                        test.assertNull(issue.getSourceFilePath());
                    });
                };

                setSourceFilePathErrorTest.run(null, new PreConditionFailure("sourceFilePath cannot be null."));

                final Action1<Path> setSourceFilePathTest = (Path sourceFilePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFilePath), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setSourceFilePathResult = issue.setSourceFilePath(sourceFilePath);
                        test.assertSame(issue, setSourceFilePathResult);
                        test.assertEqual(sourceFilePath, issue.getSourceFilePath());
                    });
                };

                setSourceFilePathTest.run(Path.parse("a"));
                setSourceFilePathTest.run(Path.parse("a/b"));
                setSourceFilePathTest.run(Path.parse("a\\b"));
                setSourceFilePathTest.run(Path.parse("/a"));
                setSourceFilePathTest.run(Path.parse("/a/b"));
                setSourceFilePathTest.run(Path.parse("\\a\\b"));
            });

            runner.testGroup("setLineNumber(int)", () ->
            {
                final Action2<Integer,Throwable> setLineNumberErrorTest = (Integer lineNumber, Throwable expected) ->
                {
                    runner.test("with " + lineNumber, (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setLineNumber(lineNumber),
                            expected);
                        test.assertNull(issue.getLineNumber());
                    });
                };

                setLineNumberErrorTest.run(-2, new PreConditionFailure("lineNumber (-2) must be greater than or equal to 0."));
                setLineNumberErrorTest.run(-1, new PreConditionFailure("lineNumber (-1) must be greater than or equal to 0."));

                final Action1<Integer> setLineNumberTest = (Integer lineNumber) ->
                {
                    runner.test("with " + lineNumber, (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setLineNumberResult = issue.setLineNumber(lineNumber);
                        test.assertSame(issue, setLineNumberResult);
                        test.assertEqual(lineNumber, issue.getLineNumber());
                    });
                };

                setLineNumberTest.run(0);
                setLineNumberTest.run(1);
                setLineNumberTest.run(2);
            });

            runner.testGroup("setColumnNumber(int)", () ->
            {
                final Action2<Integer,Throwable> setColumnNumberErrorTest = (Integer columnNumber, Throwable expected) ->
                {
                    runner.test("with " + columnNumber, (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setColumnNumber(columnNumber),
                            expected);
                        test.assertNull(issue.getColumnNumber());
                    });
                };

                setColumnNumberErrorTest.run(-2, new PreConditionFailure("columnNumber (-2) must be greater than or equal to 0."));
                setColumnNumberErrorTest.run(-1, new PreConditionFailure("columnNumber (-1) must be greater than or equal to 0."));

                final Action1<Integer> setColumnNumberTest = (Integer columnNumber) ->
                {
                    runner.test("with " + columnNumber, (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setColumnNumberResult = issue.setColumnNumber(columnNumber);
                        test.assertSame(issue, setColumnNumberResult);
                        test.assertEqual(columnNumber, issue.getColumnNumber());
                    });
                };

                setColumnNumberTest.run(0);
                setColumnNumberTest.run(1);
                setColumnNumberTest.run(2);
            });

            runner.testGroup("setType(String)", () ->
            {
                final Action2<String,Throwable> setTypeErrorTest = (String type, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(type), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setType(type),
                            expected);
                        test.assertNull(issue.getType());
                    });
                };

                setTypeErrorTest.run(null, new PreConditionFailure("type cannot be null."));
                setTypeErrorTest.run("", new PreConditionFailure("type cannot be empty."));

                final Action1<String> setTypeTest = (String type) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(type), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setTypeResult = issue.setType(type);
                        test.assertSame(issue, setTypeResult);
                        test.assertEqual(type, issue.getType());
                    });
                };

                setTypeTest.run("warning");
                setTypeTest.run("error");
                setTypeTest.run("hello");
                setTypeTest.run("hello there");
            });

            runner.testGroup("setMessage(String)", () ->
            {
                final Action2<String,Throwable> setMessageErrorTest = (String message, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(message), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        test.assertThrows(() -> issue.setMessage(message),
                            expected);
                        test.assertNull(issue.getMessage());
                    });
                };

                setMessageErrorTest.run(null, new PreConditionFailure("message cannot be null."));
                setMessageErrorTest.run("", new PreConditionFailure("message cannot be empty."));

                final Action1<String> setMessageTest = (String message) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(message), (Test test) ->
                    {
                        final JavacIssue issue = JavacIssue.create();
                        final JavacIssue setMessageResult = issue.setMessage(message);
                        test.assertSame(issue, setMessageResult);
                        test.assertEqual(message, issue.getMessage());
                    });
                };

                setMessageTest.run("warning");
                setMessageTest.run("error");
                setMessageTest.run("hello");
                setMessageTest.run("hello there");
            });
        });
    }
}
