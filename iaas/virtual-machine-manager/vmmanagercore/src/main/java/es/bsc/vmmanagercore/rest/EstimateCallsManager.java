package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.VmEstimate;
import es.bsc.vmmanagercore.model.VmToBeEstimated;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the REST calls that are related with the pricing and energy estimates.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class EstimateCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    public EstimateCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns the price and energy estimates for a set of VMs.
     *
     * @param vms the JSON document that contains the descriptions of the VMs
     * @return the JSON document that contains the price and energy estimates
     */
    public String getEstimates(String vms) {
        List<VmToBeEstimated> vmsToBeEstimated = getListOfVmsToBeEstimatedFromCallInput(vms);
        return getCallResponseFromListOfVmEstimates(vmManager.getVmEstimates(vmsToBeEstimated));
    }

    /**
     * Returns a list of VMs to be estimated from a JSON document.
     *
     * @param vms the JSON document
     * @return the list of VMs to be estimated
     */
    private List<VmToBeEstimated> getListOfVmsToBeEstimatedFromCallInput(String vms) {
        List<VmToBeEstimated> result = new ArrayList<>();
        JsonObject vmsJson = gson.fromJson(vms, JsonObject.class);
        for (JsonElement vmToBeEstimatedJson: vmsJson.getAsJsonArray("vms")) {
            result.add(gson.fromJson(vmToBeEstimatedJson, VmToBeEstimated.class));
        }
        return result;
    }

    /**
     * Returns the JSON document used in the response of the estimates call.
     *
     * @param vmEstimates a list of VM estimates
     * @return the JSON document
     */
    private String getCallResponseFromListOfVmEstimates(List<VmEstimate> vmEstimates) {
        JsonArray vmEstimatesJsonArray = new JsonArray();
        for (VmEstimate vmEstimate: vmEstimates) {
            vmEstimatesJsonArray.add(gson.toJsonTree(vmEstimate, VmEstimate.class));
        }
        JsonObject result = new JsonObject();
        result.add("estimates", vmEstimatesJsonArray);
        return result.toString();
    }

}
