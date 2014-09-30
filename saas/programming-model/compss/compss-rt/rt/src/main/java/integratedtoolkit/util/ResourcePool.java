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
package integratedtoolkit.util;

import integratedtoolkit.types.Resource;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.types.ServiceInstance;
import integratedtoolkit.types.WorkerNode;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class ResourcePool {

    //Resource Sets:
    //  Physical Resources (read from xml)
    private HashMap<String, Resource> physicalSet;
    //  Critical Resources (can't be destroyed by periodical resource policy)
    private HashMap<String, Resource> criticalSet;
    //  Non Critical Resources (can be destroyed by periodical resource policy)
    private HashMap<String, Resource> nonCriticalSet;
    //Map: coreId -> List <names of the resources where core suits>
    private LinkedList<Resource>[] coreToResource;
    //Map: coreId -> maxTaskCount accepted for that core
    private int[] coreMaxTaskCount;
    //TreeSet : Priority on criticalSet based on cost
    private TreeSet<Resource> criticalOrder;

    //public static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(integratedtoolkit.log.Loggers.RESOURCES);
    public ResourcePool(int coreCount) {
        physicalSet = new HashMap<String, Resource>();
        criticalSet = new HashMap<String, Resource>();
        nonCriticalSet = new HashMap<String, Resource>();
        coreToResource = new LinkedList[coreCount];
        coreMaxTaskCount = new int[coreCount];
        for (int i = 0; i < coreCount; i++) {
            coreToResource[i] = new LinkedList<Resource>();
            coreMaxTaskCount[i] = 0;
        }
        criticalOrder = new TreeSet<Resource>();
    }

    //Adds a new Resource on the Physical list
    public Resource addPhysical(String resourceName, int maxTaskCount, ResourceDescription description) {
        Resource newResource = new WorkerNode(resourceName, maxTaskCount, description);
        physicalSet.put(resourceName, newResource);
        return newResource;
    }

    public Resource addPhysical(String wsdl, String serviceName, String namespace, String portName, int maxTaskCount) {
        ServiceInstance newResource = new ServiceInstance(wsdl, serviceName, namespace, portName, maxTaskCount);
        physicalSet.put(wsdl, newResource);
        return newResource;
    }

    public Resource getPhysicalResource(String resourceName) {
        return physicalSet.get(resourceName);
    }

    //Adds a new Resource on the Critical list
    public Resource addCritical(String resourceName, int maxTaskCount, ResourceDescription description) {
        Resource newResource = new WorkerNode(resourceName, maxTaskCount, description);
        criticalSet.put(resourceName, newResource);
        criticalOrder.add(newResource);
        return newResource;
    }

    public Resource getDynamicResource(String resourceName) {
        Resource resource = null;
        resource = criticalSet.get(resourceName);
        if (resource == null) {
            resource = nonCriticalSet.get(resourceName);
        }
        return resource;
    }

    public LinkedList<Resource> getDynamicResources() {
        LinkedList<Resource> resources = new LinkedList<Resource>();
        resources.addAll(criticalSet.values());
        resources.addAll(nonCriticalSet.values());
        return resources;
    }

    //returns all the resource information
    public Resource getResource(String resourceName) {
        Resource resource = null;
        resource = physicalSet.get(resourceName);
        if (resource == null) {
            resource = criticalSet.get(resourceName);
        }
        if (resource == null) {
            resource = nonCriticalSet.get(resourceName);
        }
        return resource;
    }

    public Resource upgradeResource(String resName, ResourceDescription resDesc, Integer limitOfTasks) {
        Resource resource = getResource(resName);
        if (resource == null) {
            return null;
        }
        resource.update(resDesc);

        return resource;
    }

    public void setResourceCoreLinks(Resource res, int[] idealMaxTaskCount) {
        int[] oldSimTasks = new int[CoreManager.coreCount];
        System.arraycopy(res.getSimultaneousTasks(), 0, oldSimTasks, 0, CoreManager.coreCount);
        res.setResourceCoreLinks(idealMaxTaskCount);
        int[] newSimTasks = res.getSimultaneousTasks();
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            if (newSimTasks[coreId] > 0 && oldSimTasks[coreId] == 0) {
                LinkedList<Resource> resources = coreToResource[coreId];
                if (resources == null) {
                    resources = new LinkedList<Resource>();
                    coreToResource[coreId] = resources;
                }
                coreToResource[coreId].add(res);
            }
            coreMaxTaskCount[coreId] -= oldSimTasks[coreId] - newSimTasks[coreId];
        }
    }

    public boolean markResourcesToRemove(Resource r, ResourceDescription rd, int[] simTasks) {
        int[] oldSimTasks = new int[CoreManager.coreCount];
        System.arraycopy(r.getSimultaneousTasks(), 0, oldSimTasks, 0, CoreManager.coreCount);
        int[] idealMaxTaskCount = r.getIdealSimultaneousTasks();
        for (Integer coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            idealMaxTaskCount[coreId] = idealMaxTaskCount[coreId] - simTasks[coreId];
        }
        r.setResourceCoreLinks(idealMaxTaskCount);
        int[] newSimTasks = r.getSimultaneousTasks();
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            if (newSimTasks[coreId] == 0) {
                coreToResource[coreId].remove(r);
            }
            coreMaxTaskCount[coreId] -= oldSimTasks[coreId] - newSimTasks[coreId];
        }
        return r.markToRemove(rd);
    }

    public void confirmPendingReduction(Resource res, ResourceDescription modification) {
        res.confirmRemoval(modification);
    }

    //Deletes a resource from the pool
    public void delete(Resource resource) {
        String resourceName = resource.getName();
        //Remove resource from sets
        if (criticalSet.remove(resourceName) == null) {
            nonCriticalSet.remove(resourceName);
        }

        // Remove core links
        int[] simTasks = resource.getSimultaneousTasks();
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            if (simTasks[coreId] > 0) {
                coreToResource[coreId].remove(resource);
                coreMaxTaskCount[coreId] -= simTasks[coreId];
            }
        }
    }

    public int[] getCoreMaxTaskCount() {
        return coreMaxTaskCount;
    }

    //Returns a list with all coreIds that can be executed by the resource res
    public List<Integer> getExecutableCores(String res) {
        Resource resource = getResource(res);
        if (resource == null) {
            return new LinkedList<Integer>();
        }
        return resource.getExecutableCores();
    }

    //Selects a subset of the critical set able to execute all the cores
    public void defineCriticalSet() {
        boolean[] runnable = new boolean[coreToResource.length];
        for (int i = 0; i < coreToResource.length; i++) {
            runnable[i] = false;
        }

        Object[] physicalResourcesNames = physicalSet.keySet().toArray();
        String resourceName;
        for (int physicalResourceIndex = 0; physicalResourceIndex < physicalResourcesNames.length; physicalResourceIndex++) {
            resourceName = (String) physicalResourcesNames[physicalResourceIndex];
            Resource res = physicalSet.get(resourceName);
            for (int i = 0; i < res.getExecutableCores().size(); i++) {
                runnable[res.getExecutableCores().get(i)] = true;
            }
        }
        HashMap<String, Resource> toDelete = new HashMap<String, Resource>();
        for (Resource resource : criticalOrder) {
            resourceName = resource.getName();
            boolean needed = false;
            for (int i = 0; i < resource.getExecutableCores().size() && !needed; i++) {
                needed = needed || !runnable[resource.getExecutableCores().get(i)];
            }
            if (needed) {
                for (int i = 0; i < resource.getExecutableCores().size(); i++) {
                    runnable[resource.getExecutableCores().get(i)] = true;
                }
            } else {
                toDelete.put(resourceName, resource);
            }
        }

        for (java.util.Map.Entry<String, Resource> entry : toDelete.entrySet()) {
            resourceName = entry.getKey();
            Resource resource = entry.getValue();
            criticalSet.remove(resourceName);
            criticalOrder.remove(resource);
            nonCriticalSet.put(resourceName, resource);
        }
    }

    public void freeCapabilities(String resourceName, ResourceDescription capabilities) {
        Resource r = getResource(resourceName);
        r.endTask(capabilities);
    }

    public void reserveCapabilities(String resourceName, ResourceDescription capabilities) {
        Resource r = getResource(resourceName);
        r.runTask(capabilities);
    }

    public Set<Resource> getNonCriticalResources(float[] destroyRecommendations, boolean aggressive) {

        HashSet<Resource> resources = new HashSet<Resource>();
        for (Resource resource : nonCriticalSet.values()) {
            boolean add = !aggressive;
            for (int coreId : resource.getExecutableCores()) {
                if (!aggressive && destroyRecommendations[coreId] < 1) {
                    add = false;
                    break;
                }
                if (aggressive && destroyRecommendations[coreId] > 0) {
                    add = true;
                    break;
                }
            }
            if (add) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public Set<Resource> getCriticalResources(float[] destroyRecommendations, boolean aggressive) {
        HashSet<Resource> resources = new HashSet<Resource>();
        for (Resource resource : criticalSet.values()) {
            boolean add = !aggressive;
            for (int coreId : resource.getExecutableCores()) {
                if (!aggressive && destroyRecommendations[coreId] < 1) {
                    add = false;
                    break;
                }
                if (aggressive && destroyRecommendations[coreId] > 0) {
                    add = true;
                    break;
                }
            }
            if (add) {
                resources.add(resource);
            }
        }
        return resources;
    }

    //Returns the name of all the resources able to execute coreId
    public List<Resource> findAllResources() {
        LinkedList<Resource> resources = new LinkedList<Resource>();
        if (physicalSet != null && !physicalSet.isEmpty()) {
            for (int i = 0; i < physicalSet.size(); i++) {
                resources.add(((Resource) physicalSet.values().toArray()[i]));
            }
        }
        if (criticalSet != null && !criticalSet.isEmpty()) {
            for (int i = 0; i < criticalSet.size(); i++) {
                resources.add(((Resource) criticalSet.values().toArray()[i]));
            }
        }
        if (nonCriticalSet != null && !nonCriticalSet.isEmpty()) {
            for (int i = 0; i < nonCriticalSet.size(); i++) {
                resources.add(((Resource) nonCriticalSet.values().toArray()[i]));
            }
        }
        return resources;
    }

    //return all the resources able to execute a task of the core
    public List<Resource> findCompatibleResources(int coreId) {
        return coreToResource[coreId];
    }

    //Checks if resourcename can execute coreId
    public boolean matches(String resourceName, int coreId) {
        Resource resource = getResource(resourceName);
        if (resource == null) {
            return false;
        }
        boolean exists = false;
        for (int i = 0; i < resource.getExecutableCores().size() && !exists; i++) {
            exists |= resource.getExecutableCores().get(i) == coreId;
        }
        return exists;
    }

    //Returns a critical machine able to execute the core
    public String getSafeResource(int coreId) {
        LinkedList<Resource> resources = coreToResource[coreId];
        String ret = "";
        for (Resource r : resources) {
            //Recurs a poder ser físic
            if (criticalSet.containsKey(r.getName())) {
                return r.getName();
            }
            //Sinó crític
            if (physicalSet.containsKey(r.getName())) {
                ret = r.getName();
            }
        }
        return ret;
    }

    public void newCoreElementsDetected(LinkedList<Integer> newCores) {
        LinkedList[] coreToResourceTmp = new LinkedList[CoreManager.coreCount];
        System.arraycopy(coreToResource, 0, coreToResourceTmp, 0, coreToResource.length);
        for (int i = coreToResource.length; i < CoreManager.coreCount; i++) {
            coreToResourceTmp[i] = new LinkedList<String>();
        }
        coreToResource = coreToResourceTmp;

        int[] coreMaxTaskCountTmp = new int[CoreManager.coreCount];
        System.arraycopy(coreMaxTaskCount, 0, coreMaxTaskCountTmp, 0, coreMaxTaskCount.length);
        for (int i = coreMaxTaskCount.length; i < CoreManager.coreCount; i++) {
            coreMaxTaskCountTmp[i] = 0;
        }
        coreMaxTaskCount = coreMaxTaskCountTmp;

        for (Resource r : physicalSet.values()) {
            int[] slots = r.newCoreElementsDetected(newCores);
            for (Integer coreId : newCores) {
                if (slots[coreId] > 0) {
                    coreToResource[coreId].add(r);
                    coreMaxTaskCount[coreId] += slots[coreId];
                }
            }
        }
        for (Resource r : criticalSet.values()) {
            int[] slots = r.newCoreElementsDetected(newCores);
            for (Integer coreId : newCores) {
                if (slots[coreId] > 0) {
                    coreToResource[coreId].add(r);
                    coreMaxTaskCount[coreId] += slots[coreId];
                }
            }
        }
        for (Resource r : nonCriticalSet.values()) {
            int slots[] = r.newCoreElementsDetected(newCores);
            for (Integer coreId : newCores) {
                if (slots[coreId] > 0) {
                    coreToResource[coreId].add(r);
                    coreMaxTaskCount[coreId] += slots[coreId];
                }
            }
        }
    }

    public boolean isCriticalRemovalSafe(int[] slotReduction) {
        int[] slots = new int[CoreManager.coreCount];
        for (Resource r : criticalSet.values()) {
            int[] resSlots = r.getSimultaneousTasks();
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                slots[coreId] += resSlots[coreId];
            }
        }
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            if (slotReduction[coreId] > 0 && slotReduction[coreId] >= slots[coreId]) {
                return false;
            }
        }
        return true;
    }

    //DEBUG FUNCTIONS
    public String getCurrentState(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Resources:").append("\n");
        sb.append(prefix).append("\tPhysical Set:").append("\n");
        for (Resource r : physicalSet.values()) {
            sb.append(r.getResourceLinks(prefix + "\t\t"));
        }
        sb.append(prefix).append("\tCritical Set:").append("\n");
        for (Resource r : criticalSet.values()) {
            sb.append(r.getResourceLinks(prefix + "\t\t"));
        }
        sb.append(prefix).append("\tnon-Critical Set:").append("\n");
        for (Resource r : nonCriticalSet.values()) {
            sb.append(r.getResourceLinks(prefix + "\t\t"));
        }
        sb.append(prefix).append("Cores:").append("\n");
        for (int i = 0; i < CoreManager.coreCount; i++) {
            sb.append(prefix).append("\tCore ").append(i).append(" (").append(coreMaxTaskCount[i]).append("):");
            for (Resource r : coreToResource[i]) {
                sb.append(r.getName()).append(" (").append(r.getSimultaneousTasks()[i]).append("),");
            }
            sb.append("\n");

        }
        return sb.toString();
    }
}
