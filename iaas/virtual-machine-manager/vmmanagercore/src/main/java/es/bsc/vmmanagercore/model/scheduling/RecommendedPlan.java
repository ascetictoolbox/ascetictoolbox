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

package es.bsc.vmmanagercore.model.scheduling;

import java.util.*;

/**
 * Describes a recommended plan.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class RecommendedPlan {

    private final Map<String, String> plan = new HashMap<>(); // VM ID -> Host ID

    /**
     * Class constructor.
     */
    public RecommendedPlan() { }

    /**
     * Returns the plan.
     *
     * @return the plan
     */
    public Map<String, String> getPlan() {
        return Collections.unmodifiableMap(plan);
    }

    /**
     * Adds a VM assignation to host to the plan.
     *
     * @param vmId the ID of the VM
     * @param hostId the ID of the host
     */
    public void addVmToHostAssignment(String vmId, String hostId) {
        plan.put(vmId, hostId);
    }

    /**
     * Gets the VMPlacements described in the plan.
     *
     * @return the VMPlacements
     */
    public VmPlacement[] getVMPlacements() {
        List<VmPlacement> vmPlacements = new ArrayList<>();
        for (Map.Entry<String, String> entry: plan.entrySet()) {
            vmPlacements.add(new VmPlacement(entry.getKey(), entry.getValue()));
        }
        return vmPlacements.toArray(new VmPlacement[vmPlacements.size()]);
    }

}
