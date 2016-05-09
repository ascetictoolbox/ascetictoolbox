package es.bsc.demiurge.core.manager;

import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.manager.components.EstimatesManager;
import es.bsc.demiurge.core.manager.components.HostsManager;
import es.bsc.demiurge.core.manager.components.VmsManager;
import es.bsc.demiurge.core.models.estimates.ListVmEstimates;
import es.bsc.demiurge.core.models.estimates.VmToBeEstimated;
import es.bsc.demiurge.core.models.hosts.HardwareInfo;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.images.ImageUploaded;
import es.bsc.demiurge.core.models.scheduling.*;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.models.vms.VmRequirements;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.selfadaptation.options.SelfAdaptationOptions;

import java.util.List;
import java.util.Map;

/**
 * VM Manager interface.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface VmManager {

    //================================================================================
    // VM Methods
    //================================================================================

    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    List<VmDeployed> getAllVms();

    /**
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    VmDeployed getVm(String vmId) throws CloudMiddlewareException;

    /**
     * Returns all the VMs deployed that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the list of VMs
     */
    List<VmDeployed> getVmsOfApp(String appId);

    /**
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    void deleteVmsOfApp(String appId);

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    void deleteVm(String vmId) throws CloudMiddlewareException;

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException;

	// some test methods
//	String deployVmOnHost(Vm vm, String hostId) throws CloudMiddlewareException;
	// end of test methods

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    void performActionOnVm(String vmId, String action) throws CloudMiddlewareException;

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException;

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    boolean existsVm(String vmId);


    //================================================================================
    // Images Methods
    //================================================================================
    /**
     * Returns all the VM images in the system.
     *
     * @return the VM images
     */
    List<ImageUploaded> getVmImages();

    /**
     * Creates an image in the system.
     *
     * @param imageToUpload the image to be created/uploaded in the system
     * @return the ID of the image
     */
    String createVmImage(ImageToUpload imageToUpload) throws CloudMiddlewareException;

    /**
     * Returns an image with the ID.
     *
     * @param imageId the ID of the image
     * @return the image
     */
    ImageUploaded getVmImage(String imageId);

    /**
     * Deletes a VM image.
     *
     * @param id the ID of the VM image
     */
    void deleteVmImage(String id);

    /**
     * Returns the IDs of all the images in the system.
     *
     * @return the list of IDs
     */
    List<String> getVmImagesIds();


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    /**
     * Returns the scheduling algorithms that can be applied.
     *
     * @return the list of scheduling algorithms
     */
    List<String> getAvailableSchedulingAlgorithms();

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
	String getCurrentSchedulingAlgorithm();

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    void setSchedulingAlgorithm(String schedulingAlg);


    //================================================================================
    // VM Placement
    //================================================================================

    /**
     * Returns a list of the construction heuristics supported by the VM Manager.
     *
     * @return the list of construction heuristics
     */
    List<ConstructionHeuristic> getConstructionHeuristics();

    /**
     * Returns a list of the local search algorithms supported by the VM Manager.
     *
     * @return the list of local search algorithms
     */
    List<LocalSearchAlgorithmOptionsUnset> getLocalSearchAlgorithms();

    /**
     * This function calculates a deployment plan based on a request. It uses the VM placement library.
     *
     * @param recommendedPlanRequest the request
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                       boolean assignVmsToCurrentHosts,
                                       List<Vm> vmsToDeploy) throws CloudMiddlewareException;

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    void executeDeploymentPlan(VmPlacement[] deploymentPlan) throws CloudMiddlewareException;


    //================================================================================
    // Self Adaptation
    //================================================================================

    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param selfAdaptationOptions the options
     */
    void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions);

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return the options
     */
    SelfAdaptationOptions getSelfAdaptationOptions();


    //================================================================================
    // Hosts
    //================================================================================

    /**
     * Returns the hosts of the infrastructure.
     *
     * @return the list of hosts
     */
    List<Host> getHosts();

    /**
     * Returns a host by hostname.
     *
     * @param hostname the hostname
     * @return the host
     */
    Host getHost(String hostname);

    /**
     * Simulates pressing the power button of a host
     * @param hostname the hostname
     */
    void pressHostPowerButton(String hostname);
    
    //================================================================================
    // VM price and energy estimates
    //================================================================================

    /**
     * Returns price and energy estimates for a list of VMs.
     *
     * @param vmsToBeEstimated the VMs
     * @return a list with price and energy estimates for each VM
     */
    ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) throws CloudMiddlewareException;

    /**
     * Returns cost for a given vm
     * @param vmIds
     * @return a JSON with the next fields: 'vmId' as the id of the requested machine. 'cost' as the cost for the vm
     */
    String getVmsEstimates(List<String> vmIds) throws Exception;

    void executeOnDemandSelfAdaptation() throws CloudMiddlewareException ;

    HostsManager getHostsManager();
    VmManagerDb getDB();
    VmsManager getVmsManager();

    void doInitActions();

    EstimatesManager getEstimatesManager();

    Map<String, String> getFlavours();
    
    /**
     * Resizes an existing VM to new hardware settings.
     * 
     * @param vmId the VM's id to perform resize
     * @param vm the hardware settings to scale to
     */
    void resize(String vmId, VmRequirements vm) throws Exception;
    
    /**
     * Confirms a resize.
     * 
     * @param vmId the VM's id to perform resize
     */
    void confirmResize(String vmId);
    
    /**
     * Returns all information about hardware.
     * 
     * @return 
     */
    Map<String, HardwareInfo> getHardwareInfo();
    
    /**
     * Returns a specific hardware property.
     * 
     * @param hostname
     * @param hardware
     * @param property
     * @return 
     */
    String getHardwareInfo(String hostname, String hardware, String property);
}
