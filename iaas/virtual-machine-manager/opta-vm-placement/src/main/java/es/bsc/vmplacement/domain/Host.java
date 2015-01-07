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

package es.bsc.vmplacement.domain;

import es.bsc.vmplacement.common.domain.AbstractPersistable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class Host extends AbstractPersistable {

    private final String hostname;
    private final int ncpus;
    private final double ramMb;
    private final double diskGb;
    private List<Long> fixedVmsIds = new ArrayList<>(); // IDs of the VMs that need to be deployed in this host

    public Host(Long id, String hostname, int ncpus, double ramMb, double diskGb) {
        this.hostname = hostname;
        this.id = id;
        this.ncpus = ncpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
    }

    public HostUsage getUsage(List<Vm> vms) {
        int ncpusUsed = 0;
        int ramMbUsed = 0;
        int diskGbUsed = 0;
        for (Vm vm: vms) {
            if (this.equals(vm.getHost())) {
                ncpusUsed += vm.getNcpus();
                ramMbUsed += vm.getRamMb();
                diskGbUsed += vm.getDiskGb();
            }
        }
        return new HostUsage(ncpusUsed, ramMbUsed, diskGbUsed);
    }

    /**
     * Returns the overcapacity score of the host given a list of VMs.
     *
     * @param vms the list of VMs
     * @return the overcapacity score
     */
    public double getOverCapacityScore(List<Vm> vms) {
        HostUsage hostUsage = getUsage(vms);
        return getCpuOverCapacityScore(hostUsage)
                + getRamOverCapacityScore(hostUsage)
                + getDiskOverCapacityScore(hostUsage);
    }

    public boolean missingFixedVMs(List<Vm> vms) {
        for (long vmId: fixedVmsIds) {
            for (Vm vm: vms) {
                if (vm.getId().equals(vmId)) {
                    if (vm.getHost() == null || !vm.getHost().getId().equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getHostname() {
        return hostname;
    }

    public int getNcpus() {
        return ncpus;
    }

    public double getRamMb() {
        return ramMb;
    }

    public double getDiskGb() {
        return diskGb;
    }

    public void addFixedVm(long vmId) {
        fixedVmsIds.add(vmId);
    }

    @Override
    public String toString() {
        return "Host - ID:" + id.toString() + ", cpus:" + ncpus + ", ram:" + ramMb + ", disk:" + diskGb;
    }

    private double getCpuOverCapacityScore(HostUsage hostUsage) {
        return ((ncpus - hostUsage.getNcpusUsed()) < 0) ? -(hostUsage.getNcpusUsed()/(double)ncpus) : 0.0;
    }

    private double getRamOverCapacityScore(HostUsage hostUsage) {
        return ((ramMb - hostUsage.getRamMbUsed()) < 0) ? -(hostUsage.getRamMbUsed()/ramMb) : 0.0;
    }

    private double getDiskOverCapacityScore(HostUsage hostUsage) {
        return ((diskGb - hostUsage.getDiskGbUsed()) < 0) ? -(hostUsage.getDiskGbUsed()/diskGb) : 0.0;
    }

}
