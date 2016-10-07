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
                if (rvm.getStatus()!=null && rvm.getStatus().equals("ACTIVE")){
                	VM vm = detectedVMs.get(IPv4);
                	if (vm == null) {
                		vm = new VM(rvm);
                		vm.updateConsumptions(this);
                		detectedVMs.put(IPv4, vm);
                		if (!vm.getCompatibleImplementations().isEmpty()) {
                			newResources.add(vm);
                		} else {
                			System.out.println(vm.getIPv4() + " dismissed because it has no compatible Implementations.");
                		}
                	}
                }
            }
        } catch (Exception e) {
        	System.out.println("Exception discovering VMs");
            e.printStackTrace();
        }
        return newResources;
    }

    public void requestVMCreation(String type) throws ApplicationUploaderException {
        uploader.addNewVM(applicationId, deploymentId, type);
    }

    public void requestVMDestruction(String vmId) throws ApplicationUploaderException {
        uploader.deleteVM(applicationId, deploymentId, vmId);
    }

    public Cost getEstimations(String id, int coreId, int implId) throws ApplicationUploaderException {
        String eventType = generateEventType(coreId, implId);
        Cost c = null;
        try {
            c = uploader.getEventCostEstimationInVM(applicationId, deploymentId, eventType, id);
        } catch (Exception e) {
        	System.out.println("Exception getting estimations. Setting defaults");
            //e.printStackTrace();
            c = null;
        }
        return c;
    }

    public double getAccumulatedEnergy() throws ApplicationUploaderException {
        //return 0;
        return uploader.getDeploymentEnergyConsumption(applicationId, deploymentId);
    }

    public double getAccumulatedCost() throws ApplicationUploaderException {
        //return 0;
        return uploader.getDeploymentCostConsumption(applicationId, deploymentId);

    }

    private String generateEventType(int coreId, int implId) {
        return "core" + coreId + "impl" + implId;
    }
}
