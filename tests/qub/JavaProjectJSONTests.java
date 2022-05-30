package qub;

public interface JavaProjectJSONTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectJSON.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JavaProjectJSON projectJson = JavaProjectJSON.create();
                test.assertNotNull(projectJson);
                test.assertNull(projectJson.getPublisher());
                test.assertNull(projectJson.getProject());
                test.assertNull(projectJson.getVersion());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectJSON.create((JSONObject)null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action2<JSONObject, JavaProjectJSON> parseTest = (JSONObject json, JavaProjectJSON expected) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        test.assertEqual(expected, JavaProjectJSON.create(json));
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    JavaProjectJSON.create());
                parseTest.run(
                    JSONObject.create()
                        .setString("publisher", "a"),
                    JavaProjectJSON.create()
                        .setPublisher("a"));
                parseTest.run(
                    JSONObject.create()
                        .setString("project", "b"),
                    JavaProjectJSON.create()
                        .setProject("b"));
                parseTest.run(
                    JSONObject.create()
                        .setString("version", "c"),
                    JavaProjectJSON.create()
                        .setVersion("c"));
                parseTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
            });

            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectJSON.parse((File)null),
                        new PreConditionFailure("projectJsonFile cannot be null."));
                });

                runner.test("with file that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                    fileSystem.createRoot("/").await();
                    final File file = fileSystem.getFile("/file.txt").await();
                    test.assertThrows(() -> JavaProjectJSON.parse(file).await(),
                        new FileNotFoundException("/file.txt"));
                });

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                        fileSystem.createRoot("/").await();
                        final File file = fileSystem.createFile("/file.txt").await();
                        file.setContentsAsString(text).await();
                        test.assertEqual(expected, JavaProjectJSON.parse(file).await());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("parse(ByteReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectJSON.parse((ByteReadStream)null),
                        new PreConditionFailure("bytes cannot be null."));
                });

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final ByteReadStream stream = InMemoryCharacterToByteStream.create(text).endOfStream();
                        test.assertEqual(expected, JavaProjectJSON.parse(stream).await());
                        test.assertFalse(stream.isDisposed());
                        test.assertThrows(() -> stream.readByte().await(),
                            new EmptyException());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("parse(CharacterReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectJSON.parse((CharacterReadStream)null),
                        new PreConditionFailure("characters cannot be null."));
                });

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final CharacterReadStream stream = InMemoryCharacterToByteStream.create(text).endOfStream();
                        test.assertEqual(expected, JavaProjectJSON.parse(stream).await());
                        test.assertFalse(stream.isDisposed());
                        test.assertThrows(() -> stream.readCharacter().await(),
                            new EmptyException());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("parse(String)", () ->
            {
                final Action2<String,Throwable> parseErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> JavaProjectJSON.parse(text).await(),
                            expected);
                    });
                };

                parseErrorTest.run(null, new PreConditionFailure("text cannot be null."));
                parseErrorTest.run("", new ParseException("Missing object left curly bracket ('{')."));
                parseErrorTest.run("[]", new ParseException("Expected object left curly bracket ('{')."));

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(expected, JavaProjectJSON.parse(text).await());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("parse(Iterable<Character>)", () ->
            {
                final Action2<String,Throwable> parseErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> JavaProjectJSON.parse(text == null ? null : Strings.iterable(text)).await(),
                            expected);
                    });
                };

                parseErrorTest.run(null, new PreConditionFailure("characters cannot be null."));
                parseErrorTest.run("", new ParseException("Missing object left curly bracket ('{')."));
                parseErrorTest.run("[]", new ParseException("Expected object left curly bracket ('{')."));

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(expected, JavaProjectJSON.parse(Strings.iterable(text)).await());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("parse(Iterator<Character>)", () ->
            {
                final Action2<String,Throwable> parseErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> JavaProjectJSON.parse(text == null ? null : Strings.iterate(text)).await(),
                            expected);
                    });
                };

                parseErrorTest.run(null, new PreConditionFailure("characters cannot be null."));
                parseErrorTest.run("", new ParseException("Missing object left curly bracket ('{')."));
                parseErrorTest.run("[]", new ParseException("Expected object left curly bracket ('{')."));

                final Action2<String, JavaProjectJSON> parseTest = (String text, JavaProjectJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(expected, JavaProjectJSON.parse(Strings.iterate(text)).await());
                    });
                };

                parseTest.run(
                    "{}",
                    JavaProjectJSON.create());
                parseTest.run(
                    "{\"publisher\":\"a\"}",
                    JavaProjectJSON.create().setPublisher("a"));
                parseTest.run(
                    "{\"publisher\":5}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("publisher", 5)));
                parseTest.run(
                    "{\"project\":\"b\"}",
                    JavaProjectJSON.create().setProject("b"));
                parseTest.run(
                    "{\"project\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("project", true)));
                parseTest.run(
                    "{\"version\":\"c\"}",
                    JavaProjectJSON.create().setVersion("c"));
                parseTest.run(
                    "{\"version\":10}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setNumber("version", 10)));
                parseTest.run(
                    "{\"version\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("version", Iterable.create())));
                parseTest.run(
                    "{\"java\":{}}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setObject("java", JSONObject.create())));
                parseTest.run(
                    "{\"java\":[]}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setArray("java", Iterable.create())));
                parseTest.run(
                    "{\"java\":true}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", true)));
                parseTest.run(
                    "{\"java\":false}",
                    JavaProjectJSON.create(JSONObject.create()
                        .setBoolean("java", false)));
            });

            runner.testGroup("getMainClass()", () ->
            {
                final Action2<JSONObject,String> getMainClassTest = (JSONObject json, String expected) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        test.assertEqual(expected, projectJson.getMainClass());
                    });
                };

                getMainClassTest.run(
                    JSONObject.create(),
                    null);
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    null);
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setNull("mainClass")),
                    null);
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("mainClass", "")),
                    "");
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("mainClass", "hello")),
                    "hello");
            });

            runner.testGroup("setMainClass(String)", () ->
            {
                final Action2<JSONObject,String> getMainClassTest = (JSONObject json, String mainClass) ->
                {
                    runner.test("with " + English.andList(json, Strings.escapeAndQuote(mainClass)), (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        final JavaProjectJSON setMainClassResult = projectJson.setMainClass(mainClass);
                        test.assertSame(projectJson, setMainClassResult);
                        test.assertEqual(mainClass, projectJson.getMainClass());
                    });
                };

                getMainClassTest.run(
                    JSONObject.create(),
                    "fake.main.class");
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    "fake.main.class");
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setNull("mainClass")),
                    "fake.main.class");
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("mainClass", "")),
                    "fake.main.class");
                getMainClassTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("mainClass", "hello")),
                    "fake.main.class");
            });

            runner.testGroup("getShortcutName()", () ->
            {
                final Action2<JSONObject,String> getShortcutNameTest = (JSONObject json, String expected) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        test.assertEqual(expected, projectJson.getShortcutName());
                    });
                };

                getShortcutNameTest.run(
                    JSONObject.create(),
                    null);
                getShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    null);
                getShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setNull("shortcutName")),
                    null);
                getShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("shortcutName", "")),
                    "");
                getShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("shortcutName", "hello")),
                    "hello");
            });

            runner.testGroup("setShortcutName(String)", () ->
            {
                final Action2<JSONObject,String> setShortcutNameTest = (JSONObject json, String shortcutName) ->
                {
                    runner.test("with " + English.andList(json, Strings.escapeAndQuote(shortcutName)), (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        final JavaProjectJSON setShortcutNameResult = projectJson.setShortcutName(shortcutName);
                        test.assertSame(projectJson, setShortcutNameResult);
                        test.assertEqual(shortcutName, projectJson.getShortcutName());
                    });
                };

                setShortcutNameTest.run(
                    JSONObject.create(),
                    "fake-shortcut-name");
                setShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    "fake-shortcut-name");
                setShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setNull("shortcutName")),
                    "fake-shortcut-name");
                setShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("shortcutName", "")),
                    "fake-shortcut-name");
                setShortcutNameTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("shortcutName", "hello")),
                    "fake-shortcut-name");
            });

            runner.testGroup("getDependencies()", () ->
            {
                final Action2<JSONObject,Iterable<ProjectSignature>> getDependenciesTest = (JSONObject json, Iterable<ProjectSignature> expected) ->
                {
                    runner.test("with " + json, (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        test.assertEqual(expected, projectJson.getDependencies());
                    });
                };

                getDependenciesTest.run(
                    JSONObject.create(),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setString("dependencies", "hello")),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create())),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .addString("hello"))),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()))),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")))),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")))),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")
                                    .setString("version", "c")))),
                    Iterable.create(ProjectSignature.create("a", "b", "c")));
            });

            runner.testGroup("setDependencies(Iterable<ProjectSignature>)", () ->
            {
                final Action2<JSONObject,Iterable<ProjectSignature>> setDependenciesTest = (JSONObject json, Iterable<ProjectSignature> dependencies) ->
                {
                    runner.test("with " + English.andList(json, dependencies), (Test test) ->
                    {
                        final JavaProjectJSON projectJson = JavaProjectJSON.create(json);
                        final JavaProjectJSON setDependenciesResult = projectJson.setDependencies(dependencies);
                        test.assertSame(projectJson, setDependenciesResult);
                        test.assertEqual(dependencies, projectJson.getDependencies());
                    });
                };

                setDependenciesTest.run(
                    JSONObject.create(),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create())),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()))),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")))),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")))),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")
                                    .setString("version", "c")))),
                    Iterable.create());
                setDependenciesTest.run(
                    JSONObject.create(),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create())),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()))),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")))),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")))),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
                setDependenciesTest.run(
                    JSONObject.create()
                        .setObject("java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create()
                                .add(JSONObject.create()
                                    .setString("publisher", "a")
                                    .setString("project", "b")
                                    .setString("version", "c")))),
                    Iterable.create(
                        ProjectSignature.create("x", "y", "z")));
            });

            runner.testGroup("getAllDependencyFolders(QubFolder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    test.assertThrows(() -> projectJson.getAllDependencyFolders(null),
                        new PreConditionFailure("qubFolder cannot be null."));
                });

                runner.test("with no dependencies",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectJSON projectJson = JavaProjectJSON.create();
                    final QubFolder qubFolder = process.getQubFolder().await();
                    test.assertEqual(Iterable.create(), projectJson.getAllDependencyFolders(qubFolder).await());
                });

                runner.test("with not found publisher",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setDependencies(Iterable.create(
                            ProjectSignature.create("not-found-publisher", "fake-project", "7")));
                    final QubFolder qubFolder = process.getQubFolder().await();
                    test.assertThrows(() -> projectJson.getAllDependencyFolders(qubFolder).await(),
                        new FileNotFoundException("/qub/not-found-publisher/fake-project/versions/7/project.json"));
                });

                runner.test("with not found project",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setDependencies(Iterable.create(
                            ProjectSignature.create("fake-publisher", "not-found-project", "7")));
                    final QubFolder qubFolder = process.getQubFolder().await();
                    test.assertThrows(() -> projectJson.getAllDependencyFolders(qubFolder).await(),
                        new FileNotFoundException("/qub/fake-publisher/not-found-project/versions/7/project.json"));
                });

                runner.test("with not found version",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setDependencies(Iterable.create(
                            ProjectSignature.create("fake-publisher", "fake-project", "not-found-version")));
                    final QubFolder qubFolder = process.getQubFolder().await();
                    test.assertThrows(() -> projectJson.getAllDependencyFolders(qubFolder).await(),
                        new FileNotFoundException("/qub/fake-publisher/fake-project/versions/not-found-version/project.json"));
                });

                runner.test("with existing dependency",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final JavaPublishedProjectFolder dependencyFolder = JavaPublishedProjectFolder.get(
                        qubFolder.getProjectVersionFolder("fake-publisher", "fake-project", "8").await());
                    dependencyFolder.getProjectJsonFile().await()
                        .setContentsAsString(JavaProjectJSON.create()
                            .toString()).await();
                    final JavaProjectJSON projectJson = JavaProjectJSON.create()
                        .setDependencies(Iterable.create(
                            ProjectSignature.create(dependencyFolder.getPublisherName().await(), dependencyFolder.getProjectName().await(), dependencyFolder.getVersion().await())));
                    test.assertEqual(
                        Iterable.create(dependencyFolder),
                        projectJson.getAllDependencyFolders(qubFolder).await());
                });
            });
        });
    }
}
