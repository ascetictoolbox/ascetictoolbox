import es.bsc.clopla.domain.Host;

import java.util.ArrayList;
import java.util.List;

public class ClusterGenerator {

    // Supress default constructor for non-instantiability
    private ClusterGenerator() {
        throw new AssertionError();
    }

    public static List<Host> generateCluster(ClusterDimensions clusterDimensions) {
        List<Host> result = new ArrayList<>();
        for (int i = 0; i < clusterDimensions.getNhosts(); ++i) {
            result.add(generateHost(i, clusterDimensions.getHostDimensions()));
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
