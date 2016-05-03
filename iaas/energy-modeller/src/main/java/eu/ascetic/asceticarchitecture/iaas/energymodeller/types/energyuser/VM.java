/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class represents an energy user of the ASCETiC project and in particular
 * a VM that is to be deployed.
 *
 * It is somehow similar in nature to:
 *
 * @see es.bsc.vmmanagercore.model.Vm
 * @author Richard Kavanagh
 *
 */
public class VM extends EnergyUsageSource {

    private int cpus;
    private int ramMb;
    private double diskGb;
    /**
     * VM identification concepts: DeploymentID: This is the PaaS layer
     * deployment Id. The deployment Id can be used for several VMs. This may
     * also be called the application id. Disk Images: This identifies the disks
     * that are attached to the VM. The principle is that these can the be used
     * to identify the workload profile, of the VM. Application Tags: These can
     * be used in a similar fashion, these tags should be unique to an
     * application but can be extended to indicate other factors as well.
     */
    private String deploymentID = null;
    private HashSet<VmDiskImage> diskImages = new HashSet<>();
    private HashSet<String> applicationTags = new HashSet<>();
    /**
     *
     * E_i^v: is the "idle power consumption" of a VM which includes the
     * incremental cost to the hypervisor and the energy use of the OS.
     */
    private double powerConsumptionVMOverhead = 0.0;

    /**
     * This creates a vm with its key data missing. The set methods should be
     * called afterwards.
     */
    public VM() {
    }

    /**
     * This creates a VM that represents a energy usage source.
     *
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     */
    public VM(int cpus, int ramMb, double diskGb) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
    }

    /**
     * A copy constructor, for a VM.
     *
     * @param vm The vm to copy/clone.
     */
    public VM(VM vm) {
        this.cpus = vm.cpus;
        this.ramMb = vm.ramMb;
        this.diskGb = vm.diskGb;
        this.applicationTags = vm.getApplicationTags();
        this.diskImages = vm.getDiskImages();
    }

    /**
     * A copy constructor, for a VM. This constructor is aimed at resizing a VM.
     *
     * @param vm The vm to copy/clone.
     * @param cpus The number of CPUs. 
     * @param ramMb The amount of RAM in MB.
     */
    public VM(VmDeployed vm, int cpus, int ramMb) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = vm.getDiskGb();
        this.applicationTags = vm.getApplicationTags();
        this.diskImages = vm.getDiskImages();
    }    

    /**
     * A copy constructor, for a VM.
     *
     * @param vm The vm to copy/clone.
     */
    public VM(VmDeployed vm) {
        this.cpus = vm.getCpus();
        this.ramMb = vm.getRamMb();
        this.diskGb = vm.getDiskGb();
        this.applicationTags = vm.getApplicationTags();
        this.diskImages = vm.getDiskImages();
    }        
    
    /**
     * This gets the count of how many virtual cpu cores this vm has.
     *
     * @return the amount of virtual cpus cores that the VM has.
     */
    public int getCpus() {
        return cpus;
    }

    /**
     * This sets the count of how many virtual cpu cores this vm has.
     *
     * @param cpus The virtual cpu cores this vm has
     */
    public void setCpus(int cpus) {
        if (cpus <= 0) {
            throw new IllegalArgumentException("The number of cpus has to be greater than 0");
        }
        this.cpus = cpus;
    }

    /**
     * This gets the amount of ram this vm has been allocated.
     *
     * @return The ram this vm has been allocated.
     */
    public int getRamMb() {
        return ramMb;
    }

    /**
     * This sets the amount of ram this vm has been allocated.
     *
     * @param ramMb The ram this vm has been allocated.
     */
    public void setRamMb(int ramMb) {
        if (ramMb < 0) {
            throw new IllegalArgumentException("The amount of memory must not be less than zero.");
        }
        this.ramMb = ramMb;
    }

    /**
     * This gets the amount of disk space this vm has been allocated.
     *
     * @return The disk space this vm has been allocated.
     */
    public double getDiskGb() {
        return diskGb;
    }

    /**
     * This sets the amount of disk space this vm has been allocated.
     *
     * @param diskGb The disk space this vm has been allocated.
     */
    public void setDiskGb(double diskGb) {
        if (diskGb < 0) {
            throw new IllegalArgumentException("The amount of disk size must not be less than zero.");
        }
        this.diskGb = diskGb;
    }

    /**
     * This gets the power consumption overhead associated with this VM.
     *
     * @return the powerConsumptionVMOverhead
     */
    public double getPowerConsumptionVMOverhead() {
        return powerConsumptionVMOverhead;
    }

    /**
     * This sets the power consumption overhead associated with this VM.
     *
     * @param powerConsumptionVMOverhead the powerConsumptionVMOverhead to set
     */
    public void setPowerConsumptionVMOverhead(double powerConsumptionVMOverhead) {
        if (powerConsumptionVMOverhead < 0.0) {
            throw new IllegalArgumentException("The overhead due to hosting a VM must not be less than zero.");
        }
        this.powerConsumptionVMOverhead = powerConsumptionVMOverhead;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VM) {
            VM vm = (VM) obj;
            return this.cpus == vm.getCpus() && this.ramMb == vm.getRamMb() && this.diskGb == vm.getDiskGb();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.cpus;
        hash = 37 * hash + this.ramMb;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.diskGb) ^ (Double.doubleToLongBits(this.diskGb) >>> 32));
        return hash;
    }

    /**
     * This returns the deployment id of a VM. The deployment ID is a reference
     * string that is used to identify a set of VMs that belong together as part
     * of a single deployment. This field is optional.
     *
     * @return the deploymentID for the VM
     */
    public String getDeploymentID() {
        return deploymentID;
    }

    /**
     * This sets the deployment id of a VM. The deployment ID is a reference
     * string that is used to identify a set of VMs that belong together as part
     * of a single deployment. This field is optional.
     *
     * @param deploymentID the deploymentID to set
     */
    public void setDeploymentID(String deploymentID) {
        this.deploymentID = deploymentID;
    }

    /**
     * This gets the list of disk images that this VM is associated with.
     *
     * @return the diskImages
     */
    public HashSet<VmDiskImage> getDiskImages() {
        return diskImages;
    }

    /**
     * This sets the list of disk images that this VM is associated with.
     *
     * @param diskImages the diskImages to set
     */
    public void setDiskImages(HashSet<VmDiskImage> diskImages) {
        if (diskImages != null) {
            this.diskImages = diskImages;
        }
    }

    /**
     * This adds a new disk image to the VM.
     *
     * @param image The disk image to add
     */
    public void addDiskImage(String image) {
        if (image != null) {
            diskImages.add(new VmDiskImage(image));
        }
    }

    /**
     * This indicates if a VM has the same set of disks as specified.
     *
     * @param vm The vm to perform the comparison on in regards to VM disk
     * images.
     * @return If the VM has the same set of disks as specified or not.
     */
    public boolean isDiskSetEqual(VM vm) {
        return isDiskSetEqual(vm.getDiskImages());
    }

    /**
     * This indicates if a VM has the same set of disks as specified.
     *
     * @param disks The set of disks to compare.
     * @return If the VM has the same set of disks as specified or not.
     */
    public boolean isDiskSetEqual(Collection<VmDiskImage> disks) {
        if (disks.size() != diskImages.size()) {
            return false;
        }
        for (VmDiskImage vmDiskImage : diskImages) {
            if (!disks.contains(vmDiskImage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This provides the set of application tags that are part of this VM.
     * Application Tags are intended as unique tags that can be set to identify
     * the applications on the VM.
     *
     * @return the applicationTags The set of unique identifiers that can be
     * used to classify a VM based upon the type of applications that generate a
     * workload on the VM.
     */
    public HashSet<String> getApplicationTags() {
        return applicationTags;
    }

    /**
     * This allows the set of application tags that are part of this VM to be
     * set. Application Tags are intended as unique tags that can be set to
     * identify the applications on the VM.
     *
     * @param applicationTags the applicationTags The set of unique identifiers
     * that can be used to classify a VM based upon the type of applications
     * that generate a workload on the VM.
     */
    public void setApplicationTags(HashSet<String> applicationTags) {
        if (applicationTags != null) {
            this.applicationTags = applicationTags;
        }
    }

    /**
     * This allows an application tags to be added to this VM. Application Tags
     * are intended as unique tags that can be set to identify the applications
     * on the VM.
     *
     * @param tag
     */
    public void addApplicationTag(String tag) {
        if (tag != null) {
            this.applicationTags.add(tag);
        }
    }

    /**
     * This checks to see if this VM contains a specified Application ID
     *
     * @param appId The id of the application to check to see if it is installed
     * on a given VM.
     * @return If the application ID is in the VMs set of known application IDs
     */
    public boolean containsApplication(String appId) {
        return applicationTags.contains(appId);
    }

}
