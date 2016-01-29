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

package es.bsc.demiurge.core.clopla.examples;

import es.bsc.demiurge.core.clopla.domain.ConstructionHeuristic;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.lib.IClopla;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.lib.Clopla;
import es.bsc.demiurge.core.clopla.domain.Vm;
import es.bsc.demiurge.core.clopla.placement.config.localsearch.LateAcceptance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class shows an example of how to use the library.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class ExampleClient {

    // Fix a seed for the random number generator so the executions can be repeated
    private static final Random r = new Random(1);

    // Number of VMs and hosts
    private static final int N_VMS = 200;
    private static final int N_HOSTS = 100;

    // Limits for the size of a VM
    private static final int MIN_CPUS_VMS = 1;
    private static final int MAX_CPUS_VMS = 8;
    private static final int MIN_RAMMB_VMS = 1024;
    private static final int MAX_RAMMB_VMS = 8192;
    private static final int MIN_DISKGB_VMS = 1;
    private static final int MAX_DISKGB_VMS = 8;

    // Limits for the size of a host
    private static final int MIN_CPUS_HOST = 1;
    private static final int MAX_CPUS_HOST = 32;
    private static final int MIN_RAMMB_HOST = 1024;
    private static final int MAX_RAMMB_HOST = 32768;
    private static final int MIN_DISKGB_HOST = 1;
    private static final int MAX_DISKGB_HOST = 300;

    /**
     * Returns an initial list of VMs generated randomly.
     *
     * @return the list of VMs
     */
    private static List<Vm> getInitialVms() {
        List<Vm> result = new ArrayList<>();
        for (int i = 0; i < N_VMS; ++i) {
            result.add(new Vm.Builder(
                    (long) i,
                    randInt(MIN_CPUS_VMS, MAX_CPUS_VMS),
                    randInt(MIN_RAMMB_VMS, MAX_RAMMB_VMS),
                    randInt(MIN_DISKGB_VMS, MAX_DISKGB_VMS)).build());
        }
        return result;
    }

    /**
     * Returns an initial list of hosts generated randomly.
     *
     * @return the list of hosts
     */
    private static List<Host> getInitialHosts() {
        List<Host> result = new ArrayList<>();
        for (int i = 0; i < N_HOSTS; ++i) {
            result.add(new Host(
                    (long) i,
                    String.valueOf(i),
                    randInt(MIN_CPUS_HOST, MAX_CPUS_HOST),
                    randInt(MIN_RAMMB_HOST, MAX_RAMMB_HOST),
                    randInt(MIN_DISKGB_HOST, MAX_DISKGB_HOST),
                    false));
        }
        return result;
    }

    /**
     * Generates a random number within the specified range.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @return the random number generated
     */
    private static int randInt(int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    public static void main(String[] args) {
        // This example gets a list of random hosts, a list of random VMs, and then tries to find the best 
        // placement using the following options:
        //     policy: consolidation
        //     max. execution time: 30s
        //     local search algorithm: late acceptance (with late acceptance size = 400)
        //     vmsAreFixed = false. This means that we do not want to force the VMs to be deployed in the same host
        //         where they are now. In this case, we did not specify any host for any of the VMs.
        
        IClopla clopla = new Clopla();
        VmPlacementConfig vmPlacementConfig =
                new VmPlacementConfig.Builder(
                        "consolidation",
                        30,
                        ConstructionHeuristic.FIRST_FIT_DECREASING,
                        new LateAcceptance(400),
                        false).build();
        System.out.println(clopla.getBestSolution(getInitialHosts(), getInitialVms(), vmPlacementConfig));
    }

}
