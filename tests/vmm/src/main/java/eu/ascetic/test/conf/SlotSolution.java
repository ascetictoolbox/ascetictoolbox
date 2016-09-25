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
package eu.ascetic.test.conf;

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
    
    @Override
    public String toString() {
        String out = "consolidationScore = " + consolidationScore + "\n";
        for(Slot s : slots){ 
            out += s.getHostname() + " => ( cpus=" + s.getFreeCpus() + 
                ", ramMb=" + s.getFreeDiskGb() + ", diskGb=" + s.getFreeDiskGb() + " ); \n"; 
        }
        return out;
    }
    
}
