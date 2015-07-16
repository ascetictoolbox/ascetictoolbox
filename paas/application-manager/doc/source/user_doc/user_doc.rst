```````````````````
User Documentation
```````````````````

Installation
____________

Requirements
~~~~~~~~~~~~

The Application Manager requieres a previous installation of the following software in the place where it is going to be deployed.

* JDK 1.7.x (It is not compatible with Java 8)
* MySQL 5.x
* Tomcat 7

Compilation
~~~~~~~~~~~~

From the ASCETiC svn go the following folder::

  /svn/trunk/paas/application-manager/

There execute the following command to create the Application Manager war file (add to the previous line the -U option if you want to update third party libraries the Application Manager relies on, such as: Energy Modeller, Price Modeller, ...)::

  mvn -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true clean package

The war file will be located at the folder::

    /svn/trunk/paas/application-manager/target

For deployment it is necessary to rename to "application-manager.war"

Preparation of the database
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This step it is only necessary for new installations of the Application Manager or if the upgrade of it requires an update of the database schema.

In the svn there is the following project::

  /svn/trunk/utils/application-manager-database

Copy the contents to a place that has internet connection to the database and from it execute the following command as it is comented in the README file::

  liquibase/liquibase --driver=com.mysql.jdbc.Driver 
      --changeLogFile=db/changelog/db.changelog-master.xml 
      --url="jdbc:mysql://<URL_MYSQL_SERVER>/<NAME_OF_PREVIOUSLY_CREATED_DATABASE>" 
      --username=<USERNAME> --password=<PASSWORD> migrate

Change the different parameters for the actual installation of that database.

Note: Typical ASCETiC testing installation use for username: app-manager, for password: ascetic-secret and for database name: application_manager .

Deployment/Redeployment
~~~~~~~~~~~~~~~~~~~~~~~~

The Application Manager needs that its two configuration files to be located at::

  /etc/ascetic/paas/application-manager

There you can find the following two files::

  application-manager.properties  
  persistence.mysql.xml

The contents of Application Manager::

  # Application Manager Configuration
  check.deployments.status = 10 * * * * ?
  enable.slam=yes
  slam.url=http://10.4.0.16:8080/services/asceticNegotiation?wsdl
  # TODO - This needs to be changed to be collected by the Provider Registry
  vm-manager.url=http://iaas-vm-dev:34372/vmmanager
  #vm-manager.url=http://localhost:8080/vmmanager
  application-monitor.url=http://10.4.0.16:9000
  amqp.address=localhost:5672
  amqp.username=guest
  amqp.password=guest
  enable.amqp=yes
  vmcontextualizer.configuration.file.directory=/home/vmc
  em.calculate.energy.when.deletion=no
  application-manager.url=http://10.4.0.16
  sla-agreement-expiration-time=30

In detail, each one of those parameters:

.. csv-table:: Description of parameters for application-manager.properties
   :header: "Parameter", "Description"
   :widths: 10, 10

   "check.deployments.status", "(DEPRECATED) It only applies to Y1 Application Manager, it is the periocity in which the Application Manager checks if it is necessary to progress with the workflow of a deployment."
   "enable.slam", "To activate the negotiation process. Interaction between SLAM and Application Manager"
   "slam.url", "URL of the PaaS SLAM"
   "vm-manager.url", "(TO BE DEPRECATED) Y2 should read this from Provider Registry"
   "application-monitor.url", "URL of the PaaS Application Monitor"
   "amqp.address", "URL of the AMQP broker"
   "amqp.username", "User to access the AMQP broker"
   "amqp.password", "Password to access the AMQP broker"
   "enable.amqp", "Tells the Application Manager to use or not the AMQP"
   "vmcontextualizer.configuration.file.directory", "Location of the VMC home folder."
   "em.calculate.energy.when.deletion", "Enables the Application Manager to call the Energy Modeller to archive in the Application Monitor how much a deployment has consumed just before deleting it."
   "sla-agreement-expiration-time", "Time before an agreement expires in minutes."

The contents of persistence.mysql.xml

.. code-block:: xml

  <persistence xmlns="http://java.sun.com/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
    version="1.0">

    <persistence-unit name="applicationManagerDB" transaction-type="RESOURCE_LOCAL">
    <class>eu.ascetic.paas.applicationmanager.model.Application</class>
    <class>eu.ascetic.paas.applicationmanager.model.Deployment</class>
    <class>eu.ascetic.paas.applicationmanager.model.VM</class>
    <class>eu.ascetic.paas.applicationmanager.model.Image</class>
    <class>eu.ascetic.paas.applicationmanager.model.Agreement</class>
    <properties>
      <property name="hibernate.archive.autodetection" value="class, hbm"/>
      <property name="hibernate.show_sql" value="true"/>
      <property name="hibernate.connection.driver_class" value="com.mysql.jdbc.Driver"/>
      <property name="hibernate.connection.password" value="ascetic-secret"/>
      <property name="hibernate.connection.url" 
        value="jdbc:mysql://localhost:3306/application_manager"/>
      <property name="hibernate.connection.username" value="app-manager"/>
      <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
      <property name="hibernate.hbm2ddl.auto" value="validate"/>
      <property name="hibernate.c3p0.acquire_increment" value="1" />
      <property name="hibernate.c3p0.min_size" value="5" />
      <property name="hibernate.c3p0.max_size" value="20" />
      <property name="hibernate.c3p0.timeout" value="300" />
      <property name="hibernate.c3p0.max_statements" value="50" />
      <property name="hibernate.c3p0.idle_test_period" value="3000" />
    </properties>
  </persistence-unit>
    </persistence>

In this case we are only interested in modifying the following three parameters, the rest should be left untouched:

.. csv-table:: Description of parameters for application-manager.properties
   :header: "Parameter", "Description"
   :widths: 10, 10

   "hibernate.connection.url", "The MySQL database url expressed in JDBC format."
   "hibernate.connection.username", "Username to connect to the MySQL server."
   "hibernate.connection.password", "Password to connect to the MySQL server."

To deploy or redeploy the Application Manager, remove first the application-manager.war file from the webapps folder of tomcat (in typical ASCETiC installation it is located at /user/lib/tomcat7/webapps). Wait until the "application-manager" folder inside the "webapps" folder disappears. Since the undeployment by tomcat can leave garbage in the memory, it is recomened to restart tomcat::

  /etc/init.d/tomcat7 restart

Once tomcat has been restarted, copy the new .war file into the "webapps" folder and in a minute the new Application Manager will be ready to be used.