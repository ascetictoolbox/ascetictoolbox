package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import java.util.List;

/**
 * List of VMs deployed.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: At least for now, this class is only useful to make easier the conversion from JSON using Gson.
public class ListVmsDeployed {

    private List<VmDeployed> vms;

    public ListVmsDeployed(List<VmDeployed> vms) {
        this.vms = vms;
    }

    public List<VmDeployed> getVms() {
        return vms;
    }

}
