/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.types;

import java.util.LinkedList;

public class ScheduleDecisions {

    public boolean recommendsCreation;
    public float[] creationRecommendations;
    public LinkedList<Integer> requiredRecomendations;
    public boolean mandatoryCreations;

    public boolean recommendsDestruction;
    public float[] destroyRecommendations;
    public boolean mandatoryDestruction;

    private LinkedList<Movement> movements;

    public ScheduleDecisions() {
        movements = new LinkedList();
        recommendsCreation = false;
        mandatoryCreations = false;
        recommendsDestruction = false;
        mandatoryDestruction = false;
    }

    /*public void addMovement(String sourceName, int sourceSlot, String targetName, int targetSlot, int core, int amount) {
     Movement mov = new Movement();
     mov.sourceName = sourceName;
     mov.sourceSlot = sourceSlot;
     mov.targetName = targetName;
     mov.targetSlot = targetSlot;
     mov.core = core;
     mov.amount = amount;
     movements.add(mov);
     }*/
    public LinkedList<Object[]> getMovements() {
        LinkedList movs = new LinkedList();
        for (Movement mov : movements) {
            movs.add(new Object[]{mov.sourceName, mov.targetName, mov.sourceSlot, mov.targetSlot, mov.core, mov.amount});
        }
        return movs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (recommendsCreation) {
            if (mandatoryCreations) {
                sb.append("Mandatory creations : [").append(creationRecommendations[0]);
            } else {
                sb.append("Recommended creations : [").append(creationRecommendations[0]);
            }
            for (Integer i = 1; i < creationRecommendations.length; i++) {
                sb.append(", ").append(creationRecommendations[i]);
                if (requiredRecomendations.contains(i)) {
                    sb.append("*");
                }
            }
            sb.append("]\n");
        }
        if (recommendsDestruction) {
            if (mandatoryDestruction) {
                sb.append("Mandatory destructions : [").append(destroyRecommendations[0]);
            } else {
                sb.append("Recommended destructions : [").append(destroyRecommendations[0]);
            }
            for (Integer i = 1; i < destroyRecommendations.length; i++) {
                sb.append(", ").append(destroyRecommendations[i]);
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}

abstract class Movement {

    String sourceName;
    int sourceSlot;
    String targetName;
    int targetSlot;
    int core;
    int amount;

    public abstract String toString();
}
