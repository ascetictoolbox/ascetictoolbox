package es.bsc.vmmanagercore.model;

import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ListVmsToBeEstimated {

    private List<VmToBeEstimated> vms;

    public void setVms(List<VmToBeEstimated> vms) {
        this.vms = vms;
    }

    public List<VmToBeEstimated> getVms() {
        return vms;
    }

}
