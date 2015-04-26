# executable-war-maven-plugin

A Maven plug-in for creating executable Java WAR files. It replaces the standard maven-war-plugin in the build cycle
and creates a WAR file with a bundled Tomcat Embedded instance and a manifested main class that starts it. The WAR
can therefore be executed like a normal executable JAR:

`java -jar my-executable-war.war`
