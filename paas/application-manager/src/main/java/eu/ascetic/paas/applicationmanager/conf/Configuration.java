package eu.ascetic.paas.applicationmanager.conf;

import java.io.File;


import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Configuration {
	private static Logger logger = Logger.getLogger(Configuration.class);
	public static String vmManagerServiceUrl = "http://10.4.0.15:34372/vmmanager";
	private static final String applicationManagerConfigurationFile = "/etc/ascetic/paas/application-manager/application-manager.properties";
	
	static {
        try {
        	String propertiesFile = "application-manager.properties";
        	
        	File f = new File(applicationManagerConfigurationFile);
        	if(f.exists()) { 
        		propertiesFile = applicationManagerConfigurationFile; 
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	vmManagerServiceUrl = config.getString("vm-manager.url");
        }
        catch (Exception e) {
            logger.info("Error loading Application Manager configuration file");
            logger.info("Exception " + e);
        }  
    }
}
