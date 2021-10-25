package qub;

public interface BuildJSONJavaFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(BuildJSONJavaFile.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String sourceFileRelativePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFileRelativePath), (Test test) ->
                    {
                        test.assertThrows(() -> BuildJSONJavaFile.create(sourceFileRelativePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("sourceFileRelativePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("sourceFileRelativePath cannot be empty."));
                createErrorTest.run("/test.java", new PreConditionFailure("sourceFileRelativePath.isRooted() cannot be true."));

                final Action1<String> createTest = (String sourceFileRelativePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFileRelativePath), (Test test) ->
                    {
                        final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(sourceFileRelativePath);
                        test.assertNotNull(javaFile);
                        test.assertEqual(sourceFileRelativePath, javaFile.getRelativePath().toString());
                        test.assertEqual(JSONProperty.create(sourceFileRelativePath, JSONObject.create()), javaFile.toJson());
                    });
                };

                createTest.run("test.java");
                createTest.run("qub/test.java");
            });

            runner.testGroup("create(Path)", () ->
            {
                final Action2<Path,Throwable> createErrorTest = (Path sourceFileRelativePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFileRelativePath), (Test test) ->
                    {
                        test.assertThrows(() -> BuildJSONJavaFile.create(sourceFileRelativePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("sourceFileRelativePath cannot be null."));
                createErrorTest.run(Path.parse("/test.java"), new PreConditionFailure("sourceFileRelativePath.isRooted() cannot be true."));

                final Action1<Path> createTest = (Path sourceFileRelativePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(sourceFileRelativePath), (Test test) ->
                    {
                        final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(sourceFileRelativePath);
                        test.assertNotNull(javaFile);
                        test.assertEqual(sourceFileRelativePath, javaFile.getRelativePath());
                        test.assertEqual(JSONProperty.create(sourceFileRelativePath.toString(), JSONObject.create()), javaFile.toJson());
                    });
                };

                createTest.run(Path.parse("test.java"));
                createTest.run(Path.parse("qub/test.java"));
            });

            runner.testGroup("create(JSONProperty)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BuildJSONJavaFile.create((JSONProperty)null),
                        new PreConditionFailure("sourceFileProperty cannot be null."));
                });

                runner.test("with relative path property name", (Test test) ->
                {
                    final JSONProperty property = JSONProperty.create("relative/path.java", JSONObject.create());
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(property).await();
                    test.assertNotNull(javaFile);
                    test.assertEqual("relative/path.java", javaFile.getRelativePath().toString());
                    test.assertEqual(property, javaFile.toJson());
                });

                runner.test("with absolute path property name", (Test test) ->
                {
                    final JSONProperty property = JSONProperty.create("/absolute/path.java", JSONObject.create());
                    test.assertThrows(() -> BuildJSONJavaFile.create(property).await(),
                        new java.lang.IllegalArgumentException("sourceFileProperty.getName() cannot be an absolute file path."));
                });

                runner.test("with non-object property value", (Test test) ->
                {
                    final JSONProperty property = JSONProperty.create("relative/path.java", JSONString.get("hello"));
                    test.assertThrows(() -> BuildJSONJavaFile.create(property).await(),
                        new java.lang.IllegalArgumentException("sourceFileProperty.getValue() must be a JSONObject."));
                });
            });

            runner.testGroup("getLastModified()", () ->
            {
                final Action2<JSONProperty,DateTime> getLastModifiedTest = (JSONProperty property, DateTime expected) ->
                {
                    runner.test("with " + property, (Test test) ->
                    {
                        final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(property).await();
                        test.assertEqual(expected, javaFile.getLastModified());
                    });
                };

                getLastModifiedTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()),
                    null);
                getLastModifiedTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("lastModified", JSONNull.segment)),
                    null);
                getLastModifiedTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("lastModified", JSONString.get(""))),
                    null);
                getLastModifiedTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("lastModified", JSONString.get("hello"))),
                    null);
                getLastModifiedTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("lastModified", JSONString.get(DateTime.create(1, 2, 3, 4, 5, 6, 7).toString()))),
                    DateTime.create(1, 2, 3, 4, 5, 6, 7));
            });

            runner.testGroup("setLastModified(DateTime)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    test.assertThrows(() -> javaFile.setLastModified(null),
                        new PreConditionFailure("lastModified cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    final BuildJSONJavaFile setLastModifiedResult = javaFile.setLastModified(DateTime.create(1, 2, 3));
                    test.assertSame(javaFile, setLastModifiedResult);
                    test.assertEqual(DateTime.create(1, 2, 3), javaFile.getLastModified());
                    test.assertEqual(
                        JSONProperty.create("relative/path.java", JSONObject.create()
                            .setString("lastModified", "0001-02-03T00:00Z")),
                        javaFile.toJson());
                });
            });

            runner.testGroup("getDependencies()", () ->
            {
                final Action2<JSONProperty,Iterable<Path>> getDependenciesTest = (JSONProperty property, Iterable<Path> expected) ->
                {
                    runner.test("with " + property, (Test test) ->
                    {
                        final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(property).await();
                        test.assertEqual(expected, javaFile.getDependencies());
                    });
                };

                getDependenciesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("dependencies", JSONNull.segment)),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("dependencies", JSONArray.create())),
                    Iterable.create());
                getDependenciesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("dependencies", JSONArray.create(JSONString.get("hello")))),
                    Iterable.create(Path.parse("hello")));
                getDependenciesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("dependencies", JSONArray.create(JSONString.get("hello"), JSONString.get("there")))),
                    Iterable.create(Path.parse("hello"), Path.parse("there")));
            });

            runner.testGroup("setDependencies(Iterable<Path>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    test.assertThrows(() -> javaFile.setDependencies(null),
                        new PreConditionFailure("dependencies cannot be null."));
                    test.assertEqual(Iterable.create(), javaFile.getDependencies());
                    test.assertEqual(JSONProperty.create("relative/path.java", JSONObject.create()), javaFile.toJson());
                });

                runner.test("with empty", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    final BuildJSONJavaFile setDependenciesResult = javaFile.setDependencies(Iterable.create());
                    test.assertSame(javaFile, setDependenciesResult);
                    test.assertEqual(Iterable.create(), javaFile.getDependencies());
                    test.assertEqual(
                        JSONProperty.create("relative/path.java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create())),
                        javaFile.toJson());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    final BuildJSONJavaFile setDependenciesResult = javaFile.setDependencies(Iterable.create(Path.parse("hello"), Path.parse("there")));
                    test.assertSame(javaFile, setDependenciesResult);
                    test.assertEqual(Iterable.create(Path.parse("hello"), Path.parse("there")), javaFile.getDependencies());
                    test.assertEqual(
                        JSONProperty.create("relative/path.java", JSONObject.create()
                            .setArray("dependencies", JSONArray.create(JSONString.get("hello"), JSONString.get("there")))),
                        javaFile.toJson());
                });
            });

            runner.testGroup("getClassFiles()", () ->
            {
                final Action2<JSONProperty,Iterable<BuildJSONClassFile>> getClassFilesTest = (JSONProperty property, Iterable<BuildJSONClassFile> expected) ->
                {
                    runner.test("with " + property, (Test test) ->
                    {
                        final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create(property).await();
                        test.assertEqual(expected, javaFile.getClassFiles());
                    });
                };

                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONNull.segment)),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create())),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create()
                            .setNull("hello"))),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create()
                            .setString("hello", ""))),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create()
                            .setString("hello", "there"))),
                    Iterable.create());
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create()
                            .setString("hello", "0001-02-03T00:00Z"))),
                    Iterable.create(
                        BuildJSONClassFile.create(Path.parse("hello"), DateTime.create(1, 2, 3))));
                getClassFilesTest.run(
                    JSONProperty.create("relative/path.java", JSONObject.create()
                        .set("classFiles", JSONObject.create()
                            .setString("hello", "0001-02-03T00:00Z")
                            .setString("there", "0004-05-06T00:00Z"))),
                    Iterable.create(
                        BuildJSONClassFile.create(Path.parse("hello"), DateTime.create(1, 2, 3)),
                        BuildJSONClassFile.create(Path.parse("there"), DateTime.create(4, 5, 6))));
            });

            runner.testGroup("setClassFiles(Iterable<BuildJSONClassFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    test.assertThrows(() -> javaFile.setClassFiles(null),
                        new PreConditionFailure("classFiles cannot be null."));
                    test.assertEqual(Iterable.create(), javaFile.getClassFiles());
                    test.assertEqual(JSONProperty.create("relative/path.java", JSONObject.create()), javaFile.toJson());
                });

                runner.test("with empty", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    final BuildJSONJavaFile setClassFilesResult = javaFile.setClassFiles(Iterable.create());
                    test.assertSame(javaFile, setClassFilesResult);
                    test.assertEqual(Iterable.create(), javaFile.getClassFiles());
                    test.assertEqual(
                        JSONProperty.create("relative/path.java", JSONObject.create()
                            .setObject("classFiles", JSONObject.create())),
                        javaFile.toJson());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final BuildJSONJavaFile javaFile = BuildJSONJavaFile.create("relative/path.java");
                    final BuildJSONJavaFile setClassFilesResult = javaFile.setClassFiles(Iterable.create(
                        BuildJSONClassFile.create(Path.parse("hello"), DateTime.create(1, 2, 3)),
                        BuildJSONClassFile.create(Path.parse("there"), DateTime.create(4, 5, 6))));
                    test.assertSame(javaFile, setClassFilesResult);
                    test.assertEqual(
                        Iterable.create(
                            BuildJSONClassFile.create(Path.parse("hello"), DateTime.create(1, 2, 3)),
                            BuildJSONClassFile.create(Path.parse("there"), DateTime.create(4, 5, 6))),
                        javaFile.getClassFiles());
                    test.assertEqual(
                        JSONProperty.create("relative/path.java", JSONObject.create()
                            .setObject("classFiles", JSONObject.create()
                                .setString("hello", "0001-02-03T00:00Z")
                                .setString("there", "0004-05-06T00:00Z"))),
                        javaFile.toJson());
                });
            });
        });
    }
}
