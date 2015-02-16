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

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a host where VMs can be deployed.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class Host extends AbstractPersistable {

    private final String hostname;
    private final int ncpus;
    private final double ramMb;
    private final double diskGb;
    private final List<Long> fixedVmsIds = new ArrayList<>(); // IDs of the VMs that need to be deployed in this host
    private final boolean initiallyOff; // The host was off before the planning started

    public Host(Long id, String hostname, int ncpus, double ramMb, double diskGb, boolean initiallyOff) {
        this.hostname = hostname;
        this.id = id;
        this.ncpus = ncpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.initiallyOff = initiallyOff;
    }

    /**
     * Returns the usage of a host given a list of VMs.
     * This method checks the host to which each VM has been assigned, and returns the sum of
     * the resources (cpu, RAM, and disk) that the VMs assigned to this host demand.
     *  
     * @param vms the list of VMs
     * @return the host usage
     */
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
     * The overcapacity score of a host is calculated as follows:
     *  cpu_overcapacity_score + ram_overcapacity_score + disk_overcapacity_score
     * Each of the three components is calculated in the same way.
     * For example, cpu_overcapacity_score equals 0 if the VMs assigned to the host demand
     * less CPUs than the number of CPUs available in the host, and cpus_demanded_vms/cpus_host
     * otherwise. 
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

    /**
     * Checks whether all the VMs assigned to this host and marked as 'fixed' (the user indicated
     * that they need to be deployed in this host) are assigned to this host.
     *  
     * @param vms the list of VMs
     * @return False if any of the VMs that were marked as 'fixed' for this host have not been
     * assigned to this host, true otherwise 
     */
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

    public boolean wasOffInitiallly() {
        return initiallyOff;
    }
    
    @Override
    public String toString() {
        return "Host - ID:" + id.toString() + ", cpus:" + ncpus + ", ram:" + ramMb + ", disk:" + diskGb;
    }

    private double getCpuOverCapacityScore(HostUsage hostUsage) {
        return ((ncpus - hostUsage.getNcpusUsed()) < 0) ? - (hostUsage.getNcpusUsed()/(double)ncpus) : 0.0;
    }

    private double getRamOverCapacityScore(HostUsage hostUsage) {
        return ((ramMb - hostUsage.getRamMbUsed()) < 0) ? - (hostUsage.getRamMbUsed()/ramMb) : 0.0;
    }

    private double getDiskOverCapacityScore(HostUsage hostUsage) {
        return ((diskGb - hostUsage.getDiskGbUsed()) < 0) ? - (hostUsage.getDiskGbUsed()/diskGb) : 0.0;
    }

}
