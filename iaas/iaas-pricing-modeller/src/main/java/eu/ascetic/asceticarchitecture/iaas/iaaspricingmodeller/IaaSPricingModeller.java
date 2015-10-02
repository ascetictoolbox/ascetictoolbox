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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;




import org.apache.log4j.*;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeA;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeC;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing.*;

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

public class IaaSPricingModeller implements IaaSPricingModellerInterface{

	/** 
	 * The IaaS Pricing Modeler should be connected with the Energy Provider that is deployed
	 * within this component as well as with the Energy Modeler. 
	 */
	EnergyProvider energyProvider = new EnergyProvider(this);
	EnergyModeller energyModeller;
	IaaSPricingModellerBilling billing = new IaaSPricingModellerBilling(energyProvider);
	//private static int idIaaSP=0;
	
	static Logger logger = null;


	/** Constructor
	 * @param energyModeller
	 */
	public IaaSPricingModeller(EnergyModeller energyModeller) {
		this.energyModeller = energyModeller;
		//idIaaSP=idIaaSP+1;
		DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
		Date today = Calendar.getInstance().getTime();     
		String reportDate = df.format(today);
		String name = "logs/" + reportDate;
		System.setProperty("logfile.name",name);
		logger = Logger.getLogger(IaaSPricingModeller.class);
		logger.info("IaaS Pricing Modeller initiallized");
		
    }
	
	
	///////////////////////////////////////PREDICTION////////////////////////////////////////////
	 /**
     * This function returns the charges estimated for a VM under the specified scheme
     * @param CPU: integer
     * @param RAM: integer
     * @param storage: double
     * @param schemeID: integer
     * @param duration: long in seconds
     * @param hostname: String
     */
	
	public double getVMChargesPrediction(int CPU, int RAM, double storage, int schemeId,  long duration, String hostname){
		VMinfo vm = new VMinfo (RAM, CPU, storage, hostname);
		IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
		VMstate Vm = new VMstate(vm, energyProvider, scheme);
		scheme.setEnergyModeller(energyModeller);
		Vm.getPredictedInformation().setDuration(duration);
		
		// This function calls the Energy Modeller: To be replaced by the AtiveMQ
		EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, duration, hostname);
		//EnergyPrediction energyVM = new EnergyPrediction();
		//energyVM.setAvergPower(5);
		//energyVM.setTotalEnergy(10);
		Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);
		
		//Vm.getPredictedInformation().setPredictedPowerPerHour(energyPredicted/dura.getDuration());
	
		
		double predictedTotalCharges = billing.predictVMCharges(Vm).getChargesOnly();
		
		logger.info("Prediction,"+hostname+","+String.valueOf(CPU)+","+String.valueOf(RAM)+","+String.valueOf(storage)+","+String.valueOf(duration)
				+","+String.valueOf(schemeId)+","+String.valueOf(energyVM.getTotalEnergy())+","+String.valueOf(predictedTotalCharges));
		
		return predictedTotalCharges;
	}
    
	/**
     * This function returns the price estimated for a VM under the specified scheme
     * @param CPU: integer
     * @param RAM: integer
     * @param storage: double
     * @param schemeID: integer
     * @param duration: long in seconds
     * @param hostname: String
     */
	public double getVMPricePerHourPrediction(int CPU, int RAM, double storage, int schemeId,  long duration, String hostname){
		VMinfo vm = new VMinfo (RAM, CPU, storage, hostname);
		IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
		VMstate Vm = new VMstate(vm, energyProvider, scheme);
		scheme.setEnergyModeller(energyModeller);
		Vm.getPredictedInformation().setDuration(duration);
		
		// This function calls the Energy Modeller: To be replaced by the AtiveMQ
		EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, duration, hostname);
		//EnergyPrediction energyVM = new EnergyPrediction();
		//energyVM.setAvergPower(5);
		//energyVM.setTotalEnergy(10);
		Vm.getPredictedInformation().setPredictionOfEnergy(energyVM);
		
		double predictedPricePerHour = billing.predictVMCharges(Vm).getPriceOnly();
		
		logger.info("Prediction,"+hostname+","+String.valueOf(CPU)+","+String.valueOf(RAM)+","+String.valueOf(storage)+","+String.valueOf(duration)
				+","+String.valueOf(schemeId)+","+String.valueOf(energyVM.getAvrgPower())+","+String.valueOf(predictedPricePerHour));
		
		return predictedPricePerHour;
	}
	
	private Collection <VM> castCollection(Collection<VmDeployed> collection){
		Collection<VM> col = new ArrayList<VM>();
		Iterator<VmDeployed> itr = collection.iterator();
	      while(itr.hasNext()) {
	         VmDeployed element = itr.next();
	         col.add((VM)element);
	      }
	      return col;
	}
	
	///////////////////////////////////////////BILLING/////////////////////////////////////////////
	/**
	 * 	In order to start billing a VM this function has to be called first. 
	 * @param VMid: the ID of the VM, the same used with the Energy Modeller.
	 * @param schemeId: the Pricing scheme of the VM
	 */
	public void initializeVM(String VMid, int schemeId, String hostname){
		
		int CPU = energyModeller.getVM(VMid).getCpus();
		int RAM = energyModeller.getVM(VMid).getRamMb();
		double storage = energyModeller.getVM(VMid).getDiskGb();
		VMinfo vm = new VMinfo (RAM, CPU, storage, hostname);
		
		IaaSPricingModellerPricingScheme scheme = initializeScheme(schemeId);
		VMstate VM = new VMstate(VMid, vm, energyProvider, scheme);
		
		if (schemeId== 2){
			EnergyPrediction energyVM = getEnergyPredicted(CPU, RAM, storage, hostname);
			VM.getPredictedInformation().setPredictionOfEnergy(energyVM);
			VM.setPredictedPrice(billing.predictVMCharges(VM).getPriceOnly());
		}
		scheme.setEnergyModeller(energyModeller);
		
		billing.registerVM(VM);
	}
	
	/**
	 * When calling this function, the VM stops its operation and the final charges are returned	
	 * @param VMid
	 * @param deleteVM: boolean true when the VM stops operating and deleted false when VM pauses operation
	 * @return
	 */
	public double getVMFinalCharges(String VMid, boolean deleteVM){
		return billing.getVMCharges(VMid, deleteVM);
	}
   
	
	///////////////////////Basic functions///////////////////////////////
	/**
	public int getIaaSId(){
		return idIaaSP;
	}
	**/
	
    public EnergyProvider getEnergyProvider(){
    	return this.energyProvider;
    }
    
    public IaaSPricingModellerBilling getBilling(){
    	return this.billing;
    }
    
    public IaaSPricingModeller getIaaSprovider(int id){
    	return this;
    }
    
    public IaaSPricingModellerPricingScheme initializeScheme(int schemeId){
    	IaaSPricingModellerPricingScheme scheme = null;
		if (schemeId==0){
			scheme = new PricingSchemeA(schemeId);
		}
		if (schemeId==1){
			scheme = new PricingSchemeB(schemeId);
		}
		if (schemeId==2){
			scheme = new PricingSchemeC(schemeId);
		}
		return scheme;
    }
    
    public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, long duration, String hostname){
    	VM newVM = new VM(CPU, RAM, storage);
		TimeParameters dur = new TimeParameters(duration);
		TimePeriod dura = new TimePeriod(dur.getStartTime(), dur.getEndTime());
		Host host = energyModeller.getHost(hostname);
		Collection <VmDeployed> collection =  energyModeller.getVMsOnHost(host);
		Collection <VM> col = castCollection(collection);
		EnergyPrediction energyVM = new EnergyPrediction();
		energyVM.setTotalEnergy(energyModeller.getPredictedEnergyForVM(newVM, col, host, dura).getTotalEnergyUsed());
		energyVM.setAvergPower(energyModeller.getPredictedEnergyForVM(newVM, col, host, dura).getAvgPowerUsed());
		return energyVM;

		
    }
    
    public EnergyPrediction getEnergyPredicted(int CPU, int RAM, double storage, String hostname){
    	VM newVM = new VM(CPU, RAM, storage);
		Host host = energyModeller.getHost(hostname);
		Collection <VmDeployed> collection =  energyModeller.getVMsOnHost(host);
		Collection <VM> col = castCollection(collection);
		EnergyPrediction energyVM = new EnergyPrediction();
		energyVM.setTotalEnergy(energyModeller.getPredictedEnergyForVM(newVM, col, host).getTotalEnergyUsed());
		energyVM.setAvergPower(energyModeller.getPredictedEnergyForVM(newVM, col, host).getAvgPowerUsed());
		return energyVM;
    }
    
    
    
    ///////////////////not used /////////////////////////////
   /*****
    public double getVMCurrentCharges(String VMid){
		return billing.getVMCharges(VMid);
		
	}
	**/
}