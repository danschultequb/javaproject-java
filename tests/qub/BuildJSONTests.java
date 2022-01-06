package qub;

public interface BuildJSONTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(BuildJSON.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final BuildJSON buildJson = BuildJSON.create();
                test.assertNotNull(buildJson);
                test.assertEqual(JSONObject.create(), buildJson.toJson());
                test.assertNull(buildJson.getProjectJson());
                test.assertNull(buildJson.getJavacVersion());
                test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                test.assertEqual(Iterable.create(), buildJson.getDependencies());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSON.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final BuildJSON buildJson = BuildJSON.create(JSONObject.create());
                    test.assertNotNull(buildJson);
                    test.assertEqual(JSONObject.create(), buildJson.toJson());
                    test.assertNull(buildJson.getProjectJson());
                    test.assertNull(buildJson.getJavacVersion());
                    test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                    test.assertEqual(Iterable.create(), buildJson.getDependencies());
                });
            });

            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSON.parse((File)null),
                        new PreConditionFailure("buildJsonFile cannot be null."));
                });

                runner.test("with file that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File buildJsonFile = fileSystem.getFile("/build.json").await();

                    test.assertThrows(() -> BuildJSON.parse(buildJsonFile).await(),
                        new FileNotFoundException(buildJsonFile));
                    test.assertFalse(buildJsonFile.exists().await());
                });

                runner.test("with empty file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File buildJsonFile = fileSystem.createFile("/build.json").await();

                    test.assertThrows(() -> BuildJSON.parse(buildJsonFile).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,BuildJSON>> createTest = (JSONObject json, Action2<Test,BuildJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                        fileSystem.createRoot("/").await();
                        final File buildJsonFile = fileSystem.getFile("/build.json").await();
                        buildJsonFile.setContentsAsString(json.toString()).await();

                        final BuildJSON buildJson = BuildJSON.parse(buildJsonFile).await();
                        test.assertNotNull(buildJson);
                        test.assertEqual(json, buildJson.toJson());

                        validation.run(test, buildJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("project.json", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertEqual(
                            JavaProjectJSON.create(),
                            buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javacVersion", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertEqual(
                            VersionNumber.create().setMajor(1),
                            buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaFiles", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())
                            .setObject("b", JSONObject.create())),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(
                            Iterable.create(
                                BuildJSONJavaFile.create("a"),
                                BuildJSONJavaFile.create("b")),
                            buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
            });

            runner.testGroup("parse(ByteReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSON.parse((ByteReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                    readStream.dispose().await();

                    test.assertThrows(() -> BuildJSON.parse((ByteReadStream)readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create().endOfStream();

                    test.assertThrows(() -> BuildJSON.parse((ByteReadStream)readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,BuildJSON>> createTest = (JSONObject json, Action2<Test,BuildJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final BuildJSON buildJson = BuildJSON.parse((ByteReadStream)readStream).await();
                        test.assertNotNull(buildJson);
                        test.assertEqual(json, buildJson.toJson());

                        validation.run(test, buildJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("project.json", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertEqual(
                            JavaProjectJSON.create(),
                            buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javacVersion", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertEqual(
                            VersionNumber.create().setMajor(1),
                            buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaFiles", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())
                            .setObject("b", JSONObject.create())),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(
                            Iterable.create(
                                BuildJSONJavaFile.create("a"),
                                BuildJSONJavaFile.create("b")),
                            buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
            });

            runner.testGroup("parse(CharacterReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSON.parse((CharacterReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                    readStream.dispose().await();

                    test.assertThrows(() -> BuildJSON.parse((CharacterReadStream)readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create().endOfStream();

                    test.assertThrows(() -> BuildJSON.parse((CharacterReadStream)readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,BuildJSON>> createTest = (JSONObject json, Action2<Test,BuildJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final BuildJSON buildJson = BuildJSON.parse((CharacterReadStream)readStream).await();
                        test.assertNotNull(buildJson);
                        test.assertEqual(json, buildJson.toJson());

                        validation.run(test, buildJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("project.json", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertEqual(
                            JavaProjectJSON.create(),
                            buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javacVersion", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertEqual(
                            VersionNumber.create().setMajor(1),
                            buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaFiles", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())
                            .setObject("b", JSONObject.create())),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(
                            Iterable.create(
                                BuildJSONJavaFile.create("a"),
                                BuildJSONJavaFile.create("b")),
                            buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
            });

            runner.testGroup("parse(Iterator<Character>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSON.parse((Iterator<Character>)null),
                        new PreConditionFailure("characters cannot be null."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final Iterator<Character> characters = Iterator.create();

                    test.assertThrows(() -> BuildJSON.parse(characters).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,BuildJSON>> createTest = (JSONObject json, Action2<Test,BuildJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final Iterator<Character> characters = Strings.iterate(json.toString());

                        final BuildJSON buildJson = BuildJSON.parse(characters).await();
                        test.assertNotNull(buildJson);
                        test.assertEqual(json, buildJson.toJson());

                        validation.run(test, buildJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("project.json", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertEqual(
                            JavaProjectJSON.create(),
                            buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javacVersion", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertEqual(
                            VersionNumber.create().setMajor(1),
                            buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaFiles", "1"),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(Iterable.create(), buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("javaFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())
                            .setObject("b", JSONObject.create())),
                    (Test test, BuildJSON buildJson) ->
                    {
                        test.assertNull(buildJson.getProjectJson());
                        test.assertNull(buildJson.getJavacVersion());
                        test.assertEqual(
                            Iterable.create(
                                BuildJSONJavaFile.create("a"),
                                BuildJSONJavaFile.create("b")),
                            buildJson.getJavaFiles());
                        test.assertEqual(Iterable.create(), buildJson.getDependencies());
                    });
            });

            runner.testGroup("setProjectJson(JavaProjectJson)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final BuildJSON buildJson = BuildJSON.create();
                    test.assertThrows(() -> buildJson.setProjectJson(null),
                        new PreConditionFailure("projectJson cannot be null."));
                    test.assertNull(buildJson.getProjectJson());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final BuildJSON buildJson = BuildJSON.create();

                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final BuildJSON setProjectJsonResult = buildJson.setProjectJson(projectJson);
                    test.assertSame(buildJson, setProjectJsonResult);
                    test.assertEqual(projectJson, buildJson.getProjectJson());
                    test.assertEqual(
                        JSONObject.create()
                            .setObject("project.json", JSONObject.create()),
                        buildJson.toJson());
                });
            });

            runner.testGroup("getProjectJson()", () ->
            {
                final Action2<BuildJSON,JavaProjectJSON> getProjectJsonTest = (BuildJSON buildJson, JavaProjectJSON expected) ->
                {
                    runner.test("with " + buildJson.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, buildJson.getProjectJson());
                    });
                };

                getProjectJsonTest.run(
                    BuildJSON.create(),
                    null);
                getProjectJsonTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setString("project.json", "hello")),
                    null);
                getProjectJsonTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setObject("project.json", JSONObject.create())),
                    JavaProjectJSON.create());
                getProjectJsonTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setObject("project.json", JSONObject.create()
                            .setString("publisher", "fake-publisher"))),
                    JavaProjectJSON.create()
                        .setPublisher("fake-publisher"));
            });

            runner.testGroup("getDependencies()", () ->
            {
                final Action2<BuildJSON,Iterable<ProjectSignature>> getDependenciesTest = (BuildJSON buildJson, Iterable<ProjectSignature> expected) ->
                {
                    runner.test("with " + buildJson, (Test test) ->
                    {
                        test.assertEqual(expected, buildJson.getDependencies());
                    });
                };

                getDependenciesTest.run(
                    BuildJSON.create(),
                    Iterable.create());
                getDependenciesTest.run(
                    BuildJSON.create()
                        .setProjectJson(JavaProjectJSON.create()),
                    Iterable.create());
                getDependenciesTest.run(
                    BuildJSON.create()
                        .setProjectJson(JavaProjectJSON.create()
                            .setDependencies(Iterable.create())),
                    Iterable.create());
                getDependenciesTest.run(
                    BuildJSON.create()
                        .setProjectJson(JavaProjectJSON.create()
                            .setDependencies(Iterable.create(
                                ProjectSignature.create("a", "b", "c")))),
                    Iterable.create(
                        ProjectSignature.create("a", "b", "c")));
            });

            runner.testGroup("setJavacVersion(String)", () ->
            {
                final Action2<String,Throwable> setJavacVersionErrorTest = (String javacVersion, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javacVersion), (Test test) ->
                    {
                        final BuildJSON buildJson = BuildJSON.create();
                        test.assertThrows(() -> buildJson.setJavacVersion(javacVersion),
                            expected);
                        test.assertNull(buildJson.getJavacVersion());
                    });
                };

                setJavacVersionErrorTest.run(null, new PreConditionFailure("javacVersion cannot be null."));
                setJavacVersionErrorTest.run("", new PreConditionFailure("javacVersion cannot be empty."));

                final Action2<String,VersionNumber> setJavacVersionTest = (String javacVersion, VersionNumber expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javacVersion), (Test test) ->
                    {
                        final BuildJSON buildJson = BuildJSON.create();
                        final BuildJSON setJavacVersionNumber = buildJson.setJavacVersion(javacVersion);
                        test.assertSame(buildJson, setJavacVersionNumber);
                        test.assertEqual(expected, buildJson.getJavacVersion());
                        test.assertEqual(
                            JSONObject.create()
                                .setString("javacVersion", javacVersion),
                            buildJson.toJson());
                    });
                };

                setJavacVersionTest.run("1", VersionNumber.create().setMajor(1));
                setJavacVersionTest.run("16.0.1", VersionNumber.create().setMajor(16).setMinor(0).setPatch(1));
            });

            runner.testGroup("setJavacVersion(VersionNumber)", () ->
            {
                final Action2<VersionNumber,Throwable> setJavacVersionErrorTest = (VersionNumber javacVersion, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javacVersion), (Test test) ->
                    {
                        final BuildJSON buildJson = BuildJSON.create();
                        test.assertThrows(() -> buildJson.setJavacVersion(javacVersion),
                            expected);
                        test.assertNull(buildJson.getJavacVersion());
                    });
                };

                setJavacVersionErrorTest.run(null, new PreConditionFailure("javacVersion cannot be null."));

                final Action1<VersionNumber> setJavacVersionTest = (VersionNumber javacVersion) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javacVersion), (Test test) ->
                    {
                        final BuildJSON buildJson = BuildJSON.create();
                        final BuildJSON setJavacVersionNumber = buildJson.setJavacVersion(javacVersion);
                        test.assertSame(buildJson, setJavacVersionNumber);
                        test.assertEqual(javacVersion, buildJson.getJavacVersion());
                        test.assertEqual(
                            JSONObject.create()
                                .setString("javacVersion", javacVersion.toString()),
                            buildJson.toJson());
                    });
                };

                setJavacVersionTest.run(VersionNumber.create().setMajor(1));
                setJavacVersionTest.run(VersionNumber.create().setMajor(16).setMinor(0).setPatch(1));
            });

            runner.testGroup("getJavacVersion()", () ->
            {
                final Action2<BuildJSON,VersionNumber> getJavacVersionTest = (BuildJSON buildJson, VersionNumber expected) ->
                {
                    runner.test("with " + buildJson.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, buildJson.getJavacVersion());
                    });
                };

                getJavacVersionTest.run(
                    BuildJSON.create(),
                    null);
                getJavacVersionTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setArray("javacVersion", JSONArray.create())),
                    null);
                getJavacVersionTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setNull("javacVersion")),
                    null);
                getJavacVersionTest.run(
                    BuildJSON.create(JSONObject.create()
                        .setString("javacVersion", "1.2.3")),
                    VersionNumber.create()
                        .setMajor(1)
                        .setMinor(2)
                        .setPatch(3));
            });

            runner.testGroup("getJavaFile(String)", () ->
            {
                final Action3<BuildJSON,String,Throwable> getJavaFileErrorTest = (BuildJSON buildJson, String relativePath, Throwable expected) ->
                {
                    runner.test("with " + English.andList(buildJson, Strings.escapeAndQuote(relativePath)), (Test test) ->
                    {
                        test.assertThrows(() -> buildJson.getJavaFile(relativePath).await(),
                            expected);
                    });
                };

                getJavaFileErrorTest.run(BuildJSON.create(), null, new PreConditionFailure("relativePath cannot be null."));
                getJavaFileErrorTest.run(BuildJSON.create(), "", new PreConditionFailure("relativePath cannot be empty."));
                getJavaFileErrorTest.run(BuildJSON.create(), "/test.java", new PreConditionFailure("relativePath.isRooted() cannot be true."));
                getJavaFileErrorTest.run(BuildJSON.create(), "test.java", new NotFoundException("No .java file found in the BuildJSON object with the path \"test.java\"."));
                getJavaFileErrorTest.run(
                    BuildJSON.create()
                        .setJavaFile(BuildJSONJavaFile.create("TEST.JAVA")),
                    "test.java",
                    new NotFoundException("No .java file found in the BuildJSON object with the path \"test.java\"."));

                final Action3<BuildJSON,String,BuildJSONJavaFile> getJavaFileTest = (BuildJSON buildJson, String relativePath, BuildJSONJavaFile expected) ->
                {
                    runner.test("with " + English.andList(buildJson, Strings.escapeAndQuote(relativePath)), (Test test) ->
                    {
                        test.assertEqual(expected, buildJson.getJavaFile(relativePath).await());
                    });
                };

                getJavaFileTest.run(
                    BuildJSON.create()
                        .setJavaFile(BuildJSONJavaFile.create("A.java").setLastModified(DateTime.create(1, 2, 3))),
                    "A.java",
                    BuildJSONJavaFile.create("A.java").setLastModified(DateTime.create(1, 2, 3)));
            });

            runner.testGroup("getJavaFile(Path)", () ->
            {
                final Action3<BuildJSON,Path,Throwable> getJavaFileErrorTest = (BuildJSON buildJson, Path relativePath, Throwable expected) ->
                {
                    runner.test("with " + English.andList(buildJson, Strings.escapeAndQuote(relativePath)), (Test test) ->
                    {
                        test.assertThrows(() -> buildJson.getJavaFile(relativePath).await(),
                            expected);
                    });
                };

                getJavaFileErrorTest.run(BuildJSON.create(), null, new PreConditionFailure("relativePath cannot be null."));
                getJavaFileErrorTest.run(BuildJSON.create(), Path.parse("/test.java"), new PreConditionFailure("relativePath.isRooted() cannot be true."));
                getJavaFileErrorTest.run(BuildJSON.create(), Path.parse("test.java"), new NotFoundException("No .java file found in the BuildJSON object with the path \"test.java\"."));
                getJavaFileErrorTest.run(
                    BuildJSON.create()
                        .setJavaFile(BuildJSONJavaFile.create("TEST.JAVA")),
                    Path.parse("test.java"),
                    new NotFoundException("No .java file found in the BuildJSON object with the path \"test.java\"."));

                final Action3<BuildJSON,Path,BuildJSONJavaFile> getJavaFileTest = (BuildJSON buildJson, Path relativePath, BuildJSONJavaFile expected) ->
                {
                    runner.test("with " + English.andList(buildJson, Strings.escapeAndQuote(relativePath)), (Test test) ->
                    {
                        test.assertEqual(expected, buildJson.getJavaFile(relativePath).await());
                    });
                };

                getJavaFileTest.run(
                    BuildJSON.create()
                        .setJavaFile(BuildJSONJavaFile.create("A.java").setLastModified(DateTime.create(1, 2, 3))),
                    Path.parse("A.java"),
                    BuildJSONJavaFile.create("A.java").setLastModified(DateTime.create(1, 2, 3)));
            });
        });
    }
}
