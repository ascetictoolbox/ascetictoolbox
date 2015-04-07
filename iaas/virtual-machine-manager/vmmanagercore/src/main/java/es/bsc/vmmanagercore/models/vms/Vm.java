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

package es.bsc.vmmanagercore.models.vms;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.File;

/**
 * VM.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Vm {

    private final String name;
    private final String image; // It can be an ID or a URL
    private final int cpus;
    private final int ramMb;
    private final int diskGb;
    private final int swapMb;
    private String initScript;
    private String applicationId;
    
    /**
     * Class constructor.
     * @param name The name of the instance.
     * @param image The ID of the image or a URL containing it.
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     * @param swapMb The amount of swap in MB.
     * @param initScript Script that will be executed when the VM is deployed.
     */
    public Vm(String name, String image, int cpus, int ramMb, int diskGb, int swapMb,
              String initScript, String applicationId) {
        validateConstructorParams(cpus, ramMb, diskGb, swapMb);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        setInitScript(initScript);
        this.applicationId = applicationId;
    }

    // TODO: apply builder pattern.
    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId) {
        validateConstructorParams(cpus, ramMb, diskGb, 0);
        this.name = name;
        this.image = image;
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = 0;
        setInitScript(initScript);
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

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean belongsToAnApp() {
        return applicationId != null && !applicationId.equals("") && !applicationId.equals(" ");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    private void validateConstructorParams(int cpus, int ramMb, int diskGb, int swapMb) {
        Preconditions.checkArgument(cpus > 0, "CPUs was %s but expected positive", cpus);
        Preconditions.checkArgument(ramMb > 0, "RAM MB was %s but expected positive", ramMb);
        Preconditions.checkArgument(diskGb > 0, "Disk GB was %s but expected positive", diskGb);
        Preconditions.checkArgument(swapMb >= 0, "Swap MB was %s but expected non-negative", swapMb);
    }
    
}
