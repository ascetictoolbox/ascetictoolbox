package es.bsc.vmmanagercore.manager.components;

import es.bsc.vmmanagercore.monitoring.hosts.Host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostsManager {
    
    private final List<Host> hosts = new ArrayList<>();
    
    public HostsManager(List<Host> hosts) {
        this.hosts.addAll(hosts);
    }

    /**
     * Returns the hosts of the infrastructure.
     *
     * @return the list of hosts
     */
    public List<Host> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    /**
     * Returns a host by hostname.
     *
     * @param hostname the hostname
     * @return the host
     */
    public Host getHost(String hostname) {
        for (Host host: hosts) {
            if (hostname.equals(host.getHostname())) {
                return host;
            }
        }
        return null;
    }

    /**
     * Simulates pressing the power button of a host
     * @param hostname the hostname
     */
    public void pressHostPowerButton(String hostname) {
        for (Host host: hosts) {
            if (hostname.equals(host.getHostname())) {
                host.pressPowerButton();
            }
        }
    }
    
}
