# ASCETiC Test Collection

## Configuration

There are two configuration files:

     ./src/main/resources/configuration.test.properties
     ./src/main/resources/configuration.stable.properties

Each file contians the configuration parameters for the test and stable environment respectively. Passing to maven the parameter "-Ptest" it will execute the test with the "configuration.test.properties" file, passing "-Pstable"
 it will run the test using the configuration.stable.properties file. Ommiting the parameter will result in an error.

 For example, to execute the test agaisnt the ASCETiC test environment:

      mvn -Ptest clean test

and for the stable one:

      mvn -Pstable clean test

The selection of one or other of the files is done by the profile section specified in the pom file. New profiles could be easily added for future ASCETiC installation environments.