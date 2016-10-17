package integratedtoolkit.types.resources;

import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.configuration.MethodConfiguration;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.types.resources.updates.PendingReduction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class CloudMethodWorker extends MethodWorker {

    // Pending removals
    private final LinkedList<PendingReduction<MethodResourceDescription>> pendingReductions;
    private final CloudMethodResourceDescription toRemove;

    public CloudMethodWorker(CloudMethodResourceDescription description, COMPSsWorker worker, int limitOfTasks, HashMap<String, String> sharedDisks) {
        super(description.getName(), description, worker, limitOfTasks, sharedDisks);
        this.toRemove = new CloudMethodResourceDescription();
        this.pendingReductions = new LinkedList<PendingReduction< MethodResourceDescription>>();
    }

    public CloudMethodWorker(String name, CloudMethodResourceDescription description, MethodConfiguration config, HashMap<String, String> sharedDisks) {
        super(name, description, config, sharedDisks);

        if (this.description != null) {
            // Add name
            ((CloudMethodResourceDescription) this.description).setName(name);
        }

        this.toRemove = new CloudMethodResourceDescription();
        this.pendingReductions = new LinkedList<PendingReduction< MethodResourceDescription>>();
    }

    public CloudMethodWorker(CloudMethodWorker cmw) {
        super(cmw);
        this.toRemove = cmw.toRemove.copy();
        this.pendingReductions = cmw.pendingReductions;
    }

    @Override
    public Type getType() {
        return Type.WORKER;
    }

    @Override
    public String getMonitoringData(String prefix) {
        // TODO: Add full information about description (mem type, each processor information, etc)
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("<TotalComputingUnits>").append(description.getTotalComputingUnits()).append("</TotalComputingUnits>").append("\n");
        sb.append(prefix).append("<Memory>").append(description.getMemorySize()).append("</Memory>").append("\n");
        sb.append(prefix).append("<Disk>").append(description.getStorageSize()).append("</Disk>").append("\n");
        String providerName = ((CloudMethodResourceDescription) description).getProviderName();
        if (providerName == null) {
            providerName = new String("");
        }
        sb.append(prefix).append("<Provider>").append(providerName).append("</Provider>").append("\n");
        CloudImageDescription image = ((CloudMethodResourceDescription) description).getImage();
        String imageName = image.getImageName();
        if (imageName == null) {
            imageName = new String("");
        }
        sb.append(prefix).append("<Image>").append(imageName).append("</Image>").append("\n");

        return sb.toString();
    }

    public void increaseFeatures(CloudMethodResourceDescription increment, LinkedList<Implementation> compatibleImpls) {
        synchronized (available) {
            available.increase(increment);
        }
        synchronized (description) {
            description.increase(increment);
        }
        updatedFeatures(compatibleImpls);
    }

    @Override
    public MethodResourceDescription reserveResource(MethodResourceDescription consumption) {
        if (!hasAvailable(consumption)) {
            return null;
        }
        return super.reserveResource(consumption);
    }

    @Override
    public synchronized void releaseResource(MethodResourceDescription consumption) {
        logger.debug("Checking cloud resources to release...");
        // Freeing task constraints
        synchronized (available) {
            super.releaseResource(consumption);

            // Performing as much as possible reductions
            synchronized (pendingReductions) {
                if (!pendingReductions.isEmpty()) {
                    Iterator<PendingReduction< MethodResourceDescription>> prIt = pendingReductions.iterator();
                    while (prIt.hasNext()) {
                        PendingReduction< MethodResourceDescription> pRed = prIt.next();
                        if (available.containsDynamic(pRed.getModification())) {
                            // Perform reduction
                            synchronized (available) {
                                available.reduce(pRed.getModification());
                            }
                            // Untag pending to remove reduction
                            synchronized (toRemove) {
                                toRemove.reduce(pRed.getModification());
                            }
                            // Reduction is done, release sem
                            logger.debug("Releasing cloud resource " + this.getName());
                            pRed.notifyCompletion();
                            prIt.remove();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void applyReduction(PendingReduction pRed, LinkedList<Implementation> compatibleImpls) {
        MethodResourceDescription reduction = (MethodResourceDescription) pRed.getModification();
        synchronized (description) {
            description.reduce(reduction);
        }
        synchronized (available) {
            if (!hasAvailable(reduction) && this.getUsedTaskCount() > 0) {

                // This resource is still running tasks. Wait for them to finish...
                // Mark to remove and enqueue pending reduction
                synchronized (toRemove) {
                    toRemove.increase(reduction);
                }
                synchronized (pendingReductions) {
                    pendingReductions.add(pRed);
                }
            } else {
                // Resource is not executing tasks. We can erase it, nothing to do
                available.reduce(reduction);
                pRed.notifyCompletion();
            }
        }

        updatedFeatures(compatibleImpls);
    }

    @Override
    public boolean hasAvailable(MethodResourceDescription consumption) {
        synchronized (available) {
            synchronized (toRemove) {
                consumption.increaseDynamic(toRemove);
                boolean fits = super.hasAvailable(consumption);
                consumption.reduceDynamic(toRemove);
                if (logger.isDebugEnabled()) {
                    logger.debug("Cloud Method Worker received:");
                    logger.debug("With result: " + fits);
                }
                return fits;
            }
        }
    }

    @Override
    public String getResourceLinks(String prefix) {
        StringBuilder sb = new StringBuilder(super.getResourceLinks(prefix));
        sb.append(prefix).append("TYPE = WORKER").append("\n");
        sb.append(prefix).append("COMPUTING_UNITS = ").append(description.getTotalComputingUnits()).append("\n");
        sb.append(prefix).append("MEMORY = ").append(description.getMemorySize()).append("\n");

        return sb.toString();
    }

    public boolean shouldBeStopped() {
        synchronized (available) {
            synchronized (toRemove) {
                return ((available.getTotalComputingUnits() == 0) && (toRemove.getTotalComputingUnits() == 0));
            }
        }
    }

    @Override
    public Worker<?> getSchedulingCopy() {
        return new CloudMethodWorker(this);
    }

    public boolean hasPendingModifications() {
        return !pendingReductions.isEmpty();
    }

}
