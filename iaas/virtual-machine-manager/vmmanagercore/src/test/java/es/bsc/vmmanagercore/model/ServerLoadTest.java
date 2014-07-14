package es.bsc.vmmanagercore.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoadTest {

    private static double DELTA_DOUBLE_COMP = 0.05;

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
