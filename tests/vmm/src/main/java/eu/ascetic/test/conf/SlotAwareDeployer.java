/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.conf;

import es.bsc.vmmclient.models.Slot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author raimon
 */
public class SlotAwareDeployer {
    
    public SlotAwareDeployer(){
        
    }
    
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
                    System.out.println(comb.toString());
                }
            }
        }

        return results;
    }

    /**
     *
     * @param numCpus
     * @param diskGb
     * @param ramMb
     * @param slots
     * @return
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
     *
     * @param slots
     * @return
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
     *
     * @param slots
     * @param numAllCpus
     * @return
     */
    private int consolidationScore(List<Slot> slots, int numAllCpus) {
        int consolidationScore = 0;
        for (Slot s : slots) {
            if (s.getFreeCpus() == numAllCpus) {
                consolidationScore++;
            }
        }

        return consolidationScore;
    }

    /**
     *
     * @param e
     * @return
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
     *
     * @param minCpu
     * @param maxCpu
     * @return
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
     *
     * @param slots
     * @param totalCpusToAdd
     * @param cpusPerHost
     * @param minCpu
     * @param maxCpu
     * @param ramMb
     * @param diskGb
     * @return
     */
    public List<SlotSolution> getSlotsSortedByConsolidationScore(List<Slot> slots, int totalCpusToAdd, int cpusPerHost, int minCpu, int maxCpu, int ramMb, int diskGb) {
        Set<String> combinations = subsetSumBruteForce(getAllowedCpuSizes(minCpu, maxCpu), totalCpusToAdd);
        List<SlotSolution> solutions = new ArrayList<>();

        for (String combination : combinations) {
            List<Slot> results = new ArrayList<>();
            System.out.println("Trying to find place for " + combination.toString());
            String cpus[] = combination.split(",");
            List<Slot> slotsClone = this.cloneSlots(slots);
            for (int index = 0; index < cpus.length; index++) {
                int numCpus = Integer.parseInt(cpus[index]);
                Slot slotAssigned = this.assignSlot(numCpus, diskGb, ramMb, slotsClone);
                if (slotAssigned != null) {
                    results.add(slotAssigned);
                }
            }

            for (Slot s : slotsClone) {
                System.out.println(s.getFreeCpus() + " - " + s.getHostname());
            }
            double consolidationScore = consolidationScore(slotsClone, cpusPerHost);
            System.out.println("Score: " + consolidationScore);
            solutions.add(new SlotSolution(consolidationScore, results));
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
