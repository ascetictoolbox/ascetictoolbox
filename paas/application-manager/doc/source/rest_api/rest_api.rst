```````````````````
REST API
```````````````````

In this document the REST API of the Application Manager it is presented.

Parsing XML
___________

To parse or create the XML that it is necessary to communicate with the Application Manager, it is recomended to import the project: application-manager-datamodel, that can be located in the repository under the folder::

	paas/application-manager-datamodel

It is a JAXB library, in the following unit test the developer can find enough examples about how to usen it to convert from Object to XML and viceversa::

	/src/test/java/eu/ascetic/paas/applicationmanager/model/converter/ModelConverterTest.java


API
___


Root (/)
~~~~~~~~

.. code-block:: xml

	GET /

	HTTP/1.1 200 OK
	Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<root xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" href="/">
	  <version>0.1-SNAPSHOT</version>
	  <timestamp>1436737762534</timestamp>
	  <link rel="applications" href="/applications" type="application/xml"/>
	</root>


/applications
~~~~~~~~~~~~~


/applications/<application-id>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

