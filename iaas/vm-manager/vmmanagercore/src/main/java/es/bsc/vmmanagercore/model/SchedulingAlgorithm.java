package es.bsc.vmmanagercore.model;

public enum SchedulingAlgorithm {

    CONSOLIDATION("consolidation"), DISTRIBUTION("distribution"), RANDOM("random");

    private String algorithm;

    private SchedulingAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

}