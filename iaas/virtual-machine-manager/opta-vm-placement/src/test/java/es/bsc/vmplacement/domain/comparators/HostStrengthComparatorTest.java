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

import es.bsc.vmplacement.domain.Host;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class HostStrengthComparatorTest {

    private final HostStrengthComparator hostStrengthComparator = new HostStrengthComparator();

    @Test
    public void comparisonShouldReturnPositiveWhenFirstHostStronger() {
        // Compare host1(CPUs = 2, ramMb = 1, diskGb = 1) vs host2(CPUs = 1, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 1, 1, 1, false)) > 0);
    }

    @Test
    public void comparisonShouldReturnZeroWhenHostsEquallyStronger() {
        // Compare host1(CPUs = 2, ramMb = 1, diskGb = 1) vs host2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 2, 1, 1, false)) == 0);
    }

    @Test
    public void comparisonShouldReturnNegativeWhenFirstHostWeaker() {
        // Compare host1(CPUs = 1, ramMb = 1, diskGb = 1) vs host2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 1, 1, 1, false),
                new Host((long) 2, "2", 2, 1, 1, false)) < 0);
    }
    
    @Test
    public void comparisonShouldReturnPositiveWhenFirstHostWasOff() {
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 1, 1, 1, true),
                new Host((long) 2, "2", 2, 1, 1, false)) > 0);
    }

    @Test
    public void comparisonShouldReturnNegativeWhenSecondHostWasOff() {
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 1, 1, 1, true)) < 0);
    }

}
