package qub;

public class JavaPublishedProjectFolder extends QubProjectVersionFolder
{
    protected JavaPublishedProjectFolder(Folder innerFolder)
    {
        super(innerFolder);
    }

    public static JavaPublishedProjectFolder get(Folder innerFolder)
    {
        PreCondition.assertNotNull(innerFolder, "innerFolder");

        return new JavaPublishedProjectFolder(innerFolder);
    }

    public static Result<JavaPublishedProjectFolder> getIfExists(QubFolder qubFolder, ProjectSignature projectSignature)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");
        PreCondition.assertNotNull(projectSignature, "projectSignature");

        return Result.create(() ->
        {
            JavaPublishedProjectFolder result;

            final String dependencyPublisher = projectSignature.getPublisher();
            final QubPublisherFolder publisherFolder = qubFolder.getPublisherFolder(dependencyPublisher).await();
            if (!publisherFolder.exists().await())
            {
                throw new NotFoundException("No publisher folder named " + Strings.escapeAndQuote(dependencyPublisher) + " found in the Qub folder (" + qubFolder + ").");
            }
            else
            {
                final String dependencyProject = projectSignature.getProject();
                final QubProjectFolder projectFolder = publisherFolder.getProjectFolder(dependencyProject).await();
                if (!projectFolder.exists().await())
                {
                    throw new NotFoundException("No project folder named " + Strings.escapeAndQuote(dependencyProject) + " found in the " + Strings.escapeAndQuote(dependencyPublisher) + " publisher folder (" + publisherFolder + ").");
                }
                else
                {
                    final VersionNumber dependencyVersion = projectSignature.getVersion();
                    final QubProjectVersionFolder dependencyVersionFolder = projectFolder.getProjectVersionFolder(dependencyVersion).await();
                    if (!dependencyVersionFolder.exists().await())
                    {
                        throw new NotFoundException("No version folder named " + Strings.escapeAndQuote(dependencyVersion) + " found in the " + Strings.escapeAndQuote(projectSignature.toStringIgnoreVersion()) + " project folder (" + projectFolder + ").");
                    }
                    else
                    {
                        result = JavaPublishedProjectFolder.get(dependencyVersionFolder);
                    }
                }
            }

            return result;
        });
    }

    public Result<File> getProjectJsonFile()
    {
        return this.getFile("project.json");
    }

    public Result<JavaProjectJSON> getProjectJson()
    {
        return Result.create(() ->
        {
            final File projectJsonFile = this.getProjectJsonFile().await();
            return JavaProjectJSON.parse(projectJsonFile).await();
        });
    }

    public Result<Iterable<ProjectSignature>> getDependencies()
    {
        return Result.create(() ->
        {
            final JavaProjectJSON projectJson = this.getProjectJson().await();
            return projectJson.getDependencies();
        });
    }

    public Result<File> getCompiledSourcesJarFile()
    {
        return Result.create(() ->
        {
            final String projectName = this.getProjectName().await();
            final String compiledSourcesJarFileName = JavaProject.getCompiledSourcesJarFileName(projectName);
            return this.getFile(compiledSourcesJarFileName).await();
        });
    }

    public Result<File> getSourcesJarFile()
    {
        return Result.create(() ->
        {
            final String projectName = this.getProjectName().await();
            final String sourcesJarFileName = JavaProject.getSourcesJarFileName(projectName);
            return this.getFile(sourcesJarFileName).await();
        });
    }

    public Result<File> getCompiledTestsJarFile()
    {
        return Result.create(() ->
        {
            final String projectName = this.getProjectName().await();
            final String compiledSourcesJarFileName = JavaProject.getCompiledTestsJarFileName(projectName);
            return this.getFile(compiledSourcesJarFileName).await();
        });
    }

    public Result<File> getTestSourcesJarFile()
    {
        return Result.create(() ->
        {
            final String projectName = this.getProjectName().await();
            final String sourcesJarFileName = JavaProject.getTestSourcesJarFileName(projectName);
            return this.getFile(sourcesJarFileName).await();
        });
    }
}
