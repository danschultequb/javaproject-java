package qub;

public interface PackJSONTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(PackJSON.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final PackJSON packJson = PackJSON.create();
                test.assertNotNull(packJson);
                test.assertEqual(JSONObject.create(), packJson.toJson());
                test.assertEqual("{}", packJson.toString());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action2<JSONObject,Action2<Test,PackJSON>> createTest = (JSONObject json, Action2<Test,PackJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create(json);
                        test.assertNotNull(packJson);
                        test.assertSame(json, packJson.toJson());

                        validation.run(test, packJson);
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("jarVersion", "1"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setBoolean("jarVersion", true),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setBoolean("project", false),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setString("project", "fake-project"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertEqual("fake-project", packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("sources/A.java", DateTime.create(1, 2, 3).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1, 2, 3))),
                            packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("outputs/sources/A.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(2, 3, 4))),
                            packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()
                            .setString("tests/ATests.java", DateTime.create(4, 5, 6).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("tests/ATests.java", DateTime.create(4, 5, 6))),
                            packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                createTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("outputs/tests/ATests.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/tests/ATests.class", DateTime.create(2, 3, 4))),
                            packJson.getTestOutputFiles());
                    });
            });

            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.parse((File)null),
                        new PreConditionFailure("packJsonFile cannot be null."));
                });

                runner.test("with file that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File packJsonFile = fileSystem.getFile("/test.json").await();

                    test.assertThrows(() -> PackJSON.parse(packJsonFile).await(),
                        new FileNotFoundException(packJsonFile));
                    test.assertFalse(packJsonFile.exists().await());
                });

                runner.test("with empty file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File packJsonFile = fileSystem.createFile("/test.json").await();

                    test.assertThrows(() -> PackJSON.parse(packJsonFile).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,PackJSON>> parseTest = (JSONObject json, Action2<Test,PackJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                        fileSystem.createRoot("/").await();
                        final File packJsonFile = fileSystem.getFile("/test.json").await();
                        packJsonFile.setContentsAsString(json.toString()).await();

                        final PackJSON packJson = PackJSON.parse(packJsonFile).await();
                        test.assertNotNull(packJson);
                        test.assertEqual(json, packJson.toJson());

                        validation.run(test, packJson);
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("jarVersion", "1"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("jarVersion", true),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("project", false),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("project", "fake-project"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertEqual("fake-project", packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("sources/A.java", DateTime.create(1, 2, 3).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1, 2, 3))),
                            packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("outputs/sources/A.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(2, 3, 4))),
                            packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()
                            .setString("tests/ATests.java", DateTime.create(4, 5, 6).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("tests/ATests.java", DateTime.create(4, 5, 6))),
                            packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("outputs/tests/ATests.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/tests/ATests.class", DateTime.create(2, 3, 4))),
                            packJson.getTestOutputFiles());
                    });
            });

            runner.testGroup("parse(ByteReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.parse((ByteReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryByteStream readStream = InMemoryByteStream.create();
                    readStream.dispose().await();
                    test.assertThrows(() -> PackJSON.parse(readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryByteStream readStream = InMemoryByteStream.create().endOfStream();
                    test.assertThrows(() -> PackJSON.parse(readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,PackJSON>> parseTest = (JSONObject json, Action2<Test,PackJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final PackJSON packJson = PackJSON.parse((ByteReadStream)readStream).await();
                        test.assertNotNull(packJson);
                        test.assertEqual(json, packJson.toJson());

                        validation.run(test, packJson);
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("jarVersion", "1"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("jarVersion", true),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("project", false),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("project", "fake-project"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertEqual("fake-project", packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("sources/A.java", DateTime.create(1, 2, 3).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1, 2, 3))),
                            packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("outputs/sources/A.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(2, 3, 4))),
                            packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()
                            .setString("tests/ATests.java", DateTime.create(4, 5, 6).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("tests/ATests.java", DateTime.create(4, 5, 6))),
                            packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("outputs/tests/ATests.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/tests/ATests.class", DateTime.create(2, 3, 4))),
                            packJson.getTestOutputFiles());
                    });
            });

            runner.testGroup("parse(CharacterReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.parse((CharacterReadStream)null),
                        new PreConditionFailure("readStream cannot be null."));
                });

                runner.test("with disposed stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                    readStream.dispose().await();
                    test.assertThrows(() -> PackJSON.parse((CharacterReadStream)readStream).await(),
                        new PreConditionFailure("readStream.isDisposed() cannot be true."));
                });

                runner.test("with empty stream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create().endOfStream();
                    test.assertThrows(() -> PackJSON.parse((CharacterReadStream)readStream).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,PackJSON>> parseTest = (JSONObject json, Action2<Test,PackJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream readStream = InMemoryCharacterToByteStream.create();
                        json.toString(readStream).await();
                        readStream.endOfStream();

                        final PackJSON packJson = PackJSON.parse((CharacterReadStream)readStream).await();
                        test.assertNotNull(packJson);
                        test.assertEqual(json, packJson.toJson());

                        validation.run(test, packJson);
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("jarVersion", "1"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("jarVersion", true),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("project", false),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("project", "fake-project"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertEqual("fake-project", packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("sources/A.java", DateTime.create(1, 2, 3).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1, 2, 3))),
                            packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("outputs/sources/A.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(2, 3, 4))),
                            packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()
                            .setString("tests/ATests.java", DateTime.create(4, 5, 6).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("tests/ATests.java", DateTime.create(4, 5, 6))),
                            packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("outputs/tests/ATests.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/tests/ATests.class", DateTime.create(2, 3, 4))),
                            packJson.getTestOutputFiles());
                    });
            });

            runner.testGroup("parse(Iterator<Character>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.parse((Iterator<Character>)null),
                        new PreConditionFailure("characters cannot be null."));
                });

                runner.test("with empty iterator", (Test test) ->
                {
                    final Iterator<Character> characters = Iterator.create();
                    test.assertThrows(() -> PackJSON.parse(characters).await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                final Action2<JSONObject,Action2<Test,PackJSON>> parseTest = (JSONObject json, Action2<Test,PackJSON> validation) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final Iterator<Character> characters = Strings.iterate(json.toString());

                        final PackJSON packJson = PackJSON.parse(characters).await();
                        test.assertNotNull(packJson);
                        test.assertEqual(json, packJson.toJson());

                        validation.run(test, packJson);
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("hello", "there"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("jarVersion", "1"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertEqual(
                            VersionNumber.create()
                                .setMajor(1),
                            packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("jarVersion", true),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setBoolean("project", false),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setString("project", "fake-project"),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertEqual("fake-project", packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("sources/A.java", DateTime.create(1, 2, 3).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("sources/A.java", DateTime.create(1, 2, 3))),
                            packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("outputs/sources/A.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/sources/A.class", DateTime.create(2, 3, 4))),
                            packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testSourceFiles", JSONObject.create()
                            .setString("tests/ATests.java", DateTime.create(4, 5, 6).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("tests/ATests.java", DateTime.create(4, 5, 6))),
                            packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("outputs/tests/ATests.class", DateTime.create(2, 3, 4).toString())),
                    (Test test, PackJSON packJson) ->
                    {
                        test.assertNull(packJson.getJarVersion());
                        test.assertNull(packJson.getProject());
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                        test.assertEqual(
                            Iterable.create(
                                PackJSONFile.create("outputs/tests/ATests.class", DateTime.create(2, 3, 4))),
                            packJson.getTestOutputFiles());
                    });
            });

            runner.testGroup("setJarVersion(VersionNumber)", () ->
            {
                final Action2<VersionNumber,Throwable> setJarVersionErrorTest = (VersionNumber jarVersion, Throwable expected) ->
                {
                    runner.test("with " + jarVersion, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setJarVersion(jarVersion),
                            expected);
                        test.assertNull(packJson.getJarVersion());
                    });
                };

                setJarVersionErrorTest.run(null, new PreConditionFailure("jarVersion cannot be null."));
                setJarVersionErrorTest.run(VersionNumber.create(), new PreConditionFailure("jarVersion cannot be empty."));

                final Action2<PackJSON,VersionNumber> setJarVersionTest = (PackJSON packJson, VersionNumber jarVersion) ->
                {
                    runner.test("with " + English.andList(packJson, jarVersion), (Test test) ->
                    {
                        final PackJSON setJarVersionResult = packJson.setJarVersion(jarVersion);
                        test.assertSame(packJson, setJarVersionResult);
                        test.assertEqual(jarVersion, packJson.getJarVersion());
                    });
                };

                setJarVersionTest.run(
                    PackJSON.create(),
                    VersionNumber.create().setMajor(1));
            });

            runner.testGroup("setJarVersion(String)", () ->
            {
                final Action2<String,Throwable> setJarVersionErrorTest = (String jarVersion, Throwable expected) ->
                {
                    runner.test("with " + jarVersion, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setJarVersion(jarVersion),
                            expected);
                        test.assertNull(packJson.getJarVersion());
                    });
                };

                setJarVersionErrorTest.run(null, new PreConditionFailure("jarVersion cannot be null."));
                setJarVersionErrorTest.run("", new PreConditionFailure("jarVersion cannot be empty."));

                final Action2<PackJSON,String> setJarVersionTest = (PackJSON packJson, String jarVersion) ->
                {
                    runner.test("with " + English.andList(packJson, jarVersion), (Test test) ->
                    {
                        final PackJSON setJarVersionResult = packJson.setJarVersion(jarVersion);
                        test.assertSame(packJson, setJarVersionResult);
                        test.assertEqual(VersionNumber.parse(jarVersion).await(), packJson.getJarVersion());
                    });
                };

                setJarVersionTest.run(
                    PackJSON.create(),
                    "2");
            });

            runner.testGroup("setProject(String)", () ->
            {
                final Action2<String,Throwable> setProjectErrorTest = (String project, Throwable expected) ->
                {
                    runner.test("with " + project, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setProject(project),
                            expected);
                        test.assertNull(packJson.getProject());
                    });
                };

                setProjectErrorTest.run(null, new PreConditionFailure("project cannot be null."));
                setProjectErrorTest.run("", new PreConditionFailure("project cannot be empty."));

                final Action2<PackJSON,String> setProjectTest = (PackJSON packJson, String project) ->
                {
                    runner.test("with " + English.andList(packJson, project), (Test test) ->
                    {
                        final PackJSON setProjectResult = packJson.setProject(project);
                        test.assertSame(packJson, setProjectResult);
                        test.assertEqual(project, packJson.getProject());
                    });
                };

                setProjectTest.run(
                    PackJSON.create(),
                    "fake-project");
            });

            runner.testGroup("setSourceFiles(Iterable<PackJSONFile>)", () ->
            {
                final Action2<Iterable<PackJSONFile>,Throwable> setSourceFilesErrorTest = (Iterable<PackJSONFile> sourceFiles, Throwable expected) ->
                {
                    runner.test("with " + sourceFiles, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setSourceFiles(sourceFiles),
                            expected);
                        test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                    });
                };

                setSourceFilesErrorTest.run(null, new PreConditionFailure("sourceFiles cannot be null."));

                final Action2<PackJSON,Iterable<PackJSONFile>> setSourceFilesTest = (PackJSON packJson, Iterable<PackJSONFile> sourceFiles) ->
                {
                    runner.test("with " + English.andList(packJson, sourceFiles), (Test test) ->
                    {
                        final PackJSON setSourceFilesResult = packJson.setSourceFiles(sourceFiles);
                        test.assertSame(packJson, setSourceFilesResult);
                        test.assertEqual(sourceFiles, packJson.getSourceFiles());
                    });
                };

                setSourceFilesTest.run(
                    PackJSON.create(),
                    Iterable.create());
                setSourceFilesTest.run(
                    PackJSON.create(),
                    Iterable.create(
                        PackJSONFile.create("sources/B.java", DateTime.create(10, 11, 12))));
            });

            runner.testGroup("setSourceOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                final Action2<Iterable<PackJSONFile>,Throwable> setSourceOutputFilesErrorTest = (Iterable<PackJSONFile> sourceOutputFiles, Throwable expected) ->
                {
                    runner.test("with " + sourceOutputFiles, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setSourceOutputFiles(sourceOutputFiles),
                            expected);
                        test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                    });
                };

                setSourceOutputFilesErrorTest.run(null, new PreConditionFailure("sourceOutputFiles cannot be null."));

                final Action2<PackJSON,Iterable<PackJSONFile>> setSourceOutputFilesTest = (PackJSON packJson, Iterable<PackJSONFile> sourceOutputFiles) ->
                {
                    runner.test("with " + English.andList(packJson, sourceOutputFiles), (Test test) ->
                    {
                        final PackJSON setSourceOutputFilesResult = packJson.setSourceOutputFiles(sourceOutputFiles);
                        test.assertSame(packJson, setSourceOutputFilesResult);
                        test.assertEqual(sourceOutputFiles, packJson.getSourceOutputFiles());
                    });
                };

                setSourceOutputFilesTest.run(
                    PackJSON.create(),
                    Iterable.create());
                setSourceOutputFilesTest.run(
                    PackJSON.create(),
                    Iterable.create(
                        PackJSONFile.create("outputs/sources/B.class", DateTime.create(10, 11, 12))));
            });

            runner.testGroup("setTestSourceFiles(Iterable<PackJSONFile>)", () ->
            {
                final Action2<Iterable<PackJSONFile>,Throwable> setTestSourceFilesErrorTest = (Iterable<PackJSONFile> testSourceFiles, Throwable expected) ->
                {
                    runner.test("with " + testSourceFiles, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setTestSourceFiles(testSourceFiles),
                            expected);
                        test.assertEqual(Iterable.create(), packJson.getTestSourceFiles());
                    });
                };

                setTestSourceFilesErrorTest.run(null, new PreConditionFailure("testSourceFiles cannot be null."));

                final Action2<PackJSON,Iterable<PackJSONFile>> setTestSourceFilesTest = (PackJSON packJson, Iterable<PackJSONFile> testSourceFiles) ->
                {
                    runner.test("with " + English.andList(packJson, testSourceFiles), (Test test) ->
                    {
                        final PackJSON setTestSourceFilesResult = packJson.setTestSourceFiles(testSourceFiles);
                        test.assertSame(packJson, setTestSourceFilesResult);
                        test.assertEqual(testSourceFiles, packJson.getTestSourceFiles());
                    });
                };

                setTestSourceFilesTest.run(
                    PackJSON.create(),
                    Iterable.create());
                setTestSourceFilesTest.run(
                    PackJSON.create(),
                    Iterable.create(
                        PackJSONFile.create("tests/CTests.java", DateTime.create(10, 11, 12))));
            });

            runner.testGroup("setTestOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                final Action2<Iterable<PackJSONFile>,Throwable> setTestOutputFilesErrorTest = (Iterable<PackJSONFile> testOutputFiles, Throwable expected) ->
                {
                    runner.test("with " + testOutputFiles, (Test test) ->
                    {
                        final PackJSON packJson = PackJSON.create();
                        test.assertThrows(() -> packJson.setTestOutputFiles(testOutputFiles),
                            expected);
                        test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                    });
                };

                setTestOutputFilesErrorTest.run(null, new PreConditionFailure("testOutputFiles cannot be null."));

                final Action2<PackJSON,Iterable<PackJSONFile>> setTestOutputFilesTest = (PackJSON packJson, Iterable<PackJSONFile> testOutputFiles) ->
                {
                    runner.test("with " + English.andList(packJson, testOutputFiles), (Test test) ->
                    {
                        final PackJSON setTestOutputFilesResult = packJson.setTestOutputFiles(testOutputFiles);
                        test.assertSame(packJson, setTestOutputFilesResult);
                        test.assertEqual(testOutputFiles, packJson.getTestOutputFiles());
                    });
                };

                setTestOutputFilesTest.run(
                    PackJSON.create(),
                    Iterable.create());
                setTestOutputFilesTest.run(
                    PackJSON.create(),
                    Iterable.create(
                        PackJSONFile.create("outputs/tests/BTest.class", DateTime.create(10, 11, 12))));
            });
        });
    }
}
