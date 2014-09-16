/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.types;

import integratedtoolkit.connectors.Connector;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.connectors.Cost;
import integratedtoolkit.util.CloudImageManager;
import integratedtoolkit.util.CloudTypeManager;
import integratedtoolkit.util.CoreManager;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class CloudProvider {

    public String name;
    private Connector connector;
    private Cost cost;
    private CloudImageManager imgManager;
    private CloudTypeManager typeManager;
    private int currentVMCount;
    private int endingVMCount;
    private Integer limitOfVMs;

    public CloudProvider(String connectorPath, Integer limitOfVMs, HashMap<String, String> connectorProperties, String name)
            throws Exception {
        this.name = name;
        this.limitOfVMs = limitOfVMs;
        imgManager = new CloudImageManager();
        Class<?> conClass = Class.forName(connectorPath);
        Constructor<?> ctor = conClass.getDeclaredConstructors()[0];
        Object conector = ctor.newInstance(name, connectorProperties);
        connector = (Connector) conector;
        cost = (Cost) conector;
        typeManager = new CloudTypeManager();
        currentVMCount = 0;
        endingVMCount = 0;
    }

    /* -----------------------------------------
     * ------- Cloud Provider Builders ---------
     * ----------------------------------------*/
    public void addCloudImage(CloudImageDescription cid) {
        imgManager.add(cid);
    }

    public void addInstanceType(ResourceDescription rd) {
        typeManager.add(rd);
    }

    public void newCoreElementsDetected(LinkedList<Integer> newCores) {
        typeManager.newCoreElementsDetected(newCores);
    }

    /* -----------------------------------------
     * ------------- Basic Queries -------------
     * ----------------------------------------*/
    public String getName() {
        return name;
    }

    public float getCurrentCostPerHour() {
        return cost.currentCostPerHour();
    }

    public float getTotalCost() {
        return cost.getTotalCost();
    }

    public Set<String> getAllImageNames() {
        return imgManager.getAllImageNames();
    }

    public Set<String> getAllInstanceTypeNames() {
        return typeManager.getAllInstanceTypeNames();
    }

    public int[] getSimultaneousCounts(String type) {
        return typeManager.getSimultaneousTasks(type);
    }

    public long getNextCreationTime() throws Exception {
        return connector.getNextCreationTime();
    }

    public HashMap<ResourceDescription, Integer> getVMComposition(String name) {
        return typeManager.getComposition(name);
    }
    /* -----------------------------------------
     * ------------- State Changes -------------
     * ----------------------------------------*/

    public void stopReached() {
        connector.stopReached();
    }

    public boolean turnON(ResourceCreationRequest rcr) {
        currentVMCount++;
        return connector.turnON("compss" + UUID.randomUUID().toString(), rcr);
    }

    public void createdVM(String resourceName, ResourceDescription rd) {
        typeManager.createdVM(resourceName, rd.getType());
    }

    public void refusedWorker(ResourceDescription rd) {
        currentVMCount--;
    }

    public void performReduction(ResourceDestructionRequest reduction) {
        String vmName = reduction.getRequested().getName();
        String vmType = reduction.getRequested().getType();
        boolean terminate = typeManager.performReduction(vmName, vmType);
        reduction.setTerminate(terminate);
    }

    public void addPendingReduction(ResourceDestructionRequest reduction) {
        String vmName = reduction.getRequested().getName();
        String vmType = reduction.getRequested().getType();
        typeManager.addPendingReduction(vmName, vmType);
    }

    public void confirmReduction(ResourceDestructionRequest reduction) {
        String vmName = reduction.getRequested().getName();
        String vmType = reduction.getRequested().getType();
        boolean terminate = typeManager.confirmReduction(vmName, vmType);
        reduction.setTerminate(terminate);
    }

    public int getCurrentVMCount() {
        return currentVMCount;
    }

    public void terminate(ResourceDestructionRequest rdr) {
        endingVMCount++;
        if (rdr.isTerminate()) {
            typeManager.removeVM(rdr.getRequested().getName());
        }
        connector.terminate(rdr);
    }

    public void terminatedInstance() {
        endingVMCount--;
        currentVMCount--;
    }

    /* ------------------------------------------
     * -------- Recommendation Queries ----------
     * ----------------------------------------*/
    public ResourceDescription getBestIncrease(Integer amount, ResourceDescription constraints, boolean contained) {
        ResourceDescription result = null;
        if (limitOfVMs != null && currentVMCount >= limitOfVMs) {
            return result;
        }

        //Select all the compatible Type with the bigger amount possible
        for (int i = amount; i > 0; i = i / 2) {
            ResourceDescription rd = new ResourceDescription(constraints);
            rd.multiply(i);
            LinkedList<ResourceDescription> instances = typeManager.getCompatibleTypes(constraints);
            if (instances.isEmpty()) {
                continue;
            }
            if (contained) {
                result = selectContainedInstance(instances, constraints, amount);
            } else {
                result = selectContainingInstance(instances, constraints, amount);
            }
            if (result != null) {
                break;
            }
        }

        if (result != null) {
            //Pick an image to be loaded in the Type
            LinkedList<CloudImageDescription> images = imgManager.getCompatibleImages(constraints);
            if (images.isEmpty()) {
                return null;
            }
            result.setImage(images.get(0));
            result.setValue(cost.getMachineCostPerHour(result));
        }
        return result;
    }

    private ResourceDescription selectContainingInstance(LinkedList<ResourceDescription> instances, ResourceDescription constraints, int amount) {
        ResourceDescription result = instances.removeFirst();
        int slots = result.into(constraints);
        float bestDistance = slots - amount;

        for (ResourceDescription rd : instances) {
            slots = rd.into(constraints);
            float distance = slots - amount;
            if (bestDistance < 0) {
                if (distance > bestDistance) {
                    result = rd;
                    bestDistance = distance;
                }
            } else if (bestDistance > 0) {
                if (distance > 0 && distance < bestDistance) {
                    result = rd;
                    bestDistance = distance;
                } else if (bestDistance == distance) {
                    if (result.getValue() != null && rd.getValue() != null && result.getValue() > rd.getValue()) {
                        result = rd;
                        bestDistance = distance;
                    }
                }
            } else {
                if (distance == 0) {
                    if (result.getValue() != null && rd.getValue() != null && result.getValue() > rd.getValue()) {
                        result = rd;
                        bestDistance = distance;
                    }
                }
            }
        }
        if (bestDistance < 0) {
            result = null;
        }
        return result;
    }

    private ResourceDescription selectContainedInstance(LinkedList<ResourceDescription> instances, ResourceDescription constraints, int amount) {
        ResourceDescription result = instances.removeFirst();
        int slots = result.into(constraints);
        float bestDistance = slots - amount;

        for (ResourceDescription rd : instances) {
            slots = rd.into(constraints);
            float distance = slots - amount;

            if (bestDistance < 0) {
                if (distance <= 0 && distance > bestDistance) {
                    result = rd;
                    bestDistance = distance;
                } else if (distance == bestDistance) {
                    if (result.getValue() != null && rd.getValue() != null && result.getValue() > rd.getValue()) {
                        result = rd;
                        bestDistance = distance;
                    }
                }
            } else if (bestDistance > 0) {
                if (distance < bestDistance) {
                    result = rd;
                    bestDistance = distance;
                }
            } else {
                if (distance == 0) {
                    if (result.getValue() != null && rd.getValue() != null && result.getValue() > rd.getValue()) {
                        result = rd;
                        bestDistance = distance;
                    }
                }
            }
        }
        if (bestDistance > 0) { // si estem per sobre de 0
            result = null;
        }
        return result;
    }

    //TypeName -> [[# modified CE that weren't requested, 
    //              #slots removed that weren't requested,
    //              #slots removed that were requested],
    //              Type description]
    public HashMap<String, Object[]> getPossibleReductions(Resource res, float[] recommendedSlots) {
        HashMap<String, Object[]> reductions = new HashMap<String, Object[]>();
        HashMap<String, Object[]> types = typeManager.getPossibleReductions(res.getName());

        for (java.util.Map.Entry<String, Object[]> type : types.entrySet()) {
            String typeName = type.getKey();
            Object[] description = type.getValue();
            int[] reducedSlots = (int[]) description[0];
            float[] values = new float[3];
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                if (recommendedSlots[coreId] < 1 && reducedSlots[coreId] > 0) {
                    values[0]++; // Adding a desired CE whose slots will be destroyed
                    values[1] += reducedSlots[coreId]; //all reduced slots weren't requested
                } else {
                    float dif = (float) reducedSlots[coreId] - recommendedSlots[coreId];
                    if (dif < 0) {
                        values[2] += reducedSlots[coreId];
                    } else {
                        values[2] += recommendedSlots[coreId];
                        values[1] += dif;
                    }
                }

            }
            description[0] = values;
            reductions.put(typeName, description);
        }
        return reductions;
    }

    /* -----------------------------------------
     * ------------- Debug Queries -------------
     * ----------------------------------------*/
    public String getCurrentState(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + "Provider " + name + ":\n");
        sb.append(prefix).append("\tCurrent:").append(currentVMCount).append("\n");
        sb.append(prefix).append("\tEnding VMs:").append(endingVMCount).append("\n");
        sb.append(prefix).append("\tLimit of VMs:").append(limitOfVMs).append("\n");
        sb.append(typeManager.getCurrentState(prefix + "\t"));
        return sb.toString();
    }

    public void terminateAll() {
        this.currentVMCount=0;
        this.endingVMCount=0;
        typeManager.clearAll();
        connector.terminateAll();
    }

}
