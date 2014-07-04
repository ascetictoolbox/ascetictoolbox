package es.bsc.vmmanagercore.model;

import es.bsc.vmmanagercore.monitoring.Host;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {
    private Vm vm;
    private Host host;

    public VmAssignmentToHost(Vm vm, Host host) {
        this.vm = vm;
        this.host = host;
    }

    public Vm getVm() {
        return vm;
    }

    public Host getHost() {
        return host;
    }
}
