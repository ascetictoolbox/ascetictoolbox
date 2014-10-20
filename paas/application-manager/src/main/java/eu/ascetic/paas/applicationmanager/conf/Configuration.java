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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 */

public class Configuration {
	private static Logger logger = Logger.getLogger(Configuration.class);
	public static String vmManagerServiceUrl = "http://10.4.0.15:34372/vmmanager";
	public static String checkDeploymentsStatus = "1 * * * * ?";
	private static final String applicationManagerConfigurationFile = "/etc/ascetic/paas/application-manager/application-manager.properties";
//	public static String vmcontextualizerConfigurationFileDirectory = "/etc/ascetic/paas/application-manager";
	public static String vmcontextualizerConfigurationFileDirectory = "/home/vmc";
	public static String slamURL = "http://10.4.0.16:8080/services/asceticNegotiation?wsdl";
	public static String enableSLAM = "no";
	public static String applicationMonitorUrl = "http://10.4.0.16:9000";
	
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
        	enableSLAM = config.getString("enable.slam");
        	applicationMonitorUrl = config.getString("application-monitor.url");
        }
        catch (Exception e) {
            logger.info("Error loading Application Manager configuration file");
            logger.info("Exception " + e);
        }  
    }

}
