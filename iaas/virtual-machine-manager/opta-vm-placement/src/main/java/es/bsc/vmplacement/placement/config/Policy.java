package es.bsc.vmplacement.placement.config;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public enum Policy {

    CONSOLIDATION("consolidation"), DISTRIBUTION("distribution"), PRICE("price"), ENERGY("energy"),
        GROUP_BY_APP("groupByApp"), RANDOM("random");

    private final String name;

    private Policy(String name) {
        this.name = name;
    }

}