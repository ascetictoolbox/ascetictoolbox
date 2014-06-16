package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;

public class HostFilter {

    public static ArrayList<HostInfo> filter(ArrayList<HostInfo> hosts, int minCpus, int minRamMb, int minDiskGb) {
        ArrayList<HostInfo> filteredHosts = new ArrayList<>();
        for (HostInfo host: hosts) {
            if (host.hasEnoughResources(minCpus, minRamMb, minDiskGb)) {
                filteredHosts.add(host);
            }
        }
        return filteredHosts;
    }

}