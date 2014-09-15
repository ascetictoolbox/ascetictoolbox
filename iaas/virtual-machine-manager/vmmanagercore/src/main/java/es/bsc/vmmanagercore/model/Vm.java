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

package es.bsc.vmmanagercore.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.File;

/**
 * VM.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Vm {

    private String name;
    private String image; // It can be an ID or a URL
    private int cpus;
    private int ramMb;
    private int diskGb;
    private String initScript;
    private String applicationId;

    /**
     * Class constructor.
     * @param name The name of the instance.
     * @param image The ID of the image or a URL containing it.
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     * @param initScript Script that will be executed when the VM is deployed.
     */
    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId) {
        this.name = name;
        this.image = image;
        setCpus(cpus);
        setRamMb(ramMb);
        setDiskGb(diskGb);
        setInitScript(initScript);
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setInstanceName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        if (cpus <= 0) {
            throw new IllegalArgumentException("The number of cpus has to be greater than 0");
        }
        this.cpus = cpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public void setRamMb(int ramMb) {
        if (ramMb <= 0) {
            throw new IllegalArgumentException("The amount of memory has to be greater than 0");
        }
        this.ramMb = ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public void setDiskGb(int diskGb) {
        if (diskGb <= 0) {
            throw new IllegalArgumentException("The amount of disk size has to be greater than 0");
        }
        this.diskGb = diskGb;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        // If a path for an init script was specified
        if (initScript != null && !initScript.equals("")) {
            // Check that the path is valid and the file can be read
            File f = new File(initScript);
            if (!f.isFile() || !f.canRead()) {
                throw new IllegalArgumentException("The path for the init script is not valid");
            }
            this.initScript = initScript;
        }
    }

    public String getApplicationId() {
        return applicationId;
    }

    public boolean belongsToAnApp() {
        return applicationId != null && !applicationId.equals("") && !applicationId.equals(" ");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
