package integratedtoolkit.ascetic;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AppManager {

    private final String applicationId;
    private final String deploymentId;

    private final HashMap<String, VM> detectedVMs;
    private final ApplicationUploader uploader;

    public AppManager(String appId, String depId, String endpoint) {
        applicationId = appId;
        deploymentId = depId;
        uploader = new ApplicationUploader(endpoint);
        detectedVMs = new HashMap<String, VM>();
    }

    public Collection<VM> getNewResources() throws ApplicationUploaderException {

        LinkedList<VM> newResources = new LinkedList<VM>();
        try {
            List<eu.ascetic.paas.applicationmanager.model.VM> vms = uploader.getDeploymentVMDescriptions(applicationId, deploymentId);

            for (eu.ascetic.paas.applicationmanager.model.VM rvm : vms) {
                String IPv4 = rvm.getIp();
                VM vm = detectedVMs.get(IPv4);
                if (vm == null) {
                    System.out.println("Discovered new VM " + IPv4);
                    vm = new VM(rvm);
                    System.out.println("VM Created");
                    vm.updateConsumptions(this);
                    System.out.println("updated VM consumptions");
                    detectedVMs.put(IPv4, vm);
                    newResources.add(vm);
                    System.out.println("Registered new VM");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Returning " + newResources.size() + " new resources");
        return newResources;
    }

    public double getPower(String id, int coreId, int implId) throws ApplicationUploaderException {
        String eventType = generateEventType(coreId, implId);
        return uploader.getEventEnergyEstimationInVM(applicationId, deploymentId, eventType, id);
    }

    public double getPrice(String id, int coreId, int implId) throws ApplicationUploaderException {
        String eventType = generateEventType(coreId, implId);
        return uploader.getEventEnergyEstimationInVM(applicationId, deploymentId, eventType, id) * 1.2;
    }

    public Cost getEstimations(String id, int coreId, int implId) throws ApplicationUploaderException {
        String eventType = generateEventType(coreId, implId);
        System.out.println("Querying estimations for Core " + coreId + " Implementation " + implId);
        Cost c = null;
        /*try {
            c = uploader.getEventCostEstimationInVM(applicationId, deploymentId, eventType, id);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (c == null) {
            c = new Cost();
            c.setPowerValue(3d);
            c.setCharges(0d);
        }
        return c;
    }

    public double getAccumulatedEnergy() throws ApplicationUploaderException {
        return uploader.getDeploymentEnergyConsumption(applicationId, deploymentId);
    }

    public double getAccumulatedCost() throws ApplicationUploaderException {
        return uploader.getDeploymentCostConsumption(applicationId, deploymentId);

    }

    private String generateEventType(int coreId, int implId) {
        return "core" + coreId + "impl" + implId;
    }
}
