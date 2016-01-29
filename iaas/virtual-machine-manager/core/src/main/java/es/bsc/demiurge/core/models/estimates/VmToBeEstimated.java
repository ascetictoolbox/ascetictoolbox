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

package es.bsc.demiurge.core.models.estimates;

import es.bsc.demiurge.core.models.vms.Vm;

/**
 * VM for which an price/energy estimation needs to be provided.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmToBeEstimated {

    private final String id;
    private final int vcpus;
    private final int cpuFreq;
    private final int ramMb;
    private final int diskGb;
    private final int swapMb;

    public VmToBeEstimated(String id, int vcpus, int cpuFreq, int ramMb, int diskGb, int swapMb) {
        this.id = id;
        this.vcpus = vcpus;
        this.cpuFreq = cpuFreq;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
    }

    public String getId() {
        return id;
    }

    public int getVcpus() {
        return vcpus;
    }

    public int getCpuFreq() {
        return cpuFreq;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public int getSwapMb() {
        return swapMb;
    }
    
    public Vm toVm() {
        return new Vm(id, "", vcpus, ramMb, diskGb, swapMb, null, "");
    }

}
