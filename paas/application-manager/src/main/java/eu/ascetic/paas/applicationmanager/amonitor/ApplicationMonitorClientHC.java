package eu.ascetic.paas.applicationmanager.amonitor;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.Dictionary;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;
import eu.ascetic.paas.applicationmanager.amonitor.model.parser.Parser;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.http.Client;

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
 * e-mail david.garciaperez@atos.net
 * 
 * Apache HTTPD implementation of the Client to Application Monitor
 */
public class ApplicationMonitorClientHC implements ApplicationMonitorClient {
	private static Logger logger = Logger.getLogger(ApplicationMonitorClientHC.class);
	private String url;
	
	public ApplicationMonitorClientHC() {
		url = Configuration.applicationMonitorUrl;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public void setURL(String url) {
		this.url = url;
	}

	@Override
	public boolean postFinalEnergyConsumption(EnergyCosumed energyCosumed) {
		Boolean exception = false;
		String applicationMonitorAppUrl = url + "/apps";
		logger.debug("URL build: " + applicationMonitorAppUrl);
		
		try {
			String payload = Parser.getJSONEnergyConsumed(energyCosumed);
			String response = Client.postMethod(applicationMonitorAppUrl, payload, Dictionary.CONTENT_TYPE_JSON, Dictionary.CONTENT_TYPE_JSON, exception);
			logger.debug("PAYLOAD: " + response);

		} catch(Exception e) {
			logger.warn("Error trying to post an application metric in the application monitor: " + applicationMonitorAppUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		return !exception;
	}
}
