package eu.ascetic.paas.applicationmanager.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */
public class Configuration {
	private static Logger logger = Logger.getLogger(Configuration.class);
	// This is the only parameter that can not be overwritten by the configuration file
	// It could be done in a different way... 
	private static final String applicationManagerConfigurationFile = "/etc/ascetic/paas/application-manager/application-manager.properties";
	
	// All this parameters can be overwritten by the application-manager.properties file
	public static String enableSLAM = "no";
	public static String enableAMQP = "yes";
	public static String slamURL = "http://10.4.0.16:8080/services/asceticNegotiation?wsdl";
	public static String applicationMonitorUrl = "http://10.4.0.16:9000";
	public static String vmcontextualizerConfigurationFileDirectory = "/home/vmc";
	public static String applicationManagerUrl = "http://localhost";
	public static String amqpAddress = "localhost:5673";
	public static String amqpUsername = "guest";
	public static String amqpPassword = "guest";
	public static String emCalculateEnergyWhenDeletion = "yes";
	public static String emConfigurationFile = "/etc/ascetic/paas/em/config.properties";
	public static String emMeasurementsTopic = "MEASUREMENTS";
	public static String emPredictionsTopic = "PREDICTION";
	public static String providerRegistryEndpoint = "http://localhost/provider-registry";
	
	// TODO to remove this parameter, this configuration needs to be collected from the Provider Registry
	public static String vmManagerServiceUrl = "http://10.4.0.15:34372/vmmanager";
	public static String slaAgreementExpirationTime = "30"; // This value has to be specified in minutes 
	
	static {
        try {
        	String propertiesFile = "application-manager.properties";
        	
        	File f = new File(applicationManagerConfigurationFile);
        	if(f.exists()) {
        		logger.debug("Application Manager Propertires file: " + f.getAbsolutePath());
        		propertiesFile = applicationManagerConfigurationFile; 
        		logger.debug("Application Manager Propertires file loaded: " + propertiesFile);
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	enableSLAM = config.getString("enable.slam");
        	enableAMQP = config.getString("enable.amqp");
        	slamURL = config.getString("slam.url");
        	applicationMonitorUrl = config.getString("application-monitor.url");
        	vmcontextualizerConfigurationFileDirectory = config.getString("vmcontextualizer.configuration.file.directory");
        	applicationManagerUrl = config.getString("application-manager.url");
        	amqpAddress = config.getString("amqp.address");
        	amqpUsername = config.getString("amqp.username");
        	amqpPassword = config.getString("amqp.password");
        	emCalculateEnergyWhenDeletion = config.getString("em.calculate.energy.when.deletion");
        	emConfigurationFile = config.getString("em.configuration.file");
        	emMeasurementsTopic = config.getString("em.measurements.topic");
        	emPredictionsTopic = config.getString("em.predictions.topic");
        	providerRegistryEndpoint = config.getString("provider-registry-endpoint");
        	
        	// TODO to change this to be collected by the Provider Registry
        	vmManagerServiceUrl = config.getString("vm-manager.url");
        	slaAgreementExpirationTime = config.getString("sla-agreement-expiration-time");
        	
        }
        catch (Exception e) {
            logger.info("Error loading Application Manager configuration file");
            logger.info("Exception " + e);
        }  
    }

}
