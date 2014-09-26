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

package es.bsc.vmplacement.domain.comparators;

import es.bsc.vmplacement.domain.Vm;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmDifficultyComparator implements Comparator<Vm>, Serializable {

    /**
     * This function compares the "difficulty" of two VMs. The VMs that demand more resources are considered to be
     * more "difficult" because it is more difficult to find a host with enough resources to deploy them.
     *
     * @param vm1 a VM
     * @param vm2 a VM
     * @return a negative number if vm1 is less difficult than vm2, a positive number if vm1 is more difficult than
     * vm2, 0 if they are equal
     */
    @Override
    public int compare(Vm vm1, Vm vm2) {
        return Double.compare(difficulty(vm1), difficulty(vm2));
    }

    /**
     * This function calculate the difficulty of a VM.
     * This is the formula used to calculate the difficulty: vm.cpus * (vm.ramMb/1000) * (vm.diskGb/100).
     * The memory and the disk capacity are divided in the formula because it would not be fair to give the same
     * weight to 1 CPU than to 1 MB or RAM.
     */
    private double difficulty(Vm vm) {
        return vm.getNcpus()*(vm.getRamMb()/1000.0)*(vm.getDiskGb()/100.0);
    }

}