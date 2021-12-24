package qub;

public interface TestJSONTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(TestJSON.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final TestJSON testJson = TestJSON.create();
                test.assertNotNull(testJson);
                test.assertEqual(JSONObject.create(), testJson.toJson());
                test.assertEqual("{}", testJson.toString());
                test.assertNull(testJson.getJavaVersion());
                test.assertEqual(Iterable.create(), testJson.getClassFiles());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSON.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action2<JSONObject,Action2<Test,TestJSON>> createTest = (JSONObject json, Action2<Test,TestJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final TestJSON testJson = TestJSON.create(json);
                        test.assertNotNull(testJson);
                        test.assertSame(json, testJson.toJson());

                        validation.run(test, testJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaVersion", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("classFiles", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setString("a", "b")),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", "b"))),
                            testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()))),
                            testJson.getClassFiles());
                    });
            });

            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSON.parse((File)null),
                        new PreConditionFailure("testJsonFile cannot be null."));
                });

                runner.test("with file that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File testJsonFile = fileSystem.getFile("/test.json").await();

                    test.assertThrows(() -> TestJSON.parse(testJsonFile).await(),
                        new FileNotFoundException(testJsonFile));
                    test.assertFalse(testJsonFile.exists().await());
                });

                runner.test("with empty file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File testJsonFile = fileSystem.createFile("/test.json").await();

                    test.assertThrows(() -> TestJSON.parse(testJsonFile).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,TestJSON>> createTest = (JSONObject json, Action2<Test,TestJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                        fileSystem.createRoot("/").await();
                        final File testJsonFile = fileSystem.getFile("/test.json").await();
                        testJsonFile.setContentsAsString(json.toString()).await();

                        final TestJSON testJson = TestJSON.parse(testJsonFile).await();
                        test.assertNotNull(testJson);
                        test.assertEqual(json, testJson.toJson());

                        validation.run(test, testJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaVersion", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("classFiles", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setString("a", "b")),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", "b"))),
                            testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()))),
                            testJson.getClassFiles());
                    });
            });

            runner.testGroup("parse(ByteReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSON.parse((ByteReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryByteStream readStream = InMemoryByteStream.create();
                    readStream.dispose().await();
                    test.assertThrows(() -> TestJSON.parse(readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryByteStream readStream = InMemoryByteStream.create().endOfStream();
                    test.assertThrows(() -> TestJSON.parse(readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,TestJSON>> createTest = (JSONObject json, Action2<Test,TestJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final TestJSON testJson = TestJSON.parse((ByteReadStream)readStream).await();
                        test.assertNotNull(testJson);
                        test.assertEqual(json, testJson.toJson());

                        validation.run(test, testJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaVersion", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("classFiles", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setString("a", "b")),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", "b"))),
                            testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()))),
                            testJson.getClassFiles());
                    });
            });

            runner.testGroup("parse(CharacterReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSON.parse((CharacterReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                    readStream.dispose().await();
                    test.assertThrows(() -> TestJSON.parse((CharacterReadStream)readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create().endOfStream();
                    test.assertThrows(() -> TestJSON.parse((CharacterReadStream)readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,TestJSON>> createTest = (JSONObject json, Action2<Test,TestJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final TestJSON testJson = TestJSON.parse((CharacterReadStream)readStream).await();
                        test.assertNotNull(testJson);
                        test.assertEqual(json, testJson.toJson());

                        validation.run(test, testJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaVersion", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("classFiles", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setString("a", "b")),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", "b"))),
                            testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()))),
                            testJson.getClassFiles());
                    });
            });

            runner.testGroup("parse(Iterator<Character>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSON.parse((Iterator<Character>)null),
                        new PreConditionFailure("characters cannot be null."));
                });

                runner.test("with empty iterator", (Test test) ->
                {
                    final Iterator<Character> characters = Iterator.create();
                    test.assertThrows(() -> TestJSON.parse(characters).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,TestJSON>> createTest = (JSONObject json, Action2<Test,TestJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final Iterator<Character> characters = Strings.iterate(json.toString());

                        final TestJSON testJson = TestJSON.parse(characters).await();
                        test.assertNotNull(testJson);
                        test.assertEqual(json, testJson.toJson());

                        validation.run(test, testJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("javaVersion", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("classFiles", "1"),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(Iterable.create(), testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setString("a", "b")),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", "b"))),
                            testJson.getClassFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("classFiles", JSONObject.create()
                            .setObject("a", JSONObject.create())),
                    (Test test, TestJSON testJson) ->
                    {
                        test.assertNull(testJson.getJavaVersion());
                        test.assertEqual(
                            Iterable.create(
                                TestJSONClassFile.create(JSONProperty.create("a", JSONObject.create()))),
                            testJson.getClassFiles());
                    });
            });

            runner.testGroup("setJavaVersion(String)", () ->
            {
                final Action2<String,Throwable> setJavaVersionErrorTest = (String javaVersion, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javaVersion), (Test test) ->
                    {
                        final TestJSON testJson = TestJSON.create();
                        test.assertThrows(() -> testJson.setJavaVersion(javaVersion),
                            expected);
                        test.assertEqual(JSONObject.create(), testJson.toJson());
                        test.assertNull(testJson.getJavaVersion());
                    });
                };

                setJavaVersionErrorTest.run(null, new PreConditionFailure("javaVersion cannot be null."));
                setJavaVersionErrorTest.run("", new PreConditionFailure("javaVersion cannot be empty."));

                final Action3<String,TestJSON,VersionNumber> setJavaVersionTest = (String javaVersion, TestJSON testJson, VersionNumber expected) ->
                {
                    runner.test(" with " + English.andList(Strings.escapeAndQuote(javaVersion), testJson.toString()), (Test test) ->
                    {
                        final TestJSON setJavaVersionResult = testJson.setJavaVersion(javaVersion);
                        test.assertSame(testJson, setJavaVersionResult);
                        test.assertEqual(javaVersion, testJson.toJson().getString("javaVersion").await());
                        test.assertEqual(expected, testJson.getJavaVersion());
                    });
                };

                setJavaVersionTest.run(
                    "hello",
                    TestJSON.create(),
                    VersionNumber.create().setSuffix("hello"));
                setJavaVersionTest.run(
                    "16.0.1",
                    TestJSON.create(),
                    VersionNumber.create()
                        .setMajor(16)
                        .setMinor(0)
                        .setPatch(1));
            });

            runner.testGroup("setJavaVersion(VersionNumber)", () ->
            {
                final Action2<VersionNumber,Throwable> setJavaVersionErrorTest = (VersionNumber javaVersion, Throwable expected) ->
                {
                    runner.test("with " + javaVersion, (Test test) ->
                    {
                        final TestJSON testJson = TestJSON.create();
                        test.assertThrows(() -> testJson.setJavaVersion(javaVersion),
                            expected);
                        test.assertEqual(JSONObject.create(), testJson.toJson());
                        test.assertNull(testJson.getJavaVersion());
                    });
                };

                setJavaVersionErrorTest.run(null, new PreConditionFailure("javaVersion cannot be null."));

                final Action2<VersionNumber,TestJSON> setJavaVersionTest = (VersionNumber javaVersion, TestJSON testJson) ->
                {
                    runner.test(" with " + English.andList(Strings.escapeAndQuote(javaVersion), testJson.toString()), (Test test) ->
                    {
                        final TestJSON setJavaVersionResult = testJson.setJavaVersion(javaVersion);
                        test.assertSame(testJson, setJavaVersionResult);
                        test.assertEqual(javaVersion.toString(), testJson.toJson().getString("javaVersion").await());
                        test.assertEqual(javaVersion, testJson.getJavaVersion());
                    });
                };

                setJavaVersionTest.run(
                    VersionNumber.create().setSuffix("hello"),
                    TestJSON.create());
                setJavaVersionTest.run(
                    VersionNumber.create()
                        .setMajor(16)
                        .setMinor(0)
                        .setPatch(1),
                    TestJSON.create());
            });

            runner.testGroup("setClassFiles(Iterable<TestJSONClassFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final TestJSON testJson = TestJSON.create();
                    test.assertThrows(() -> testJson.setClassFiles(null),
                        new PreConditionFailure("classFiles cannot be null."));
                    test.assertEqual(JSONObject.create(), testJson.toJson());
                    test.assertEqual(Iterable.create(), testJson.getClassFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final TestJSON testJson = TestJSON.create();
                    final TestJSON setClassFilesResult = testJson.setClassFiles(Iterable.create());
                    test.assertSame(testJson, setClassFilesResult);
                    test.assertEqual(
                        JSONObject.create()
                            .setObject("classFiles", JSONObject.create()),
                        testJson.toJson());
                    test.assertEqual(Iterable.create(), testJson.getClassFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final TestJSON testJson = TestJSON.create();
                    final Iterable<TestJSONClassFile> classFiles = Iterable.create(
                        TestJSONClassFile.create(JSONProperty.create("hello", "there")),
                        TestJSONClassFile.create(JSONProperty.create("up", "down")));
                    final TestJSON setClassFilesResult = testJson.setClassFiles(classFiles);
                    test.assertSame(testJson, setClassFilesResult);
                    test.assertEqual(
                        JSONObject.create()
                            .setObject("classFiles", JSONObject.create()
                                .setString("hello", "there")
                                .setString("up", "down")),
                        testJson.toJson());
                    test.assertEqual(classFiles, testJson.getClassFiles());
                });
            });
        });
    }
}
