package qub;

public interface JDKFolderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JDKFolder.class, () ->
        {
            runner.testGroup("get(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JDKFolder.get(null),
                        new PreConditionFailure("innerFolder cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder innerFolder = process.getCurrentFolder();
                    final JDKFolder jdkFolder = JDKFolder.get(innerFolder).await();
                    test.assertNotNull(jdkFolder);
                    test.assertEqual(innerFolder, jdkFolder);
                });
            });

            runner.testGroup("getLatestVersion(QubFolder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JDKFolder.getLatestVersion(null),
                        new PreConditionFailure("qubFolder cannot be null."));
                });

                runner.test("with no installed JDK folders",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    test.assertThrows(() -> JDKFolder.getLatestVersion(qubFolder).await(),
                        new NotFoundException("No project named openjdk/jdk has been published."));
                });

                runner.test("with one installed JDK folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectVersionFolder jdk17Folder = qubFolder.getProjectVersionFolder("openjdk", "jdk", "17").await();
                    jdk17Folder.create().await();

                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder).await();
                    test.assertNotNull(jdkFolder);
                    test.assertEqual(jdk17Folder, jdkFolder);
                });

                runner.test("with two installed JDK folders",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectVersionFolder jdk16Folder = qubFolder.getProjectVersionFolder("openjdk", "jdk", "16.0.1").await();
                    jdk16Folder.create().await();
                    final QubProjectVersionFolder jdk17Folder = qubFolder.getProjectVersionFolder("openjdk", "jdk", "17").await();
                    jdk17Folder.create().await();

                    final JDKFolder jdkFolder = JDKFolder.getLatestVersion(qubFolder).await();
                    test.assertNotNull(jdkFolder);
                    test.assertEqual(jdk17Folder, jdkFolder);
                });
            });

            runner.test("getJavacFile()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                final File javacFile = jdkFolder.getFile("bin/javac").await();
                test.assertEqual(javacFile, jdkFolder.getJavacFile().await());
            });

            runner.testGroup("getJavac(DesktopProcess)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    test.assertThrows(() -> jdkFolder.getJavac((DesktopProcess)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    final Javac javac = jdkFolder.getJavac(process).await();
                    test.assertNotNull(javac);
                    test.assertEqual(jdkFolder.getJavacFile().await().getPath(), javac.getExecutablePath());
                });
            });

            runner.testGroup("getJavac(ChildProcessRunner)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    test.assertThrows(() -> jdkFolder.getJavac((ChildProcessRunner)null),
                        new PreConditionFailure("childProcessRunner cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    final Javac javac = jdkFolder.getJavac(process.getChildProcessRunner()).await();
                    test.assertNotNull(javac);
                    test.assertEqual(jdkFolder.getJavacFile().await().getPath(), javac.getExecutablePath());
                });
            });

            runner.test("getJavaFile()",
                (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                (Test test, FakeDesktopProcess process) ->
            {
                final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                final File javaFile = jdkFolder.getFile("bin/java").await();
                test.assertEqual(javaFile, jdkFolder.getJavaFile().await());
            });

            runner.testGroup("getJava(DesktopProcess)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    test.assertThrows(() -> jdkFolder.getJava((DesktopProcess)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    final Java java = jdkFolder.getJava(process).await();
                    test.assertNotNull(java);
                    test.assertEqual(jdkFolder.getJavaFile().await().getPath(), java.getExecutablePath());
                });
            });

            runner.testGroup("getJava(ChildProcessRunner)", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    test.assertThrows(() -> jdkFolder.getJava((ChildProcessRunner)null),
                        new PreConditionFailure("childProcessRunner cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final JDKFolder jdkFolder = JDKFolder.get(process.getCurrentFolder()).await();
                    final Java java = jdkFolder.getJava(process.getChildProcessRunner()).await();
                    test.assertNotNull(java);
                    test.assertEqual(jdkFolder.getJavaFile().await().getPath(), java.getExecutablePath());
                });
            });
        });
    }
}
