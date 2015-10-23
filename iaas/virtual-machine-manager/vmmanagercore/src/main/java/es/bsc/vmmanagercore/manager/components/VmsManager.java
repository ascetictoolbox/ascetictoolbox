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
import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.manager.DeploymentEngine;
import es.bsc.vmmanagercore.message_queue.MessageQueue;
import es.bsc.vmmanagercore.modellers.energy.EnergyModeller;
import es.bsc.vmmanagercore.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import es.bsc.vmmanagercore.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.vmmanagercore.models.scheduling.*;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.hosts.Host;
import es.bsc.vmmanagercore.monitoring.zabbix.ZabbixConnector;
import es.bsc.vmmanagercore.scheduler.Scheduler;
import es.bsc.vmmanagercore.selfadaptation.AfterVmDeleteSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.AfterVmsDeploymentSelfAdaptationRunnable;
import es.bsc.vmmanagercore.selfadaptation.SelfAdaptationManager;
import es.bsc.vmmanagercore.utils.TimeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static es.bsc.vmmanagercore.manager.DeploymentEngine.LEGACY;
import static es.bsc.vmmanagercore.manager.DeploymentEngine.OPTAPLANNER;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmsManager {

	private final Logger log = LogManager.getLogger(VmsManager.class);
    private final HostsManager hostsManager;
    private final CloudMiddleware cloudMiddleware;
    private final VmManagerDb db;
    private final SelfAdaptationManager selfAdaptationManager;
    private final Scheduler scheduler;
    private final PricingModeller pricingModeller;
    private final EnergyModeller energyModeller;

    private static final String ASCETIC_ZABBIX_SCRIPT_PATH = "/DFS/ascetic/vm-scripts/zabbix_agents.sh";

    public VmsManager(HostsManager hostsManager, CloudMiddleware cloudMiddleware, VmManagerDb db, 
                      SelfAdaptationManager selfAdaptationManager, 
                      EnergyModeller energyModeller, PricingModeller pricingModeller) {
        this.hostsManager = hostsManager;
        this.cloudMiddleware = cloudMiddleware;
        this.db = db;
        this.selfAdaptationManager = selfAdaptationManager;
        this.pricingModeller = pricingModeller;
        this.energyModeller = energyModeller;
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
			try {
            	result.add(getVm(vmId));
			} catch(CloudMiddlewareException ex) {
				log.warn("Ignoring this exception, which should never happen: " + ex.getMessage(), ex);
			}
        }
        return result;
    }

    /**
     * Returns the VMs that have been scheduled (have been assigned a host), but have not been deployed yet.
     *
     * @return the list of VMs that have been scheduled, but not deployed
     */
    public List<VmDeployed> getScheduledNonDeployedVms() throws CloudMiddlewareException {
        // It might seem confusing that this function returns a list of VmDeployed instead of Vm.
        // The reason is that the Vm class does not have a host assigned whereas VmDeployed does.
        // This is a temporary solution. I need to create a new 'ScheduledNonDeployedVm' class separated from
        // Vm and VmDeployed.

        List<VmDeployed> result = new ArrayList<>();
        for (String vmId: cloudMiddleware.getScheduledNonDeployedVmsIds()) {
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
    public VmDeployed getVm(String vmId) throws CloudMiddlewareException {
        // We need to set the state information of the VM that is not managed by the cloud middleware here.
        // Currently, we need to set the Application ID, the OVF ID, and the SLA ID.
        // The OVF and the SLA IDs are specific for Ascetic.
        VmDeployed vm = cloudMiddleware.getVM(vmId);
        if (vm != null) {
            vm.setApplicationId(db.getAppIdOfVm(vm.getId()));
            vm.setOvfId(db.getOvfIdOfVm(vm.getId()));
            vm.setSlaId(db.getSlaIdOfVm(vm.getId()));
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
			try {
				result.add(getVm(vmId));
			} catch (CloudMiddlewareException e) {
				log.error(e.getMessage(),e);
			}
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
			try {
            	deleteVm(vmDeployed.getId());
			} catch(CloudMiddlewareException ex) {
				log.error(ex.getMessage(), ex);

			}
        }
    }

    /**
     * Deletes a VM and applies self-adaptation if it is enabled.
     *
     * @param vmId the ID of the VM
     */
    public void deleteVm(final String vmId) throws CloudMiddlewareException {
		long now = System.currentTimeMillis();
		log.debug("Destroying VM: " + vmId);
        VmDeployed vmToBeDeleted = getVm(vmId);

        cloudMiddleware.destroy(vmId);
        db.deleteVm(vmId);

        // If the monitoring system is Zabbix, then we need to delete the VM from Zabbix
        if (usingZabbix()) {
            try {
                ZabbixConnector.deleteVmFromZabbix(vmId, vmToBeDeleted.getHostName());
            } catch(Exception e) {
                log.error(e.getMessage(),e);
            }
        }
		log.debug(vmId + " destruction took " + (System.currentTimeMillis()/1000.0) + " seconds");
        MessageQueue.publishMessageVmDestroyed(vmToBeDeleted);
        performAfterVmDeleteSelfAdaptation();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// indicating vm has been stopped
					pricingModeller.getVMFinalCharges(vmId,true);
				} catch (Exception e) {
					log.warn("Error closing pricing Modeler for VM " + vmId + ": " + e.getMessage());
				}
			}
		}).start();
	}

    /**
     * Deploys a list of VMs and returns its IDs.
     *
     * @param vms the VMs to deploy
     * @return the IDs of the VMs deployed in the same order that they were received
     */
    public List<String> deployVms(List<Vm> vms) throws CloudMiddlewareException {
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
            // agents is executed.
            String originalVmInitScript = vmToDeploy.getInitScript();
            setAsceticInitScript(vmToDeploy);


            String vmId;
            if (VmManagerConfiguration.getInstance().deployVmWithVolume) {
                vmId = deployVmWithVolume(vmToDeploy, hostForDeployment, originalVmInitScript);
            }
            else {
                vmId = deployVm(vmToDeploy, hostForDeployment);
            }

            db.insertVm(vmId, vmToDeploy.getApplicationId(), vmToDeploy.getOvfId(), vmToDeploy.getSlaId());
            ids.put(vmToDeploy, vmId);

            VMMLogger.logVmDeploymentWaitingTime(vmId,
                    TimeUtils.getDifferenceInSeconds(calendarDeployRequestReceived, Calendar.getInstance()));

            // If the monitoring system is Zabbix, then we need to call the Zabbix wrapper to initialize
            // the Zabbix agents. To register the VM we agreed to use the name <vmId>_<hostWhereTheVmIsDeployed>
            if (usingZabbix()) {
                ZabbixConnector.registerVmInZabbix(vmId, getVm(vmId).getHostName(), getVm(vmId).getIpAddress());
            }

			if (energyModeller instanceof AsceticEnergyModellerAdapter) {
				((AsceticEnergyModellerAdapter) energyModeller).initializeVmInEnergyModellerSystem(
						vmId,
						vmToDeploy.getApplicationId(),
						vmToDeploy.getImage());
			}

            if (pricingModeller instanceof AsceticPricingModellerAdapter) {
				try {
                	initializeVmBilling(vmId, hostForDeployment.getHostname(), vmToDeploy.getApplicationId());
				} catch(Exception e) {
					log.error("Error when initializing vm billing: " + e.getMessage(), e);
				}
            }

            if (vmToDeploy.needsFloatingIp()) {
                cloudMiddleware.assignFloatingIp(vmId);
            }
        }

        performAfterVmsDeploymentSelfAdaptation();

        // Return the IDs of the VMs deployed in the same order that they were received
        List<String> idsDeployedVms = new ArrayList<>();
        for (Vm vm: vms) {
            idsDeployedVms.add(ids.get(vm));
        }

        queueDeployedVmsMessages(idsDeployedVms);
        return idsDeployedVms;
    }

    /**
     * Performs an action on a VM (reboot, suspend, etc.).
     *
     * @param vmId the Id of the VM
     * @param action the action to perform
     */
    public void performActionOnVm(String vmId, String action) throws CloudMiddlewareException {
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
        MessageQueue.publishMessageVmChangedState(getVm(vmId), action);
    }

    /**
     * Migrates a VM to a specific host.
     *
     * @param vmId the ID of the VM
     * @param destinationHostName the host where the VM will be migrated to
     */
    public void migrateVm(String vmId, String destinationHostName) throws CloudMiddlewareException {
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

    private String deployVm(Vm vm, Host host) throws CloudMiddlewareException {
        // If the host is not on, turn it on and wait
        if (!host.isOn()) {
            hostsManager.pressHostPowerButton(host.getHostname());
            while (!host.isOn()) { // Find a better way to do this
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return cloudMiddleware.deploy(vm, host.getHostname());
    }

    private String deployVmWithVolume(Vm vm, Host host, String isoPath) throws CloudMiddlewareException {
        // If the host is not on, turn it on and wait
        if (!host.isOn()) {
            hostsManager.pressHostPowerButton(host.getHostname());
            while (!host.isOn()) { // Find a better way to do this
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return cloudMiddleware.deployWithVolume(vm, host.getHostname(), isoPath);
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

    private DeploymentPlan chooseBestDeploymentPlan(List<Vm> vms, DeploymentEngine deploymentEngine) throws CloudMiddlewareException {
        switch (deploymentEngine) {
            case LEGACY:
                // The scheduling algorithm could have been changed. Therefore, we need to set it again.
                // This is a quick fix. I need to find a way of telling the system to update properly the
                // scheduling algorithm when using the legacy deployment engine. This does not occur when using
                // the optaplanner deployment engine.
                SchedAlgorithmNameEnum currentSchedulingAlg = db.getCurrentSchedulingAlg();
                scheduler.setSchedAlgorithm(currentSchedulingAlg);
                return scheduler.chooseBestDeploymentPlan(vms, hostsManager.getHosts());
            case OPTAPLANNER:
                if (repeatedNameInVmList(vms)) {
                    throw new IllegalArgumentException("There was an error while choosing a deployment plan.");
                }

                RecommendedPlan recommendedPlan = selfAdaptationManager.getRecommendedPlanForDeployment(vms);

                // Construct deployment plan from recommended plan with only the VMs that we want to deploy,
                // we do not need here the ones that are already deployed even though they appear in the plan
                List<VmAssignmentToHost> vmAssignmentToHosts = new ArrayList<>();
                for (Vm vm: vms) {
                    // TODO: analyze if this works when some VMs have a preferred host and others do not
                    if (vm.getPreferredHost() != null && !vm.getPreferredHost().equals("")) {
                        vmAssignmentToHosts.add(new VmAssignmentToHost(
                                vm, hostsManager.getHost(vm.getPreferredHost())));
                    }
                    else {
                        VmPlacement vmPlacement = findVmPlacementByVmId(
                                recommendedPlan.getVMPlacements(), vm.getName());
                        Host host = hostsManager.getHost(vmPlacement.getHostname());
                        vmAssignmentToHosts.add(new VmAssignmentToHost(vm, host));
                    }
                }
                return new DeploymentPlan(vmAssignmentToHosts);
            default:
                throw new IllegalArgumentException("The deployment engine selected is not supported.");
        }
    }

    private void setAsceticInitScript(Vm vmToDeploy) {
        if (usingZabbix()) { // It would be more correct to check whether the VMM is running for the Ascetic project.
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

    private void queueDeployedVmsMessages(List<String> deployedVmsIds) throws CloudMiddlewareException {
        for (String idDeployedVm: deployedVmsIds) {
            MessageQueue.publishMessageVmDeployed(getVm(idDeployedVm));
        }
    }

    private void initializeVmBilling(final String vmId, final String hostname, final String appId) {
        Thread thread = new Thread() {
            public void run(){
				//
				try {
					log.info("Waiting 10 seconds before initializing VM billing. VM ID = " + vmId + "; Hostname = " + hostname);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pricingModeller.initializeVM(vmId,  hostname, appId);
            }
        };
        thread.start();
    }
    
}
