package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;
import es.bsc.vmmanagercore.monitoring.HostInfo;

public class HostFilter {

    public static ArrayList<HostInfo> filter(ArrayList<HostInfo> hosts, int minCpus,
            int minRamMb, int minDiskGb) {
        ArrayList<HostInfo> filteredHosts = new ArrayList<>();
        for (HostInfo host: hosts) {
            if (host.hasEnoughResources(minCpus, minRamMb, minDiskGb)) {
                filteredHosts.add(host);
            }
        }
        return filteredHosts;
    }

}