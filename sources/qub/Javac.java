package qub;

public class Javac extends ChildProcessRunnerWrapper<Javac,JavacParameters>
{
    private Javac(ChildProcessRunner childProcessRunner)
    {
        super(childProcessRunner, JavacParameters::create, "javac");
    }

    public static Javac create(ChildProcessRunner childProcessRunner)
    {
        return new Javac(childProcessRunner);
    }

    public Result<VersionNumber> version()
    {
        return Result.create(() ->
        {
            final InMemoryCharacterToByteStream outputStream = InMemoryCharacterToByteStream.create();
            this.run((JavacParameters parameters) ->
            {
                parameters.addArgument("--version");
                parameters.redirectOutputTo(outputStream);
            }).await();
            final String outputText = outputStream.getText().await();
            final String trimmedOutputText = outputText.trim();
            final String versionString = trimmedOutputText.substring("javac ".length());
            return VersionNumber.parse(versionString).await();
        });
    }

    public Result<JavacResult> compile(Action1<JavacParameters> parametersSetup)
    {
        PreCondition.assertNotNull(parametersSetup, "parametersSetup");

        return Result.create(() ->
        {
            final JavacResult result = JavacResult.create();

            final InMemoryCharacterToByteStream errorStream = InMemoryCharacterToByteStream.create();
            result.setExitCode(this.run((JavacParameters parameters) ->
            {
                parametersSetup.run(parameters);
                parameters.redirectErrorTo(errorStream);
            }).await());

            errorStream.endOfStream();

            final Iterator<String> errorLines = Strings.iterateLines(CharacterReadStream.iterate(errorStream)).start();
            while (errorLines.hasCurrent())
            {
                final JavacIssue issue = JavacIssue.create();

                final String errorLine = errorLines.takeCurrent();
                final int firstColon = errorLine.indexOf(':');
                if (firstColon >= 0)
                {
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

                            if (errorLines.hasCurrent())
                            {
                                // Take source code line.
                                errorLines.takeCurrent();

                                if (errorLines.hasCurrent())
                                {
                                    final String caretLine = errorLines.takeCurrent();
                                    final int caretIndex = caretLine.indexOf('^');
                                    final int columnNumber = caretIndex + 1;
                                    issue.setColumnNumber(columnNumber);

                                    result.addIssue(issue);
                                }
                            }
                        }
                    }
                }
            }

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }
}
