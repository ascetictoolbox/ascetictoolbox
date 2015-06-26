package eu.ascetic.paas.applicationmanager.pm;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;

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
 * 
 * Class responsible to connecting to the Price Modeller
 *
 */
public class PriceModellerClient {
	private static Logger logger = Logger.getLogger(PriceModellerClient.class);
	private static PriceModellerClient instance = null;
	public PaaSPricingModeller priceModeller =  new PaaSPricingModeller();
	
	protected PriceModellerClient() {
		// Exists only to defeat instantiation.
	}
	
	public static PriceModellerClient getInstance() {
		if(instance == null) {
			instance = new PriceModellerClient();
		}
		return instance;
	}

	public static double calculatePrice(int applicationId, int deploymentId, double iaasPrice) {
		logger.debug("Connecting to Price Modeller using fixed IaaSPrice: " + iaasPrice);
		//double paasPrice = PriceModellerClient.getInstance().priceModeller.getAppPriceEstimation(deploymentId, applicationId, 0, iaasPrice);
		// TODO Change this to the new Price Modeller interface
		double paasPrice = 120;
		logger.debug("New price from the Price Modeller : " + paasPrice);
		return paasPrice;
	}
}
