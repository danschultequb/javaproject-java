package qub;

/**
 * An issue that occurred while compiling Java source code.
 */
public class JavacIssue extends JSONObjectWrapperBase
{
    private static final String sourceFilePathPropertyName = "sourceFilePath";
    private static final String lineNumberPropertyName = "lineNumber";
    private static final String columnNumberPropertyName = "columnNumber";
    private static final String typePropertyName = "type";
    private static final String messagePropertyName = "message";

    private JavacIssue(JSONObject json)
    {
        super(json);
    }

    public static JavacIssue create()
    {
        return JavacIssue.create(JSONObject.create());
    }

    public static JavacIssue create(JSONObject json)
    {
        return new JavacIssue(json);
    }

    public static Iterator<JavacIssue> parseIssues(Iterator<String> lines)
    {
        PreCondition.assertNotNull(lines, "lines");

        return Iterator.create((IteratorActions<JavacIssue> actions) ->
        {
            lines.start();

            while (lines.hasCurrent())
            {
                final String errorLine = lines.takeCurrent();
                final int firstColon = errorLine.indexOf(':');
                if (firstColon >= 0)
                {
                    final JavacIssue issue = JavacIssue.create();

                    final String sourceFilePathString = errorLine.substring(0, firstColon);
                    issue.setSourceFilePath(Path.parse(sourceFilePathString).normalize());

                    final int secondColon = errorLine.indexOf(':', firstColon + 1);
                    if (secondColon >= 0)
                    {
                        final String lineNumberString = errorLine.substring(firstColon + 1, secondColon);
                        final Integer lineNumber = Integers.parse(lineNumberString)
                            .catchError(NumberFormatException.class)
                            .await();
                        if (lineNumber != null)
                        {
                            issue.setLineNumber(lineNumber);

                            final int thirdColon = errorLine.indexOf(':', secondColon + 1);

                            final String issueType = errorLine.substring(secondColon + 1, thirdColon).trim();
                            issue.setType(issueType);

                            final String message = errorLine.substring(thirdColon + 1).trim();
                            issue.setMessage(message);

                            if (lines.hasCurrent())
                            {
                                // Take source code line.
                                lines.takeCurrent();

                                if (lines.hasCurrent())
                                {
                                    final String caretLine = lines.takeCurrent();
                                    final int caretIndex = caretLine.indexOf('^');
                                    final int columnNumber = caretIndex + 1;
                                    issue.setColumnNumber(columnNumber);

                                    actions.returnValue(issue);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

//    public static Result<Iterable<JavacIssue>> parseIssues(CharacterReadStream readStream)
//    {
//        PreCondition.assertNotNull(readStream, "readStream");
//
//        return Result.create(() ->
//        {
//            final List<JavacIssue> result = List.create();
//
//            final JavacIssue issue = JavacIssue.create();
//
//            final String errorLine = readNextLine.run();
//            if (!Strings.isNullOrEmpty(errorLine))
//            {
//                final int firstColon = errorLine.indexOf(':');
//                if (firstColon >= 0)
//                {
//                    final String sourceFilePathString = errorLine.substring(0, firstColon);
//                    issue.setSourceFilePath(Path.parse(sourceFilePathString).normalize());
//
//                    final int secondColon = errorLine.indexOf(':', firstColon + 1);
//                    if (secondColon >= 0)
//                    {
//                        final String lineNumberString = errorLine.substring(firstColon + 1, secondColon);
//                        final Integer lineNumber = Integers.parse(lineNumberString)
//                            .catchError(NumberFormatException.class)
//                            .await();
//                        if (lineNumber != null)
//                        {
//                            issue.setLineNumber(lineNumber);
//
//                            final int thirdColon = errorLine.indexOf(':', secondColon + 1);
//
//                            final String issueType = errorLine.substring(secondColon + 1, thirdColon).trim();
//                            issue.setType(issueType);
//
//                            final String message = errorLine.substring(thirdColon + 1).trim();
//                            issue.setMessage(message);
//
//                            final String sourceCodeLine = javacErrorStream.readLine().catchError().await();
//                            if (sourceCodeLine != null)
//                            {
//                                final String caretLine = javacErrorStream.readLine().catchError().await();
//                                if (!Strings.isNullOrEmpty(caretLine))
//                                {
//                                    final int caretIndex = caretLine.indexOf('^');
//                                    final int columnNumber = caretIndex + 1;
//                                    issue.setColumnNumber(columnNumber);
//
//                                    actions.returnValue(issue);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            return result;
//        });
//    }

    public Path getSourceFilePath()
    {
        return this.toJson().getString(JavacIssue.sourceFilePathPropertyName)
            .then(Path::parse)
            .catchError()
            .await();
    }

    public JavacIssue setSourceFilePath(String sourceFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(sourceFilePath, "sourceFilePath");

        return this.setSourceFilePath(Path.parse(sourceFilePath));
    }

    public JavacIssue setSourceFilePath(Path sourceFilePath)
    {
        PreCondition.assertNotNull(sourceFilePath, "sourceFilePath");

        this.toJson().setString(JavacIssue.sourceFilePathPropertyName, sourceFilePath.toString());

        return this;
    }

    public Integer getLineNumber()
    {
        return this.toJson().getInteger(JavacIssue.lineNumberPropertyName)
            .catchError()
            .await();
    }

    public JavacIssue setLineNumber(int lineNumber)
    {
        PreCondition.assertGreaterThanOrEqualTo(lineNumber, 0, "lineNumber");

        this.toJson().setNumber(JavacIssue.lineNumberPropertyName, lineNumber);

        return this;
    }

    public Integer getColumnNumber()
    {
        return this.toJson().getInteger(JavacIssue.columnNumberPropertyName)
            .catchError()
            .await();
    }

    public JavacIssue setColumnNumber(int columnNumber)
    {
        PreCondition.assertGreaterThanOrEqualTo(columnNumber, 0, "columnNumber");

        this.toJson().setNumber(JavacIssue.columnNumberPropertyName, columnNumber);

        return this;
    }

    public String getType()
    {
        return this.toJson().getString(JavacIssue.typePropertyName)
            .catchError()
            .await();
    }

    public JavacIssue setType(String type)
    {
        PreCondition.assertNotNullAndNotEmpty(type, "type");

        this.toJson().setString(JavacIssue.typePropertyName, type);

        return this;
    }

    public String getMessage()
    {
        return this.toJson().getString(JavacIssue.messagePropertyName)
            .catchError()
            .await();
    }

    public JavacIssue setMessage(String message)
    {
        PreCondition.assertNotNullAndNotEmpty(message, "message");

        this.toJson().setString(JavacIssue.messagePropertyName, message);

        return this;
    }
}
