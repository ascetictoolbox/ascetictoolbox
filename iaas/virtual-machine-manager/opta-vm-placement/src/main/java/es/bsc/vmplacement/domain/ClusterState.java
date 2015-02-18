/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmplacement.domain;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.value.ValueRangeProvider;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.persistence.xstream.XStreamScoreConverter;

import java.util.*;

/**
 * This class defines the state of the cluster at any given time. 
 * The state contains the hosts of the cluster and the VMs that are deployed or need to be deployed. 
 * The state also specifies, for each VM, the host where it should be deployed.
 *  
 * @author David Ortiz (david.ortiz@bsc.es)
 */
@PlanningSolution
public class ClusterState extends AbstractPersistable implements Solution<Score> {

    private List<Vm> vms;
    private List<Host> hosts;

    public ClusterState () { } // OptaPlanner needs no arg constructor to clone
    
    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private Score score;

    /**
     * Checks whether a given host is idle.
     *
     * @param host the host
     * @return true if the host is idle, false otherwise.
     */
    public boolean hostIsIdle(Host host) {
        return getIdleHosts().contains(host);
    }

    /**
     * Counts the hosts that do not have any VMs assigned.
     *
     * @return the number of hosts that do not have any VMs assigned.
     */
    public int countIdleHosts() {
        return getIdleHosts().size();
    }

    /**
     * Counts the hosts that have at least one VM assigned.
     *
     * @return the number of hosts that have at least one VM assigned.
     */
    public int countNonIdleHosts() {
        return hosts.size() - getIdleHosts().size();
    }

    /**
     * Returns the IDs of the apps that have some VMs deployed in a host.
     *
     * @param host the host
     * @return the list of IDs of the apps
     */
    public List<String> getIdsOfAppsDeployedInHost(Host host) {
        List<String> result = new ArrayList<>();
        for (Vm vm: vms) {
            if (host.equals(vm.getHost()) && vm.getAppId() != null) {
                result.add(vm.getAppId());
            }
        }
        return result;
    }

    /**
     * Returns the VMs deployed in a host.
     *
     * @param host the host
     * @return the list of VMs
     */
    public List<Vm> getVmsDeployedInHost(Host host) {
        List<Vm> result = new ArrayList<>();
        for (Vm vm: vms) {
            if (host.equals(vm.getHost())) {
                result.add(vm);
            }
        }
        return result;
    }

    /**
     * Calculates the avg number of CPUs assigned per host.
     *
     * @return the avg number of CPUs assigned per host
     */
    public double avgCpusAssignedPerHost() {
        return calculateTotalCpusAssigned()/hosts.size();
    }

    /**
     * Returns the number of CPUs assigned in a specific host.
     *
     * @param host the host
     * @return the number of CPUs
     */
    public int cpusAssignedInHost(Host host) {
        int result = 0;
        for (Vm vm: getVmsDeployedInHost(host)) {
            result += vm.getNcpus();
        }
        return result;
    }

    /**
     * Returns the total unused CPU % of the cluster.
     * It is calculated as follows. The unused CPU % of a host is (unused_cpus/total_cpus).
     * The total unused CPU % of the cluster is simply the sum for all the hosts.
     *
     * @return the total unused CPU % of the cluster.
     */
    public int calculateCumulativeUnusedCpuPerc() {
        double cumulativeUnusedCpuPerc = 0;
        for (Host host: hosts) {
            cumulativeUnusedCpuPerc += calculateUnusedCpuRatio(host);
        }
        return (int)(cumulativeUnusedCpuPerc*100);
    }

    /**
     * Returns the stddev of the CPUs assigned per host.
     *
     * @return the stddev of CPUs assigned per host
     */
    public double calculateStdDevCpusAssignedPerHost() {
        return Math.sqrt(calculateVarianceCpusAssignedPerHost());
    }

    /**
     * Counts the number of hosts that are switched off.
     *
     * @return the number of hosts that are switched off
     */
    public int countOffHosts() {
        int result = 0;
        for (Host host: hosts) {
            if (host.wasOffInitiallly() && getVmsDeployedInHost(host).size() == 0) {
                ++result;
            }
        }
        return result;
    }

    /**
     * Returns the number of VM migrations needed to go from this cluster state to the given one.
     *  
     * @param destinyClusterState the destiny cluster state
     * @return the number of VM migrations needed
     */
    public int countVmMigrationsNeeded(ClusterState destinyClusterState) {
        int result = 0;
        for (Vm vm: vms) {
            if (!vm.isInTheSameHost(destinyClusterState.getVmById(vm.getId()))) {
                ++result;
            }
        }
        return result;
    }

    /**
     * Gets a VM by ID.
     *  
     * @param id the VM ID
     * @return the VM or null if it does not exist
     */
    public Vm getVmById(long id) {
        for (Vm vm: vms) {
            if (vm.getId() == id) {
                return vm;
            }
        }
        return null;
    }
    
    @PlanningEntityCollectionProperty
    public List<Vm> getVms() {
        return vms;
    }

    public void setVms(List<Vm> vms) {
        this.vms = vms;
    }

    @ValueRangeProvider(id = "hostRange")
    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void setScore(Score score) {
        this.score = score;
    }

    @Override
    public Collection<?> getProblemFacts() {
        return new ArrayList<Object>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Vm vm: getVms()) {
            sb.append(vm).append(" --> ").append(vm.getHost()).append("\n");
        }
        return sb.toString();
    }

    private List<Host> getIdleHosts() {
        // Initialize all hosts to idle
        Map<Host, Boolean> idleHosts = new HashMap<>();
        for (Host host: hosts) {
            idleHosts.put(host, true);
        }

        // Check what hosts are not idle
        for (Vm vm: vms) {
            idleHosts.put(vm.getHost(), false);
        }

        // Return result
        List<Host> result = new ArrayList<>();
        for (Map.Entry<Host, Boolean> idleHostsEntry: idleHosts.entrySet()) {
            if (idleHostsEntry.getValue()) {
                result.add(idleHostsEntry.getKey());
            }
        }
        return result;
    }

    private int calculateTotalCpusAssigned() {
        int result = 0;
        for (Host host: hosts) {
            result += cpusAssignedInHost(host);
        }
        return result;
    }

    private double calculateVarianceCpusAssignedPerHost() {
        double temp = 0;
        for (Host host: hosts) {
            temp += Math.pow((avgCpusAssignedPerHost() - cpusAssignedInHost(host)), 2);
        }
        return temp/(hosts.size());
    }

    private double calculateUnusedCpuRatio(Host host) {
        double unusedPerc = (double)(host.getNcpus() - cpusAssignedInHost(host))/(host.getNcpus());
        return unusedPerc > 0 ? unusedPerc : 0; // If a host is overbooked simply return 0
    }
    
}
