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

public class Vm {

    private final String name;
    private final String image; // It can be an ID or a URL
    private final int cpus;
    private final int ramMb;
    private final int diskGb;
    private final int swapMb;
    private final String initScript;
    private final String applicationId;

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

}
