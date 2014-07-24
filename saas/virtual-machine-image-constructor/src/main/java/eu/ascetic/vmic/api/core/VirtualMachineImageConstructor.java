/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api.core;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

/**
 * Core logic of the Virtual Machine Image Constructor (VMIC).
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualMachineImageConstructor implements Runnable {

    protected static final Logger LOGGER = Logger
            .getLogger(VirtualMachineImageConstructor.class);

    private static final int THREAD_SLEEP_TIME_LONG = 250;

    /**
     * Stores the mode of operation for generating images.<br>
     * Offline = 1<br>
     * Online = 2.
     */
    private int mode = -1;

    private VmicApi vmicApi;
    private OvfDefinition ovfDefinition;
    private String ovfDefinitionId;

    /**
     * Constructor
     * 
     * @param vmicApi
     *            The initial invocation of the API for accessing global state.
     * @param ovfDefinition
     *            The OVF Definition.
     */
    public VirtualMachineImageConstructor(VmicApi vmicApi,
            OvfDefinition ovfDefinition) {
        this.vmicApi = vmicApi;
        this.ovfDefinition = ovfDefinition;
        this.ovfDefinitionId = ovfDefinition.getVirtualSystemCollection()
                .getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Initialise progress...
        vmicApi.getGlobalState().addImageProgress(ovfDefinitionId);
        LOGGER.debug("Added new new progress object, initial progress is: "
                + vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .getCurrentPercentageCompletion());
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // 1) Retrieve Data from OVF
        retriveImageGenerationData();

        // 2) Select mode of image generation
        if (mode == 1) {
            // Offline mode
            generateImageOffline();
        } else {
            // Online mode
            generateImageOnline();
        }

        // Image generation complete...
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.FINALISE_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                AbstractProgressData.COMPLETED_PERCENTAGE);
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // Finally add ovfDefinition to progressData object and set completion
        // flag.
        vmicApi.getGlobalState().setOvfDefinition(ovfDefinition);
        vmicApi.getGlobalState().setComplete(ovfDefinitionId);
    }

    /**
     * Gathers the image generation data from the OVF Definition.
     */
    private void retriveImageGenerationData() {
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.RETRIEVE_DATA_PHASE_ID);

        // TODO: Parse OVF here and see what mode to operate in. Also see what
        // & how to operate over files.
        OvfDefinitionParser ovfDefinitionParser = new OvfDefinitionParser(
                ovfDefinition);
        mode = ovfDefinitionParser.parse();

        // Sleep a little for progress data to update..
        try {
            vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                    AbstractProgressData.COMPLETED_PERCENTAGE);
            Thread.sleep(THREAD_SLEEP_TIME_LONG);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Method to generate images that mounts the image while it is offline.
     */
    private void generateImageOffline() {

        // TODO: Mount the image(s) using remote a system call

        // TODO: Add the file(s) to the image(s) using remote a system call

        // TODO: Unmount the image(s) using a remote system call
    }

    /**
     * Method to generate images using chef while the image is online and
     * booted.
     */
    private void generateImageOnline() {

        // TODO: Boot up the image from a snapshot (using libvirt?)

        // TODO: Bootstrap the image to chef via a remote system call to knife.

        // TODO: Upload the chef recipes to the chef workspace (using rsync?)

        // TODO: Push out the chef recipe(s) via a remote system call to knife.

        // TODO: Shutdown the VM (using libvirt?)

        // TODO: Remove the node from the chef server
    }

}
