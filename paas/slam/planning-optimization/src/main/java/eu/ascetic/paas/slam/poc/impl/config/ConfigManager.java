/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */


package eu.ascetic.paas.slam.poc.impl.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;


public class ConfigManager {

	private static Properties properties = new Properties();

	private static ConfigManager instance = null;

	
	private ConfigManager() {
		try {
			properties.load(new FileInputStream(configFile));
		}
		catch (FileNotFoundException e) {
			LOGGER.error("Could not open POC config file: " + e.getMessage());
			e.printStackTrace();
			setDefaultProperties();
		} catch (IOException e) {
			LOGGER.error("Could not read properties in config file: " + e.getMessage());
			e.printStackTrace();
			setDefaultProperties();
		} 		
	}

	
	private static final String sepr = System.getProperty("file.separator");
	
	private static final String confPath = System.getenv("SLASOI_HOME");
	
	private static final String configFile = confPath + sepr
			+ "ascetic-slamanager" + sepr + "planning-optimization" + sepr
			+ "planning_optimization.properties";

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class);

	
	private static void setDefaultProperties() {
		LOGGER.info("Setting default properties");
		properties.put("terms", "cpu_speed,memory,vm_cores,price");
		properties.put("price_term_name", "price");
		properties.put("price_normalization", "ABS");
		properties.put("algorithm_class",
			"eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms.MaxOfferDistance");
		properties.put("max_offers", "4");
		properties.put("fed-api-host", "localhost");
		properties.put("fed-api-port", "4444");
		properties.put("fed-api-base-path", "api/cimi");
		properties.put("registry-endpoint", "https://providerregistry.apiary-mock.com/");
	}
	
	
	public static ConfigManager getInstance() {
		if (instance == null) {
			synchronized (ConfigManager.class) {
				instance = new ConfigManager();
			}
		}
		return instance;
	}

	
	public String[] getSupportedTerms() {
		return properties.getProperty("terms").split(",");
	}
	

	public String getPriceTermName() {
		return properties.getProperty("price_term_name"); 
	}

	
	public String getPriceNormalization() {
		return properties.getProperty("price_normalization"); 
	}

	
	public String getAlgorithmClass() {
		return properties.getProperty("algorithm_class"); 
	}
	

	public int getMaxOffers() {
		return Integer.parseInt(properties.getProperty("max_offers")); 
	}


	public String getFedApiHost() {
		return properties.getProperty("fed-api-host"); 
	}
	

	public String getFedApiPort() {
		return properties.getProperty("fed-api-port"); 
	}

	
	public String getFedApiBasePath() {
		return properties.getProperty("fed-api-base-path"); 
	}

	public String getRegistryEndpoint() {
		return properties.getProperty("registry-endpoint"); 
	}


}
