README document for the ASCETIC Experiment manager mojo.

h1. Abreviations :

SKB : SaaS Knowledge Base


h1. Developer's Corner

h2. Requirements 

* Maven 3.4 or higher
* Ascetic experiment runner compiled and installed in the local maven repository

h2. Build

To compile, just run :
mvn compile install

Now the mojo is executable as described bellow.

h1. Usage

2 uses cases :
- Upload the Experiment in the SaaS Knowledge Base
- Compute and upload the snapshot result


h2. Upload the experiment in the SaaS Knowledge Base

h3. Preconditions

* the SaaS Knowledge Base is running and accessible from the local computer
* the experiment configuration files is located into the src/main/resource folder
* the path to the SaaS Knowledge base is defined in the pom file in the properties "saas-knowledge-base.url".

h3. Running command

Just running the following command from the root of your project : 

mvn eu.ascetic.saas.experimentmanager:experiment-manager-mojo:save-experiment

h3. Results

If an experiment with this id already exists in the SKB, an error message is displayed to the user and the SKB remains unchanged.
Else, the experiment is stored in the SKB according to the persistent model.

h2. Compute and upload the snapshot result

h3. Preconditions

* the SaaS Knowledge Base is running and accessible from the local computer
* the experiment configuration files is located into the src/main/resource folder
* the path to the SaaS Knowledge base is defined in the pom file in the properties "saas-knowledge-base.url".
* the identifier of the deployment targeted by the experiment running in the properties "deployment.id".
* the scope configuration file is located into the src/main/resource folder
* the experiment related to the snapshot is already uploaded in the SKB.

h3. Running command

Just running the following command from the root of your project : 

mvn eu.ascetic.saas.experimentmanager:experiment-manager-mojo:run-experiment

h3. Results

If an experiment with this id already exists in the SKB, an error message is displayed to the user and the SKB remains unchanged.
Else, the experiment is stored in the SKB according to the persistent model.