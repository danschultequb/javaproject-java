package qub;

public class VerboseChildProcessRunner implements ChildProcessRunner
{
    private final ChildProcessRunner innerChildProcessRunner;
    private final CharacterWriteStream writeStream;

    private VerboseChildProcessRunner(ChildProcessRunner innerChildProcessRunner, CharacterWriteStream writeStream)
    {
        PreCondition.assertNotNull(innerChildProcessRunner, "innerChildProcessRunner");
        PreCondition.assertNotNull(writeStream, "writeStream");

        this.innerChildProcessRunner = innerChildProcessRunner;
        this.writeStream = writeStream;
    }

    public static VerboseChildProcessRunner create(DesktopProcess process, CharacterWriteStream writeStream)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(writeStream, "writeStream");

        return VerboseChildProcessRunner.create(process.getChildProcessRunner(), writeStream);
    }

    public static VerboseChildProcessRunner create(ChildProcessRunner innerChildProcessRunner, CharacterWriteStream writeStream)
    {
        PreCondition.assertNotNull(innerChildProcessRunner, "innerChildProcessRunner");
        PreCondition.assertNotNull(writeStream, "writeStream");

        return new VerboseChildProcessRunner(innerChildProcessRunner, writeStream);
    }

    @Override
    public Result<? extends ChildProcess> start(ChildProcessParameters parameters)
    {
        return Result.create(() ->
        {
            final Path workingFolderPath = parameters.getWorkingFolderPath();
            if (workingFolderPath != null)
            {
                writeStream.write(workingFolderPath.toString()).await();
                writeStream.write(": ").await();
            }

            writeStream.write(parameters.getExecutablePath().toString()).await();

            final Iterable<String> arguments = parameters.getArguments();
            if (!Iterable.isNullOrEmpty(arguments))
            {
                for (final String argument : parameters.getArguments())
                {
                    writeStream.write(" ").await();
                    writeStream.write(argument).await();
                }
            }
            writeStream.writeLine().await();

            return this.innerChildProcessRunner.start(parameters).await();
        });
    }
}
