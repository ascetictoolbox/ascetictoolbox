/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.paas.self.adaptation.manager.rules;

import es.bsc.vmmclient.models.Slot;
import eu.ascetic.paas.self.adaptation.manager.utils.CombinationGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * 
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center 
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
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class F7SlotAwareVMDeploymentTest {
    private static final Logger logger = Logger.getLogger("F7SlotAwareVMDeploymentTest");
    
    private String extendCombination(String[] e) {
        if(e.length == 1){
            return e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0];
        }
        else if(e.length == 2){
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",");
        }
        else if(e.length == 3){
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") ;
        }
        else if(e.length == 4){
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") ;
        }
        
        System.out.println("Too many combinations!");
        return "";
    }
    
    public Set<String> subsetSumBruteForce(String allowedSizes, int total){
        
        List<String> combinations = CombinationGenerator.getCombinations(allowedSizes);
        Set<String> results = new HashSet<>();
        
        for(String combination : combinations){
            String e[] = combination.split(",");
            List<String> subcombinations = CombinationGenerator.getCombinations(extendCombination(e));
            
            //System.out.println( "combinations:" );
            for(String comb : subcombinations){
                String subset[] = comb.split(",");
                
                int sum = 0;
                for (String s : subset){
                    sum += Integer.parseInt(s);
                }
                
                if(sum == total && !results.contains(comb.toString())){
                    results.add(comb.toString());
                    System.out.println( comb.toString() );
                }
            }
        }
        
        return results;
    }
    
    private void assignSlot(int numCpus, List<Slot> slots) {
        for(int index = 0; index < slots.size(); index++){
            Slot slot = slots.get(index);
            if(slot.getFreeCpus() >= numCpus){
                slot.setFreeCpus(slot.getFreeCpus() - numCpus);
                slots.set(index, slot);
                return;
            }
        }
    }
    
    public List<Slot> cloneSlots(List<Slot> slots) {
        List<Slot> clonedSlots = new ArrayList<>();
        
        for(Slot s : slots){
            clonedSlots.add(
              new Slot(s.getHostname(), s.getFreeCpus(), s.getFreeDiskGb(), s.getFreeMemoryMb())
            );
        }
        
        return clonedSlots;
    }
    
    public int consolidationScore(List<Slot> slots, int numAllCpus) {
        int consolidationScore = 0;
        for(Slot s : slots){ 
            if(s.getFreeCpus() == numAllCpus){
                consolidationScore++;
            }
        }
        
        return consolidationScore;
    }
    
    @Test
    public void testAll(){
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostB", 4, 400, 4000));
        slots.add(new Slot("hostC", 8, 800, 8000));
        
        String allowedSizes = "2,3,4";
        int total = 6;
        int cpusPerHost = 8;
        
        Set<String> results = subsetSumBruteForce(allowedSizes, total);
        
        for(String result : results){
            System.out.println("Trying to find place for " + result.toString());
            String c[] = result.split(",");
            List<Slot> slotsClone = this.cloneSlots(slots);
            for(int index = 0; index < c.length; index++){
                int numCpus = Integer.parseInt(c[index]);
                this.assignSlot(numCpus, slotsClone);
            }
            
            for(Slot s : slotsClone){  System.out.println(s.getFreeCpus() + " - " + s.getHostname() ); }
            System.out.println("Score: " + this.consolidationScore(slotsClone, cpusPerHost));
        }
    }
}
