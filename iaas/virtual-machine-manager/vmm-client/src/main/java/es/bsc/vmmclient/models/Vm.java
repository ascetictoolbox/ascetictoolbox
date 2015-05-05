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

package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

public class Vm {

    protected final String name;
    protected final String image; // It can be an ID or a URL
    protected final int cpus;
    protected final int ramMb;
    protected final int diskGb;
    protected final int swapMb;
    protected final String initScript;
    protected final String applicationId;

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId) {
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.initScript = initScript;
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
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

    public String getInitScript() {
        return initScript;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("image", image)
                .add("cpus", cpus)
                .add("ramMb", ramMb)
                .add("diskGb", diskGb)
                .add("swapMb", swapMb)
                .add("initScript", initScript)
                .add("applicationId", applicationId)
                .toString();
    }
}
