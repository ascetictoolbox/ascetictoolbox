import es.bsc.clopla.domain.Host;
import es.bsc.clopla.domain.Vm;
import es.bsc.clopla.placement.config.VmPlacementConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExperimentExecution {

    private final List<Vm> vms = new ArrayList<>();
    private final List<Host> hosts = new ArrayList<>();
    private final VmPlacementConfig vmPlacementConfig;

    public ExperimentExecution(List<Vm> vms, List<Host> hosts, VmPlacementConfig vmPlacementConfig) {
        this.vms.addAll(vms);
        this.hosts.addAll(hosts);
        this.vmPlacementConfig = vmPlacementConfig;
    }

    public List<Vm> getVms() {
        return Collections.unmodifiableList(vms);
    }

    public List<Host> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    public VmPlacementConfig getVmPlacementConfig() {
        return vmPlacementConfig;
    }

}
