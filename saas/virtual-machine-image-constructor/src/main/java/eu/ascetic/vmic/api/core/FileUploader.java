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

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;

/**
 * Provides functionality to upload files referenced in an OVF document.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class FileUploader implements Runnable {

    protected static final Logger LOGGER = Logger.getLogger(FileUploader.class);

    private static final int THREAD_SLEEP_TIME = 500;

    private String ovfDefinitionId;
    private VmicApi vmicApi;
    private File file;
    private String progressDataId;

    /**
     * Constructor.
     * 
     * @param vmicApi
     *            The initial invocation of the API for accessing global state
     * @param ovfDefinitionId
     *            The OVF Definition ID
     * @param file
     *            The file to upload
     */
    public FileUploader(VmicApi vmicApi, String ovfDefinitionId, File file) {
        this.vmicApi = vmicApi;
        this.ovfDefinitionId = ovfDefinitionId;
        this.file = file;
        this.progressDataId = ovfDefinitionId + ":" + file.getName();
    }

    /**
     * Uploads a file using rsync and returns its remote path.
     * 
     * @return The remote URI of the File.
     */
    public void run() {

        // Initialise progress...
        vmicApi.getGlobalState().addFileProgress(progressDataId);
        LOGGER.debug("Added new new progress object, initial progress is: "
                + vmicApi.getGlobalState().getProgressData(progressDataId)
                        .getCurrentPercentageCompletion());
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                progressDataId));

        // Construct remote repository PATH
        String remotePath = vmicApi.getGlobalState().getConfiguration()
                .getRepositoryPath()
                + "/" + ovfDefinitionId;
        // Get host address
        String hostAddress = vmicApi.getGlobalState().getConfiguration()
                .getHostAddress();
        // Get path to rsync binary
        String rsyncPath = vmicApi.getGlobalState().getConfiguration()
                .getRsyncPath();
        // Get path to SSH binary
        String sshPath = vmicApi.getGlobalState().getConfiguration()
                .getSshPath();
        // Get path to SSH key
        String sshKeyPath = vmicApi.getGlobalState().getConfiguration()
                .getSshKeyPath();
        // Get path to SSH key
        String sshUser = vmicApi.getGlobalState().getConfiguration()
                .getSshUser();
        // Get file URI
        String fileUri = file.getAbsolutePath();
        // Get file's parent directory
        String filePath = file.getParent();

        ArrayList<String> arguments = new ArrayList<String>();

        // The rsync command to perform
        arguments.add("-avzP");
        // Enable SSH
        arguments.add("-e");
        // SSH binary and key path
        arguments.add(sshPath + " -i " + sshKeyPath);
        // Files to add
        arguments.add(fileUri);
        // Remote location to put the files
        arguments.add(sshUser + "@" + hostAddress + ":" + remotePath);

        // Construct and invoke system call to rsync
        SystemCall systemCall = new SystemCall(filePath);

        try {
            // Execute the system call
            systemCall.runCommand(rsyncPath, arguments);

            vmicApi.getGlobalState().setProgressPhase(progressDataId,
                    ProgressDataFile.UPLOADING_FILE_PHASE_ID);

            while (systemCall.getReturnValue() == -1) {
                try {
                    Double currentPercentageCompletion = 0.0;

                    // TODO: Parse the system call output and set file upload
                    // progress accordingly
                    systemCall.getOutput();

                    vmicApi.getGlobalState().setProgressPercentage(
                            progressDataId, currentPercentageCompletion);
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
                LOGGER.error("File upload failed!", e);
                vmicApi.getGlobalState().getProgressData(progressDataId)
                        .setError(true);
                vmicApi.getGlobalState().getProgressData(progressDataId)
                        .setException(e);
                return;
            }
        }

        if (systemCall.getReturnValue() != 0) {
            LOGGER.error("File upload failed! Rsync error code was: "
                    + systemCall.getReturnValue());
            vmicApi.getGlobalState().getProgressData(progressDataId)
                    .setError(true);
            vmicApi.getGlobalState()
                    .getProgressData(progressDataId)
                    .setException(
                            new ProgressException(
                                    "File upload failed! Rsync error code was: "
                                            + systemCall.getReturnValue()));
            return;
        }

        // Image generation complete...
        vmicApi.getGlobalState().setProgressPhase(progressDataId,
                ProgressDataFile.FINALISE_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(progressDataId,
                AbstractProgressData.COMPLETED_PERCENTAGE);
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                progressDataId));

        // Finally add remote file path to progressData object and set
        // completion flag.
        vmicApi.getGlobalState().setRemotePath(progressDataId, remotePath);
        vmicApi.getGlobalState().setComplete(progressDataId);
    }
}
