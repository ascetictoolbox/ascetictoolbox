package es.bsc.vmmanagercore.selfadaptation.options;

import es.bsc.vmmanagercore.models.scheduling.LocalSearchAlgorithmOptionsSet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests for the AfterVmDeleteSelfAdaptationOps class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class AfterVmDeleteSelfAdaptationOpsTest {
    
    private static AfterVmDeleteSelfAdaptationOps afterVmDeleteSelfAdaptationOps;
    
    @BeforeClass
    public static void onlyOnce() {
        Map<String, Integer> options = new HashMap<>();
        options.put("Size", 1);
        LocalSearchAlgorithmOptionsSet localSearchAlgorithmOptionsSet = 
                new LocalSearchAlgorithmOptionsSet("Hill Climbing", options);
        afterVmDeleteSelfAdaptationOps = new AfterVmDeleteSelfAdaptationOps(localSearchAlgorithmOptionsSet, 10);
    }
    
    @Test
    public void testGetters() {
        assertEquals(10, afterVmDeleteSelfAdaptationOps.getMaxExecTimeSeconds());
        assertEquals("Hill Climbing", afterVmDeleteSelfAdaptationOps.getLocalSearchAlgorithm().getName());
    }
    
}
