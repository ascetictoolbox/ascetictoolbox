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

import integratedtoolkit.types.ResourceDescription;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class CloudTypeManager {

    /**
     * Relation between the name of an template and its features
     */
    private HashMap<String, Type> types;

    /**
     * Relation between VM and their composing types [created, to be remove]
     */
    private HashMap<String, HashMap<String, int[]>> vmToType;

    /**
     * Constructs a new CloudImageManager
     */
    public CloudTypeManager() {
        types = new HashMap<String, Type>();
        vmToType = new HashMap<String, HashMap<String, int[]>>();
    }

    /**
     * Adds a new instance type which can be used by the Cloud Provider
     *
     * @param rd Description of the resource
     */
    public void add(ResourceDescription rd) {
        types.put(rd.getName(), new Type(rd));
    }

    /**
     * Finds all the types provided by the Cloud Provider which fulfill the
     * resource description.
     *
     * @param requested description of the features that the image must provide
     * @return The best instance type provided by the Cloud Provider which
     * fulfills the resource description
     */
    public LinkedList<ResourceDescription> getCompatibleTypes(ResourceDescription requested) {

        LinkedList<ResourceDescription> compatiblesList = new LinkedList<ResourceDescription>();
        if (types.isEmpty()) {
            compatiblesList.add(requested);
        }
        for (Type type : types.values()) {
            ResourceDescription rd = type.rd;
            ResourceDescription mixedDescription = new ResourceDescription(rd);
            // TODO: CHECK  constraints
            if (mixedDescription.getProcessorArchitecture().compareTo("[unassigned]") == 0) {
                mixedDescription.setProcessorArchitecture(requested.getProcessorArchitecture());
            } else if ((requested.getProcessorArchitecture().compareTo("[unassigned]") != 0)
                    && requested.getProcessorArchitecture().compareTo(mixedDescription.getProcessorArchitecture()) != 0) {
                continue;
            }

            if (mixedDescription.getProcessorCPUCount() == 0) {
                mixedDescription.setProcessorCPUCount(requested.getProcessorCoreCount());
            } else if (requested.getProcessorCPUCount() > 0
                    && requested.getProcessorCPUCount() > mixedDescription.getProcessorCoreCount()) {
                continue;
            }

            if (mixedDescription.getProcessorCoreCount() == 0) {
                mixedDescription.setProcessorCoreCount(requested.getProcessorCoreCount());
            } else if (requested.getProcessorCoreCount() > 0
                    && requested.getProcessorCoreCount() > mixedDescription.getProcessorCoreCount()) {
                continue;
            }

            if (mixedDescription.getMemoryPhysicalSize() == 0.0f) {
                mixedDescription.setMemoryPhysicalSize(requested.getMemoryPhysicalSize());
            } else if (requested.getMemoryPhysicalSize() > 0.0f
                    && requested.getMemoryPhysicalSize() > mixedDescription.getMemoryPhysicalSize()) {
                continue;
            }

            if (mixedDescription.getMemoryVirtualSize() == 0.0f) {
                mixedDescription.setMemoryVirtualSize(requested.getMemoryVirtualSize());
            } else if (requested.getMemoryVirtualSize() > 0.0f
                    && requested.getMemoryVirtualSize() > mixedDescription.getMemoryVirtualSize()) {
                continue;
            }

            if (mixedDescription.getStorageElemSize() == 0.0f) {
                mixedDescription.setStorageElemSize(requested.getStorageElemSize());
            } else if (requested.getStorageElemSize() > 0.0f
                    && requested.getStorageElemSize() > mixedDescription.getStorageElemSize()) {
                continue;
            }

            if (mixedDescription.getProcessorSpeed() == 0.0f) {
                mixedDescription.setProcessorSpeed(requested.getProcessorSpeed());
            } else if (requested.getProcessorSpeed() > 0.0f
                    && requested.getProcessorSpeed() > mixedDescription.getProcessorSpeed()) {
                continue;
            }

            if (mixedDescription.getMemorySTR() == 0.0f) {
                mixedDescription.setMemorySTR(requested.getMemorySTR());
            } else if (requested.getMemorySTR() > 0.0f
                    && requested.getMemorySTR() > mixedDescription.getMemorySTR()) {
                continue;
            }

            if (mixedDescription.getMemoryAccessTime() == 0.0f) {
                mixedDescription.setMemoryAccessTime(requested.getMemoryAccessTime());
            } else if (requested.getMemoryAccessTime() > 0.0f
                    && requested.getMemoryAccessTime() > mixedDescription.getMemoryAccessTime()) {
                continue;
            }

            if (mixedDescription.getStorageElemAccessTime() == 0.0f) {
                mixedDescription.setStorageElemAccessTime(requested.getStorageElemAccessTime());
            } else if (requested.getStorageElemAccessTime() > 0.0f
                    && requested.getStorageElemAccessTime() > mixedDescription.getStorageElemAccessTime()) {
                continue;
            }

            if (mixedDescription.getStorageElemSTR() == 0.0f) {
                mixedDescription.setStorageElemSTR(requested.getStorageElemSTR());
            } else if (requested.getStorageElemSTR() > 0.0f
                    && requested.getStorageElemSTR() > mixedDescription.getStorageElemSTR()) {
                continue;
            }
            compatiblesList.add(mixedDescription);
        }
        return compatiblesList;
    }

    /**
     * Return all the image names offered by that Cloud Provider
     *
     * @return set of image names offered by that Cloud Provider
     */
    public Set<String> getAllInstanceTypeNames() {
        return types.keySet();
    }

    public void createdVM(String resourceName, String requestType) {
        HashMap<String, int[]> vm = vmToType.get(resourceName);
        if (vm == null) {
            vm = new HashMap<String, int[]>();
            for (String type : types.keySet()) {
                vm.put(type, new int[]{0, 0});
            }
            vm.put(requestType, new int[]{1, 0});
            vmToType.put(resourceName, vm);
        } else {
            vm.get(requestType)[0]++;
        }
    }

    public void clearAll() {
        vmToType.clear();
    }

    public ResourceDescription getDescription(String type) {
        Type t = types.get(type);
        if (t != null) {
            return t.rd;
        }
        return null;
    }

    public int[] getSimultaneousTasks(String type) {
        Type t = types.get(type);
        if (t != null) {
            return t.slots;
        }
        return null;
    }

    //typeName->[(int[] slots that will be removed, (ResourceDescription) description of the resource that will be destroyed]
    public HashMap<String, Object[]> getPossibleReductions(String name) {
        HashMap<String, Object[]> h = new HashMap<String, Object[]>();
        for (java.util.Map.Entry<String, int[]> entry : vmToType.get(name).entrySet()) {
            String type = entry.getKey();
            Object[] value = new Object[2];
            int[] amount = entry.getValue();
            if (amount != null && (amount[0] - amount[1]) > 0) {
                Type t = types.get(type);
                if (t != null) {
                    value[0] = t.slots;
                    value[1] = t.rd;
                    h.put(type, value);
                }
            }

        }
        return h;
    }

    public boolean performReduction(String name, String typeName) {
        HashMap<String, int[]> vm = vmToType.get(name);
        int[] type = vm.get(typeName);
        type[0]--;
        return hasValidInstances(vm);
    }

    public void addPendingReduction(String name, String typeName) {
        HashMap<String, int[]> vm = vmToType.get(name);
        int[] type = vm.get(typeName);
        type[1]++;
    }

    public boolean confirmReduction(String name, String typeName) {
        HashMap<String, int[]> vm = vmToType.get(name);
        int[] type = vm.get(typeName);
        type[0]--;
        type[1]--;
        return hasValidInstances(vm);
    }

    public void removeVM(String name) {
        vmToType.remove(name);
    }

    private boolean hasValidInstances(HashMap<String, int[]> vm) {
        int validCount = 0;
        for (int[] amounts : vm.values()) {
            validCount += amounts[0];
            validCount -= amounts[1];
        }
        return validCount == 0;
    }

    public HashMap<ResourceDescription, Integer> getComposition(String name) {
        HashMap<String, int[]> vm = vmToType.get(name);
        HashMap<ResourceDescription, Integer> composition = new HashMap<ResourceDescription, Integer>();
        for (java.util.Map.Entry<String, int[]> entry : vm.entrySet()) {
            String typeName = entry.getKey();
            int[] counts = entry.getValue();
            Type type = types.get(typeName);
            if (type != null && counts[0] > 0) {
                composition.put(type.rd, counts[0]);
            }
        }
        return composition;
    }

    public void newCoreElementsDetected(LinkedList<Integer> newCores) {
        for (Type type : types.values()) {
            int[] slots = new int[CoreManager.coreCount];
            System.arraycopy(type.slots, 0, slots, 0, type.slots.length);
            for (int coreId : newCores) {
                ResourceDescription[] descriptions = CoreManager.getResourceConstraints(coreId);
                for (ResourceDescription description : descriptions) {
                    if (description != null) {
                        Integer into = type.rd.into(description);
                        if (into != null) {
                            slots[coreId] = Math.max(slots[coreId], into);
                        }
                    }
                }
            }
            type.slots = slots;
        }
    }

    public String getCurrentState(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + "Types:\n");
        for (java.util.Map.Entry<String, Type> type : types.entrySet()) {
            sb.append(prefix).append("\t").append(type.getKey()).append(": [");
            for (int i = 0; i < CoreManager.coreCount; i++) {
                sb.append(type.getValue().slots[i]).append(", ");
            }
            sb.append("]\n");
        }
        sb.append(prefix).append("Virtual Instances:\n");
        for (java.util.Map.Entry<String, HashMap<String, int[]>> vm : vmToType.entrySet()) {
            String vmName = vm.getKey();
            HashMap<String, int[]> composition = vm.getValue();
            sb.append(prefix).append("\t").append(vmName).append(":").append("\n");
            for (java.util.Map.Entry<String, int[]> component : composition.entrySet()) {
                String componentName = component.getKey();
                int[] amount = component.getValue();
                sb.append(prefix).append("\t\t").append(componentName).append(":").append(amount[0]).append("/").append(amount[1]).append(" pending for destruction").append("\n");
            }
        }
        return sb.toString();
    }

    private class Type {

        ResourceDescription rd;
        int[] slots;

        Type(ResourceDescription rd) {
            slots = new int[CoreManager.coreCount];
            this.rd = rd;
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                ResourceDescription[] descriptions = CoreManager.getResourceConstraints(coreId);
                for (ResourceDescription description : descriptions) {
                    if (description != null) {
                        Integer into = rd.into(description);
                        if (into != null) {
                            slots[coreId] = Math.max(slots[coreId], into);
                        }
                    }
                }
            }
        }
    }
}
