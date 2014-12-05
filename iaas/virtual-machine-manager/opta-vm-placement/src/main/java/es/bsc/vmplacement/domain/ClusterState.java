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
import es.bsc.vmplacement.common.domain.AbstractPersistable;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.value.ValueRangeProvider;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.persistence.xstream.XStreamScoreConverter;

import java.util.*;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
@PlanningSolution
public class ClusterState extends AbstractPersistable implements Solution<Score> {

    private List<Vm> vms;
    private List<Host> hosts;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private Score score;

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

    /**
     * Checks whether a given host is idle.
     *
     * @param aHost the host
     * @return true if the host is idle, false otherwise.
     */
    public boolean hostIsIdle(Host aHost) {
        // Initialize all hosts to idle
        Map<Host, Boolean> idleHosts = new HashMap<>();
        for (Host host: hosts) {
            idleHosts.put(host, true);
        }

        // Check what hosts are not idle
        for (Vm vm: vms) {
            idleHosts.put(vm.getHost(), false);
        }

        return idleHosts.get(aHost);
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
            if (host.equals(vm.getHost())) {
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

    public double avgCpusAssignedPerHost() {
        int totalAssignedCpus = 0;
        for (Host host: hosts) {
            totalAssignedCpus += cpusAssignedInHost(host);
        }
        return totalAssignedCpus/hosts.size();
    }

    public int cpusAssignedInHost(Host host) {
        int result = 0;
        List<Vm> vms = getVmsDeployedInHost(host);
        for (Vm vm: vms) {
            result += vm.getNcpus();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Vm vm: getVms()) {
            sb.append(vm).append(" --> ").append(vm.getHost()).append("\n");
        }
        return sb.toString();
    }

}
