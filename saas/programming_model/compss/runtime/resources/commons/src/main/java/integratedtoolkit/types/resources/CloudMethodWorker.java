package integratedtoolkit.types.resources;

import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class CloudMethodWorker extends MethodWorker {

    // Pending removals
    private final LinkedList<PendingReduction> pendingReductions;
    private final CloudMethodResourceDescription toRemove;

    public CloudMethodWorker(CloudMethodResourceDescription description, COMPSsWorker worker, Integer maxTaskCount) {
        super(description.getName(), description, worker, maxTaskCount);
        this.toRemove = new CloudMethodResourceDescription();
        this.pendingReductions = new LinkedList<PendingReduction>();
    }

    public CloudMethodWorker(String name, CloudMethodResourceDescription description, String adaptor, HashMap<String, String> properties, Integer maxTaskCount) throws Exception {
        
    	super(name, description, adaptor, properties, maxTaskCount);
        if (description != null) {
            this.description.setSlots(maxTaskCount);
            ((CloudMethodResourceDescription)this.description).setName(name);
        }
        this.toRemove = new CloudMethodResourceDescription();
        this.pendingReductions = new LinkedList<PendingReduction>();
    }

    @Override
    public Type getType() {
        return Type.WORKER;
    }

    @Override
    public String getMonitoringData(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("<CPU>").append(description.getProcessorCPUCount()).append("</CPU>").append("\n");
        sb.append(prefix).append("<Core>").append(description.getProcessorCoreCount()).append("</Core>").append("\n");
        sb.append(prefix).append("<Memory>").append(description.getMemoryPhysicalSize()).append("</Memory>").append("\n");
        sb.append(prefix).append("<Disk>").append(description.getStorageElemSize()).append("</Disk>").append("\n");
        String providerName = ((CloudMethodResourceDescription)description).getProviderName();
        if (providerName == null) {
            providerName = new String("");
        }
        sb.append(prefix).append("<Provider>").append(providerName).append("</Provider>").append("\n");
        CloudImageDescription image = ((CloudMethodResourceDescription)description).getImage();
        String imageName = new String("");
        if (image != null) {
            if (image.getName() != null) {
                imageName = image.getName();
            }
        }
        sb.append(prefix).append("<Image>").append(imageName).append("</Image>").append("\n");
        return sb.toString();
    }

    public synchronized void increaseFeatures(CloudMethodResourceDescription increment) {
        available.setProcessorCoreCount(available.getProcessorCoreCount() + increment.getProcessorCoreCount());
        available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() + increment.getMemoryPhysicalSize());
        description.setProcessorCoreCount(description.getProcessorCoreCount() + increment.getProcessorCoreCount());
        description.setMemoryPhysicalSize(description.getMemoryPhysicalSize() + increment.getMemoryPhysicalSize());
        updatedFeatures();
    }

    public synchronized boolean reserveResource(CloudMethodResourceDescription consumption) {
        if (hasAvailable(consumption)) {
            return false;
        }
        super.reserveResource(consumption);
        return true;
    }

    public synchronized void releaseResource(CloudMethodResourceDescription consumption) {
        super.releaseResource(consumption);
        if (!pendingReductions.isEmpty()) {
            for (PendingReduction pRed : pendingReductions) {
                if (hasAvailable(pRed.reduction)) {
                    toRemove.setProcessorCoreCount(available.getProcessorCoreCount() - pRed.reduction.getProcessorCoreCount());
                    toRemove.setMemoryPhysicalSize(available.getMemoryPhysicalSize() - pRed.reduction.getMemoryPhysicalSize());
                    pRed.sem.release();
                } else {
                    break;
                }
            }
        }
    }

    public synchronized Semaphore reduceFeatures(CloudMethodResourceDescription reduction) {
        description.setProcessorCoreCount(description.getProcessorCoreCount() - reduction.getProcessorCoreCount());
        description.setMemoryPhysicalSize(description.getMemoryPhysicalSize() - reduction.getMemoryPhysicalSize());
        Semaphore sem = null;
        if (hasAvailable(reduction)) {
            available.setProcessorCoreCount(available.getProcessorCoreCount() - reduction.getProcessorCoreCount());
            available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() - reduction.getMemoryPhysicalSize());
        } else {
            PendingReduction pRed = new PendingReduction(reduction);
            pendingReductions.add(pRed);
            sem = pRed.sem;
            toRemove.setProcessorCoreCount(toRemove.getProcessorCoreCount() + reduction.getProcessorCoreCount());
            toRemove.setMemoryPhysicalSize(toRemove.getMemoryPhysicalSize() + reduction.getMemoryPhysicalSize());
        }
        updatedFeatures();
        return sem;
    }

    @Override
    public boolean hasAvailable(MethodResourceDescription consumption) {
        return !(available.getProcessorCoreCount() < consumption.getProcessorCoreCount() - toRemove.getProcessorCoreCount()
                || available.getMemoryPhysicalSize() < consumption.getMemoryPhysicalSize() - toRemove.getMemoryPhysicalSize());
    }

    @Override
    public String getResourceLinks(String prefix) {
        StringBuilder sb = new StringBuilder(super.getResourceLinks(prefix));
        sb.append(prefix).append("TYPE = WORKER").append("\n");
        sb.append(prefix).append("CPU = ").append(description.getProcessorCPUCount()).append("\n");
        sb.append(prefix).append("MEMORY = ").append(description.getMemoryPhysicalSize()).append("\n");

        return sb.toString();
    }

    public boolean shouldBeStopped() {
        return ((available.getProcessorCoreCount() == 0) && (toRemove.getProcessorCoreCount() == 0));
    }

    private class PendingReduction {

        CloudMethodResourceDescription reduction;
        Semaphore sem;

        private PendingReduction(CloudMethodResourceDescription reduction) {
            this.reduction = reduction;
            this.sem = new Semaphore(0);
        }
    }
}
