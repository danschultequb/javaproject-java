package qub;

public interface JavaProjectConfigurationTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectConfiguration.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final JavaProjectConfiguration configuration = JavaProjectConfiguration.create();
                test.assertNotNull(configuration);
                test.assertEqual(JSONObject.create(), configuration.toJson());
                test.assertEqual(Iterable.create(), configuration.getIgnoredStackTraceTypes());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectConfiguration.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create();
                    final JavaProjectConfiguration configuration = JavaProjectConfiguration.create(json);
                    test.assertNotNull(configuration);
                    test.assertSame(json, configuration.toJson());
                    test.assertEqual(Iterable.create(), configuration.getIgnoredStackTraceTypes());
                });
            });

            runner.testGroup("getConfigurationFile(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectConfiguration.getConfigurationFile(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final File configurationFile = JavaProjectConfiguration.getConfigurationFile(process).await();
                    test.assertNotNull(configurationFile);
                    test.assertEqual("/qub/fake-publisher/fake-project/data/configuration.json", configurationFile.toString());
                });
            });

            runner.testGroup("setConfigurationFile(DesktopProcess,JavaProjectConfiguration)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    final JavaProjectConfiguration configuration = JavaProjectConfiguration.create();
                    test.assertThrows(() -> JavaProjectConfiguration.setConfigurationFile(null, configuration),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null configuration",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectConfiguration.setConfigurationFile(process, null),
                        new PreConditionFailure("configuration cannot be null."));
                    test.assertFalse(JavaProjectConfiguration.getConfigurationFile(process).await().exists().await());
                });

                runner.test("with non-null process and configuration",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectConfiguration configuration = JavaProjectConfiguration.create()
                        .setIgnoredStackTraceTypes(Iterable.create("a", "b"));
                    
                    final Void setConfigurationFileResult = JavaProjectConfiguration.setConfigurationFile(process, configuration).await();
                    test.assertNull(setConfigurationFileResult);

                    test.assertEqual(configuration, JavaProjectConfiguration.parse(process).await());
                });
            });

            runner.testGroup("parse(DesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectConfiguration.parse((DesktopProcess)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-existing configuration file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> JavaProjectConfiguration.parse(process).await(),
                        new FileNotFoundException("/qub/fake-publisher/fake-project/data/configuration.json"));
                });

                runner.test("with existing configuration file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectConfiguration configuration = JavaProjectConfiguration.create();
                    configuration.toJson().setString("a", "b");
                    configuration.setIgnoredStackTraceTypes(Iterable.create("c", "d"));
                    JavaProjectConfiguration.setConfigurationFile(process, configuration).await();

                    final JavaProjectConfiguration parseResult = JavaProjectConfiguration.parse(process).await();
                    test.assertEqual(configuration, parseResult);
                });
            });

            runner.testGroup("getIgnoredStackTraceTypes()", () ->
            {
                final Action2<JavaProjectConfiguration,Iterable<String>> getIgnoredStackTraceTypesTest = (JavaProjectConfiguration configuration, Iterable<String> expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getIgnoredStackTraceTypes());
                    });
                };

                getIgnoredStackTraceTypesTest.run(
                    JavaProjectConfiguration.create(),
                    Iterable.create());
                getIgnoredStackTraceTypesTest.run(
                    JavaProjectConfiguration.create()
                        .setIgnoredStackTraceTypes(Iterable.create()),
                    Iterable.create());
                getIgnoredStackTraceTypesTest.run(
                    JavaProjectConfiguration.create()
                        .setIgnoredStackTraceTypes(Iterable.create(
                            "a")),
                    Iterable.create("a"));
            });
        });
    }
}
