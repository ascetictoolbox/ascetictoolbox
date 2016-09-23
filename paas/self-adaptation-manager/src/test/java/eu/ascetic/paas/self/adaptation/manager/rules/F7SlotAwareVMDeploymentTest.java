/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.paas.self.adaptation.manager.rules;

import es.bsc.vmmclient.models.Slot;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.SlotSolution;
import eu.ascetic.paas.self.adaptation.manager.utils.CombinationGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        
        return StringUtils.join(e, ",");
    }
    
    public Set<String> subsetSumBruteForce(String allowedSizes, int total){
        //System.out.println("subsetSumBruteForce " + allowedSizes + " - " + total);
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
    
    private Slot assignSlot(int numCpus, int diskGb,  int ramMb, List<Slot> slots) {
        for(int index = 0; index < slots.size(); index++){
            Slot slot = slots.get(index);
            if(slot.getFreeCpus() >= numCpus && 
               slot.getFreeDiskGb() >= diskGb && 
               slot.getFreeMemoryMb() >= ramMb){
                
                slot.setFreeCpus(slot.getFreeCpus() - numCpus);
                slot.setFreeDiskGb(slot.getFreeDiskGb() - diskGb);
                slot.setFreeMemoryMb(slot.getFreeMemoryMb() - ramMb);
                slots.set(index, slot);
                
                return new Slot(slot.getHostname(), numCpus,diskGb, ramMb);
            }
        }
        
        return null;
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
    
    private String getAllowedCpuSizes(int minCpu, int maxCpu) {
        String allowedCpuSizes = ""; 
        for(int i = minCpu; i <= maxCpu; i++){
            if(i > minCpu){ allowedCpuSizes += ","; }
            allowedCpuSizes += String.valueOf(i);
        }
        
        return allowedCpuSizes;
    }
    
    private List<SlotSolution> getSlotsSortedByConsolidationScore(List<Slot> slots, int totalCpusToAdd, int cpusPerHost, int minCpu, int maxCpu, int ramMb, int diskGb) {
        Set<String> combinations = subsetSumBruteForce(getAllowedCpuSizes(minCpu, maxCpu), totalCpusToAdd);
        List<SlotSolution> solutions = new ArrayList<>();
        
        for(String combination : combinations){
            List<Slot> results = new ArrayList<>();
            System.out.println("Trying to find place for " + combination.toString());
            String cpus[] = combination.split(",");
            List<Slot> slotsClone = this.cloneSlots(slots);
            for(int index = 0; index < cpus.length; index++){
                int numCpus = Integer.parseInt(cpus[index]);
                Slot slotAssigned = this.assignSlot(numCpus, diskGb, ramMb, slotsClone);
                if(slotAssigned != null) {
                   results.add(slotAssigned);
                }
            }
            
            for(Slot s : slotsClone){  System.out.println(s.getFreeCpus() + " - " + s.getHostname() ); }
            double consolidationScore = consolidationScore(slotsClone, cpusPerHost);
            System.out.println("Score: " + consolidationScore);
            solutions.add(new SlotSolution(consolidationScore, results));
        }
        
        return solutions;
    }
    
    @Test
    public void testAll(){
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostB", 4, 400, 4000));
        slots.add(new Slot("hostC", 8, 800, 8000));
        
        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 6;
        int cpusPerHost = 8;
        
        List<SlotSolution> solutions = getSlotsSortedByConsolidationScore(slots, totalCpusToAdd, cpusPerHost, minCpus, maxCpus, 1000, 100);
        Collections.sort(solutions, new Comparator<SlotSolution>() {
            @Override
            public int compare(SlotSolution a, SlotSolution b) {
                return a.getConsolidationScore() > b.getConsolidationScore() ? -1 : (a.getConsolidationScore() < b.getConsolidationScore()) ? 1 : 0;
            }
        });
        
        System.out.println(solutions);
    }
}
