README.TXT
----------

A example class called ZabbixApiClientTest is available in order to execute some tests. This class execute two methods to retrieve information from Zabbix server:
- getAllHosts: retrieve all hosts in Zabbix server
- getItemsFromHost: retrieve items from a host called "Zabbix server"
The execution output of this class is available in the same folder of this file, called "ZabbixApiClientTest class output.txt"

Methods available to get information from Zabbix:
- getAllHosts()
- getAllItems()
- getHostByName(hostName)
- getItemsFromHost(hostName)
- getItemFromHost(itemName, hostName)

Javadoc available in doc folder, one level up from here.

The ascetic-zabbix-api.properties file must be located in project classpath.

Don't hesitate to contact me if you have any question/problem: David Rojo - ATOS (david.rojoa@atos.net)