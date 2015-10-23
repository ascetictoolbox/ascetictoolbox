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

package es.bsc.vmmanagercore.models.scheduling;

/**
 * The scheduling algorithms that can be applied in the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public enum SchedAlgorithmNameEnum {

    CONSOLIDATION("consolidation"), COST_AWARE("costAware"), DISTRIBUTION("distribution"),
    ENERGY_AWARE("energyAware"), GROUP_BY_APP("groupByApp"), RANDOM("random");

    private String name;

    private SchedAlgorithmNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SchedAlgorithmNameEnum fromName(String name) {
        switch (name) { // There can be only one, so get the elem with index 0
            case "consolidation":
                return SchedAlgorithmNameEnum.CONSOLIDATION;
            case "costAware":
                return SchedAlgorithmNameEnum.COST_AWARE;
            case "distribution":
                return SchedAlgorithmNameEnum.DISTRIBUTION;
            case "energyAware":
                return SchedAlgorithmNameEnum.ENERGY_AWARE;
            case "groupByApp":
                return SchedAlgorithmNameEnum.GROUP_BY_APP;
            case "random":
                return SchedAlgorithmNameEnum.RANDOM;
            default:
                // Presumably Throws an exception
                return SchedAlgorithmNameEnum.valueOf(name);
        }
    }
}