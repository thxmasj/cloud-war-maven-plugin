# WAR Buddy

A Maven plug-in for creating Java WAR files featuring a set of enhancements over standard WAR files:

* Easy deployment by using an embedded web container: `java -jar myapp.war`
* Cluster-ready: HTTP session replication across a cluster of auto-discovered member nodes.
* Out-of-the-box secure HTTPS simply by including a JKS or PKCS#12 key store.
* Easy set-up by following the «convention over configuration» principle.

Just include the plugin in pom.xml and it will 
Declaratively select the enhancement features you want in the plugin´s configuration.

# Usage

Include this plugin in pom.xml to replace the standard maven-war-plugin in the packaging phase. It extends the standard plugin and therefore supports all the standard plug-in's configuration in addition to configuration for enhancing the packaged WAR.

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
