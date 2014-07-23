package eu.ascetic.asceticarchitecture.iaas.zabbixApi.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * The Class Configuration.
 * 
 * @author David Rojo Antona - ATOS
 */
public class Configuration {
	
	private static Logger logger = Logger.getLogger(Configuration.class);
	
	/** The zabbix user. */
//	public static String zabbixUser = "Admin";
	public static String zabbixUser = "admin";
//	public static String zabbixPassword = "zabbix";
	public static String zabbixPassword = "73046447cce977b10167";
//	public static String zabbixUrl = "http://172.24.76.124/zabbix/api_jsonrpc.php";
	public static String zabbixUrl = "https://10.4.0.15/zabbix/api_jsonrpc.php";
	public static String virtualMachinesGroupName = "Virtual Machines";
	public static String osLinuxTemplateName = "Template OS Linux";
	
	private static final String zabbixConfigurationFile = "ascetic-zabbix-api.properties";
	
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
        	virtualMachinesGroupName = config.getString("zabbix.group.vm");
        	osLinuxTemplateName = config.getString("zabbix.template.linux");
        	}
        catch (Exception e) {
            logger.info("Error loading the configuration of the Zabbix server");
            logger.info("Exception " + e);
        }  
    }

}
