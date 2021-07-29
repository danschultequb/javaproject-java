package qub;

public interface JavaProjectCreate
{
    static void addAction(CommandLineActions actions)
    {
        PreCondition.assertNotNull(actions, "actions");

        actions.addAction("create", JavaProjectCreate::run)
            .setDescription("Create a new Java source code project.");
    }

    static void run(DesktopProcess process, CommandLineAction action)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(action, "action");

        final CommandLineParameters parameters = action.createCommandLineParameters();
        final CommandLineParameter<Folder> projectFolderParameter = JavaProject.addProjectFolderParameter(parameters, process,
            "The folder that the new Java project will be created in.");
        final CommandLineParameterHelp helpParameter = parameters.addHelp();
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);

        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            final Folder dataFolder = process.getQubProjectDataFolder().await();
            final Folder projectFolder = projectFolderParameter.getValue().await();

            final LogStreams logStreams = CommandLineLogsAction.getLogStreamsFromDataFolder(dataFolder, process.getOutputWriteStream(), verboseParameter.getVerboseCharacterToByteWriteStream().await());
            try (final Disposable logStream = logStreams.getLogStream())
            {
                final CharacterToByteWriteStream outputStream = logStreams.getOutput();
                final VerboseCharacterToByteWriteStream verboseStream = logStreams.getVerbose();
                final File projectJsonSchemaFile = dataFolder.getFile("javaproject.schema.json").await();
                if (!projectJsonSchemaFile.exists().await())
                {
                    verboseStream.write("Creating " + projectJsonSchemaFile + "... ").await();
                    projectJsonSchemaFile.setContentsAsString(
                        JSONSchema.create()
                            .setSchema("http://json-schema.org/draft-04/schema")
                            .setType(JSONSchemaType.Object)
                            .addProperty("publisher", JSONSchema.create()
                                .setType(JSONSchemaType.String)
                                .setMinLength(1)
                                .setDescription("The person or organization that owns this project."))
                            .addProperty("project", JSONSchema.create()
                                .setType(JSONSchemaType.String)
                                .setMinLength(1)
                                .setDescription("The name of this project."))
                            .addProperty("version", JSONSchema.create()
                                .setType(JSONSchemaType.String)
                                .setMinLength(1)
                                .setDescription("The version of this project."))
                            .addProperty("java", JSONSchema.create()
                                .setType(JSONSchemaType.Object)
                                .addProperty("mainClass", JSONSchema.create()
                                    .setType(JSONSchemaType.String)
                                    .setMinLength(1)
                                    .setDescription("The name of the type that contains the \"main\" method for this project. This property should only be used if this project is executable."))
                                .addProperty("shortcutName", JSONSchema.create()
                                    .setType(JSONSchemaType.String)
                                    .setMinLength(1)
                                    .setDescription("The name of the shortcut file that will be used to run this project. This property should only be used if this project is executable."))
                                .addProperty("dependencies", JSONSchema.create()
                                    .setType(JSONSchemaType.Array)
                                    .setItems(JSONSchema.create()
                                        .setType(JSONSchemaType.Object)
                                        .addProperty("publisher", JSONSchema.create()
                                            .setType(JSONSchemaType.String)
                                            .setMinLength(1)
                                            .setDescription("The person or organization that owns the dependency project."))
                                        .addProperty("project", JSONSchema.create()
                                            .setType(JSONSchemaType.String)
                                            .setMinLength(1)
                                            .setDescription("The name of the dependency project."))
                                        .addProperty("version", JSONSchema.create()
                                            .setType(JSONSchemaType.String)
                                            .setMinLength(1)
                                            .setDescription("The version of the dependency project."))
                                        .setAdditionalProperties(false)
                                        .setRequired("publisher", "project", "version"))))
                            .setAdditionalProperties(true)
                            .setRequired("publisher", "project", "version", "java")
                            .toString(JSONFormat.pretty))
                        .await();
                    verboseStream.writeLine("Done.").await();
                }

                final File projectJsonFile = projectFolder.getFile("project.json").await();
                if (projectJsonFile.exists().await())
                {
                    outputStream.writeLine("A project already exists in folder " + Strings.escapeAndQuote(projectFolder) + ".").await();
                    process.setExitCode(-1);
                }
                else
                {
                    final String publisherName = "qub";
                    final String projectName = projectFolder.getName();
                    final String version = "1";
                    final String owner = "danschultequb";
                    final String sourcePackage = "qub";

                    final ProjectSignature projectSignature = ProjectSignature.create(publisherName, projectName, version);

                    outputStream.write("Creating Java project " + Strings.escapeAndQuote(projectSignature) + " in " + projectFolder + "... ").await();
                    verboseStream.writeLine().await();

                    final IndentedCharacterWriteStream indentedVerboseStream = IndentedCharacterWriteStream.create(verboseStream)
                        .setCurrentIndent("  ");

                    indentedVerboseStream.write("Creating " + projectJsonFile + "... ").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setSchema(projectJsonSchemaFile)
                            .setPublisher(projectSignature.getPublisher())
                            .setProject(projectSignature.getProject())
                            .setVersion(projectSignature.getVersion())
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create()))
                            .toString(JSONFormat.pretty))
                        .await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final File readmeMdFile = projectFolder.getFile("README.md").await();
                    indentedVerboseStream.write("Creating " + readmeMdFile + "... ").await();
                    readmeMdFile.setContentsAsString("# " + projectSignature.toStringIgnoreVersion()).await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final File licenseFile = projectFolder.getFile("LICENSE").await();
                    indentedVerboseStream.write("Creating " + licenseFile + "... ").await();
                    licenseFile.setContentsAsString(
                        Strings.join('\n', Iterable.create(
                            "MIT License",
                            "",
                            "Copyright (c) " + process.getClock().getCurrentDateTime().getYear() + " " + owner,
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
                            "SOFTWARE.")))
                        .await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final File gitIgnoreFile = projectFolder.getFile(".gitignore").await();
                    indentedVerboseStream.write("Creating " + gitIgnoreFile + "... ").await();
                    gitIgnoreFile.setContentsAsString(
                        Strings.join('\n', Iterable.create(
                            ".idea",
                            "out",
                            "outputs",
                            "target")))
                        .await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final Folder sourcesFolder = projectFolder.getFolder("sources").await()
                        .getFolder(sourcePackage).await();
                    indentedVerboseStream.write("Creating " + sourcesFolder + "... ").await();
                    sourcesFolder.create().await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final Folder testsFolder = projectFolder.getFolder("tests").await()
                        .getFolder(sourcePackage).await();
                    indentedVerboseStream.write("Creating " + testsFolder + "... ").await();
                    testsFolder.create().await();
                    indentedVerboseStream.writeLine("Done.").await();

                    indentedVerboseStream.write("Initializing Git repository... ").await();
                    final Git git = Git.create(process);
                    git.init(p -> p.addDirectory(projectFolder)).await();
                    indentedVerboseStream.writeLine("Done.").await();

                    final String gitHubTokenEnvironmentVariableName = "GITHUB_TOKEN";
                    final String gitHubToken = process.getEnvironmentVariable(gitHubTokenEnvironmentVariableName)
                        .catchError(NotFoundException.class)
                        .await();
                    if (Strings.isNullOrEmpty(gitHubToken))
                    {
                        indentedVerboseStream.writeLine("No GitHub token found in the environment variable " + gitHubTokenEnvironmentVariableName + ".").await();
                    }
                    else
                    {
                        indentedVerboseStream.write("Creating GitHub repository... ").await();
                        final GitHubRepository repository = GitHubClient.create(process.getNetwork())
                            .setAccessToken(gitHubToken)
                            .createRepository(CreateRepositoryParameters.create()
                                .setName(projectName))
                            .await();
                        indentedVerboseStream.writeLine("Done.").await();

                        indentedVerboseStream.write("Adding remote reference to GitHub repository... ").await();
                        git.remoteAdd(p ->
                        {
                            p.addName("origin");
                            p.addUrl(repository.getGitUrl());
                        }).await();
                        indentedVerboseStream.writeLine("Done.").await();
                    }

                    outputStream.writeLine("Done.").await();
                }
            }
        }
    }
}
