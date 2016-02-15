package eu.ascetic.test.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
	// PaaS Variables
	public static String applicationManagerURL = "";
	public static int queueSize = 100;
	public static String paasActiveMQUrl = "amqp://guest:guest@localhost:5672";
	
	static {
        try {
        	String propertiesFile = "config.properties";
        	
        	File f = new File(propertiesFile);
        	if(f.exists()) {
        		logger.info("ASCETiC tests configuration file: " + f.getAbsolutePath());
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	// PaaS Variables
        	applicationManagerURL = config.getString("application.manager.url");
        	paasActiveMQUrl = config.getString("paas.activemq.url");
        }
        catch (Exception e) {
            logger.info("Error loading ASCETiC Tests configuration file");
            logger.info("Exception " + e);
        }
	}
}
