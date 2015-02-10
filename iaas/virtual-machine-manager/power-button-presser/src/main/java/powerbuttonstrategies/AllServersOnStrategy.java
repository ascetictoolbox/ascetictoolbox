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

import java.util.List;

public class AllServersOnStrategy implements PowerButtonStrategy {

    private final VmmClient vmmClient;

    public AllServersOnStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        pressPowerButton(clusterState.getTurnedOffHosts());
    }
    
    private void pressPowerButton(List<Host> hosts) {
        for (Host host: hosts) {
            vmmClient.pressPowerButton(host.getHostname());
        }
    }

}
