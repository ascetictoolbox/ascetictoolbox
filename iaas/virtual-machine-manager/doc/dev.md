# Developer/Deployer Manual

## Index

* [How to compile and run](#howtocompile)
* [Configuring properties files](#configuringproperties)
* [Repository structure](#projectstructure)
* [Secure access](#secureaccess)
* [Creating your own assembly](#creatingyourown)
* [Adding new policies](#addingnewpolicies)
* [Adding new drivers](#addingnewdrivers)

## How to compile and run <a name="howtocompile"/>

To compile:

	mvn clean install -DskipTests -P <profile>
	
Where `profile` can be `ascetic`, `renewit` or `fake`.

To run:

	java -jar dist/target/demiurge.jar

## Configuring properties files <a name="configuringproperties"/>

## Repository structure <a name="projectstructure"/>

* `core` defines the core functionalities, models and interfaces of _Demiurge_.
* `drivers` implements the drivers for different infrastructure and monitoring managers.
* `assemblies` contains subprojects with different configurations of _Demiurge_, plus some extra code to adapt
  it to different environments.
* `client` implements a simple REST client to facilitate the integration of _Demiurge_.
* `frontend` contains subprojects for GUI and REST services (WIP).

## Secure access <a name="secureaccess"/>

## Creating your own assembly <a name="creatingyourown"/>

## Adding new policies <a name="addingnewpolicies"/>

## Adding new drivers <a name="addingnewdrivers"/>