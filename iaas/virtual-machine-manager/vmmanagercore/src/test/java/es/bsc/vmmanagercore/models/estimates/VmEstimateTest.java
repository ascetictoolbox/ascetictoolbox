package es.bsc.vmmanagercore.models.estimates;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the VmEstimate class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmEstimateTest {
    
    private final VmEstimate vmEstimate = new VmEstimate("testId", 100, 200);
    
    @Test
    public void testGetters() {
        assertEquals("testId", vmEstimate.getId());
        assertEquals(100, vmEstimate.getPowerEstimate(), 0.1);
        assertEquals(200, vmEstimate.getPriceEstimate(), 0.1);
    }
    
}
