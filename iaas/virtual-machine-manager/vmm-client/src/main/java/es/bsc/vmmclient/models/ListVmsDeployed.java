package es.bsc.vmmclient.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListVmsDeployed {

    List<VmDeployed> vms = new ArrayList<>();

    public ListVmsDeployed(List<VmDeployed> vms) {
        this.vms.addAll(vms);
    }

    public List<VmDeployed> getVms() {
        return Collections.unmodifiableList(vms);
    }

}
