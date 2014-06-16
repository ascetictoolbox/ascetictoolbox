package es.bsc.vmmanagercore.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.cloudmiddleware.JCloudsMiddleware;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbHsql;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ImageUploaded;
import es.bsc.vmmanagercore.model.SchedulingAlgorithm;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoGanglia;
import es.bsc.vmmanagercore.monitoring.HostInfoOpenStack;
import es.bsc.vmmanagercore.scheduler.Scheduler;

/**
 * VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManager {

    private CloudMiddleware cloudMiddleware;
    private VmManagerDb db;
    private Scheduler scheduler;
    private ArrayList<HostInfo> hostsInfo;

    public VmManager(String dbName) {
        // Choose the DB
        try {
            db = new VmManagerDbHsql(dbName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select the middleware, the monitoring, and the scheduling policy
        // according to what is specified in the configuration file
        VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
        selectMiddleware(conf.middleware);
        selectMonitoring(conf.monitoring, conf.hosts);
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), getAllVms());
    }


    //================================================================================
    // VM Methods
    //================================================================================

    public ArrayList<VmDeployed> getAllVms() {
        // Get the IDs of all the VMs deployed
        Collection<String> allVmsIds = cloudMiddleware.getAllVMsId();

        // Retrieve the information of each VM
        ArrayList<VmDeployed> vmsInfo = new ArrayList<>();
        for (String vmId: allVmsIds) {
            vmsInfo.add(cloudMiddleware.getVMInfo(vmId));
        }

        return vmsInfo;
    }

    public VmDeployed getVm(String vmId) {
        return cloudMiddleware.getVMInfo(vmId);
    }

    public ArrayList<VmDeployed> getVmsOfApp(String appId) {
        // Get the IDs of the VMs of the application
        ArrayList<String> vmsIds = db.getVmsOfApp(appId);

        // Get the information for each of the VMs
        ArrayList<VmDeployed> vmsInfo = new ArrayList<>();
        for (String vmId: vmsIds) {
            vmsInfo.add(cloudMiddleware.getVMInfo(vmId));
        }

        return vmsInfo;
    }

    public void deleteVm(String vmId) {
        cloudMiddleware.destroy(vmId);
        db.deleteVm(vmId);
        db.closeConnection();
    }

    private void freeHostResources(String hostname) {
        for (HostInfo host: hostsInfo) {
            if (host.getHostname().equals(hostname)) {
                host.resetReserved();
            }
        }
    }

    public ArrayList<String> deployVms(ArrayList<Vm> vmDescriptions) {
        // HashMap VmDescription -> ID after deployment.
        // This is used to return the IDs in the same order of the input
        HashMap<Vm, String> ids = new HashMap<>();

        // Decide where to deploy each VM of the application
        HashMap<Vm, String> vmsScheduling =
                scheduler.schedule(vmDescriptions, hostsInfo);

        // TODO si devuelve null es que no hay host disponible. Que hacer en ese caso?
        // For each VM that is part of the application
        for(Entry<Vm, String> vmScheduling: vmsScheduling.entrySet()) {
            // Get the host name of the server where the VM is going to be deployed
            String hostname = vmScheduling.getValue();

            // Deploy the VM
            String vmId = cloudMiddleware.deploy(vmScheduling.getKey(), hostname);

            // Free the resources that were reserved on the host during the scheduling process
            freeHostResources(hostname);

            // Insert the VM info in the DB
            db.insertVm(vmId, vmScheduling.getKey().getApplicationId()); // vmId -> appId

            // Save the ID of the VM deployed
            ids.put(vmScheduling.getKey(), vmId);
        }

        // Close the DB connection
        db.closeConnection();

        // Return the IDs of the VMs deployed in the same order that they were received
        ArrayList<String> idsDeployedVms = new ArrayList<>();
        for (Vm vmDescription: vmDescriptions) {
            idsDeployedVms.add(ids.get(vmDescription));
        }
        return idsDeployedVms;
    }

    public void performActionOnVm(String vmId, String action) {
        switch (action) {
            case "rebootHard":
                cloudMiddleware.rebootHardVm(vmId);
                break;
            case "rebootSoft":
                cloudMiddleware.rebootSoftVm(vmId);
                break;
            case "start":
                cloudMiddleware.startVm(vmId);
                break;
            case "stop":
                cloudMiddleware.stopVm(vmId);
                break;
            case "suspend":
                cloudMiddleware.suspendVm(vmId);
                break;
            case "resume":
                cloudMiddleware.resumeVm(vmId);
                break;
            default:
                // TODO: error case
                break;
        }
    }

    public boolean existsVm(String vmId) {
        return cloudMiddleware.existsVm(vmId);
    }


    //================================================================================
    // Images Methods
    //================================================================================
 
    public Collection<ImageUploaded> getVmImages() {
        return cloudMiddleware.getVmImages();
    }

    public String createVmImage(ImageToUpload imageToUpload) {
        return cloudMiddleware.createVmImage(imageToUpload);
    }

    public ImageUploaded getVmImage(String imageId) {
        return cloudMiddleware.getVmImage(imageId);
    }

    public void deleteVmImage(String id) {
        cloudMiddleware.deleteVmImage(id);
    }

    public ArrayList<String> getVmImagesIds() {
        ArrayList<String> vmImagesIds = new ArrayList<>();
        Collection<ImageUploaded> imagesDescriptions = cloudMiddleware.getVmImages();
        for (ImageUploaded imageDesc: imagesDescriptions) {
            vmImagesIds.add(imageDesc.getId());
        }
        return vmImagesIds;
    }


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    public ArrayList<SchedulingAlgorithm> getAvailableSchedulingAlgorithms() {
        return db.getAvailableSchedulingAlg();
    }

    public SchedulingAlgorithm getCurrentSchedulingAlgorithm() {
        return db.getCurrentSchedulingAlg();
    }

    public void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlg) {
        db.setCurrentSchedulingAlg(schedulingAlg);
    }


    //================================================================================
    // Auxiliary Methods
    //================================================================================

    private void selectMonitoring(VmManagerConfiguration.Monitoring monitoring, String[] hosts) {
        hostsInfo = new ArrayList<>();
        switch (monitoring) {
        case GANGLIA:
            for (String hostname: hosts) {
                hostsInfo.add(new HostInfoGanglia(hostname));
            }
            break;
        case OPENSTACK:
            for (String hostname: hosts) {
                hostsInfo.add(new HostInfoOpenStack(hostname, (JCloudsMiddleware) cloudMiddleware));
            }
            break;
        default:
            //TODO - invalid
            break;
        }
    }

    private void selectMiddleware(VmManagerConfiguration.Middleware middleware) {
        switch (middleware) {
        case OPENSTACK:
            cloudMiddleware = new JCloudsMiddleware(db);
            break;
        default:
            //TODO - invalid
            break;
        }
    }

}
