package es.bsc.vmmanagercore.monitoring;

import es.bsc.monitoring.ganglia.Ganglia;
import es.bsc.monitoring.ganglia.infrastructure.Cluster;

import java.util.List;

/**
 * Status of a host of an infrastructure monitored by Ganglia.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostGanglia extends Host {

    public es.bsc.monitoring.ganglia.infrastructure.Host gangliaHost;

    public HostGanglia(String hostname) {
        super(hostname);

        //get the Ganglia host from its hostname
        List<Cluster> clusterList = new Ganglia().getGridInfo();
        for (Cluster cluster : clusterList) {
            List<es.bsc.monitoring.ganglia.infrastructure.Host> hosts = cluster.getHosts();
            for (es.bsc.monitoring.ganglia.infrastructure.Host host: hosts) {
                if (host.getName().equals(hostname)) {
                    gangliaHost = host;
                }
            }
        }
        
        //initialize the total resources of the host
        initTotalResources();
    }

    private void initTotalResources() {
        totalCpus = gangliaHost.getCpuNum();
        totalMemoryMb = Math.round(gangliaHost.getMemTotal()/1024);
        totalDiskGb = gangliaHost.getDiskTotal();
    }

    @Override
    public double getAssignedCpus() {
        double assignedCpus = gangliaHost.getLoadOne();
        updateAssignedCpus(assignedCpus);
        return assignedCpus;
    }

    @Override
    public double getAssignedMemoryMb() {
        int assignedMemoryMb = Math.round(
                (gangliaHost.getMemTotal() - gangliaHost.getMemFree() -
                gangliaHost.getMemCached() - gangliaHost.getMemBuffers())/1024);
        updateAssignedMemoryMb(assignedMemoryMb);
        return assignedMemoryMb;
    }

    @Override
    public double getAssignedDiskGb() {
        double assignedDiskGb = gangliaHost.getDiskUsed();
        updateAssignedDiskGb(assignedDiskGb);
        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    @Override
    public double getFreeCpus() {
        return totalCpus - getAssignedCpus();
    }

    /**
     * @return available memory of the host (in MB)
     */
    @Override
    public double getFreeMemoryMb() {
        return totalMemoryMb - getAssignedMemoryMb();
    }

    /**
     * @return available disk space of the host (in GB)
     */
    @Override
    public double getFreeDiskGb() {
        return totalDiskGb - getAssignedDiskGb();
    }

}
