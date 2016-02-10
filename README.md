[![Build Status](https://travis-ci.org/thxmasj/war-buddy.svg?branch=master)](https://travis-ci.org/thxmasj/war-buddy)

# WAR Buddy

A Maven plug-in for creating Java WAR files featuring a set of enhancements over standard WAR files:

* Easy deployment by using an embedded web container: `java -jar myapp.war`
* Cluster-ready: HTTP session replication across a cluster of auto-discovered member nodes.
* Out-of-the-box secure HTTPS simply by including a JKS or PKCS#12 key store.
* Authentication through SAML 2's Web Browser SSO Profile.
* Easy set-up by following the «convention over configuration» principle.
* The web application can be deployed directly from source by using the `war-buddy:execute` Maven goal.

Just include the plugin in pom.xml and it will replace the standard plugin in the packaging phase.
Declaratively select the enhancement features you want in the plugin´s configuration.

# Usage

Include this plugin in pom.xml. As it is an extension of the default plugin it also supports the standard settings in
addition to the settings for the enhancement features...

```xml
<plugin>
  <groupId>it.thomasjohansen.warbuddy</groupId>
  <artifactId>war-buddy-maven-plugin</artifactId>
  <version>1.0</version>
  <extensions>true</extensions>
  <configuration>
    <engine>tomcat</engine>
    <port>8080</port>
    <contextPath>/</contextPath>
    <keyStorePath>/tls.jks</keyStorePath>
    <failOnMissingWebXml>false</failOnMissingWebXml>
  </configuration>
</plugin>
```
