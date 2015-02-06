package es.bsc.vmplacement.domain.comparators;

import es.bsc.vmplacement.domain.Host;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class HostStrengthComparatorTest {

    private HostStrengthComparator hostStrengthComparator = new HostStrengthComparator();

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

}
