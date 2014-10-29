/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
        setGangliaHost();
        initTotalResources();
        initAssignedResources();
    }

    private void setGangliaHost() {
        List<Cluster> clusterList = new Ganglia().getGridInfo();
        for (Cluster cluster : clusterList) {
            List<es.bsc.monitoring.ganglia.infrastructure.Host> hosts = cluster.getHosts();
            for (es.bsc.monitoring.ganglia.infrastructure.Host host: hosts) {
                if (host.getName().equals(hostname)) {
                    gangliaHost = host;
                }
            }
        }
    }

    private void initTotalResources() {
        totalCpus = gangliaHost.getCpuNum();
        totalMemoryMb = Math.round(gangliaHost.getMemTotal()/1024);
        totalDiskGb = gangliaHost.getDiskTotal();
    }

    private void initAssignedResources() {
        assignedCpus = getAssignedCpus();
        assignedMemoryMb = getAssignedMemoryMb();
        assignedDiskGb = getAssignedDiskGb();
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

    @Override
    public void refreshMonitoringInfo() {
        setGangliaHost();
        initTotalResources();
        initAssignedResources();
    }

}
