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
import java.util.Vector;

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
        final String rsyncPath = vmicApi.getGlobalState().getConfiguration()
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
        // Get file's parent directory
        String fileAbsolutePath = file.getAbsolutePath();

        Vector<String> arguments = new Vector<String>();

        // The rsync command to perform
        arguments.add("-avzPh");
        // Add command to create the remote path if it doesn't exist
        arguments.add("--rsync-path");
        arguments.add("\"mkdir -p " + remotePath + " && rsync\"");
        // Enable SSH
        arguments.add("-e");
        // SSH binary and key path (use blowfish for faster uploading"
        arguments
                .add("\""
                        + sshPath
                        + " -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -c blowfish"
                        + " -i " + sshKeyPath + "\"");
        // Files to add
        if (fileAbsolutePath.contains("cygwin")){
            fileAbsolutePath = fileAbsolutePath.replace("\\", "/");
            String newFileAbsolutePath = "/cygdrive/" + fileAbsolutePath.substring(0, fileAbsolutePath.indexOf(':')) + "/" + fileAbsolutePath.substring(fileAbsolutePath.indexOf(':') + 2, fileAbsolutePath.length());
            arguments.add(newFileAbsolutePath);
            LOGGER.debug("Cygwin rsync binary detected, altering file path to: " + newFileAbsolutePath);
        } else {
            arguments.add(fileAbsolutePath);
            LOGGER.debug("Using absolute path to file to be uploaded" + fileAbsolutePath);
        }
        // Remote location to put the files
        arguments.add(sshUser + "@" + hostAddress + ":" + remotePath);

        // Construct and invoke system call to rsync
        SystemCall systemCall = new SystemCall(file.getParent(), rsyncPath, arguments);
        Thread thread = new Thread(systemCall);

        try {
            // Execute the system call in a thread
            thread.start();

            vmicApi.getGlobalState().setProgressPhase(progressDataId,
                    ProgressDataFile.UPLOADING_FILE_PHASE_ID);

            while (systemCall.getReturnValue() == -1 && !systemCall.isError()) {
                try {
                    // Parse the system call output and set file upload
                    // progress accordingly
                    Vector<String> output = (Vector<String>) systemCall
                            .getOutput();

                    if (output.size() == 0) {
                        Thread.sleep(THREAD_SLEEP_TIME);
                        continue;
                    }

                    String transfered = null;
                    String percentage = null;
                    String speed = null;
                    String eta = null;

                    // Work backwards until we find '%'
                    for (int i = output.size() - 1; i >= 0; i--) {
                        String line = output.get(i);
                        line = line.trim();
                        if (line.indexOf('%') != -1) {
                            String[] lineSplit = line.split(" +");

                            try {
                                transfered = lineSplit[0];
                                percentage = lineSplit[1];
                                percentage = percentage.substring(0,
                                        percentage.length() - 1);
                                speed = lineSplit[2];
                                eta = lineSplit[3];
                            } catch (IndexOutOfBoundsException e) {
                                throw new SystemCallException(
                                        "Failed to parse rsync output!", e);
                            }
                            break;
                        }
                    }

                    // Set the progress
                    if (percentage != null) {
                        vmicApi.getGlobalState().setProgressPercentage(
                                progressDataId, Double.parseDouble(percentage));
                        vmicApi.getGlobalState().setFileProgressDetails(
                                progressDataId, transfered, speed, eta);
                    }
                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            if (systemCall.isError()) {
                throw systemCall.getSystemCallException();
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

        // Image upload complete...
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
