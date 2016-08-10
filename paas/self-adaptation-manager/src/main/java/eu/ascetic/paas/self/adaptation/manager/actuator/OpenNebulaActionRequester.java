/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.actuator;

import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.paas.applicationmanager.model.SLALimits;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opennebula.client.Client;
import org.opennebula.client.OneResponse;
import org.opennebula.client.host.Host;
import org.opennebula.client.host.HostPool;
import org.opennebula.client.vm.VirtualMachine;
import org.opennebula.client.vm.VirtualMachinePool;

/**
 * This requests adaptation actions to be performed by an actuator. This is done
 * in order to obtain the planned form of adaptation.
 *
 * @author Richard Kavanagh
 */
public class OpenNebulaActionRequester implements Runnable, ActuatorInvoker {

    private final LinkedBlockingDeque<Response> queue = new LinkedBlockingDeque<>();
    private boolean stop = false;
    private final Client client;

    public OpenNebulaActionRequester() {
        client = getClient("oneadmin", "neo", "http://10.10.0.1:2633/RPC2");
    }

    /**
     * This creates a client that has access to the OpenNebula infrastructure.
     *
     * @param username The username to be used by the client
     * @param passwd The password to be used by the client
     * @param url The connection url to use, such as: http://10.10.0.1:2633/RPC2
     * @return
     */
    private Client getClient(String username, String passwd, String url) {
        Client answer;
        try {
            answer = new Client(username + ":" + passwd, url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return answer;
    }

    /**
     * This gets the list of hosts from Open Nebula
     *
     * @return The list of all hosts.
     */
    private HostPool getHostPool() {
        HostPool hostPool = new HostPool(client);
        /**
         * This asks the pool object to retrieve the information from OpenNebula
         */
        OneResponse rc = hostPool.info();
        if (!rc.isError()) {
            for (Host host : hostPool) {
                host.info();
            }
            return hostPool;
        }
        return null;
    }

    /**
     * This gets the list of VMs available
     *
     * @return The list of all VMs.
     */
    private VirtualMachinePool getVMPool() {
        VirtualMachinePool vmPool = new VirtualMachinePool(client);
        /**
         * This asks the pool object to retrieve the information from OpenNebula
         */
        OneResponse rc = vmPool.info();
        if (!rc.isError()) {
            for (VirtualMachine vm : vmPool) {
                vm.info();
            }
            return vmPool;
        }
        return null;
    }

    @Override
    public String getOvf(String applicationId, String deploymentId) {
        return null; //Open nebula doesn't support OVF information for deployments.
    }

    @Override
    public List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId) {
        ArrayList<String> names = new ArrayList<>();
        /**
         * A List of hard coded VM types is added here. Only JBoss is considered
         * to scale in the 3 tier web app demo.
         */
        if (getVmCountOfGivenType("JBoss") < 10) { //upper bound = 10 running VMs
            names.add("JBoss");
        }
        return names;
    }

    @Override
    public List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId) {
        List<Integer> vmsIds = new ArrayList<>();
        VirtualMachinePool pool = getVMPool();
        if (pool == null) {
            System.out.println("Pool not created correctly");
            return vmsIds;
        }
        for (VirtualMachine vm : pool) {
            if (vm.getName().contains("JBoss") && vm.getId() != null) {
                vmsIds.add(Integer.parseInt(vm.getId()));
            }
        }
        return vmsIds;
    }

    @Override
    public List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<String> names = new ArrayList<>();
        /**
         * A List of hard coded VM types is added here. Only JBoss is considered
         * to scale in the 3 tier web app demo.
         */
        if (getVmCountOfGivenType("JBoss") > 1) {
            names.add("JBoss");
        }
        return names;
    }

    /**
     * This gets the count of VMs that are been monitored by this hypervisor.
     *
     * @param vms The list of VMs to count
     * @param type The type of VM to count
     * @return
     */
    public int getVmCountOfGivenType(List<VM> vms, String type) {
        return getVmCountOfGivenType(type);
    }

    @Override
    public int getVmCountOfGivenType(String applicationId, String deploymentId, String type) {
        return getVmCountOfGivenType(type);
    }

    /**
     * This gets the count of VMs that are been monitored by this hypervisor.
     *
     * @param type The type of VM to count
     * @return The count of VMs
     */
    public int getVmCountOfGivenType(String type) {
        int answer = 0;
        VirtualMachinePool pool = getVMPool();
        for (VirtualMachine vm : pool) {
            if (vm.getName().contains(type)) {
                answer = answer + 1;
            }
        }
        return answer;
    }

    @Override
    public void addVM(String applicationId, String deploymentId, String ovfId) {
        /**
         * This VM template is a valid one, but it will probably fail to run if
         * we try to deploy it; the path for the image is unlikely to exist.
         */
        String vmTemplate
                = "CPU=\"3\"\n"
                + "DESCRIPTION=\"JBoss Instance. To use this image you must alter its Network configuration (after cloning the template) to use a IP range reservation from your own private vnet. For example:\n"
                + "\n"
                + "1) Go to: Virtual Network and select 'Private vnet for sc****'.\n"
                + "2) Press [Reserve].\n"
                + "3) Fill out the form:\n"
                + "- Number of addresses: 10\n"
                + "- Virtual Network Name: 'Private vnet for sc**** (JBoss)'\n"
                + "- Press [Advanced options]\n"
                + "- Select your address range\n"
                + "- First address:10.*.*.3\n"
                + "4) Replace the existing vnet with 'Private vnet for sc**** (JBoss)' in your cloned template\n"
                + "\n"
                + "NOTE: the maximum number of JBOSS instances supported by the 3tier webapp is 250, thus the reservation should be within the range 3-253  and the 'Number of addresses' should not exceed 250).\"\n"
                + "DISK=[\n"
                + "  CACHE=\"none\",\n"
                + "  DEV_PREFIX=\"vd\",\n"
                + "  DRIVER=\"qcow2\",\n"
                + "  IMAGE=\"3Tier Debian Squeeze x86_64 Base\",\n"
                + "  IMAGE_UNAME=\"scsdja\",\n"
                + "  IO=\"native\",\n"
                + "  READONLY=\"no\",\n"
                + "  TARGET=\"vda\" ]\n"
                + "FEATURES=[\n"
                + "  ACPI=\"yes\",\n"
                + "  APIC=\"yes\" ]\n"
                + "GRAPHICS=[\n"
                + "  KEYMAP=\"en-gb\",\n"
                + "  LISTEN=\"0.0.0.0\",\n"
                + "  TYPE=\"VNC\" ]\n"
                + "HYPERVISOR=\"kvm\"\n"
                + "LOGO=\"images/logos/debian.png\"\n"
                + "MEMORY=\"8192\"\n"
                + "NIC=[\n"
                + "  MODEL=\"virtio\",\n"
                + "  NETWORK=\"Private vnet for scsdja (JBoss)\",\n"
                + "  NETWORK_UNAME=\"scsdja\" ]\n"
                + "SCHED_REQUIREMENTS=\"ID=\\\"0\\\" | ID=\\\"1\\\" | ID=\\\"2\\\" | ID=\\\"3\\\" | ID=\\\"4\\\" | ID=\\\"7\\\"\"\n"
                + "SUNSTONE_CAPACITY_SELECT=\"YES\"\n"
                + "SUNSTONE_NETWORK_SELECT=\"YES\"\n"
                + "VCPU=\"3\"";

        System.out.print("Trying to allocate the virtual machine... ");
        OneResponse rc = VirtualMachine.allocate(client, vmTemplate);

        if (rc.isError()) {
            System.out.println("failed!");
        }

        // The response message is the new VM's ID
        int newVMID = Integer.parseInt(rc.getMessage());
        System.out.println("ok, ID " + newVMID + ".");

        //Create VM representation and rename, ensuring the JBoss Type can be detected
        VirtualMachine vm = new VirtualMachine(newVMID, client);
        vm.rename(vm.getName() + " JBoss");
    }

    @Override
    public void deleteVM(String application, String deployment, String vmID) {
        VirtualMachine vm = new VirtualMachine(Integer.parseInt(vmID), client);
        OneResponse rc = vm.shutdown(false);
        if (!rc.isError()) {
            vm.shutdown(true);
        }
        vm.delete();
    }

    @Override
    public void renegotiate(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void hardShutdown(String applicationId, String deploymentId) {
        /**
         * This works over the entire pool, for a named user. The application ID
         * make no sense to the IaaS layer, and no equivalent grouping exits.
         */
        for (VirtualMachine vm : getVMPool()) {
            OneResponse rc = vm.shutdown(false);
            if (!rc.isError()) {
                vm.shutdown(true);
            }
            vm.delete();
        }
    }

    /**
     * This scales a VM type to a set amount of VMs
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @param response The response to actuator for
     */
    @Override
    public void horizontallyScaleToNVms(String applicationId, String deploymentId, Response response) {
        String vmType = response.getAdaptationDetail("VM_TYPE");
        String vmsToRemove = response.getAdaptationDetail("VMs_TO_REMOVE");
        if (vmsToRemove == null) { //Add VMs
            int count = Integer.parseInt(response.getAdaptationDetail("VM_COUNT"));
            for (int i = 0; i < count; i++) {
                addVM(applicationId, deploymentId, vmType);
            }
        } else { //Remove VMs
            for (String vmId : vmsToRemove.split(",")) {
                deleteVM(applicationId, deploymentId, vmId.trim());
            }
        }
    }

    @Override
    public void scaleUpVM(String application, String deployment, String vmID) {
        VirtualMachine vm = new VirtualMachine(Integer.parseInt(vmID), client);
        //TODO, complete the code here - Need to find next template name!!!!
        OneResponse rc = vm.resize("", false);
    }

    @Override
    public void scaleDownVM(String application, String deployment, String vmID) {
        VirtualMachine vm = new VirtualMachine(Integer.parseInt(vmID), client);
        //TODO, complete the code here - Need to find next template name!!!!        
        OneResponse rc = vm.resize("", false);
    }   

    @Override
    public double getPowerUsageVM(String application, String deployment, String vmID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getAveragePowerUsage(String applicationId, String deploymentId, String vmType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTotalPowerUsage(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }        

    @Override
    public SLALimits getSlaLimits(String applicationId, String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Slot> getSlots(VmRequirements requirements) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } 
    
    /**
     * The things that are needed to invoke an action are:
     *
     * Application ID Deployment ID VM ID (deleting) upper and lower bounds for
     * each VM ("asceticLowerBound" and "asceticUpperBound") A decision on which
     * VM type to increase would need to be done (possible??) Additional
     * Application knowledge (in OVF?) Rank order of scaling. Which VMs are very
     * busy?? Application Monitor??
     */
    @Override
    public void run() {
        while (!stop || !queue.isEmpty()) {
            try {
                Response currentItem = queue.poll(30, TimeUnit.SECONDS);
                if (currentItem != null) {
                    ArrayList<Response> actions = new ArrayList<>();
                    actions.add(currentItem);
                    queue.drainTo(actions);
                    for (Response action : actions) {
                        try {
                            launchAction(action);
                        } catch (Exception ex) {
                            /**
                             * This prevents exceptions when messaging the
                             * server from propagating and stopping the thread
                             * from running.
                             */
                            Logger.getLogger(OpenNebulaActionRequester.class.getName()).log(Level.SEVERE, null, ex);
                            action.setPerformed(true);
                            action.setPossibleToAdapt(false);
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(OpenNebulaActionRequester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This permanently stops the action requester from working. It will however
     * perform all queued work, before quitting.
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * This executes a given action for a response that has been placed in the
     * actuator's queue for deployment.
     *
     * @param response The response object to launch the action for
     */
    private void launchAction(Response response) {
        if (!response.isPossibleToAdapt()) {
            response.setPerformed(true);
            return;
        }
        switch (response.getActionType()) {
            case ADD_VM:
                addVM(response.getApplicationId(), response.getDeploymentId(), response.getAdaptationDetails());
                break;
            case REMOVE_VM:
                deleteVM(response.getApplicationId(), response.getDeploymentId(), response.getVmId());
                break;
            case HARD_SHUTDOWN_APP:
                hardShutdown(response.getApplicationId(), response.getDeploymentId());
                break;
            case DEFLATE_VM:
                scaleDownVM(response.getApplicationId(), response.getDeploymentId(), response.getVmId());
                break;
            case INFLATE_VM:
                scaleUpVM(response.getApplicationId(), response.getDeploymentId(), response.getVmId());
                break;
            case SCALE_TO_N_VMS:
                horizontallyScaleToNVms(response.getApplicationId(), response.getDeploymentId(), response);
                break;
            case RENEGOTIATE:
                renegotiate(response.getApplicationId(), response.getDeploymentId());
                break;                  
            default:
                Logger.getLogger(OpenNebulaActionRequester.class.getName()).log(Level.SEVERE, "The Response type was not recoginised by this adaptor");
                break; 
        }
        response.setPerformed(true);
    }

    @Override
    public void actuate(Response response) {
        queue.add(response);
    }

}
