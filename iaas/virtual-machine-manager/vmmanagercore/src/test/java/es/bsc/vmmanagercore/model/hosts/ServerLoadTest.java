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

package es.bsc.vmmanagercore.model.hosts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoadTest {

    private final static double DELTA_DOUBLE_COMP = 0.05;

    @Test
    public void getTotalOverloadReturnsZeroWhenThereIsNoOverload() {
        assertEquals(0, new ServerLoad(0.5, 0.6, 0.7).getTotalOverload(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getTotalOverloadWorksWhenThereIsOverload() {
        assertEquals(4, new ServerLoad(1.5, 2.5, 3).getTotalOverload(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedCpuPercWhenThereIsOverload() {
        assertEquals(0, new ServerLoad(1.2, 0.1, 0.1).getUnusedCpuPerc(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedCpuPercWhenThereIsNoOverload() {
        assertEquals(0.25, new ServerLoad(0.75, 0.1, 0.1).getUnusedCpuPerc(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedMemoryPercWhenThereIsOverload() {
        assertEquals(0, new ServerLoad(0.1, 1.2, 0.1).getUnusedRamPerc(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedMemoryPercWhenThereIsNoOverload() {
        assertEquals(0.75, new ServerLoad(0.1, 0.25, 0.1).getUnusedRamPerc(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedDiskPercWhenThereIsOverload() {
        assertEquals(0, new ServerLoad(0.1, 0.1, 1.2).getUnusedDiskPerc(), DELTA_DOUBLE_COMP);
    }

    @Test
    public void getUnusedDiskPercWhenThereIsNoOverload() {
        assertEquals(0.6, new ServerLoad(0.1, 0.1, 0.4).getUnusedDiskPerc(), DELTA_DOUBLE_COMP);
    }

}