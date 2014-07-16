package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.SchedulingAlgorithm;

import javax.ws.rs.WebApplicationException;

/**
 * This class implements the REST calls that are related with scheduling algorithms.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulingAlgorithmCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
    public SchedulingAlgorithmCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns a JSON document that contains the scheduling algorithms supported.
     *
     * @return the JSON document
     */
    public String getSchedulingAlgorithms() {
        JsonArray schedAlgsArrayJson = new JsonArray();
        for (SchedulingAlgorithm schedAlg: vmManager.getAvailableSchedulingAlgorithms()) {
            JsonObject schedAlgJson = new JsonObject();
            schedAlgJson.addProperty("name", schedAlg.getName());
            schedAlgsArrayJson.add(schedAlgJson);
        }
        JsonObject result = new JsonObject();
        result.add("scheduling_algorithms", schedAlgsArrayJson);
        return result.toString();
    }

    /**
     * Returns a JSON document that contains the scheduling algorithms being used now.
     *
     * @return the JSON document
     */
    public String getCurrentSchedulingAlgorithm() {
        JsonObject result = new JsonObject();
        result.addProperty("name", vmManager.getCurrentSchedulingAlgorithm().getName());
        return result.toString();
    }

    /**
     * Sets the scheduling algorithm.
     *
     * @param schedAlgToSet the scheduling algorithm
     */
    public void setSchedulingAlgorithm(String schedAlgToSet) {
        // Get the algorithm
        JsonObject jsonObject = gson.fromJson(schedAlgToSet, JsonObject.class);
        if (jsonObject.get("algorithm") == null) { // Invalid JSON format. Throw error.
            throw new WebApplicationException(400);
        }
        String algorithm = jsonObject.get("algorithm").getAsString();

        SchedulingAlgorithm schedulingAlg;
        if (algorithm.equals(SchedulingAlgorithm.CONSOLIDATION.getName())) {
            schedulingAlg = SchedulingAlgorithm.CONSOLIDATION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.COST_AWARE.getName())) {
            schedulingAlg = SchedulingAlgorithm.COST_AWARE;
        }
        else if (algorithm.equals(SchedulingAlgorithm.DISTRIBUTION.getName())) {
            schedulingAlg = SchedulingAlgorithm.DISTRIBUTION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.ENERGY_AWARE.getName())) {
            schedulingAlg = SchedulingAlgorithm.ENERGY_AWARE;
        }
        else if (algorithm.equals(SchedulingAlgorithm.GROUP_BY_APP.getName())) {
            schedulingAlg = SchedulingAlgorithm.GROUP_BY_APP;
        }
        else if (algorithm.equals(SchedulingAlgorithm.RANDOM.getName())) {
            schedulingAlg = SchedulingAlgorithm.RANDOM;
        }
        else { // Invalid algorithm. Throw error.
            throw new WebApplicationException(400);
        }
        vmManager.setSchedulingAlgorithm(schedulingAlg);
    }


}
