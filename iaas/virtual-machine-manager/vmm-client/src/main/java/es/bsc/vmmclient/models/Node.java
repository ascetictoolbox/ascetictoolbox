package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

public class Node {

    private final String hostname;
    private final int totalCpus;
    private final double totalMemoryMb;
    private final double totalDiskGb;
    private final double assignedCpus;
    private final double assignedMemoryMb;
    private final double assignedDiskGb;
    private final double currentPower;

    public Node(String hostname, int totalCpus, double totalMemoryMb, double totalDiskGb, double assignedCpus,
                double assignedMemoryMb, double assignedDiskGb, double currentPower) {
        this.hostname = hostname;
        this.totalCpus = totalCpus;
        this.totalMemoryMb = totalMemoryMb;
        this.totalDiskGb = totalDiskGb;
        this.assignedCpus = assignedCpus;
        this.assignedMemoryMb = assignedMemoryMb;
        this.assignedDiskGb = assignedDiskGb;
        this.currentPower = currentPower;
    }

    public String getHostname() {
        return hostname;
    }

    public int getTotalCpus() {
        return totalCpus;
    }

    public double getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    public double getAssignedCpus() {
        return assignedCpus;
    }

    public double getAssignedMemoryMb() {
        return assignedMemoryMb;
    }

    public double getAssignedDiskGb() {
        return assignedDiskGb;
    }

    public double getCurrentPower() {
        return currentPower;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostname", hostname)
                .add("totalCpus", totalCpus)
                .add("totalMemoryMb", totalMemoryMb)
                .add("totalDiskGb", totalDiskGb)
                .add("assignedCpus", assignedCpus)
                .add("assignedMemoryMb", assignedMemoryMb)
                .add("assignedDiskGb", assignedDiskGb)
                .add("currentPower", currentPower)
                .toString();
    }
}
