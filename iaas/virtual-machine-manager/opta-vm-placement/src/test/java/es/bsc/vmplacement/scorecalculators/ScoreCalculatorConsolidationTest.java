package es.bsc.vmplacement.scorecalculators;

import es.bsc.vmplacement.domain.ClusterState;
import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorConsolidationTest {

    private ScoreCalculatorConsolidation scoreCalculatorConsolidation = new ScoreCalculatorConsolidation();

    @Test
    public void genericTestSoftAndHardScoreCalculator() {
        ClusterState clusterState = getTestClusterState();
        assertEquals(-4, scoreCalculatorConsolidation.calculateScore(clusterState).getHardScore(0));
        assertEquals(1, scoreCalculatorConsolidation.calculateScore(clusterState).getSoftScore(0));
        assertEquals(2, scoreCalculatorConsolidation.calculateScore(clusterState).getSoftScore(1));
        assertEquals(-275, scoreCalculatorConsolidation.calculateScore(clusterState).getSoftScore(2));
    }

    private ClusterState getTestClusterState() {
        // Create hosts
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host((long) 1, "1", 8, 8192, 8, false);
        Host host2 = new Host((long) 2, "2", 4, 4096, 4, false);
        Host host3 = new Host((long) 3, "3", 2, 2048, 2, false);
        Host host4 = new Host((long) 4, "4", 1, 1024, 1, true);
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        hosts.add(host4);

        // Create VMs
        List<Vm> vms = new ArrayList<>();
        Vm vm1 = new Vm.Builder((long) 1, 4, 4096, 4).build();
        Vm vm2 = new Vm.Builder((long) 2, 5, 5120, 5).build();
        Vm vm3 = new Vm.Builder((long) 3, 1, 5120, 1).build();
        vm1.setHost(host1);
        vm2.setHost(host1);
        vm3.setHost(host2);
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);

        // Build the solution
        ClusterState result = new ClusterState();
        result.setHosts(hosts);
        result.setVms(vms);
        return result;
    }

}
