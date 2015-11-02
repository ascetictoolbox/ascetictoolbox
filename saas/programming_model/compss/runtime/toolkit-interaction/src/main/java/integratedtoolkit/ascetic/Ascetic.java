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

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.ResourceManager;
import integratedtoolkit.log.Loggers;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

public class Ascetic {

    private final static AsceticMonitor monitor;
    public static boolean changes = false;

    private static final HashMap<String, VM> resources = new HashMap<String, VM>();

    private static double currentCost = 0;
    private static double currentPower = 0;
    
    private static double accumulatedEnergy = 0d;
    private static double accumulatedCost = 0d;
    private static double initEnergy = 0d;
    private static double initCost = 0d;
    private static long initTime = System.currentTimeMillis();
    
    protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();
    private static boolean realValues = false; 
    private Ascetic() {

    }

    static {
    	String real = System.getProperty("realValues");
		if (real!= null){
			realValues = Boolean.parseBoolean(real);
		}
		if (realValues){
			try {
				initEnergy = AppManager.getAccumulatedEnergy();
				initCost = AppManager.getAccumulatedCost();
			}catch (Exception e) {
				logger.error("Error getting accumulated energy");
			}
		}
    	
    	monitor = new AsceticMonitor();
        monitor.setName("Ascetic Monitor");
        monitor.start();
    }

    public static void discoverNewResources() {
        try {
            for (VM vm : AppManager.getNewResources()) {
                resources.put(vm.getIPv4(), vm);
                (new WorkerStarter(vm)).start();
            }
        } catch (Exception e) {
        	logger.error("Error getting resources");
        }
    }

    public static void updateConsumptions() {
    	if (realValues){
    		try {
    			accumulatedCost = AppManager.getAccumulatedCost();
    		} catch (ApplicationUploaderException e) {
    			logger.error("Error updating accumulated cost", e);
			
    		}
    		try {
    			accumulatedEnergy = AppManager.getAccumulatedEnergy();
    		} catch (ApplicationUploaderException e) {
    			logger.error("Error updating accumulated energy", e);
    		}
    	}
		
    	for (VM vm : resources.values()) {
            double priceStart = vm.getCurrentPrice();
            double powerStart = vm.getCurrentPower();
            vm.updateConsumptions();
            double priceEnd = vm.getCurrentPrice();
            double powerEnd = vm.getCurrentPower();
            currentCost += priceEnd - priceStart;
            currentPower += powerEnd - powerStart;
            if (powerEnd != powerStart || priceEnd != priceStart) {
                ResourceManager.updatedConsumptions(vm.getWorker());
            }
        }
    }
    
    public static Collection<VM> getResources(){
    	return resources.values();
    	
    }

    public static String getAccumulatedCost(){
    	if (realValues){
    		return Double.toString(accumulatedCost-initCost);
    	}else{
    		double cost = accumulatedCost;
    		//System.out.println("accumulated cost is "+ cost);
    		 for (VM vm : Ascetic.getResources()) {
    			 cost += vm.getCurrentCost();
    		 }
    		 //System.out.println("total cost is "+ cost);
    		 return Double.toString(cost);
    		 //return String.format("%.5g%n", cost);
    	}
    }
    
    public static String getAccumulatedEnergy(){
    	if (realValues){
    		return Double.toString(accumulatedEnergy-initEnergy);
    	}else{
    		double energy = accumulatedEnergy;
    		for (VM vm : Ascetic.getResources()) {
   			 	energy += vm.getCurrentEnergy();
    		}
    		return Double.toString(energy);
    		//return String.format("%.5g%n", energy);
   		}
    }
    
    public static String getAccumulatedTime(){
    	long time = (System.currentTimeMillis()-initTime)/1000;
    	//return String.format("%.4g%n", time);
    	return Long.toString(time);
    }
    
    public static boolean executionWithinBoundaries(Worker r, Implementation impl) {
        String IPv4 = r.getName();
        VM vm = resources.get(IPv4);
        double cost = vm.getPrice(impl.getCoreId(), impl.getImplementationId());
        double power = vm.getPower(impl.getCoreId(), impl.getImplementationId());
        double nextCost = currentCost + cost;
        double nextPower = currentPower + power;
        logger.debug("nextCost = " + nextCost + "("+Configuration.getEconomicalBoundary()+") nextPower="+nextPower +"("+Configuration.getEnergyBoundary()+")");
        return ((nextCost < Configuration.getEconomicalBoundary())
                && (nextPower < Configuration.getEnergyBoundary()));
    }

    public static void startEvent(Worker resource, Task t, Implementation impl) {
        String IPv4 = resource.getName();
        VM vm = resources.get(IPv4);
        vm.startJob(impl, t.getId());
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        currentCost += vm.getPrice(coreId, implId);
        currentPower += vm.getPower(coreId, implId);
        String eventType = "core" + coreId + "impl" + implId;
        String eventId = ApplicationMonitor.startEvent(vm, eventType);
        t.setEventId(eventId);
        changes = true;
    }

    public static void stopEvent(Worker resource, Task t, Implementation impl) {

        String IPv4 = resource.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        
        double[] measurements = vm.endJob(impl, t.getId());
        if (!realValues){
        		accumulatedEnergy += measurements[0];
        		accumulatedCost += measurements[1];
        }
        currentCost -= vm.getPrice(coreId, implId);
        currentPower -= vm.getPower(coreId, implId); 
        ApplicationMonitor.stopEvent(t.getEventId());
        changes = true;
    }

    public static void stop() {
        monitor.stop = true;
    }

    public static String getSchedulerOptimization() {
        return Configuration.getOptimizationParameter();
    }

    public static double getEconomicalBoundary() {
        return Configuration.getEconomicalBoundary();
    }

    public static double getPrice(Worker w, Implementation impl) {
        String IPv4 = w.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        return vm.getPrice(coreId, implId);
    }

    public static double getEnergyBoundary() {
        return Configuration.getEnergyBoundary();
    }

    public static double getPower(Worker w, Implementation impl) {
        String IPv4 = w.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        return vm.getPower(coreId, implId);
    }

    private static class AsceticMonitor extends Thread {

        private boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    //Interupted. Do nothing
                }
                Ascetic.discoverNewResources();
                Ascetic.updateConsumptions();
            }
        }
    }

	public static boolean isReal() {
		return realValues;
	}
}
