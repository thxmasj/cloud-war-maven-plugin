package it.thomasjohansen.maven.exewar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author thomas@thomasjohansen.it
 */
@Mojo(
        name = "war",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
public class ExecutableWarMojo extends WarMojo {

    public enum Engine {tomcat, jetty}

    @Component( role = ArchiverManager.class )
    private ArchiverManager archiverManager;

    @Component(role = ArtifactFactory.class)
    private ArtifactFactory artifactFactory;

    @Component(role = ArtifactResolver.class)
    private ArtifactResolver artifactResolver;

    @Component(role = MavenProjectBuilder.class)
    private MavenProjectBuilder mavenProjectBuilder;

    @Component(role = ArtifactMetadataSource.class)
    private ArtifactMetadataSource artifactMetadataSource;

    @Parameter(defaultValue = "${project.localRepository}")
    private ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.remoteRepositories}")
    private List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "tomcat")
    private Engine engine;

    @Override
    public void buildWebapp( MavenProject mavenProject, File webapplicationDirectory )
            throws MojoExecutionException, MojoFailureException, IOException {
        super.buildWebapp(mavenProject, webapplicationDirectory);
        Set<Artifact> artifacts = findLauncherArtifacts();
        for (Artifact artifact : artifacts)
            overlayArtifact(artifact, webapplicationDirectory);
        addManifest();
    }

    private void addManifest() throws MojoExecutionException {
            Manifest manifest = new Manifest();
            Manifest.Attribute mainClassAttribute = new Manifest.Attribute(
                    "Main-Class",
                    engine == Engine.tomcat ?
                            "it.thomasjohansen.launcher.TomcatLauncher"
                            : "it.thomasjohansen.launcher.JettyLauncher"
            );
        try {
            manifest.addConfiguredAttribute(mainClassAttribute);
            getWarArchiver().addConfiguredManifest(manifest);
        } catch (ManifestException e) {
            throw new MojoExecutionException("Failed to add manifest", e);
        }
    }

    private Set findLauncherArtifacts() throws MojoExecutionException {
        Artifact tomcatLauncherArtifact = artifactFactory.createArtifact(
                "it.thomasjohansen.launcher",
                engine + "-launcher",
                "1.0-SNAPSHOT",
                "",
                "jar"
        );
        try {
            MavenProject project = mavenProjectBuilder.buildFromRepository(tomcatLauncherArtifact, remoteRepositories, localRepository);
            Set artifacts = project.createArtifacts(artifactFactory, null, null);
            artifacts.add(tomcatLauncherArtifact);
            return artifacts;
//            ArtifactResolutionResult resolutionResult = artifactResolver.resolveTransitively(
//                    artifacts,
//                    tomcatLauncherPom,
//                    project.getManagedVersionMap(),
//                    localRepository,
//                    remoteRepositories,
//                    artifactMetadataSource,
//                    null
//            );
//            return resolutionResult.getArtifacts();
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
//        } catch (ArtifactNotFoundException e) {
//            throw new MojoExecutionException(e.getMessage(), e);
//        } catch (ArtifactResolutionException e) {
//            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void overlayArtifact(Artifact artifact, File webapplicationDirectory)
            throws MojoExecutionException {
        try {
            artifactResolver.resolve(artifact, remoteRepositories, localRepository);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Failed to resolve artifact", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Artifact not found", e);
        }
        try {
            UnArchiver unArchiver = archiverManager.getUnArchiver(artifact.getFile());
            unArchiver.setSourceFile(artifact.getFile());
            unArchiver.setDestDirectory(webapplicationDirectory);
            unArchiver.setOverwrite(true);
            getLog().info("Extracting " + artifact.getFile() + " to " + webapplicationDirectory);
            unArchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Failed to overlay artifact", e);
        }
    }

}
