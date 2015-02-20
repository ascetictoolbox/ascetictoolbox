/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmplacement.examples;

import es.bsc.vmplacement.domain.ConstructionHeuristic;
import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import es.bsc.vmplacement.lib.IOptaVmPlacement;
import es.bsc.vmplacement.lib.OptaVmPlacement;
import es.bsc.vmplacement.placement.config.Policy;
import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import es.bsc.vmplacement.placement.config.localsearch.LateAcceptance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class shows an example of how to use the library.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class ExampleClient {

    // Fix a seed for the random number generator so the experiments can be repeated
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
        IOptaVmPlacement optaVmPlacement = new OptaVmPlacement();
        VmPlacementConfig vmPlacementConfig =
                new VmPlacementConfig.Builder(
                        Policy.CONSOLIDATION,
                        30,
                        ConstructionHeuristic.FIRST_FIT_DECREASING,
                        new LateAcceptance(400), 
                        false).build();
        System.out.println(optaVmPlacement.getBestSolution(getInitialHosts(), getInitialVms(), vmPlacementConfig));
    }

}
