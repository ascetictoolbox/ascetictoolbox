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
import eu.ascetic.vmic.api.datamodel.ProgressData;

/**
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
        vmicApi.getGlobalState().addProgress(ovfDefinitionId);
        LOGGER.debug("Added new new progress object, initial progress is: "
                + vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .getCurrentPercentageCompletion());
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // 1) Retrieve Data from OVF
        retriveImageGenerationData();

        // 2) Select mode of image generation
        if (mode == 1) {
            // TODO: Offline mode
        } else {
            // TODO: Online mode
        }

        // Image generation complete...
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressData.FINALISE_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                ProgressData.COMPLETED_PERCENTAGE);
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
                ProgressData.RETRIEVE_DATA_PHASE_ID);

        // TODO: Parse OVF here and see what mode to operate in
        mode = 1;

        // Sleep a little for progress data to update..
        try {
            vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                    ProgressData.COMPLETED_PERCENTAGE);
            Thread.sleep(THREAD_SLEEP_TIME_LONG);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
