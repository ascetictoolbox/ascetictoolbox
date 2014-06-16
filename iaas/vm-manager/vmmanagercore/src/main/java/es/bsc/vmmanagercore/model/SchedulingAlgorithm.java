package es.bsc.vmmanagercore.model;

public enum SchedulingAlgorithm {

    CONSOLIDATION("consolidation"), DISTRIBUTION("distribution"), GROUP_BY_APP("groupByApp"), RANDOM("random");

    private String algorithm;

    private SchedulingAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

}