## Created using the hello-world-plugin archetype
mvn archetype:generate -Dfilter=io.jenkins.archetypes:hello-world-plugin
## Requires JAVA_HOME environment variable to be set
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk/
## Build Plugin
mvn clean package -X
## Start local jenkins with lighthouse-plugin
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn hpi:run -Djetty.port=8090 -Dhpi.prefix=/jenkins
## Go to your browser
127.0.0.1:8090/jenkins
