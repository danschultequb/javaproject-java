package qub;

public interface JavaProjectFolderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProjectFolder.class, () ->
        {
            runner.testGroup("get(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProjectFolder.get(null),
                        new PreConditionFailure("folder cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder folder = process.getCurrentFolder();
                    final JavaProjectFolder projectFolder = JavaProjectFolder.get(folder);
                    test.assertNotNull(projectFolder);
                    test.assertEqual(folder.getPath(), projectFolder.getPath());
                });
            });

            runner.test("getOutputsFolder()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                test.assertNotNull(outputsFolder);
                test.assertEqual("outputs", outputsFolder.relativeTo(projectFolder).toString());
            });

            runner.test("getOutputsSourcesFolder()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Folder outputsSourcesFolder = projectFolder.getOutputsSourcesFolder().await();
                test.assertNotNull(outputsSourcesFolder);
                test.assertEqual("outputs/sources", outputsSourcesFolder.relativeTo(projectFolder).toString());
            });

            runner.test("getOutputsTestsFolder()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Folder outputsTestsFolder = projectFolder.getOutputsTestsFolder().await();
                test.assertNotNull(outputsTestsFolder);
                test.assertEqual("outputs/tests", outputsTestsFolder.relativeTo(projectFolder).toString());
            });

            runner.test("getBuildJsonFile()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                test.assertNotNull(buildJsonFile);
                test.assertEqual("outputs/build.json", buildJsonFile.relativeTo(projectFolder).toString());
            });

            runner.test("getBuildJsonRelativePath()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Path buildJsonRelativePath = projectFolder.getBuildJsonRelativePath().await();
                test.assertNotNull(buildJsonRelativePath);
                test.assertEqual("outputs/build.json", buildJsonRelativePath.toString());
            });

            runner.testGroup("getBuildJson()", () ->
            {
                runner.test("when it doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                        test.assertFalse(buildJsonFile.exists().await());

                        test.assertThrows(() -> projectFolder.getBuildJson().await(),
                            new FileNotFoundException(buildJsonFile));

                        test.assertFalse(buildJsonFile.exists().await());
                    });

                runner.test("when it is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                        buildJsonFile.create().await();

                        test.assertThrows(() -> projectFolder.getBuildJson().await(),
                            new ParseException("Missing object left curly bracket ('{')."));

                        test.assertEqual("", buildJsonFile.getContentsAsString().await());
                    });

                runner.test("when it is an empty JSON object",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                        buildJsonFile.setContentsAsString(JSONObject.create().toString()).await();

                        final BuildJSON buildJson = projectFolder.getBuildJson().await();
                        test.assertNotNull(buildJson);
                        test.assertEqual(JSONObject.create(), buildJson.toJson());
                    });
            });

            runner.testGroup("writeBuildJson(BuildJSON)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        test.assertThrows(() -> projectFolder.writeBuildJson(null),
                            new PreConditionFailure("buildJson cannot be null."));

                        test.assertFalse(projectFolder.getBuildJsonFile().await().exists().await());
                    });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        final BuildJSON buildJson = BuildJSON.create()
                            .setJavacVersion("fake-javac-version");

                        projectFolder.writeBuildJson(buildJson).await();

                        final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                        test.assertTrue(buildJsonFile.exists().await());
                        test.assertEqual(buildJson.toString(JSONFormat.pretty), buildJsonFile.getContentsAsString().await());
                    });
            });

            runner.test("getTestJsonFile()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final File testJsonFile = projectFolder.getTestJsonFile().await();
                test.assertNotNull(testJsonFile);
                test.assertEqual("outputs/test.json", testJsonFile.relativeTo(projectFolder).toString());
            });

            runner.test("getTestJsonRelativePath()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Path testJsonRelativePath = projectFolder.getTestJsonRelativePath().await();
                test.assertNotNull(testJsonRelativePath);
                test.assertEqual("outputs/test.json", testJsonRelativePath.toString());
            });

            runner.testGroup("getTestJson()", () ->
            {
                runner.test("when it doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File testJsonFile = projectFolder.getTestJsonFile().await();
                        test.assertFalse(testJsonFile.exists().await());

                        test.assertThrows(() -> projectFolder.getTestJson().await(),
                            new FileNotFoundException(testJsonFile));

                        test.assertFalse(testJsonFile.exists().await());
                    });

                runner.test("when it is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File testJsonFile = projectFolder.getTestJsonFile().await();
                        testJsonFile.create().await();

                        test.assertThrows(() -> projectFolder.getTestJson().await(),
                            new ParseException("Missing object left curly bracket ('{')."));

                        test.assertEqual("", testJsonFile.getContentsAsString().await());
                    });

                runner.test("when it is an empty JSON object",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File testJsonFile = projectFolder.getTestJsonFile().await();
                        testJsonFile.setContentsAsString(JSONObject.create().toString()).await();

                        final TestJSON testJson = projectFolder.getTestJson().await();
                        test.assertNotNull(testJson);
                        test.assertEqual(JSONObject.create(), testJson.toJson());
                    });
            });

            runner.testGroup("writeTestJson(TestJSON)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        test.assertThrows(() -> projectFolder.writeTestJson(null),
                            new PreConditionFailure("testJson cannot be null."));

                        test.assertFalse(projectFolder.getTestJsonFile().await().exists().await());
                    });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        final TestJSON testJson = TestJSON.create()
                            .setJavaVersion("fake-java-version");

                        projectFolder.writeTestJson(testJson).await();

                        final File testJsonFile = projectFolder.getTestJsonFile().await();
                        test.assertTrue(testJsonFile.exists().await());
                        test.assertEqual(testJson.toString(JSONFormat.pretty), testJsonFile.getContentsAsString().await());
                    });
            });

            runner.test("getPackJsonFile()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final File packJsonFile = projectFolder.getPackJsonFile().await();
                test.assertNotNull(packJsonFile);
                test.assertEqual("outputs/pack.json", packJsonFile.relativeTo(projectFolder).toString());
            });

            runner.test("getPackJsonRelativePath()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                final Path packJsonRelativePath = projectFolder.getPackJsonRelativePath().await();
                test.assertNotNull(packJsonRelativePath);
                test.assertEqual("outputs/pack.json", packJsonRelativePath.toString());
            });

            runner.testGroup("getPackJson()", () ->
            {
                runner.test("when it doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File packJsonFile = projectFolder.getPackJsonFile().await();
                        test.assertFalse(packJsonFile.exists().await());

                        test.assertThrows(() -> projectFolder.getPackJson().await(),
                            new FileNotFoundException(packJsonFile));

                        test.assertFalse(packJsonFile.exists().await());
                    });

                runner.test("when it is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File packJsonFile = projectFolder.getPackJsonFile().await();
                        packJsonFile.create().await();

                        test.assertThrows(() -> projectFolder.getPackJson().await(),
                            new ParseException("Missing object left curly bracket ('{')."));

                        test.assertEqual("", packJsonFile.getContentsAsString().await());
                    });

                runner.test("when it is an empty JSON object",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final File packJsonFile = projectFolder.getPackJsonFile().await();
                        packJsonFile.setContentsAsString(JSONObject.create().toString()).await();

                        final PackJSON packJson = projectFolder.getPackJson().await();
                        test.assertNotNull(packJson);
                        test.assertEqual(JSONObject.create(), packJson.toJson());
                    });
            });

            runner.testGroup("writePackJson(PackJSON)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        test.assertThrows(() -> projectFolder.writePackJson(null),
                            new PreConditionFailure("packJson cannot be null."));

                        test.assertFalse(projectFolder.getPackJsonFile().await().exists().await());
                    });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        final PackJSON packJson = PackJSON.create()
                            .setJarVersion("fake-jar-version");

                        projectFolder.writePackJson(packJson).await();

                        final File packJsonFile = projectFolder.getPackJsonFile().await();
                        test.assertTrue(packJsonFile.exists().await());
                        test.assertEqual(packJson.toString(JSONFormat.pretty), packJsonFile.getContentsAsString().await());
                    });
            });

            runner.testGroup("iterateClassFiles()", () ->
            {
                runner.test("when outputs folder doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        test.assertEqual(Iterable.create(), projectFolder.iterateClassFiles().toList());
                    });

                runner.test("when outputs folder is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        projectFolder.getOutputsFolder().await()
                            .create().await();

                        test.assertEqual(Iterable.create(), projectFolder.iterateClassFiles().toList());
                    });

                runner.test("with a class file in outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(JavaClassFile.get(aClassFile)),
                            projectFolder.iterateClassFiles().toList());
                    });

                runner.test("with a class file in outputs/sources folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("sources/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(JavaClassFile.get(aClassFile)),
                            projectFolder.iterateClassFiles().toList());
                    });

                runner.test("with a class file in outputs/tests folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("tests/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(JavaClassFile.get(aClassFile)),
                            projectFolder.iterateClassFiles().toList());
                    });
            });

            runner.testGroup("iterateSourceClassFiles()", () ->
            {
                runner.test("when outputs folder doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        test.assertEqual(Iterable.create(), projectFolder.iterateSourceClassFiles().toList());
                    });

                runner.test("when outputs folder is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        projectFolder.getOutputsFolder().await()
                            .create().await();

                        test.assertEqual(Iterable.create(), projectFolder.iterateSourceClassFiles().toList());
                    });

                runner.test("with a class file in outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(),
                            projectFolder.iterateSourceClassFiles().toList());
                    });

                runner.test("with a class file in outputs/sources folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("sources/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(JavaClassFile.get(aClassFile)),
                            projectFolder.iterateSourceClassFiles().toList());
                    });

                runner.test("with a class file in outputs/tests folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("tests/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(),
                            projectFolder.iterateSourceClassFiles().toList());
                    });
            });

            runner.testGroup("iterateTestClassFiles()", () ->
            {
                runner.test("when outputs folder doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        test.assertEqual(Iterable.create(), projectFolder.iterateTestClassFiles().toList());
                    });

                runner.test("when outputs folder is empty",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        projectFolder.getOutputsFolder().await()
                            .create().await();

                        test.assertEqual(Iterable.create(), projectFolder.iterateTestClassFiles().toList());
                    });

                runner.test("with a class file in outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(),
                            projectFolder.iterateTestClassFiles().toList());
                    });

                runner.test("with a class file in outputs/sources folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("sources/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(),
                            projectFolder.iterateTestClassFiles().toList());
                    });

                runner.test("with a class file in outputs/tests folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        final Folder outputsFolder = projectFolder.getOutputsFolder().await();
                        final File aClassFile = outputsFolder.getFile("tests/A.class").await();
                        aClassFile.create().await();

                        test.assertEqual(
                            Iterable.create(JavaClassFile.get(aClassFile)),
                            projectFolder.iterateTestClassFiles().toList());
                    });
            });

            runner.testGroup("getDeletedJavaFiles()", () ->
            {
                runner.test("with no outputs folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        test.assertEqual(Iterable.create(), projectFolder.getDeletedJavaFiles().await());
                    });

                runner.test("with no build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                        projectFolder.getOutputsFolder().await()
                            .create().await();

                        test.assertEqual(Iterable.create(), projectFolder.getDeletedJavaFiles().await());
                    });

                runner.test("with no deleted .java files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        final Folder sourcesFolder = projectFolder.getSourcesFolder().await();
                        final File aJavaFile = sourcesFolder.createFile("A.java").await();
                        final File bJavaFile = sourcesFolder.createFile("B.java").await();

                        projectFolder.writeBuildJson(BuildJSON.create()
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder)),
                                BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder)))))
                            .await();

                        test.assertEqual(Iterable.create(), projectFolder.getDeletedJavaFiles().await());
                    });

                runner.test("with a deleted .java file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                    {
                        final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                        final Folder sourcesFolder = projectFolder.getSourcesFolder().await();
                        final File aJavaFile = sourcesFolder.getFile("A.java").await();
                        final File bJavaFile = sourcesFolder.createFile("B.java").await();

                        projectFolder.writeBuildJson(BuildJSON.create()
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder)),
                                BuildJSONJavaFile.create(bJavaFile.relativeTo(projectFolder)))))
                            .await();

                        test.assertEqual(
                            Iterable.create(JavaFile.get(aJavaFile)),
                            projectFolder.getDeletedJavaFiles().await());
                    });
            });

            runner.testGroup("deleteClassFiles(Iterable<JavaFile>)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);

                    test.assertThrows(() -> projectFolder.deleteClassFiles(null),
                        new PreConditionFailure("javaFiles cannot be null."));
                });

                runner.test("with no .java files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    
                    final Iterable<JavaFile> javaFiles = projectFolder.iterateJavaFiles().toList();
                    test.assertFalse(javaFiles.any());

                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(javaFiles).await();
                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(),
                        deletedClassFiles);
                });

                runner.test("with .java files but none deleted",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    projectFolder.createFile("sources/A.java").await();
                    projectFolder.createFile("tests/BTests.java").await();
                    
                    final Iterable<JavaFile> javaFiles = Iterable.create();
                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(javaFiles).await();
                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(),
                        deletedClassFiles);
                });

                runner.test("with .java files but none deleted",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    projectFolder.createFile("sources/A.java").await();
                    projectFolder.createFile("tests/BTests.java").await();
                    
                    final Iterable<JavaFile> javaFiles = Iterable.create();
                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(javaFiles).await();
                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(),
                        deletedClassFiles);
                });

                runner.test("with no build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    final File aJavaFile = projectFolder.getFile("sources/A.java").await();
                    projectFolder.createFile("tests/BTests.java").await();
                    
                    final Iterable<JavaFile> javaFiles = Iterable.create(JavaFile.get(aJavaFile));
                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(javaFiles).await();
                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(),
                        deletedClassFiles);
                });

                runner.test("with deleted .java file not in build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    
                    final Folder sourcesFolder = projectFolder.getSourcesFolder().await();
                    final JavaFile aJavaFile = JavaFile.get(sourcesFolder.getFile("A.java").await());
                    
                    final Folder outputsSourcesFolder = projectFolder.getOutputsSourcesFolder().await();
                    final JavaClassFile aClassFile = JavaClassFile.get(outputsSourcesFolder.getFile("A.class").await());
                    aClassFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.getTestSourcesFolder().await();
                    final JavaFile bTestsJavaFile = JavaFile.get(testsFolder.getFile("BTests.java").await());
                    bTestsJavaFile.setContentsAsString("BTests.java source code").await();

                    final Folder outputsTestsFolder = projectFolder.getOutputsTestsFolder().await();
                    final JavaClassFile bTestsClassFile = JavaClassFile.get(outputsTestsFolder.getFile("BTests.class").await());
                    bTestsClassFile.setContentsAsString("BTests.java byte code").await();
                    
                    final Clock clock = process.getClock();
                    final DateTime now = clock.getCurrentDateTime();
                    final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(bTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(now)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(bTestsClassFile.relativeTo(projectFolder), now)))))
                            .toString(JSONFormat.pretty))
                        .await();
                    
                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(
                        Iterable.create(
                            JavaFile.get(aJavaFile)))
                        .await();

                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(),
                        deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(
                            projectFolder.getOutputsFolder().await(),
                            testsFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            aClassFile,
                            bTestsClassFile,
                            bTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                });

                runner.test("with deleted .java file in build.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JavaProjectFolder projectFolder = JavaProjectFolderTests.getProjectFolder(process);
                    
                    final Folder sourcesFolder = projectFolder.getSourcesFolder().await();
                    final JavaFile aJavaFile = JavaFile.get(sourcesFolder.getFile("A.java").await());
                    
                    final Folder outputsSourcesFolder = projectFolder.getOutputsSourcesFolder().await();
                    final JavaClassFile aClassFile = JavaClassFile.get(outputsSourcesFolder.getFile("A.class").await());
                    aClassFile.setContentsAsString("A.java source code").await();
                    
                    final Folder testsFolder = projectFolder.getTestSourcesFolder().await();
                    final JavaFile bTestsJavaFile = JavaFile.get(testsFolder.getFile("BTests.java").await());
                    bTestsJavaFile.setContentsAsString("BTests.java source code").await();

                    final Folder outputsTestsFolder = projectFolder.getOutputsTestsFolder().await();
                    final JavaClassFile bTestsClassFile = JavaClassFile.get(outputsTestsFolder.getFile("BTests.class").await());
                    bTestsClassFile.setContentsAsString("BTests.java byte code").await();
                    
                    final Clock clock = process.getClock();
                    final DateTime now = clock.getCurrentDateTime();
                    final File buildJsonFile = projectFolder.getBuildJsonFile().await();
                    buildJsonFile.setContentsAsString(
                        BuildJSON.create()
                            .setJavacVersion("17")
                            .setJavaFiles(Iterable.create(
                                BuildJSONJavaFile.create(aJavaFile.relativeTo(projectFolder))
                                    .setLastModified(now)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(aClassFile.relativeTo(projectFolder), now))),
                                BuildJSONJavaFile.create(bTestsJavaFile.relativeTo(projectFolder))
                                    .setLastModified(now)
                                    .setClassFiles(Iterable.create(
                                        BuildJSONClassFile.create(bTestsClassFile.relativeTo(projectFolder), now)))))
                            .toString(JSONFormat.pretty))
                        .await();
                    
                    final Iterable<JavaClassFile> deletedClassFiles = projectFolder.deleteClassFiles(
                        Iterable.create(
                            JavaFile.get(aJavaFile)))
                        .await();

                    test.assertNotNull(deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(
                            aClassFile),
                        deletedClassFiles);
                    test.assertEqual(
                        Iterable.create(
                            projectFolder.getOutputsFolder().await(),
                            testsFolder,
                            outputsSourcesFolder,
                            outputsTestsFolder,
                            buildJsonFile,
                            bTestsClassFile,
                            bTestsJavaFile),
                        projectFolder.iterateEntriesRecursively().toList());
                });
            });
        });
    }

    static JavaProjectFolder getProjectFolder(FakeDesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return JavaProjectFolder.get(process.getCurrentFolder());
    }
}
