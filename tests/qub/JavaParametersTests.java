package qub;

public interface JavaParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaParameters.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JavaParameters parameters = JavaParameters.create();
                test.assertNotNull(parameters);
                test.assertEqual("java", parameters.getExecutablePath().toString());
                test.assertEqual(Iterable.create(), parameters.getArguments());
                test.assertNull(parameters.getWorkingFolderPath());
                test.assertNull(parameters.getOutputStreamHandler());
                test.assertNull(parameters.getErrorStreamHandler());
            });

            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String executablePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        test.assertThrows(() -> JavaParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("executablePath cannot be empty."));

                final Action1<String> createTest = (String executablePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create(executablePath);
                        test.assertNotNull(parameters);
                        test.assertEqual(executablePath, parameters.getExecutablePath().toString());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run("java");
                createTest.run("java.exe");
                createTest.run("my-java");
                createTest.run("relative/path/to/java.exe");
            });

            runner.testGroup("create(Path)", () ->
            {
                final Action2<Path,Throwable> createErrorTest = (Path executablePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        test.assertThrows(() -> JavaParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));

                final Action1<Path> createTest = (Path executablePathString) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePathString), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create(executablePathString);
                        test.assertNotNull(parameters);
                        test.assertEqual(executablePathString, parameters.getExecutablePath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run(Path.parse("java"));
                createTest.run(Path.parse("java.exe"));
                createTest.run(Path.parse("my-java"));
                createTest.run(Path.parse("relative/path/to/java.exe"));
            });

            runner.testGroup("create(File)",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (FakeDesktopProcess process) ->
            {
                final Folder currentFolder = process.getCurrentFolder();

                final Action2<File,Throwable> createErrorTest = (File executableFile, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executableFile), (Test test) ->
                    {
                        test.assertThrows(() -> JavaParameters.create(executableFile),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executable cannot be null."));

                final Action1<File> createTest = (File executableFile) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executableFile), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create(executableFile);
                        test.assertNotNull(parameters);
                        test.assertEqual(executableFile.getPath(), parameters.getExecutablePath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run(currentFolder.getFile("java").await());
                createTest.run(currentFolder.getFile("java.exe").await());
                createTest.run(currentFolder.getFile("my-java").await());
                createTest.run(currentFolder.getFile("relative/path/to/java.exe").await());
            });

            runner.testGroup("addClasspath(String)", () ->
            {
                final Action2<String,Throwable> addClasspathErrorTest = (String classpath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        test.assertThrows(() -> parameters.addClasspath(classpath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addClasspathErrorTest.run(null, new PreConditionFailure("classpath cannot be null."));
                addClasspathErrorTest.run("", new PreConditionFailure("classpath cannot be empty."));

                final Action1<String> addClasspathTest = (String classpath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addClasspathResult = parameters.addClasspath(classpath);
                        test.assertSame(parameters, addClasspathResult);
                        test.assertEqual(Iterable.create("-classpath", classpath), parameters.getArguments());
                    });
                };

                addClasspathTest.run("hello");
                addClasspathTest.run("a;b;c");
            });

            runner.testGroup("addClasspath(Iterator<String>)", () ->
            {
                final Action2<Iterable<String>,Throwable> addClasspathErrorTest = (Iterable<String> classpath, Throwable expected) ->
                {
                    runner.test("with " + classpath, (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        test.assertThrows(() -> parameters.addClasspath(classpath == null ? null : classpath.iterate()),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addClasspathErrorTest.run(null, new PreConditionFailure("classpath cannot be null."));
                addClasspathErrorTest.run(Iterable.create(), new PreConditionFailure("classpath cannot be empty."));

                final Action1<Iterable<String>> addClasspathTest = (Iterable<String> classpath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addClasspathResult = parameters.addClasspath(classpath.iterate());
                        test.assertSame(parameters, addClasspathResult);
                        test.assertEqual(Iterable.create("-classpath", Strings.join(';', classpath)), parameters.getArguments());
                    });
                };

                addClasspathTest.run(Iterable.create("hello"));
                addClasspathTest.run(Iterable.create("a", "b", "c"));
            });

            runner.testGroup("addClasspath(Iterable<String>)", () ->
            {
                final Action2<Iterable<String>,Throwable> addClasspathErrorTest = (Iterable<String> classpath, Throwable expected) ->
                {
                    runner.test("with " + classpath, (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        test.assertThrows(() -> parameters.addClasspath(classpath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addClasspathErrorTest.run(null, new PreConditionFailure("classpath cannot be null."));
                addClasspathErrorTest.run(Iterable.create(), new PreConditionFailure("classpath cannot be empty."));

                final Action1<Iterable<String>> addClasspathTest = (Iterable<String> classpath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addClasspathResult = parameters.addClasspath(classpath);
                        test.assertSame(parameters, addClasspathResult);
                        test.assertEqual(Iterable.create("-classpath", Strings.join(';', classpath)), parameters.getArguments());
                    });
                };

                addClasspathTest.run(Iterable.create("hello"));
                addClasspathTest.run(Iterable.create("a", "b", "c"));
            });

            runner.testGroup("addJavaAgent(String)", () ->
            {
                final Action2<String,Throwable> addJavaAgentErrorTest = (String javaAgent, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javaAgent), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        test.assertThrows(() -> parameters.addJavaAgent(javaAgent),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addJavaAgentErrorTest.run(null, new PreConditionFailure("javaAgent cannot be null."));
                addJavaAgentErrorTest.run("", new PreConditionFailure("javaAgent cannot be empty."));

                final Action1<String> addJavaAgentTest = (String javaAgent) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javaAgent), (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addJavaAgentResult = parameters.addJavaAgent(javaAgent);
                        test.assertSame(parameters, addJavaAgentResult);
                        test.assertEqual(Iterable.create("-javaagent:" + javaAgent), parameters.getArguments());
                    });
                };

                addJavaAgentTest.run("fake-java-agent");
            });

            runner.test("addVersion()", (Test test) ->
            {
                final JavaParameters parameters = JavaParameters.create();
                final JavaParameters addVersionResult = parameters.addVersion();
                test.assertSame(parameters, addVersionResult);
                test.assertEqual(Iterable.create("--version"), parameters.getArguments());
            });

            runner.testGroup("addVersion(JavaVersionDestination)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaParameters parameters = JavaParameters.create();
                    test.assertThrows(() -> parameters.addVersion(null),
                        new PreConditionFailure("javaVersionDestination cannot be null."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                final Action2<JavaVersionDestination,String> addVersionTest = (JavaVersionDestination javaVersionDestination, String expected) ->
                {
                    runner.test("with " + javaVersionDestination, (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addVersionResult = parameters.addVersion(javaVersionDestination);
                        test.assertSame(parameters, addVersionResult);
                        test.assertEqual(Iterable.create(expected), parameters.getArguments());
                    });
                };

                addVersionTest.run(JavaVersionDestination.StandardOutput, "--version");
                addVersionTest.run(JavaVersionDestination.StandardError, "-version");
            });

            runner.test("addShowVersion()", (Test test) ->
            {
                final JavaParameters parameters = JavaParameters.create();
                final JavaParameters addShowVersionResult = parameters.addShowVersion();
                test.assertSame(parameters, addShowVersionResult);
                test.assertEqual(Iterable.create("--show-version"), parameters.getArguments());
            });

            runner.testGroup("addShowVersion(JavaVersionDestination)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaParameters parameters = JavaParameters.create();
                    test.assertThrows(() -> parameters.addShowVersion(null),
                        new PreConditionFailure("javaVersionDestination cannot be null."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                final Action2<JavaVersionDestination,String> addShowVersionTest = (JavaVersionDestination javaVersionDestination, String expected) ->
                {
                    runner.test("with " + javaVersionDestination, (Test test) ->
                    {
                        final JavaParameters parameters = JavaParameters.create();
                        final JavaParameters addShowVersionResult = parameters.addShowVersion(javaVersionDestination);
                        test.assertSame(parameters, addShowVersionResult);
                        test.assertEqual(Iterable.create(expected), parameters.getArguments());
                    });
                };

                addShowVersionTest.run(JavaVersionDestination.StandardOutput, "--show-version");
                addShowVersionTest.run(JavaVersionDestination.StandardError, "-showversion");
            });
        });
    }
}
