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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class RecommendedPlan {

    private final Map<String, String> plan = new HashMap<>(); // VM ID -> Host ID

    public RecommendedPlan() { }

    public Map<String, String> getPlan() {
        return Collections.unmodifiableMap(plan);
    }

    public void addVmToHostAssignment(String vmId, String hostId) {
        plan.put(vmId, hostId);
    }

}
