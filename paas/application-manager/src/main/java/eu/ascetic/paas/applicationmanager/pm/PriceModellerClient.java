package eu.ascetic.paas.applicationmanager.pm;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;

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
	protected PaaSPricingModeller priceModeller;
	
	private PriceModellerClient() {
		logger.info("PriceModeller has been created for the first time...");
		
		try {
			priceModeller = new PaaSPricingModeller();
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
		}
	}
	
	public static PriceModellerClient getInstance() {
		if(instance == null) {
			instance = new PriceModellerClient();
		}
		return instance;
	}
	
	public void initializeApplication(int deploymentId, int schemeId) {
		priceModeller.initializeApp(deploymentId, schemeId);
	}
	
	public double getAppPredictedCharges(int deploymentId, int schemeID, double iaaSCharges) {
		try {
			return priceModeller.getAppPredictedCharges(deploymentId, schemeID, iaaSCharges);
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
			return -1.0;
		}
	}
	
	public double getAppPredictedPrice(int deploymentId, int schemeID, double iaaSCharges, long duration) {
		try {
			return priceModeller.getAppPredictedPrice(deploymentId, schemeID, iaaSCharges, duration);
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
			return -1.0;
		}
	}
	
	public double getAppTotalCharges(int deploymentId, int schemeID, double iaaSCharges) {
		try {
			return priceModeller.getAppTotalCharges(deploymentId, schemeID, iaaSCharges);
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
			return -1.0;
		}
	}
	
	public double getEventPredictedCharges(int deploymentId, 
			                               int cpu, 
			                               int ram, 
			                               double storage, 
			                               double energy, 
			                               int schemeId, 
			                               long duration, 
			                               int numberOfevents) {
		try {
			return priceModeller.getEventPredictedCharges(deploymentId, cpu, ram, storage, energy, schemeId, duration, numberOfevents);
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
			return -1.0;
		}
	}
	
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy,int schemeId) {
		try {
			return priceModeller.getEventPredictedChargesOfApp(deplID, VMs, energy, schemeId);
		} catch(Exception ex) {
			logger.warn("Error getting the reploy from the PaaS PM");
			logger.warn(ex.getMessage());
			logger.warn(ex.getStackTrace());
			return -1.0;
		}
	}
}
