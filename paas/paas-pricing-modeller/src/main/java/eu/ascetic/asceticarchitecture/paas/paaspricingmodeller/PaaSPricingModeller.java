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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import org.apache.log4j.Logger;



import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModellerInterface;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.AmqpClientPM;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.QueueInitializator;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.VMBasic;
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
	static PricingModellerQueueServiceManager  producer;
	/////static HashMap<Integer,IaaSProvider> IaaSProviders = new HashMap<Integer,IaaSProvider>();
	static HashMap<Integer,Double> VMsEnergy = new HashMap<Integer, Double>();
	
	 //Logging
    static Logger logger = null;
  
   /* DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
    Date today = Calendar.getInstance().getTime();
    String reportDate = df.format(today);
    String name = "logs/" + reportDate;
    */
    
	public PaaSPricingModellerBilling getBilling() {
		return billing;
	}
	
	//Constructor
	public PaaSPricingModeller() throws Exception{

		DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
		Date today = Calendar.getInstance().getTime();     
		String reportDate = df.format(today);
		String name = "logs/" + reportDate;
		System.setProperty("logfile.name",name);
		
		//PropertyConfigurator.configure(log4jProperties);
		//BasicConfigurator.configure();
		 
		
		logger = Logger.getLogger(PaaSPricingModeller.class);
	    logger.info("PaaS Pricing Modeller initiallized");
		
		try{
			AmqpClientPM PMqueue = new AmqpClientPM();
			PMqueue.setup(null,  "guest", "guest",  "PRICING");
			producer = new PricingModellerQueueServiceManager(PMqueue, this);
			producer.createConsumers("APPLICATION.*.DEPLOYMENT.*.VM.*.*","vm.*.item.* ");
		//	producer.createConsumers("PRICING","PMPREDICTION.DEPLOYMENTID.*.*");
		//	producer.createConsumers("PRICING","PMBILLING.DEPLOYMENTID.*.*");
		//	producer.createConsumers("PRICING","PMPREDICTION.DEPLOYMENTID.*.VMID.*.*");
		//	producer.createConsumers("PRICING","PMBILLING.DEPLOYMENTID.*.VMID.*.*");
			billing.setQueue(producer);
		}catch(Exception ex){
			//System.out.println("PM: The queue was not initiated");
			logger.info("PM: The queue was not initiated");
		}
		
	}

	
	//////////////////////////////////YEAR 3 //////////////////////////////////////////////////////////
		/*Default IaaSProviderID = 0*/
	//TESTED
	public void initializeApp(String appID, int deplID, LinkedList<VMinfo> VMs){
			billing.registerApp(deplID, VMs, billing);
	}
	
	//TESTED
	public void addVM(int deplID, VMinfo VM){
	//	System.out.println("Modeller: VMid to add " + VM.getVMid());
		logger.info("Modeller: VMid to add " + VM.getVMid());
		billing.addVM(deplID, VM);
	}
	
	//TESTED
	public void removeVM(int deplID, int VMid){
		billing.removeVM(deplID, VMid);
	}
	
	//TESTED
	public void resizeVM(int deplID, int VMid,double CPU, double RAM, double storage){
		//System.out.println("Modeller: VMid to resize " + VMid);
		logger.info("Modeller: VMid to resize " + VMid);
		 billing.resizeVM(deplID, VMid,CPU, RAM, storage);
	}

	/**
	 * This function returns the total charges of the application based on the IaaS Charges
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 * @throws Exception 
	 */
	//TESTED
	public double getAppTotalCharges(int deplID, int schemeID, double IaaSCharges) throws Exception{
		logger.info("ChargesCalculator calculation of charges");
		double charges;
		double chargesOnIaaS = billing.getAppCurrentTotalCharges(deplID, IaaSCharges);
		double chargesOnPaaS = billing.getAppCurrentTotalCharges(deplID, null, schemeID);
		//	System.out.println("Charges on IaaS: " + chargesOnIaaS);
		//	System.out.println("Charges on PaaS: " + chargesOnPaaS);
		if (billing.getEnergyFlag()){
			charges=chargesOnIaaS;
		}
		
		if (chargesOnPaaS>chargesOnIaaS){
			charges = chargesOnPaaS;
		}
		else if(chargesOnIaaS>0){
			charges = chargesOnIaaS;
		}
		else{
			charges = 100;
		}
		return charges;
		//System.out.println("ChargesCalculator calculation of charges");		
	}
	
	//TESTED
		public double getAppTotalCharges(int deplID, int schemeID, double IaaSCharges, HashMap<Integer,Double> energyOfVMs) throws Exception{
			logger.info("ChargesCalculator calculation of charges");
			double chargesOnIaaS = billing.getAppCurrentTotalCharges(deplID, IaaSCharges);
			double chargesOnPaaS = billing.getAppCurrentTotalCharges(deplID, energyOfVMs, schemeID);
			//System.out.println("Charges on IaaS: " + chargesOnIaaS);
			//System.out.println("Charges on PaaS: " + chargesOnPaaS);
			
			return chargesOnPaaS;
			//System.out.println("ChargesCalculator calculation of charges");		
		}
		
		
		//TESTED
	public double predictPriceforNextPeriod(int depID, double duration){
		DeploymentInfo depl = billing.getApp(depID);
		depl.resetCurrentCharges();
		int allVMs = depl.getNumberOfVMs();
		for (int x =0; x<allVMs; x++){
			depl.getVM(x).resetCurrentCharges();
		}
		return billing.predictPriceofNextHour(depl, duration);
	}
	
	//TESTED
	public double predictAppPriceforNextHour(int depID, LinkedList<VMinfo> VMs){
		DeploymentInfo depl = billing.getApp(depID);
		double price = 0.0;
		price = billing.predictPriceofNextHour(depl, 3600);
		VMinfo VM;
		for (int i=0;i<VMs.size();i++){
			VM = VMs.get(i);
			price = price +billing.getVMPredictedPrice(VM, 3600);
		}
		return price;		
	}
	
	
	// IT TAKES INTO ACCOUNT THE SCHEME OF EACH VM IF THIS HAS BEEN SET ALREADY
	//TESTED
	public double getAppPredictedCharges(int scheme, LinkedList<VMinfo> VMs){
			DeploymentInfo deployment = new DeploymentInfo();
			deployment.setSchemeID(scheme);
			deployment.setVMs(VMs);
			double charges = billing.predictCharges(deployment, null);
			List<Integer> vms = new ArrayList<>();
			for (int i=0;i<VMs.size();i++){
				vms.add(VMs.get(i).getVMid());
			}
			/*
			try{
				producer.sendToQueue("PMPREDICTION",VMs.getLast().getIaaSProvider().getID(), deplID, vms, Unit.CHARGES, charges);	
			}
			catch(Exception ex){
				//	System.out.println("PM: Could not send message to queue");
					logger.error("PM: Could not send message to queue");
			}*/
			return charges;
	}

	//TESTED
	public double getAppPredictedPrice(int scheme,  double duration, LinkedList<VMinfo> VMs){
		double charges = getAppPredictedCharges(scheme, VMs);
		double totalDurationOfApp = 0;
		double price = 0.0;
		//List<Integer> vms = new ArrayList<>();
		//		for (int i=0;i<VMs.size();i++){
		//			totalDurationOfApp = totalDurationOfApp + VMs.get(i).getPredictedDuration();
		//			vms.add(VMs.get(i).getVMid());
		//		}
		//System.out.println("the charges are " +charges+" the duration is "+duration);
		if (duration > 3600){
				 price = charges / (duration/3600);}
		else
			 price = charges;
	//	System.out.println(price);
				/*
				try{
					producer.sendToQueue("PMPREDICTION", VMs.getLast().getIaaSProvider().getID(), deplID, vms, Unit.PRICEHOUR, price);	
				}
				catch(Exception ex){
					//System.out.println("PM: Could not send message to queue");
					logger.error("PM: Could not send message to queue");
			}*/
		return price;

	}
	
	public LinkedList<Integer> getCostlyVMs(int deplID){
		return billing.getCostlyVMs(deplID);
	}
	
	public LinkedList<Integer> getCostlyVMs(int deplID, double currentAppCharges, double totalChargesLimit, double remainingAppDuration, HashMap<Integer, Double> averageWattPerHourPerVM){
		return billing.getCostlyVMs(deplID, currentAppCharges, totalChargesLimit, remainingAppDuration,  averageWattPerHourPerVM);
		
	}

	/**
	 * This function returns the total charges for one event running on multiple VMs with different scheme ids
	 * @param deplID
	 * @param LinkedList<>: the list of VMs
	 * @param energy: the predicted energy from EM
	 * @param schemeId
	 * @param duration: the duration of the event in seconds
	 * @param numberOfevents: the number of the events on the same VM (given from EM)
	 * @return
	 */
	//TESTED
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy) throws Exception{
		//int VMschemeId= VMs.getFirst().getSchemeID();
		//try{
			DeploymentInfo deployment = new DeploymentInfo(deplID);
			deployment.setVMs(VMs);
			deployment.setEnergy(energy);
			deployment.setIaaSProvider("0"); 
			double charges = billing.predictAppEventChargesVMbased(deployment);
			return charges;
		//}
	//	catch(NullPointerException e){
		//	logger.error("Predict event running on multiple VM which have different schemes found VM without scheme");
	//		return -2;
		//}
		
		//logger.info("Event:"+deplID+","+energy+","+schemeId+","+charges);
		
		
	}
	
	
	
	////////////////////////////////////////YEAR 2 //////////////////////////////////////////////////////////
	/**
	 * This function initialized the application in order for the Pricing Modeller to initiate the billing of the app
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 */
	/******************In comments for not to be confused with Y3***************************/
	public void initializeApp(String appID, int deplID, int schemeId){
		billing.registerApp(appID, deplID, schemeId);
	}
	
	
	
	/**
	 * This function returns the total charges for all events running on one VM
	 * @param deplID
	 * @param CPU
	 * @param RAM
	 * @param storage
	 * @param energy: the predicted energy from EM
	 * @param schemeId
	 * @param duration: the duration of the event in seconds
	 * @param numberOfevents: the number of the events on the same VM (given from EM)
	 * @return
	 * @throws Exception 
	 */
	///TO CHECK!!
	public double getEventPredictedCharges(int deplID, int CPU, int RAM, double storage, double energy, int schemeId, long duration, int numberOfevents) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider("0"); 
		VMinfo VM = new VMinfo(RAM, CPU, storage,duration);
		VM.setEnergyPredicted(energy);
		VM.setNumberOfEvents(numberOfevents);
		deployment.addVM(VM);
		deployment.getPredictedInformation().setDuration(duration);
		double charges = billing.predictEventCharges(deployment);
		return charges;
	}

	/**
	 * This function returns the total charges for one event running on multiple VMs
	 * @param deplID
	 * @param LinkedList<>: the list of VMs
	 * @param energy: the predicted energy from EM
	 * @param schemeId
	 * @param duration: the duration of the event in seconds
	 * @param numberOfevents: the number of the events on the same VM (given from EM)
	 * @return
	 * @throws Exception 
	 */
	
	//TESTED
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy,int schemeId) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider("0"); 
		deployment.setVMs(VMs);
		deployment.setEnergy(energy);
		double charges = billing.predictAppEventCharges(deployment);
		return charges;	
	}
	
/*********************************** PREDICTION**************************************************/
	
	/**
	 * This function returns the predicted charges for an application based on the IaaS charges. 
	 * @param deplID
	 * @param schemeID: the pricing scheme followed
	 * @param IaaSCharges: the charges coming from the IaaS Provider
	 * @return
	 * @throws Exception 
	 */
	
	///TO CHECK!!!
	public double getAppPredictedCharges(int deplID, int schemeID, double IaaSCharges) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		double charges = billing.predictCharges(deployment);
		return charges;
	}
	
	/**
	 * This function returns the price of the application  per hour
	 * @param deplID
	 * @param schemeID: the pricing scheme followed
	 * @param IaaSCharges the charges coming from the IaaS Provider
	 * @param duration: The duration of the application in seconds
	 * @return
	 * @throws Exception 
	 */
	///TO CHECK!!
	public double getAppPredictedPrice(int deplID, int schemeID, double IaaSCharges, long duration) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		deployment.getPredictedInformation().setDuration(duration);
		double price = billing.predictPrice(deployment);
		double charges = billing.predictCharges(deployment);
		return price;
	}
	
	
	/*	public double getAppAveragePredictedPrice(int deplID, LinkedList<VMinfo> VMs, int IaaSProviderID, HashMap<Integer, Double> energy){
	double charges = getAppTotalPredictedCharges(deplID, VMs, IaaSProviderID, energy);
	double totalDurationOfApp = 0;
	List<Integer> vms = new ArrayList<>();
			for (int i=0;i<VMs.size();i++){
				totalDurationOfApp = totalDurationOfApp + VMs.get(i).getActualDuration();
				vms.add(VMs.get(i).getVMid());
			}
			double price = charges / totalDurationOfApp;
			producer.sendToQueue("PMPREDICTION", IaaSProviderID, deplID, vms, Unit.PRICEHOUR, price);	
	return price;

}*/
	//To add boolean to stop the app
	/*public double getAppTotalChargesPaaSCalculated(int deplID, HashMap<Integer, Double> energyPerVM) {
		try {
			billing.updateVMEnergy(energyPerVM);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double charges = billing.getAppCurrentTotalCharges(deplID);
		
		return charges;
	}*/

	//-----------------------------------------------------------------------------------------------///
/*	public double getAppTotalPredictedCharges(int deplID, LinkedList<VMinfo> VMs, int IaaSProviderID, HashMap<Integer, Double> energy){
		DeploymentInfo deployment = new DeploymentInfo(deplID);
			deployment.setVMs(VMs);
			return billing.predictCharges(deployment, energy);
	}*/
}