/**
 * Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.IaaSPricingModellerBilling;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeA;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeC;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue.GenericPricingMessage;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPrediction;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.TimeParameters;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMinfo;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.VMstate;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue.*;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.log4j.Logger;

/**
 * This is the interface of the IaaS Pricing Modeller with the other components of the IaaS Layer.
 *
 * It provides the following operations
 * 1. estimation of the price per hour and the total charges of a VM
 * 2. Total charges of a VM after the end of its operation
 *
 * Three different pricing schemes are provided
 * 1. Static pricing scheme (with schemeID = 0)
 * 2. Two tariffs pricing scheme (with schemeID = 1)
 * 3. Two tariffs pricing scheme employing discounts (with schemeID = 2)
 *
 * To start the billing of a VM, first the initiallizeVM function should be called.
 *
 * @author E. Agiatzidou
 */
public class IaaSPricingModeller implements IaaSPricingModellerInterface {

    /**
	 * The IaaS Pricing Modeller should be connected with the Energy Provider that is deployed
	 * within this component as well as with the Energy Modeller. 
     */
	//Initialize energy provider 
    EnergyProvider energyProvider = new EnergyProvider(this);
    
    //The id of the provider. Default is 1.
    private int idIaaSP=1;
    
    //Energy modeller
    EnergyModeller energyModeller;
    
    //Initialize billing sub-component 
    IaaSPricingModellerBilling billing = new IaaSPricingModellerBilling(energyProvider, idIaaSP);
        
    //The queue created from this provider
    //  static  PricingModellerQueueServiceManager  producer;
    
    //Logging
    static Logger logger = null;
    DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
   Date today = Calendar.getInstance().getTime();
   String reportDate = df.format(today);
   String name = "logs/" + reportDate;

////////////////////////////////////////Constructors////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param energyModeller
     * @throws Exception 
     */
    //New version of the constructor to support multiple IaaS Providers
    public IaaSPricingModeller(int IaaSID, EnergyModeller energyModeller) throws Exception {
        this.energyModeller = energyModeller;
        idIaaSP = IaaSID;
        
        //Activemq
      //->  AmqpMessageProducer pricingqueue = new AmqpMessageProducer("localhost:5673", "guest", "guest", "ENERGY.PRICE",true);
	//->	producer = new PricingModellerQueueServiceManager (pricingqueue);
		//GenericPricingMessage msg = new GenericPricingMessage(idIaaSP, energyProvider.getNewDynamicEnergyPrice().getPriceOnly());
		//publishToQueue(msg);
       System.setProperty("logfile.name", name);
        logger = Logger.getLogger(IaaSPricingModeller.class);
     //   System.out.println("IaaS Pricing Modeller initiallized with ID= " + idIaaSP);
        logger.info("IaaS Pricing Modeller initiallized with ID= " + idIaaSP);
    }
    
    
    public IaaSPricingModeller(EnergyModeller energyModeller) throws Exception {
        this.energyModeller = energyModeller;
    //    System.setProperty("logfile.name", name);
        logger = Logger.getLogger(IaaSPricingModeller.class);
        logger.info("IaaS Pricing Modeller initiallized");
        /*
        AmqpMessageProducer pricingqueue = new AmqpMessageProducer("localhost:5672", "guest", "guest", "test.topic2",true);
		producer = new PricingModellerQueueServiceManager (pricingqueue);
		GenericPricingMessage msg = new GenericPricingMessage(idIaaSP, energyProvider.getNewDynamicEnergyPrice().getPriceOnly());
		publishToQueue(msg);
		*/
    }
    
    public IaaSPricingModeller() throws Exception {
        this.energyModeller = null;
   //     System.setProperty("logfile.name", name);
        logger = Logger.getLogger(IaaSPricingModeller.class);
        logger.info("IaaS Pricing Modeller initiallized");
    }
      
    public int getIaaSProviderID(){
    	return idIaaSP;
    }
    
    public void changeEnergyPrice(){
    	energyProvider.updateEnergyPrice();
    }
    
    public double getEnergyPrice(){
    	return energyProvider.getNewDynamicEnergyPrice().getPriceOnly();
    }
///////////////////////////////QUEUE//////////////////////////////////////////////////////////////////////
   
    public void publishToQueue(GenericPricingMessage msg) throws Exception{
    	try{
    		//->producer.sendToQueue(msg);
    		}
    		catch (NullPointerException ex){
    			logger.info("Could not send the message to queue");
    		}
    }
	
   
///////////////////////////////////////////BILLING///////////////////////////////////////////////////////


    /*
     * 	In order to start billing a VM this function has to be called first. 
     * @param VMid: the ID of the VM, the same used with the Energy Modeller.
     * @param schemeId: the Pricing scheme of the VM
     */
    public void initializeVM(String VMid, int schemeId, String hostname, String appID) {
    	
    	//Default values for these parameters
    	int CPU = 2;
        int RAM = 7680;
        double storage = 32000; 
        
        
        try {
            VmDeployed vm = energyModeller.getVM(VMid);
            CPU = vm.getCpus();
            RAM = vm.getRamMb();
            storage = vm.getDiskGb();
           // System.out.println("The VM with VMid " + VMid + " has been registered, with values CPU: " + CPU + ", RAM: " + RAM + ", storage: " + storage);
            logger.info("The VM with VMid " + VMid + " has been registered, with values CPU: " + CPU + ", RAM: " + RAM + ", storage: " + storage);
           

        } catch (NullPointerException ex) {
            logger.error("The VM with VMid " + VMid + " has not been registered therefore a default VM has been initalized with CPU = 1, memory = 4096, storage = 50GB");
            
        }
        
        VMinfo newVM = new VMinfo(RAM, CPU, storage, hostname);
        newVM.setIaaSID(idIaaSP);
        
               
        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        VMstate VM = new VMstate(VMid, newVM, energyProvider, scheme, appID);

        if (schemeId == 2) {
            EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, hostname);
            VM.getPredictedInformation().setPredictionOfEnergy(energyVM);
            VM.setPredictedCharges(billing.predictVMCharges(VM).getPriceOnly());
        }
        
        scheme.setEnergyModeller(energyModeller);
        billing.registerVM(VM);
        
 //     System.out.println("Modeller: The VM with VMid " + VMid + " has been registered at: " + VM.getStartTime().getTimeInMillis()+" with RAM "+RAM+" CPU "+CPU+" storage "+storage);
        logger.info("IaaS Pricing Modeller: The VM with VMid " + VMid + "has been registered at: " + VM.getStartTime().getTimeInMillis()+" with RAM "+RAM+" CPU "+CPU+" storage "+storage);
    }

    /**
     * When calling this function, the VM stops its operation and the final
     * charges are returned
     *
     * @param VMid
     * @param deleteVM: boolean true when the VM stops operating and deleted
     * else false
     * @return
     * @throws Exception 
     */
    public double getVMFinalCharges(String VMid, boolean deleteVM) throws Exception {
       	try{
            VMstate VM = billing.getVM(VMid);
           /*
            if (VM.getVMinfo().getRAM() == 0 || VM.getVMinfo().getStorage() == 0.0) {

                try {
                    VmDeployed vm = energyModeller.getVM(VMid);
                    int CPU = vm.getCpus();
                    int RAM = vm.getRamMb();
                    double storage = vm.getDiskGb();
                    logger.info("The VM with VMid " + VMid + "has requested new values for CPU: " + CPU + ", RAM: " + RAM + ", STORAGE: " + storage);
                    
                } catch (NullPointerException ex) {
                    logger.error("The VM with VMid " + VMid + "has not taken the values from the energy modeller");
                    
                }
            }
            */
           
          /*  if (VM.getVMinfo().getRAM() == 0 || VM.getVMinfo().getStorage() == 0.0) {
                logger.error("The VM with VMid " + VMid + "has taken zero values from the energy modeller for the CPU, RAM, storage");
            }*/

           double charges = billing.getVMCharges(VMid);


            logger.info("Billing scheme used for getting VM final charges was scheme ." + billing.getVM(VMid).getPricingScheme().getSchemeId());
            logger.info("Duration for VM " + VMid +" is: " + billing.getVM(VMid).getVMDuration());

            if (billing.getVM(VMid).getPricingScheme().getSchemeId() == 0) {
                billing.getVM(VMid).setTotalEnergyConsumed(0);
            }

            if (billing.getVM(VMid).getPricingScheme().getSchemeId() != 2) {
                logger.info("Billing," + VMid + ","
                        + billing.getVM(VMid).getVMinfo().getCPU() + ","
                        + billing.getVM(VMid).getVMinfo().getRAM() + ","
                        + billing.getVM(VMid).getVMinfo().getStorage() + ","
                        + billing.getVM(VMid).getTotalDuration() + ","
                        + billing.getVM(VMid).getPricingScheme().getSchemeId() + ","
                        + billing.getVM(VMid).getTotalEnergyConsumed() + ","
                        + billing.getVM(VMid).getTotalCharges());
            } else {
                logger.info(
                        "Billing," + VMid 
                                + "," + billing.getVM(VMid).getVMinfo().getCPU()
                        + "," + billing.getVM(VMid).getVMinfo().getRAM()
                        + "," + billing.getVM(VMid).getVMinfo().getStorage() 
                        + "," + billing.getVM(VMid).getTotalDuration()
                        + "," + billing.getVM(VMid).getPricingScheme().getSchemeId() 
                        + "," + billing.getVM(VMid).getTotalEnergyConsumed()
                        + "," + billing.getVM(VMid).getTotalCharges() 
                        + "," + billing.getVM(VMid).getPredictedInformation().getPredictedPowerPerHour() 
                        + "," + billing.getVM(VMid).getPredictedInformation().getPredictedCharges().getChargesOnly() 
                        + "," + billing.getVM(VMid).getPredictedInformation().getTotalPredictedEnergy());
            }

            if (deleteVM) {
                logger.info("VM: " + VMid + " was unregistered from the pricing modeller.");
               // billing.unregisterVM(billing.getVM(VMid));
                billing.stopChargingVM(billing.getVM(VMid));
            }
            /*else
             billing.stopChargingVM(billing.getVM(VMid));*/
            
            /*
            GenericPricingMessage msg = new GenericPricingMessage(idIaaSP,charges);
            publishToQueue(msg);
            */
            return charges;
            
        } catch (NullPointerException ex) {
            logger.error("The VM with VMid " + VMid + " is no longer valid");
        }

        return 0.0;

    }

	

    public double getAppFinalCharges(String appID, boolean deleteApp) {
        double charges = billing.getAppCharges(appID, energyModeller);

        if (deleteApp) {
            logger.info("Deleting app id = " + appID);
            billing.unregisterApp(appID);
        }
        return charges;
    }

	public void resizeVM(String VMid,int CPU, int RAM, double storage){
		 billing.resizeVM(VMid,CPU, RAM, storage);
		
	}
	
    //////////////////////////NEW PREDICTION APP///////////////////////////////////////////////////////////////////////////////////
    public double getAppPredictedCharges(String appID, LinkedList<VMinfo> VMs, int schemeId, long duration) {
        LinkedList<VMstate> AppVMs = new LinkedList<>();
        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        scheme.setEnergyModeller(energyModeller);
        ListIterator<VMinfo> listIterator = VMs.listIterator();
        while (listIterator.hasNext()) {
            VMinfo vm = listIterator.next();
            VMstate Vm = new VMstate(vm, energyProvider, scheme);
            Vm.setAppID(appID);
            Vm.getPredictedInformation().setDuration(duration);
            EnergyPrediction energyVM = getEnergyPredicted(vm.getCPU(), vm.getRAM(), vm.getStorage(), duration, vm.gethostname());
            Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);
            billing.predictVMCharges(Vm);
            logger.info("Prediction," + vm.gethostname() + "," + String.valueOf(vm.getCPU()) + "," + String.valueOf(vm.getRAM()) + "," + String.valueOf(vm.getStorage()) + "," + String.valueOf(duration)
                    + "," + String.valueOf(schemeId) + "," + String.valueOf(energyVM.getTotalEnergy()) + "," + String.valueOf(Vm.getPredictedCharges().getChargesOnly()) + "," + appID);
            AppVMs.add(Vm);
        }
        ListIterator<VMstate> listIt = AppVMs.listIterator();
        double totalCharges = 0;
        while (listIt.hasNext()) {
            totalCharges = totalCharges + listIt.next().getPredictedCharges().getChargesOnly();
        }
        return totalCharges;
    }

	
    public double getAppPredictedPricePerHour(String appID, LinkedList<VMinfo> VMs, int schemeId, long duration) {
        LinkedList<VMstate> AppVMs = new LinkedList<>();
        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        scheme.setEnergyModeller(energyModeller);
        ListIterator<VMinfo> listIterator = VMs.listIterator();
        while (listIterator.hasNext()) {
            VMinfo vm = listIterator.next();
            VMstate Vm = new VMstate(vm, energyProvider, scheme);
            Vm.setAppID(appID);
            Vm.getPredictedInformation().setDuration(duration);
            EnergyPrediction energyVM = getEnergyPredicted(vm.getCPU(), vm.getRAM(), vm.getStorage(), duration, vm.gethostname());
            Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);
            billing.predictVMCharges(Vm);
            logger.info("Prediction," + vm.gethostname() + "," + String.valueOf(vm.getCPU()) + "," + String.valueOf(vm.getRAM()) + "," + String.valueOf(vm.getStorage()) + "," + String.valueOf(duration)
                    + "," + String.valueOf(schemeId) + "," + String.valueOf(energyVM.getTotalEnergy()) + "," + String.valueOf(Vm.getPredictedCharges().getChargesOnly()) + "," + appID);
            AppVMs.add(Vm);
        }
        ListIterator<VMstate> listIt = AppVMs.listIterator();
        double totalPrice = 0;
        while (listIt.hasNext()) {
            totalPrice = totalPrice + listIt.next().getPredictedCharges().getPriceOnly();
        }
        return totalPrice;

    }

    ///////////////////////////////////////PREDICTION////////////////////////////////////////////
    /**
     * This function returns the charges estimated for a VM under the specified scheme
     * @param CPU: integer
     * @param RAM: integer
     * @param storage: double
     * @param schemeId: integer
     * @param duration: long in seconds
     * @param hostname: String
     */
    public double getVMChargesPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname) {
    	
        VMinfo vm = new VMinfo(RAM, CPU, storage, hostname);
        vm.setIaaSID(idIaaSP); 		
        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        VMstate Vm = new VMstate(vm, energyProvider, scheme);
        scheme.setEnergyModeller(energyModeller);
        Vm.getPredictedInformation().setDuration(duration);

        // This function calls the Energy Modeller: To be replaced by the AtiveMQ
        EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, duration, hostname);

        Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);

        //Vm.getPredictedInformation().setPredictedPowerPerHour(energyPredicted/dura.getDuration());
	
		
        double predictedTotalCharges = billing.predictVMCharges(Vm).getChargesOnly();

        logger.info("Prediction," + hostname + "," + String.valueOf(CPU) + "," + String.valueOf(RAM) + "," + String.valueOf(storage) + "," + String.valueOf(duration)
                + "," + String.valueOf(schemeId) + "," + String.valueOf(energyVM.getTotalEnergy()) + "," + String.valueOf(predictedTotalCharges));

        return predictedTotalCharges;
    }

    /**
     * This function returns the price estimated for a VM under the specified scheme
     * @param CPU: integer
     * @param RAM: integer
     * @param storage: double
     * @param schemeId: integer
     * @param duration: long in seconds
     * @param hostname: String
     */
    public double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId, long duration, String hostname) {
        VMinfo vm = new VMinfo(RAM, CPU, storage, hostname);
        vm.setIaaSID(idIaaSP);
        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        VMstate Vm = new VMstate(vm, energyProvider, scheme);
        scheme.setEnergyModeller(energyModeller);
        Vm.getPredictedInformation().setDuration(duration);

        // This function calls the Energy Modeller: To be replaced by the AtiveMQ
        EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, duration, hostname);

        Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);

        double predictedPricePerHour = billing.predictVMCharges(Vm).getPriceOnly();

        logger.info("Prediction," + hostname + "," + String.valueOf(CPU) + "," + String.valueOf(RAM) + "," + String.valueOf(storage) + "," + String.valueOf(duration)
                + "," + String.valueOf(schemeId) + "," + String.valueOf(energyVM.getAvrgPower()) + "," + String.valueOf(predictedPricePerHour));
        return predictedPricePerHour;
    }

    private Collection<VM> castCollection(Collection<VmDeployed> collection) {
        Collection<VM> col = new ArrayList<VM>();
        Iterator<VmDeployed> itr = collection.iterator();
        while (itr.hasNext()) {
            VmDeployed element = itr.next();
            col.add((VM) element);
        }
        return col;
    }

  
    
    ///////////////////////Basic functions///////////////////////////////
	
    public EnergyProvider getEnergyProvider() {
        return this.energyProvider;
    }

    public IaaSPricingModellerBilling getBilling() {
        return this.billing;
    }

    public IaaSPricingModeller getIaaSprovider(int id) {
        return this;
    }

    public IaaSPricingModellerPricingScheme initializeScheme(int schemeId) {
        IaaSPricingModellerPricingScheme scheme = null;
        if (schemeId == 0) {
            scheme = new PricingSchemeA(schemeId, idIaaSP);
        }
        if (schemeId == 1) {
            scheme = new PricingSchemeB(schemeId, idIaaSP);
        }
        if (schemeId == 2) {
            scheme = new PricingSchemeC(schemeId, idIaaSP);
        }
        return scheme;
    }

    public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname) {
        EnergyPrediction energyVM = new EnergyPrediction();
        try {
            logger.info("The pricing modeller is obtaining an energy estimation for CPU: "
                    + CPU + ", RAM: " + RAM + ", STORAGE: " + storage + ", duration: " + duration + ", hostname: " + hostname);
            VM newVM = new VM(CPU, RAM, storage);
            TimeParameters dur = new TimeParameters(duration);
            TimePeriod dura = new TimePeriod(dur.getStartTime(), dur.getEndTime());
            Host host = energyModeller.getHost(hostname);
            Collection<VmDeployed> collection = energyModeller.getVMsOnHost(host);
            Collection<VM> col = castCollection(collection);

            EnergyUsagePrediction energyPrediction = energyModeller.getPredictedEnergyForVM(newVM, col, host, dura);
            energyVM.setTotalEnergy(energyPrediction.getTotalEnergyUsed());
            energyVM.setAvergPower(energyPrediction.getAvgPowerUsed());
        } catch (NullPointerException ex) {
            logger.error("Obtaining the predicted energy for " + hostname + "failed.", ex);
            energyVM.setTotalEnergy(100);
            energyVM.setAvergPower(100);
        }
        return energyVM;

		
    }

    public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname) {
        EnergyPrediction energyVM = new EnergyPrediction();
        try {
            VM newVM = new VM(CPU, RAM, storage);
            Host host = energyModeller.getHost(hostname);
            Collection<VmDeployed> collection = energyModeller.getVMsOnHost(host);
            Collection<VM> col = castCollection(collection);

            energyVM.setTotalEnergy(energyModeller.getPredictedEnergyForVM(newVM, col, host).getTotalEnergyUsed());
            energyVM.setAvergPower(energyModeller.getPredictedEnergyForVM(newVM, col, host).getAvgPowerUsed());
        } catch (NullPointerException ex) {
            logger.error("Obtaining the predicted energy for " + hostname + "failed.", ex);
            energyVM.setTotalEnergy(0);
            energyVM.setAvergPower(0);
        }
        return energyVM;
    }

    public void initializeVM(String VMid, int CPU, int RAM, double storage, int schemeId, String hostname, String appID) {

    	
        VMinfo vm = new VMinfo(RAM, CPU, storage, hostname);
        vm.setIaaSID(idIaaSP);

        IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
        VMstate VM = new VMstate(VMid, vm, energyProvider, scheme, appID);

        if (schemeId == 2) {
            EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, hostname);
            VM.getPredictedInformation().setPredictionOfEnergy(energyVM);
            VM.setPredictedCharges(billing.predictVMCharges(VM).getPriceOnly());

        }
        scheme.setEnergyModeller(energyModeller);

		
        billing.registerVM(VM);
    }
    
  
}