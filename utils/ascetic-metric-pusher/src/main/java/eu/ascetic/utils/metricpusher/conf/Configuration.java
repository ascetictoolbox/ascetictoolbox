package eu.ascetic.utils.metricpusher.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Configuration {

	
	private static Logger logger = Logger.getLogger(Configuration.class);
	// This is the only parameter that can not be overwritten by the configuration file
	// It could be done in a different way... 
	private static final String metricPusherConfigurationFile = "/etc/ascetic/iaas/metric-pusher/metric-pusher.properties";
	
	// All this parameters can be overwritten by the application-manager.properties file
	public static String enableAMQP = "yes";
	public static String amqpAddress = "localhost:5673";
	public static String amqpUsername = "guest";
	public static String amqpPassword = "guest";
	public static String publishFrequency = "60000";
	public static String hostFilterBegins = "_wally";
	
	static {
        try {
        	String propertiesFile = "metric-pusher.properties";
        	
        	File f = new File(metricPusherConfigurationFile);
        	if(f.exists()) { 
        		propertiesFile = metricPusherConfigurationFile; 
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	enableAMQP = config.getString("enable.amqp");
        	amqpAddress = config.getString("amqp.address");
        	amqpUsername = config.getString("amqp.username");
        	amqpPassword = config.getString("amqp.password");
        	publishFrequency = config.getString("metricpusher.publish.frequency");
        	hostFilterBegins = config.getString("metricpusher.host.filter.begins");
        }
        catch (Exception e) {
            logger.info("Error loading Metric Pusher configuration file");
            logger.info("Exception " + e);
        }  
    }
	
}
