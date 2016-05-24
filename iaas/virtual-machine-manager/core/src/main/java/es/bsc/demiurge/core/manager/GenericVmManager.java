/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.manager;

import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.drivers.VmAction;
import es.bsc.demiurge.core.manager.components.*;
import es.bsc.demiurge.core.models.estimates.VmToBeEstimated;
import es.bsc.demiurge.core.models.images.ImageUploaded;
import es.bsc.demiurge.core.models.scheduling.*;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.HostFactory;
import es.bsc.demiurge.core.selfadaptation.PeriodicSelfAdaptationRunnable;
import es.bsc.demiurge.core.VmmGlobalListener;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.db.VmManagerDbFactory;
import es.bsc.demiurge.core.models.estimates.ListVmEstimates;
import es.bsc.demiurge.core.models.hosts.HardwareInfo;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.vms.VmRequirements;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.selfadaptation.SelfAdaptationManager;
import es.bsc.demiurge.core.selfadaptation.options.SelfAdaptationOptions;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic VM Manager.
 * For the moment, this VM Manager is used both in TUB and BSC testbeds. In the future, it might be a good
 * idea to create two different implementations.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class GenericVmManager implements VmManager {

    // VMM components. The VMM delegates all the work to this subcomponents
    private ImageManager imageManager;
    private HostsManager hostsManager;
    private VmsManager vmsManager;
    private SelfAdaptationOptsManager selfAdaptationOptsManager;
    private VmPlacementManager vmPlacementManager;
    private EstimatesManager estimatesManager;
    
    private CloudMiddleware cloudMiddleware;
    private SelfAdaptationManager selfAdaptationManager;
	private VmManagerDb db;

    // Specific for the Ascetic project

    private static boolean periodicSelfAdaptationThreadRunning = false;

    private static final Config conf = Config.INSTANCE;
	private Logger log = LogManager.getLogger(GenericVmManager.class);

	/**
     * Constructs a VmManager with the name of the database to be used.
     *
     */
    public GenericVmManager() {
        db = VmManagerDbFactory.getDb(conf.dbName);

    }


    //================================================================================
    // VM Methods
    //================================================================================

    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    @Override
    public List<VmDeployed> getAllVms() {
        return vmsManager.getAllVms();
    }

    /**
     * Returns a specific VM deployed.
     *
     * @param vmId the ID of the VM
     * @return the VM
     */
    @Override
    public VmDeployed getVm(String vmId) throws CloudMiddlewareException {
        return vmsManager.getVm(vmId);
    }

    /**
     * Returns all the VMs deployed that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the list of VMs
     */
    @Override
    public List<VmDeployed> getVmsOfApp(String appId) {
        return vmsManager.getVmsOfApp(appId);
    }

    /**
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    @Override
    public void deleteVmsOfApp(String appId) {
        vmsManager.deleteVmsOfApp(appId);
    }

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    @Override
    public void deleteVm(String vmId) throws CloudMiddlewareException {
        vmsManager.deleteVm(vmId);
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    @Override
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
        return vmsManager.deployVms(vms);
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    @Override
    public void performActionOnVm(String vmId, String action) throws CloudMiddlewareException {
        vmsManager.performActionOnVm(vmId, VmAction.fromCamelCase(action));
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    @Override
    public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {
        vmsManager.migrateVm(vmId, destinationHostName);
    }

    /**
     * Checks whether a VM exists.
     *
     * @param vmId the ID of the VM
     * @return True if exists a VM with the input ID, false otherwise
     */
    @Override
    public boolean existsVm(String vmId) {
        return vmsManager.existsVm(vmId);
    }


    //================================================================================
    // Images Methods
    //================================================================================

    /**
     * Returns all the VM images in the system.
     *
     * @return the VM images
     */
    @Override
    public List<ImageUploaded> getVmImages() {
        return imageManager.getVmImages();
    }

    /**
     * Creates an image in the system.
     *
     * @param imageToUpload the image to be created/uploaded in the system
     * @return the ID of the image
     */
    @Override
    public String createVmImage(ImageToUpload imageToUpload) throws CloudMiddlewareException {
        return imageManager.createVmImage(imageToUpload);
    }

    /**
     * Returns an image with the ID.
     *
     * @param imageId the ID of the image
     * @return the image
     */
    @Override
    public ImageUploaded getVmImage(String imageId) {
        return imageManager.getVmImage(imageId);
    }

    /**
     * Deletes a VM image.
     *
     * @param id the ID of the VM image
     */
    @Override
    public void deleteVmImage(String id) {
        imageManager.deleteVmImage(id);
    }

    /**
     * Returns the IDs of all the images in the system.
     *
     * @return the list of IDs
     */
    @Override
    public List<String> getVmImagesIds() {
        return imageManager.getVmImagesIds();
    }


    //================================================================================
    // Scheduling Algorithms Methods
    //================================================================================

    /**
     * Returns the scheduling algorithms that can be applied.
     *
     * @return the list of scheduling algorithms
     */
    @Override
    public List<String> getAvailableSchedulingAlgorithms() {
		return new ArrayList<>(Config.INSTANCE.getPlacementPolicies().keySet());
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    @Override
    public String getCurrentSchedulingAlgorithm() {
        return db.getCurrentSchedulingAlg();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    @Override
    public void setSchedulingAlgorithm(String schedulingAlg) {
		db.setCurrentSchedulingAlg(schedulingAlg);
    }


    //================================================================================
    // VM Placement
    //================================================================================

    /**
     * Returns a list of the construction heuristics supported by the VM Manager.
     *
     * @return the list of construction heuristics
     */
    @Override
    public List<ConstructionHeuristic> getConstructionHeuristics() {
        return vmPlacementManager.getConstructionHeuristics();
    }

    /**
     * Returns a list of the local search algorithms supported by the VM Manager.
     *
     * @return the list of local search algorithms
     */
    @Override
    public List<LocalSearchAlgorithmOptionsUnset> getLocalSearchAlgorithms() {
        return vmPlacementManager.getLocalSearchAlgorithms();
    }

    /**
     * This function calculates a deployment plan based on a request. It uses the VM placement library.
     *
     * @param recommendedPlanRequest the request
     * @param selfAdaptationAction indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    @Override
    public RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                              SelfAdaptationAction selfAdaptationAction,
                                              List<Vm> vmsToDeploy) throws CloudMiddlewareException {
        return vmPlacementManager.getRecommendedPlan(db.getCurrentSchedulingAlg(),recommendedPlanRequest, selfAdaptationAction, vmsToDeploy, vmsManager.getHardwareInfo());
    }

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    @Override
    public void executeDeploymentPlan(VmPlacement[] deploymentPlan) throws CloudMiddlewareException {
        vmPlacementManager.executeDeploymentPlan(deploymentPlan);
    }


    //================================================================================
    // Self Adaptation
    //================================================================================

    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param selfAdaptationOptions the options
     */
    @Override
    public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
        selfAdaptationOptsManager.saveSelfAdaptationOptions(selfAdaptationOptions);
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return the options
     */
    @Override
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        return selfAdaptationOptsManager.getSelfAdaptationOptions();
    }


    //================================================================================
    // Hosts
    //================================================================================

    /**
     * Returns the hosts of the infrastructure.
     *
     * @return the list of hosts
     */
    @Override
    public List<Host> getHosts() {
        return hostsManager.getHosts();
    }

    /**
     * Returns a host by hostname.
     *
     * @param hostname the hostname
     * @return the host
     */
    @Override
    public Host getHost(String hostname) {
        return hostsManager.getHost(hostname);
    }

    /**
     * Simulates pressing the power button of a host
     * @param hostname the hostname
     */
    @Override
    public void pressHostPowerButton(String hostname) {
        hostsManager.pressHostPowerButton(hostname);
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
    @Override
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) throws CloudMiddlewareException {
        return estimatesManager.getVmEstimates(vmsToBeEstimated);
    }
    
    
    //================================================================================
    // Private Methods
    //================================================================================
    
    @Override
    public void doInitActions() {
        this.cloudMiddleware = conf.getCloudMiddleware();

        selfAdaptationManager = new SelfAdaptationManager(this, GenericVmManager.conf.dbName, conf.getVmmGlobalListeners());

        // Initialize all the VMM components
        imageManager = new ImageManager(cloudMiddleware);

        // Instantiates the hosts according to the monitoring software selected.
        HostFactory hf = Config.INSTANCE.getHostFactory();

        List<Host> hosts = new ArrayList<>();

        for(String hostname : Config.INSTANCE.hosts) {
            hosts.add(hf.getHost(hostname));
        }

        hostsManager = new HostsManager(hosts);

        // initializes other subcomponents
        estimatesManager = new EstimatesManager(this, conf.getEstimators());

        vmsManager = new VmsManager(hostsManager, cloudMiddleware, db, selfAdaptationManager, estimatesManager, conf.getVmmListeners(), conf.getHwinfo());

        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager(vmsManager, hostsManager, estimatesManager);

        // Start periodic self-adaptation thread if it is not already running.
        // This check would not be needed if only one instance of this class was created.
        if (!periodicSelfAdaptationThreadRunning) {
            periodicSelfAdaptationThreadRunning = true;
            startPeriodicSelfAdaptationThread();
        }

        for(VmmGlobalListener l : conf.getVmmGlobalListeners()) {
            l.onVmmStart();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Notifying vmm global listeners on shutdown hook");
                for(VmmGlobalListener l : conf.getVmmGlobalListeners()) {
                    l.onVmmStop();
                }
            }
        }));
    }

    @Override
    public EstimatesManager getEstimatesManager() {
        return estimatesManager;
    }

    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
            new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
            "periodicSelfAdaptationThread");
        thread.start();
    }

    @Override
    public HostsManager getHostsManager() {
        return hostsManager;
    }

    @Override
    public VmManagerDb getDB() {
        return db;
    }

    @Override
    public VmsManager getVmsManager() {
        return vmsManager;
    }
    
    /**
     * Starts a self-adaptation thread triggered externally.
     * 
     * @throws CloudMiddlewareException 
     */
    @Override
    public void executeOnDemandSelfAdaptation() throws CloudMiddlewareException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Starting new Thread for self-adaptaion");
                selfAdaptationManager.applyOnDemandSelfAdaptation(
                        new SelfAdaptationAction()
                );
                log.debug("Self-adaptation thread ended");
            }
        },"onDemandSelfAdaptationThread").start();
    }
    
    /**
     * Executes a self-adaptation thread based on new requirements that can be obtained from SLAm.
     * 
     * @param slamMessage the message received via SLA manager.
     * @throws CloudMiddlewareException 
     */
    @Override
    public void executeOnDemandSelfAdaptation(final String slamMessage) throws CloudMiddlewareException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Starting new Thread for self-adaptaion");
                selfAdaptationManager.applyOnDemandSelfAdaptation( 
                    new SelfAdaptationAction(slamMessage) 
                );
                log.debug("Self-adaptation thread ended");
            }
        },"onDemandSelfAdaptationThread").start();
    }

    @Override
    public String getVmsEstimates(List<String> vmIds) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for(String vmid : vmIds) {
            VmDeployed vm = vmsManager.getVm(vmid);
            if(vm == null) {
                throw new Exception("VM '"+vmid+"' does not exist");
            }
            if(first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append("{\"vmId\":\"").append(vmid).append('\"');
            for(Estimator estimator : estimatesManager) {
                sb.append(",\"");
                sb.append(estimator.getLabel());
                sb.append("\":");
                Map<String,Object> options = new HashMap<>();
                options.put("undeployed",false); // hack for ascetic pricing modeler
                sb.append(estimator.getCurrentEstimation(vmid,options));
            }
            sb.append('}');
        }
        String retJson = sb.append(']').toString();

        log.debug("getVMscost returned: " + retJson);

        return retJson;
    }

    @Override
    public Map<String, String> getFlavours() {
        return vmsManager.getFlavours();
    }

    @Override
    public void resize(String vmId, VmRequirements vm) throws Exception{
        vmsManager.resize(vmId, vm);
    }
    
    @Override
    public void confirmResize(String vmId) {
        vmsManager.confirmResize(vmId);
    }
    
    @Override
    public Map<String,HardwareInfo> getHardwareInfo() {
        return vmsManager.getHardwareInfo();
    }
    
    @Override
    public String getHardwareInfo(String hostname, String hardware, String property) {
        return vmsManager.getHardwareInfo(hostname, hardware, property);
    }
}
