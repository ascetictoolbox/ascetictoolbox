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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.SLALimits;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.rest.generated.ProviderSlotClient;
import eu.ascetic.paas.self.adaptation.manager.rest.generated.RestDeploymentClient;
import eu.ascetic.paas.self.adaptation.manager.rest.generated.RestVMClient;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.io.StringReader;
import java.lang.reflect.Type;
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

    /**
     * This gets the ovf of a given deployment.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The ovf that describes a given deployment.
     */
    @Override
    public String getOvf(String applicationId, String deploymentId) {
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
            return deployment.getOvf();
        } catch (JAXBException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            client.close();
        }
        return null;
    }

    /**
     * This gets a VM given its application, deployment and VM ids.
     *
     * @param application The application ID
     * @param deployment The deployment ID
     * @param vmID The VM id
     * @return The VM given the id values specified.
     */
    @Override
    public VM getVM(String application, String deployment, String vmID) {
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
        } finally {
            client.close();
        }
        return null;
    }

    /**
     * This lists which VMs can be added to a deployment in order to make it
     * scale.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to scale the named deployment
     */
    @Override
    public List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMax() > 0
                    && getVmCountOfGivenType(vms, vm.getOvfId()) < vm.getNumberVMsMax()
                    && !answer.contains(vm.getOvfId())) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }

    /**
     * This lists which VMs can be added to a deployment in order to make it
     * scale.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The OVF ids that can be used to down size the named deployment
     */
    @Override
    public List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0
                    && getVmCountOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()
                    && !answer.contains(vm.getOvfId())) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }

    @Override
    public List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId
    ) {
        ArrayList<Integer> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0
                    && getVmCountOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()) {
                answer.add(vm.getId());
            }
        }
        return answer;
    }

    @Override
    public int getVmCountOfGivenType(String applicationId, String deploymentId, String type) {
        return getVmCountOfGivenType(getVMs(applicationId, deploymentId), type);
    }

    /**
     * This counts how many VMs have a given deployment type in a set of VMs
     *
     * @param vms The vms to look count
     * @param type The ovf Id of the type of VMs to look for
     * @return
     */
    public int getVmCountOfGivenType(List<VM> vms, String type) {
        int answer = 0;
        for (VM vm : vms) {
            if (vm.getOvfId().equals(type)
                    && !vm.getStatus().equals("ERROR")
                    && !vm.getStatus().equals("TERMINATED")
                    && !vm.getStatus().equals("DELETED")) {
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
    public List<VM> getVMs(String applicationId, String deploymentId) {
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
        } finally {
            client.close();
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
        VM vm = cloneVm(getVm(applicationId, deploymentId, ovfId));
        if (vm == null) {
            return;
        }
        String vmDetails = "AppID: " + applicationId + "Deployment ID: " 
                + deploymentId + "OVF ID: " + ovfId + "VM: " + vm;
        Logger.getLogger(ActionRequester.class.getName()).log(Level.INFO, vmDetails);
        client.postVM(ModelConverter.objectVMToXML(vm));
        client.close();
    }

    /**
     * This clones a VM except for its identifying information
     *
     * @param vm The VM to clone
     * @return The cloned VM.
     */
    public VM cloneVm(VM vm) {
        VM answer = new VM();
        answer.setImages(vm.getImages());
        answer.setLinks(vm.getLinks());
        answer.setCpuActual(vm.getCpuActual());
        answer.setCpuMax(vm.getCpuMax());
        answer.setCpuMin(vm.getCpuMin());
        answer.setRamActual(vm.getRamActual());
        answer.setRamMin(vm.getRamMin());
        answer.setRamMax(vm.getRamMax());
        answer.setSwapActual(vm.getSwapActual());
        answer.setSwapMin(vm.getSwapMin());
        answer.setSwapMax(vm.getSwapMax());
        answer.setSlaAgreement(vm.getSlaAgreement());
        answer.setDeployment(vm.getDeployment());
        answer.setDiskActual(vm.getDiskActual());
        answer.setDiskMax(vm.getDiskMax());
        answer.setDiskMin(vm.getDiskMin());
        answer.setHref(vm.getHref());
        answer.setNumberVMsMax(vm.getNumberVMsMax());
        answer.setNumberVMsMin(vm.getNumberVMsMin());
        answer.setOvfId(vm.getOvfId());
        return answer;
    }

    /**
     * This provides details of a VM of the same ovf type as specified.
     *
     * @param applicationId The application id to get the VM for
     * @param deploymentId The deployment id to get the VM for
     * @param ovfId The ovf id to get the VM for
     * @return The VM type
     */
    private VM getVm(String applicationId, String deploymentId, String ovfId) {
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getOvfId().equals(ovfId)) {
                return vm;
            }
        }
        return null;
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
        client.close();
    }

    /**
     * This scales up a named VM. VM types are expected to be in a co-ordinated
     * series, thus allowing a +1 or -1 notion of direction and scaling to be
     * used.
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    @Override
    public void scaleUpVM(String application, String deployment, String vmID) {
        RestVMClient client = new RestVMClient(application, deployment);
        //TODO Adjust code here.
        //client.deleteVM(vmID);
    }

    /**
     * This scales down a named VM. VM types are expected to be in a
     * co-ordinated series, thus allowing a +1 or -1 notion of direction and
     * scaling to be used.
     *
     * @param application The application the VM is part of
     * @param deployment The id of the deployment instance of the VM
     * @param vmID The id of the VM to delete
     */
    @Override
    public void scaleDownVM(String application, String deployment, String vmID) {
        RestVMClient client = new RestVMClient(application, deployment);
        //TODO Adjust code here.
        //client.deleteVM(vmID);
    }

    /**
     * This gets the power usage of a VM.
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     * @param vmID The id of the VM to get the measurement for
     */
    @Override
    public double getPowerUsageVM(String applicationId, String deploymentId, String vmID) {
        RestVMClient client = new RestVMClient(applicationId, deploymentId);
        String response = client.getPowerEstimation(String.class, vmID);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            PowerMeasurement measurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(response));
            return measurement.getValue();
        } catch (JAXBException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            client.close();
        }
        return -1;
    }

    @Override
    public double getAveragePowerUsage(String applicationId, String deploymentId, String vmType) {
        double answer = 0.0;
        double count = 0.0;
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getOvfId().equals(vmType)) {
                answer = answer + getPowerUsageVM(applicationId, deploymentId, vm.getId() + "");
                count = count + 1;
            }
        }
        if (count >= 1) {
            return answer / count;
        } else {
            return answer;
        }
    }

    @Override
    public double getTotalPowerUsage(String applicationId, String deploymentId) {
        double answer = 0.0;
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            answer = answer + getPowerUsageVM(applicationId, deploymentId, vm.getId() + "");
        }
        return answer;
    }

    @Override
    public SLALimits getSlaLimits(String applicationId, String deploymentId) {
        RestDeploymentClient client = new RestDeploymentClient(applicationId);
        try {
            String response = client.getSLALimits(String.class, deploymentId);
            JAXBContext jaxbContext = JAXBContext.newInstance(SLALimits.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SLALimits slaLimits = (SLALimits) jaxbUnmarshaller.unmarshal(new StringReader(response));
            return slaLimits;
        } catch (JAXBException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            client.close();
        }
        return null;
    }

    @Override
    public List<Slot> getSlots(VmRequirements requirements) {
        ProviderSlotClient client = new ProviderSlotClient("1"); //TODO fix this
        Gson gson = new Gson();
        String reqs = gson.toJson(requirements);
        ClientResponse clientResponse = client.postSlots(reqs);
        String response = clientResponse.getEntity(String.class);
        Type listType = new TypeToken<ArrayList<Slot>>() {
        }.getType();
        ArrayList<Slot> output = gson.fromJson(response, listType);
        return output;
    }
    
    @Override
    public List<Slot> getSlots() {
        ProviderSlotClient client = new ProviderSlotClient("1"); //TODO fix this
        Gson gson = new Gson();
        ClientResponse clientResponse = client.getSlots();
        String response = clientResponse.getEntity(String.class);
        Type listType = new TypeToken<ArrayList<Slot>>() {
        }.getType();
        ArrayList<Slot> output = gson.fromJson(response, listType);
        return output;
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
    public void renegotiate(String applicationId, String deploymentId) {
        RestDeploymentClient client = new RestDeploymentClient(applicationId);
        client.renegotiate(String.class, deploymentId);
        client.close();
    }

    /**
     * This deletes all VMs of an application
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     */
    @Override
    public void hardShutdown(String applicationId, String deploymentId) {
        RestVMClient client = new RestVMClient(applicationId, deploymentId);
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            client.deleteVM(vm.getId() + "");
        }
        client.close();
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
                            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
                            action.setPerformed(true);
                            action.setPossibleToAdapt(false);
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, "The Response type was not recoginised by this adaptor");
                break;
        }
        response.setPerformed(true);
    }

    @Override
    public void actuate(Response response) {
        queue.add(response);
    }

}
