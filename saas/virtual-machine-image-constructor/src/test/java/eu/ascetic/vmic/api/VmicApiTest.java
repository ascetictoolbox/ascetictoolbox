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
package eu.ascetic.vmic.api;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VmicApiTest extends TestCase {
    protected final static Logger LOGGER = Logger.getLogger(VmicApiTest.class);

    /**
     * Create the test case.
     * 
     * @param testName
     *            Name of the test case.
     */
    public VmicApiTest(String testName) {
        super(testName);
    }

    /**
     * Get current method name
     * 
     * @return Method Name
     */
    public String getCurrentMethodName() {
        StackTraceElement stackTraceElements[] = (new Throwable())
                .getStackTrace();
        return stackTraceElements[1].toString();
    }

    /**
     * Tests the workflow of file uploading.
     */
    public void testFileUploadWorkflow() {
        LOGGER.info("### TEST: " + getCurrentMethodName() + " STARTED ###");

        try {
            ProgressDataFile progressDataFile = null;

            // Initialise the VMIC's configuration
            GlobalConfiguration globalConfiguration = new GlobalConfiguration();
            VmicApi vmicApi = new VmicApi(globalConfiguration);

            // Use a test file
            File file = new File(
                    "C:\\Users\\django\\cygwin\\home\\django\\test.file");

            vmicApi.uploadFile("ovfDefinitionId", file);

            // Wait until the file upload has been registered with the VMIC
            // before polling the progress data...
            while (true) {
                try {
                    LOGGER.info("TEST: Trying to fetch progress data...");
                    vmicApi.progressCallback("ovfDefinitionId", file);
                    LOGGER.info("TEST: No ProgressException...");
                    break;
                } catch (ProgressException e) {
                    LOGGER.warn("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    Thread.sleep(250);
                }
            }

            // Poll the progress data until completion...
            while (true) {

                // We have progress data, do something with it...
                progressDataFile = (ProgressDataFile) vmicApi.progressCallback(
                        "ovfDefinitionId", file);

                // We have an error so stop everything!
                if (progressDataFile.isError()) {
                    // Say what the error is...
                    LOGGER.error(progressDataFile.getException().getMessage(),
                            progressDataFile.getException());
                    // For test case to report failure correctly
                    assertFalse(progressDataFile.isError());
                    return;
                } else {
                    LOGGER.info("TEST: progressDataFileObject total progress is: "
                            + progressDataFile.getTotalProgress());
                    LOGGER.info("TEST: progressDataFileObject history of Phase with ID: "
                            + progressDataFile.getPhaseName(progressDataFile
                                    .getCurrentPhaseId())
                            + ", % is: "
                            + progressDataFile.getHistory().get(
                                    progressDataFile.getCurrentPhaseId()));
                    if (progressDataFile.getCurrentPhaseId() == ProgressDataFile.UPLOADING_FILE_PHASE_ID) {
                        String transfered = progressDataFile.getTransfered();
                        String speed = progressDataFile.getSpeed();
                        String eta = progressDataFile.getEta();
                        if (transfered != null && speed != null && eta != null) {
                            LOGGER.info("TEST: progressDataFileObject additional upload progress details: transfered - "
                                    + transfered
                                    + ", speed - "
                                    + speed
                                    + ", ETA - " + eta);
                        }
                    }
                }

                // 250ms delay between polling...
                Thread.sleep(250);

                // Test to see if upload has finished...
                // FIXME: Should we be getting the serviceId from a property
                // here, confirm with consortium?
                if (vmicApi.progressCallback("ovfDefinitionId", file)
                        .isComplete()) {
                    LOGGER.warn("TEST: Detected upload has completed!");
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("TEST: Test Failed! Cause: " + e.getCause(), e);
        }

        LOGGER.warn("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
    }
}
