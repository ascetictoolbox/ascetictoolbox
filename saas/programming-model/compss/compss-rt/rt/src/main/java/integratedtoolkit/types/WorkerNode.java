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

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Method;

public class WorkerNode extends Resource {

    String name;
    // Resource capabilities
    ResourceDescription description;
    // Available resource capabilities
    ResourceDescription available;
    // Pending removals
    ResourceDescription toRemove;

    public WorkerNode(String name, Integer maxTaskCount, ResourceDescription description) {
        super(maxTaskCount);
        this.name = name;
        this.description = description;
        if (description != null) {
            this.description.setSlots(maxTaskCount);
            this.description.setName(name);
        }
        this.available = new ResourceDescription(description); // clone
        this.toRemove = new ResourceDescription();
        this.toRemove.processorCPUCount = 0;
        this.toRemove.processorCoreCount = 0;
        this.toRemove.memoryPhysicalSize = 0;
        this.toRemove.memoryVirtualSize = 0;
        this.toRemove.storageElemSize = 0;
    }

    public void setDescription(ResourceDescription description) {
        this.description = description;
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public void setAvailable(ResourceDescription available) {
        this.available = available;
    }

    public ResourceDescription getAvailable() {
        return available;
    }

    public void setToRemove(ResourceDescription toRemove) {
        this.toRemove = toRemove;
    }

    public ResourceDescription getToRemove() {
        return toRemove;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    boolean checkResource(ResourceDescription consumption) {
        return consumption.getProcessorCoreCount() <= (available.getProcessorCoreCount() - toRemove.getProcessorCoreCount())
                && consumption.getMemoryPhysicalSize() <= (available.getMemoryPhysicalSize() - toRemove.getMemoryPhysicalSize());
    }

    @Override
    void reserveResource(ResourceDescription consumption) {
        available.setProcessorCoreCount(available.getProcessorCoreCount() - consumption.getProcessorCoreCount());
        available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() - consumption.getMemoryPhysicalSize());
    }

    @Override
    void releaseResource(ResourceDescription consumption) {
        available.setProcessorCoreCount(available.getProcessorCoreCount() + consumption.getProcessorCoreCount());
        available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() + consumption.getMemoryPhysicalSize());
    }

    @Override
    Integer fitCount(Implementation impl) {
        if (impl.getType() == Implementation.Type.SERVICE) {
            return Integer.MAX_VALUE;
        }
        return this.description.into(((Method) impl).getResource());
    }

    @Override
    public Type getType() {
        return Type.WORKER;
    }

    @Override
    public String getMonitoringData(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("<CPU>").append(description.getProcessorCoreCount()).append("</CPU>\n");
        sb.append(prefix).append("<Memory>").append(description.getMemoryPhysicalSize()).append("</Memory>\n");
        sb.append(prefix).append("<Disk>").append(description.getStorageElemSize()).append("</Disk>\n");
        return sb.toString();

    }

    @Override
    public int compareTo(Resource t) {
        if (t == null) {
            throw new NullPointerException();
        }
        switch (t.getType()) {
            case SERVICE:
                return 1;
            case WORKER:
                WorkerNode w = (WorkerNode) t;
                if (description.getValue() == null) {
                    if (w.description.getValue() == null) {
                        return w.getName().compareTo(getName());
                    }
                    return 1;
                }
                if (w.description.getValue() == null) {
                    return -1;
                }
                float dif = w.description.getValue() - this.description.getValue();
                if (dif > 0) {
                    return -1;
                }
                if (dif < 0) {
                    return 1;
                }
                return getName().compareTo(w.getName());
            default:
                return getName().compareTo(t.getName());
        }
    }

    @Override
    public boolean canRun(Implementation implementation) {
        switch (implementation.getType()) {
            case METHOD:
                ResourceDescription constraints = implementation.getResource();
                return description.contains(constraints);
            default:
                return false;
        }
    }

    @Override
    public void update(ResourceDescription resDesc) {
        description.processorCPUCount += resDesc.processorCPUCount;
        description.processorCoreCount += resDesc.processorCoreCount;
        description.memoryPhysicalSize += resDesc.memoryPhysicalSize;
        description.memoryVirtualSize += resDesc.memoryVirtualSize;
        description.storageElemSize += resDesc.storageElemSize;

        available.processorCPUCount += resDesc.processorCPUCount;
        available.processorCoreCount += resDesc.processorCoreCount;
        available.memoryPhysicalSize += resDesc.memoryPhysicalSize;
        available.memoryVirtualSize += resDesc.memoryVirtualSize;
        available.storageElemSize += resDesc.storageElemSize;

    }

    @Override
    public boolean markToRemove(ResourceDescription resDesc) {
        if (isAvailable(resDesc)) {
            description.processorCPUCount -= resDesc.processorCPUCount;
            description.processorCoreCount -= resDesc.processorCoreCount;
            description.memoryPhysicalSize -= resDesc.memoryPhysicalSize;
            description.memoryVirtualSize -= resDesc.memoryVirtualSize;
            description.storageElemSize -= resDesc.storageElemSize;

            available.processorCPUCount -= resDesc.processorCPUCount;
            available.processorCoreCount -= resDesc.processorCoreCount;
            available.memoryPhysicalSize -= resDesc.memoryPhysicalSize;
            available.memoryVirtualSize -= resDesc.memoryVirtualSize;
            available.storageElemSize -= resDesc.storageElemSize;
            return true;
        } else {
            toRemove.processorCPUCount += resDesc.processorCPUCount;
            toRemove.processorCoreCount += resDesc.processorCoreCount;
            toRemove.memoryPhysicalSize += resDesc.memoryPhysicalSize;
            toRemove.memoryVirtualSize += resDesc.memoryVirtualSize;
            toRemove.storageElemSize += resDesc.storageElemSize;
            return false;
        }
    }

    @Override
    public boolean isAvailable(ResourceDescription rd) {
        return available.processorCPUCount >= rd.processorCPUCount
                && available.processorCoreCount >= rd.processorCoreCount
                && available.memoryPhysicalSize >= rd.memoryPhysicalSize
                && available.memoryVirtualSize >= rd.memoryVirtualSize
                && available.storageElemSize >= rd.storageElemSize;
    }

    @Override
    public void confirmRemoval(ResourceDescription modification) {
        description.processorCPUCount -= modification.processorCPUCount;
        description.processorCoreCount -= modification.processorCoreCount;
        description.memoryPhysicalSize -= modification.memoryPhysicalSize;
        description.memoryVirtualSize -= modification.memoryVirtualSize;
        description.storageElemSize -= modification.storageElemSize;

        available.processorCPUCount -= modification.processorCPUCount;
        available.processorCoreCount -= modification.processorCoreCount;
        available.memoryPhysicalSize -= modification.memoryPhysicalSize;
        available.memoryVirtualSize -= modification.memoryVirtualSize;
        available.storageElemSize -= modification.storageElemSize;

        toRemove.processorCPUCount -= modification.processorCPUCount;
        toRemove.processorCoreCount -= modification.processorCoreCount;
        toRemove.memoryPhysicalSize -= modification.memoryPhysicalSize;
        toRemove.memoryVirtualSize -= modification.memoryVirtualSize;
        toRemove.storageElemSize -= modification.storageElemSize;
    }

}
