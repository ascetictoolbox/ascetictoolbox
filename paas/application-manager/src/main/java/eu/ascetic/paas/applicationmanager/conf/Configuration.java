package eu.ascetic.paas.applicationmanager.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Configuration {
	private static Logger logger = Logger.getLogger(Configuration.class);
	public static String vmManagerServiceUrl = "http://10.4.0.15:34372/vmmanager";
	public static String checkDeploymentsStatus = "1 * * * * ?";
	private static final String applicationManagerConfigurationFile = "/etc/ascetic/paas/application-manager/application-manager.properties";
//	public static String vmcontextualizerConfigurationFileDirectory = "/etc/ascetic/paas/application-manager";
	public static String vmcontextualizerConfigurationFileDirectory = "/home/vmc";
	public static String slamURL = "http://10.4.0.16:8080/services/asceticNegotiation?wsdl";
	
	static {
        try {
        	String propertiesFile = "application-manager.properties";
        	
        	File f = new File(applicationManagerConfigurationFile);
        	if(f.exists()) { 
        		propertiesFile = applicationManagerConfigurationFile; 
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	vmManagerServiceUrl = config.getString("vm-manager.url");
        	checkDeploymentsStatus = config.getString("check.deployments.status");
        	slamURL = config.getString("slam.url");
        }
        catch (Exception e) {
            logger.info("Error loading Application Manager configuration file");
            logger.info("Exception " + e);
        }  
    }

}
