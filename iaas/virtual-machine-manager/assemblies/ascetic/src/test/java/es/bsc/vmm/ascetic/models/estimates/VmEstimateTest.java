package es.bsc.vmm.ascetic.models.estimates;

import es.bsc.demiurge.core.models.estimates.VmEstimate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the VmEstimate class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmEstimateTest {
    
    private VmEstimate vmEstimate;

	@Before
	public void setUp() {
		vmEstimate	= new VmEstimate("testId");
		vmEstimate.addEstimate("power",100);
		vmEstimate.addEstimate("price",200);
	}
    
    @Test
    public void testGetters() {
        assertEquals("testId", vmEstimate.getId());
        assertEquals(100, vmEstimate.getEstimate("power"), 0.1);
        assertEquals(200, vmEstimate.getEstimate("price"), 0.1);
    }
    
}
