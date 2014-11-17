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
import es.bsc.vmmanagercore.cloudmiddleware.openstack.JCloudsMiddleware;
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbFactory;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.estimations.ListVmEstimates;
import es.bsc.vmmanagercore.model.estimations.VmToBeEstimated;
import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.scheduling.*;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.monitoring.HostFactory;
import es.bsc.vmmanagercore.monitoring.HostType;
import es.bsc.vmmanagercore.monitoring.ZabbixConnector;
import es.bsc.vmmanagercore.scheduler.EstimatesGenerator;
import es.bsc.vmmanagercore.scheduler.Scheduler;
import es.bsc.vmmanagercore.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.PeriodicSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.SelfAdaptationManager;
import es.bsc.vmmanagercore.selfadaptation.options.SelfAdaptationOptions;
import es.bsc.vmmanagercore.utils.FileSystem;
import es.bsc.vmmanagercore.vmplacement.OptaVmPlacementConversor;
import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.lib.OptaVmPlacement;
import es.bsc.vmplacement.lib.OptaVmPlacementImpl;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmManager {

    private CloudMiddleware cloudMiddleware;
    private VmManagerDb db;
    private Scheduler scheduler;
    private EstimatesGenerator estimatesGenerator = new EstimatesGenerator();
    private SelfAdaptationManager selfAdaptationManager;
    private List<Host> hosts = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private OptaVmPlacement optaVmPlacement = new OptaVmPlacementImpl(); // Library used for the VM Placement
    private OptaVmPlacementConversor optaVmPlacementConversor = new OptaVmPlacementConversor();

    public static EnergyModeller energyModeller;
    public static IaaSPricingModeller pricingModeller;

    // Specific for the Ascetic project
    private static final String ASCETIC_SCRIPTS_PATH = "/DFS/ascetic/vm-scripts/";
    private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";
    private static final String[] ASCETIC_DEFAULT_SEC_GROUPS = {"vmm_allow_all", "default"};

    private static boolean periodicSelfAdaptationThreadRunning = false;

    private static final VmManagerConfiguration conf = VmManagerConfiguration.getInstance();

    /**
     * Constructs a VmManager with the name of the database to be used.
     *
     * @param dbName the name of the DB
     */
    public VmManager(String dbName) {
        db = VmManagerDbFactory.getDb(dbName);
        selectMiddleware(conf.middleware);
        initializeHosts(conf.monitoring, conf.hosts);
        List<VmDeployed> vmsDeployed = getAllVms();
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), vmsDeployed);
        selfAdaptationManager = new SelfAdaptationManager(this, dbName);
        energyModeller = EnergyModeller.getInstance();
        pricingModeller = new IaaSPricingModeller();

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
    public List<VmDeployed> getAllVms() {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getAllVMsIds()) {
            result.add(cloudMiddleware.getVM(vmId));
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
        return cloudMiddleware.getVM(vmId);
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
            result.add(cloudMiddleware.getVM(vmId));
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
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    public void deleteVm(String vmId) {
        String hostname = getVm(vmId).getHostName(); // We need to get the hostname before we delete the VM
        cloudMiddleware.destroy(vmId);
        db.deleteVm(vmId);

        // If the monitoring system is Zabbix, then we need to delete the VM from Zabbix
        if (usingZabbix()) {
            // The ID of a VM in Zabbix is: vm_id + _ + hostname_where_vm_is_deployed (agreed in Ascetic)
            ZabbixConnector.getZabbixClient().deleteVM(vmId + "_" + hostname);
        }

        performAfterVmDeleteSelfAdaptation();
    }

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    public List<String> deployVms(List<Vm> vms) {
        // HashMap (VmDescription,ID after deployment). Used to return the IDs in the same order that they are received
        Map<Vm, String> ids = new HashMap<>();

        // Choose the best deployment plan
        DeploymentPlan deploymentPlan = chooseBestDeploymentPlan(
                vms, VmManagerConfiguration.getInstance().deploymentEngine);

        // Loop through the VM assignments to hosts defined in the best deployment plan
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            Vm vmToDeploy = vmAssignmentToHost.getVm();
            Host hostForDeployment = vmAssignmentToHost.getHost();

            // Note: this is only valid for the Ascetic project
            // If the monitoring system is Zabbix, we need to make sure that the script that sets up the Zabbix
            // agents is executed. Also, if an ISO is received, we need to make sure that we execute a script
            // that mounts it
            String vmScriptName = setAsceticInitScript(vmToDeploy);

            String vmId = cloudMiddleware.deploy(vmToDeploy, hostForDeployment.getHostname());
            db.insertVm(vmId, vmToDeploy.getApplicationId());
            ids.put(vmToDeploy, vmId);

            // If the monitoring system is Zabbix, then we need to call the Zabbix wrapper to initialize
            // the Zabbix agents. To register the VM we agreed to use the name <vmId>_<hostWhereTheVmIsDeployed>
            // We also need to delete the script for the VM if it was created before
            if (usingZabbix()) {
                ZabbixConnector.getZabbixClient().createVM(
                        vmId + "_" + cloudMiddleware.getVM(vmId).getHostName(),
                        cloudMiddleware.getVM(vmId).getIpAddress());

                // Delete the script if one was created
                if (vmScriptName != null) {
                    FileSystem.deleteFile(ASCETIC_SCRIPTS_PATH + vmScriptName);
                }
            }
        }

        performAfterVmsDeploymentSelfAdaptation();

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
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    public void migrateVm(String vmId, String destinationHostName) {
        VMMLogger.logMigration(vmId, destinationHostName);
        cloudMiddleware.migrate(vmId, destinationHostName);
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
        List<SchedulingAlgorithm> result = new ArrayList<>();
        result.addAll(Arrays.asList(SchedulingAlgorithm.values()));
        return result;
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
    // VM Placement
    //================================================================================

    /**
     * Returns a list of the construction heuristics supported by the VM Manager.
     *
     * @return the list of construction heuristics
     */
    public List<ConstructionHeuristic> getConstructionHeuristics() {
        List<ConstructionHeuristic> result = new ArrayList<>();
        for (es.bsc.vmplacement.domain.ConstructionHeuristic heuristic: optaVmPlacement.getConstructionHeuristics()) {
            result.add(new ConstructionHeuristic(heuristic.name()));
        }
        return result;
    }

    /**
     * Returns a list of the local search algorithms supported by the VM Manager.
     *
     * @return the list of local search algorithms
     */
    public List<LocalSearchAlgorithmOptionsUnset> getLocalSearchAlgorithms() {
        List<LocalSearchAlgorithmOptionsUnset> result = new ArrayList<>();
        for (es.bsc.vmplacement.domain.LocalSearchAlgorithm algorithm: optaVmPlacement.getLocalSearchAlgorithms()) {
            result.add(new LocalSearchAlgorithmOptionsUnset(algorithm.getName(), algorithm.getOptions()));
        }
        return result;
    }

    /**
     * This function calculates a deployment plan based on a request. It uses the OptaVMPlacement library.
     *
     * @param recommendedPlanRequest the request
     * @param assignVmsToCurrentHosts indicates whether the hosts should be set in the VM instances
     * @param vmsToDeploy list of VMs that need to be deployed
     * @return the recommended plan
     */
    public RecommendedPlan getRecommendedPlan(RecommendedPlanRequest recommendedPlanRequest,
                                              boolean assignVmsToCurrentHosts,
                                              List<Vm> vmsToDeploy) {
        ClusterState clusterStateRecommendedPlan = optaVmPlacement.getBestSolution(
                optaVmPlacementConversor.getOptaHosts(getHosts()),
                optaVmPlacementConversor.getOptaVms(
                        getAllVms(),
                        vmsToDeploy,
                        optaVmPlacementConversor.getOptaHosts(getHosts()),
                        assignVmsToCurrentHosts),
                optaVmPlacementConversor.getOptaPlacementConfig(
                        getCurrentSchedulingAlgorithm(),
                        recommendedPlanRequest));
        return optaVmPlacementConversor.getRecommendedPlan(clusterStateRecommendedPlan);
    }

    /**
     * This function executes a deployment plan. This means that each of the VMs of the deployment plan are migrated
     * to the host specified if they were not already deployed there.
     *
     * @param deploymentPlan the deployment plan
     */
    public void executeDeploymentPlan(VmPlacement[] deploymentPlan) {
        for (VmPlacement vmPlacement: deploymentPlan) {

            // We need to check that the VM is still deployed.
            // It might be the case that a VM was deleted in the time interval between a recommended plan is
            // calculated and the execution order for that deployment plan is received
            if (cloudMiddleware.getVM(vmPlacement.getVmId()) != null) {
                boolean vmAlreadyDeployedInHost = vmPlacement.getHostname().equals(cloudMiddleware.getVM(
                        vmPlacement.getVmId()).getHostName());
                if (!vmAlreadyDeployedInHost) {
                    migrateVm(vmPlacement.getVmId(), vmPlacement.getHostname());
                }
            }

        }
    }


    //================================================================================
    // Self Adaptation
    //================================================================================

    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param selfAdaptationOptions the options
     */
    public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
        selfAdaptationManager.saveSelfAdaptationOptions(selfAdaptationOptions);
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return the options
     */
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        return selfAdaptationManager.getSelfAdaptationOptions();
    }


    //================================================================================
    // Hosts
    //================================================================================

    /**
     * Returns the hosts of the infrastructure.
     *
     * @return the list of hosts
     */
    public List<Host> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    /**
     * Returns a host by hostname.
     *
     * @param hostname the hostname
     * @return the host
     */
    public Host getHost(String hostname) {
        for (Host host: hosts) {
            if (hostname.equals(host.getHostname())) {
                return host;
            }
        }
        return null;
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
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        return estimatesGenerator.getVmEstimates(scheduler.chooseBestDeploymentPlan(
                vmsToBeEstimatedToVms(vmsToBeEstimated), hosts), getAllVms());
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
        for (String hostname: hostnames) {
            switch (monitoring) {
                case OPENSTACK:
                    hosts.add(HostFactory.getHost(hostname, HostType.OPENSTACK, (JCloudsMiddleware) cloudMiddleware));
                    break;
                case GANGLIA:
                    hosts.add(HostFactory.getHost(hostname, HostType.GANGLIA, null));
                    break;
                case ZABBIX:
                    hosts.add(HostFactory.getHost(hostname, HostType.ZABBIX, null));
                    break;
                default:
                    break;
            }
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
                cloudMiddleware = new JCloudsMiddleware(conf.openStackIP, conf.keyStonePort, conf.keyStoneTenant,
                        conf.keyStoneUser, conf.keyStonePassword, conf.glancePort, conf.keyStoneTenantId,
                        db, securityGroups);
                break;
            default:
                throw new IllegalArgumentException("The cloud middleware selected is not supported");
        }
    }

    /**
     * Transforms a list of VMs to be estimated to a list of VMs.
     *
     * @param vmsToBeEstimated the list of VMs to be estimated
     * @return the list of VMs
     */
    // Note: this function would not be needed if VmToBeEstimated inherited from Vm
    private List<Vm> vmsToBeEstimatedToVms(List<VmToBeEstimated> vmsToBeEstimated) {
        List<Vm> result = new ArrayList<>();
        for (VmToBeEstimated vmToBeEstimated: vmsToBeEstimated) {
            result.add(vmToBeEstimated.toVm());
        }
        return result;
    }

    private boolean usingZabbix() {
        return VmManagerConfiguration.getInstance().monitoring.equals(VmManagerConfiguration.Monitoring.ZABBIX);
    }

    private boolean isoReceivedInInitScript(Vm vm) {
        return vm.getInitScript() != null && !vm.getInitScript().equals("")
                && (vm.getInitScript().contains(".iso_") || vm.getInitScript().endsWith(".iso"));
    }

    private String setAsceticInitScript(Vm vmToDeploy) {
        String vmScriptName = null;
        if (usingZabbix()) { // It would be more correct to check whether the VMM is running for the Ascetic project.
            if (isoReceivedInInitScript(vmToDeploy)) {
                try {
                    // Copy the Zabbix agents script
                    vmScriptName = "vm_" + vmToDeploy.getName() +
                            "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".sh";
                    Files.copy(Paths.get(ASCETIC_ZABBIX_SCRIPT_PATH),
                            Paths.get(ASCETIC_SCRIPTS_PATH + vmScriptName), REPLACE_EXISTING);

                    // Append the instruction to mount the ISO
                    try (PrintWriter out = new PrintWriter(new BufferedWriter(
                            new FileWriter(ASCETIC_SCRIPTS_PATH + vmScriptName, true)))) {
                        out.println("/usr/local/sbin/set_iso_path " + vmToDeploy.getInitScript());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Assign the new script to the VM
                    vmToDeploy.setInitScript(ASCETIC_SCRIPTS_PATH + vmScriptName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Path zabbixAgentsScriptPath = FileSystems.getDefault().getPath(ASCETIC_ZABBIX_SCRIPT_PATH);
                if (Files.exists(zabbixAgentsScriptPath)) {
                    vmToDeploy.setInitScript(ASCETIC_ZABBIX_SCRIPT_PATH);
                }
                else { // This is for when I perform tests locally and do not have access to the script (and
                    // do not need it)
                    vmToDeploy.setInitScript(null);
                }
            }
        }
        return vmScriptName;
    }


    private DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms, String deploymentEngine) {
        switch (deploymentEngine) {
            case "legacy":
                return scheduler.chooseBestDeploymentPlan(vms, hosts);
            case "optaPlanner":
                if (repeatedNameInVmList(vms)) {
                    throw new IllegalArgumentException("There was an error while choosing a deployment plan.");
                }

                RecommendedPlan recommendedPlan = selfAdaptationManager.getRecommendedPlanForDeployment(vms);

                // Construct deployment plan from recommended plan with only the VMs that we want to deploy,
                // we do not need here the ones that are already deployed even though they appear in the plan
                List<VmAssignmentToHost> vmAssignmentToHosts = new ArrayList<>();
                for (Vm vm: vms) {
                    VmPlacement vmPlacement = findVmPlacementByVmId(recommendedPlan.getVMPlacements(), vm.getName());
                    Host host = getHost(vmPlacement.getHostname());
                    vmAssignmentToHosts.add(new VmAssignmentToHost(vm, host));
                }
                return new DeploymentPlan(vmAssignmentToHosts);
            default:
                throw new IllegalArgumentException("The deployment engine selected is not supported.");
        }
    }

    private VmPlacement findVmPlacementByVmId(VmPlacement[] vmPlacements, String vmId) {
        for (VmPlacement vmPlacement: vmPlacements) {
            if (vmId.equals(vmPlacement.getVmId())) {
                return vmPlacement;
            }
        }
        return null;
    }

    private boolean repeatedNameInVmList(List<Vm> vms) {
        for (int i = 0; i < vms.size(); ++i) {
            for (int j = i + 1; j < vms.size(); ++j) {
                if (vms.get(i).getName().equals(vms.get(j).getName())) {
                    return false;
                }
            }
        }
        return false;
    }

    private void performAfterVmDeleteSelfAdaptation() {
        // Execute self adaptation in a separate thread, because we need to give an answer without
        // waiting for the self-adaptation to finish
        Thread thread = new Thread(
                new AfterVmDeleteSelfAdaptationRunnable(selfAdaptationManager),
                "afterVmDeleteSelfAdaptationThread");
        thread.start();
    }

    private void performAfterVmsDeploymentSelfAdaptation() {
        Thread thread = new Thread(
                new AfterVmsDeploymentSelfAdaptationRunnable(selfAdaptationManager),
                "afterVmsDeploymentSelfAdaptationThread");
        thread.start();
    }

    private void startPeriodicSelfAdaptationThread() {
        Thread thread = new Thread(
                new PeriodicSelfAdaptationRunnable(selfAdaptationManager),
                "periodicSelfAdaptationThread");
        thread.start();
    }

}
