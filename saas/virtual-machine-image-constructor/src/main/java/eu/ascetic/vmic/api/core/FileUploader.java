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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;

/**
 * Provides functionality to upload files for referencing in an OVF document.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class FileUploader implements Runnable {

    protected static final Logger LOGGER = Logger.getLogger(FileUploader.class);

    private static final int THREAD_SLEEP_TIME = 500;

    private String ovfDefinitionId;
    private VmicApi vmicApi;

    /**
     * Constructor.
     * 
     * @param vmicApi
     *            The initial invocation of the API for accessing global state
     * @param ovfDefinitionId
     *            The OVF Definition ID
     */
    public FileUploader(VmicApi vmicApi, String ovfDefinitionId) {
        this.vmicApi = vmicApi;
        this.ovfDefinitionId = ovfDefinitionId;
    }

    /**
     * Uploads a file using rsync and returns its remote path.
     * 
     * @return The remote URI of the File.
     */
    public void run() {

        // Initialise progress...
        vmicApi.getGlobalState().addFileProgress(ovfDefinitionId);
        LOGGER.debug("Added new new progress object, initial progress is: "
                + vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .getCurrentPercentageCompletion());
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // Construct remote repository PATH
        String remotePath = vmicApi.getGlobalState().getConfiguration()
                .getRepositoryPath()
                + "/" + ovfDefinitionId;

        String rsyncPath = vmicApi.getGlobalState().getConfiguration()
                .getRsyncPath();

        ArrayList<String> arguments = new ArrayList<String>();

        arguments.add("-avz"); // The rsync command
        arguments.add("-avz"); // TODO: SSH key?
        arguments.add(""); // TODO: Directory of files to add
        arguments.add(""); // TODO: Remote location to put the files

        // Construct and invoke system call to rsync

        // TODO: Set some path here?
        SystemCall systemCall = new SystemCall("");

        try {
            systemCall.runCommand(rsyncPath, arguments);

            // TODO: Set file upload progress here...
            while (systemCall.getReturnValue() == -1) {
                try {
                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

        } catch (SystemCallException e) {
            if (vmicApi.getGlobalState().getConfiguration().isDefaultValues()) {
                LOGGER.warn(
                        "Failed to run command, is this invocation in a unit test?",
                        e);
            } else {

            }
        }

        if (systemCall.getReturnValue() != 0) {
            LOGGER.error("Hardisk conversion Failed! Error code was: "
                    + systemCall.getReturnValue());
        }

        // Image generation complete...
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataFile.FINALISE_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                AbstractProgressData.COMPLETED_PERCENTAGE);
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // Finally add remote file path to progressData object and set completion
        // flag.
        vmicApi.getGlobalState().setRemotePath(ovfDefinitionId, remotePath);
        vmicApi.getGlobalState().setComplete(ovfDefinitionId);
    }
}
