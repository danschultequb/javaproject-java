package qub;

public class Jar extends ChildProcessRunnerWrapper<Jar,JarParameters>
{
    private Jar(ChildProcessRunner childProcessRunner)
    {
        super(childProcessRunner, JarParameters::create, "jar");
    }

    public static Jar create(ChildProcessRunner childProcessRunner)
    {
        return new Jar(childProcessRunner);
    }

    public Result<VersionNumber> version()
    {
        return Result.create(() ->
        {
            final InMemoryCharacterToByteStream outputStream = InMemoryCharacterToByteStream.create();
            this.run((JarParameters parameters) ->
            {
                parameters.addVersion();
                parameters.redirectOutputTo(outputStream);
            }).await();
            final String outputText = outputStream.getText().await();
            final String trimmedOutputText = outputText.trim();
            final String versionString = trimmedOutputText.substring("jar ".length());
            return VersionNumber.parse(versionString).await();
        });
    }
}
