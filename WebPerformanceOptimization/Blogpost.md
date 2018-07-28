# Web Performance Optimization for Continuous Deployment 
The performance of websites today is a decisive factor in how many users visit them and thus how much money can be earned from them. The impact of this fact is further enhanced by the widespread use of mobile devices and the speed of the mobile Internet.  
To counteract the development of heavyweight websites, web performance optimizations should be integrated into the development process as early as possible.  
As part of this blog post I want to address this topic in the context of Continous Deployment using the following sections.

- [Motivation](#motivation)
- [Implementation](#implementation)
- [Lessons Learned](#lessonslearned)
- [Conclusion](#conclusion)

## Motivation
<a name="motivation"></a>
To avoid starting a continuous deployment environment from scratch, I used a previous project as a basis. 
The Debts² project is a distributed application used to jointly manage expenses in groups. The application consists of the following three components.
- A backend server
- A native Android App
- A simple admin frontend  

The [backend](http://cloudproject.mi.hdm-stuttgart.de:8081/v1/status) was hosted on a server of the HdM. Using this server, both the [admin frontend](http://cloudproject.mi.hdm-stuttgart.de/#/) and the [Jenkins](https://jenkins.io/) could be hosted as a continuous deployment environment. Within this environment the native Android app was also built. Additional resources related to debts² can be found [here](https://drive.google.com/open?id=1-nLpRGqmhLp7_h3A1u77xGPB9ETMatoa) for further information.  
So, the Jenkins and the admin frontend created the starting point for the integration of Web performance optimizations in a continuous deployment environment.
To enable a before-and-after comparison, a snapshot of the admin frontend and the Jenkins [pipeline](https://jenkins.io/doc/book/pipeline/) was taken first. In order to measure the performance of the admin frontend a [lighthouse](https://developers.google.com/web/tools/lighthouse/) report was generated. The following figures represent the initial state of the Jenkins pipeline and the performance of the admin frontend.
| ![Initial pipeline snapshot](images/old-snapshot.png) |
| :--: |
| *Initial pipeline snapshot* |

| ![Initial lighthouse report](images/old-snapshot.png) |
| :--: |
| *Initial lighthouse report* |
Based on this initial status survey, I have formulated the following goals.
- Extend the Jenkins pipeline to automatically measure the performance of the admin frontend and modify its pipeline status accordingly
- Make use of the measurements to optimize the performance of the admin frontend

## Implementation
<a name="implementation"></a>
All source code is available at [GitHub](https://github.com/BenjaminKowatsch/InteractiveMedia). Code concerning this blog post can be found in the following subdirectories of the mono repository.

- [Jenkins](https://github.com/BenjaminKowatsch/InteractiveMedia/tree/master/Jenkins)
- [lighthouse](https://github.com/BenjaminKowatsch/InteractiveMedia/tree/master/lighthouse)
- [lighthouse-plugin](https://github.com/BenjaminKowatsch/InteractiveMedia/tree/master/lighthouse-plugin)
- [frontend](https://github.com/BenjaminKowatsch/InteractiveMedia/tree/master/frontend)

In order to achieve the first goal, I decided to integrate a lighthouse report generation into the pipeline using [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/). 
After some research I found multiple possible options to proceed. 
1. Making use of this [web service](https://builder-dot-lighthouse-ci.appspot.com/ci?format=html&key=123&url=https://example.com/) to generate the lighthouse report while providing the URL to be tested as a URL parameter.
2. Running a Docker Container on the Jenkins server to generate the report locally.
3. Running an own web service using another server.

Due to the minimal effort of option one, I decided to try the second one and declare the first one to my fallback solution. Until I arrived at a working solution, I had to go through some attempts.  
First, I wanted to make use of a prexisting Docker image. After trying several images without success, I was able to run the image [lighthouse-ci](https://hub.docker.com/r/kmturley/lighthouse-ci/) on my local system. Next, it should run on the Jenkins server. Unfortunately, the image could not be executed on the  server due to missing UI. The container couldn't also be executed using its settings for the headless mode of google chrome.  
As a further try, I installed lighthouse and chrome on the server directly without Docker to reduce complexity. However, this attempt failed because Lighthouse waited for Chrome on a specific port, although it had already started in headless mode. In hindsight, I have to say that at this point the information about starting chrome with remote debugging would have saved a lot of effort.  
For the last attempt I organized a remote server with UI in order to have all prerequisites to make it work.
Unfortunately, when starting google chrome I received a missleading error regarding no display connection being established. To overcome this difficulty, I took a closer look at the google chrome headless mode and how it interacts with lighthouse. I learned that chrome must be started in headless mode using remote debugging on a specific port in order to work with lighthouse.  
Based on this insight I was able to create a local solution first. Subsequently, I managed to build an own Docker Image based on the [chrome-headless-trunk](https://hub.docker.com/r/alpeware/chrome-headless-trunk/) by installing [Node.js](https://nodejs.org/en/download/package-manager/#debian-and-ubuntu-based-linux-distributions) and the [lighthouse npm package](https://www.npmjs.com/package/lighthouse) manually. The resulting Dockerfile is accessible [here](https://github.com/BenjaminKowatsch/InteractiveMedia/blob/master/lighthouse/Dockerfile).  
Initially, I thought that the generation of the lighthouse report would be integrated into the pipeline via a separate build stage at the [Jenkinsfile](https://github.com/BenjaminKowatsch/InteractiveMedia/blob/master/lighthouse/Dockerfile). By using this Dockerfile in combination with Docker Compose [health checks](https://docs.docker.com/compose/compose-file/compose-file-v2/#healthcheck), I was able to line up the build and run of my custom Docker lighthouse container into the execution order of the build and run of the actual distributed application. The resulting output contains only a few lines of code at the [Docker Compose file](https://github.com/BenjaminKowatsch/InteractiveMedia/blob/master/docker-compose.local.yml). Additionally, no separate build stage is required.  
Next, to put the lighthouse report at disposal at Jenkins I used the Jenkins [HTML Publisher Plugin](https://wiki.jenkins.io/display/JENKINS/HTML+Publisher+Plugin) to publish the HTML version of the lighthouse report.
Therefore I created a new stage at the Jenkinsfile.  
To ensure the performance of the website under load, an additional stage was appended. By the means of [Taurus](https://gettaurus.org/), the Automation-friendly framework for Continous Testing, load tests can be specified in a declarative manner by using a YAML-file. Simply put, the file consists of the following five sections. 
- scenarios
- execution
- reporting
- provisioning
- modules  

The section scenarios depicts actual http requests to be executed.  
The section execution describes load test metrics such as a concurrency, locations, scenario, ramp-up and hold-for time. The subsection scenario links one scenario to be realized.  
In the section [reporting](https://gettaurus.org/docs/Reporting/) consists of modules aggregating the results of the executors and feeding them into a report. By making use of the module [passfail](https://gettaurus.org/docs/PassFail/) rules for certain metrics like the average response or latency time can be defined to either let the test succeed or fail.  
To perform the load tests in the [cloud](https://gettaurus.org/docs/Cloud/), the provisioning section must be set correctly. By default Taurus uses local provisioning.
BlazeMeter's free plan allows one location only for cloud testing. So, be sure to set only one location, when enabling cloud provisioning.  
In the module section, you can provide the credentials and further settings to connect to BlazeMeter or other Cloud Testing platforms. In addition, data worth protecting can be defined at the module section of the .bzt-rc file of the current user. A more comprehensive breakdown regarging the YAML-file can be found [here](https://gettaurus.org/docs/YAMLTutorial/)  

In conjunction with Taurus I harnessed the Testing platform [BlazeMeter](https://www.blazemeter.com/) for load test execution. In order to connect the Jenkins server to BlazeMeter the [Taurus command line tool *bzt*](https://gettaurus.org/docs/CommandLine/) has to be [installed](https://gettaurus.org/install/Installation/) on the Jenkins host machine. A well structured tutorial containing detailed Information about the installation is avaiable [here](https://dzone.com/articles/how-to-run-a-taurus-test-through-jenkins-pipelines). Next, the Taurus command line tool *bzt* has to connect to BlazeMeter. Therefore an API key and API secret has to be generated at BlazeMeter's account settings. To avoid exposing the credentials, it's recommended to write these into the .bzt-rc file at the home directory of the Jenkins user. to Afterwards it's ready for use in the Jenkinsfile. Invoking a report to be accessed at BlazeMeter the option 'report' has to be applied. To better distinguish the load tests, the Jenkins build number can also be integrated into the test name. During each build process, a link to the newly created load test report on BlazeMeter is now displayed in the console. In the following picture an overview of a sample report of a cloud test is depicted.

| ![Overview Cloud Test on BlazeMeter](images/BlazeMeter_Cloud_Test.png) |
| :--: |
| *Overview Cloud Test on BlazeMeter* |

 
- Influence the pipeline status according to the results of the lighthouse report using the JSON version of the lighthouse report.
  - Using a script section in Jenkinsfile
  - Using a custom Jenkins Plugin
    - Little documentation 
- Admin frontend optimizations
  - Gzip compression
  - Uglify/Minify
  - Cache Control
  - [Critical CSS path](https://www.sitelocity.com/critical-path-css-generator)
  - [Unused CSS](https://www.jitbit.com/unusedcss/)
  - SSL certificates

## Lessons Learned
<a name="lessonslearned"></a>
  - WPO Concepts
  - Improved skills (nginx, lighthouse)
## Conclusion
<a name="conclusion"></a>
 - Results
 - 

[Presentation](https://docs.google.com/presentation/d/1zSLEMyPWvWehIqo3YlQUSIo7wDnUUFUUTjbfS97cn3g/edit?usp=sharing)

TODO:

- Implementation (5 1/2 Pages)
  - Course of action
    - Jenkins Docker
  - Challenges
  - Appendix 
    - Jenkins Plugin
- Lessons Learned (3 Pages)
  - WPO Concepts
  - Improved skills (nginx, lighthouse)
- Conclusion (1/2 Page)