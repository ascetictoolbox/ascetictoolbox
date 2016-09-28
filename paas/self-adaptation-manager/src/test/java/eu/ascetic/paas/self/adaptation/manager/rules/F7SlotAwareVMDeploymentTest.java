/**
 *
 * Copyright (C) 2013-2014 Barcelona Supercomputing Center
 *
 * Licensed under the Apache License, Version 2.0 (the License); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package eu.ascetic.paas.self.adaptation.manager.rules;

import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Slot;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.SlotSolution;
import eu.ascetic.paas.self.adaptation.manager.utils.SlotAwareDeployer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class F7SlotAwareVMDeploymentTest {
    
    @Test
    public void testSlotAwareDeployment() {
        Map<String, Node> nodesTable = new HashMap<String, Node>();
        nodesTable.put("hostA", new Node("hostA", 8, 8*1024, 80, 6, 6*1024, 60, 0));
        nodesTable.put("hostB", new Node("hostB", 8, 8*1024, 80, 4, 4*1024, 40, 0));
        nodesTable.put("hostC", new Node("hostC", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostC", 8, 800, 8000));
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostB", 4, 400, 4000));

        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 6;
        
        SlotAwareDeployer deployer = new SlotAwareDeployer();
        List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, nodesTable, totalCpusToAdd, minCpus, maxCpus, 1024, 10);
        System.out.println(solutions);
    }
}
