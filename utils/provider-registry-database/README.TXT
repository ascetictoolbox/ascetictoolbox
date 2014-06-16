Requirements:

	- Java VM >1.6
	- MySQL DB > 5.0
	- Create a database in your MySQL installation 
To install manually the database in an local mysql db, follow the next steps:

	$ pwd
	/root/utils/provider-registry-database
	$ export LIQUIBASE_HOME=/root/utils/provider-registry-database/liquibase
	$ liquibase/liquibase --driver=com.mysql.jdbc.Driver --changeLogFile=db/changelog/db.changelog-master.xml --url="jdbc:mysql://<URL_MYSQL_SERVER>/<NAME_OF_PREVIOUSLY_CREATED_DATABASE>" --username=<USERNAME> --password=<PASSWORD> migrate

