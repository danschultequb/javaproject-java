package qub;

public interface JavaProjectCreateTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectCreate.class, () ->
        {
            runner.testGroup("addAction(CommandLineActions)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectCreate.addAction(null),
                        new PreConditionFailure("actions cannot be null."));
                });
            });
            
            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null process", (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final DesktopProcess nullProcess = null;
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");
                    test.assertThrows(() -> JavaProjectCreate.run(nullProcess, action),
                        new PreConditionFailure("process cannot be null."));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with null action", (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineAction action = null;
                    test.assertThrows(() -> JavaProjectCreate.run(process, action),
                        new PreConditionFailure("action cannot be null."));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with \"--help\"",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--help")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: qub fake-project fake-action [[--projectFolder=]<projectFolder-value>] [--help] [--verbose]",
                            "  Fake action description",
                            "  --projectFolder: The folder that the new Java project will be created in.",
                            "  --help(?):       Show the help message for this application.",
                            "  --verbose(v):    Whether or not to show verbose logs."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder();
                    test.assertEqual(
                        Iterable.create(),
                        projectFolder.iterateEntriesRecursively().toList());

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with " + English.andList("no arguments", "no existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.setDefaultCurrentFolder("/my-project/");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .addArguments("init", "/my-project/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Creating Java project \"qub/my-project@1\" in /my-project/... Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/my-project"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"my-project\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/my-project@1\" in /my-project/... ",
                            "VERBOSE:   Creating /my-project/project.json... Done.",
                            "VERBOSE:   Creating /my-project/README.md... Done.",
                            "VERBOSE:   Creating /my-project/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("no arguments", "existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    process.setDefaultCurrentFolder("/my-project/");
                    final Folder projectFolder = process.getCurrentFolder();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("relative path argument", "no existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("relative/path")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.setDefaultCurrentFolder("/my-project/");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .addArguments("init", "/my-project/relative/path/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Creating Java project \"qub/path@1\" in /my-project/relative/path/... Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder().getFolder("relative/path").await();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/path"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"path\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /my-project/relative/path/... ",
                            "VERBOSE:   Creating /my-project/relative/path/project.json... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/README.md... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("relative path argument", "existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("relative/path")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    process.setDefaultCurrentFolder("/my-project/");
                    final Folder projectFolder = process.getCurrentFolder().getFolder("relative/path/").await();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/relative/path/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/relative/path/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("rooted path argument", "no existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/rooted/path")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .addArguments("init", "/rooted/path/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Creating Java project \"qub/path@1\" in /rooted/path/... Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getFileSystem().getFolder("/rooted/path").await();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/path"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"path\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /rooted/path/... ",
                            "VERBOSE:   Creating /rooted/path/project.json... Done.",
                            "VERBOSE:   Creating /rooted/path/README.md... Done.",
                            "VERBOSE:   Creating /rooted/path/LICENSE... Done.",
                            "VERBOSE:   Creating /rooted/path/.gitignore... Done.",
                            "VERBOSE:   Creating /rooted/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /rooted/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("rooted path argument", "existing project.json file"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("rooted/path")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    final Folder projectFolder = process.getFileSystem().getFolder("/rooted/path").await();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/rooted/path/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/rooted/path/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("no arguments", "no existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.setDefaultCurrentFolder("/my-project/");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .addArguments("init", "/my-project/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/my-project@1\" in /my-project/... ",
                            "VERBOSE:   Creating /my-project/project.json... Done.",
                            "VERBOSE:   Creating /my-project/README.md... Done.",
                            "VERBOSE:   Creating /my-project/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/my-project"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"my-project\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/my-project@1\" in /my-project/... ",
                            "VERBOSE:   Creating /my-project/project.json... Done.",
                            "VERBOSE:   Creating /my-project/README.md... Done.",
                            "VERBOSE:   Creating /my-project/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("no arguments", "existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    process.setDefaultCurrentFolder("/my-project/");
                    final Folder projectFolder = process.getCurrentFolder();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("relative path argument", "no existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("relative/path", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.setDefaultCurrentFolder("/my-project/");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .addArguments("init", "/my-project/relative/path/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /my-project/relative/path/... ",
                            "VERBOSE:   Creating /my-project/relative/path/project.json... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/README.md... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getCurrentFolder().getFolder("relative/path").await();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/path"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"path\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /my-project/relative/path/... ",
                            "VERBOSE:   Creating /my-project/relative/path/project.json... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/README.md... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/LICENSE... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/.gitignore... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /my-project/relative/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("relative path argument", "existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("relative/path", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    process.setDefaultCurrentFolder("/my-project/");
                    final Folder projectFolder = process.getCurrentFolder().getFolder("relative/path/").await();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/relative/path/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/my-project/relative/path/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("rooted path argument", "no existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("/rooted/path", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    process.getProcessFactory().add(FakeProcessRun.get("git")
                        .setWorkingFolder("/")
                        .addArguments("init", "/rooted/path/"));

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /rooted/path/... ",
                            "VERBOSE:   Creating /rooted/path/project.json... Done.",
                            "VERBOSE:   Creating /rooted/path/README.md... Done.",
                            "VERBOSE:   Creating /rooted/path/LICENSE... Done.",
                            "VERBOSE:   Creating /rooted/path/.gitignore... Done.",
                            "VERBOSE:   Creating /rooted/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /rooted/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, process.getExitCode());

                    final Folder projectFolder = process.getFileSystem().getFolder("/rooted/path").await();
                    final Folder sourcesFolder = projectFolder.getFolder("sources").await();
                    final Folder testsFolder = projectFolder.getFolder("tests").await();
                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    final File projectJsonFile = projectFolder.getFile("project.json").await();
                    final Folder sourcesQubFolder = sourcesFolder.getFolder("qub").await();
                    final Folder testsQubFolder = testsFolder.getFolder("qub").await();
                    test.assertEqual(
                        Iterable.create(
                            sourcesFolder,
                            testsFolder,
                            gitIgnoreFile,
                            licenseFile,
                            readmeMdFile,
                            projectJsonFile,
                            sourcesQubFolder,
                            testsQubFolder),
                        projectFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target"),
                        Strings.getLines(gitIgnoreFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " danschultequb",
                            "",
                            "Permission is hereby granted, free of charge, to any person obtaining a copy",
                            "of this software and associated documentation files (the \"Software\"), to deal",
                            "in the Software without restriction, including without limitation the rights",
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell",
                            "copies of the Software, and to permit persons to whom the Software is",
                            "furnished to do so, subject to the following conditions:",
                            "",
                            "The above copyright notice and this permission notice shall be included in all",
                            "copies or substantial portions of the Software.",
                            "",
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR",
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,",
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE",
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER",
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,",
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE",
                            "SOFTWARE."),
                        Strings.getLines(licenseFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "# qub/path"),
                        Strings.getLines(readmeMdFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"file:////qub/fake-publisher/fake-project/data/javaproject.schema.json\",",
                            "  \"publisher\": \"qub\",",
                            "  \"project\": \"path\",",
                            "  \"version\": \"1\",",
                            "  \"java\": {",
                            "    \"dependencies\": []",
                            "  }",
                            "}"),
                        Strings.getLines(projectJsonFile.getContentsAsString().await()));

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.getFile("javaproject.schema.json").await();
                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(
                            "{",
                            "  \"$schema\": \"http://json-schema.org/draft-04/schema\",",
                            "  \"type\": \"object\",",
                            "  \"properties\": {",
                            "    \"publisher\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The person or organization that owns this project.\"",
                            "    },",
                            "    \"project\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The name of this project.\"",
                            "    },",
                            "    \"version\": {",
                            "      \"type\": \"string\",",
                            "      \"minLength\": 1,",
                            "      \"description\": \"The version of this project.\"",
                            "    },",
                            "    \"java\": {",
                            "      \"type\": \"object\",",
                            "      \"properties\": {",
                            "        \"mainClass\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the type that contains the \\\"main\\\" method for this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"shortcutName\": {",
                            "          \"type\": \"string\",",
                            "          \"minLength\": 1,",
                            "          \"description\": \"The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable.\"",
                            "        },",
                            "        \"dependencies\": {",
                            "          \"type\": \"array\",",
                            "          \"items\": {",
                            "            \"type\": \"object\",",
                            "            \"properties\": {",
                            "              \"publisher\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The person or organization that owns the dependency project.\"",
                            "              },",
                            "              \"project\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The name of the dependency project.\"",
                            "              },",
                            "              \"version\": {",
                            "                \"type\": \"string\",",
                            "                \"minLength\": 1,",
                            "                \"description\": \"The version of the dependency project.\"",
                            "              }",
                            "            },",
                            "            \"additionalProperties\": false,",
                            "            \"required\": [",
                            "              \"publisher\",",
                            "              \"project\",",
                            "              \"version\"",
                            "            ]",
                            "          }",
                            "        }",
                            "      }",
                            "    }",
                            "  },",
                            "  \"additionalProperties\": true,",
                            "  \"required\": [",
                            "    \"publisher\",",
                            "    \"project\",",
                            "    \"version\",",
                            "    \"java\"",
                            "  ]",
                            "}"),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Creating /qub/fake-publisher/fake-project/data/javaproject.schema.json... Done.",
                            "Creating Java project \"qub/path@1\" in /rooted/path/... ",
                            "VERBOSE:   Creating /rooted/path/project.json... Done.",
                            "VERBOSE:   Creating /rooted/path/README.md... Done.",
                            "VERBOSE:   Creating /rooted/path/LICENSE... Done.",
                            "VERBOSE:   Creating /rooted/path/.gitignore... Done.",
                            "VERBOSE:   Creating /rooted/path/sources/qub/... Done.",
                            "VERBOSE:   Creating /rooted/path/tests/qub/... Done.",
                            "VERBOSE:   Initializing Git repository... Done.",
                            "Done."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });

                runner.test("with " + English.andList("rooted path argument", "existing project.json file", "verbose logs"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("rooted/path", "--verbose")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final CommandLineActions actions = process.createCommandLineActions()
                        .setApplicationName("qub fake-project");
                    final CommandLineAction action = actions.addAction("fake-action", (DesktopProcess actionProcess) -> {})
                        .setDescription("Fake action description");

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubPublisherFolder fakePublisherFolder = qubFolder.getPublisherFolder("fake-publisher").await();
                    final QubProjectFolder fakeProjectFolder = fakePublisherFolder.getProjectFolder("fake-project").await();
                    final Folder fakeProjectDataFolder = fakeProjectFolder.getProjectDataFolder().await();
                    final File javaProjectSchemaJsonFile = fakeProjectDataFolder.createFile("javaproject.schema.json").await();

                    final Folder projectFolder = process.getFileSystem().getFolder("/rooted/path").await();
                    final File projectJsonFile = projectFolder.createFile("project.json").await();

                    JavaProjectCreate.run(process, action);

                    test.assertLinesEqual(
                        Iterable.create(
                            "A project already exists in folder \"/rooted/path/\"."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(-1, process.getExitCode());

                    test.assertEqual(
                        Iterable.create(
                            projectJsonFile),
                        projectFolder.iterateEntriesRecursively().toList());

                    final Folder fakeProjectLogsFolder = fakeProjectDataFolder.getFolder("logs").await();
                    final File fakeProjectLogsFile = fakeProjectLogsFolder.getFile("1.log").await();
                    final QubProjectVersionFolder fakeProjectVersionFolder = fakeProjectFolder.getProjectVersionFolder("8").await();
                    test.assertEqual(
                        Iterable.create(
                            fakePublisherFolder,
                            fakeProjectFolder,
                            fakeProjectDataFolder,
                            fakeProjectFolder.getProjectVersionsFolder().await(),
                            fakeProjectLogsFolder,
                            javaProjectSchemaJsonFile,
                            fakeProjectLogsFile,
                            fakeProjectVersionFolder,
                            fakeProjectVersionFolder.getCompiledSourcesFile().await()),
                        qubFolder.iterateEntriesRecursively().toList());
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(javaProjectSchemaJsonFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "A project already exists in folder \"/rooted/path/\"."),
                        Strings.getLines(fakeProjectLogsFile.getContentsAsString().await()));
                });
            });
        });
    }
}