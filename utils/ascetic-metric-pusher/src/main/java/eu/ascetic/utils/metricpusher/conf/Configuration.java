package eu.ascetic.utils.metricpusher.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class store configuration data for the metric pusher
 *
 */

public class Configuration {

	
	private static Logger logger = Logger.getLogger(Configuration.class);
	// This is the only parameter that can not be overwritten by the configuration file
	// It could be done in a different way... 
	private static final String metricPusherConfigurationFile = "/home/ubuntu/ascetic/iaas/metric-pusher/metric-pusher.properties";
	
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
