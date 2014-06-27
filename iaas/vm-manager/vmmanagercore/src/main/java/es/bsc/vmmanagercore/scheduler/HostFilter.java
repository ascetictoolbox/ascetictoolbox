package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Host filter. Selects hosts that meet a certain criteria about free CPUs, RAM, and disk.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostFilter {

    /**
     * From a list of hosts, returns the ones that have enough CPUs, RAM, and disk available.
     *
     * @param hosts the lists of hosts
     * @param minCpus the minimum number of free CPUs
     * @param minRamMb the minimum amount of free RAM (MB)
     * @param minDiskGb the minimum amount of free disk (GB)
     * @return hosts from the input that meet the CPU, RAM, and disk requirements
     */
    public static List<HostInfo> filter(List<HostInfo> hosts, int minCpus, int minRamMb, int minDiskGb) {
        List<HostInfo> filteredHosts = new ArrayList<>();
        for (HostInfo host: hosts) {
            if (host.hasEnoughResources(minCpus, minRamMb, minDiskGb)) {
                filteredHosts.add(host);
            }
        }
        return filteredHosts;
    }

}