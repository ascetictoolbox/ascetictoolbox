package es.bsc.clopla_benchmarking;

import es.bsc.clopla.domain.Vm;
import es.bsc.clopla_benchmarking.utils.Randomizer;

import java.util.ArrayList;
import java.util.List;

public class VmCollectionGenerator {

    // Suppress default constructor for non-instantiability
    private VmCollectionGenerator() {
        throw new AssertionError();
    }

    public static List<Vm> generateVmCollection(int nVms, VmDimensions vmDimensions) {
        List<Vm> result = new ArrayList<>();
        for (int i = 0; i < nVms; ++i) {
            result.add(generateVm(i, vmDimensions));
        }
        return result;
    }

    private static Vm generateVm(int id, VmDimensions vmDimensions) {
        return new Vm.Builder((long) id,
                Randomizer.generate(vmDimensions.getMinCpus(), vmDimensions.getMaxCpus()),
                (Randomizer.generate(vmDimensions.getMinRamGb(), vmDimensions.getMaxRamGb()))*1024,
                Randomizer.generate(vmDimensions.getMinDiskGb(), vmDimensions.getMaxDiskGb())).build();
    }

}
