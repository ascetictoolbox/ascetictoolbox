/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.dataaggregator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.datamodel.ContextData;
import eu.ascetic.vmc.api.datamodel.VirtualMachine;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.EndPoint;
import eu.ascetic.vmc.api.datamodel.image.HardDisk;
import eu.ascetic.vmc.api.datamodel.image.Iso;
import eu.ascetic.utils.ovf.api.*;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;

/**
 * Class for parsing the contextualization data from the OVF Definition
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.6
 */
public class OvfDefinitionClient {

    protected static final Logger LOGGER = Logger
            .getLogger(OvfDefinitionClient.class);

    private OvfDefinition ovfDefinition;

    /**
     * Constructor for operating on a specific OVF definition
     * 
     * @param ovfDefinition
     *            The OVF Definition to use.
     */
    public OvfDefinitionClient(OvfDefinition ovfDefinition) {
        this.ovfDefinition = ovfDefinition;
    }

    /**
     * Parse the OvfDefinition to a usable object
     * 
     * TODO: Split this into discrete methods on a per context data type basis?
     * 
     * @return The ContextData parsed from the OVF Definition
     */
    public ContextData parse() {
        ContextData contextData = new ContextData();

        // Parse VM description here...
        LOGGER.debug("Parsing VM Description from OVF Definition...");
        int virtualSystems = ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemArray().length;

        LOGGER.debug("Number of virtualSystems is: " + virtualSystems);

        VirtualSystem[] virtualSystemArray = ovfDefinition
                .getVirtualSystemCollection().getVirtualSystemArray();

        // Iterate over all VM components getting details of disk images...
        for (int i = 0; i < virtualSystemArray.length; i++) {
            // Parse the number of instances per VM component here...
            String componentId = virtualSystemArray[i].getId();
            LOGGER.debug("Processing virtualMachineComponent with component ID: "
                    + componentId);

            // FIXME: Are we always using the first instances of ProductSection
            // here?
            // Fetch the UpperBound with a hard coded key
            int upperBound = virtualSystemArray[i].getProductSectionAtIndex(0)
                    .getUpperBound();
            LOGGER.debug("Allocation constraint upper bound is: " + upperBound);

            //TODO: Get virtual system collection wide probe and software. 
            
            // Add data to appropriate object(s) in data model.
            VirtualMachine virtualMachine = new VirtualMachine(componentId,
                    upperBound);
            LOGGER.debug("Created new VirtualMachine");

            Item[] item = virtualSystemArray[i].getVirtualHardwareSection()
                    .getItemArray();
            ArrayList<String> virtualMachineDiskIds = new ArrayList<String>();
            for (int k = 0; k < item.length; k++) {
                if (ResourceType.DISK_DRIVE.equals(item[k].getResourceType())) {
                    // FIXME: Only one host resource supported, i.e. no disk
                    // diffs
                    String virtualSystemDiskId = item[k]
                            .findHostRosourceId((item[k].getHostResourceArray()[0]));
                    virtualMachineDiskIds.add(virtualSystemDiskId);
                    LOGGER.debug("VirtualMachine has disk Id: "
                            + virtualSystemDiskId);
                }
            }

            // Parse the disk details i.e. URI
            Disk[] virtualDiskDescArray = ovfDefinition.getDiskSection()
                    .getDiskArray();
            for (int j = 0; j < virtualDiskDescArray.length; j++) {

                String diskId = virtualDiskDescArray[j].getDiskId();

                for (int k = 0; k < virtualMachineDiskIds.size(); k++) {
                    if (diskId.equals(virtualMachineDiskIds.get(k))) {
                        LOGGER.debug("Found OVF disk with ID: " + diskId);

                        String diskCapacity = virtualDiskDescArray[i]
                                .getCapacity();
                        LOGGER.debug("OVF disk capacity is: " + diskCapacity);
                        String diskFormatString = virtualDiskDescArray[i]
                                .getFormat().getSpecificationUrl();
                        LOGGER.debug("OVF disk format string is: "
                                + diskFormatString);
                        String diskFileRef = virtualDiskDescArray[i]
                                .getFileRef();
                        LOGGER.debug("OVF disk file ref is: " + diskFileRef);

                        References references = ovfDefinition.getReferences();
                        eu.ascetic.utils.ovf.api.File[] fileArray = references
                                .getFileArray();
                        String uri = null;
                        for (int l = 0; l < fileArray.length; l++) {
                            if (fileArray[l].getId().equals(diskFileRef)) {
                                uri = fileArray[l].getHref();
                            }
                        }

                        if (uri == null) {
                            LOGGER.error("Failed to find OVF URI for diskid: "
                                    + diskId);
                        }

                        LOGGER.debug("OVF URI for disk is: " + uri);
                        String fileName = new File(uri).getName();

                        HardDisk hardDisk = new HardDisk(diskId, fileName, uri,
                                null, diskCapacity, diskFormatString);

                        LOGGER.debug("Created new HardDisk");
                        virtualMachine.getHardDisks().put(diskId, hardDisk);
                    }
                }
            }

            LOGGER.debug("Added HardDisk to VirtualMachine");
            contextData.getVirtualMachines().put(componentId, virtualMachine);
            LOGGER.debug("Added VM to contextData with ID: " + componentId);

            // TODO: Get security keys either collection or virtual system
            // specific
            LOGGER.warn("Parsing security keys not currently implemented");

            // Get service end points for this virtual machine component
            int endpointNumber = virtualSystemArray[i]
                    .getProductSectionAtIndex(0).getEndPointNumber();
            for (int j = 0; j < endpointNumber; j++) {
                String id = virtualSystemArray[i]
                        .getProductSectionAtIndex(0).getEndPointId(i);
                String uri = virtualSystemArray[i]
                        .getProductSectionAtIndex(0).getEndPointUri(i);
                String type = virtualSystemArray[i]
                        .getProductSectionAtIndex(0).getEndPointType(i);
                String subtype = virtualSystemArray[i]
                        .getProductSectionAtIndex(0).getEndPointSubtype(i);
                String interval = virtualSystemArray[i]
                        .getProductSectionAtIndex(0).getEndPointInterval(i);
                LOGGER.debug("Adding end point: " + id + " for "
                        + componentId);
                EndPoint endPoint = new EndPoint(id, uri, type, subtype, interval);
                virtualMachine.getEndPoints().put(id, endPoint);
            }

            // TODO: Get software dependencies
            LOGGER.warn("Parsing software dependencies not currently implemented");

        }

        return contextData;
    }

    /**
     * Adds the ISO name and URI to the Ovf Definition.
     * 
     * @param virtualMachines
     *            The VM's for which we want to add the ISO for in the OVF
     *            Definition.
     * @return The altered ovfDefinition.
     */
    public OvfDefinition addIsosToOvfDefinition(
            Map<String, VirtualMachine> virtualMachines) {
        // Iterate over all the virtual machines for this service
        for (VirtualMachine virtualMachine : virtualMachines.values()) {
            String componentId = virtualMachine.getComponentId();
            LOGGER.info("Adding ISO to virtual machine with component ID: "
                    + componentId);

            HashMap<String, Iso> isoImages = (HashMap<String, Iso>) virtualMachine
                    .getIsoImages();

            // Add the ISO base name to the OVF references
            Iso iso = isoImages.get("1");

            String baseUri = iso.getUri().substring(0,
                    iso.getUri().lastIndexOf("_"));
            String baseId = componentId + "-iso";

            LOGGER.info("Adding ISO base name URI to OVF: " + baseUri);
            ovfDefinition.getReferences().addFile(
                    eu.ascetic.utils.ovf.api.File.Factory.newInstance(baseId,
                            baseUri));

            // Add the ISO file reference ID to a new Disk element
            Disk disk = Disk.Factory.newInstance();
            String diskId = baseId + "-disk";
            disk.setDiskId(diskId);
            disk.setFileRef(baseId);
            disk.setFormat(DiskFormatType.ISO9660);
            disk.setCapacityAllocationUnits("byte * 2^30");
            disk.setCapacity("4");
            // Provide a rough estimate
            disk.setPopulatedSize(new File(iso.getUri()).length());
            ovfDefinition.getDiskSection().addDisk(disk);

            // Add disk element id to a new Item element
            VirtualSystem[] virtualSystemArray = ovfDefinition
                    .getVirtualSystemCollection().getVirtualSystemArray();

            for (int i = 0; i < virtualSystemArray.length; i++) {
                if (virtualSystemArray[i].getId().equals(componentId)) {
                    Item item = Item.Factory.newInstance();

                    item.setDescription("VM CDROM");
                    item.setElementName("Context Base CD 1");
                    item.addHostResource("ovf:/disk/" + diskId);
                    Integer lastInstanceId = new Integer(virtualSystemArray[i]
                            .getVirtualHardwareSection()
                            .getItemAtIndex(
                                    virtualSystemArray[i]
                                            .getVirtualHardwareSection()
                                            .getItemArray().length - 1)
                            .getInstanceID());
                    item.setInstanceId(new Integer(lastInstanceId + 1)
                            .toString());
                    item.setResourceType(ResourceType.CD_DRIVE);
                    virtualSystemArray[i].getVirtualHardwareSection().addItem(
                            item);
                }
            }
        }
        return ovfDefinition;
    }

    /**
     * Adds the HardDisk name and URI to the OVF Definition.
     * 
     * @param virtualMachines
     *            The VM's for which we want to add the HardDisks for in the
     *            OVF.
     * @return The altered ovfDefinition.
     */
    public OvfDefinition addHardDisksToOvfDefinition(
            Map<String, VirtualMachine> virtualMachines) {

        for (VirtualMachine virtualMachine : virtualMachines.values()) {
            String componentId = virtualMachine.getComponentId();
            LOGGER.info("Adding HardDisk href to virtual machine with component ID: "
                    + componentId);

            HashMap<String, HardDisk> hardDisks = (HashMap<String, HardDisk>) virtualMachine
                    .getHardDisks();

            for (HardDisk hardDisk : hardDisks.values()) {
                // FIXME: OVF definition only supports a single image, this
                // should
                // change?
                LOGGER.info("Adding new HardDisk URI to OVF: "
                        + hardDisk.getUri());

                eu.ascetic.utils.ovf.api.File[] fileArray = ovfDefinition
                        .getReferences().getFileArray();
                for (int i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].getId().equals(hardDisk.getImageId())) {
                        fileArray[i].setHref(hardDisk.getUri());
                    }
                }

                // TODO: Alter the Disk element format
            }

        }

        return ovfDefinition;
    }
}
