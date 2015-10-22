/**
 *  Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.io.IOException;

import org.apache.log4j.Logger;




import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;


/**
 * This is the main interface of the pricing modeller of PaaS layer. 
 * Functionality:
 * The ability to provide a price estimation of an application per hour, given the energy consumed of this app, the deployment id, 
 * the application id, the IaaS provider id and the IaaS provider's price. 
 * 
 * The price estimation can be also given without the provision of energy estimation.
 * 
 * The price estimation can be also given without the provision of an PaaS price. 
 * @author E. Agiatzidou 
 */


public class PaaSPricingModeller implements PaaSPricingModellerInterface{

	PaaSPricingModellerBilling billing = new PaaSPricingModellerBilling();
	static Logger logger = null;
	
	public PaaSPricingModeller(){
		DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
		Date today = Calendar.getInstance().getTime();     
		String reportDate = df.format(today);
		//Properties log4jProperties = new Properties();
		String name = "logs/" + reportDate;
		System.setProperty("logfile.name",name);
		logger = Logger.getLogger(PaaSPricingModeller.class);
		//PropertyConfigurator.configure(log4jProperties);
		//BasicConfigurator.configure();
		logger.info("PaaS Pricing Modeller initiallized");
	}
	

	
/////////////////////////////////////////BASED ON CALCULATIONS FROM IAAS LAYER///////////////////////////////////
	/**
	 * This function returns the predicted charges for an application based on the IaaS charges. 
	 * @param deplID
	 * @param schemeID: the pricing scheme followed
	 * @param IaaSCharges: the charges coming from the IaaS Provider
	 * @return
	 */
	public double getAppPredictedCharges(int deplID, int schemeID, double IaaSCharges){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		double charges = billing.predictCharges(deployment);
		logger.info("Prediction:"+deplID+","+schemeID+","+IaaSCharges+","+charges);
		return charges;
	}
	
	/**
	 * This function returns the price of the application  per hour
	 * @param deplID
	 * @param schemeID: the pricing scheme followed
	 * @param IaaSCharges the charges coming from the IaaS Provider
	 * @param duration: The duration of the application in seconds
	 * @return
	 */
	public double getAppPredictedPrice(int deplID, int schemeID, double IaaSCharges, long duration){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		deployment.getPredictedInformation().setDuration(duration);
		double price = billing.predictPrice(deployment);
		double charges = billing.predictCharges(deployment);
		logger.info("Prediction:"+deplID+","+schemeID+","+IaaSCharges+","+charges+","+price);
		return charges;
	}
	
	/**
	 * This function initialized the application in order for the Pricing Modeller to initiate the billing of the app
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 */
	public void initializeApp(int deplID, int schemeId){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		
		billing.registerApp(deployment);
	}
	
	/**
	 * This function returns the total charges of the application based on the IaaS Charges
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 */
	public double getAppTotalCharges(int deplID, int schemeID, double IaaSCharges){
		double charges = billing.getAppCurrentTotalCharges(deplID, IaaSCharges);
		logger.info("Billing:"+deplID+","+schemeID+","+IaaSCharges+","+charges);
		return charges;
	}
	
//////////////////////Based on the layer's calculations////////////////////////////
	//currently not working
	public double getAppPredictedCharges(int deplID, int schemeID){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		double IaaSCharges = 0;
		deployment.setIaaSProvider(1);
		deployment.setIaaSPredictedCharges(IaaSCharges); 
		double charges = billing.predictCharges(deployment);
		return charges;
	}
	
	
/////////////////////////Event Based/////////////////////////////////////////
	/**
	 * This function returns the total charges for an event 
	 * @param deplID
	 * @param CPU
	 * @param RAM
	 * @param storage
	 * @param energy: the predicted energy from EM
	 * @param schemeId
	 * @param duration: the duration of the event in seconds
	 * @param numberOfevents: the number of the events on the same VM (given from EM)
	 * @return
	 */
	public double getEventPredictedCharges(int deplID, int CPU, int RAM, double storage, double energy, int schemeId, long duration, int numberOfevents){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider(1); 
		VMinfo VM = new VMinfo(RAM, CPU, storage,duration);
		VM.setEnergyPredicted(energy);
		VM.setNumberOfEvents(numberOfevents);
		deployment.addVM(VM);
		deployment.getPredictedInformation().setDuration(duration);
		double charges = billing.predictEventCharges(deployment);
		logger.info("Event:"+deplID+","+CPU+","+RAM+","+storage+","+energy+","+schemeId+","+duration+","+numberOfevents+","+charges);
		return charges;
	}
	
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy,int schemeId){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider(1); 
		deployment.setVMs(VMs);
		deployment.setEnergy(energy);
		double charges = billing.predictAppEventCharges(deployment);
		logger.info("Event:"+deplID+","+energy+","+schemeId+","+charges);
		return charges;
		
	}
	
	////////////////not for year 2//////////////////////////
	/*public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy){
		DeploymentInfo deployment = new DeploymentInfo(deplID);
		deployment.setIaaSProvider(1); 
		deployment.setVMs(VMs);
		deployment.setEnergy(energy);
		double charges = billing.predictAppEventChargesVMbased(deployment);
		//logger.info("Event:"+deplID+","+energy+","+schemeId+","+charges);
		return charges;
		
	}*/
	
}