package es.bsc.vmmanagercore.scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;

public class HostFilterTest {

    @Test
    public void filter() {
        // Create fake hosts {name, totalCpus, totalRamMb, totalDiskGb,
        // usedCpus, usedRamMb, usedDiskGb}
        ArrayList<HostInfo> hosts = new ArrayList<>();
        hosts.add(new HostInfoFake("host1", 4, 4096, 4, 1, 1024, 1)); // OK
        hosts.add(new HostInfoFake("host2", 4, 4096, 4, 3, 1024, 1)); // not enough CPUs
        hosts.add(new HostInfoFake("host3", 4, 4096, 4, 1, 3072, 1)); // not enough RAM
        hosts.add(new HostInfoFake("host4", 4, 4096, 4, 1, 1024, 3)); // not enough disk

        // Filter hosts and check that only host1 is in the result
        ArrayList<HostInfo> filteredHosts = HostFilter.filter(hosts, 2, 2048, 2);
        assertTrue(filteredHosts.size() == 1 && filteredHosts.get(0).getHostname().equals("host1"));
    }

}