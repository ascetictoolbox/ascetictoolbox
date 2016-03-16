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

package es.bsc.demiurge.core.models.vms;

import com.google.common.base.MoreObjects;

public class VmRequirements {

    protected final int cpus;
    protected final int ramMb;
    protected final int diskGb;
    protected final int swapMb;
    private boolean autoConfirmResize;

    public VmRequirements(int cpus, int ramMb, int diskGb, int swapMb) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.autoConfirmResize = true;
    }

    public int getCpus() {
        return cpus;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpus", cpus)
                .add("ramMb", ramMb)
                .add("diskGb", diskGb)
                .add("swapMb", swapMb)
                .toString();
    }

    /**
     * @return checks if confirmResize has to be done automatically
     */
    public boolean isAutoConfirm() {
        return autoConfirmResize;
    }

    /**
     * @param autoConfirm if set to true confirmResize will be done automatically
     */
    public void setAutoConfirm(boolean autoConfirm) {
        this.autoConfirmResize = autoConfirm;
    }
}