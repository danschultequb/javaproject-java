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
