package es.bsc.vmmanagercore.model;

/**
 * The scheduling algorithms that can be applied in the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public enum SchedulingAlgorithm {

    CONSOLIDATION("consolidation"), DISTRIBUTION("distribution"), ENERGY_AWARE("energyAware"),
        GROUP_BY_APP("groupByApp"), RANDOM("random");

    private String name;

    private SchedulingAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}