/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.demiurge.core.clopla.placement.scorecalculators;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;

/**
 * This class includes score functions that are used in several score calculators.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public abstract class ScoreCalculatorCommon {

    public final static int PENALTY_FOR_MOVING_FIXED_VMS = 10000;
    public final static int PENALTY_FOR_WRONG_HARDWARE = 10000;

    public static double getClusterOverCapacityScore(ClusterState clusterState) {
        double result = 0;
        for (Host host: clusterState.getHosts()) {
            result -= host.getOverCapacityScore(clusterState.getVms());
        }
        return result;
    }
    
    public static double getClusterHardwareScore(ClusterState clusterState) {
        double result = 0;
        for (Host host: clusterState.getHosts()) {
            if(!host.matchesHardwareRequirements(clusterState.getVms())){
                result -= PENALTY_FOR_WRONG_HARDWARE;
            }
        }
        if(result < 0) {
            System.out.println("getClusterHardwareScore: " + result);
        }
        return result;
    }
    
    public static double getClusterPenaltyScoreForFixedVms(ClusterState clusterState) {
        double result = 0;
        for (Host host: clusterState.getHosts()) {
            if (host.missingFixedVMs(clusterState.getVms())) {
                result -= PENALTY_FOR_MOVING_FIXED_VMS;
            }
        }
        if(result < 0) {
            System.out.println("getClusterPenaltyScoreForFixedVms: " + result);
        }
        return result;
    }

}
