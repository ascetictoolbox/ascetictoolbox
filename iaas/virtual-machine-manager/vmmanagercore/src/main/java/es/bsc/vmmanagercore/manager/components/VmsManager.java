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

package es.bsc.vmmanagercore.manager.components;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.energymodeller.EnergyModeller;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlan;
import es.bsc.vmmanagercore.model.scheduling.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.scheduling.VmPlacement;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.zabbix.ZabbixConnector;
import es.bsc.vmmanagercore.pricingmodeller.PricingModeller;
import es.bsc.vmmanagercore.scheduler.Scheduler;
import es.bsc.vmmanagercore.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.SelfAdaptationManager;
import es.bsc.vmmanagercore.utils.FileSystem;
import es.bsc.vmmanagercore.utils.TimeUtils;

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
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmsManager {

    private final HostsManager hostsManager;
    private final CloudMiddleware cloudMiddleware;
    private final VmManagerDb db;
    private final SelfAdaptationManager selfAdaptationManager;
    private final Scheduler scheduler;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String ASCETIC_SCRIPTS_PATH = "/DFS/ascetic/vm-scripts/";
    private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";
    
    public VmsManager(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, 
                      SelfAdaptationManager selfAdaptationManager, 
                      EnergyModeller energyModeller, PricingModeller pricingModeller) {
        this.hostsManager = hostsManager;
        this.cloudMiddleware = cloudMiddleware;
        this.db = db;
        this.selfAdaptationManager = selfAdaptationManager;
        scheduler = new Scheduler(db.getCurrentSchedulingAlg(), getAllVms(), energyModeller, pricingModeller);
    }
    
    /**
     * Returns a list of the VMs deployed.
     *
     * @return the list of VMs deployed.
     */
    public List<VmDeployed> getAllVms() {
        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getAllVMsIds()) {
            result.add(getVm(vmId));
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
        // The cloud middleware should not have knowledge of the app a specific VM is part of.
        // Typical middlewares such as OpenStack do not store that information.
        // Therefore, we need to set the app ID here
        VmDeployed vm = cloudMiddleware.getVM(vmId);
        if (vm != null) {
            vm.setApplicationId(db.getAppIdOfVm(vm.getId()));
        }
        return vm;
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
            result.add(getVm(vmId));
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
            ZabbixConnector.deleteVmFromZabbix(vmId, hostname);
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
        // Get current time to know how much each VM has to wait until it is deployed.
        Calendar calendarDeployRequestReceived = Calendar.getInstance();

        // HashMap (VmDescription,ID after deployment). Used to return the IDs in the same order that they are received
        Map<Vm, String> ids = new HashMap<>();

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

            String vmId = deployVm(vmToDeploy, hostForDeployment);
            db.insertVm(vmId, vmToDeploy.getApplicationId());
            ids.put(vmToDeploy, vmId);

            VMMLogger.logVmDeploymentWaitingTime(vmId,
                    TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance()));

            // If the monitoring system is Zabbix, then we need to call the Zabbix wrapper to initialize
            // the Zabbix agents. To register the VM we agreed to use the name <vmId>_<hostWhereTheVmIsDeployed>
            if (usingZabbix()) {
                ZabbixConnector.registerVmInZabbix(vmId, getVm(vmId).getHostName(), getVm(vmId).getIpAddress());
            }

            // Delete the script if one was created
            if (vmScriptName != null) {
                FileSystem.deleteFile(ASCETIC_SCRIPTS_PATH + vmScriptName);
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

    private boolean usingZabbix() {
        return VmManagerConfiguration.getInstance().monitoring.equals(VmManagerConfiguration.Monitoring.ZABBIX);
    }

    private String deployVm(Vm vm, Host host) {
        // If the host is not on, turn it on and wait
        if (!host.isOn()) {
            hostsManager.pressHostPowerButton(host.getHostname());
            while (!host.isOn()) { // Is there a cleaner way to do this?
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return cloudMiddleware.deploy(vm, host.getHostname());
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

    private DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms, String deploymentEngine) {
        switch (deploymentEngine) {
            case "legacy":
                return scheduler.chooseBestDeploymentPlan(vms, hostsManager.getHosts());
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
                    Host host = hostsManager.getHost(vmPlacement.getHostname());
                    vmAssignmentToHosts.add(new VmAssignmentToHost(vm, host));
                }
                return new DeploymentPlan(vmAssignmentToHosts);
            default:
                throw new IllegalArgumentException("The deployment engine selected is not supported.");
        }
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

    private VmPlacement findVmPlacementByVmId(VmPlacement[] vmPlacements, String vmId) {
        for (VmPlacement vmPlacement: vmPlacements) {
            if (vmId.equals(vmPlacement.getVmId())) {
                return vmPlacement;
            }
        }
        return null;
    }

    private boolean isoReceivedInInitScript(Vm vm) {
        return vm.getInitScript() != null && !vm.getInitScript().equals("")
                && (vm.getInitScript().contains(".iso_") || vm.getInitScript().endsWith(".iso"));
    }
    
}