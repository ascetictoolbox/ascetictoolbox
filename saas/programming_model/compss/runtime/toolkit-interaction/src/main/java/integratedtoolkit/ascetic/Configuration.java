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

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductProperty;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import integratedtoolkit.ITConstants;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
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
    private final static HashMap<String, HashMap<String, String>> componentProperties;
    private final static HashMap<String, MethodResourceDescription> componentDescription;
    private final static HashMap<String, LinkedList<Implementation>> componentImplementations;
    private final static float energyBoundary;
    private final static float economicalBoundary;
    private final static String optimizationParameter;

    static {
        String contextLocation = System.getProperty(ITConstants.IT_CONTEXT);

        String ovfContent = "";
        System.out.println("reading Manifest from " + contextLocation + File.separator + "ovf.xml");
        try {
            ovfContent = readManifest(contextLocation + File.separator + "ovf.xml");
        } catch (IOException ex) {
            System.err.println("Could not load service description");
        }
        OvfDefinition ovf = OvfDefinition.Factory.newInstance(ovfContent);
        applicationId = ovf.getVirtualSystemCollection().getId();
        System.out.println("Application ID is "+applicationId);
        String dId;
        try {
            dId = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getDeploymentId();
        } catch (NullPointerException ex) {
            System.err.println("Could not find the deployment Id");
            dId = "Test-Deployment";
        }
        deploymentId = dId;
        System.out.println("Deployment ID is "+deploymentId);
        ProductProperty pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticAppManagerURL");
        applicationManagerEndpoint = pp.getValue();
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticAppMonitorURL");
        applicationMonitorEndpoint = pp.getValue();
        //applicationMonitorEndpoint = "http://10.4.0.16:9000/";
        componentDescription = new HashMap<String, MethodResourceDescription>();
        componentImplementations = new HashMap<String, LinkedList<Implementation>>();
        componentProperties = new HashMap<String, HashMap<String, String>>();
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticEnergyOptimizationBoundary");
        energyBoundary = Float.parseFloat(pp.getValue());
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticCostOptimizationBoundary");
        economicalBoundary = Float.parseFloat(pp.getValue());
        pp = ovf.getVirtualSystemCollection().getProductSectionAtIndex(0).getPropertyByKey("asceticOptimizationParameter");
        optimizationParameter = pp.getValue();
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
            MethodResourceDescription rd = createComponentDescription(componentName, vs, storageElemSize);
            componentDescription.put(componentName, rd);
            LinkedList<Implementation> impls = new LinkedList<Implementation>();
            String implList = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMElements").getValue();
            if (implList.length() > 0) {
                for (String signature : implList.split(";")) {
                    Implementation impl = CoreManager.getImplementation(signature);
                    if (impl != null) {
                        impls.add(impl);
                    }
                }
            }
            HashMap<String, String> properties = new HashMap<String, String>();
            String propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMUser").getValue();
            properties.put(ITConstants.USER, propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMInstallDir").getValue();
            properties.put(ITConstants.INSTALL_DIR, propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMWorkingDir").getValue();
            properties.put(ITConstants.WORKING_DIR, propertyValue);
            propertyValue = vs.getProductSectionArray()[0].getPropertyByKey("asceticPMAppDir").getValue();
            properties.put(ITConstants.APP_DIR, propertyValue);
            componentProperties.put(componentName, properties);
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
        int cpuspeed = vs.getVirtualHardwareSection().getCPUSpeed();
        rd.setProcessorCPUCount(1);
        rd.setProcessorCoreCount(coreCount);
        rd.setSlots(coreCount);
        rd.setProcessorSpeed((float) cpuspeed / 1000f);

        int memory = vs.getVirtualHardwareSection().getMemorySize();
        if (memory > 0) {
            rd.setMemoryPhysicalSize((float) memory / 1024f);
        }

        rd.setValue(0);
        rd.setType(name);
        rd.setStorageElemSize(storage);

        HashMap<String, String> properties = new HashMap<String, String>();
        ProductProperty p = vs.getProductSectionAtIndex(0).getPropertyByKey("asceticPMInstallDir");
        if (p != null) {
            properties.put(ITConstants.INSTALL_DIR, p.getValue());
        }
        p = vs.getProductSectionAtIndex(0).getPropertyByKey("asceticPMWorkingDir");
        if (p != null) {
            properties.put(ITConstants.WORKING_DIR, p.getValue());
        }
        p = vs.getProductSectionAtIndex(0).getPropertyByKey("asceticPMUser");
        if (p != null) {
            properties.put(ITConstants.USER, p.getValue());
        }
        p = vs.getProductSectionAtIndex(0).getPropertyByKey("asceticPMAppDir");
        if (p != null) {
            properties.put(ITConstants.APP_DIR, p.getValue());
        }
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

    public static HashMap<String, String> getComponentProperties(String component) {
        return componentProperties.get(component);
    }

    public static float getEnergyBoundary() {
        return energyBoundary;
    }

    public static float getEconomicalBoundary() {
        return economicalBoundary;
    }

    public static String getOptimizationParameter() {
        return optimizationParameter;
    }
}
