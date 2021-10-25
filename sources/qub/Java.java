package qub;

public class Java extends ChildProcessRunnerWrapper<Java,JavaParameters>
{
    private Java(ChildProcessRunner childProcessRunner)
    {
        super(childProcessRunner, JavaParameters::create, "java");
    }

    public static Java create(ChildProcessRunner childProcessRunner)
    {
        return new Java(childProcessRunner);
    }
}
