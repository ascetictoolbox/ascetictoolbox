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
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmDifficultyComparatorTest {

    private final VmDifficultyComparator vmDifficultyComparator = new VmDifficultyComparator();

    @Test
    public void comparisonShouldReturnPositiveWhenFirstVmMoreDifficult() {
        // Compare vm1(CPUs = 2, ramMb = 1, diskGb = 1) vs vm2(CPUs = 1, ramMb = 1, diskGb = 1)
        assertTrue(vmDifficultyComparator.compare(
                new Vm.Builder((long) 1, 2, 1, 1).build(), 
                new Vm.Builder((long) 2, 1, 1, 1).build()) > 0);
    }

    @Test
    public void comparisonShouldReturnZeroWhenVmsEquallyDifficult() {
        // Compare vm1(CPUs = 2, ramMb = 1, diskGb = 1) vs vm2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(vmDifficultyComparator.compare(
                new Vm.Builder((long) 1, 2, 1, 1).build(),
                new Vm.Builder((long) 2, 2, 1, 1).build()) == 0);
    }

    @Test
    public void comparisonShouldReturnNegativeWhenFirstVmLessDifficult() {
        // Compare vm1(CPUs = 1, ramMb = 1, diskGb = 1) vs vm2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(vmDifficultyComparator.compare(
                new Vm.Builder((long) 1, 1, 1, 1).build(),
                new Vm.Builder((long) 2, 2, 1, 1).build()) < 0);
    }

}