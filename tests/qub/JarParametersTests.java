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

            runner.test("addVersion()", (Test test) ->
            {
                final JarParameters parameters = JarParameters.create();
                final JarParameters addCreateResult = parameters.addCreate();
                test.assertSame(parameters, addCreateResult);
                test.assertEqual(Iterable.create("--create"), parameters.getArguments());
            });

            runner.testGroup("addJarFile(String)", () ->
            {
                final Action2<String,Throwable> addJarFileErrorTest = (String jarFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addJarFile(jarFilePath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addJarFileErrorTest.run(null, new PreConditionFailure("jarFilePath cannot be null."));
                addJarFileErrorTest.run("", new PreConditionFailure("jarFilePath cannot be empty."));

                final Action2<String,Iterable<String>> addJarFileWithNoWorkingFolderPathTest = (String jarFilePath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addJarFileResult = parameters.addJarFile(jarFilePath);
                        test.assertSame(parameters, addJarFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addJarFileWithNoWorkingFolderPathTest.run("fake-jar-file", Iterable.create("--file=fake-jar-file"));
                addJarFileWithNoWorkingFolderPathTest.run("relative/fake-jar-file", Iterable.create("--file=relative/fake-jar-file"));
                addJarFileWithNoWorkingFolderPathTest.run("/rooted/fake-jar-file", Iterable.create("--file=/rooted/fake-jar-file"));

                final Action3<String,String,Iterable<String>> addJarFileWithWorkingFolderPathTest = (String jarFilePath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addJarFileResult = parameters.addJarFile(jarFilePath);
                        test.assertSame(parameters, addJarFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addJarFileWithWorkingFolderPathTest.run("fake-jar-file", "/working/folder/", Iterable.create("--file=fake-jar-file"));
                addJarFileWithWorkingFolderPathTest.run("relative/fake-jar-file", "/working/folder/", Iterable.create("--file=relative/fake-jar-file"));
                addJarFileWithWorkingFolderPathTest.run("/rooted/fake-jar-file", "/working/folder/", Iterable.create("--file=../../rooted/fake-jar-file"));
            });

            runner.testGroup("addJarFile(Path)", () ->
            {
                final Action2<Path,Throwable> addJarFileErrorTest = (Path jarFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addJarFile(jarFilePath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addJarFileErrorTest.run(null, new PreConditionFailure("jarFilePath cannot be null."));

                final Action2<Path,Iterable<String>> addJarFileWithNoWorkingFolderPathTest = (Path jarFilePath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addJarFileResult = parameters.addJarFile(jarFilePath);
                        test.assertSame(parameters, addJarFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addJarFileWithNoWorkingFolderPathTest.run(Path.parse("fake-jar-file"), Iterable.create("--file=fake-jar-file"));
                addJarFileWithNoWorkingFolderPathTest.run(Path.parse("relative/fake-jar-file"), Iterable.create("--file=relative/fake-jar-file"));
                addJarFileWithNoWorkingFolderPathTest.run(Path.parse("/rooted/fake-jar-file"), Iterable.create("--file=/rooted/fake-jar-file"));

                final Action3<Path,String,Iterable<String>> addJarFileWithWorkingFolderPathTest = (Path jarFilePath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(jarFilePath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addJarFileResult = parameters.addJarFile(jarFilePath);
                        test.assertSame(parameters, addJarFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addJarFileWithWorkingFolderPathTest.run(Path.parse("fake-jar-file"), "/working/folder/", Iterable.create("--file=fake-jar-file"));
                addJarFileWithWorkingFolderPathTest.run(Path.parse("relative/fake-jar-file"), "/working/folder/", Iterable.create("--file=relative/fake-jar-file"));
                addJarFileWithWorkingFolderPathTest.run(Path.parse("/rooted/fake-jar-file"), "/working/folder/", Iterable.create("--file=../../rooted/fake-jar-file"));
            });

            runner.testGroup("addManifestFile(String)", () ->
            {
                final Action2<String,Throwable> addManifestFileErrorTest = (String manifestFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addManifestFile(manifestFilePath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addManifestFileErrorTest.run(null, new PreConditionFailure("manifestFilePath cannot be null."));
                addManifestFileErrorTest.run("", new PreConditionFailure("manifestFilePath cannot be empty."));

                final Action2<String,Iterable<String>> addManifestFileWithNoWorkingFolderPathTest = (String manifestFilePath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addManifestFileResult = parameters.addManifestFile(manifestFilePath);
                        test.assertSame(parameters, addManifestFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addManifestFileWithNoWorkingFolderPathTest.run("fake-manifest-file", Iterable.create("--manifest=fake-manifest-file"));
                addManifestFileWithNoWorkingFolderPathTest.run("relative/fake-manifest-file", Iterable.create("--manifest=relative/fake-manifest-file"));
                addManifestFileWithNoWorkingFolderPathTest.run("/rooted/fake-manifest-file", Iterable.create("--manifest=/rooted/fake-manifest-file"));

                final Action3<String,String,Iterable<String>> addManifestFileWithWorkingFolderPathTest = (String manifestFilePath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addManifestFileResult = parameters.addManifestFile(manifestFilePath);
                        test.assertSame(parameters, addManifestFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addManifestFileWithWorkingFolderPathTest.run("fake-manifest-file", "/working/folder/", Iterable.create("--manifest=fake-manifest-file"));
                addManifestFileWithWorkingFolderPathTest.run("relative/fake-manifest-file", "/working/folder/", Iterable.create("--manifest=relative/fake-manifest-file"));
                addManifestFileWithWorkingFolderPathTest.run("/rooted/fake-manifest-file", "/working/folder/", Iterable.create("--manifest=../../rooted/fake-manifest-file"));
            });

            runner.testGroup("addManifestFile(Path)", () ->
            {
                final Action2<Path,Throwable> addManifestFileErrorTest = (Path manifestFilePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addManifestFile(manifestFilePath),
                            expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addManifestFileErrorTest.run(null, new PreConditionFailure("manifestFilePath cannot be null."));

                final Action2<Path,Iterable<String>> addManifestFileWithNoWorkingFolderPathTest = (Path manifestFilePath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addManifestFileResult = parameters.addManifestFile(manifestFilePath);
                        test.assertSame(parameters, addManifestFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addManifestFileWithNoWorkingFolderPathTest.run(Path.parse("fake-manifest-file"), Iterable.create("--manifest=fake-manifest-file"));
                addManifestFileWithNoWorkingFolderPathTest.run(Path.parse("relative/fake-manifest-file"), Iterable.create("--manifest=relative/fake-manifest-file"));
                addManifestFileWithNoWorkingFolderPathTest.run(Path.parse("/rooted/fake-manifest-file"), Iterable.create("--manifest=/rooted/fake-manifest-file"));

                final Action3<Path,String,Iterable<String>> addManifestFileWithWorkingFolderPathTest = (Path manifestFilePath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(manifestFilePath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addManifestFileResult = parameters.addManifestFile(manifestFilePath);
                        test.assertSame(parameters, addManifestFileResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addManifestFileWithWorkingFolderPathTest.run(Path.parse("fake-manifest-file"), "/working/folder/", Iterable.create("--manifest=fake-manifest-file"));
                addManifestFileWithWorkingFolderPathTest.run(Path.parse("relative/fake-manifest-file"), "/working/folder/", Iterable.create("--manifest=relative/fake-manifest-file"));
                addManifestFileWithWorkingFolderPathTest.run(Path.parse("/rooted/fake-manifest-file"), "/working/folder/", Iterable.create("--manifest=../../rooted/fake-manifest-file"));
            });

            runner.testGroup("addContentPathStrings(Iterable<String>)", () ->
            {
                final Action2<Iterable<String>,Throwable> addContentPathStringsErrorTest = (Iterable<String> contentPathStrings, Throwable expected) ->
                {
                    runner.test("with " + contentPathStrings, (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addContentPathStrings(contentPathStrings), expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addContentPathStringsErrorTest.run(null, new PreConditionFailure("contentPathStrings cannot be null."));
                addContentPathStringsErrorTest.run(Iterable.create((String)null), new PreConditionFailure("pathString cannot be null."));

                final Action2<Iterable<String>,Iterable<String>> addContentPathStringsWithNoWorkingFolderPathTest = (Iterable<String> contentPathStrings, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPathStrings) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addContentPathStringsResult = parameters.addContentPathStrings(contentPathStrings);
                        test.assertSame(parameters, addContentPathStringsResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create("fake-manifest-file"), Iterable.create("fake-manifest-file"));
                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create("relative/fake-manifest-file"), Iterable.create("relative/fake-manifest-file"));
                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create("/rooted/fake-manifest-file"), Iterable.create("/rooted/fake-manifest-file"));

                final Action3<Iterable<String>,String,Iterable<String>> addContentPathStringsWithWorkingFolderPathTest = (Iterable<String> contentPathStrings, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPathStrings) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addContentPathStringsResult = parameters.addContentPathStrings(contentPathStrings);
                        test.assertSame(parameters, addContentPathStringsResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create("fake-manifest-file"), "/working/folder/", Iterable.create("fake-manifest-file"));
                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create("relative/fake-manifest-file"), "/working/folder/", Iterable.create("relative/fake-manifest-file"));
                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create("/rooted/fake-manifest-file"), "/working/folder/", Iterable.create("../../rooted/fake-manifest-file"));
            });

            runner.testGroup("addContentPaths(Iterable<Path>)", () ->
            {
                final Action2<Iterable<Path>,Throwable> addContentPathStringsErrorTest = (Iterable<Path> contentPaths, Throwable expected) ->
                {
                    runner.test("with " + contentPaths, (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addContentPaths(contentPaths), expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addContentPathStringsErrorTest.run(null, new PreConditionFailure("contentPaths cannot be null."));
                addContentPathStringsErrorTest.run(Iterable.create((Path)null), new PreConditionFailure("contentPath cannot be null."));

                final Action2<Iterable<Path>,Iterable<String>> addContentPathStringsWithNoWorkingFolderPathTest = (Iterable<Path> contentPaths, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + contentPaths + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addContentPathStringsResult = parameters.addContentPaths(contentPaths);
                        test.assertSame(parameters, addContentPathStringsResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create(Path.parse("fake-manifest-file")), Iterable.create("fake-manifest-file"));
                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create(Path.parse("relative/fake-manifest-file")), Iterable.create("relative/fake-manifest-file"));
                addContentPathStringsWithNoWorkingFolderPathTest.run(Iterable.create(Path.parse("/rooted/fake-manifest-file")), Iterable.create("/rooted/fake-manifest-file"));

                final Action3<Iterable<Path>,String,Iterable<String>> addContentPathStringsWithWorkingFolderPathTest = (Iterable<Path> contentPaths, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + contentPaths + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addContentPathStringsResult = parameters.addContentPaths(contentPaths);
                        test.assertSame(parameters, addContentPathStringsResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create(Path.parse("fake-manifest-file")), "/working/folder/", Iterable.create("fake-manifest-file"));
                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create(Path.parse("relative/fake-manifest-file")), "/working/folder/", Iterable.create("relative/fake-manifest-file"));
                addContentPathStringsWithWorkingFolderPathTest.run(Iterable.create(Path.parse("/rooted/fake-manifest-file")), "/working/folder/", Iterable.create("../../rooted/fake-manifest-file"));
            });

            runner.testGroup("addContentPath(String)", () ->
            {
                final Action2<String,Throwable> addContentPathErrorTest = (String contentPath, Throwable expected) ->
                {
                    runner.test("with " + contentPath, (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addContentPath(contentPath), expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addContentPathErrorTest.run(null, new PreConditionFailure("contentPath cannot be null."));
                addContentPathErrorTest.run("", new PreConditionFailure("contentPath cannot be empty."));

                final Action2<String,Iterable<String>> addContentPathWithNoWorkingFolderPathTest = (String contentPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addContentPathResult = parameters.addContentPath(contentPath);
                        test.assertSame(parameters, addContentPathResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathWithNoWorkingFolderPathTest.run("fake-manifest-file", Iterable.create("fake-manifest-file"));
                addContentPathWithNoWorkingFolderPathTest.run("relative/fake-manifest-file", Iterable.create("relative/fake-manifest-file"));
                addContentPathWithNoWorkingFolderPathTest.run("/rooted/fake-manifest-file", Iterable.create("/rooted/fake-manifest-file"));

                final Action3<String,String,Iterable<String>> addContentPathWithWorkingFolderPathTest = (String contentPath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addContentPathResult = parameters.addContentPath(contentPath);
                        test.assertSame(parameters, addContentPathResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathWithWorkingFolderPathTest.run("fake-manifest-file", "/working/folder/", Iterable.create("fake-manifest-file"));
                addContentPathWithWorkingFolderPathTest.run("relative/fake-manifest-file", "/working/folder/", Iterable.create("relative/fake-manifest-file"));
                addContentPathWithWorkingFolderPathTest.run("/rooted/fake-manifest-file", "/working/folder/", Iterable.create("../../rooted/fake-manifest-file"));
            });

            runner.testGroup("addContentPath(Path)", () ->
            {
                final Action2<Path,Throwable> addContentPathErrorTest = (Path contentPath, Throwable expected) ->
                {
                    runner.test("with " + contentPath, (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        test.assertThrows(() -> parameters.addContentPath(contentPath), expected);
                        test.assertEqual(Iterable.create(), parameters.getArguments());
                    });
                };

                addContentPathErrorTest.run(null, new PreConditionFailure("contentPath cannot be null."));

                final Action2<Path,Iterable<String>> addContentPathWithNoWorkingFolderPathTest = (Path contentPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPath) + " with no working folder path", (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create();
                        final JarParameters addContentPathResult = parameters.addContentPath(contentPath);
                        test.assertSame(parameters, addContentPathResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathWithNoWorkingFolderPathTest.run(Path.parse("fake-manifest-file"), Iterable.create("fake-manifest-file"));
                addContentPathWithNoWorkingFolderPathTest.run(Path.parse("relative/fake-manifest-file"), Iterable.create("relative/fake-manifest-file"));
                addContentPathWithNoWorkingFolderPathTest.run(Path.parse("/rooted/fake-manifest-file"), Iterable.create("/rooted/fake-manifest-file"));

                final Action3<Path,String,Iterable<String>> addContentPathWithWorkingFolderPathTest = (Path contentPath, String workingFolderPath, Iterable<String> expectedArguments) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(contentPath) + " with working folder path " + Strings.escapeAndQuote(workingFolderPath), (Test test) ->
                    {
                        final JarParameters parameters = JarParameters.create()
                            .setWorkingFolderPath(workingFolderPath);
                        final JarParameters addContentPathResult = parameters.addContentPath(contentPath);
                        test.assertSame(parameters, addContentPathResult);
                        test.assertEqual(expectedArguments, parameters.getArguments());
                    });
                };

                addContentPathWithWorkingFolderPathTest.run(Path.parse("fake-manifest-file"), "/working/folder/", Iterable.create("fake-manifest-file"));
                addContentPathWithWorkingFolderPathTest.run(Path.parse("relative/fake-manifest-file"), "/working/folder/", Iterable.create("relative/fake-manifest-file"));
                addContentPathWithWorkingFolderPathTest.run(Path.parse("/rooted/fake-manifest-file"), "/working/folder/", Iterable.create("../../rooted/fake-manifest-file"));
            });
        });
    }
}
