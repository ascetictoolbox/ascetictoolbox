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
package eu.ascetic.paas.self.adaptation.manager.rest;

import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.rest.generated.RestDeploymentClient;
import eu.ascetic.paas.self.adaptation.manager.rest.generated.RestVMClient;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * This is the action requester that works using the Application manager's REST
 * interface.
 *
 * @author Richard Kavanagh
 */
public class ActionRequester implements Runnable, ActuatorInvoker {

    private final LinkedBlockingDeque<Response> queue = new LinkedBlockingDeque<>();
    private boolean stop = false;    
    
    public static void main(String[] args) {
        VM vm = getVM("threeTierWebApp", "100", "1686");
        printVM(vm);
        System.out.println("----");
        List<VM> vms = getVMs("threeTierWebApp", "100");
        for (VM vm1 : vms) {
            printVM(vm1);
        }
    }

    /**
     * 
     * @param vm 
     */
    private static void printVM(VM vm) {
        System.out.println("---- VM ----");
        System.out.println("VM ID: " + vm.getId());
        System.out.println("IP: " + vm.getIp());
        System.out.println("Status: " + vm.getStatus());
        System.out.println("CPU Min: " + vm.getCpuMin()
                + " Max: " + vm.getCpuMax()
                + " Actual: " + vm.getCpuActual());
        System.out.println("OVF ID: " + vm.getOvfId());
        System.out.println("---- VM END ----");
    }

    /**
     * This gets a VM given its application, deployment and VM ids.
     *
     * @param application The application ID
     * @param deployment The deployment ID
     * @param vmID The VM id
     * @return The VM given the id values specified.
     */
    public static VM getVM(String application, String deployment, String vmID) {
        /**
         * An example url is:
         * http://192.168.3.16/application-manager/applications/threeTierWebApp/deployments/100/vms/
         */
        RestVMClient client = new RestVMClient(application, deployment);
        String response = client.getVM(String.class, vmID);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(VM.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (VM) jaxbUnmarshaller.unmarshal(new StringReader(response));
        } catch (JAXBException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This lists which VMs can be added to a deployment in order to make it scale.
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to scale the named deployment
     */
    @Override
    public List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMax() > 0 && getVMsOfGivenType(vms, vm.getOvfId()) < vm.getNumberVMsMax()) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }
    
    /**
     * This lists which VMs can be added to a deployment in order to make it scale.
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to down size the named deployment
     */    
    @Override
    public List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0 && getVMsOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }
    
    @Override
    public List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<Integer> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0 && getVMsOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()) {
                answer.add(vm.getId());
            }
        }
        return answer;
    }    
    
    /**
     * This counts how many VMs have a given deployment type in a set of VMs
     * @param vms The vms to look count
     * @param type The ovf Id of the type of VMs to look for
     */
    @Override
    public int getVMsOfGivenType(List<VM> vms, String type) {
        int answer = 0;
        for (VM vm : vms) {
            if (vm.getOvfId().equals(type)) {
                answer = answer + 1;
            }
        }
        return answer;
    }
    
    /**
     * This gets a VM given its application, deployment and VM ids.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The VM given the id values specified.
     */
    public static List<VM> getVMs(String applicationId, String deploymentId) {
        /**
         * An example url is:
         * http://192.168.3.16/application-manager/applications/threeTierWebApp/deployments/100/vms/
         */
        RestDeploymentClient client = new RestDeploymentClient(applicationId);
        String response = client.getDeployment(String.class, deploymentId);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Deployment.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Deployment deployment = (Deployment) jaxbUnmarshaller.unmarshal(new StringReader(response));
            return deployment.getVms();
        } catch (JAXBException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This adds a vm of a given ovf type to named deployment.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @param ovfId The OVF id that indicates which VM type to instantiate
     */
    @Override    
    public void addVM(String applicationId, String deploymentId, String ovfId) {
        RestVMClient client = new RestVMClient(applicationId, deploymentId);
        client.postVM(ovfId);
    }

    /**
     * This deletes a VM
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    @Override    
    public void deleteVM(String application, String deployment, String vmID) {
        RestVMClient client = new RestVMClient(application, deployment);
        client.deleteVM(vmID);
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
                        launchAction(action);
                    } 
                }
            } catch (InterruptedException ex) {
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
     * @param response The response object to launch the action for
     */
    private void launchAction(Response response) {
        switch (response.getActionType()) {
            case ADD_VM:
                addVM(response.getApplicationId(), response.getDeploymentId(), response.getAdapationDetails());
                break;
            case REMOVE_VM:
                deleteVM(response.getApplicationId(), response.getDeploymentId(), response.getVmId());
                break;
        }
        response.setPerformed(true);
    }
    
    @Override
    public void actuate(Response response) {
        queue.add(response);
    }

}