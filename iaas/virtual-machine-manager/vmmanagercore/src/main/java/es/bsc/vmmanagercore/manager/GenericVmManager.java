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

package es.bsc.vmmanagercore.manager;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.cloudmiddleware.fake.FakeCloudMiddleware;
import es.bsc.vmmanagercore.cloudmiddleware.openstack.OpenStackCredentials;
import es.bsc.vmmanagercore.cloudmiddleware.openstack.OpenStackJclouds;
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbFactory;
import es.bsc.vmmanagercore.manager.components.*;
import es.bsc.vmmanagercore.modellers.energy.EnergyModeller;
import es.bsc.vmmanagercore.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.vmmanagercore.modellers.energy.dummy.DummyEnergyModeller;
import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import es.bsc.vmmanagercore.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.vmmanagercore.modellers.price.dummy.DummyPricingModeller;
import es.bsc.vmmanagercore.models.estimates.ListVmEstimates;
import es.bsc.vmmanagercore.models.estimates.VmToBeEstimated;
import es.bsc.vmmanagercore.models.images.ImageToUpload;
import es.bsc.vmmanagercore.models.images.ImageUploaded;
import es.bsc.vmmanagercore.models.scheduling.*;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.hosts.HostFactory;
import es.bsc.vmmanagercore.monitoring.hosts.HostFake;
import es.bsc.vmmanagercore.monitoring.hosts.HostType;
import es.bsc.vmmanagercore.selfadaptation.PeriodicSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.SelfAdaptationManager;
import es.bsc.vmmanagercore.selfadaptation.options.SelfAdaptationOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic VM Manager.
 * For the moment, this VM Manager is used both in TUB and BSC testbeds. In the future, it might be a good
 * idea to create two different implementations.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class GenericVmManager implements VmManager {

    // VMM components. The VMM delegates all the work to this subcomponents
    private final ImageManager imageManager;
    private final SchedulingAlgorithmsManager schedulingAlgorithmsManager;
    private final HostsManager hostsManager;
    private final VmsManager vmsManager;
    private final SelfAdaptationOptsManager selfAdaptationOptsManager;
    private final VmPlacementManager vmPlacementManager;
    private final EstimatesManager estimatesManager;
    
    private CloudMiddleware cloudMiddleware;
    private SelfAdaptationManager selfAdaptationManager;
    private List<Host> hosts = new ArrayList<>();

    public static EnergyModeller energyModeller;
    public static PricingModeller pricingModeller;

    // Specific for the Ascetic project
    private static final String[] ASCETIC_DEFAULT_SEC_GROUPS = {"vmm_allow_all", "default"};

    private static boolean periodicSelfAdaptationThreadRunning = false;

    private static final VmManagerConfiguration conf = VmManagerConfiguration.getInstance();

    /**
     * Constructs a VmManager with the name of the database to be used.
     *
     * @param dbName the name of the DB
     */
    public GenericVmManager(String dbName) {
        VmManagerDb db = VmManagerDbFactory.getDb(dbName);
        selectMiddleware(conf.middleware);
        initializeHosts(conf.monitoring, conf.hosts);
        selectModellers(conf.project);
        selfAdaptationManager = new SelfAdaptationManager(this, dbName);

        // Initialize all the VMM components
        imageManager = new ImageManager(cloudMiddleware);
        schedulingAlgorithmsManager = new SchedulingAlgorithmsManager(db);
        hostsManager = new HostsManager(hosts);
        vmsManager = new VmsManager(hostsManager, cloudMiddleware, db, selfAdaptationManager, 
                energyModeller, pricingModeller);
        selfAdaptationOptsManager = new SelfAdaptationOptsManager(selfAdaptationManager);
        vmPlacementManager = new VmPlacementManager(vmsManager, hostsManager, schedulingAlgorithmsManager,
                energyModeller, pricingModeller);
        estimatesManager = new EstimatesManager(vmsManager, hostsManager, db, energyModeller, pricingModeller);
        
        // Start periodic self-adaptation thread if it is not already running.
        // This check would not be needed if only one instance of this class was created.
        if (!periodicSelfAdaptationThreadRunning) {
            periodicSelfAdaptationThreadRunning = true;
            startPeriodicSelfAdaptationThread();
        }

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
    public VmDeployed getVm(String vmId) {
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
    public void deleteVm(String vmId) {
        vmsManager.deleteVm(vmId);
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    @Override
    public List<String> deployVms(List<Vm> vms) {
        return vmsManager.deployVms(vms);
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    @Override
    public void performActionOnVm(String vmId, String action) {
        vmsManager.performActionOnVm(vmId, action);
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    @Override
    public void migrateVm(String vmId, String destinationHostName) {
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
    public String createVmImage(ImageToUpload imageToUpload) {
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
    public List<SchedulingAlgorithm> getAvailableSchedulingAlgorithms() {
        return schedulingAlgorithmsManager.getAvailableSchedulingAlgorithms();
    }

    /**
     * Returns the scheduling algorithm that is being used now.
     *
     * @return the scheduling algorithm being used
     */
    @Override
    public SchedulingAlgorithm getCurrentSchedulingAlgorithm() {
        return schedulingAlgorithmsManager.getCurrentSchedulingAlgorithm();
    }

    /**
     * Changes the scheduling algorithm.
     *
     * @param schedulingAlg the scheduling algorithm to be used
     */
    @Override
    public void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlg) {
        schedulingAlgorithmsManager.setSchedulingAlgorithm(schedulingAlg);
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
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    @Override
    public RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                              boolean assignVmsToCurrentHosts,
                                              List<Vm> vmsToDeploy) {
        return vmPlacementManager.getRecommendedPlan(recommendedPlanRequest, assignVmsToCurrentHosts, vmsToDeploy);
    }

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    @Override
    public void executeDeploymentPlan(VmPlacement[] deploymentPlan) {
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
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        return estimatesManager.getVmEstimates(vmsToBeEstimated);
    }


    //================================================================================
    // Private Methods
    //================================================================================
    
    /**
     * Instantiates the hosts according to the monitoring software selected.
     *
     * @param monitoring the monitoring software (Ganglia, Zabbix, etc.)
     * @param hostnames the names of the hosts in the infrastructure
     */
    private void initializeHosts(VmManagerConfiguration.Monitoring monitoring, String[] hostnames) {
        switch (monitoring) {
            case OPENSTACK:
                generateOpenStackHosts(hostnames);
                break;
            case GANGLIA:
                generateGangliaHosts(hostnames);
                break;
            case ZABBIX:
                generateZabbixHosts(hostnames);
                break;
            case FAKE:
                generateFakeHosts(hostnames);
                break;
            default:
                break;
        }
    }

    private void generateOpenStackHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.OPENSTACK, cloudMiddleware));
        }
    }

    private void generateGangliaHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.GANGLIA, null));
        }
    }

    private void generateZabbixHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.ZABBIX, null));
        }
    }

    private void generateFakeHosts(String[] hostnames) {
        for (String hostname: hostnames) {
            hosts.add(HostFactory.getHost(hostname, HostType.FAKE, cloudMiddleware));
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
                String[] securityGroups = {};
                if (usingZabbix()) { // I should check whether the VMM is configured for the Ascetic project
                    securityGroups = ASCETIC_DEFAULT_SEC_GROUPS;
                }
                cloudMiddleware = new OpenStackJclouds(getOpenStackCredentials(), securityGroups);
                break;
            case FAKE:
                cloudMiddleware = new FakeCloudMiddleware(new ArrayList<HostFake>());
                break;
            default:
                throw new IllegalArgumentException("The cloud middleware selected is not supported");
        }
    }

    private void selectModellers(String project) {
        switch (project) {
            case "ascetic":
                energyModeller = new AsceticEnergyModellerAdapter();
                pricingModeller = new AsceticPricingModellerAdapter(
                        AsceticEnergyModellerAdapter.getEnergyModeller());
                break;
            default:
                energyModeller = new DummyEnergyModeller();
                pricingModeller = new DummyPricingModeller();
                break;
        }
    }

    private boolean usingZabbix() {
        return VmManagerConfiguration.getInstance().monitoring.equals(VmManagerConfiguration.Monitoring.ZABBIX);
    }

    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
                new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
                "periodicSelfAdaptationThread");
        thread.start();
    }

    private OpenStackCredentials getOpenStackCredentials() {
        return new OpenStackCredentials(conf.openStackIP,
                conf.keyStonePort,
                conf.keyStoneTenant,
                conf.keyStoneUser,
                conf.keyStonePassword,
                conf.glancePort,
                conf.keyStoneTenantId);
    }

}
