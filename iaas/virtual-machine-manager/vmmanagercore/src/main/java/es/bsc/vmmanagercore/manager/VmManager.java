package es.bsc.vmmanagercore.manager;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.cloudmiddleware.JCloudsMiddleware;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbHsql;
import es.bsc.vmmanagercore.model.*;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.monitoring.HostGanglia;
import es.bsc.vmmanagercore.monitoring.HostOpenStack;
import es.bsc.vmmanagercore.monitoring.HostZabbix;
import es.bsc.vmmanagercore.scheduler.EstimatesGenerator;
import es.bsc.vmmanagercore.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private EstimatesGenerator estimatesGenerator = new EstimatesGenerator();
    private List<Host> hosts = new ArrayList<>();

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

        VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
        selectMiddleware(conf.middleware);
        initializeHostsAccordingToMonitoring(conf.monitoring, conf.hosts);
        List<VmDeployed> vmsDeployed = getAllVms();
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), vmsDeployed);
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
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    public void deleteVmsOfApp(String appId) {
        for (VmDeployed vmDeployed: getVmsOfApp(appId)) {
            deleteVm(vmDeployed.getId());
        }
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
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    public List<String> deployVms(List<Vm> vms) {
        // HashMap VmDescription -> ID after deployment.
        // This is used to return the IDs in the same order of the input
        Map<Vm, String> ids = new HashMap<>();

        // Choose the best deployment plan
        DeploymentPlan deploymentPlan = scheduler.chooseBestDeploymentPlan(vms, hosts);

        // Loop through the VM assignments to hosts defined in the best deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmToDeploy = vmAssignmentToHost.getVm();
            Host hostForDeployment = vmAssignmentToHost.getHost();

            // Deploy the VM
            String vmId = cloudMiddleware.deploy(vmToDeploy, hostForDeployment.getHostname());

            // Insert the VM info in the DB
            db.insertVm(vmId, vmToDeploy.getApplicationId());

            // Save the ID of the VM deployed
            ids.put(vmToDeploy, vmId);
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
                throw new IllegalArgumentException("The action selected is not supported.");
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
    // VM price and energy estimates
    //================================================================================

    /**
     * Returns price and energy estimates for a list of VMs.
     *
     * @param vmsToBeEstimated the VMs
     * @return a list with price and energy estimates for each VM
     */
    public List<VmEstimate> getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        return estimatesGenerator.getVmEstimates(
                scheduler.chooseBestDeploymentPlan(vmsToBeEstimatedToVms(vmsToBeEstimated), hosts), getAllVms());
    }


    //================================================================================
    // Auxiliary Methods
    //================================================================================

    /**
     * Instantiates the hosts according to the monitoring software selected.
     *
     * @param monitoring the monitoring software (Ganglia, Zabbix, etc.)
     * @param hostnames the names of the hosts in the infrastructure
     */
    private void initializeHostsAccordingToMonitoring(VmManagerConfiguration.Monitoring monitoring,
            String[] hostnames) {
        switch (monitoring) {
            case GANGLIA:
                for (String hostname: hostnames) {
                    hosts.add(new HostGanglia(hostname));
                }
                break;
            case OPENSTACK:
                for (String hostname: hostnames) {
                    hosts.add(new HostOpenStack(hostname, (JCloudsMiddleware) cloudMiddleware));
                }
                break;
            case ZABBIX:
                for (String hostname: hostnames) {
                    hosts.add(new HostZabbix(hostname));
                }
                break;
            default:
                throw new IllegalArgumentException("The monitoring software selected is not supported.");
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
                throw new IllegalArgumentException("The cloud middleware selected is not supported");
        }
    }

    // Note: this function would not be needed if VmToBeEstimated inherited from Vm
    private List<Vm> vmsToBeEstimatedToVms(List<VmToBeEstimated> vmsToBeEstimated) {
        List<Vm> result = new ArrayList<>();
        for (VmToBeEstimated vmToBeEstimated: vmsToBeEstimated) {
            result.add(vmToBeEstimated.toVm());
        }
        return result;
    }
}
