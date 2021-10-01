package qub;

public interface JavaProjectJsonTests
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
                            new EndOfStreamException());
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
                            new EndOfStreamException());
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
        });
    }
}
