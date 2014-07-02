package es.bsc.vmmanagercore.model;

import es.bsc.vmmanagercore.monitoring.HostInfo;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {
    private Vm vm;
    private HostInfo host;

    public VmAssignmentToHost(Vm vm, HostInfo host) {
        this.vm = vm;
        this.host = host;
    }

    public Vm getVm() {
        return vm;
    }

    public HostInfo getHost() {
        return host;
    }
}
