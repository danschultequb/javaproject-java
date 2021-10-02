package qub;

public interface JavacParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavacParameters.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JavacParameters parameters = JavacParameters.create();
                test.assertNotNull(parameters);
                test.assertEqual(Path.parse("javac"), parameters.getExecutablePath());
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
                        test.assertThrows(() -> JavacParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("executablePath cannot be empty."));

                final Action1<String> createTest = (String executablePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create(executablePath);
                        test.assertNotNull(parameters);
                        test.assertEqual(Path.parse(executablePath), parameters.getExecutablePath());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getInputStream());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run("javac");
                createTest.run("bin/javac");
                createTest.run("/bin/javac");
            });

            runner.testGroup("create(Path)", () ->
            {
                final Action2<Path,Throwable> createErrorTest = (Path executablePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        test.assertThrows(() -> JavacParameters.create(executablePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("executablePath cannot be null."));

                final Action1<Path> createTest = (Path executablePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(executablePath), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create(executablePath);
                        test.assertNotNull(parameters);
                        test.assertEqual(executablePath, parameters.getExecutablePath());
                        test.assertNull(parameters.getWorkingFolderPath());
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                        test.assertNull(parameters.getInputStream());
                        test.assertNull(parameters.getOutputStreamHandler());
                        test.assertNull(parameters.getErrorStreamHandler());
                    });
                };

                createTest.run(Path.parse("javac"));
                createTest.run(Path.parse("bin/javac"));
                createTest.run(Path.parse("/bin/javac"));
            });

            runner.testGroup("create(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavacParameters.create((File)null),
                        new PreConditionFailure("executable cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    final File file = fileSystem.getFile("/bin/javac").await();
                    final JavacParameters parameters = JavacParameters.create(file);
                    test.assertNotNull(parameters);
                    test.assertEqual(file.getPath(), parameters.getExecutablePath());
                    test.assertNull(parameters.getWorkingFolderPath());
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                    test.assertNull(parameters.getInputStream());
                    test.assertNull(parameters.getOutputStreamHandler());
                    test.assertNull(parameters.getErrorStreamHandler());
                });
            });

            runner.testGroup("addDirectory(String)", () ->
            {
                final Action2<String,Throwable> addDirectoryErrorTest = (String directory, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(directory), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        test.assertThrows(() -> parameters.addDirectory(directory),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addDirectoryErrorTest.run(null, new PreConditionFailure("directory cannot be null."));
                addDirectoryErrorTest.run("", new PreConditionFailure("directory cannot be empty."));

                final Action1<String> addDirectoryTest = (String directory) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(directory), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        final JavacParameters addDirectoryResult = parameters.addDirectory(directory);
                        test.assertSame(parameters, addDirectoryResult);
                        test.assertEqual(Iterable.create("-d", directory), parameters.getArguments());
                    });
                };

                addDirectoryTest.run("outputs");
                addDirectoryTest.run("outputs/");
                addDirectoryTest.run("outputs/folder");
                addDirectoryTest.run("/outputs/folder/");
            });

            runner.testGroup("addDirectory(Path)", () ->
            {
                final Action2<Path,Throwable> addDirectoryErrorTest = (Path directory, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(directory), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        test.assertThrows(() -> parameters.addDirectory(directory),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addDirectoryErrorTest.run(null, new PreConditionFailure("directory cannot be null."));

                final Action1<Path> addDirectoryTest = (Path directory) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(directory), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        final JavacParameters addDirectoryResult = parameters.addDirectory(directory);
                        test.assertSame(parameters, addDirectoryResult);
                        test.assertEqual(Iterable.create("-d", directory.toString()), parameters.getArguments());
                    });
                };

                addDirectoryTest.run(Path.parse("outputs"));
                addDirectoryTest.run(Path.parse("outputs/"));
                addDirectoryTest.run(Path.parse("outputs/folder"));
                addDirectoryTest.run(Path.parse("/outputs/folder/"));
            });

            runner.testGroup("addDirectory(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    test.assertThrows(() -> parameters.addDirectory((Folder)null),
                        new PreConditionFailure("directory cannot be null."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    final Folder folder = fileSystem.getFolder("/project/outputs/").await();
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addDirectoryResult = parameters.addDirectory(folder);
                    test.assertSame(parameters, addDirectoryResult);
                    test.assertEqual(Iterable.create("-d", folder.toString()), parameters.getArguments());
                });
            });

            runner.test("addVerbose()", (Test test) ->
            {
                final JavacParameters parameters = JavacParameters.create();
                final JavacParameters addVerboseResult = parameters.addVerbose();
                test.assertSame(parameters, addVerboseResult);
                test.assertEqual(Iterable.create("-verbose"), parameters.getArguments());
            });

            runner.test("addVersion()", (Test test) ->
            {
                final JavacParameters parameters = JavacParameters.create();
                final JavacParameters addVersionResult = parameters.addVersion();
                test.assertSame(parameters, addVersionResult);
                test.assertEqual(Iterable.create("--version"), parameters.getArguments());
            });

            runner.testGroup("addClasspath(String...)", () ->
            {
                final Action2<String,Throwable> addClasspathErrorTest = (String classpath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        test.assertThrows(() -> parameters.addClasspath(classpath),
                            expected);
                        test.assertEqual(Iterable.create("--class-path"), parameters.getArguments());
                    });
                };

                addClasspathErrorTest.run(null, new PreConditionFailure("argument cannot be empty."));
                addClasspathErrorTest.run("", new PreConditionFailure("argument cannot be empty."));

                final Action1<String> addClasspathTest = (String classpath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classpath), (Test test) ->
                    {
                        final JavacParameters parameters = JavacParameters.create();
                        final JavacParameters addClasspathResult = parameters.addClasspath(classpath);
                        test.assertSame(parameters, addClasspathResult);
                        test.assertEqual(Iterable.create("--class-path", classpath), parameters.getArguments());
                    });
                };

                addClasspathTest.run("outputs");
                addClasspathTest.run("outputs/");
                addClasspathTest.run("outputs/folder");
                addClasspathTest.run("/outputs/folder/");
            });

            runner.testGroup("addXLint(String...)", () ->
            {
                runner.test("with null keys", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    test.assertThrows(() -> parameters.addXLint((String[])null),
                        new PreConditionFailure("xlintKeys cannot be null."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                runner.test("with null key", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    test.assertThrows(() -> parameters.addXLint("abc", null, "def"),
                        new PreConditionFailure("xlintKeys.contains(null) cannot be true."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                runner.test("with no arguments", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint();
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint"), parameters.getArguments());
                });

                runner.test("with empty array", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint(new String[0]);
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint"), parameters.getArguments());
                });

                runner.test("with one key", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint("all");
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint:all"), parameters.getArguments());
                });

                runner.test("with multiple keys", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint("module", "open", "options");
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint:module,open,options"), parameters.getArguments());
                });
            });

            runner.testGroup("addXLint(Iterable<String>)", () ->
            {
                runner.test("with null keys", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    test.assertThrows(() -> parameters.addXLint((Iterable<String>)null),
                        new PreConditionFailure("xlintKeys cannot be null."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                runner.test("with null key", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    test.assertThrows(() -> parameters.addXLint(Iterable.create("abc", null, "def")),
                        new PreConditionFailure("xlintKeys.contains(null) cannot be true."));
                    test.assertEqual(Iterable.create(), parameters.getArguments());
                });

                runner.test("with empty array", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint(Iterable.create());
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint"), parameters.getArguments());
                });

                runner.test("with one key", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint(Iterable.create("all"));
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint:all"), parameters.getArguments());
                });

                runner.test("with multiple keys", (Test test) ->
                {
                    final JavacParameters parameters = JavacParameters.create();
                    final JavacParameters addXLintResult = parameters.addXLint(Iterable.create("module", "open", "options"));
                    test.assertSame(parameters, addXLintResult);
                    test.assertEqual(Iterable.create("-Xlint:module,open,options"), parameters.getArguments());
                });
            });
        });
    }
}
