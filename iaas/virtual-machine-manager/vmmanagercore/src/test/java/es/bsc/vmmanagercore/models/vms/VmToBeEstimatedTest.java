package es.bsc.vmmanagercore.models.vms;

import es.bsc.vmmanagercore.models.estimations.VmToBeEstimated;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the VmToBeEstimated class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmToBeEstimatedTest {
    
    private final VmToBeEstimated vmToBeEstimated = new VmToBeEstimated("testId", 1, 1000, 1024, 10, 512);
    
    @Test
    public void testGetters() {
        assertEquals("testId", vmToBeEstimated.getId());
        assertEquals(1, vmToBeEstimated.getVcpus());
        assertEquals(1000, vmToBeEstimated.getCpuFreq());
        assertEquals(1024, vmToBeEstimated.getRamMb());
        assertEquals(10, vmToBeEstimated.getDiskGb());
        assertEquals(512, vmToBeEstimated.getSwapMb());
    }
    
    @Test
    public void toVm() {
        Vm convertedVm = vmToBeEstimated.toVm();
        assertEquals("testId", convertedVm.getName());
        assertEquals("", convertedVm.getImage());
        assertEquals(1, convertedVm.getCpus());
        assertEquals(1024, convertedVm.getRamMb());
        assertEquals(10, convertedVm.getDiskGb());
        assertEquals(512, convertedVm.getSwapMb());
        assertNull(convertedVm.getInitScript());
        assertEquals("", convertedVm.getApplicationId());
    }
    
}
