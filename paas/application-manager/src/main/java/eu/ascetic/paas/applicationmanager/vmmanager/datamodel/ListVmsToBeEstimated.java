package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

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
