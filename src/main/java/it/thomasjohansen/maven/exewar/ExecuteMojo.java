package it.thomasjohansen.maven.exewar;

import it.thomasjohansen.launcher.web.Launcher;
import it.thomasjohansen.launcher.web.LauncherConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thomas@thomasjohansen.it
 */
@Mojo(
        name = "execute",
        defaultPhase = LifecyclePhase.NONE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
@SuppressWarnings("unused")
public class ExecuteMojo extends ExecutableWarMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true )
    @SuppressWarnings("unused")
    private String buildDirectory;
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    @SuppressWarnings("unused")
    private String finalName;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            ClassLoader classLoader = createClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            Class<?> launcherClass = Class.forName(engine == Engine.tomcat ? tomcatClassName : jettyClassName, true, classLoader);
            @SuppressWarnings("unchecked")
            Constructor<? extends Launcher> constructor = (Constructor<? extends Launcher>)launcherClass.getDeclaredConstructor(LauncherConfiguration.class);
            Launcher launcher = constructor.newInstance(createLauncherConfiguration(classLoader));
            launcher.launch();
            launcher.awaitTermination();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Launcher class not found", e);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("Launcher class has no main method", e);
        } catch (InvocationTargetException e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        }
    }

    private ClassLoader createClassLoader() throws MojoExecutionException, MalformedURLException {
        List<URL> paths = new ArrayList<URL>();
        paths.add(new File(buildDirectory + "/" + finalName).toURI().toURL());
        System.out.println(paths.get(0));
        for (Artifact artifact : findLauncherArtifacts()) {
            try {
                URL path = artifact.getFile().toURI().toURL();
                System.out.println(path);
                paths.add(path);
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Failed to setup class loader", e);
            }
        }
        return new URLClassLoader(paths.toArray(new URL[paths.size()]), this.getClass().getClassLoader());
    }

    private LauncherConfiguration createLauncherConfiguration(ClassLoader classLoader) {
        LauncherConfiguration.Builder builder = LauncherConfiguration.builder()
                .addApplication("/", buildDirectory + "/" + finalName)
                .classLoader(classLoader);
        if (keyStorePath != null)
            builder.addSecureConnector(port, keyStorePath, "changeit");
        else
            builder.addConnector(port);
        return builder.build();
    }

}
