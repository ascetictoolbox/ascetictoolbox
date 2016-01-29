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

package es.bsc.power_button_presser.strategies;

import es.bsc.power_button_presser.hostselectors.HostSelector;
import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.models.Host;

import java.util.List;

public class NBackupHostsStrategy implements PowerButtonStrategy {

    private final int desiredBackupHosts;
    private final HostSelector hostSelector;

    public NBackupHostsStrategy(int desiredBackupHosts, HostSelector hostSelector) {
        this.desiredBackupHosts = desiredBackupHosts;
        this.hostSelector = hostSelector;
    }

    @Override
    public List<Host> getPowerButtonsToPress(ClusterState clusterState) {
        if (currentNumberOfBackUpHosts(clusterState) > desiredBackupHosts) {
            return hostSelector.selectHostsToBeTurnedOff(
                    clusterState.getHostsWithoutVmsAndSwitchedOn(),
                    currentNumberOfBackUpHosts(clusterState) - desiredBackupHosts);
        }
        return hostSelector.selectHostsToBeTurnedOn(
                clusterState.getTurnedOffHosts(), 
                desiredBackupHosts - currentNumberOfBackUpHosts(clusterState));
    }

    /**
     * Returns the number of hosts that are on, but that do not contain any VMs, in the given cluster state.
     * @param clusterState the state of the cluster
     * @return the number of hosts that are turned on, but that are not hosting any VMs
     */
    private int currentNumberOfBackUpHosts(ClusterState clusterState) {
        return clusterState.getHostsWithoutVmsAndSwitchedOn().size();
    }
    
}
