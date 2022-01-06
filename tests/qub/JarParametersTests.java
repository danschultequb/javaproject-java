package qub;

public interface JarParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JarParameters.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JarParameters parameters = JarParameters.create();
                test.assertNotNull(parameters);
                test.assertEqual(Path.parse("jar"), parameters.getExecutablePath());
                test.assertNull(parameters.getWorkingFolderPath());
                test.assertEqual(Iterable.create(), parameters.getArguments());
                test.assertNull(parameters.getInputStream());
                test.assertNull(parameters.getOutputStreamHandler());
                test.assertNull(parameters.getErrorStreamHandler());
            });

            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String executablePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        test.assertThrows(() -> JarParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("executablePath cannot be empty."));

                final Action1<String> createTest = (String executablePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create(executablePath);
                        test.assertNotNull(parameters);
                        test.assertEqual(Path.parse(executablePath), parameters.getExecutablePath());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getInputStream());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run("jar");
                createTest.run("bin/jar");
                createTest.run("/bin/jar");
            });

            runner.testGroup("create(Path)", () ->
            {
                final Action2<Path,Throwable> createErrorTest = (Path executablePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        test.assertThrows(() -> JarParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));

                final Action1<Path> createTest = (Path executablePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create(executablePath);
                        test.assertNotNull(parameters);
                        test.assertEqual(executablePath, parameters.getExecutablePath());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getInputStream());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run(Path.parse("jar"));
                createTest.run(Path.parse("bin/jar"));
                createTest.run(Path.parse("/bin/jar"));
            });

            runner.testGroup("create(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JarParameters.create((File)null),
                        new PreConditionFailure("executable cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    final File file = fileSystem.getFile("/bin/jar").await();
                    final JarParameters parameters = JarParameters.create(file);
                    test.assertNotNull(parameters);
                    test.assertEqual(file.getPath(), parameters.getExecutablePath());
                    test.assertNull(parameters.getWorkingFolderPath());
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                    test.assertNull(parameters.getInputStream());
                    test.assertNull(parameters.getOutputStreamHandler());
                    test.assertNull(parameters.getErrorStreamHandler());
                });
            });

            runner.test("addVersion()", (Test test) ->
            {
                final JarParameters parameters = JarParameters.create();
                final JarParameters addVersionResult = parameters.addVersion();
                test.assertSame(parameters, addVersionResult);
                test.assertEqual(Iterable.create("--version"), parameters.getArguments());
            });
        });
    }
}
