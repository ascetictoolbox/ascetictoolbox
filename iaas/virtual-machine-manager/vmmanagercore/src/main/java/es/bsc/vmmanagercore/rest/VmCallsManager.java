package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.ListVms;
import es.bsc.vmmanagercore.model.ListVmsDeployed;

import javax.ws.rs.WebApplicationException;
import java.util.List;

/**
 * This class implements the REST calls that are related with virtual machines.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    public VmCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns a JSON that contains all the VMs deployed.
     *
     * @return the JSON document
     */
    public String getAllVms() {
        return gson.toJson(new ListVmsDeployed(vmManager.getAllVms()));
    }

    /**
     * Deploys a VM or set of VMs specified in a JSON document.
     *
     * @param vms the JSON document containing the VMs to be deployed
     * @return a JSON document that contains, for each VM deployed, its ID
     */
    public String deployVMs(String vms) {
        // Check if the JSON that contains the VMs is specified correctly
        if (!VmmRestInputValidator.checkVmDescriptions(gson.fromJson(vms, JsonObject.class))) {
            throw new WebApplicationException(400);
        }

        // Deploy the VMs
        List<String> idsVmsDeployed = vmManager.deployVms(gson.fromJson(vms, ListVms.class).getVms());

        // Return the JSON with the IDs of the VMs deployed
        return getJsonResponseFromListOfVmsIds(idsVmsDeployed).toString();
    }

    /**
     * Returns a JSON document that contains all the information about a specific VM.
     *
     * @param vmId the ID of the VM
     * @return the JSON document
     */
    public String getVm(String vmId) {
        if (vmManager.getVm(vmId) == null) { // If the VM does not exist, return 404 error
            throw new WebApplicationException(404);
        }
        return gson.toJson(vmManager.getVm(vmId));
    }

    /**
     * Performs an action (reboot, suspend, etc.) on a VM.
     *
     * @param vmId the VM ID
     * @param actionJson the JSON document that contains the action to perfom
     */
    public void changeStateVm(String vmId, String actionJson) {
        if (!vmManager.existsVm(vmId)) { // If the VM does not exists, return a 404 error
            throw new WebApplicationException(404);
        }

        // Get the action to perform (resume, reboot, etc.)
        JsonObject jsonObject = gson.fromJson(actionJson, JsonObject.class);
        if (jsonObject.get("action") == null) {
            throw new WebApplicationException(400);
        }

        // Perform the action or return a 400 error if the user did not specify a valid action
        if (!VmmRestInputValidator.isValidAction(jsonObject.get("action").getAsString())) {
            throw new WebApplicationException(400);
        }
        vmManager.performActionOnVm(vmId, jsonObject.get("action").getAsString());
    }

    /**
     * Destroys a VM.
     *
     * @param vmId the VM ID
     */
    public void destroyVm(String vmId) {
        if (!vmManager.existsVm(vmId)) { // If the VM does not exists, return a 404 error
            throw new WebApplicationException(404);
        }
        vmManager.deleteVm(vmId);
    }

    /**
     * Returns a JSON document containing the information about all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     * @return the JSON document
     */
    public String getVmsOfApp(String appId) {
        return gson.toJson(new ListVmsDeployed(vmManager.getVmsOfApp(appId)));
    }

    /**
     * Deletes all the VMs that belong to a specific application.
     *
     * @param appId the ID of the application
     */
    public void deleteVmsOfApp(String appId) {
        vmManager.deleteVmsOfApp(appId);
    }

    /**
     * Returns a JSON object from a list of VM IDs.
     *
     * @param idsVms the list of VM IDs
     * @return the JSON object
     */
    private JsonObject getJsonResponseFromListOfVmsIds(List<String> idsVms) {
        JsonArray idsArrayJson = new JsonArray();
        for (String idVmDeployed: idsVms) {
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", idVmDeployed);
            idsArrayJson.add(idJson);
        }
        JsonObject result = new JsonObject();
        result.add("ids", idsArrayJson);
        return result;
    }

}
