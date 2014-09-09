/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.ascetic;

import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.util.CoreManager;
import java.util.HashMap;

public class VM {

    private static final long UPDATE_FREQ = 30000;
    private long lastUpdate = 0l;

    private final static int[] implCount = new int[CoreManager.coreCount];

    static {
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            implCount[coreId] = CoreManager.getCoreImplementations(coreId).length;
        }
    }

    private final static HashMap<String, ResourceDescription> componentDescription = Configuration.getComponentDescriptions();

    private final String componentId;
    private final String providerId;
    private final String IPv4;
    private final ResourceDescription description;

    private int[][] consumption;

    public VM(String IPv4, String providerID, String componentId) {
        this.IPv4 = IPv4;
        this.providerId = providerID;
        this.componentId = componentId;
        System.out.println("ComponentID:"+componentId);
        ResourceDescription rd= componentDescription.get(componentId);
        System.out.println("Description: "+rd);
        description = new ResourceDescription(rd);
        description.setName(IPv4);
        consumption = new int[CoreManager.coreCount][];
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            consumption[coreId] = new int[implCount[coreId]];
        }
        updateConsumptions();
    }

    public String getIPv4() {
        return IPv4;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getComponentId() {
        return componentId;
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public int[] getConsumptions(int coreId) {
        return consumption[coreId];
    }

    public void updateConsumptions() {
        if (System.currentTimeMillis() - lastUpdate > UPDATE_FREQ) {
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                for (int implId = 0; implId < implCount[coreId]; implId++) {
                    consumption[coreId][implId] = AppManager.getConsumption(IPv4, coreId + "_" + implId);
                }

            }
            lastUpdate=System.currentTimeMillis();
        }
    }

}
