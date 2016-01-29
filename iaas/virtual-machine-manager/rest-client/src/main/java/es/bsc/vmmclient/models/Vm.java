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
    protected final String ovfId;
    protected final String slaId;
    protected String preferredHost = null; // Where we want the VM to be deployed
    protected boolean needsFloatingIp = false;

    // Use builder pattern here...

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId, String ovfId, String slaId) {
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.initScript = initScript;
        this.applicationId = applicationId;
        this.ovfId = ovfId;
        this.slaId = slaId;
    }

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId, String ovfId, String slaId, String preferredHost) {
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.initScript = initScript;
        this.applicationId = applicationId;
        this.ovfId = ovfId;
        this.slaId = slaId;
        this.preferredHost = preferredHost;
    }

    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId, String ovfId, String slaId, boolean needsFloatingIp) {
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.initScript = initScript;
        this.applicationId = applicationId;
        this.ovfId = ovfId;
        this.slaId = slaId;
        this.needsFloatingIp = needsFloatingIp;
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

    public String getOvfId() {
        return ovfId;
    }

    public String getSlaId() {
        return slaId;
    }

    public String getPreferredHost() {
        return preferredHost;
    }

    public boolean needsFloatingIp() {
        return needsFloatingIp;
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
                .add("ovfId", ovfId)
                .add("slaId", slaId)
                .add("preferredHost", preferredHost)
                .add("needsFloatingIp", needsFloatingIp)
                .toString();
    }

}
