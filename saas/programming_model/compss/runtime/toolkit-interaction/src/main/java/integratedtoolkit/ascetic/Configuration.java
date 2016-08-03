package integratedtoolkit.ascetic;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductProperty;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import integratedtoolkit.ITConstants;
import integratedtoolkit.ascetic.Ascetic.OptimizationParameter;
import integratedtoolkit.nio.master.configuration.NIOConfiguration;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.components.Processor;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.util.CoreManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class Configuration {

    private final static String applicationId;
    private final static String deploymentId;
    private final static String applicationManagerEndpoint;
    private final static String applicationMonitorEndpoint;
    private final static HashMap<String, NIOConfiguration> componentProperties;
    private final static HashMap<String, CloudMethodResourceDescription> componentDescription;
    private final static HashMap<String, LinkedList<Implementation>> componentImplementations;
    private final static HashMap<String, Cost[][]> componentCosts;
    private final static HashMap<String, long[][]> componentTimes;
    private final static HashMap<String, float[][]> componentWeights;
    private final static float energyBoundary;
    private final static float economicalBoundary;
    private final static OptimizationParameter optimizationParameter;
    private final static boolean FAKE_AM;
    private final static long DISCOVERY_PERIOD;

    static {
        String contextLocation = System.getProperty(ITConstants.IT_CONTEXT);

        String ovfContent = "";
        System.out.println("reading Manifest from " + contextLocation + File.separator + "ovf.xml");
        try {
            ovfContent = readManifest(contextLocation + File.separator + "ovf.xml");
        } catch (IOException ex) {
            System.err.println("Could not load service description");
        }

        FAKE_AM = (System.getProperty("realValues") != null && !Boolean.parseBoolean(System.getProperty("realValues")));

        DISCOVERY_PERIOD = (System.getProperty("discoveryPeriod") != null ? Long.parseLong(System.getProperty("discoveryPeriod")) : 10000l);

        OvfDefinition ovf = OvfDefinition.Factory.newInstance(ovfContent);
        applicationId = ovf.getVirtualSystemCollection().getId();
        System.out.println("Application ID is " + applicationId);
        String dId;
        try {
            dId = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getDeploymentId();
        } catch (NullPointerException ex) {
            System.err.println("Could not find the deployment Id");
            dId = "Test-Deployment";
        }
        deploymentId = dId;
        System.out.println("Deployment ID is " + deploymentId);
        ProductProperty pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticAppManagerURL");
        applicationManagerEndpoint = pp.getValue();
        System.out.println("AppMan EP:" + applicationManagerEndpoint);
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticAppMonitorURL");
        applicationMonitorEndpoint = pp.getValue();
        componentDescription = new HashMap<String, CloudMethodResourceDescription>();
        componentImplementations = new HashMap<String, LinkedList<Implementation>>();
        componentProperties = new HashMap<String, NIOConfiguration>();
        componentWeights = new HashMap<String, float[][]>();
        componentTimes = new HashMap<String, long[][]>();
        componentCosts = new HashMap<String, Cost[][]>();
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticEnergyOptimizationBoundary");
        energyBoundary = Float.parseFloat(pp.getValue());
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticCostOptimizationBoundary");
        economicalBoundary = Float.parseFloat(pp.getValue());
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticOptimizationParameter");
        String opParam = pp.getValue().toLowerCase();
        if (opParam.equals("energy")) {
            optimizationParameter = OptimizationParameter.ENERGY;
        } else if (opParam.equals("cost")) {
            optimizationParameter = OptimizationParameter.COST;
        } else {
            optimizationParameter = OptimizationParameter.TIME;
        }
        parseComponents(ovf);
    }

    public static String getApplicationId() {
        return applicationId;
    }

    public static String getDeploymentId() {
        return deploymentId;
    }

    public static String getApplicationMonitorEndpoint() {
        return applicationMonitorEndpoint;
    }

    public static String getApplicationManagerEndpoint() {
        return applicationManagerEndpoint;
    }

    public static LinkedList<Implementation> getComponentImplementations(String component) {
        return componentImplementations.get(component);
    }

    private static void parseComponents(OvfDefinition ovf) {
        HashMap<String, Integer> diskSize = new HashMap<String, Integer>();
        for (Disk d : ovf.getDiskSection().getDiskArray()) {
            diskSize.put(d.getDiskId(), Integer.parseInt(d.getCapacity()) / 1024);
        }
        for (VirtualSystem vs : ovf.getVirtualSystemCollection().getVirtualSystemArray()) {
            String componentName = vs.getId();
            Integer storageElemSize = diskSize.get(componentName + "-disk");
            CloudMethodResourceDescription rd = createComponentDescription(componentName, vs, storageElemSize);
            componentDescription.put(componentName, rd);
            float[][] eventWeights = new float[CoreManager.getCoreCount()][];
            long[][] eventTimes = new long[CoreManager.getCoreCount()][];
            Cost[][] eventCosts = new Cost[CoreManager.getCoreCount()][];
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                int implCount = CoreManager.getCoreImplementations(coreId).length;
                eventWeights[coreId] = new float[implCount];
                eventTimes[coreId] = new long[implCount];
                eventCosts[coreId] = new Cost[implCount];
                for (int implId = 0; implId < implCount; implId++) {
                    eventWeights[coreId][implId] = 0f;
                    eventTimes[coreId][implId] = Long.MAX_VALUE;
                    eventCosts[coreId][implId] = new Cost();
                }
            }
            componentWeights.put(componentName, eventWeights);
            componentTimes.put(componentName, eventTimes);
            componentCosts.put(componentName, eventCosts);
            LinkedList<Implementation> impls = new LinkedList<Implementation>();
            String implList = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMElements").getValue();
            if (implList.length() > 0) {
                for (String implementationString : implList.split(";")) {
                    String[] implementation = implementationString.split("@");
                    String signature = implementation[0];
                    float eventWeight = Float.parseFloat(implementation[1]);
                    long time = Long.parseLong(implementation[2]);
                    double charges = Math.random() /* Double.parseLong(implementation[3])*/;
                    double energy = Math.random()/*  Double.parseLong(implementation[4])*/;
                    Implementation impl = CoreManager.getImplementation(signature);
                    if (impl != null) {
                        impls.add(impl);
                        int coreId = impl.getCoreId();
                        int implId = impl.getImplementationId();
                        eventWeights[coreId][implId] = eventWeight;
                        eventTimes[coreId][implId] = time;
                        eventCosts[coreId][implId].setCharges(charges);
                        eventCosts[coreId][implId].setEnergyValue(energy);
                    }
                }
            }
            NIOConfiguration conf = new NIOConfiguration("integratedtoolkit.nio.master.NIOAdaptor");
            String propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMUser").getValue();
            conf.setUser(propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMInstallDir").getValue();
            conf.setInstallDir(propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMWorkingDir").getValue();
            conf.setWorkingDir(propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMAppDir").getValue();
            conf.setAppDir(propertyValue);
            conf.setMinPort(43001);
            conf.setMaxPort(43001);
            componentProperties.put(componentName, conf);
            componentImplementations.put(componentName, impls);
        }
    }

    private static CloudMethodResourceDescription createComponentDescription(String name, VirtualSystem vs, int storage) {
        CloudMethodResourceDescription rd = new CloudMethodResourceDescription();
        rd.setName(name);
        int coreCount = vs.getVirtualHardwareSection().getNumberOfVirtualCPUs();
        if (coreCount == 0) {
            coreCount = 1;
        }
        Processor proc = new Processor();
        rd.addProcessor(proc);
        float cpuspeed = ((float) vs.getVirtualHardwareSection().getCPUSpeed() / 1000f);
        proc.setComputingUnits(coreCount);
        proc.setSpeed(cpuspeed);

        int memory = vs.getVirtualHardwareSection().getMemorySize();
        if (memory > 0) {
            rd.setMemorySize((float) memory / 1024f);
        }
        rd.setType(name);
        rd.setStorageSize(storage);
        return rd;
    }

    private static String readManifest(String manifestLocation) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(manifestLocation));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    public static MethodResourceDescription getComponentDescriptions(String component) {
        return componentDescription.get(component);
    }

    public static NIOConfiguration getComponentProperties(String component) {
        return componentProperties.get(component);
    }

    public static float getEnergyBoundary() {
        return energyBoundary;
    }

    public static float getEconomicalBoundary() {
        return economicalBoundary;
    }

    public static OptimizationParameter getOptimizationParameter() {
        return optimizationParameter;
    }

    public static boolean useFakeAppManager() {
        return FAKE_AM;
    }

    public static long getDiscoveryPeriod() {
        return DISCOVERY_PERIOD;
    }

    public static float[][] getEventWeights(String component) {
        return componentWeights.get(component);
    }

    public static long[][] getComponentTimes(String component) {
        return componentTimes.get(component);
    }

    public static Cost[][] getDefaultCosts(String component) {
        return componentCosts.get(component);
    }
}
