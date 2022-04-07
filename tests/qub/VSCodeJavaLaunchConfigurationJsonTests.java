package qub;

public interface VSCodeJavaLaunchConfigurationJsonTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(VSCodeJavaLaunchConfigurationJson.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                test.assertNotNull(configuration);
                test.assertEqual(JSONObject.create(), configuration.toJson());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> VSCodeJavaLaunchConfigurationJson.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create();
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create(json);
                    test.assertNotNull(configuration);
                    test.assertSame(json, configuration.toJson());
                });
            });

            runner.testGroup("getMainClass()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getMainClassTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getMainClass());
                    });
                };

                getMainClassTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getMainClassTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("mainClass", "")),
                    "");
                getMainClassTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("mainClass", "a")),
                    "a");
                getMainClassTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setMainClass(""),
                    "");
                getMainClassTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setMainClass("a"),
                    "a");
            });

            runner.testGroup("setMainClass(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setMainClass(null),
                        new PreConditionFailure("mainClass cannot be null."));
                    test.assertNull(configuration.getMainClass());
                });

                final Action1<String> setMainClassTest = (String mainClass) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(mainClass), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setMainClassResult = configuration.setMainClass(mainClass);
                        test.assertSame(configuration, setMainClassResult);
                        test.assertEqual(mainClass, configuration.getMainClass());
                    });
                };

                setMainClassTest.run("");
                setMainClassTest.run("a");
                setMainClassTest.run("qub.JavaProject");
                setMainClassTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getArgs()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getArgsTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getArgs());
                    });
                };

                getArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("args", "")),
                    "");
                getArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("args", "a")),
                    "a");
                getArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setArgs(""),
                    "");
                getArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setArgs("a"),
                    "a");
            });

            runner.testGroup("setArgs(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setArgs(null),
                        new PreConditionFailure("args cannot be null."));
                    test.assertNull(configuration.getArgs());
                });

                final Action1<String> setArgsTest = (String args) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(args), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setArgsResult = configuration.setArgs(args);
                        test.assertSame(configuration, setArgsResult);
                        test.assertEqual(args, configuration.getArgs());
                    });
                };

                setArgsTest.run("");
                setArgsTest.run("a");
                setArgsTest.run("qub.JavaProject");
                setArgsTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getClassPaths()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,Iterable<String>> getClassPathsTest = (VSCodeJavaLaunchConfigurationJson configuration, Iterable<String> expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getClassPaths());
                    });
                };

                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    Iterable.create());
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("classPaths", JSONArray.create())),
                    Iterable.create());
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("classPaths", JSONArray.create(
                            JSONString.get("a")))),
                    Iterable.create("a"));
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("classPaths", JSONArray.create(
                            JSONString.get("a"),
                            JSONString.get("b")))),
                    Iterable.create("a", "b"));
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setClassPaths(Iterable.create()),
                    Iterable.create());
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setClassPaths(Iterable.create("a")),
                    Iterable.create("a"));
                getClassPathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setClassPaths(Iterable.create("a", "b")),
                    Iterable.create("a", "b"));
            });

            runner.testGroup("setClassPaths(Iterable<String>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setClassPaths(null),
                        new PreConditionFailure("classPaths cannot be null."));
                    test.assertEqual(Iterable.create(), configuration.getClassPaths());
                });

                final Action1<Iterable<String>> setClassPathsTest = (Iterable<String> classPaths) ->
                {
                    runner.test("with " + classPaths.toString(), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setClassPathsResult = configuration.setClassPaths(classPaths);
                        test.assertSame(configuration, setClassPathsResult);
                        test.assertEqual(classPaths, configuration.getClassPaths());
                    });
                };

                setClassPathsTest.run(Iterable.create());
                setClassPathsTest.run(Iterable.create("a"));
                setClassPathsTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("getConsole()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getConsoleTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getConsole());
                    });
                };

                getConsoleTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getConsoleTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("console", "")),
                    "");
                getConsoleTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("console", "a")),
                    "a");
                getConsoleTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setConsole(""),
                    "");
                getConsoleTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setConsole("a"),
                    "a");
            });

            runner.testGroup("setConsole(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setConsole(null),
                        new PreConditionFailure("console cannot be null."));
                    test.assertNull(configuration.getConsole());
                });

                final Action1<String> setConsoleTest = (String console) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(console), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setConsoleResult = configuration.setConsole(console);
                        test.assertSame(configuration, setConsoleResult);
                        test.assertEqual(console, configuration.getConsole());
                    });
                };

                setConsoleTest.run("");
                setConsoleTest.run("a");
                setConsoleTest.run("qub.JavaProject");
                setConsoleTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getEncoding()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getEncodingTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getEncoding());
                    });
                };

                getEncodingTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getEncodingTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("encoding", "")),
                    "");
                getEncodingTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("encoding", "a")),
                    "a");
                getEncodingTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setEncoding(""),
                    "");
                getEncodingTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setEncoding("a"),
                    "a");
            });

            runner.testGroup("setEncoding(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setEncoding(null),
                        new PreConditionFailure("encoding cannot be null."));
                    test.assertNull(configuration.getEncoding());
                });

                final Action1<String> setEncodingTest = (String encoding) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(encoding), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setEncodingResult = configuration.setEncoding(encoding);
                        test.assertSame(configuration, setEncodingResult);
                        test.assertEqual(encoding, configuration.getEncoding());
                    });
                };

                setEncodingTest.run("");
                setEncodingTest.run("a");
                setEncodingTest.run("qub.JavaProject");
                setEncodingTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getModulePaths()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,Iterable<String>> getModulePathsTest = (VSCodeJavaLaunchConfigurationJson configuration, Iterable<String> expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getModulePaths());
                    });
                };

                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    Iterable.create());
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("modulePaths", JSONArray.create())),
                    Iterable.create());
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("modulePaths", JSONArray.create(
                            JSONString.get("a")))),
                    Iterable.create("a"));
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("modulePaths", JSONArray.create(
                            JSONString.get("a"),
                            JSONString.get("b")))),
                    Iterable.create("a", "b"));
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setModulePaths(Iterable.create()),
                    Iterable.create());
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setModulePaths(Iterable.create("a")),
                    Iterable.create("a"));
                getModulePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setModulePaths(Iterable.create("a", "b")),
                    Iterable.create("a", "b"));
            });

            runner.testGroup("setModulePaths(Iterable<String>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setModulePaths(null),
                        new PreConditionFailure("modulePaths cannot be null."));
                    test.assertEqual(Iterable.create(), configuration.getModulePaths());
                });

                final Action1<Iterable<String>> setModulePathsTest = (Iterable<String> modulePaths) ->
                {
                    runner.test("with " + modulePaths.toString(), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setModulePathsResult = configuration.setModulePaths(modulePaths);
                        test.assertSame(configuration, setModulePathsResult);
                        test.assertEqual(modulePaths, configuration.getModulePaths());
                    });
                };

                setModulePathsTest.run(Iterable.create());
                setModulePathsTest.run(Iterable.create("a"));
                setModulePathsTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("getProjectName()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getProjectNameTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getProjectName());
                    });
                };

                getProjectNameTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getProjectNameTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("projectName", "")),
                    "");
                getProjectNameTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("projectName", "a")),
                    "a");
                getProjectNameTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setProjectName(""),
                    "");
                getProjectNameTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setProjectName("a"),
                    "a");
            });

            runner.testGroup("setProjectName(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setProjectName(null),
                        new PreConditionFailure("projectName cannot be null."));
                    test.assertNull(configuration.getProjectName());
                });

                final Action1<String> setProjectNameTest = (String projectName) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(projectName), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setProjectNameResult = configuration.setProjectName(projectName);
                        test.assertSame(configuration, setProjectNameResult);
                        test.assertEqual(projectName, configuration.getProjectName());
                    });
                };

                setProjectNameTest.run("");
                setProjectNameTest.run("a");
                setProjectNameTest.run("qub.JavaProject");
                setProjectNameTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getShortenCommandLine()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getShortenCommandLineTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getShortenCommandLine());
                    });
                };

                getShortenCommandLineTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getShortenCommandLineTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("shortenCommandLine", "")),
                    "");
                getShortenCommandLineTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("shortenCommandLine", "a")),
                    "a");
                getShortenCommandLineTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setShortenCommandLine(""),
                    "");
                getShortenCommandLineTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setShortenCommandLine("a"),
                    "a");
            });

            runner.testGroup("setShortenCommandLine(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setShortenCommandLine(null),
                        new PreConditionFailure("shortenCommandLine cannot be null."));
                    test.assertNull(configuration.getShortenCommandLine());
                });

                final Action1<String> setShortenCommandLineTest = (String shortenCommandLine) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(shortenCommandLine), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setShortenCommandLineResult = configuration.setShortenCommandLine(shortenCommandLine);
                        test.assertSame(configuration, setShortenCommandLineResult);
                        test.assertEqual(shortenCommandLine, configuration.getShortenCommandLine());
                    });
                };

                setShortenCommandLineTest.run("");
                setShortenCommandLineTest.run("a");
                setShortenCommandLineTest.run("qub.JavaProject");
                setShortenCommandLineTest.run("qub.JavaProjectTest");
            });

            runner.testGroup("getSourcePaths()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,Iterable<String>> getSourcePathsTest = (VSCodeJavaLaunchConfigurationJson configuration, Iterable<String> expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getSourcePaths());
                    });
                };

                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    Iterable.create());
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("sourcePaths", JSONArray.create())),
                    Iterable.create());
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("sourcePaths", JSONArray.create(
                            JSONString.get("a")))),
                    Iterable.create("a"));
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("sourcePaths", JSONArray.create(
                            JSONString.get("a"),
                            JSONString.get("b")))),
                    Iterable.create("a", "b"));
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setSourcePaths(Iterable.create()),
                    Iterable.create());
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setSourcePaths(Iterable.create("a")),
                    Iterable.create("a"));
                getSourcePathsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setSourcePaths(Iterable.create("a", "b")),
                    Iterable.create("a", "b"));
            });

            runner.testGroup("setSourcePaths(Iterable<String>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setSourcePaths(null),
                        new PreConditionFailure("sourcePaths cannot be null."));
                    test.assertEqual(Iterable.create(), configuration.getSourcePaths());
                });

                final Action1<Iterable<String>> setSourcePathsTest = (Iterable<String> sourcePaths) ->
                {
                    runner.test("with " + sourcePaths.toString(), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setSourcePathsResult = configuration.setSourcePaths(sourcePaths);
                        test.assertSame(configuration, setSourcePathsResult);
                        test.assertEqual(sourcePaths, configuration.getSourcePaths());
                    });
                };

                setSourcePathsTest.run(Iterable.create());
                setSourcePathsTest.run(Iterable.create("a"));
                setSourcePathsTest.run(Iterable.create("a", "b"));
            });

            runner.testGroup("getStepFilters()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,VSCodeJavaLaunchConfigurationStepFiltersJson> getStepFiltersTest = (VSCodeJavaLaunchConfigurationJson configuration, VSCodeJavaLaunchConfigurationStepFiltersJson expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getStepFilters());
                    });
                };

                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setBoolean("stepFilters", false)),
                    null);
                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setObject("stepFilters", JSONObject.create())),
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create());
                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setObject("stepFilters", JSONObject.create()
                            .setArray("skipClasses", JSONArray.create()))),
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create()));
                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setStepFilters(VSCodeJavaLaunchConfigurationStepFiltersJson.create()),
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create());
                getStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setStepFilters(VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                            .setSkipClasses(Iterable.create("a", "b"))),
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create("a", "b")));
            });

            runner.testGroup("setStepFilters(VSCodeJavaLaunchConfigurationStepFiltersJson)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setStepFilters(null),
                        new PreConditionFailure("stepFilters cannot be null."));
                    test.assertNull(configuration.getStepFilters());
                });

                final Action1<VSCodeJavaLaunchConfigurationStepFiltersJson> setStepFiltersTest = (VSCodeJavaLaunchConfigurationStepFiltersJson stepFilters) ->
                {
                    runner.test("with " + stepFilters.toString(), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setStepFiltersResult = configuration.setStepFilters(stepFilters);
                        test.assertSame(configuration, setStepFiltersResult);
                        test.assertEqual(stepFilters, configuration.getStepFilters());
                    });
                };

                setStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create());
                setStepFiltersTest.run(
                    VSCodeJavaLaunchConfigurationStepFiltersJson.create()
                        .setSkipClasses(Iterable.create("qub.Result", "qub.LazyResult")));
            });

            runner.testGroup("getStopOnEntry()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,Boolean> getStopOnEntryTest = (VSCodeJavaLaunchConfigurationJson configuration, Boolean expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getStopOnEntry());
                    });
                };

                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setArray("stopOnEntry", JSONArray.create())),
                    null);
                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setBoolean("stopOnEntry", false)),
                    false);
                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setBoolean("stopOnEntry", true)),
                    true);
                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setStopOnEntry(false),
                    false);
                getStopOnEntryTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setStopOnEntry(true),
                    true);
            });

            runner.testGroup("setStopOnEntry(boolean)", () ->
            {
                final Action1<Boolean> setStopOnEntryTest = (Boolean stopOnEntry) ->
                {
                    runner.test("with " + stopOnEntry, (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setStopOnEntryResult = configuration.setStopOnEntry(stopOnEntry);
                        test.assertSame(configuration, setStopOnEntryResult);
                        test.assertEqual(stopOnEntry, configuration.getStopOnEntry());
                    });
                };

                setStopOnEntryTest.run(false);
                setStopOnEntryTest.run(true);
            });

            runner.testGroup("getVmArgs()", () ->
            {
                final Action2<VSCodeJavaLaunchConfigurationJson,String> getVmArgsTest = (VSCodeJavaLaunchConfigurationJson configuration, String expected) ->
                {
                    runner.test("with " + configuration.toString(), (Test test) ->
                    {
                        test.assertEqual(expected, configuration.getVmArgs());
                    });
                };

                getVmArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(),
                    null);
                getVmArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("vmArgs", "")),
                    "");
                getVmArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create(JSONObject.create()
                        .setString("vmArgs", "a")),
                    "a");
                getVmArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setVmArgs(""),
                    "");
                getVmArgsTest.run(
                    VSCodeJavaLaunchConfigurationJson.create()
                        .setVmArgs("a"),
                    "a");
            });

            runner.testGroup("setVmArgs(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                    test.assertThrows(() -> configuration.setVmArgs(null),
                        new PreConditionFailure("vmArgs cannot be null."));
                    test.assertNull(configuration.getVmArgs());
                });

                final Action1<String> setVmArgsTest = (String vmArgs) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(vmArgs), (Test test) ->
                    {
                        final VSCodeJavaLaunchConfigurationJson configuration = VSCodeJavaLaunchConfigurationJson.create();
                        final VSCodeJavaLaunchConfigurationJson setVmArgsResult = configuration.setVmArgs(vmArgs);
                        test.assertSame(configuration, setVmArgsResult);
                        test.assertEqual(vmArgs, configuration.getVmArgs());
                    });
                };

                setVmArgsTest.run("");
                setVmArgsTest.run("a");
                setVmArgsTest.run("qub.JavaProject");
                setVmArgsTest.run("qub.JavaProjectTest");
            });
        });
    }
}
