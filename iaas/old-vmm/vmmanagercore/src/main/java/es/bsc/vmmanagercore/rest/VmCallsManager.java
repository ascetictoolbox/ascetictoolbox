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

package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.models.vms.ListVms;
import es.bsc.vmmanagercore.models.vms.ListVmsDeployed;

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
    VmmRestInputValidator inputValidator = new VmmRestInputValidator();

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
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
    public String deployVMs(String vms) throws CloudMiddlewareException {
        inputValidator.checkVmDescriptions(gson.fromJson(vms, JsonObject.class));
        List<String> idsVmsDeployed = vmManager.deployVms(gson.fromJson(vms, ListVms.class).getVms());
        return getJsonResponseFromListOfVmsIds(idsVmsDeployed).toString();
    }

    /**
     * Returns a JSON document that contains all the information about a specific VM.
     *
     * @param vmId the ID of the VM
     * @return the JSON document
     */
    public String getVm(String vmId) throws CloudMiddlewareException {
        inputValidator.checkVmExists(vmManager.existsVm(vmId));
        return gson.toJson(vmManager.getVm(vmId));
    }

    /**
     * Performs an action (reboot, suspend, etc.) on a VM.
     *
     * @param vmId the VM ID
     * @param actionJson the JSON document that contains the action to perform
     */
    public void changeStateVm(String vmId, String actionJson) throws CloudMiddlewareException {
        inputValidator.checkVmExists(vmManager.existsVm(vmId));
        JsonObject actionJsonObject = gson.fromJson(actionJson, JsonObject.class);
        inputValidator.checkJsonActionFormat(actionJsonObject);
        String action = actionJsonObject.get("action").getAsString();
        if (!action.equals("migrate")) {
            vmManager.performActionOnVm(vmId, actionJsonObject.get("action").getAsString());
        }
        else { // It is a migration. They are treated a bit differently than the other actions.
            JsonArray optionsArray = (JsonArray) actionJsonObject.get("options");
            JsonObject destinationHostNameObject = (JsonObject)optionsArray.get(0);
            String hostName = destinationHostNameObject.get("destinationHostName").getAsString();
            inputValidator.checkHostExists(vmManager.getHost(hostName) != null);
            vmManager.migrateVm(vmId, hostName);
        }
    }

    /**
     * Destroys a VM.
     *
     * @param vmId the VM ID
     */
    public void destroyVm(String vmId) throws CloudMiddlewareException {
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
