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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class provides services which convert OVF descriptions into the energy
 * modellers internal representation.
 *
 * @author Richard
 */
public class OVFConverterFactory {

    /**
     * This creates a VM object in cases where the VM has yet to be
     * instantiated.
     *
     * @param deploymentOVF the virtual machines for deployment as described in
     * OVF.
     * @return A new VM with the parameters specified above.
     */
    public static Collection<VM> getVMs(OvfDefinition deploymentOVF) {
        ArrayList<VM> answer = new ArrayList<>();
        if (deploymentOVF == null) {
            return answer;
        }
        DiskSection diskSection = deploymentOVF.getDiskSection();
        VirtualSystemCollection vmsSection = deploymentOVF.getVirtualSystemCollection();
        String deploymentId = getDeploymentID(deploymentOVF);
        for (VirtualSystem virtualMachine : vmsSection.getVirtualSystemArray()) {
            VM vm = new VM(virtualMachine.getVirtualHardwareSection().getNumberOfVirtualCPUs(),
                    virtualMachine.getVirtualHardwareSection().getMemorySize(),
                    getVmDiskSize(virtualMachine, diskSection));
            vm.setDiskImages(getVmDiskImages(virtualMachine, diskSection));
            vm.setDeploymentID(deploymentId);
            answer.add(vm);
        }
        return answer;
    }

    /**
     * This returns the ASCETiC deployment ID for a set of VMs, if it exists
     * @param deploymentOVF the virtual machines for deployment as described in
     * OVF.
     * @return The deploymentID for a VM if it exists otherwise it returns the 
     * empty string "".
     */
    private static String getDeploymentID(OvfDefinition deploymentOVF) {
        if (deploymentOVF == null || deploymentOVF.getVirtualSystemCollection().
                getProductSectionAtIndex(0) == null) {
            return "";
        }
        return deploymentOVF.getVirtualSystemCollection()
                .getProductSectionAtIndex(0).getDeploymentId();
    }

    /**
     * This returns the size of the overall disk space available to the VM.
     * @param diskSection The set of disks as defined in the OVF
     * @param virtualMachine The virtual machine to find the disk size for
     * @return The size of this disk in Gb
     */
    private static double getVmDiskSize(VirtualSystem virtualMachine, DiskSection diskSection) {
        double answer = 0.0f;
        List<Disk> disks = Arrays.asList(diskSection.getDiskArray());
        ArrayList<String> diskIds = getVMsDiskIds(virtualMachine);
        for (Disk disk : disks) {
            if (diskIds.contains(disk.getDiskId())) {
                answer = answer + getDiskCapacity(disk);
            }
        }
        return answer / Math.pow(2, 30);
    }

    /**
     * This returns the list of images that make up the VM.
     * @param diskSection The set of disks as defined in the OVF
     * @param virtualMachine The virtual machine to find the disk size for
     * @return The size of this disk in Gb
     */
    private static HashSet<VmDiskImage> getVmDiskImages(VirtualSystem virtualMachine, DiskSection diskSection) {
        HashSet<VmDiskImage> answer = new HashSet<>();
        List<Disk> disks = Arrays.asList(diskSection.getDiskArray());
        ArrayList<String> diskIds = getVMsDiskIds(virtualMachine);
        for (Disk disk : disks) {
            if (diskIds.contains(disk.getDiskId())) {
                answer.add(new VmDiskImage(disk.getFileRef()));
            }
        }
        return answer;
    }    
    
    /**
     * This returns the disk capacity in bytes.
     *
     * @param disk The disk to obtain the size information for
     * @return The disk's capacity in bytes.
     */
    private static double getDiskCapacity(Disk disk) {
        double answer = Double.parseDouble(disk.getCapacity());
        if (disk.getCapacityAllocationUnits() != null) {
            switch (disk.getCapacityAllocationUnits()) {
                case "byte * 2^30": //Gb
                    return answer * Math.pow(2, 30);
                case "byte * 2^20": //MByte
                    return answer * Math.pow(2, 20);
                case "byte * 2^10": //KByte
                    return answer * Math.pow(2, 10);
            }
        }
        return answer;
    }

    /**
     * Returns the ID of the referenced file or disk from a host resource URI.
     *
     * @param hostResource The host resource URI to find an ID for
     * @return The the ID of the references file or disk
     */
    public static String findHostRosourceId(String hostResource) {
        if (hostResource.lastIndexOf('/') != -1) {
            return hostResource.substring(hostResource.lastIndexOf('/') + 1).replace("\n", "").trim();
        } else {
            return null;
        }
    }

    /**
     * This identifies from a VirtualSystem the disk ids associated.
     *
     * @param virtualMachine The virtual system to find the disk ids for
     * @return The list of ids for the virtual machines disk.
     */
    private static ArrayList<String> getVMsDiskIds(VirtualSystem virtualMachine) {
        ArrayList<String> ids = new ArrayList<>();
        for (Item item : virtualMachine.getVirtualHardwareSection().getItemArray()) {
            if (item.getResourceType().equals(ResourceType.DISK_DRIVE)) {
                for (String id : Arrays.asList(item.getHostResourceArray())) {
                    ids.add(findHostRosourceId(id));
                }
            }
        }
        return ids;
    }

}
