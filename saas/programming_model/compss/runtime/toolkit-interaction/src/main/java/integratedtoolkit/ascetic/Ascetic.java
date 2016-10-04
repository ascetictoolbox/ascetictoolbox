package integratedtoolkit.ascetic;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.log.Loggers;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import integratedtoolkit.ascetic.fake.FakeAppManager;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.MethodResourceDescription;
import java.util.LinkedList;

public class Ascetic {

    public enum OptimizationParameter {

        TIME,
        COST,
        ENERGY
    }
    private final static AsceticMonitor monitor;
    public static boolean changes = false;

    private static final HashMap<String, VM> resources = new HashMap<String, VM>();

    private static double currentCost = 0;
    private static double currentPower = 0;

    private static double initEnergy = 0d;
    private static double initCost = 0d;
    private static long initTime = System.currentTimeMillis();

    protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();

    private static final AppManager APP_MANAGER;

    static {
        String applicationId = Configuration.getApplicationId();
        String deploymentId = Configuration.getDeploymentId();
        String amEndpoint = Configuration.getApplicationManagerEndpoint();

        if (Configuration.useFakeAppManager()) {
            APP_MANAGER = new FakeAppManager(applicationId, deploymentId);
        } else {
            APP_MANAGER = new AppManager(applicationId, deploymentId, amEndpoint);
        }

        try {
        	System.out.println("***** Initial Values ******");
            initEnergy = APP_MANAGER.getAccumulatedEnergy();
            System.out.println("* - Initial Energy: "+initEnergy);
            initCost = APP_MANAGER.getAccumulatedCost();
            System.out.println("* - Initial Cost: "+initCost);
            
        } catch (Exception e) {
            logger.error("Error getting accumulated energy or cost", e);
            System.out.println("Error getting accumulated energy");
            e.printStackTrace();
        }

        monitor = new AsceticMonitor();
        monitor.setName("Ascetic Monitor");
        monitor.start();
    }

    public static LinkedList<String> getComponentNames() {
        return Configuration.getComponentNames();
    }

    public static MethodResourceDescription getComponentDescription(String componentName) {
        return Configuration.getComponentDescriptions(componentName);
    }

    public static void discoverNewResources() {
        System.out.println("Discovering new Resources");
        try {
            for (VM vm : APP_MANAGER.getNewResources()) {
                resources.put(vm.getIPv4(), vm);
                (new WorkerStarter(vm)).start();
            }
        } catch (Exception e) {
            logger.error("Error getting resources");
        }
    }

    public static void addNewResource(VM vm) {
        resources.put(vm.getIPv4(), vm);
        vm.updateConsumptions(APP_MANAGER);
    }

    public static void removeResource(String ip) {
        resources.remove(ip);
    }

    public static void updateConsumptions() {

        for (VM vm : resources.values()) {
            double priceStart = vm.getRunningPrice();
            double powerStart = vm.getRunningPower();
            vm.updateConsumptions(APP_MANAGER);
            double priceEnd = vm.getRunningPrice();
            double powerEnd = vm.getRunningPower();
            currentCost += priceEnd - priceStart;
            currentPower += powerEnd - powerStart;
        }
    }

    public static Collection<VM> getResources() {
        return resources.values();
    }

    public static double getAccumulatedCost() {
        try {
            double accumulatedCost = APP_MANAGER.getAccumulatedCost();
            return accumulatedCost - initCost;
        } catch (ApplicationUploaderException e) {
            logger.error("Error obtaining accumulated cost", e);
            return initCost;
        }

    }

    public static double getAccumulatedEnergy() {
        try {
            double accumulatedEnergy = APP_MANAGER.getAccumulatedEnergy();
            return accumulatedEnergy - initEnergy;
        } catch (ApplicationUploaderException e) {
            logger.error("Error updating accumulated energy", e);
            return initEnergy;
        }
    }

    public static long getAccumulatedTime() {
        return (System.currentTimeMillis() - initTime) / 1000;
    }

    public static boolean executionWithinBoundaries(Worker r, Implementation impl) {
        String IPv4 = r.getName();
        VM vm = resources.get(IPv4);
        double cost = 0;
        double power = 0;
        if (vm != null) {
            cost = vm.getPrice(impl.getCoreId(), impl.getImplementationId());
            power = vm.getPower(impl.getCoreId(), impl.getImplementationId());
        } else {
            logger.debug("VM " + IPv4 + "not found in resources.");
        }
        double nextCost = currentCost + cost;
        double nextPower = currentPower + power;
        logger.debug("nextCost = " + nextCost + "(" + Configuration.getEconomicalBoundary() + ") nextPower=" + nextPower + "(" + Configuration.getEnergyBoundary() + ")");
        return ((nextCost < Configuration.getEconomicalBoundary())
                && (nextPower < Configuration.getEnergyBoundary()));
    }

    public static void requestVMCreation(String image, String type) throws ApplicationUploaderException {
        System.out.println("Requesting a " +type + " instance with image " + image + " to the APP_MANAGER");
        APP_MANAGER.requestVMCreation(type);
    }

    public static void requestVMDestruction(Worker worker) throws ApplicationUploaderException {
        String IPv4 = worker.getName();
        VM vm = resources.get(IPv4);
        String amId = Integer.toString(vm.getAMId());
        System.out.println("Requesting destruction of " + worker.getName() + "(ID: " + amId + ") to the APP_MANAGER");
        APP_MANAGER.requestVMDestruction(amId);
    }

    public static void startEvent(Worker resource, Implementation impl, AllocatableAction action) {
        String IPv4 = resource.getName();
        VM vm = resources.get(IPv4);
        vm.startJob(action);
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        currentCost += vm.getPrice(coreId, implId);
        currentPower += vm.getPower(coreId, implId);
        String eventId = ApplicationMonitor.startEvent(vm, impl);
        //String eventId = java.util.UUID.randomUUID().toString();
        action.setEventId(eventId);
        changes = true;
    }

    public static void stopEvent(Worker resource, Implementation impl, AllocatableAction action) {

        String IPv4 = resource.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        vm.endJob(action);
        currentCost -= vm.getPrice(coreId, implId);
        currentPower -= vm.getPower(coreId, implId);
        ApplicationMonitor.stopEvent(action.getEventId());
        changes = true;
    }

    public static void stop() {
        monitor.stop = true;
    }

    public static OptimizationParameter getSchedulerOptimization() {
        return Configuration.getOptimizationParameter();
    }

    public static double getEconomicalBoundary() {
        return Configuration.getEconomicalBoundary();
    }

    public static double getPrice(String componentName) {
        return Configuration.getIdleCosts(componentName).getCharges();
    }

    public static double getPrice(String componentName, Implementation impl) {
        return Configuration.getDefaultCosts(componentName)[impl.getCoreId()][impl.getImplementationId()].getCharges();
    }

    public static double getPrice(Worker w) {
        String IPv4 = w.getName();
        VM vm = resources.get(IPv4);
        return vm.getIdlePrice();
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

    public static double getPower(String componentName) {
        return Configuration.getIdleCosts(componentName).getPowerValue();
    }

    public static double getPower(String componentName, Implementation impl) {
        return Configuration.getDefaultCosts(componentName)[impl.getCoreId()][impl.getImplementationId()].getPowerValue();
    }

    public static double getPower(Worker w) {
        String IPv4 = w.getName();
        VM vm = resources.get(IPv4);
        return vm.getIdlePower();
    }

    public static double getPower(Worker w, Implementation impl) {
        String IPv4 = w.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        return vm.getPower(coreId, implId);
    }

    public static long getTimeBoundary() {
        return Configuration.getPerformanceBoundary();
    }

    public static long getExecutionTime(String componentName, Implementation impl) {
        return Configuration.getComponentTimes(componentName)[impl.getCoreId()][impl.getImplementationId()];
    }

    public static long getExecutionTime(Worker w, Implementation impl) {
        String IPv4 = w.getName();
        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        VM vm = resources.get(IPv4);
        return vm.getExecutionTime(coreId, implId);
    }

    public static boolean canReplicateComponent(String componentName) {
        int currentVMs = VM.getComponentCount(componentName);
        return Configuration.withinBoundaries(componentName, currentVMs + 1);
    }

    public static boolean canTerminateVM(Worker w) {
        String IPv4 = w.getName();
        VM vm = resources.get(IPv4);
        String componentName = vm.getComponentId();
        int currentVMs = VM.getComponentCount(componentName);
        return Configuration.withinBoundaries(componentName, currentVMs - 1);
    }

    private static class AsceticMonitor extends Thread {

        private boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                Ascetic.discoverNewResources();
                Ascetic.updateConsumptions();
                try {
                    Thread.sleep(Configuration.getDiscoveryPeriod());
                } catch (InterruptedException ex) {
                    //Interupted. Do nothing
                }
            }
            System.out.println("***** Ascetic Monitoring stopped ******");
            System.out.println("-Elapsed Time: " + Ascetic.getAccumulatedTime());
            System.out.println("-Total Cost: " + Ascetic.getAccumulatedCost());
            System.out.println("-Total Energy: " + Ascetic.getAccumulatedEnergy());
            System.out.println("***************************************");
        }
    }

}
