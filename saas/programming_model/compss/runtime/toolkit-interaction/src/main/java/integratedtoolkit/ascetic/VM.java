package integratedtoolkit.ascetic;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.components.Processor;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.nio.master.configuration.NIOConfiguration;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.MethodWorker;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.CloudManager;
import integratedtoolkit.util.CoreManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class VM {

    protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();
    private static final long UPDATE_FREQ = 30000;
    private long lastUpdate = 0l;

    private HashMap<AllocatableAction, JobExecution> runningJobs = new HashMap<AllocatableAction, JobExecution>();

    private final static int[] implCount = new int[CoreManager.getCoreCount()];
    private final static TreeMap<String, Integer> componentCount = new TreeMap<String, Integer>();

    static {
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            implCount[coreId] = CoreManager.getCoreImplementations(coreId).length;
        }
    }

    private final eu.ascetic.paas.applicationmanager.model.VM vm;

    private Worker worker;
    private final CloudMethodResourceDescription description;
    private final NIOConfiguration configuration;
    private final LinkedList<Implementation> compatibleImpls;
    private double idlePower;
    private double idlePrice;
    private double[][] power;
    private double[][] price;
    private float[][] eventWeights;
    private long[][] times;
    private double coresEnergy;
    private double coresCost;

    public VM(eu.ascetic.paas.applicationmanager.model.VM vm) {
        logger.info("Creating a new VM");
        System.out.println("Detected VM " + vm.getIp());
        this.vm = vm;
        String ovfId = vm.getOvfId();
        Integer count = componentCount.get(ovfId);
        if (count == null) {
            count = 0;
        }
        count++;
        componentCount.put(ovfId, count);
        MethodResourceDescription rd = Configuration.getComponentDescriptions(ovfId);
        description = new CloudMethodResourceDescription(rd);
        description.setType(ovfId);
        description.setImage(CloudManager.getProvider("Ascetic").getImage(ovfId));
        updateComponentDescription(vm, description);
        configuration = new NIOConfiguration(Configuration.getComponentProperties(ovfId));
        configuration.setLimitOfTasks(rd.getProcessors().get(0).getComputingUnits());
        configuration.setTotalComputingUnits(rd.getProcessors().get(0).getComputingUnits());
        configuration.setHost(vm.getIp());
        compatibleImpls = Configuration.getComponentImplementations(ovfId);
        Cost idleCost = Configuration.getIdleCosts(ovfId);
        idlePower = idleCost.getPowerValue();
        idlePrice = idleCost.getCharges();
        power = new double[CoreManager.getCoreCount()][];
        price = new double[CoreManager.getCoreCount()][];
        Cost[][] defaultCosts = Configuration.getDefaultCosts(ovfId);
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            power[coreId] = new double[implCount[coreId]];
            price[coreId] = new double[implCount[coreId]];
            for (int implId = 0; implId < implCount[coreId]; implId++) {
                if (defaultCosts[coreId][implId] != null) {
                    power[coreId][implId] = defaultCosts[coreId][implId].getPowerValue();
                    price[coreId][implId] = defaultCosts[coreId][implId].getCharges();
                }
            }
        }
        
        times = Configuration.getComponentTimes(ovfId);
        eventWeights = Configuration.getEventWeights(ovfId);
        coresEnergy = 0;
        coresCost = 0;
        
        System.out.println("\tIdle");
        System.out.println("\t\t Power: " + idlePower);
        System.out.println("\t\t Price: " + idlePrice);
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            System.out.println("\tCore " + coreId);
            for (int implId = 0; implId < implCount[coreId]; implId++) {
                System.out.println("\t\tImplementation " + implId);
                System.out.println("\t\t\t Time: " + times[coreId][implId]);
                System.out.println("\t\t\t Power: " + power[coreId][implId]);
                System.out.println("\t\t\t Energy: " + power[coreId][implId] * times[coreId][implId] / 1000);
                System.out.println("\t\t\t Price: " + price[coreId][implId]);
            }
        }
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
    
    public int getAMId() {
        return vm.getId();
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public void updateConsumptions(AppManager appManager) {
    	System.out.println(" *** Update Consumptions ****");
        if (System.currentTimeMillis() - lastUpdate > UPDATE_FREQ) {
            idlePower = idlePower;
            idlePrice = idlePrice;
            System.out.println(" *** Update Consumptions ****");
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                for (int implId = 0; implId < implCount[coreId]; implId++) {
                	System.out.println("\t\t CURRENT VALUES for " + getIPv4());
                	Cost c = null;
                    try {
                        c = appManager.getEstimations("" + vm.getId(), coreId, implId);
                    } catch (ApplicationUploaderException ex) {
                        System.err.println("Could not update the energy consumtion for"
                                + " core " + coreId + " implementation " + implId
                                + " in " + vm.getIp());
                        ex.printStackTrace(System.err);
                    }
                    if (c != null) {
                        if (price[coreId][implId] <= 0) {
                            price[coreId][implId] = c.getCharges();
                        }else{
                        	System.out.println("\t\t\t Obtained: Charges are "+c.getCharges());
                        }
                        
                        if (power[coreId][implId] <= 0) {
                            power[coreId][implId] = c.getPowerValue();
                        }else{
                        	System.out.println("\t\t\t Obtained: Power are "+c.getPowerValue());
                        }
                    }else{
                    	System.out.println("Estimations returned null. Could not update the energy consumtion for"
                                + " core " + coreId + " implementation " + implId
                                + " in " + vm.getIp());
                    }
                    System.out.println("\t\t Final VALUES for " + getIPv4()
                            + ": Core " + coreId + " impl " + implId
                            + " Power:  " + power[coreId][implId]
                            + " Price: " + price[coreId][implId]);
                }
            }
            lastUpdate = System.currentTimeMillis();
        }
    }
    
    private static void updateComponentDescription(eu.ascetic.paas.applicationmanager.model.VM vm, CloudMethodResourceDescription rd) {
        int coreCount = vm.getCpuActual();
        if (coreCount > 0) {
        	System.out.println("Updating coreCount to "+coreCount);
            Processor proc = rd.getProcessors().get(0);
            proc.setComputingUnits(coreCount);
        }
        
        long memory = vm.getRamActual();
        if (memory > 0) {
        	System.out.println("Updating memory to "+memory);
            rd.setMemorySize((float) memory / 1024f);
        }
        long storage = vm.getDiskActual();
        if (storage > 0) {
        	System.out.println("Updating storage to "+storage);
            rd.setStorageSize(storage);
        }
    }

    public NIOConfiguration getConfiguration() {
        return this.configuration;
    }

    public void startJob(AllocatableAction action) {
        Implementation impl = action.getAssignedImplementation();
        runningJobs.put(action, new JobExecution(action, impl));
    }

    public void endJob(AllocatableAction action) {
        long currentTime = System.currentTimeMillis();
        JobExecution je = runningJobs.get(action);
        int coreId = je.impl.getCoreId();
        int implId = je.impl.getImplementationId();
        coresEnergy += power[coreId][implId] * (currentTime - je.startTime) / (3600 * 1000);
        coresCost += price[coreId][implId] * (currentTime - je.startTime) / (3600 * 1000);
        runningJobs.remove(action);
    }

    public double getRunningPrice() {
        double currentPrice = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            currentPrice += price[coreId][implId];
        }
        return currentPrice;
    }

    public double getRunningCost() {
        long currentTime = System.currentTimeMillis();
        double cost = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            cost += price[coreId][implId] * (currentTime - je.startTime) / (3600 * 1000);
        }
        return cost;
    }

    public double getAccumulatedCost() {
        return this.coresCost;
    }

    public double getRunningPower() {
        double currentPower = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            currentPower += power[coreId][implId];
        }
        return currentPower;
    }

    public double getRunningEnergy() {
        long currentTime = System.currentTimeMillis();
        double energy = 0d;
        for (JobExecution je : runningJobs.values()) {
            int coreId = je.impl.getCoreId();
            int implId = je.impl.getImplementationId();
            energy += power[coreId][implId] * (currentTime - je.startTime) / (3600 * 1000);
        }
        return energy;
    }

    public double getAccumulatedEnergy() {
        return this.coresEnergy;
    }

    public long getExecutionTime(int coreId, int implId) {
        return times[coreId][implId];
    }

    public double[] getPrice(int coreId) {
        return price[coreId];
    }

    public double getPrice(int coreId, int implId) {
        double c = price[coreId][implId];
        if (c <= 0) {
            return 0.0001;
        } else {
            return c;
        }
    }

    public double[] getPower(int coreId) {
        return power[coreId];
    }

    public double getPower(int coreId, int implId) {
        double pw = power[coreId][implId];
        if (pw <= 0) {
            return 0.1;
        } else {
            return pw;
        }
    }

    public float getEventWeight(int coreId, int implId) {
        return this.eventWeights[coreId][implId];
    }

    public void setWorker(MethodWorker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return this.worker;
    }

    public LinkedList<Implementation> getCompatibleImplementations() {
        return this.compatibleImpls;
    }

    public double getIdlePower() {
        return idlePower;
    }

    public double getIdlePrice() {
        return idlePrice;
    }

    public static int getComponentCount(String component) {
        Integer count = componentCount.get(component);
        if (count == null) {
            count = 0;
        }
        return count;
    }

    private class JobExecution {

        AllocatableAction action;
        Implementation impl;
        long startTime;

        JobExecution(AllocatableAction action, Implementation impl) {
            this.action = action;
            this.impl = impl;
            this.startTime = System.currentTimeMillis();
        }
    }
}
