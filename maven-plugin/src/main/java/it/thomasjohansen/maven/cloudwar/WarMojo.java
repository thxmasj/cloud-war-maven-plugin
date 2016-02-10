package it.thomasjohansen.maven.cloudwar;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author thomas@thomasjohansen.it
 */
@Mojo(
        name = "war",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
public class WarMojo extends WarBuddyMojo {

}
