/**
 * Copyright 2016 Barcelona Super Computing Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.utils;

import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Slot;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.SlotSolution;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class SlotAwareDeployer {
    String provider;
    
    /**
     * Empty constructor.
     */
    public SlotAwareDeployer(){
        this.provider = "providerA";
    }
    
    /**
     * 
     * @param provider the provider of this deployer.
     */
    public SlotAwareDeployer(String provider){
        this.provider = provider;
    }
    
    /**
     * Calculates subset of sums.
     * TODO: Find a most efficient way of calculate this.
     * 
     * @param allowedSizes string that represents allowed sizes i.e. 2,3,4
     * @param total the total expected from the sums
     * @return all sum subset combinations
     */
    public Set<String> subsetSumBruteForce(String allowedSizes, int total) {
        //System.out.println("subsetSumBruteForce " + allowedSizes + " - " + total);
        List<String> combinations = CombinationGenerator.getCombinations(allowedSizes);
        Set<String> results = new HashSet<>();

        for (String combination : combinations) {
            String e[] = combination.split(",");
            List<String> subcombinations = CombinationGenerator.getCombinations(extendCombination(e));

            //System.out.println( "combinations:" );
            for (String comb : subcombinations) {
                String subset[] = comb.split(",");

                int sum = 0;
                for (String s : subset) {
                    sum += Integer.parseInt(s);
                }

                if (sum == total && !results.contains(comb.toString())) {
                    results.add(comb.toString());
                    //System.out.println(comb.toString());
                }
            }
        }

        return results;
    }

    /**
     * Assigns a slot to a given size.
     * 
     * @param numCpus number of cpus
     * @param diskGb gb of disk
     * @param ramMb ram memory
     * @param slots slots to assign
     * @return the assigned slot
     */
    private Slot assignSlot(int numCpus, int diskGb, int ramMb, List<Slot> slots) {
        for (int index = 0; index < slots.size(); index++) {
            Slot slot = slots.get(index);
            if (slot.getFreeCpus() >= numCpus
                    && slot.getFreeDiskGb() >= diskGb
                    && slot.getFreeMemoryMb() >= ramMb) {

                slot.setFreeCpus(slot.getFreeCpus() - numCpus);
                slot.setFreeDiskGb(slot.getFreeDiskGb() - diskGb);
                slot.setFreeMemoryMb(slot.getFreeMemoryMb() - ramMb);
                slots.set(index, slot);

                return new Slot(slot.getHostname(), numCpus, diskGb, ramMb);
            }
        }

        return null;
    }

    /**
     * Function that clones slots.
     * 
     * @param slots the slots to clone
     * @return a clone of the slots
     */
    private List<Slot> cloneSlots(List<Slot> slots) {
        List<Slot> clonedSlots = new ArrayList<>();

        for (Slot s : slots) {
            clonedSlots.add(
                    new Slot(s.getHostname(), s.getFreeCpus(), s.getFreeDiskGb(), s.getFreeMemoryMb())
            );
        }

        return clonedSlots;
    }
    
    /**
     * Sort slots by consolidation criteria.
     * 
     * @param slots the slots to sort
     */
    private void sortSlotsByConsolidation(List<Slot> slots) {
        Collections.sort(slots, new Comparator<Slot>() {
            @Override
            public int compare(Slot a, Slot b) {
                if (a.getFreeCpus() > b.getFreeCpus()) {
                    return 1;
                }

                if (a.getFreeCpus() < b.getFreeCpus()) {
                    return -1;
                }

                return 0;
            }
        });
    }

    /**
     * Calculates consolidationScore for a given solution and configuration of nodes.
     * When we find a slot with all cpus available we increase score + 1.
     * 
     * @param slots a list of slots with a proposed solution.
     * @param nodes information about the nodes to extract total cpus per host.
     * @return the consolidation score
     */
    private double consolidationScore(List<Slot> slots, Map<String,Node> nodes) {
        double consolidationScore = 0.0;
        for (Slot s : slots) {
            if (s.getFreeCpus() == nodes.get(s.getHostname()).getTotalCpus()) {
                consolidationScore += 1.0;
            }
            else if (s.getFreeCpus() == 0) {
                consolidationScore += 0.1;
            }
        }

        return consolidationScore;
    }

    /**
     * Helper to calculate all combinations.
     * 
     * @param e combination to extend.
     * @return the extended combination
     */
    private String extendCombination(String[] e) {
        if (e.length == 1) {
            return e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0] + "," + e[0];
        } else if (e.length == 2) {
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",");
        } else if (e.length == 3) {
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",") + "," + StringUtils.join(e, ",");
        } else if (e.length == 4) {
            return StringUtils.join(e, ",") + "," + StringUtils.join(e, ",");
        }

        return StringUtils.join(e, ",");
    }

    /**
     * Calculate string with allowed sizes i.e. from (2,4) it would be 2,3,4
     * 
     * @param minCpu min of cpus
     * @param maxCpu max of cpus
     * @return a string with all allowed cpu sizes i.e. 2,3,4
     */
    private String getAllowedCpuSizes(int minCpu, int maxCpu) {
        String allowedCpuSizes = "";
        for (int i = minCpu; i <= maxCpu; i++) {
            if (i > minCpu) {
                allowedCpuSizes += ",";
            }
            allowedCpuSizes += String.valueOf(i);
        }

        return allowedCpuSizes;
    }
    
    /**
     * Provides all combinations of a list. 
     * It's an approximation since it uses Collections.shuffle internally
     * TODO: Find a most efficient way of calculate this.
     * 
     * @param slots list of slots you want to combine.
     * @return a map with all the possible combinations of this list.
     */
    private Map<Integer, List<Slot>> getAllListOrders(List<Slot> slots) {
        Map<Integer, List<Slot>> out = new HashMap<>();
        
        List<Slot> slotsClone = this.cloneSlots(slots);
        int numTries = 100*slots.size();
        System.out.println("numTries( 100*(" + slots.size() + ") ) = " + numTries);
        for(int i = 0; i < numTries; i++){
            Collections.shuffle(slotsClone);
            //System.out.println("slotsClone("  + slotsClone.hashCode() + ") = " + slotsClone.toString());
            if(!out.containsKey(slotsClone.hashCode())){
                System.out.println("Added to out.");
                //out.put(slotsClone.hashCode(), this.cloneSlots(slotsClone));
            }
        }
        
        /*
        for(Integer k : out.keySet()){
            System.out.println(k);
            for(Slot s : out.get(k)){
                System.out.println(s.getHostname());
            }
            
            System.out.println("====");
        }
        */
        
        System.out.println(out.toString());
        return out;
    }

    /**
     * Returns a list of SolutionSlot sorted by consolidation criteria.
     * 
     * @param slots slots given by VMM.
     * @param nodes information about physical nodes given by VMM.
     * @param totalCpusToAdd number of cpus that you want to add.
     * @param minCpu min of cpus allowed.
     * @param maxCpu max of cpus allowed.
     * @param ramMb memory ram allowed.
     * @param diskGb gb of disk allowed.
     * @return a list with all slot solutions
     */
    public List<SlotSolution> getSlotsSortedByConsolidationScore(
            List<Slot> slots, Map<String, Node> nodes, int totalCpusToAdd, int minCpu, int maxCpu, int ramMb, int diskGb) {
        Set<String> combinations = subsetSumBruteForce(getAllowedCpuSizes(minCpu, maxCpu), totalCpusToAdd);
        Map<Integer, List<Slot>> slotsInAllOrders = getAllListOrders(slots);
        List<SlotSolution> solutions = new ArrayList<>();

        for (String combination : combinations) {
            //System.out.println("Trying to find place for " + combination.toString());
            String cpus[] = combination.split(",");
            
            for(int k : slotsInAllOrders.keySet()){
                List<Slot> results = new ArrayList<>();
                List<Slot> slotsOrderProposal = this.cloneSlots(slotsInAllOrders.get(k));
                
                //System.out.println("Sorted slots: " + slotsClone.toString());
                for (int index = 0; index < cpus.length; index++) {
                    int numCpus = Integer.parseInt(cpus[index]);
                    Slot slotAssigned = this.assignSlot(numCpus, diskGb, ramMb, slotsOrderProposal);
                    //System.out.println("Sorted slots (II): " + slotsClone.toString());
                    if (slotAssigned != null) {
                        results.add(slotAssigned);
                    }
                }

                for (Slot s : slotsOrderProposal) {
                    //System.out.println(s.getFreeCpus() + " - " + s.getHostname());
                }
                double consolidationScore = consolidationScore(slotsOrderProposal, nodes);
                //System.out.println("Score: " + consolidationScore);
                solutions.add(new SlotSolution(consolidationScore, results, this.provider));
            }
        }

        Collections.sort(solutions, new Comparator<SlotSolution>() {
            @Override
            public int compare(SlotSolution a, SlotSolution b) {
                if (a.getConsolidationScore() > b.getConsolidationScore()) {
                    return -1;
                }

                if (a.getConsolidationScore() < b.getConsolidationScore()) {
                    return 1;
                }

                if (a.getSlots().size() < b.getSlots().size()) {
                    return -1;
                }

                if (a.getSlots().size() > b.getSlots().size()) {
                    return 1;
                }

                return 0;
            }
        });

        return solutions;
    }
}