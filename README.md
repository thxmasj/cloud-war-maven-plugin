# cloud-war-maven-plugin

A Maven plug-in for creating Java WAR files ready for cloud infrastructure. Features are:

* Easy deployment: `java -jar myapp.war`
* Cluster-ready: HTTP session replication with auto-discovered cluster members.
* Modern TLS-support: Out-of-the-box secure HTTPS just by including a keystore.
* Convention over configuration for easy set-up.

It works by replacing the standard maven-war-plugin in the build cycle and creates a WAR file with a bundled web container and a manifested main class that starts it. The WAR can then be executed like a normal executable JAR like this

`java -jar my-executable-war.war`

while it also can be deployed to a container like normal WAR files.

Both Tomcat and Jetty are options for the bundled container.

# Usage

Replace the standard maven-war-plugin with this plugin in the POM. It supports the same settings as the maven-war-plugin, with additional properties shown below.

```
<plugin>
  <groupId>it.thomasjohansen.maven</groupId>
  <artifactId>cloud-war-maven-plugin</artifactId>
  <version>1.0</version>
  <extensions>true</extensions>
  <configuration>
    <!-- Default engine is "tomcat", but "jetty" is supported as well -->
    <engine>tomcat</engine>
    <!-- Default port is 8081 -->
    <port>8081</port>
    <contextPath>/</contextPath>
    <!-- This enables TLS and uses private key from specified resource path -->
    <keyStorePath>/tls.jks</keyStorePath>
    <!-- This plug-in extends maven-war-plugin, so it's configuration is supported as well -->
    <failOnMissingWebXml>false</failOnMissingWebXml>
  </configuration>
</plugin>
```
