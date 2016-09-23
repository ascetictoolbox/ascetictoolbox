/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.paas.self.adaptation.manager.rules.datatypes;

import es.bsc.vmmclient.models.Slot;
import java.util.List;

/**
 *
 * @author raimon
 */
public class SlotSolution {
    private List<Slot> slots;
    private double consolidationScore;
    
    public SlotSolution(double consolidationScore, List<Slot> slots){
        this.consolidationScore = consolidationScore;
        this.slots = slots;
    }

    /**
     * @return the slots
     */
    public List<Slot> getSlots() {
        return slots;
    }

    /**
     * @param slots the slots to set
     */
    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    /**
     * @return the consolidationScore
     */
    public double getConsolidationScore() {
        return consolidationScore;
    }

    /**
     * @param consolidationScore the consolidationScore to set
     */
    public void setConsolidationScore(double consolidationScore) {
        this.consolidationScore = consolidationScore;
    }
    
    public String toString() {
        String out = "consolidationScore = " + consolidationScore + "\n";
        for(Slot s : slots){ 
            out += s.getHostname() + " => ( cpus=" + s.getFreeCpus() + 
                ", ramMb=" + s.getFreeDiskGb() + ", diskGb=" + s.getFreeDiskGb() + " ); \n"; 
        }
        return out;
    }
    
}
