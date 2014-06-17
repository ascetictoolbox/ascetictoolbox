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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.datamodel.ContextData;
import eu.ascetic.vmc.api.datamodel.VirtualMachine;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.EndPoint;
import eu.ascetic.vmc.api.datamodel.image.HardDisk;
import eu.ascetic.vmc.api.datamodel.image.Iso;
import eu.ascetic.utils.ovf.api.*;

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
	 * @return contextData the ContextData parsed from the OVF Definition
	 */
	public ContextData parse() {
		ContextData contextData = new ContextData();

		// Parse VM description here...
		LOGGER.debug("Parsing VM Description from OVF Definition...");
		int virtualSystems = ovfDefinition.getVirtualSystemArray().length;
		
		LOGGER.debug("Number of virtualSystems is: "
				+ virtualSystems);
		
		VirtualSystem[] virtualSystemArray = ovfDefinition.getVirtualSystemArray();
		
		// Iterate over all VM components getting details of disk images...
		for (int i = 0; i < virtualSystemArray.length; i++) {
			// Parse the number of instances per VM component here...
			String componentId = virtualSystemArray[i].getId();
			LOGGER.debug("Processing virtualMachineComponent with component ID: "
					+ componentId);
			
			int upperBound = Integer.parseInt(virtualSystemArray[i].getProductSection().getPropertyByKey("upperBound").getValue());
			LOGGER.debug("Allocation constraint upper bound is: " + upperBound);

			// Add data to appropriate object(s) in data model.
			VirtualMachine virtualMachine = new VirtualMachine(componentId,
					upperBound);
			LOGGER.debug("Created new VirtualMachine");
			
			// Parse the disk details i.e. URI		
			VirtualDiskDesc[] virtualDiskDescArray = ovfDefinition.getDiskSection().getDiskArray();
			for (int j = 0; j < virtualDiskDescArray.length; j++) {
				String diskId = virtualDiskDescArray[i].getDiskId();
				LOGGER.debug("Found OVF disk with ID: " + diskId);
				String diskCapacity = virtualDiskDescArray[i].getCapacity();
				LOGGER.debug("OVF disk capacity is: " + diskCapacity);
				String diskFormatString = virtualDiskDescArray[i].getFormat();
				LOGGER.debug("OVF disk format string is: " + diskFormatString);
				String diskFileRef = virtualDiskDescArray[i].getFileRef();

				References references = ovfDefinition.getReferences();
				eu.ascetic.utils.ovf.api.File[] fileArray = references.getFileArray();
				String uri = null;
				for (int k = 0; k < fileArray.length; k++) {
					if(fileArray[i].getId().equals(diskFileRef)) {
						fileArray[i].getHref();
					}
				}
				
				if(uri == null){
					LOGGER.error("Failed to find OVF URI for diskid: " + diskId);
				}
				
				LOGGER.debug("OVF URI for disk is: " + uri);
				String fileName = new File(uri).getName();
				
				HardDisk hardDisk = new HardDisk(diskId, fileName, uri, null,
						diskCapacity, diskFormatString);
				
				LOGGER.debug("Created new HardDisk");
				virtualMachine.getHardDisks().put(diskId, hardDisk);
			}

			LOGGER.debug("Added HardDisk to VirtualMachine");
			contextData.getVirtualMachines().put(componentId, virtualMachine);
			LOGGER.debug("Added VM to contextData with ID: " + componentId);

			// Get service end points for this virtual machine component	
			int k = 1;
			while (true) {				
				// FIXME: Need a better way to fetch end points so that they have a useful name 
				ProductProperty productProperty = virtualSystemArray[i].getProductSection().getPropertyByKey("endpoint" + k);
				if (productProperty == null) {
					break;
				} else {
					String endPointName = productProperty.getKey();
					String endPointUri = productProperty.getValue();

					EndPoint endPoint = new EndPoint(endPointName, endPointUri);
					virtualMachine.getEndPoints().put(endPointName, endPoint);
				}
			}
		}

		return contextData;
	}

	/**
	 * Adds the ISO name and URI to the Ovf Definition.
	 * 
	 * @param virtualMachines
	 *            The VM's for which we want to add the ISO for in the OVF Definition.
	 * @return The altered ovfDefinition.
	 */
	public OvfDefinition addIsosToOvfDefinition(
			Map<String, VirtualMachine> virtualMachines) {
		// Iterate over all the virtual machines for this service
		for (VirtualMachine virtualMachine : virtualMachines.values()) {
			String componentId = virtualMachine.getComponentId();
			LOGGER.info("Adding ISO to virtual machine with component ID: "
					+ componentId);

			HashMap<String, Iso> isoImages = (HashMap<String, Iso>) virtualMachine.getIsoImages();

			// Add the ISO base name to the OVF
			Iso iso = isoImages.get("1");
			String[] temp;
			String delimiter = "_";
			temp = iso.getFileName().split(delimiter);
			String baseName = temp[0] + "_" + temp[1] + ".iso";
			temp = iso.getUri().split(iso.getFileName());
			String baseUri = temp[0];

			LOGGER.info("Adding ISO base name URI to OVF: " + baseUri + baseName);
			// FIXME: Remove the reference to the contextulizationFile should be agnostic of location somehow?
			ovfDefinition.getReferences().getContextualizationFile().setHref(baseUri + baseName);
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

			HashMap<String, HardDisk> hardDisks = (HashMap<String, HardDisk>) virtualMachine.getHardDisks();

			for (HardDisk hardDisk : hardDisks.values()) {
				// FIXME: OVF definition only supports a single image, this should
				// change?
				LOGGER.info("Adding new HardDisk URI to OVF: " + hardDisk.getUri());
				ovfDefinition.getReferences().getImageFile()
						.setHref(hardDisk.getUri());
			}
		}

		return ovfDefinition;
	}
}
