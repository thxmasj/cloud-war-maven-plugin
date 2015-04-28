# executable-war-maven-plugin

A Maven plug-in for creating executable Java WAR files. It replaces the standard maven-war-plugin in the build cycle
and creates a WAR file with a bundled web container and a manifested main class that starts it. The WAR
can therefore be executed like a normal executable JAR:

`java -jar my-executable-war.war`

Currently Tomcat and Jetty are options for the bundled container.

# Usage

Replace the standard maven-war-plugin with this plugin in the POM. It supports the same settings as the maven-war-plugin, with additional properties shown below.

```
<plugin>
  <groupId>it.thomasjohansen.maven</groupId>
  <artifactId>executable-war-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <configuration>
    <!-- "tomcat" is default engine -->
    <engine>jetty</engine>
  </configuration>
</plugin>
```
