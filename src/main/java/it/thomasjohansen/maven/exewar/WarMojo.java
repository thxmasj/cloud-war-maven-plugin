package it.thomasjohansen.maven.exewar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import java.io.File;
import java.io.IOException;
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
public class WarMojo extends ExecutableWarMojo {

    @Component( role = ArchiverManager.class )
    private ArchiverManager archiverManager;

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

}
