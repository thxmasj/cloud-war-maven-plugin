package it.thomasjohansen.maven.exewar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author thomas@thomasjohansen.it
 */
public abstract class ExecutableWarMojo extends WarMojo {

    public enum Engine {tomcat, jetty}

    @Component(role = ArtifactFactory.class)
    @Inject
    private ArchiverManager archiverManager;

    @Inject
    private ArtifactFactory artifactFactory;

    @Inject
    private ArtifactResolver artifactResolver;

    @Inject
    private MavenProjectBuilder mavenProjectBuilder;

    @Inject
    private ArtifactMetadataSource artifactMetadataSource;

    @Parameter(defaultValue = "${project.localRepository}")
    private ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.remoteRepositories}")
    private List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "tomcat")
    protected Engine engine;

    @Parameter(defaultValue = "8080", required = true)
    protected int port;

    @Parameter(defaultValue = "/", required = true)
    protected String contextPath;

    @Parameter(required = false)
    protected String keyStorePath;

    protected static final String tomcatClassName = "it.thomasjohansen.launcher.web.tomcat.TomcatLauncher";
    protected static final String jettyClassName = "it.thomasjohansen.launcher.web.jetty.JettyLauncher";

    protected Set<Artifact> findLauncherArtifacts() throws MojoExecutionException {
        Artifact tomcatLauncherArtifact = artifactFactory.createArtifact(
                "it.thomasjohansen.launcher",
                engine + "-launcher",
                "1.2",
                "",
                "jar"
        );
        try {
            MavenProject project = mavenProjectBuilder.buildFromRepository(tomcatLauncherArtifact, remoteRepositories, localRepository);
            Set<Artifact> artifacts = project.createArtifacts(artifactFactory, null, null);
            artifacts.add(tomcatLauncherArtifact);
            for (Artifact artifact : artifacts)
                resolveArtifact(artifact);
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

    private void resolveArtifact(Artifact artifact) throws MojoExecutionException {
        try {
            artifactResolver.resolve(artifact, remoteRepositories, localRepository);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Failed to resolve artifact", e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Artifact not found", e);
        }
    }

}
