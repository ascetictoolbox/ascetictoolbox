package es.bsc.vmmanagercore.manager;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.cloudmiddleware.JCloudsMiddleware;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbHsql;
import es.bsc.vmmanagercore.model.*;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoGanglia;
import es.bsc.vmmanagercore.monitoring.HostInfoOpenStack;
import es.bsc.vmmanagercore.monitoring.HostInfoZabbix;
import es.bsc.vmmanagercore.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    private List<HostInfo> hostsInfo;

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
        selectMonitoring(conf.monitoring, conf.hosts, conf.hostsZabbix);
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), getAllVms());
    }


    //================================================================================
    // VM Methods
    //================================================================================

    public List<VmDeployed> getAllVms() {
        // Get the IDs of all the VMs deployed
        List<String> allVmsIds = cloudMiddleware.getAllVMsId();

        // Retrieve the information of each VM
        List<VmDeployed> vmsInfo = new ArrayList<>();
        for (String vmId: allVmsIds) {
            vmsInfo.add(cloudMiddleware.getVMInfo(vmId));
        }

        return vmsInfo;
    }

    public VmDeployed getVm(String vmId) {
        return cloudMiddleware.getVMInfo(vmId);
    }

    public List<VmDeployed> getVmsOfApp(String appId) {
        // Get the IDs of the VMs of the application
        List<String> vmsIds = db.getVmsOfApp(appId);

        // Get the information for each of the VMs
        List<VmDeployed> vmsInfo = new ArrayList<>();
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

    public List<String> deployVms(List<Vm> vms) {
        // HashMap VmDescription -> ID after deployment.
        // This is used to return the IDs in the same order of the input
        Map<Vm, String> ids = new HashMap<>();

        // Decide where to deploy each VM of the application
        Map<Vm, String> vmsScheduling = scheduler.schedule(vms, hostsInfo);

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
        List<String> idsDeployedVms = new ArrayList<>();
        for (Vm vm: vms) {
            idsDeployedVms.add(ids.get(vm));
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
 
    public List<ImageUploaded> getVmImages() {
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

    public List<String> getVmImagesIds() {
        List<String> vmImagesIds = new ArrayList<>();
        List<ImageUploaded> images = cloudMiddleware.getVmImages();
        for (ImageUploaded imageDesc: images) {
            vmImagesIds.add(imageDesc.getId());
        }
        return vmImagesIds;
    }


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    public List<SchedulingAlgorithm> getAvailableSchedulingAlgorithms() {
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

    // The third parameter is required because Zabbix uses different host names. I should look more into that.
    // Passing that parameter is a temporary solution.
    private void selectMonitoring(VmManagerConfiguration.Monitoring monitoring, String[] hosts, String[] hostsZabbix) {
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
            case ZABBIX:
                for (String hostname: hostsZabbix) {
                    hostsInfo.add(new HostInfoZabbix(hostname));
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
