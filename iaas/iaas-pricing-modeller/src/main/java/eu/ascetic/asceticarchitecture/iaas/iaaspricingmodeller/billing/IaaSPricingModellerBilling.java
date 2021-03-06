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

package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing;

import org.apache.log4j.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeA;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeB;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.PricingSchemeC;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;

/**
 * This is the main function that enables the prediction and the calculation of the charges and the prices of the VMs. 
 *
 * @author E. Agiatzidou
 */

public class IaaSPricingModellerBilling implements IaaSPricingModellerBillingInterface {

    HashMap<String, AppInfo> apps = new HashMap<String, AppInfo>();

    static HashMap<String, VMstate> registeredStaticEnergyPricesVMs = new HashMap<String, VMstate>();
    static HashMap<String, VMstate> registeredDynamicEnergyPricesVMs = new HashMap<String, VMstate>();

    Price averageDynamicEnergyPrice = new StaticEnergyPrice();

    EnergyProvider energyProvider;

    int IaaSID;
	
    public IaaSPricingModellerBilling(EnergyProvider provider, int IaaSID) {
        this.energyProvider = provider;
        this.IaaSID = IaaSID;

		
    }

    // ////////////////////////////// FOR PREDICTION // /////////////////////////////////
    public PredictedCharges predictVMCharges(VMstate VM) {
        IaaSPricingModellerPricingScheme scheme = VM.getPricingScheme();
        double charges = scheme.predictCharges(VM, averageDynamicEnergyPrice);

        VM.setPredictedCharges(charges);
        VM.setPredictedPrice(charges / Math.ceil(VM.getPredictedInformation().getPredictedDuration() / 3600));
        return VM.getPredictedCharges();
    }

    // ///////////////////////////// VM REGISTRATION////////////////////////////////
    @Override
    public void registerVM(VMstate vm) {
        if ((vm.getPricingScheme().getSchemeId() == 0) || (vm.getPricingScheme().getSchemeId() == 2)) {
            registeredStaticEnergyPricesVMs.put(vm.getVMid(), vm);
            
            if (apps.containsKey(vm.getAppID())) {
            //	System.out.println("Billing: The VM belongs to app= " + vm.getAppID());
                apps.get(vm.getAppID()).addVM(vm);
            } else {
            //	System.out.println("Billing: New app = " + vm.getAppID());
                AppInfo app = new AppInfo(vm.getAppID());
                app.addVM(vm);
                apps.put(vm.getAppID(), app);
             //   System.out.println("Billing: New app has been added. Now we have " + apps.size()+" number of apps");
            }

        } 
        else {
            registeredDynamicEnergyPricesVMs.put(vm.getVMid(), vm);

            if (apps.containsKey(vm.getAppID())) {
            //	System.out.println("Billing: The VM belongs to app= " + vm.getAppID());
                apps.get(vm.getAppID()).addVM(vm);

            } else {
                AppInfo app = new AppInfo(vm.getAppID());
           //     System.out.println("Billing: New app = " + vm.getAppID());
                app.addVM(vm);
                apps.put(vm.getAppID(), app);
           //     System.out.println("Billing: New app has been added. Now we have " + apps.size()+" number of apps");
            }
        }
    }


    @Override
    public void unregisterVM(VMstate vm) {
        if ((vm.getPricingScheme().getSchemeId() == 0)) {
        	 if (apps.containsKey(vm.getAppID())) {
        		 apps.get(vm.getAppID()).getList().remove(vm);
        		 if (apps.get(vm.getAppID()).getList().isEmpty())
        			 apps.remove(vm.getAppID());
        	 }
        	
            registeredStaticEnergyPricesVMs.remove(vm.getVMid());
           // vm.setEndTime();
        } else {
        	 if (apps.containsKey(vm.getAppID())) {
        		 apps.get(vm.getAppID()).getList().remove(vm);
        		 if (apps.get(vm.getAppID()).getList().isEmpty())
        			 apps.remove(vm.getAppID());
        	 }
            registeredDynamicEnergyPricesVMs.remove(vm.getVMid());
           // vm.setEndTime();
        }

    }

    @Override
    public void stopChargingVM(VMstate vm) {
    	vm.stopped();
        vm.setEndTime(vm.getChangeTime().getTimeInMillis());

    }

	
		// ///////////////////////////////BILLING////////////////////////////////////

	
    @Override
    public double getVMCharges(String VMid) {
    	if (getVM(VMid).isActive()){
    		IaaSPricingModellerPricingScheme scheme = getVM(VMid).getPricingScheme();
    		return scheme.getTotalCharges(getVM(VMid));
    	}
    	else 
    		return getVM(VMid).getTotalCharges();
    }


	// //////////////////////////// FOR DYNAMIC ENERGY PRICES  // /////////////////////////

    public void updateVMCharges(Price price) {
        updateAverageEnergyPrice();
        for (String key : registeredDynamicEnergyPricesVMs.keySet()) {
        //	System.out.println("I am searching for VMs to be updated");
            registeredDynamicEnergyPricesVMs.get(key).getPricingScheme().updateVMCharges(getVM(key));
        }

    }

    public void updateAverageEnergyPrice() {
        IaaSPricingModellerPricingScheme scheme = new PricingSchemeB(1, IaaSID);
        averageDynamicEnergyPrice.setPrice(scheme.updateAverageEnergyPrice(
                energyProvider, averageDynamicEnergyPrice));

    }

    public Price getAverageDynamicEnergyPrice() {
        return averageDynamicEnergyPrice;
    }


	
	
	
	// ///////////////////////// GENERAL //////////////////////////

	
	
    public void printStaticVMs() {
        for (String i : registeredStaticEnergyPricesVMs.keySet()) {
            VMstate value = registeredStaticEnergyPricesVMs.get(i);
           System.out.println(i + " "
                    + value.getVMinfo().getVMCharacteristics());

        }
    }

    public void printDynamicVMs() {
        for (String i : registeredDynamicEnergyPricesVMs.keySet()) {
            VMstate value = registeredDynamicEnergyPricesVMs.get(i);
            System.out.println(i + " " + value.printVMCharacteristics());

        }
    }

    public VMstate getVM(String VMid) {
        if (registeredDynamicEnergyPricesVMs.containsKey(VMid)) {
            return registeredDynamicEnergyPricesVMs.get(VMid);
        } else {
            return registeredStaticEnergyPricesVMs.get(VMid);
        }
    }

    
    /*
    public IaaSPricingModellerPricingScheme initializedScheme(int schemeId) {
        IaaSPricingModellerPricingScheme scheme = null;
        if (schemeId == 0) {
            scheme = new PricingSchemeA(schemeId);
        }
        if (schemeId == 1) {
            scheme = new PricingSchemeB(schemeId);
        }
        if (schemeId == 2) {
            scheme = new PricingSchemeC(schemeId);
        }
        return scheme;
    }*/

    public double getAppCharges(String appID, EnergyModeller energyModeller) {
    	
        if (apps.containsKey(appID)) {
            double charges = 0;
            LinkedList<VMstate> temp = apps.get(appID).getList();
           
            ListIterator<VMstate> listIterator = temp.listIterator();

            while (listIterator.hasNext()) {
                VMstate VM = listIterator.next();
            	String VMid = VM.getVMid();
                
                if (VM.getVMinfo().getRAM()==0||VM.getVMinfo().getStorage()==0.0){
    		  		
            		try {
            			VmDeployed vm = energyModeller.getVM(VMid);
            			int CPU = vm.getCpus();
            			int RAM = vm.getRamMb();
            			double storage = vm.getDiskGb();
            			

            		} catch (NullPointerException ex) {
            			
            		}
            	}
            	
                charges = charges + getVMCharges(VMid);
            }
       //     System.out.println("Billing: the total app charges of "+appID+ " are " + charges);
            return charges;
        } else {
            return 0;
        }

    }

    public void unregisterApp(String appID) {
        if (apps.containsKey(appID)) {
            LinkedList<VMstate> temp = apps.get(appID).getList();
            ListIterator<VMstate> listIterator = temp.listIterator();
            while (listIterator.hasNext()) {
                unregisterVM(listIterator.next());
            }
        }

        apps.remove(appID);
    }

	public void resizeVM(String vMid, int CPU, int RAM, double storage) {
		VMstate VM = getVM(vMid);
		double charges = getVMCharges(vMid);
	//	System.out.println("Billing: Resize VM. the charges until now are: " + charges);
		Charges tempCharges = new Charges (VM.getChangeTime(), charges);
		VM.getVMinfo().setTotalCharges(tempCharges);
		VMinfo vm = new VMinfo(RAM, CPU, storage, VM.getVMinfo().gethostname(), VM.getVMinfo().getIaaSID());
		VM.setNewVMinfo(vm);
		
	}


	
    ////////////////////////////////////////not used//////////////////////////////////////////////
    /**
	public void registerVM(String VMid) {
		ListIterator<VMstate> listIterator = queue.listIterator();
		VMstate vm;
		boolean found = false;
		while (listIterator.hasNext()) {
			vm = listIterator.next();
			if (vm.getVMid().matches(VMid)) {
				if ((vm.getPricingScheme().getSchemeId() == 0)) {
					registeredStaticEnergyPricesVMs.put(vm.getVMid(), vm);
					
					vm.setStartTime();
				} else {
					registeredDynamicEnergyPricesVMs.put(vm.getVMid(), vm);
					
					vm.setStartTime();
				}
				found = true;
			}
		}
		if (found == false)
			System.out.println("VM has not been found");
}
	**/
}