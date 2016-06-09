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
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import org.apache.log4j.Logger;




import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;


import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.GenericPricingMessage;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PaaSPricingMessageHandler;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.QueueInitializator;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.IaaSProvider;
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


public class PaaSPricingModeller_withqueue implements PaaSPricingModellerInterface{

	
	PaaSPricingModellerBilling billing = new PaaSPricingModellerBilling();
	static Logger logger = null;
	static  PricingModellerQueueServiceManager  producer;
	static HashMap<Integer,IaaSProvider> IaaSProviders = new HashMap<Integer,IaaSProvider>();
	
	//Constructor
	public PaaSPricingModeller_withqueue() throws Exception{

		DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
		Date today = Calendar.getInstance().getTime();     
		String reportDate = df.format(today);
		//Properties log4jProperties = new Properties();
		String name = "logs/" + reportDate;
		System.setProperty("logfile.name",name);
		logger = Logger.getLogger(PaaSPricingModeller_withqueue.class);
		//PropertyConfigurator.configure(log4jProperties);
		//BasicConfigurator.configure();
		logger.info("PaaS Pricing Modeller initiallized");
		
		QueueInitializator queues = new QueueInitializator();
	
		producer = new PricingModellerQueueServiceManager (queues.InitializeProducerQueue("localhost:5672", "guest", "guest", "test.topic3",true));
		
		/*Initialization of the receiver to listen to IaaS pricing Modeller's queue*/
	//	queues.InitializeRecieverQueue("localhost:5672", "guest", "guest", "test.topic2",true, this);
		createProvider();
	}
	
private void createProvider() {
		IaaSProvider Prov = new IaaSProvider(0);
		IaaSProviders.put(0, Prov);
	}

	//////////////////////////////////INITIALIZATION OF APP////////////////////////////////////
	/**
	 * This function initialized the application in order for the Pricing Modeller to initiate the billing of the app
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 */
	public void initializeApp(int deplID, int schemeId){
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
	//	billing.registerApp(deployment);
	}
	
	/*Default IaaSProviderID = 0*/
	public void initializeApp(int deplID, LinkedList<VMinfo> VMs, int IaaSProviderID){
		DeploymentInfo deployment = new DeploymentInfo(deplID);
		
		if (IaaSProviderID==0){
			deployment.setIaaSProvider(IaaSProviders.get(0));
		}
		else {
			IaaSProvider Prov = new IaaSProvider(IaaSProviderID);
			IaaSProviders.put(IaaSProviderID, Prov);
			deployment.setIaaSProvider(IaaSProviders.get(IaaSProviderID));
		}
			deployment.setVMs(VMs);
		//registerApp(deployment);
	}
	
/////////////////////////////////////////BASED ON CALCULATIONS FROM IAAS LAYER///////////////////////////////////
	
	/*********************************** PREDICTION**************************************************/
	
	/**
	 * This function returns the predicted charges for an application based on the IaaS charges. 
	 * @param deplID
	 * @param schemeID: the pricing scheme followed
	 * @param IaaSCharges: the charges coming from the IaaS Provider
	 * @return
	 * @throws Exception 
	 */
	public double getAppPredictedCharges(int deplID, int schemeID, double IaaSCharges) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		double charges = billing.predictCharges(deployment);
		GenericPricingMessage msg = new GenericPricingMessage(deplID, schemeID, charges);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
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
	 * @throws Exception 
	 */
	public double getAppPredictedPrice(int deplID, int schemeID, double IaaSCharges, long duration) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeID);
		deployment.setIaaSPredictedCharges(IaaSCharges);
		deployment.getPredictedInformation().setDuration(duration);
		double price = billing.predictPrice(deployment);
		double charges = billing.predictCharges(deployment);
		GenericPricingMessage msg = new GenericPricingMessage(deplID, schemeID, price);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
		logger.info("Prediction:"+deplID+","+schemeID+","+IaaSCharges+","+charges+","+price);
		return price;
	}
	
	
	
	/* CALCULATION*/	
	/**
	 * This function returns the total charges of the application based on the IaaS Charges
	 * @param deplID
	 * @param schemeId: the pricing scheme followed
	 * @throws Exception 
	 */
	public double getAppTotalCharges(int deplID, int schemeID, double IaaSCharges) throws Exception{
		double charges = billing.getAppCurrentTotalCharges(deplID, IaaSCharges);
		GenericPricingMessage msg = new GenericPricingMessage(deplID, schemeID, charges);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
		logger.info("Billing:"+deplID+","+schemeID+","+IaaSCharges+","+charges);
		return charges;
	}
	
//////////////////////BASED ON LAYER'S CALCULATION////////////////////////////

	public double getAppTotalChargesPaaSCalculated(int deplID){
		double charges = billing.getAppCurrentTotalCharges(deplID);
		return charges;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/////////////////////////USED FOR EVENTS/////////////////////////////////////////
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
	public double getEventPredictedCharges(int deplID, int CPU, int RAM, double storage, double energy, int schemeId, long duration, int numberOfevents) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider(1); 
		VMinfo VM = new VMinfo(RAM, CPU, storage,duration);
		VM.setEnergyPredicted(energy);
		VM.setNumberOfEvents(numberOfevents);
		deployment.addVM(VM);
		deployment.getPredictedInformation().setDuration(duration);
		double charges = billing.predictEventCharges(deployment);
		////To be checked with Energy Modeller
		GenericPricingMessage msg = new GenericPricingMessage(deplID, schemeId, charges);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
		logger.info("Event:"+deplID+","+CPU+","+RAM+","+storage+","+energy+","+schemeId+","+duration+","+numberOfevents+","+charges);
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
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy,int schemeId) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID, schemeId);
		deployment.setIaaSProvider(1); 
		deployment.setVMs(VMs);
		deployment.setEnergy(energy);
		double charges = billing.predictAppEventCharges(deployment);
		GenericPricingMessage msg = new GenericPricingMessage(deplID, charges);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
		logger.info("Event:"+deplID+","+energy+","+schemeId+","+charges);
		return charges;
		
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
	public double getEventPredictedChargesOfApp(int deplID, LinkedList<VMinfo> VMs, double energy) throws Exception{
		DeploymentInfo deployment = new DeploymentInfo(deplID);
		deployment.setIaaSProvider(1); 
		deployment.setVMs(VMs);
		deployment.setEnergy(energy);
		double charges = billing.predictAppEventChargesVMbased(deployment);
		//logger.info("Event:"+deplID+","+energy+","+schemeId+","+charges);
		GenericPricingMessage msg = new GenericPricingMessage(deplID, charges);
		try{
		producer.sendToQueue("test", msg);
		}
		catch (NullPointerException ex){
			logger.info("Could not send the message to queue");
		}
		return charges;
		
	}
	
	public IaaSProvider getProvider(int i){
		return IaaSProviders.get(i);
		
	}

	public PaaSPricingModellerBilling getBilling() {
		return billing;
	}
	
}