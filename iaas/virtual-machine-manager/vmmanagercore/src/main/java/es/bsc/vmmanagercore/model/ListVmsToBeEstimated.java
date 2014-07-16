package es.bsc.vmmanagercore.model;

import java.util.List;

/**
 * List of VMs to be estimated.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: At least for now, this class is only useful to make easier the conversion from JSON using Gson.
public class ListVmsToBeEstimated {

    private List<VmToBeEstimated> vms;

    public void setVms(List<VmToBeEstimated> vms) {
        this.vms = vms;
    }

    public List<VmToBeEstimated> getVms() {
        return vms;
    }

}
