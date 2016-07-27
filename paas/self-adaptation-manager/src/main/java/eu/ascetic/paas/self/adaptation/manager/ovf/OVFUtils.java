/**
 * Copyright 2016 University of Leeds
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

package eu.ascetic.paas.self.adaptation.manager.ovf;

import es.bsc.vmmclient.models.VmRequirements;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This class provides services for the interpretation of OVF.
 * @author Richard Kavanagh
 */
public class OVFUtils {

    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(OVFUtils.class);

    /**
     * Gets the ovf definition object.
     *
     * @param ovf the ovf
     * @return the ovf definition
     */
    public static OvfDefinition getOvfDefinition(String ovf) {
        if (ovf == null || ovf.equals("")) {
            return null;
        }
        try {
            OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
            return ovfDocument;
        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }    

    /**
     * Extracts the field ovf:Name from VirtualSystemCollection to differenciate
     * between applications.
     *
     * @param ovf String representing the OVF definition of an Application
     * @return the application name
     */
    public static String getApplicationName(OvfDefinition ovf) {

        try {
            return ovf.getVirtualSystemCollection().getId();
        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }    

    /**
     * This gets the VirtualSystem object representation for an specific ovf VM 
     * type.
     *
     * @param ovf The ovf definition to be checked
     * @param ovfId The ovf id/ VM type to extract from the OVF
     * @return The Virtual System for the given VM type, otherwise null if it does
     * not exist.
     */
    public static VirtualSystem getVMFromOvfType(OvfDefinition ovf, String ovfId) {

        if (ovf == null || ovfId == null) {
            return null;
        }
        try {
            VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
            for (int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
                VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
                String ovfVirtualSystemID = virtualSystem.getId();

                if (ovfId.equals(ovfVirtualSystemID)) {
                    return virtualSystem;
                }
            }

        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * This gets the VirtualSystem object representation for an specific ovf VM 
     * type.
     *
     * @param ovf The ovf definition to be checked
     * @param ovfId The ovf id/ VM type to extract from the OVF
     * @return The Virtual System for the given VM type, otherwise null if it does
     * not exist.
     */
    public static VmRequirements getVMRequirementsFromOvfType(OvfDefinition ovf, String ovfId) {

        if (ovf == null || ovfId == null) {
            return null;
        }
        try {
            VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
            for (int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
                VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
                String ovfVirtualSystemID = virtualSystem.getId();

                if (ovfId.equals(ovfVirtualSystemID)) {
                    int cpu = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
                    int ram = virtualSystem.getVirtualHardwareSection().getMemorySize();
                    int disk = (int) getVmDiskSize(virtualSystem, ovf.getDiskSection());
                    int swap = 0; //TODO - Swap is not specified in the OVF
                    return new VmRequirements(cpu, ram, disk, swap);
                }
            }
        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * This returns the size of the overall disk space available to the VM.
     *
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
     * Returns the ProductSection for an specific ovfID
     *
     * @param ovf String representing the ovf file where to look
     * @param ovfId Ovf ID of the wanted product section
     * @return Returns a ProductSection object if the sections exits or null
     * otherwise
     */
    public static ProductSection getProductionSectionFromOvfType(OvfDefinition ovf, String ovfId) {

        VirtualSystem virtualSystem = getVMFromOvfType(ovf, ovfId);

        if (virtualSystem != null) {
            return virtualSystem.getProductSectionAtIndex(0);
        }

        return null;
    }


}
