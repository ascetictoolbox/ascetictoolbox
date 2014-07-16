package es.bsc.vmmanagercore.model;

import java.util.List;

/**
 * List of VMs
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: At least for now, this class is only useful to make easier the conversion from JSON using Gson.
public class ListVms {

    private List<Vm> vms;

    public ListVms(List<Vm> vms) {
        this.vms = vms;
    }

    public List<Vm> getVms() {
        return vms;
    }

}
