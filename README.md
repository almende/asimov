![ASIMOV](https://raw.githubusercontent.com/almende/asimov/master/src/test/resources/gui/html/images/asimov_logo_large.png)

# About the tool

ASIMOV is a loosely coupled distributed inference engine that infers knowledge about the details of a process (e.g., consultation) occurring in a context (e.g., hospital) with the sole generic information about the process itself and measurement about a real-life process of that kind. Such knowledge can then be used to calculate KPIs, visualize analytics, make predictions, validate models, explain processes etc.   

Given a business process described as a flowchart and some measurements about the process, ASIMOV can infer what sequence of events have led to the measurements, thus inferring and abstracting knowledge about the process itself. 

As input ASIMOV receives two types of parameters together called the *usecase*:
  - A Process/Workflow that describes what resources (Actors, Assets, Materials, Places, etc.) are **required** to participate in an activity at the same place and time together, and how these activities are sequenced after eachother expressed by a branching probability.
  - A Context describing the types and other properties of resources required to execute the process/workflow descriptions providing detailed information on how they **constrain** the process/workflow in both space and time dimentions.

ASIMOV produces:
  - Events that **explain** the execution of the process/workflow descriptions meeting the requirements of the the process/workflow and the constraints of the resources.
  - A GUI offering Visual Analytics tooling for **verifying/validating** the *usecase* againts the *events*.

### Version
0.0.1 Beta Release

### Tech

Currently ASIMOV is written in JAVA, more implementations are currently being considered, but not yet planned. ASIMOV uses a number of open source projects to work properly:

* [EVE] - A web-based agent platform
* [VisJS] - Awesome tooling we use for our visual analytics for verification and validation
* [Neo4J] - A graph database engine used for process and resource path finding.
* [COALA] - A novel abstraction layer for Agents that helps reuse agent code across MASs and ABMs
* [MongoDB] - A NoSQL database for quickly storing produced events, and generation of multiple output views on it.

> In this BETA version the Multi Agent System bootstraps from a command line application and
> reads an input xml and writes the output to a new xml file once it is finished.
> Plans and preperations are made to transform the application to a more loosley coupled 
> micro-service architecture, providing an extention API for developers and a GUI for 
> modelers/researchers.

### Installation

First make sure that you have:
[MongoDB installed] on your machine.
[Java JDK version 1.7 or above installed] on your machine.
[Maven installed] on your machine.
[Github installed] on your machine.

Make sure your JAVA_HOME env variable is set to the JDK 1.7 installation directory.
Also ensure that your mongoDB is running, if not start the $MONGO_DIR/bin/mongod executable.

To build and install ASIMOV, type the following commands in your favorite shell:
```sh
$ git clone https://github.com/almende/asimov asimov
$ cd asimov
$ mvn clean install -Dmaven.test.skip=true
$ cd dist
```

Now you are ready to run ASIMOV, please refer to the examples listed below to get a more clear understanding of how to use ASIMOV.

### Running ASIMOV

```sh
$ java -jar ASIMOV.jar <inputxmlfilename> <durationInDays> <outputdirectory>
```

Examples with more detailed information for input xml's can be found in the section below.

### Examples

Examples of how to use ASIMOV can be found here :

* [examples/simple/README.md](https://github.com/almende/asimov/tree/master/examples/simple/README.md)

### Development

ASIMOV is being developed by [Almende], a Dutch research company specialized in information and communication technologies. At the core of all Almende solutions are hybrid agent networks: humans and computers working together. Almende looks towards agent technology to develop smart software that truly supports people in organizing their own lives.

ASIMOV is an open-source product, Want to contribute? Great!

The contact person within Almende for development on ASIMOV is [Suki van Beusekom].

### Todo's

 - Provide examples in example pages
 - Add ASIMOV SL to input XML.
 - Remove COALA dependencies work directly with EVE.
 - JS implementation.
 - Micro-service extension architecture with RESTFull and RPC-API.
 - GUI for building usecases.
 - Provide developer documentation.
 - Add machine learning feedback loop to adjust processes and resource compositions.
 - Finish todo's :-)

License
----

MIT Licence

[EVE]:http://eve.almende.com
[VisJS]:http://visjs.org/
[Neo4j]:http://neo4j.com/
[COALA]:https://github.com/krevelen/coala
[MongoDB]:https://www.mongodb.org/
[MongoDB installed]:http://docs.mongodb.org/getting-started/shell/installation/
[Github installed]:http://git-scm.com/book/en/v2/Getting-Started-Installing-Git
[Maven installed]:https://maven.apache.org/download.cgi
[Java JDK version 1.7 or above installed]:http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
[Almende]:http://www.almende.com
[Suki van Beusekom]:https://github.com/sukivb
