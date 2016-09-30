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
package eu.ascetic.paas.self.adaptation.manager.rules.datatypes;

import es.bsc.vmmclient.models.Slot;
import java.util.List;

/**
 *
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class SlotSolution {
    private List<Slot> slots;
    private double consolidationScore;
    private String provider;
    
    /**
     * Constructor of SlotSolution
     * 
     * @param consolidationScore score for this solution.
     * @param slots list of slots with the solution proposed.
     * @param provider the provider of the solution.
     */
    public SlotSolution(double consolidationScore, List<Slot> slots, String provider){
        this.consolidationScore = consolidationScore;
        this.slots = slots;
        this.provider = provider;
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
    
    @Override
    public String toString() {
        String out = "consolidationScore = " + consolidationScore + "\n";
        for(Slot s : slots){ 
            out += s.getHostname() + " => ( cpus=" + s.getFreeCpus() + 
                ", ramMb=" + s.getFreeDiskGb() + ", diskGb=" + s.getFreeDiskGb() + ", provider = " + this.getProvider() + " ); \n"; 
        }
        return out;
    }

    /**
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
}