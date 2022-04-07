package qub;

public interface VSCodeJavaLaunchConfigurationStepFiltersJsonTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(VSCodeJavaLaunchConfigurationStepFiltersJson.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                test.assertNotNull(filters);
                test.assertEqual(JSONObject.create(), filters.toJson());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> VSCodeJavaLaunchConfigurationStepFiltersJson.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create();
                    final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create(json);
                    test.assertNotNull(filters);
                    test.assertSame(json, filters.toJson());
                });
            });

            runner.testGroup("getSkipClasses()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationStepFiltersJson,Iterable<String>> getSkipClassesTest = (VSCodeJavaLaunchConfigurationStepFiltersJson filters, Iterable<String> expected) ->
                {
                    runner.test("with " + filters.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, filters.getSkipClasses());
                    });
                };

                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(),
                    Iterable.create());
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipClasses", JSONArray.create())),
                    Iterable.create());
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipClasses", JSONArray.create(
                            JSONString.get("a")))),
                    Iterable.create("a"));
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipClasses", JSONArray.create(
                            JSONString.get("a"),
                            JSONString.get("b")))),
                    Iterable.create("a", "b"));
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create()),
                    Iterable.create());
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create("a")),
                    Iterable.create("a"));
                getSkipClassesTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create("a", "b")),
                    Iterable.create("a", "b"));
            });

            runner.testGroup("setSkipClasses(Iterable<String>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                    test.assertThrows(() -> filters.setSkipClasses(null),
                        new PreConditionFailure("skipClasses cannot be null."));
                    test.assertEqual(Iterable.create(), filters.getSkipClasses());
                });

                final Action1<Iterable<String>> setSkipClassesTest = (Iterable<String> skipClasses) ->
                {
                    runner.test("with " + skipClasses.toString(), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                        final VSCodeJavaLaunchConfigurationStepFiltersJson setSkipClassesResult = filters.setSkipClasses(skipClasses);
                        test.assertSame(filters, setSkipClassesResult);
                        test.assertEqual(skipClasses, filters.getSkipClasses());
                    });
                };

                setSkipClassesTest.run(Iterable.create());
                setSkipClassesTest.run(Iterable.create("a"));
                setSkipClassesTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("getSkipSynthetics()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationStepFiltersJson,Boolean> getSkipSyntheticsTest = (VSCodeJavaLaunchConfigurationStepFiltersJson filters, Boolean expected) ->
                {
                    runner.test("with " + filters.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, filters.getSkipSynthetics());
                    });
                };

                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(),
                    null);
                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipSynthetics", JSONArray.create())),
                    null);
                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipSynthetics", false)),
                    false);
                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipSynthetics", true)),
                    true);
                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipSynthetics(false),
                    false);
                getSkipSyntheticsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipSynthetics(true),
                    true);
            });

            runner.testGroup("setSkipSynthetics(boolean)", () ->
            {
                final Action1<Boolean> setSkipSyntheticsTest = (Boolean skipSynthetics) ->
                {
                    runner.test("with " + skipSynthetics, (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                        final VSCodeJavaLaunchConfigurationStepFiltersJson setSkipSyntheticsResult = filters.setSkipSynthetics(skipSynthetics);
                        test.assertSame(filters, setSkipSyntheticsResult);
                        test.assertEqual(skipSynthetics, filters.getSkipSynthetics());
                    });
                };

                setSkipSyntheticsTest.run(false);
                setSkipSyntheticsTest.run(true);
            });

            runner.testGroup("getSkipStaticInitializers()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationStepFiltersJson,Boolean> getSkipStaticInitializersTest = (VSCodeJavaLaunchConfigurationStepFiltersJson filters, Boolean expected) ->
                {
                    runner.test("with " + filters.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, filters.getSkipStaticInitializers());
                    });
                };

                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(),
                    null);
                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipStaticInitializers", JSONArray.create())),
                    null);
                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipStaticInitializers", false)),
                    false);
                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipStaticInitializers", true)),
                    true);
                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipStaticInitializers(false),
                    false);
                getSkipStaticInitializersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipStaticInitializers(true),
                    true);
            });

            runner.testGroup("setSkipStaticInitializers(boolean)", () ->
            {
                final Action1<Boolean> setSkipStaticInitializersTest = (Boolean skipStaticInitializers) ->
                {
                    runner.test("with " + skipStaticInitializers, (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                        final VSCodeJavaLaunchConfigurationStepFiltersJson setSkipStaticInitializersResult = filters.setSkipStaticInitializers(skipStaticInitializers);
                        test.assertSame(filters, setSkipStaticInitializersResult);
                        test.assertEqual(skipStaticInitializers, filters.getSkipStaticInitializers());
                    });
                };

                setSkipStaticInitializersTest.run(false);
                setSkipStaticInitializersTest.run(true);
            });

            runner.testGroup("getSkipConstructors()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationStepFiltersJson,Boolean> getSkipConstructorsTest = (VSCodeJavaLaunchConfigurationStepFiltersJson filters, Boolean expected) ->
                {
                    runner.test("with " + filters.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, filters.getSkipConstructors());
                    });
                };

                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(),
                    null);
                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setArray("skipConstructors", JSONArray.create())),
                    null);
                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipConstructors", false)),
                    false);
                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create(JSONObject.create()
                        .setBoolean("skipConstructors", true)),
                    true);
                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipConstructors(false),
                    false);
                getSkipConstructorsTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipConstructors(true),
                    true);
            });

            runner.testGroup("setSkipConstructors(boolean)", () ->
            {
                final Action1<Boolean> setSkipConstructorsTest = (Boolean skipConstructors) ->
                {
                    runner.test("with " + skipConstructors, (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationStepFiltersJson filters = VSCodeJavaLaunchConfigurationStepFiltersJson.create();
                        final VSCodeJavaLaunchConfigurationStepFiltersJson setSkipConstructorsResult = filters.setSkipConstructors(skipConstructors);
                        test.assertSame(filters, setSkipConstructorsResult);
                        test.assertEqual(skipConstructors, filters.getSkipConstructors());
                    });
                };

                setSkipConstructorsTest.run(false);
                setSkipConstructorsTest.run(true);
            });
        });
    }
}
