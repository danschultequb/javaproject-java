package qub;

public interface JavacTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(Javac.class, () ->
        {
            runner.test("version()",
                (TestResources resources) -> Tuple.create(resources.getProcess()),
                (Test test, DesktopProcess process) ->
            {
                final Javac javac = Javac.create(process.getChildProcessRunner());
                final VersionNumber versionNumber = javac.version().await();
                test.assertEqual(
                    VersionNumber.create()
                        .setMajor(17)
                        .setMinor(0)
                        .setPatch(1),
                    versionNumber);
            });

            runner.testGroup("run(JavacParameters)", () ->
            {
                runner.test("with null parameters",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    test.assertThrows(() -> javac.run((JavacParameters)null),
                        new PreConditionFailure("parameters cannot be null."));
                });

                runner.test("with no command line arguments",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    final InMemoryCharacterToByteStream javacOutput = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream javacError = InMemoryCharacterToByteStream.create();
                    final JavacParameters parameters = JavacParameters.create();
                    parameters.redirectOutputTo(javacOutput);
                    parameters.redirectErrorTo(javacError);

                    final Integer runResult = javac.run(parameters).await();

                    test.assertLinesEqual(
                        Iterable.create(
                            "Usage: javac <options> <source files>",
                            "where possible options include:",
                            "  @<filename>                  Read options and filenames from file",
                            "  -Akey[=value]                Options to pass to annotation processors",
                            "  --add-modules <module>(,<module>)*",
                            "        Root modules to resolve in addition to the initial modules, or all modules",
                            "        on the module path if <module> is ALL-MODULE-PATH.",
                            "  --boot-class-path <path>, -bootclasspath <path>",
                            "        Override location of bootstrap class files",
                            "  --class-path <path>, -classpath <path>, -cp <path>",
                            "        Specify where to find user class files and annotation processors",
                            "  -d <directory>               Specify where to place generated class files",
                            "  -deprecation",
                            "        Output source locations where deprecated APIs are used",
                            "  --enable-preview",
                            "        Enable preview language features. To be used in conjunction with either -source or --release.",
                            "  -encoding <encoding>         Specify character encoding used by source files",
                            "  -endorseddirs <dirs>         Override location of endorsed standards path",
                            "  -extdirs <dirs>              Override location of installed extensions",
                            "  -g                           Generate all debugging info",
                            "  -g:{lines,vars,source}       Generate only some debugging info",
                            "  -g:none                      Generate no debugging info",
                            "  -h <directory>",
                            "        Specify where to place generated native header files",
                            "  --help, -help, -?            Print this help message",
                            "  --help-extra, -X             Print help on extra options",
                            "  -implicit:{none,class}",
                            "        Specify whether or not to generate class files for implicitly referenced files",
                            "  -J<flag>                     Pass <flag> directly to the runtime system",
                            "  --limit-modules <module>(,<module>)*",
                            "        Limit the universe of observable modules",
                            "  --module <module>(,<module>)*, -m <module>(,<module>)*",
                            "        Compile only the specified module(s), check timestamps",
                            "  --module-path <path>, -p <path>",
                            "        Specify where to find application modules",
                            "  --module-source-path <module-source-path>",
                            "        Specify where to find input source files for multiple modules",
                            "  --module-version <version>",
                            "        Specify version of modules that are being compiled",
                            "  -nowarn                      Generate no warnings",
                            "  -parameters",
                            "        Generate metadata for reflection on method parameters",
                            "  -proc:{none,only}",
                            "        Control whether annotation processing and/or compilation is done.",
                            "  -processor <class1>[,<class2>,<class3>...]",
                            "        Names of the annotation processors to run; bypasses default discovery process",
                            "  --processor-module-path <path>",
                            "        Specify a module path where to find annotation processors",
                            "  --processor-path <path>, -processorpath <path>",
                            "        Specify where to find annotation processors",
                            "  -profile <profile>",
                            "        Check that API used is available in the specified profile",
                            "  --release <release>",
                            "        Compile for the specified Java SE release. Supported releases: 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17",
                            "  -s <directory>               Specify where to place generated source files",
                            "  --source <release>, -source <release>",
                            "        Provide source compatibility with the specified Java SE release. Supported releases: 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17",
                            "  --source-path <path>, -sourcepath <path>",
                            "        Specify where to find input source files",
                            "  --system <jdk>|none          Override location of system modules",
                            "  --target <release>, -target <release>",
                            "        Generate class files suitable for the specified Java SE release. Supported releases: 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17",
                            "  --upgrade-module-path <path>",
                            "        Override location of upgradeable modules",
                            "  -verbose                     Output messages about what the compiler is doing",
                            "  --version, -version          Version information",
                            "  -Werror                      Terminate compilation if warnings occur",
                            ""),
                        javacOutput);
                    test.assertLinesEqual(
                        Iterable.create(),
                        javacError);
                    test.assertEqual(2, runResult);
                });

                runner.test("with --help-extra",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    final InMemoryCharacterToByteStream javacOutput = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream javacError = InMemoryCharacterToByteStream.create();
                    final JavacParameters parameters = JavacParameters.create();
                    parameters.redirectOutputTo(javacOutput);
                    parameters.redirectErrorTo(javacError);
                    parameters.addArgument("--help-extra");

                    final Integer runResult = javac.run(parameters).await();

                    test.assertLinesEqual(
                        Iterable.create(
                            "  --add-exports <module>/<package>=<other-module>(,<other-module>)*",
                            "        Specify a package to be considered as exported from its defining module",
                            "        to additional modules, or to all unnamed modules if <other-module> is ALL-UNNAMED.",
                            "  --add-reads <module>=<other-module>(,<other-module>)*",
                            "        Specify additional modules to be considered as required by a given module.",
                            "        <other-module> may be ALL-UNNAMED to require the unnamed module.",
                            "  --default-module-for-created-files <module-name>",
                            "        Fallback target module for files created by annotation processors, if none specified or inferred.",
                            "  -Djava.endorsed.dirs=<dirs>  Override location of endorsed standards path",
                            "  -Djava.ext.dirs=<dirs>       Override location of installed extensions",
                            "  --help-lint                  Print the supported keys for -Xlint",
                            "  --patch-module <module>=<file>(:<file>)*",
                            "        Override or augment a module with classes and resources",
                            "        in JAR files or directories",
                            "  -Xbootclasspath:<path>       Override location of bootstrap class files",
                            "  -Xbootclasspath/a:<path>     Append to the bootstrap class path",
                            "  -Xbootclasspath/p:<path>     Prepend to the bootstrap class path",
                            "  -Xdiags:{compact,verbose}    Select a diagnostic mode",
                            "  -Xdoclint",
                            "        Enable recommended checks for problems in javadoc comments",
                            "  -Xdoclint:(all|none|[-]<group>)[/<access>]",
                            "        Enable or disable specific checks for problems in javadoc comments,",
                            "        where <group> is one of accessibility, html, missing, reference, or syntax,",
                            "        and <access> is one of public, protected, package, or private.",
                            "  -Xdoclint/package:[-]<packages>(,[-]<package>)*",
                            "        Enable or disable checks in specific packages. Each <package> is either the",
                            "        qualified name of a package or a package name prefix followed by .*, which",
                            "        expands to all sub-packages of the given package. Each <package> can be prefixed",
                            "        with - to disable checks for the specified package or packages.",
                            "  -Xlint                       Enable recommended warnings",
                            "  -Xlint:<key>(,<key>)*",
                            "        Warnings to enable or disable, separated by comma.",
                            "        Precede a key by - to disable the specified warning.",
                            "        Use --help-lint to see the supported keys.",
                            "  -Xmaxerrs <number>           Set the maximum number of errors to print",
                            "  -Xmaxwarns <number>          Set the maximum number of warnings to print",
                            "  -Xpkginfo:{always,legacy,nonempty}",
                            "        Specify handling of package-info files",
                            "  -Xplugin:\"name args\"",
                            "        Name and optional arguments for a plug-in to be run",
                            "  -Xprefer:{source,newer}",
                            "        Specify which file to read when both a source file and class file are found for an implicitly compiled class",
                            "  -Xprint",
                            "        Print out a textual representation of specified types",
                            "  -XprintProcessorInfo",
                            "        Print information about which annotations a processor is asked to process",
                            "  -XprintRounds",
                            "        Print information about rounds of annotation processing",
                            "  -Xstdout <filename>          Redirect standard output",
                            "",
                            "These extra options are subject to change without notice."),
                        javacOutput);
                    test.assertLinesEqual(
                        Iterable.create(),
                        javacError);
                    test.assertEqual(0, runResult);
                });

                runner.test("with --help-lint",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    final InMemoryCharacterToByteStream javacOutput = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream javacError = InMemoryCharacterToByteStream.create();
                    final JavacParameters parameters = JavacParameters.create();
                    parameters.redirectOutputTo(javacOutput);
                    parameters.redirectErrorTo(javacError);
                    parameters.addArgument("--help-lint");

                    final Integer runResult = javac.run(parameters).await();

                    test.assertLinesEqual(
                        Iterable.create(
                            "The supported keys for -Xlint are:",
                            "    all                  Enable all warnings",
                            "    auxiliaryclass       Warn about an auxiliary class that is hidden in a source file, and is used from other files.",
                            "    cast                 Warn about use of unnecessary casts.",
                            "    classfile            Warn about issues related to classfile contents.",
                            "    deprecation          Warn about use of deprecated items.",
                            "    dep-ann              Warn about items marked as deprecated in JavaDoc but not using the @Deprecated annotation.",
                            "    divzero              Warn about division by constant integer 0.",
                            "    empty                Warn about empty statement after if.",
                            "    exports              Warn about issues regarding module exports.",
                            "    fallthrough          Warn about falling through from one case of a switch statement to the next.",
                            "    finally              Warn about finally clauses that do not terminate normally.",
                            "    missing-explicit-ctor Warn about missing explicit constructors in public and protected classes in exported packages.",
                            "    module               Warn about module system related issues.",
                            "    opens                Warn about issues regarding module opens.",
                            "    options              Warn about issues relating to use of command line options.",
                            "    overloads            Warn about issues regarding method overloads.",
                            "    overrides            Warn about issues regarding method overrides.",
                            "    path                 Warn about invalid path elements on the command line.",
                            "    processing           Warn about issues regarding annotation processing.",
                            "    rawtypes             Warn about use of raw types.",
                            "    removal              Warn about use of API that has been marked for removal.",
                            "    requires-automatic   Warn about use of automatic modules in the requires clauses.",
                            "    requires-transitive-automatic Warn about automatic modules in requires transitive.",
                            "    serial               Warn about Serializable classes that do not provide a serial version ID. ",
                            "                         Also warn about access to non-public members from a serializable element.",
                            "    static               Warn about accessing a static member using an instance.",
                            "    strictfp             Warn about unnecessary use of the strictfp modifier.",
                            "    synchronization      Warn about synchronization attempts on instances of value-based classes.",
                            "    text-blocks          Warn about inconsistent white space characters in text block indentation.",
                            "    try                  Warn about issues relating to use of try blocks (i.e. try-with-resources).",
                            "    unchecked            Warn about unchecked operations.",
                            "    varargs              Warn about potentially unsafe vararg methods.",
                            "    preview              Warn about use of preview language features.",
                            "    none                 Disable all warnings"),
                        javacOutput);
                    test.assertLinesEqual(
                        Iterable.create(),
                        javacError);
                    test.assertEqual(0, runResult);
                });

                runner.test("with file that doesn't exist",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    final InMemoryCharacterToByteStream javacOutput = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream javacError = InMemoryCharacterToByteStream.create();
                    final JavacParameters parameters = JavacParameters.create();
                    parameters.redirectOutputTo(javacOutput);
                    parameters.redirectErrorTo(javacError);
                    parameters.addArgument("filethatdoesntexist.java");

                    final Integer runResult = javac.run(parameters).await();

                    test.assertLinesEqual(
                        Iterable.create(),
                        javacOutput);
                    test.assertLinesEqual(
                        Iterable.create(
                            "error: file not found: filethatdoesntexist.java",
                            "Usage: javac <options> <source files>",
                            "use --help for a list of possible options"),
                        javacError);
                    test.assertEqual(2, runResult);
                });

                final Action7<String,Map<String,Iterable<String>>,Iterable<String>,Iterable<String>,Iterable<String>,Integer,Iterable<String>> runTest = (String testName, Map<String,Iterable<String>> sourceFiles, Iterable<String> extraArguments, Iterable<String> expectedOutput, Iterable<String> expectedError, Integer expectedExitCode, Iterable<String> expectedOutputFiles) ->
                {
                    runner.test(testName,
                        (TestResources resources) -> Tuple.create(resources.getProcess(), resources.getTemporaryFolder()),
                        (Test test, DesktopProcess process, Folder tempFolder) ->
                    {
                        final Folder outputsFolder = tempFolder.createFolder("outputs").await();
                        final Folder sourcesFolder = tempFolder.createFolder("sources").await();

                        final List<String> filesToCompile = List.create();
                        for (final MapEntry<String,Iterable<String>> sourceFileEntry : sourceFiles)
                        {
                            final String sourceFileRelativePath = sourceFileEntry.getKey();
                            final Iterable<String> sourceFileContents = sourceFileEntry.getValue();
                            final File sourceFile = sourcesFolder.getFile(sourceFileRelativePath).await();
                            sourceFile.setContentsAsString(Strings.join('\n', sourceFileContents)).await();
                            filesToCompile.add(sourceFile.relativeTo(tempFolder).toString());
                        }

                        final Javac javac = Javac.create(process.getChildProcessRunner());
                        final InMemoryCharacterToByteStream javacOutput = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream javacError = InMemoryCharacterToByteStream.create();
                        final JavacParameters parameters = JavacParameters.create();
                        parameters.setWorkingFolder(tempFolder);
                        parameters.redirectOutputTo(javacOutput);
                        parameters.redirectErrorTo(javacError);
                        parameters.addDirectory(outputsFolder);
                        parameters.addArguments(extraArguments);
                        parameters.addArguments(filesToCompile);

                        final Integer runResult = javac.run(parameters).await();

                        test.assertLinesEqual(
                            expectedOutput,
                            javacOutput);
                        test.assertLinesEqual(
                            expectedError,
                            javacError);
                        test.assertEqual(expectedExitCode, runResult);

                        test.assertEqual(
                            expectedOutputFiles,
                            outputsFolder.iterateEntries()
                                .map((FileSystemEntry entry) -> entry.relativeTo(outputsFolder).toString())
                                .toList());
                    });
                };

                runTest.run("with empty file",
                    Map.<String,Iterable<String>>create()
                        .set("Empty.java", Iterable.create()),
                    Iterable.create(),
                    Iterable.create(),
                    Iterable.create(),
                    0,
                    Iterable.create());

                runTest.run("with empty file and --verbose",
                    Map.<String,Iterable<String>>create()
                        .set("Empty.java", Iterable.create()),
                    Iterable.create("--verbose"),
                    Iterable.create(),
                    Iterable.create(
                        "error: invalid flag: --verbose",
                        "Usage: javac <options> <source files>",
                        "use --help for a list of possible options"),
                    2,
                    Iterable.create());

                runTest.run("with file with basic text",
                    Map.<String,Iterable<String>>create()
                        .set("Empty.java", Iterable.create(
                            "Hello there. I'm not a java file.")),
                    Iterable.create(),
                    Iterable.create(),
                    Iterable.create(
                        "sources\\Empty.java:1: error: class, interface, enum, or record expected",
                        "Hello there. I'm not a java file.",
                        "^",
                        "sources\\Empty.java:1: error: unclosed character literal",
                        "Hello there. I'm not a java file.",
                        "              ^",
                        "2 errors"),
                    1,
                    Iterable.create());

                runTest.run("with file with class with different name than file name",
                    Map.<String,Iterable<String>>create()
                        .set("Empty.java", Iterable.create(
                            "public class Class1",
                            "{",
                            "}")),
                    Iterable.create(),
                    Iterable.create(),
                    Iterable.create(
                        "sources\\Empty.java:1: error: class Class1 is public, should be declared in a file named Class1.java",
                        "public class Class1",
                        "       ^",
                        "1 error"),
                    1,
                    Iterable.create());

                runTest.run("with file with empty class",
                    Map.<String,Iterable<String>>create()
                        .set("Empty.java", Iterable.create(
                            "public class Empty",
                            "{",
                            "}")),
                    Iterable.create(),
                    Iterable.create(),
                    Iterable.create(),
                    0,
                    Iterable.create(
                        "Empty.class"));

                runTest.run("with deprecated method usage",
                    Map.<String,Iterable<String>>create()
                        .set("A.java", Iterable.create(
                            "public class A",
                            "{",
                            "  @Deprecated",
                            "  public static void hello()",
                            "  {",
                            "  }",
                            "}"))
                        .set("B.java", Iterable.create(
                            "public class B",
                            "{",
                            "  public static void there()",
                            "  {",
                            "    A.hello();",
                            "  }",
                            "}")),
                    Iterable.create(),
                    Iterable.create(),
                    Iterable.create(
                        "Note: sources\\B.java uses or overrides a deprecated API.",
                        "Note: Recompile with -Xlint:deprecation for details."),
                    0,
                    Iterable.create(
                        "A.class",
                        "B.class"));

                runTest.run("with deprecated method usage",
                    Map.<String,Iterable<String>>create()
                        .set("A.java", Iterable.create(
                            "public class A",
                            "{",
                            "  @Deprecated",
                            "  public static void hello()",
                            "  {",
                            "  }",
                            "}"))
                        .set("B.java", Iterable.create(
                            "public class B",
                            "{",
                            "  public static void there()",
                            "  {",
                            "    A.hello();",
                            "  }",
                            "}")),
                    Iterable.create("-Xlint:deprecation"),
                    Iterable.create(),
                    Iterable.create(
                        "sources\\B.java:5: warning: [deprecation] hello() in A has been deprecated",
                        "    A.hello();",
                        "     ^",
                        "1 warning"),
                    0,
                    Iterable.create(
                        "A.class",
                        "B.class"));

                runTest.run("with deprecated generic method usage",
                    Map.<String,Iterable<String>>create()
                        .set("A.java", Iterable.create(
                            "public class A",
                            "{",
                            "  @Deprecated",
                            "  public static <T> void hello()",
                            "  {",
                            "  }",
                            "}"))
                        .set("B.java", Iterable.create(
                            "public class B",
                            "{",
                            "  public static void there()",
                            "  {",
                            "    A.<String>hello();",
                            "  }",
                            "}")),
                    Iterable.create("-Xlint:deprecation"),
                    Iterable.create(),
                    Iterable.create(
                        "sources\\B.java:5: warning: [deprecation] <T>hello() in A has been deprecated",
                        "    A.<String>hello();",
                        "     ^",
                        "  where T is a type-variable:",
                        "    T extends Object declared in method <T>hello()",
                        "1 warning"),
                    0,
                    Iterable.create(
                        "A.class",
                        "B.class"));
            });

            runner.testGroup("run(String...)", () ->
            {
                runner.test("with no arguments",
                    (TestResources resources) -> Tuple.create(resources.getProcess()),
                    (Test test, DesktopProcess process) ->
                {
                    final Javac javac = Javac.create(process.getChildProcessRunner());
                    final Integer runResult = javac.run().await();
                    test.assertEqual(2, runResult);
                });
            });
        });
    }
}
