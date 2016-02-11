package it.thomasjohansen.maven.cloudwar;

import it.thomasjohansen.weblauncher.Launcher;
import it.thomasjohansen.weblauncher.LauncherConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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
public class ExecuteMojo extends WarBuddyMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true )
    @SuppressWarnings("unused")
    private String buildDirectory;
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    @SuppressWarnings("unused")
    private String finalName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        try {
            ClassLoader classLoader = createClassLoader();
            Class<?> launcherClass = Class.forName(engine == Engine.tomcat ? tomcatClassName : jettyClassName, true, classLoader);
            @SuppressWarnings("unchecked")
            Constructor<? extends Launcher> constructor = (Constructor<? extends Launcher>)launcherClass.getDeclaredConstructor(LauncherConfiguration.class);
            Launcher launcher = constructor.newInstance(createLauncherConfiguration(classLoader));
            launcher.launch();
            launcher.awaitTermination();
        } catch (MalformedURLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Launcher class not found", e);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("Launcher class has no main method", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to invoke launcher", e);
        }
    }

    private ClassLoader createClassLoader() throws MojoExecutionException, MalformedURLException {
        return new URLClassLoader(
                new URL[]{new File(buildDirectory + "/" + finalName).toURI().toURL()},
                Thread.currentThread().getContextClassLoader()
        );
    }

    private LauncherConfiguration createLauncherConfiguration(ClassLoader classLoader) {
        LauncherConfiguration.Builder builder = LauncherConfiguration.builder()
                .application("/", buildDirectory + "/" + finalName)
                .classLoader(classLoader);
        if (keyStorePath != null)
            builder.secureConnector(port, keyStorePath, "changeit");
        else
            builder.connector(port);
        return builder.build();
    }

}
