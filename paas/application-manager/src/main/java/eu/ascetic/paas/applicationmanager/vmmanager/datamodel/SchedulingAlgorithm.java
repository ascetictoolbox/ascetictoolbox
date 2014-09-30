package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojoa@atos.net 
 *
 * The scheduling algorithms that can be applied in the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public enum SchedulingAlgorithm {

    CONSOLIDATION("consolidation"), COST_AWARE("costAware"), DISTRIBUTION("distribution"),
    ENERGY_AWARE("energyAware"), GROUP_BY_APP("groupByApp"), RANDOM("random");

    private String name;

    private SchedulingAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}