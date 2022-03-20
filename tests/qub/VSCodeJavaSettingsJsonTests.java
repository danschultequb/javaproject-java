package qub;

public interface VSCodeJavaSettingsJsonTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(VSCodeJavaSettingsJson.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                test.assertNotNull(settingsJson);
                test.assertEqual(JSONObject.create(), settingsJson.toJson());
                test.assertEqual(Iterable.create(), settingsJson.getJavaProjectSourcePaths());
                test.assertEqual(Iterable.create(), settingsJson.getJavaProjectReferencedLibraries());
                test.assertNull(settingsJson.getJavaFormatSettingsUrl());
                test.assertNull(settingsJson.getJavaFormatEnabled());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> VSCodeJavaSettingsJson.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create();
                    final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create(json);
                    test.assertNotNull(settingsJson);
                    test.assertSame(json, settingsJson.toJson());
                    test.assertEqual(Iterable.create(), settingsJson.getJavaProjectSourcePaths());
                    test.assertEqual(Iterable.create(), settingsJson.getJavaProjectReferencedLibraries());
                    test.assertNull(settingsJson.getJavaFormatSettingsUrl());
                    test.assertNull(settingsJson.getJavaFormatEnabled());
                });
            });

            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> VSCodeJavaSettingsJson.parse(null),
                        new PreConditionFailure("file cannot be null."));
                });
            });

            runner.testGroup("setJavaProjectSourcePaths(Iterable<String>)", () ->
            {
                final Action2<Iterable<String>,Throwable> setJavaProjectSourcePathsErrorTest = (Iterable<String> sourcePaths, Throwable expected) ->
                {
                    runner.test("with " + sourcePaths, (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        test.assertThrows(() -> settingsJson.setJavaProjectSourcePaths(sourcePaths),
                            expected);
                        test.assertEqual(Iterable.create(), settingsJson.getJavaProjectSourcePaths());
                    });
                };

                setJavaProjectSourcePathsErrorTest.run(null, new PreConditionFailure("javaProjectSourcePaths cannot be null."));

                final Action1<Iterable<String>> setJavaProjectSourcePathsTest = (Iterable<String> javaProjectSourcePaths) ->
                {
                    runner.test("with " + javaProjectSourcePaths, (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        final VSCodeJavaSettingsJson setJavaProjectSourcePathsResult = settingsJson.setJavaProjectSourcePaths(javaProjectSourcePaths);
                        test.assertSame(settingsJson, setJavaProjectSourcePathsResult);
                        test.assertEqual(javaProjectSourcePaths, settingsJson.getJavaProjectSourcePaths());
                        test.assertEqual(
                            JSONObject.create()
                                .setArray("java.project.sourcePaths", JSONArray.create(javaProjectSourcePaths.map(JSONString::get))),
                            settingsJson.toJson());
                    });
                };
                
                setJavaProjectSourcePathsTest.run(Iterable.create());
                setJavaProjectSourcePathsTest.run(Iterable.create("a"));
                setJavaProjectSourcePathsTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("setJavaProjectReferencedLibraries(Iterable<String>)", () ->
            {
                final Action2<Iterable<String>,Throwable> setJavaProjectReferencedLibrariesErrorTest = (Iterable<String> javaProjectReferencedLibraries, Throwable expected) ->
                {
                    runner.test("with " + javaProjectReferencedLibraries, (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        test.assertThrows(() -> settingsJson.setJavaProjectReferencedLibraries(javaProjectReferencedLibraries),
                            expected);
                        test.assertEqual(Iterable.create(), settingsJson.getJavaProjectReferencedLibraries());
                    });
                };

                setJavaProjectReferencedLibrariesErrorTest.run(null, new PreConditionFailure("javaProjectReferencedLibraries cannot be null."));

                final Action1<Iterable<String>> setJavaProjectReferencedLibrariesTest = (Iterable<String> javaProjectReferencedLibraries) ->
                {
                    runner.test("with " + javaProjectReferencedLibraries, (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        final VSCodeJavaSettingsJson setJavaProjectReferencedLibrariesResult = settingsJson.setJavaProjectReferencedLibraries(javaProjectReferencedLibraries);
                        test.assertSame(settingsJson, setJavaProjectReferencedLibrariesResult);
                        test.assertEqual(javaProjectReferencedLibraries, settingsJson.getJavaProjectReferencedLibraries());
                        test.assertEqual(
                            JSONObject.create()
                                .setArray("java.project.referencedLibraries", JSONArray.create(javaProjectReferencedLibraries.map(JSONString::get))),
                            settingsJson.toJson());
                    });
                };
                
                setJavaProjectReferencedLibrariesTest.run(Iterable.create());
                setJavaProjectReferencedLibrariesTest.run(Iterable.create("a"));
                setJavaProjectReferencedLibrariesTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("setJavaFormatSettingsUrl(String)", () ->
            {
                final Action2<String,Throwable> setJavaFormatSettingsUrlErrorTest = (String javaFormatSettingsUrl, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javaFormatSettingsUrl), (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        test.assertThrows(() -> settingsJson.setJavaFormatSettingsUrl(javaFormatSettingsUrl),
                            expected);
                        test.assertNull(settingsJson.getJavaFormatSettingsUrl());
                    });
                };

                setJavaFormatSettingsUrlErrorTest.run(null, new PreConditionFailure("javaFormatSettingsUrl cannot be null."));

                final Action1<String> setJavaFormatSettingsUrlTest = (String javaFormatSettingsUrl) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(javaFormatSettingsUrl), (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        final VSCodeJavaSettingsJson setJavaFormatSettingsUrlResult = settingsJson.setJavaFormatSettingsUrl(javaFormatSettingsUrl);
                        test.assertSame(settingsJson, setJavaFormatSettingsUrlResult);
                        test.assertEqual(javaFormatSettingsUrl, settingsJson.getJavaFormatSettingsUrl());
                        test.assertEqual(
                            JSONObject.create()
                                .setString("java.format.settings.url", javaFormatSettingsUrl),
                            settingsJson.toJson());
                    });
                };
                
                setJavaFormatSettingsUrlTest.run("");
                setJavaFormatSettingsUrlTest.run("a");
                setJavaFormatSettingsUrlTest.run("hello there");
            });

            runner.testGroup("setJavaFormatSettingsUrl(String)", () ->
            {
                final Action1<Boolean> setJavaFormatSettingsUrlErrorTest = (Boolean javaFormatEnabled) ->
                {
                    runner.test("with " + javaFormatEnabled, (Test test) ->
                    {
                        final VSCodeJavaSettingsJson settingsJson = VSCodeJavaSettingsJson.create();
                        final VSCodeJavaSettingsJson setJavaFormatEnabledResult = settingsJson.setJavaFormatEnabled(javaFormatEnabled.booleanValue());
                        test.assertSame(settingsJson, setJavaFormatEnabledResult);
                        test.assertEqual(javaFormatEnabled, settingsJson.getJavaFormatEnabled());
                    });
                };

                setJavaFormatSettingsUrlErrorTest.run(false);
                setJavaFormatSettingsUrlErrorTest.run(true);
            });
        });
    }
}
