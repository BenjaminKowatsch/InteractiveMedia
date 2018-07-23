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
## Pipeline Usage
step([$class: 'LighthousePlugin',
   filepath: 'report.json',
   // Performance score
   path: 'reportCategories/Array/0/Object/score/Double',
   action: 'lt',
   value: '60',
   failStatus: 'UNSTABLE'])

### Path syntax
 <JSONObjectKey> '/' <JSONObjectType> or 
 <JSONArrayKey> '/' <JSONObjectType>
 Example: 'reportCategories/Array/0/Object'

### Actions
- 'eq' equals
- 'lt' lower than
- 'gt' greater than

### FailStatus
- 'UNSTABLE' UNSTABLE (default)
- 'ABORTED' ABORTED
- 'FAILURE' FAILURE

## Extended example

   step([$class: 'LighthousePlugin',
        filepath: './lighthouse_reports/report.report.json',
        // Performance score
        path: 'reportCategories/Array/0/Object/score/Double',
        action: 'lt',
        value: '60',
        failStatus: 'UNSTABLE'])

   step([$class: 'LighthousePlugin',
        filepath: './lighthouse_reports/report.report.json',
        // PWA score
        path: 'reportCategories/Array/1/Object/score/Double',
        action: 'lt',
        value: '30',
        failStatus: 'UNSTABLE'])

   step([$class: 'LighthousePlugin',
        filepath: './lighthouse_reports/report.report.json',
        // first meaningful paint
        path: 'audits/object/first-meaningful-paint/Object/rawValue/Double',
        action: 'lt',
        value: '5000',
        failStatus: 'UNSTABLE'])