package es.bsc.vmplacement.domain.comparators;

import es.bsc.vmplacement.domain.Vm;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmDifficultyComparatorTest {

    private VmDifficultyComparator vmDifficultyComparator = new VmDifficultyComparator();

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