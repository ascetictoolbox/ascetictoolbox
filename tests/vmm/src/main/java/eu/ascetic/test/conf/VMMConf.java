package eu.ascetic.test.conf;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


/**
 * 
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center 
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
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class VMMConf {
	private static Logger logger = Logger.getLogger(VMMConf.class);
	public static String vmManagerURL = "";
    public static String activeMqUrl = "";
    public static String imageId = "";
    public static String environment = "";
	
	static {
        try {
        	String propertiesFile = "config.properties";
        	
        	File f = new File(propertiesFile);
        	if(f.exists()) {
        		logger.info("ASCETiC tests configuration file: " + f.getAbsolutePath());
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	vmManagerURL = config.getString("vm.manager.url");
            activeMqUrl = config.getString("activemq.url");
            imageId = config.getString("image.id");
            environment = config.getString("environment");
        }
        catch (Exception e) {
            logger.info("Error loading ASCETiC Tests configuration file");
            logger.info("Exception " + e);
        }
	}
}