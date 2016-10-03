package integratedtoolkit.connectors.ascetic;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.connectors.Connector;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.connectors.Cost;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import java.util.HashMap;

public class AsceticConnector implements Cost, Connector {

    public AsceticConnector(String providerName, HashMap<String, String> props) {

    }

    @Override
    public boolean turnON(String name, ResourceCreationRequest rR) {
        new ResourceRequester(rR).start();
        return true;
    }

    public void terminate(CloudMethodWorker worker, CloudMethodResourceDescription reduction) {
        new ResourceTerminator(worker, reduction).start();
    }

    @Override
    public Float getTotalCost() {
        return (float) Ascetic.getAccumulatedCost();
    }

    @Override
    public Float currentCostPerHour() {
        return 0f;
    }

    @Override
    public Float getMachineCostPerHour(CloudMethodResourceDescription rc) {
        return (float) Ascetic.getPrice(rc.getType());
    }

    @Override
    public void stopReached() {
        return;
    }

    @Override
    public Long getNextCreationTime() throws ConnectorException {
        return 0l;
    }

    @Override
    public void terminateAll() {

    }

    @Override
    public long getTimeSlot() {
        return 0l;
    }

    private class ResourceRequester extends Thread {

        private ResourceCreationRequest rR;

        ResourceRequester(ResourceCreationRequest rR) {
            this.rR = rR;
        }

        public void run() {
            try {
                Ascetic.requestVMCreation(rR.getRequested().getImage().getImageName(), rR.getRequested().getType());
            } catch (Exception e) {
                System.err.println("Error creating new VM " + rR.getRequested().getType());
                e.printStackTrace();
            }

        }
    }

    private class ResourceTerminator extends Thread {

        private CloudMethodWorker worker;
        private CloudMethodResourceDescription reduction;

        ResourceTerminator(CloudMethodWorker worker, CloudMethodResourceDescription reduction) {
            this.worker = worker;
            this.reduction = reduction;
        }

        public void run() {
            /*
             System.out.println("----------SHUTTING DOWN " + worker.getName());
             ResourceUpdate ru = ResourceManager.reduceCloudWorker(worker, reduction, new LinkedList());
             try {
             System.out.println("Waiting to release enough resources to reduce");
             ru.waitForCompletion();
             } catch (Exception e) {
             System.out.println("ERROR: Exception raised on worker reduction");
             e.printStackTrace();
             }

             System.out.println("RETRIEVING DATA FROM " + worker.getName());
             worker.retrieveData(true);
             Semaphore sem = new Semaphore(0);
             ShutdownListener sl = new ShutdownListener(sem);
             System.out.println("Stopping worker " + worker.getName() + "...");
             worker.stop(sl);

             sl.enable();
             try {
             sem.acquire();
             } catch (Exception e) {
             System.out.println("ERROR: Exception raised on worker shutdown");
             }
             */
            try {
                Ascetic.requestVMDestruction(worker);
            } catch (Exception e) {
                System.err.println("Error deleting VM " + worker.getName());
                e.printStackTrace();
            }
        }
    }

}
