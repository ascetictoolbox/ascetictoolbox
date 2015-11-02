package es.bsc.clopla_benchmarking.cluster_generation;

import es.bsc.clopla.domain.Host;
import es.bsc.clopla_benchmarking.utils.Randomizer;

import java.util.ArrayList;
import java.util.List;

public class HostCollectionGenerator {

    // Suppress default constructor for non-instantiability
    private HostCollectionGenerator() {
        throw new AssertionError();
    }

    public static List<Host> generateHostCollection(int nhosts, HostDimensions hostsDimensions) {
        List<Host> result = new ArrayList<>();
        for (int i = 0; i < nhosts; ++i) {
            result.add(generateHost(i, hostsDimensions));
        }
        return result;
    }

    private static Host generateHost(int id, HostDimensions hostDimensions) {
        return new Host((long) id,
                Integer.toString(id),
                Randomizer.generate(hostDimensions.getMinCpus(), hostDimensions.getMaxCpus()),
                (Randomizer.generate(hostDimensions.getMinRamGb(), hostDimensions.getMaxRamGb()))*1024,
                Randomizer.generate(hostDimensions.getMinDiskGb(), hostDimensions.getMaxDiskGb()),
                false);
    }

}
