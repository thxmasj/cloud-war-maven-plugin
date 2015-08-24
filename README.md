# cloud-war-maven-plugin

A Maven plug-in for creating Java WAR files fit for the cloud. Currently it features creating self-contained,
executable WARs. It works by replacing the standard maven-war-plugin in the build cycle
and creates a WAR file with a bundled web container and a manifested main class that starts it. The WAR
can then be executed like a normal executable JAR like this

`java -jar my-executable-war.war`

while it also can be deployed to a container like normal WAR files.

Currently Tomcat and Jetty are options for the bundled container.

# Usage

Replace the standard maven-war-plugin with this plugin in the POM. It supports the same settings as the maven-war-plugin, with additional properties shown below.

```
<plugin>
  <groupId>it.thomasjohansen.maven</groupId>
  <artifactId>cloud-war-maven-plugin</artifactId>
  <version>1.0</version>
  <configuration>
    <engine>jetty</engine> <!-- "tomcat" is the default -->
  </configuration>
</plugin>
```
