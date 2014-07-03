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

    /**
     * Constructs a VmManager with the name of the database to be used.
     *
     * @param dbName the name of the DB
     */
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

    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    public List<VmDeployed> getAllVms() {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getAllVMsId()) {
            result.add(cloudMiddleware.getVMInfo(vmId));
        }
        return result;
    }

    /**
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    public VmDeployed getVm(String vmId) {
        return cloudMiddleware.getVMInfo(vmId);
    }

    /**
     * Returns all the VMs deployed that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the list of VMs
     */
    public List<VmDeployed> getVmsOfApp(String appId) {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: db.getVmsOfApp(appId)) {
            result.add(cloudMiddleware.getVMInfo(vmId));
        }
        return result;
    }

    /**
     * Deletes a VM.
     *
     * @param vmId the ID of the VM
     */
    public void deleteVm(String vmId) {
        cloudMiddleware.destroy(vmId);
        db.deleteVm(vmId);
        db.closeConnection();
    }

    /**
     * Frees the resources of a host.
     *
     * @param hostname the name of the host
     */
    private void freeHostResources(String hostname) {
        for (HostInfo host: hostsInfo) {
            if (host.getHostname().equals(hostname)) {
                host.resetReserved();
            }
        }
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
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

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
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

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    public boolean existsVm(String vmId) {
        return cloudMiddleware.existsVm(vmId);
    }


    //================================================================================
    // Images Methods
    //================================================================================

    /**
     * Returns all the VM images in the system.
     *
     * @return the VM images
     */
    public List<ImageUploaded> getVmImages() {
        return cloudMiddleware.getVmImages();
    }

    /**
     * Creates an image in the system.
     *
     * @param imageToUpload the image to be created/uploaded in the system
     * @return the ID of the image
     */
    public String createVmImage(ImageToUpload imageToUpload) {
        return cloudMiddleware.createVmImage(imageToUpload);
    }

    /**
     * Returns an image with the ID.
     *
     * @param imageId the ID of the image
     * @return the image
     */
    public ImageUploaded getVmImage(String imageId) {
        return cloudMiddleware.getVmImage(imageId);
    }

    /**
     * Deletes a VM image.
     *
     * @param id the ID of the VM image
     */
    public void deleteVmImage(String id) {
        cloudMiddleware.deleteVmImage(id);
    }

    /**
     * Returns the IDs of all the images in the system.
     *
     * @return the list of IDs
     */
    public List<String> getVmImagesIds() {
        List<String> vmImagesIds = new ArrayList<>();
        for (ImageUploaded imageDesc: cloudMiddleware.getVmImages()) {
            vmImagesIds.add(imageDesc.getId());
        }
        return vmImagesIds;
    }


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    /**
     * Returns the scheduling algorithms that can be applied.
     *
     * @return the list of scheduling algorithms
     */
    public List<SchedulingAlgorithm> getAvailableSchedulingAlgorithms() {
        return db.getAvailableSchedulingAlg();
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    public SchedulingAlgorithm getCurrentSchedulingAlgorithm() {
        return db.getCurrentSchedulingAlg();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    public void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlg) {
        db.setCurrentSchedulingAlg(schedulingAlg);
    }


    //================================================================================
    // Auxiliary Methods
    //================================================================================

    /**
     * Instantiates the hosts according to the monitoring software selected.
     *
     * @param monitoring the monitoring software (Ganglia, Zabbix, etc.)
     * @param hosts the hosts
     */
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
            case ZABBIX:
                for (String hostname: hosts) {
                    hostsInfo.add(new HostInfoZabbix(hostname));
                }
                break;
            default:
                //TODO - invalid
                break;
        }
    }

    /**
     * Instantiates the cloud middleware.
     *
     * @param middleware the cloud middleware to be used (OpenStack, CloudStack, etc.)
     */
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
