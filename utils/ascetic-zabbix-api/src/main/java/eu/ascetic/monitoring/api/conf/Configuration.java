package eu.ascetic.monitoring.api.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Configuration {
	
	private static Logger logger = Logger.getLogger(Configuration.class);
	public static String zabbixUser = "Admin";
	public static String zabbixPassword = "zabbix";
	public static String zabbixUrl = "http://172.24.76.124/zabbix/api_jsonrpc.php";
	
	private static final String zabbixConfigurationFile = "C://tests/ascetic-zabbix-api.properties";
	
	static {
        try {
        	String propertiesFile = "ascetic-zabbix-api.properties";
        	
        	File f = new File(zabbixConfigurationFile);
        	if(f.exists()) { 
        		propertiesFile = zabbixConfigurationFile; 
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	zabbixUrl = config.getString("zabbix.server.url");
        	zabbixPassword = config.getString("zabbix.password");
        	zabbixUser = config.getString("zabbix.user");
        }
        catch (Exception e) {
            logger.info("Error loading the configuration of the Zabbix server");
            logger.info("Exception " + e);
        }  
    }

}
