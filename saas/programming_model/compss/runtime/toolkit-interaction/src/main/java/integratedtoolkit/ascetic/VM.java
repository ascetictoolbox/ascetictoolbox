/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.ascetic;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.MethodWorker;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.CoreManager;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

public class VM {
	protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();
    private static final long UPDATE_FREQ = 30000;
    private long lastUpdate = 0l;
    private HashMap<Integer,JobExecution> runningJobs = new HashMap<Integer,JobExecution>();

    private final static int[] implCount = new int[CoreManager.getCoreCount()];

    static {
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            implCount[coreId] = CoreManager.getCoreImplementations(coreId).length;
        }
    }

    private final eu.ascetic.paas.applicationmanager.model.VM vm;

    private Worker worker;
    private final ResourceDescription description;
    private final HashMap<String, String> properties;
    private double[][] power;
    private double[][] price;
    //private double[][] energy;
    private boolean[][] executed;

    public VM(eu.ascetic.paas.applicationmanager.model.VM vm) {
        logger.info("Creating a new VM");
        this.vm = vm;
        MethodResourceDescription rd = Configuration.getComponentDescriptions(vm.getOvfId());
        description = new MethodResourceDescription(rd);
        properties = Configuration.getComponentProperties(vm.getOvfId());
        power = new double[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            power[coreId] = new double[implCount[coreId]];
        }
        price = new double[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            price[coreId] = new double[implCount[coreId]];
        }
        /*energy = new double[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            energy[coreId] = new double[implCount[coreId]];
        }*/
        executed = new boolean[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            executed[coreId] = new boolean[implCount[coreId]];
            for (int implId = 0; implId < implCount[coreId]; implId++) {
            	executed[coreId][implId] = false;
            }
        }
        
        logger.debug("Updating consumptions");
        updateConsumptions();
    }

    public String getIPv4() {
        return vm.getIp();
    }

    public String getProviderId() {
        return vm.getProviderVmId();
    }

    public String getComponentId() {
        return vm.getOvfId();
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public void updateConsumptions() {
        if (System.currentTimeMillis() - lastUpdate > UPDATE_FREQ) {
            if (Ascetic.isReal()){
				for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
					for (int implId = 0; implId < implCount[coreId]; implId++) {
						String eventType = "core" + coreId + "impl" + implId;
						try {

							Cost c = AppManager.getEstimations("" + vm.getId(),
									eventType);
							if (price[coreId][implId] <= 0) {
								price[coreId][implId] = c.getCharges();
							}
							if (power[coreId][implId] <= 0) {
								power[coreId][implId] = c.getPowerValue();
							}
							/*
							 * if (energy[coreId][implId] <= 0){
							 * energy[coreId][implId] = c.getEnergyValue(); }
							 */
						} catch (ApplicationUploaderException ex) {
							if (power[coreId][implId] < 0) {
								power[coreId][implId] = 0;
							}
							if (price[coreId][implId] < 0) {
								price[coreId][implId] = 0;
							}
							/*
							 * if (energy[coreId][implId] < 0){
							 * energy[coreId][implId] = 0; }
							 */
							System.err
									.println("Could not update the energy consumtion for "
											+ eventType + " in " + vm.getIp());
							ex.printStackTrace(System.err);
						}
						logger.debug("\t\t CURRENT VALUES for " + getIPv4()
								+ ": Core " + coreId + " impl " + implId
								+ " Power:  " + power[coreId][implId]
								+ " Price: " + price[coreId][implId]);
					}
				}
            }else{
				for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
					for (int implId = 0; implId < implCount[coreId]; implId++) {

						if (implId == 1) {
							power[coreId][implId] = 25.00 + (Math.random() * 1.5);
						} else {
							power[coreId][implId] = 13.00 + (Math.random() * 1.5);
						}
						price[coreId][implId] = 0.085 + (0.0002 * power[coreId][implId]);
					}
				}
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    public void startJob(Implementation impl, int taskId) {
        runningJobs.put(taskId,new JobExecution(taskId,impl));
        executed[impl.getCoreId()][impl.getImplementationId()] = true;
    }

    public double[] endJob(Implementation impl, int taskId) {
    	long currentTime = System.currentTimeMillis();
    	double[] measurements = new double[2];
    	JobExecution je = runningJobs.get(taskId);
    	int coreId = je.impl.getCoreId();
        int implId = je.impl.getImplementationId();
        measurements[0] = power[coreId][implId]*(currentTime-je.startTime)/(3600*1000);
        measurements[1] = price[coreId][implId]*(currentTime-je.startTime)/(3600*1000);
    	runningJobs.remove(taskId);
    	return measurements;
    }

    public double getCurrentPrice() {
        double currentPrice = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            currentPrice += price[coreId][implId];
        }
        return currentPrice;
    }
    
    public double getCurrentCost() {
        long currentTime = System.currentTimeMillis();
    	double cost = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            cost += price[coreId][implId]*(currentTime-je.startTime)/(3600*1000);
        }
        return cost;
    }
    
    public double getCurrentEnergy() {
        long currentTime = System.currentTimeMillis();
    	double energy = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            energy += power[coreId][implId]*(currentTime-je.startTime)/(3600*1000);
        }
        return energy;
    }

    public double[] getPrice(int coreId) {
        return price[coreId];
    }

    public double getPrice(int coreId, int implId) {
        double c = price[coreId][implId];
        if (c <= 0){
        	return 0.0001;
        }else{
        	return c;
        }
    }

    public double getCurrentPower() {
        double currentPower = 0d;
        for (JobExecution je : runningJobs.values()) {
        	int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            currentPower += power[coreId][implId];
        }
        return currentPower;
    }

    public double[] getPower(int coreId) {
        return power[coreId];
    }

    public double getPower(int coreId, int implId) {
    	double pw = power[coreId][implId];
        if (pw <= 0 ){
        	return 0.1;
        }else{
        	return pw;
        }
    }

    public void setWorker(MethodWorker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return this.worker;
    }
    
    public boolean isrunning(int coreId, int implId){
    	return executed[coreId][implId];
    }
  
    private class JobExecution{
	int taskId;
	Implementation impl;
	long startTime;
	
	JobExecution(int taskId, Implementation impl){
		this.taskId = taskId;
		this.impl = impl;
		this.startTime = System.currentTimeMillis();
	}
}  
}


