package qub;

public class JavacResult
{
    private int exitCode;
    private final List<JavacIssue> issues;

    private JavacResult()
    {
        this.issues = List.create();
    }

    public static JavacResult create()
    {
        return new JavacResult();
    }

    public int getExitCode()
    {
        return this.exitCode;
    }

    public JavacResult setExitCode(int exitCode)
    {
        this.exitCode = exitCode;

        return this;
    }

    public Iterable<JavacIssue> getIssues()
    {
        return this.issues;
    }

    public JavacResult addIssue(JavacIssue issue)
    {
        PreCondition.assertNotNull(issue, "issue");

        this.issues.add(issue);

        return this;
    }
}
