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

package powerbuttonstrategies;

import models.ClusterState;
import models.Host;
import vmm.VmmClient;

import java.util.Collections;
import java.util.List;

public class NBackupHostsStrategy implements PowerButtonStrategy {

    private final VmmClient vmmClient;
    private final int desiredBackupHosts;

    public NBackupHostsStrategy(VmmClient vmmClient, int desiredBackupHosts) {
        this.vmmClient = vmmClient;
        this.desiredBackupHosts = desiredBackupHosts;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        if (currentNumberOfBackUpHosts(clusterState) > desiredBackupHosts) {
            turnOff(clusterState.getHostsWithoutVmsAndSwitchedOn(), 
                    currentNumberOfBackUpHosts(clusterState) - desiredBackupHosts);
        }
        else {
            turnOn(clusterState.getTurnedOffHosts(),
                    desiredBackupHosts - currentNumberOfBackUpHosts(clusterState));
        }
    }

    /**
     * Returns the number of hosts that are on, but that do not contain any VMs, in the given cluster state.
     * @param clusterState the state of the cluster
     * @return the number of hosts that are turned on, but that are not hosting any VMs
     */
    private int currentNumberOfBackUpHosts(ClusterState clusterState) {
        return clusterState.getHostsWithoutVmsAndSwitchedOn().size();
    }

    /**
     * Shuffles the given list of hosts and turns off a given number of them starting from the beginning of the
     * shuffled list.
     * If the number of host to be turned off is greater than the size of the list of hosts received, all of them are
     * turned off.
     * @param hostCandidatesToBeTurnedOff the list of hosts that can be turned off
     * @param numberOfHostsToBeTurnedOff the number of hosts to be turned off
     */
    private void turnOff(List<Host> hostCandidatesToBeTurnedOff, int numberOfHostsToBeTurnedOff) {
        int hostsToTurnOff = Math.min(numberOfHostsToBeTurnedOff, hostCandidatesToBeTurnedOff.size());
        Collections.shuffle(hostCandidatesToBeTurnedOff);
        for (int i = 0; i < hostsToTurnOff; ++i) {
            vmmClient.pressPowerButton(hostCandidatesToBeTurnedOff.get(i).getHostname());
        }
    }

    /**
     * Shuffles the given list of hosts and turns on a given number of them starting from the beginning of the
     * shuffled list.
     * If the number of host to be turned on is greater than the size of the list of hosts received, all of them are
     * turned on.
     * @param hostCandidatesToBeTurnedOn the list of host that can be turned on
     * @param numberOfHostsToBeTurnedOn the number of hosts to be turned on
     */
    private void turnOn(List<Host> hostCandidatesToBeTurnedOn, int numberOfHostsToBeTurnedOn) {
        int hostsToTurnOn = Math.min(numberOfHostsToBeTurnedOn, hostCandidatesToBeTurnedOn.size());
        Collections.shuffle(hostCandidatesToBeTurnedOn);
        for (int i = 0; i < hostsToTurnOn; ++i) {
            vmmClient.pressPowerButton(hostCandidatesToBeTurnedOn.get(i).getHostname());
        }
    }
    
}
