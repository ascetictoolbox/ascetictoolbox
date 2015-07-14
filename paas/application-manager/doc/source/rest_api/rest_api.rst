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

Root of the REST service, it shows all the top level entities it can manage. In this case only **Aplications**.

**Request**::

	GET / HTTP/1.1

**Response**:

.. code-block:: xml
	:emphasize-lines: 1,2

	200 (OK)
	Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<root xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" href="/">
	  <version>0.1-SNAPSHOT</version>
	  <timestamp>1436737762534</timestamp>
	  <link rel="applications" href="/applications" type="application/xml"/>
	</root>


/applications
~~~~~~~~~~~~~

A GET over this path returns the list of applications that have been deployed at some point in the Application Manager.

**Request**::

	/applications / HTTP/1.1

**Response**:

.. code-block:: xml
	:emphasize-lines: 1,2

	200 (OK)
	Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<collection xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" 
			href="/applications">
    	<items offset="0" total="5">
        	<application href="/applications/JEPlus">
            	<id>4</id>
            	<name>JEPlus</name>
            	<link rel="parent" href="/applications" type="application/xml"/>
            	<link rel="self" href="/applications/JEPlus" type="application/xml"/>
            	<link rel="deployments" href="/applications/JEPlus/deployments" 
            		type="application/xml"/>
            	<link rel="cache-image" href="/applications/JEPlus/cache-images" 
            		type="application/xml"/>
        	</application>
	        <application href="/applications/NewsAsset">
	            <id>6</id>
	            <name>NewsAsset</name>
	            <link rel="parent" href="/applications" type="application/xml"/>
	            <link rel="self" href="/applications/NewsAsset" type="application/xml"/>
	            <link rel="deployments" href="/applications/NewsAsset/deployments" 
	            	type="application/xml"/>
	            <link rel="cache-image" href="/applications/NewsAsset/cache-images" 
	            	type="application/xml"/>
	        </application>
	    </items>
	    <link rel="parent" href="/" type="application/xml"/>
	    <link rel="self" href="/applications" type="application/xml"/>
	</collection>

A POST over this path allows to deploy an application. In this case it could happen two things:

* If the application does not exits in the Application Manager database, the application will be created and a deployment added to it.
* If the application already exits in the database. A deployment will be added to that application and registered in the Application Manager database.

**Request**::

	POST /applications?{negotiation} HTTP/1.1

.. csv-table:: Description of parameters for POST /applications
   :header: "Parameter", "Optional", "Description"
   :widths: 10, 10, 20

   "negotiation", "yes", "It allows two values: *manual* or *automatic*. By defualt it is *automatic*. If *manual* is selected it will enable the manual negotiation process, where the user needs to check the different agreements reached with the different IaaS providers and pick one. The defualt behaviour it is for the Application Manager to select the  best one."

The request body it must be a valid ASCETiC OVF document.

**Request body**:

.. code-block:: xml
	
	Content-Type: application/xml

	<ovf:Envelope xsi:schemaLocation="http://schemas.dmtf.org/ovf/envelope/1 ../dsp8023.xsd" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xmlns:vssd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData"
		xmlns:rasd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData" 
		xmlns:ovf="http://schemas.dmtf.org/ovf/envelope/1">
	  <!--References to all external files-->
	  <ovf:References>
	    <!--MySQL VM Image-->
	    <ovf:File ovf:id="mysqlA-img" ovf:href="/DFS/ascetic/vm-images/threeTierWebApp/mysql.img"/>
	    <ovf:File ovf:id="mysqlA-iso" ovf:href="/DFS/ascetic/vm-images/threeTierWebApp/mysql.iso"/>
	  </ovf:References>
	  <!--Describes meta-information about all virtual disks in the package.-->
	  <ovf:DiskSection>
	    <ovf:Info>List of the virtual disks used in the 3 Tier Web App package.</ovf:Info>
	    <!--MySQL Disk-->
	    <ovf:Disk ovf:diskId="mysql-img-disk" ovf:fileRef="mysqlA-img" 
	    	ovf:capacityAllocationUnits="byte * 2^30" ovf:capacity="20" 
	    	ovf:format="http://www.gnome.org/~markmc/qcow-image-format.html"/>
	    <ovf:Disk ovf:diskId="mysql-iso-disk" ovf:fileRef="mysqlA-iso" 
	    	ovf:format="http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-119.pdf" 
	    	ovf:capacityAllocationUnits="byte * 2^30" ovf:capacity="4" ovf:populatedSize="0"/>
	  </ovf:DiskSection>
	  <!--Describes all networks used in the package-->
	  <ovf:NetworkSection>
	    <ovf:Info>Logical networks used in the package.</ovf:Info>
	    <ovf:Network ovf:name="threeTierWebApp-net">
	      <ovf:Description>Network that the Web App will be available on.</ovf:Description>
	    </ovf:Network>
	  </ovf:NetworkSection>
	  <ovf:VirtualSystemCollection ovf:id="davidgpTestApp">
	    <ovf:Info>A basic three tier web application description.</ovf:Info>
	    <ovf:Name>Three Tier Web App</ovf:Name>
	    <!--Product specific attributes applicable to all VirtualSystems (VMs)-->
	    <ovf:ProductSection ovf:class="eu.ascetic.application">
	      <ovf:Info>Product customisation for the installed software.</ovf:Info>
	      <ovf:Product>DavidGPTestApp</ovf:Product>
	      <ovf:Version>1.0</ovf:Version>
	      <!--Product Properties that stores variables such as workload description,
	                deployment ID etc.-->
	    </ovf:ProductSection>
	    <!--MySQL Virtual System Instances Template-->
	    <ovf:VirtualSystem ovf:id="mysqlA">
	      <ovf:Info>MySQL Virtual System</ovf:Info>
	      <ovf:Name>MySQL</ovf:Name>
	      <!--Product specific attributes applicable to a single Virtual System
	                (VM)-->
	      <ovf:ProductSection ovf:class="eu.ascetic.vm">
	        <ovf:Info>Product customisation for the installed software.</ovf:Info>
	        <ovf:Product>MySQL</ovf:Product>
	        <ovf:Version>1.0</ovf:Version>
	        <!--Product Properties that stores variables such as probe end points-->
	        <ovf:Property ovf:key="asceticLowerBound" ovf:type="uint32" ovf:value="1"/>
	        <ovf:Property ovf:key="asceticUpperBound" ovf:type="uint32" ovf:value="2"/>
	        <ovf:Property ovf:key="asceticCacheImage" ovf:type="uint32" ovf:value="1"/>
	      </ovf:ProductSection>
	      <!--Operating System details-->
	      <ovf:OperatingSystemSection ovf:id="32" ovf:Version="6.5">
	        <ovf:Info>Specifies the operating system installed</ovf:Info>
	        <ovf:Description>CentOS Linux</ovf:Description>
	      </ovf:OperatingSystemSection>
	      <!--Hardware specification of the Virtual System (VM)-->
	      <ovf:VirtualHardwareSection>
	        <ovf:Info>Virtual Hardware Requirements</ovf:Info>
	        <ovf:System>
	          <vssd:ElementName>Virtual Hardware Family</vssd:ElementName>
	          <vssd:InstanceID>0</vssd:InstanceID>
	          <vssd:VirtualSystemType>kvm</vssd:VirtualSystemType>
	        </ovf:System>
	        <ovf:Item>
	          <rasd:Description>Number of virtual CPUs</rasd:Description>
	          <rasd:ElementName>1 virtual CPU</rasd:ElementName>
	          <rasd:InstanceID>1</rasd:InstanceID>
	          <rasd:ResourceType>3</rasd:ResourceType>
	          <rasd:VirtualQuantity>1</rasd:VirtualQuantity>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:AllocationUnits>hertz * 2^20</rasd:AllocationUnits>
	          <rasd:Description>CPU Speed</rasd:Description>
	          <rasd:ElementName>2000 MHz CPU speed reservation</rasd:ElementName>
	          <rasd:InstanceID>1</rasd:InstanceID>
	          <rasd:Reservation>2000</rasd:Reservation>
	          <rasd:ResourceSubType>cpuspeed</rasd:ResourceSubType>
	          <rasd:ResourceType>3</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>Memory Size</rasd:Description>
	          <rasd:ElementName>1024 MB of memory</rasd:ElementName>
	          <rasd:InstanceID>2</rasd:InstanceID>
	          <rasd:ResourceType>4</rasd:ResourceType>
	          <rasd:VirtualQuantity>1024</rasd:VirtualQuantity>
	          <rasd:VirtualQuantityUnits>byte * 2^20</rasd:VirtualQuantityUnits>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:AutomaticAllocation>true</rasd:AutomaticAllocation>
	          <rasd:Connection>threeTierWebApp-net</rasd:Connection>
	          <rasd:Description>Virtual Network</rasd:Description>
	          <rasd:ElementName>Ethernet adapter on threeTierWebApp-net
	                        network</rasd:ElementName>
	          <rasd:InstanceID>3</rasd:InstanceID>
	          <rasd:ResourceType>10</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>VM Disk</rasd:Description>
	          <rasd:ElementName>VM Disk Drive 1</rasd:ElementName>
	          <rasd:HostResource>ovf:/disk/mysql-img-disk</rasd:HostResource>
	          <rasd:InstanceID>4</rasd:InstanceID>
	          <rasd:ResourceType>17</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>VM CDROM</rasd:Description>
	          <rasd:ElementName>Context Base CD 1</rasd:ElementName>
	          <rasd:HostResource>ovf:/disk/mysql-iso-disk</rasd:HostResource>
	          <rasd:InstanceID>5</rasd:InstanceID>
	          <rasd:ResourceType>15</rasd:ResourceType>
	        </ovf:Item>
	      </ovf:VirtualHardwareSection>
	    </ovf:VirtualSystem>
	  </ovf:VirtualSystemCollection>
	</ovf:Envelope>

In the response message we are ommiting to put again the OVF in the ovf field for brevety reasons.

**Response**:

.. code-block:: xml
	
	201 (CREATED)
	Content-Type: application/xml

	<application href="/applications/davidgpTestApp">
	        <deployment href="/applications/davidgpTestApp/deployments/462">
	            <id>462</id>
	            <status>SUBMITTED</status>
	            <start-date>13/07/2015:08:11:59 +0000</start-date>
	            <ovf>OVF SUBMITTED WITH THE TEXT ESCAPED</ovf>
	            <vms/>
	            <link rel="parent" href="/applications/davidgpTestApp/deployments" 
	            	type="application/xml"/>
	            <link rel="self" href="/applications/davidgpTestApp/deployments/462" 
	            	type="application/xml"/>
	            <link rel="ovf" href="/applications/davidgpTestApp/deployments/462/ovf" 
	            	type="application/xml"/>
	            <link rel="vms" href="/applications/davidgpTestApp/deployments/462/vms" 
	            	type="application/xml"/>
	            <link rel="energy-consumption" 
	            	href="/applications/davidgpTestApp/deployments/462/energy-consumption" 
	            	type="application/xml"/>
	            <link rel="agreements" 
	            	href="/applications/davidgpTestApp/deployments/462/agreements" 
	            	type="application/xml"/>
	        </deployment>
	    </deployments>
	    <link rel="parent" href="/applications" type="application/xml"/>
	    <link rel="self" href="/applications/davidgpTestApp" type="application/xml"/>
	    <link rel="deployments" href="/applications/davidgpTestApp/deployments" 
	    	type="application/xml"/>
	    <link rel="cache-image" href="/applications/davidgpTestApp/cache-images" 
	    	type="application/xml"/>
	</application>


/applications/{application-id}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For this path, only a GET method has been implemented so far.

**Request**::

	GET /applications/{application-id} HTTP/1.1

.. csv-table:: Description of parameters for GET /applications/{application-id}
   :header: "Parameter", "Optional", "Description"
   :widths: 10, 10, 20

   "application-id", "no", "Name of the application from which we want to get the information."

**Response**:

.. code-block:: xml
	:emphasize-lines: 1,2

	200 (OK)
	Content-Type: application/xml

	<application xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" 
		href="/applications/davidgpTestApp">
	    <id>5</id>
	    <name>davidgpTestApp</name>
	    <link rel="parent" href="/applications" type="application/xml"/>
	    <link rel="self" href="/applications/davidgpTestApp" 
	    	type="application/xml"/>
	    <link rel="deployments" href="/applications/davidgpTestApp/deployments" 
	    	type="application/xml"/>
	    <link rel="cache-image" href="/applications/davidgpTestApp/cache-images" 
	    	type="application/xml"/>
	</application>

/applications/{application-id}/deployments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

It returns all the deployments for an application. The returned list can be filtered by the *status* parameter.

**Request**::

	GET /applications/{application-id}/deployments?{status} HTTP/1.1

.. csv-table:: GET /applications/{application-id}/deployments
   :header: "Parameter", "Optional", "Description"
   :widths: 10, 10, 20

   "application-id", "no", "Name of the application from which we want to get the information."
   "status", "yes", "It filters the list of deployments by their status, for example, if the user inputs status=DEPLOYED, it will only return the current deployed deployments"


**Response**:

.. code-block:: xml
  :emphasize-lines: 1,2

	200 (OK)
	Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<collection xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" 
		href="/applications/davidgpTestApp/deployments">
		<items offset="0" total="2">
			<deployment href="/applications/davidgpTestApp/deployments/452">
	    	<id>452</id>
	      <status>DEPLOYED</status>
	      <price>120.0</price>
	      <start-date>09/06/2015:14:55:01 +0000</start-date>
	      <ovf>DELETED FOR BREVETY</ovf>
	      <vms>
	      	<vm href="/applications/davidgpTestApp/deployments/452/vms/1703">
	        	<id>1703</id>
	          <ovf-id>mysqlA</ovf-id>
	          <provider-vm-id>409ecf78-6426-4356-8bf0-4c79b74be1ea</provider-vm-id>
	          <status>ACTIVE</status>
	          <ip>10.4.0.24</ip>
	          <images>
	         		<image>
	              <id>291</id>
	              <ovf-id>mysqlA-img</ovf-id>
	              <provider-image-id>b3c711ff-c553-4089-a3a3-4277c0de6cb0</provider-image-id>
	              <ovf-href>/DFS/ascetic/vm-images/threeTierWebApp/mysql.img</ovf-href>
	            	<demo>true</demo>
	          	</image>
	          </images>
	          <cpu-max>-1</cpu-max>
	          <cpu-min>-1</cpu-min>
	          <cpu-actual>-1</cpu-actual>
	          <ram-max>-1</ram-max>
	          <ram-min>-1</ram-min>
	          <ram-actual>-1</ram-actual>
	          <swap-max>-1</swap-max>
	          <swap-min>-1</swap-min>
	          <swap-actual>-1</swap-actual>
	          <disk-max>-1</disk-max>
	          <disk-min>-1</disk-min>
	          <disk-actual>-1</disk-actual>
	          <number-vms-max>-1</number-vms-max>
	          <number-vms-min>-1</number-vms-min>
	         	<link rel="parent" href="/applications/davidgpTestApp/deployments/452/vms" 
	          	type="application/xml"/>
	        	<link rel="self" href="/applications/davidgpTestApp/deployments/452/vms/1703" 
	        		type="application/xml"/>
	        </vm>
	        <vm href="/applications/davidgpTestApp/deployments/452/vms/1704">
	          <id>1704</id>
	          <ovf-id>mysqlA</ovf-id>
	          <provider-vm-id>597da2ce-8f78-4475-8483-d43ed936e566</provider-vm-id>
	          <status>ACTIVE</status>
	          <ip>10.4.0.28</ip>
	          <images>
	            <image>
	              <id>291</id>
	              <ovf-id>mysqlA-img</ovf-id>
	              <provider-image-id>b3c711ff-c553-4089-a3a3-4277c0de6cb0</provider-image-id>
	              <ovf-href>/DFS/ascetic/vm-images/threeTierWebApp/mysql.img</ovf-href>
	            	<demo>true</demo>
	          	</image>
	          </images>
	          <cpu-max>-1</cpu-max>
	          <cpu-min>-1</cpu-min>
	          <cpu-actual>-1</cpu-actual>
	          <ram-max>-1</ram-max>
	          <ram-min>-1</ram-min>
	          <ram-actual>-1</ram-actual>
	          <swap-max>-1</swap-max>
	          <swap-min>-1</swap-min>
	          <swap-actual>-1</swap-actual>
	          <disk-max>-1</disk-max>
	          <disk-min>-1</disk-min>
	          <disk-actual>-1</disk-actual>
	          <number-vms-max>-1</number-vms-max>
	          <number-vms-min>-1</number-vms-min>
	          <link rel="parent" href="/applications/davidgpTestApp/deployments/452/vms" 
	          	type="application/xml"/>
	        	<link rel="self" href="/applications/davidgpTestApp/deployments/452/vms/1704" 
	          	type="application/xml"/>
	      	</vm>
	      </vms>
	      <link rel="parent" href="/applications/davidgpTestApp/deployments" type="application/xml"/>
	      <link rel="self" href="/applications/davidgpTestApp/deployments/452" type="application/xml"/>
	      <link rel="ovf" href="/applications/davidgpTestApp/deployments/452/ovf" type="application/xml"/>
	      <link rel="vms" href="/applications/davidgpTestApp/deployments/452/vms" type="application/xml"/>
	      <link rel="energy-consumption" href="/applications/davidgpTestApp/deployments/452/energy-consumption" 
	      	type="application/xml"/>
	    	<link rel="agreements" href="/applications/davidgpTestApp/deployments/452/agreements" 
	    		type="application/xml"/>
	    </deployment>
			<deployment href="/applications/davidgpTestApp/deployments/462">
	    	<id>462</id>
	      <status>DEPLOYED</status>
	      <start-date>13/07/2015:08:11:59 +0000</start-date>
	      <ovf>DELETED FOR BREVETY</ovf>
	      <vms>
	      	<vm href="/applications/davidgpTestApp/deployments/462/vms/1713">
	        	<id>1713</id>
	          <ovf-id>mysqlA</ovf-id>
	          <provider-vm-id>8870d0e5-c39a-44a7-ae6a-4be31e349092</provider-vm-id>
	          <status>ACTIVE</status>
	          <ip>10.4.0.24</ip>
	          <images>
	          	<image>
	            	<id>291</id>
	              <ovf-id>mysqlA-img</ovf-id>
	              <provider-image-id>b3c711ff-c553-4089-a3a3-4277c0de6cb0</provider-image-id>
	              <ovf-href>/DFS/ascetic/vm-images/threeTierWebApp/mysql.img</ovf-href>
	              <demo>true</demo>
	            </image>
	          </images>
	          <cpu-max>0</cpu-max>
	          <cpu-min>0</cpu-min>
	          <cpu-actual>0</cpu-actual>
	          <ram-max>0</ram-max>
	          <ram-min>0</ram-min>
	          <ram-actual>0</ram-actual>
	          <swap-max>0</swap-max>
	          <swap-min>0</swap-min>
	          <swap-actual>0</swap-actual>
	          <disk-max>0</disk-max>
	          <disk-min>0</disk-min>
	          <disk-actual>0</disk-actual>
	          <number-vms-max>0</number-vms-max>
	          <number-vms-min>0</number-vms-min>
	          <link rel="parent" href="/applications/davidgpTestApp/deployments/462/vms" 
	            type="application/xml"/>
	          <link rel="self" href="/applications/davidgpTestApp/deployments/462/vms/1713" 
	            type="application/xml"/>
	        </vm>
	        <vm href="/applications/davidgpTestApp/deployments/462/vms/1714">
	        	<id>1714</id>
	          <ovf-id>mysqlA</ovf-id>
	          <provider-vm-id>5574b6ab-cbfe-4f3d-9db0-87ece3b58f14</provider-vm-id>
	          <status>ACTIVE</status>
	          <ip>10.4.0.28</ip>
	          <images>
	          	<image>
	            	<id>291</id>
	              <ovf-id>mysqlA-img</ovf-id>
	              <provider-image-id>b3c711ff-c553-4089-a3a3-4277c0de6cb0</provider-image-id>
	              <ovf-href>/DFS/ascetic/vm-images/threeTierWebApp/mysql.img</ovf-href>
	              <demo>true</demo>
	            </image>
	          </images>
	          <cpu-max>0</cpu-max>
	          <cpu-min>0</cpu-min>
	          <cpu-actual>0</cpu-actual>
	          <ram-max>0</ram-max>
	          <ram-min>0</ram-min>
	          <ram-actual>0</ram-actual>
	          <swap-max>0</swap-max>
	          <swap-min>0</swap-min>
	          <swap-actual>0</swap-actual>
	          <disk-max>0</disk-max>
	          <disk-min>0</disk-min>
	          <disk-actual>0</disk-actual>
	          <number-vms-max>0</number-vms-max>
	          <number-vms-min>0</number-vms-min>
	          <link rel="parent" href="/applications/davidgpTestApp/deployments/462/vms" 
	          	type="application/xml"/>
	          <link rel="self" href="/applications/davidgpTestApp/deployments/462/vms/1714" 	
	          	type="application/xml"/>
	        </vm>
	      </vms>
	      <link rel="parent" href="/applications/davidgpTestApp/deployments" 
	      	type="application/xml"/>
	      <link rel="self" href="/applications/davidgpTestApp/deployments/462" 
	        type="application/xml"/>
	      <link rel="ovf" href="/applications/davidgpTestApp/deployments/462/ovf" 
	      	type="application/xml"/>
	      <link rel="vms" href="/applications/davidgpTestApp/deployments/462/vms" 
	      	type="application/xml"/>
	      <link rel="energy-consumption" href="/applications/davidgpTestApp/deployments/462/energy-consumption" 
	      	type="application/xml"/>
	      <link rel="agreements" href="/applications/davidgpTestApp/deployments/462/agreements" 
	      	type="application/xml"/>
	    </deployment>
	  </items>
	  <link rel="parent" href="/applications/davidgpTestApp" type="application/xml"/>
	  <link rel="self" href="/applications/davidgpTestApp/deployments" type="application/xml"/>
	</collection>

A POST over this path allows to deploy an application. In this case it could happen two things:

* If the application does not exits the query will return an error.
* If the application already exits in the database. A deployment will be added to that application and registered in the Application Manager database.

**Request**::

	POST /applications/{application-id}/deployments?{negotiation} HTTP/1.1

.. csv-table:: Description of parameters for POST /applications
   :header: "Parameter", "Optional", "Description"
   :widths: 10, 10, 20

   "negotiation", "yes", "It allows two values: *manual* or *automatic*. By defualt it is *automatic*. If *manual* is selected it will enable the manual negotiation process, where the user needs to check the different agreements reached with the different IaaS providers and pick one. The defualt behaviour it is for the Application Manager to select the  best one."

The request body it must be a valid ASCETiC OVF document.

**Request body**:

.. code-block:: xml
	
	Content-Type: application/xml

	<ovf:Envelope xsi:schemaLocation="http://schemas.dmtf.org/ovf/envelope/1 ../dsp8023.xsd" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xmlns:vssd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData"
		xmlns:rasd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData" 
		xmlns:ovf="http://schemas.dmtf.org/ovf/envelope/1">
	  <!--References to all external files-->
	  <ovf:References>
	    <!--MySQL VM Image-->
	    <ovf:File ovf:id="mysqlA-img" ovf:href="/DFS/ascetic/vm-images/threeTierWebApp/mysql.img"/>
	    <ovf:File ovf:id="mysqlA-iso" ovf:href="/DFS/ascetic/vm-images/threeTierWebApp/mysql.iso"/>
	  </ovf:References>
	  <!--Describes meta-information about all virtual disks in the package.-->
	  <ovf:DiskSection>
	    <ovf:Info>List of the virtual disks used in the 3 Tier Web App package.</ovf:Info>
	    <!--MySQL Disk-->
	    <ovf:Disk ovf:diskId="mysql-img-disk" ovf:fileRef="mysqlA-img" 
	    	ovf:capacityAllocationUnits="byte * 2^30" ovf:capacity="20" 
	    	ovf:format="http://www.gnome.org/~markmc/qcow-image-format.html"/>
	    <ovf:Disk ovf:diskId="mysql-iso-disk" ovf:fileRef="mysqlA-iso" 
	    	ovf:format="http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-119.pdf" 
	    	ovf:capacityAllocationUnits="byte * 2^30" ovf:capacity="4" ovf:populatedSize="0"/>
	  </ovf:DiskSection>
	  <!--Describes all networks used in the package-->
	  <ovf:NetworkSection>
	    <ovf:Info>Logical networks used in the package.</ovf:Info>
	    <ovf:Network ovf:name="threeTierWebApp-net">
	      <ovf:Description>Network that the Web App will be available on.</ovf:Description>
	    </ovf:Network>
	  </ovf:NetworkSection>
	  <ovf:VirtualSystemCollection ovf:id="davidgpTestApp">
	    <ovf:Info>A basic three tier web application description.</ovf:Info>
	    <ovf:Name>Three Tier Web App</ovf:Name>
	    <!--Product specific attributes applicable to all VirtualSystems (VMs)-->
	    <ovf:ProductSection ovf:class="eu.ascetic.application">
	      <ovf:Info>Product customisation for the installed software.</ovf:Info>
	      <ovf:Product>DavidGPTestApp</ovf:Product>
	      <ovf:Version>1.0</ovf:Version>
	      <!--Product Properties that stores variables such as workload description,
	                deployment ID etc.-->
	    </ovf:ProductSection>
	    <!--MySQL Virtual System Instances Template-->
	    <ovf:VirtualSystem ovf:id="mysqlA">
	      <ovf:Info>MySQL Virtual System</ovf:Info>
	      <ovf:Name>MySQL</ovf:Name>
	      <!--Product specific attributes applicable to a single Virtual System
	                (VM)-->
	      <ovf:ProductSection ovf:class="eu.ascetic.vm">
	        <ovf:Info>Product customisation for the installed software.</ovf:Info>
	        <ovf:Product>MySQL</ovf:Product>
	        <ovf:Version>1.0</ovf:Version>
	        <!--Product Properties that stores variables such as probe end points-->
	        <ovf:Property ovf:key="asceticLowerBound" ovf:type="uint32" ovf:value="1"/>
	        <ovf:Property ovf:key="asceticUpperBound" ovf:type="uint32" ovf:value="2"/>
	        <ovf:Property ovf:key="asceticCacheImage" ovf:type="uint32" ovf:value="1"/>
	      </ovf:ProductSection>
	      <!--Operating System details-->
	      <ovf:OperatingSystemSection ovf:id="32" ovf:Version="6.5">
	        <ovf:Info>Specifies the operating system installed</ovf:Info>
	        <ovf:Description>CentOS Linux</ovf:Description>
	      </ovf:OperatingSystemSection>
	      <!--Hardware specification of the Virtual System (VM)-->
	      <ovf:VirtualHardwareSection>
	        <ovf:Info>Virtual Hardware Requirements</ovf:Info>
	        <ovf:System>
	          <vssd:ElementName>Virtual Hardware Family</vssd:ElementName>
	          <vssd:InstanceID>0</vssd:InstanceID>
	          <vssd:VirtualSystemType>kvm</vssd:VirtualSystemType>
	        </ovf:System>
	        <ovf:Item>
	          <rasd:Description>Number of virtual CPUs</rasd:Description>
	          <rasd:ElementName>1 virtual CPU</rasd:ElementName>
	          <rasd:InstanceID>1</rasd:InstanceID>
	          <rasd:ResourceType>3</rasd:ResourceType>
	          <rasd:VirtualQuantity>1</rasd:VirtualQuantity>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:AllocationUnits>hertz * 2^20</rasd:AllocationUnits>
	          <rasd:Description>CPU Speed</rasd:Description>
	          <rasd:ElementName>2000 MHz CPU speed reservation</rasd:ElementName>
	          <rasd:InstanceID>1</rasd:InstanceID>
	          <rasd:Reservation>2000</rasd:Reservation>
	          <rasd:ResourceSubType>cpuspeed</rasd:ResourceSubType>
	          <rasd:ResourceType>3</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>Memory Size</rasd:Description>
	          <rasd:ElementName>1024 MB of memory</rasd:ElementName>
	          <rasd:InstanceID>2</rasd:InstanceID>
	          <rasd:ResourceType>4</rasd:ResourceType>
	          <rasd:VirtualQuantity>1024</rasd:VirtualQuantity>
	          <rasd:VirtualQuantityUnits>byte * 2^20</rasd:VirtualQuantityUnits>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:AutomaticAllocation>true</rasd:AutomaticAllocation>
	          <rasd:Connection>threeTierWebApp-net</rasd:Connection>
	          <rasd:Description>Virtual Network</rasd:Description>
	          <rasd:ElementName>Ethernet adapter on threeTierWebApp-net
	                        network</rasd:ElementName>
	          <rasd:InstanceID>3</rasd:InstanceID>
	          <rasd:ResourceType>10</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>VM Disk</rasd:Description>
	          <rasd:ElementName>VM Disk Drive 1</rasd:ElementName>
	          <rasd:HostResource>ovf:/disk/mysql-img-disk</rasd:HostResource>
	          <rasd:InstanceID>4</rasd:InstanceID>
	          <rasd:ResourceType>17</rasd:ResourceType>
	        </ovf:Item>
	        <ovf:Item>
	          <rasd:Description>VM CDROM</rasd:Description>
	          <rasd:ElementName>Context Base CD 1</rasd:ElementName>
	          <rasd:HostResource>ovf:/disk/mysql-iso-disk</rasd:HostResource>
	          <rasd:InstanceID>5</rasd:InstanceID>
	          <rasd:ResourceType>15</rasd:ResourceType>
	        </ovf:Item>
	      </ovf:VirtualHardwareSection>
	    </ovf:VirtualSystem>
	  </ovf:VirtualSystemCollection>
	</ovf:Envelope>

In the response message we are ommiting to put again the OVF in the ovf field for brevety reasons.

**Response**:

.. code-block:: xml
  :emphasize-lines: 1,2

	200 (OK)
	Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<deployment xmlns="http://application_manager.ascetic.eu/doc/schemas/xml" 
		href="/applications/davidgpTestApp/deployments/463">
	  <id>463</id>
	  <status>SUBMITTED</status>
	  <start-date>13/07/2015:10:18:33 +0000</start-date>
	  <ovf>OMITTED FOR BREVITY</ovf>
	  <link rel="parent" 
	  	href="/applications/davidgpTestApp/deployments" 
	  	type="application/xml"/>
	  <link rel="self" 
	  	href="/applications/davidgpTestApp/deployments/463" 
	  	type="application/xml"/>
	  <link rel="ovf" 
	  	href="/applications/davidgpTestApp/deployments/463/ovf" 
	  	type="application/xml"/>
	  <link rel="vms" 
	  	href="/applications/davidgpTestApp/deployments/463/vms" 
	  	type="application/xml"/>
	  <link rel="energy-consumption" 
	  	href="/applications/davidgpTestApp/deployments/463/energy-consumption" 
	  	type="application/xml"/>
	  <link rel="agreements" 
	  	href="/applications/davidgpTestApp/deployments/463/agreements" 
	  	type="application/xml"/>
	</deployment>

/applications/{application-id}/deployments/{deployment-id}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

