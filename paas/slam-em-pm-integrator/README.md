# EM - SLAM Integrator

## Build and run

	mvn package
	java -jar target/emslamintegrator-0.0.1-SNAPSHOT.jar

## Configuration

The `resources/application.properties` file contains the default configuration options. However, you
can override them as java system properties in the form:

	java -Dpropertyname=value -jar target/emslamintegrator-0.0.1-SNAPSHOT.jar


Next are enumerated the configurable options that could vary depending on each deployment.

### Message Queue (ActiveMQ) properties

* `topic.name`
    - Name of the topic to listen for the *initiateMonitoring* commands
    - Default: `appmon`

* `spring.activemq.broker-url`
    - URL of the ActiveMQ broker.
    - Default value: `tcp://localhost:61616`

* `spring.activemq.user` and `spring.activemq.password`
    - User and password of the ActiveMQ broker, if any
    - Default: *undefined*

### Application Manager integration

* `application.manager.url`
    - URL of the REST endpoint for the Application Manager
    - Default: `http://localhost/application-manager`

### Other options

* `min.reporting.rate`
    - Minimum rate (in milliseconds), between each reporting from the modellers, for the same application/deployment
    - Default: 1000 ms.
	
* `logging.level.es.bsc`
    - Log level for package `es.bsc`
    - Default value: `debug`