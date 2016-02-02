package it.thomasjohansen.maven.cloudwar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author thomas@thomasjohansen.it
 */
public class CloudWarMojo extends WarMojo {

    public enum Engine {tomcat, jetty}

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

    @Parameter(defaultValue = "false")
    private boolean enableCluster = false;

    @Parameter(defaultValue = "false")
    private boolean enableSingleSignOn = false;

    protected static final String tomcatClassName = "it.thomasjohansen.launcher.web.tomcat.TomcatLauncher";
    protected static final String jettyClassName = "it.thomasjohansen.launcher.web.jetty.JettyLauncher";

    @Override
    public void buildWebapp( MavenProject mavenProject, File webapplicationDirectory )
            throws MojoExecutionException, MojoFailureException, IOException {
        super.buildWebapp(mavenProject, webapplicationDirectory);
        Set<Artifact> artifacts = findEngineArtifacts();
        for (Artifact artifact : artifacts)
            overlayArtifact(artifact, webapplicationDirectory);
        if (enableCluster) {
            addClusterSessionFragment(webapplicationDirectory);
        }
        if (enableSingleSignOn)
            addSingleSignOnFragment(webapplicationDirectory);
        addManifest();
    }

    private void addClusterSessionFragment(File root) throws MojoExecutionException, IOException {
        Path targetDir = Paths.get(root.getPath(), "WEB-INF", "lib");
        if (!targetDir.toFile().exists())
            Files.createDirectories(targetDir);
        try {
            for (Artifact artifact : findClusterArtifacts()) {
                Path source = Paths.get(artifact.getFile().getPath());
                Path target = Paths.get(targetDir.toString(), artifact.getFile().getName());
                Files.copy(
                        source,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (ProjectBuildingException|InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Failed to add fragment", e);
        }
    }

    private void addSingleSignOnFragment(File root) throws MojoExecutionException, IOException {
        Path targetDir = Paths.get(root.getPath(), "WEB-INF", "lib");
        if (!targetDir.toFile().exists())
            Files.createDirectories(targetDir);
        try {
            for (Artifact artifact : findSingleSignOnArtifacts()) {
                Path source = Paths.get(artifact.getFile().getPath());
                Path target = Paths.get(targetDir.toString(), artifact.getFile().getName());
                Files.copy(
                        source,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (ProjectBuildingException|InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Failed to add fragment", e);
        }
    }

    private void addManifest() throws MojoExecutionException {
        Manifest manifest = new Manifest();
        try {
            addManifestAttribute(
                    manifest,
                    "Main-Class",
                    engine == Engine.tomcat ? tomcatClassName : jettyClassName
            );
            addManifestAttribute(manifest, "WebLauncher-Port", "" + port);
            addManifestAttribute(manifest, "WebLauncher-ContextPath", contextPath);
            if (keyStorePath != null)
                addManifestAttribute(manifest, "WebLauncher-KeyStorePath", keyStorePath);
            getWarArchiver().addConfiguredManifest(manifest);
        } catch (ManifestException e) {
            throw new MojoExecutionException("Failed to add manifest", e);
        }
    }

    private void addManifestAttribute(Manifest manifest, String name, String value) throws ManifestException {
        manifest.addConfiguredAttribute(new Manifest.Attribute(name, value));
    }

    private void overlayArtifact(Artifact artifact, File webapplicationDirectory)
            throws MojoExecutionException {
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

    private Set<Artifact> findEngineArtifacts() throws MojoExecutionException {
        Artifact launcherArtifact = artifactFactory.createArtifact(
                "it.thomasjohansen.launcher",
                engine + "-launcher",
                "1.4-SNAPSHOT",
                "",
                "jar"
        );
        return findArtifacts(launcherArtifact);
    }

    private Set<Artifact> findClusterArtifacts() throws ProjectBuildingException, InvalidDependencyVersionException, MojoExecutionException {
        Artifact clusterArtifact = artifactFactory.createArtifact(
                "it.thomasjohansen.warbuddy",
                "cluster-support",
                "1.0-SNAPSHOT",
                "",
                "jar"
        );
        return findArtifacts(clusterArtifact);
    }

    private Set<Artifact> findSingleSignOnArtifacts() throws ProjectBuildingException, InvalidDependencyVersionException, MojoExecutionException {
        Artifact clusterArtifact = artifactFactory.createArtifact(
                "it.thomasjohansen.maven",
                "saml-web-fragment",
                "1.1-SNAPSHOT",
                "",
                "jar"
        );
        return findArtifacts(clusterArtifact);
    }

    private Set<Artifact> findArtifacts(Artifact mainArtifact) throws MojoExecutionException {
        MavenProject project;
        Set<Artifact> artifacts;
        try {
            project = mavenProjectBuilder.buildFromRepository(mainArtifact, remoteRepositories, localRepository);
            artifacts = project.createArtifacts(
                    artifactFactory,
                    null, // inheritedScope
                    new ScopeArtifactFilter(DefaultArtifact.SCOPE_RUNTIME)
            );
        } catch (ProjectBuildingException|InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Failed to find artifacts", e);
        }
        artifacts.add(mainArtifact);
        for (Artifact artifact : artifacts)
            resolveArtifact(artifact);
        return artifacts;
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
