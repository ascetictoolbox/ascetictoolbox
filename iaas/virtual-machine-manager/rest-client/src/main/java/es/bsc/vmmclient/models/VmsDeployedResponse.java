package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import es.bsc.demiurge.core.models.vms.VmDeployed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VmsDeployedResponse {

    private final List<VmDeployed> vms = new ArrayList<>();

    public VmsDeployedResponse(List<VmDeployed> vms) {
        this.vms.addAll(vms);
    }

    public List<VmDeployed> getVms() {
        return Collections.unmodifiableList(vms);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vms", vms)
                .toString();
    }

}
