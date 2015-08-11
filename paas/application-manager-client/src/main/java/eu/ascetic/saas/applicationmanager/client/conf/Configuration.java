package eu.ascetic.saas.applicationmanager.client.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


public class Configuration {

		private static Logger logger = Logger.getLogger(Configuration.class);
		// This is the only parameter that can not be overwritten by the configuration file
		// It could be done in a different way... 
		private static final String applicationPackagerConfigurationFile = "/etc/ascetic/paas/application-manager/application-manager.properties";
		
		// All this parameters can be overwritten by the application-manager.properties file
//		public static String applicationManagerUrl = "http://10.4.0.16/application-manager"; //Y1 AppManager
		public static String applicationManagerUrl = "http://192.168.3.16/application-manager"; //Y2 Testing AppManager
//		public static String applicationManagerUrl = "http://192.168.3.222/application-manager"; //Y2 Stable AppManager
	
		static {
			try {
	        	String propertiesFile = "application-manager.properties";
	        	
	        	File f = new File(applicationPackagerConfigurationFile);
	        	if(f.exists()) { 
	        		propertiesFile = applicationPackagerConfigurationFile; 
	        	}
	        	
	        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
	        	applicationManagerUrl = config.getString("application-manager.url");

	        }
	        catch (Exception e) {
	            logger.info("Error loading Application Manager configuration file");
	            logger.info("Exception " + e);
	        } 

		}
}
