package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.ListVmsToBeEstimated;

/**
 * This class implements the REST calls that are related with the pricing and energy estimates.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
    public EstimatesCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns the price and energy estimates for a set of VMs.
     *
     * @param vms the JSON document that contains the descriptions of the VMs
     * @return the JSON document that contains the price and energy estimates
     */
    public String getEstimates(String vms) {
        ListVmsToBeEstimated listVmsToBeEstimated = gson.fromJson(vms, ListVmsToBeEstimated.class);
        return gson.toJson(vmManager.getVmEstimates(listVmsToBeEstimated.getVms()));
    }

}
