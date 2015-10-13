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
import java.util.HashMap;

public class Ascetic {

    private final static AsceticMonitor monitor;
    public static boolean changes = false;

    private static final HashMap<String, VM> resources = new HashMap<String, VM>();

    private static double currentCost = 0;
    private static double currentPower = 0;

    private Ascetic() {

    }

    static {
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

        }
    }

    public static void updateConsumptions() {
        for (VM vm : resources.values()) {
            double costStart = vm.getCurrentCost();
            double powerStart = vm.getCurrentPower();
            vm.updateConsumptions();
            double costEnd = vm.getCurrentCost();
            double powerEnd = vm.getCurrentPower();
            currentCost += costEnd - costStart;
            currentPower += powerEnd - powerStart;
            if (powerEnd != powerStart || costEnd != costStart) {
                ResourceManager.updatedConsumptions(vm.getWorker());
            }
        }
    }

    public static boolean executionWithinBoundaries(Worker r, Implementation impl) {
        String IPv4 = r.getName();
        VM vm = resources.get(IPv4);
        double cost = vm.getCost(impl.getCoreId(), impl.getImplementationId());
        double power = vm.getPower(impl.getCoreId(), impl.getImplementationId());
        return ((currentCost + cost < Configuration.getEconomicalBoundary())
                && (currentPower + power < Configuration.getEnergyBoundary()));
    }

    public static void startEvent(Worker resource, Task t, Implementation impl) {
        String IPv4 = resource.getName();
        VM vm = resources.get(IPv4);
        vm.startJob(impl);
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        currentCost += vm.getCost(coreId, implId);
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
        vm.endJob(impl);
        currentCost -= vm.getCost(coreId, implId);
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

    public static double getCost(Worker w, Implementation impl) {
        String IPv4 = w.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        return vm.getCost(coreId, implId);
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
}
