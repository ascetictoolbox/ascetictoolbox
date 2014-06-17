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
package eu.ascetic.vmc.api.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.dataaggregator.OvfDefinitionClient;
import eu.ascetic.vmc.api.datamodel.ProgressData;
import eu.ascetic.vmc.api.datamodel.Service;
import eu.ascetic.vmc.api.datamodel.VirtualMachine;
import eu.ascetic.vmc.api.datamodel.image.HardDisk;
import eu.ascetic.vmc.api.datamodel.image.Iso;
import eu.ascetic.vmc.api.imageconverter.ImageConversion;
import eu.ascetic.vmc.api.isocreator.IsoImageCreation;

/**
 * Core logic of the VMC.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.7
 */
public class VirtualMachineContextualizer implements Runnable {

	private static final int NUMBER_OF_PHASES = 5;

	private static final double NUMBER_OF_PHASES_USED_IN_PROGRESS_UPDATE = 4.0;

	private static final int THREAD_SLEEP_TIME_VERY_LONG = 500;

	private static final int THREAD_SLEEP_TIME_LONG = 250;

	private static final int THREAD_SLEEP_TIME_SHORT = 100;

	protected static final Logger LOGGER = Logger
			.getLogger(VirtualMachineContextualizer.class);

	private VmcApi vmcApi;
	private OvfDefinition ovfDefinition;
	private Service service;
	private String serviceId;

	private String imageFormat = null;

	/**
	 * Constructor
	 * 
	 * @param vmcApi
	 *            The initial invocation of the API for accessing global state.
	 * @param ovfDefinition
	 *            The OVF Definition.
	 */
	public VirtualMachineContextualizer(VmcApi vmcApi, OvfDefinition ovfDefinition) {
		this.vmcApi = vmcApi;
		this.ovfDefinition = ovfDefinition;
	}

	/**
	 * Overloaded constructor for supporting image conversion.
	 * 
	 * 
	 * @param vmcApi
	 *            The initial invocation of the API for accessing global state.
	 * @param ovfDefinition
	 *            The OVF Definition.
	 * @param imageFormat
	 *            Format to convert images to. Possible values are: "raw",
	 *            "vmdk" and "qcow2"
	 */
	public VirtualMachineContextualizer(VmcApi vmcApi, OvfDefinition ovfDefinition,
			String imageFormat) {
		this.vmcApi = vmcApi;
		this.ovfDefinition = ovfDefinition;
		this.imageFormat = imageFormat;
	}

	/**
	 * Core logic for performing contextualization of a service in a thread.
	 */
	public void run() {
		// FIXME: Should we be getting the serviceId from a property here, confirm with consortium? 
		serviceId = ovfDefinition.getVirtualSystemArray(0).getProductSection().getPropertyByKey("serviceId").getValue();
		LOGGER.debug("Service ID is: " + serviceId);

		// Initialise progress...
		vmcApi.getGlobalState().addProgress(serviceId);
		LOGGER.debug("Added new new progress object, initial progress is: "
				+ vmcApi.getGlobalState().getProgressData(serviceId)
						.getProgressString());
		LOGGER.info(vmcApi.getGlobalState().getProgressLogString(serviceId));

		// Five primary stages of contextualization...

		// 1) Retrieve Data
		retriveContextualizationData();
		LOGGER.info(vmcApi.getGlobalState().getProgressLogString(serviceId));

		// 2) Convert VM Images
		try {
			convertVmImages();
		} catch (SystemCallException e) {
			LOGGER.error("Failed to convert VM image!", e);
			vmcApi.getGlobalState().getProgressData(serviceId).setError(true);
			vmcApi.getGlobalState().getProgressData(serviceId).setException(e);
			return;
		}
		LOGGER.info(vmcApi.getGlobalState().getProgressLogString(serviceId));

		// 3) Create ISO Images
		try {
			createIsoImages();
		} catch (SystemCallException e) {
			LOGGER.error("Failed to create ISO image!", e);
			vmcApi.getGlobalState().getProgressData(serviceId).setError(true);
			vmcApi.getGlobalState().getProgressData(serviceId).setException(e);
			return;
		}
		LOGGER.info(vmcApi.getGlobalState().getProgressLogString(serviceId));

		// Contextualization complete...
		vmcApi.getGlobalState().setProgressPhase(serviceId,
				ProgressData.FINALISE_PHASE_ID);
		vmcApi.getGlobalState().setProgressPercentage(serviceId,
				ProgressData.COMPLETED_PERCENTAGE);
		LOGGER.info(vmcApi.getGlobalState().getProgressLogString(serviceId));

		// Finally add ovfDefinition to progressData object and set completion flag.
		vmcApi.getGlobalState().setOvfDefinition(ovfDefinition);
		vmcApi.getGlobalState().setComplete(serviceId);
	}

	/**
	 * Gathers the contextualization data from multiple sources including the
	 * OVF Definition and interCloudSecurity component.
	 */
	private void retriveContextualizationData() {
		vmcApi.getGlobalState().setProgressPhase(serviceId,
				ProgressData.RETRIEVE_DATA_PHASE_ID);

		service = new Service(ovfDefinition);
		// Get context data from the ovfDefinition:
		service.parseOvfDefinition();
		// Get security context data from other sources:
		service.generateContextData();

		// Sleep a little for progress data to update..
		try {
			vmcApi.getGlobalState().setProgressPercentage(serviceId,
					ProgressData.COMPLETED_PERCENTAGE);
			Thread.sleep(THREAD_SLEEP_TIME_LONG);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Converts the service's VM images to the desired format.
	 */
	private void convertVmImages() throws SystemCallException {
		vmcApi.getGlobalState().setProgressPhase(serviceId,
				ProgressData.CONVERT_IMAGE_PHASE_ID);

		if (imageFormat != null) {
			HashMap<String, VirtualMachine> virtualMachines = (HashMap<String, VirtualMachine>) service
					.getContextData().getVirtualMachines();
			// Iterate over all the virtual machines for this service

			double progress = 0;
			double processsed = 0;

			for (VirtualMachine virtualMachine : virtualMachines.values()) {
				// Convert the images to the desired format
				HashMap<String, HardDisk> hardDisks = (HashMap<String, HardDisk>) virtualMachine
						.getHardDisks();

				// TODO: OVF Definition only supports a single image per component?
				for (HardDisk hardDisk : hardDisks.values()) {

					ImageConversion imageConversion = new ImageConversion(
							vmcApi.getGlobalState().getConfiguration(),
							imageFormat, hardDisk);

					Thread thread = new Thread(imageConversion);
					thread.start();

					// Monitor for completion
					while (imageConversion.getSystemCall().getReturnValue() != 0) {

						while (true) {

							ArrayList<String> output = (ArrayList<String>) imageConversion
									.getSystemCall().getOutput();
							if (output.isEmpty()
									|| ! output.get(0).contains(")")) {
								if (imageConversion.getSystemCall()
										.getReturnValue() == SystemCall.RETURN_VALUE_ON_ERROR) {
									LOGGER.error("SystemCall Error!");
									break;
								}

								// Sleep until output is populated with data
								try {
									Thread.sleep(THREAD_SLEEP_TIME_SHORT);
								} catch (InterruptedException e) {
									LOGGER.error(e.getMessage(), e);
								}
							} else {
								break;
							}

						}

						// Break again if we have an error so we don't report
						// total crap....
						if (imageConversion.getSystemCall().getReturnValue() == SystemCall.RETURN_VALUE_ON_ERROR
								|| imageConversion.getSystemCall()
										.getReturnValue() == 1) {
							// Ignore if we are testing
							if (vmcApi.getGlobalState().getConfiguration()
									.isDefaultValues()) {
								break;
							} else {
								throw new SystemCallException();
							}
						}

						ArrayList<String> output = (ArrayList<String>) imageConversion
								.getSystemCall().getOutput();
						double qemuProgress = Double.valueOf(output.get(
								output.size() - 1).substring(
								output.get(output.size() - 1).indexOf("(") + 1,
								output.get(output.size() - 1).indexOf("/")));
						LOGGER.debug("qemu progress value is: " + qemuProgress);
						double totalProgress = progress
								+ (((ProgressData.COMPLETED_PERCENTAGE / virtualMachines
										.size()) / ProgressData.COMPLETED_PERCENTAGE) * qemuProgress);
						vmcApi.getGlobalState().setProgressPercentage(
								serviceId, totalProgress);

						try {
							Thread.sleep(THREAD_SLEEP_TIME_VERY_LONG);
						} catch (InterruptedException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}

					HardDisk alteredHardDisk = imageConversion.getHardDisk();
					hardDisks
							.put(alteredHardDisk.getImageId(), alteredHardDisk);
				}
				virtualMachine.setHardDisks(hardDisks);
				processsed++;
				progress = (ProgressData.COMPLETED_PERCENTAGE / virtualMachines
						.size()) * processsed;
				vmcApi.getGlobalState().setProgressPercentage(serviceId,
						progress);
			}
			service.getContextData().setVirtualMachines(virtualMachines);

			// Add the HardDisk image URI's to the ovfDefinition
			LOGGER.info("Adding altered HardDisk URI's to ovfDefinition with id: "
					+ ovfDefinition.getVirtualSystemArray(0).getProductSection().getPropertyByKey("serviceId").getValue());
			OvfDefinitionClient ovfDefinitionClient = new OvfDefinitionClient(
					ovfDefinition);
			ovfDefinition = ovfDefinitionClient
					.addHardDisksToOvfDefinition(virtualMachines);
			vmcApi.getGlobalState().setProgressPercentage(serviceId,
					ProgressData.COMPLETED_PERCENTAGE);
		} else {
			// We're not doing any image conversion...
			try {
				for (int i = 1; i < NUMBER_OF_PHASES; i++) {
					Thread.sleep(THREAD_SLEEP_TIME_LONG);
					vmcApi.getGlobalState().setProgressPercentage(
							serviceId,
							(i / NUMBER_OF_PHASES_USED_IN_PROGRESS_UPDATE)
									* ProgressData.COMPLETED_PERCENTAGE);
				}
				Thread.sleep(THREAD_SLEEP_TIME_LONG);
				vmcApi.getGlobalState().setProgressPercentage(serviceId,
						ProgressData.COMPLETED_PERCENTAGE);
			} catch (InterruptedException e) {
				// Do nothing...
			}
		}
	}

	/**
	 * Creates the ISO images that store the contextualization data on a per VM
	 * instance basis.
	 * 
	 * @throws SystemCallException
	 */
	private void createIsoImages() throws SystemCallException {
		double isosToProcess = 0;
		double isosProcessed = 0;
		vmcApi.getGlobalState().setProgressPhase(serviceId,
				ProgressData.CREATE_ISOS_PHASE_ID);

		HashMap<String, VirtualMachine> virtualMachines = (HashMap<String, VirtualMachine>) service
				.getContextData().getVirtualMachines();

		// Figure out how many Iso's will be create for updating progressData.
		// TODO: Maybe store this when gathering context data from the ovfDefinition?
		for (VirtualMachine virtualMachine : virtualMachines.values()) {
			// Create all the ISO images up to the upper bound limit.
			isosToProcess = isosToProcess + virtualMachine.getUpperBound();
		}

		// Iterate over all the virtual machines for this service
		for (VirtualMachine virtualMachine : virtualMachines.values()) {

			// Create all the ISO images up to the upper bound limit.
			for (int i = 1; i <= virtualMachine.getUpperBound(); i++) {
				// TODO: Confirm mount point location is /media/context/?
				// TODO: Confirm what string format the VM instance IDs will be
				// using...

				String isoFileName = service.getServiceId() + "_"
						+ virtualMachine.getComponentId() + "_" + i + ".iso";
				String isoPath = vmcApi.getGlobalState().getConfiguration()
						.getRepository()
						+ File.separator + isoFileName;

				Iso iso = new Iso(Integer.toString(i), isoFileName, isoPath,
						"ISO9660", "/media/context/");
				LOGGER.info("Creating ISO with ID: " + iso.getImageId()
						+ " and file name: " + isoFileName);

				IsoImageCreation isoImageCreation = new IsoImageCreation(iso,
						vmcApi.getGlobalState().getConfiguration(), ovfDefinition);

				// Store the contextualization data
				isoImageCreation.storeContextData(service.getContextData(),
						virtualMachine);

				// Create the ISO..
				iso = isoImageCreation.create();

				// Add the ISO to the VMs ISO hashMap.
				virtualMachine.getIsoImages().put(iso.getImageId(), iso);
				LOGGER.info("Completed creating ISO with URI: " + iso.getUri());

				// Calculate the progress...
				isosProcessed++;
				vmcApi.getGlobalState().setProgressPercentage(
						serviceId,
						(isosProcessed / isosToProcess)
								* ProgressData.COMPLETED_PERCENTAGE);
			}
		}

		// Add the ISO URI's to the OVF Definition
		// FIXME: Should we be getting the serviceId from a property here, confirm with consortium? 
		LOGGER.info("Adding ISOs to OVF Definition with id: "
				+ ovfDefinition.getVirtualSystemArray(0).getProductSection().getPropertyByKey("serviceId").getValue());
		OvfDefinitionClient ovfDefinitionClient = new OvfDefinitionClient(
				ovfDefinition);
		ovfDefinition = ovfDefinitionClient.addIsosToOvfDefinition(virtualMachines);
	}
}
