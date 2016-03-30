/**
 *  Copyright 2016 University of Leeds
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
package eu.ascetic.test.vmic;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

/**
 * @author Django Armstrong (ULeeds)
 *
 */
public class OfflineModeTest {

    protected final static Logger LOGGER = Logger.getLogger(OfflineModeTest.class);
    
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

    
    @Test
    public void test() {
        LOGGER.info("### TEST: " + getCurrentMethodName() + " STARTED ###");

        try {
            ProgressDataImage progressDataImage = null;

            // Initialise the VMIC's configuration
            String configPropertiesFileUri = "";
            try {
                File f = new File(this.getClass()
                        .getResource("/config.properties").toURI());
                if (f.exists()) {
                    LOGGER.info("ASCETiC tests configuration file: "
                            + f.getAbsolutePath());
                    configPropertiesFileUri = f.getAbsolutePath();
                }
            }
            catch (Exception e) {
                LOGGER.info("Error loading ASCETiC Tests configuration file");
                LOGGER.info("Exception " + e);
            }
            GlobalConfiguration globalConfiguration = new GlobalConfiguration(configPropertiesFileUri);
            VmicApi vmicApi = new VmicApi(globalConfiguration);

            // Read the testing OVF
            URL url = getClass().getClassLoader().getResource("offline-test-ovf.xml");
            String ovfDefinitionAsString = null;
            try {
                ovfDefinitionAsString = new String(Files.readAllBytes(Paths
                        .get(url.toURI())));
                LOGGER.info(ovfDefinitionAsString);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            OvfDefinition ovfDefinition = OvfDefinition.Factory
                    .newInstance(ovfDefinitionAsString);

            String ovfDefinitionId = ovfDefinition.getVirtualSystemCollection()
                    .getId();

            vmicApi.generateImage(ovfDefinition);

            // Wait until the file upload has been registered with the VMIC
            // before polling the progress data...
            while (true) {
                try {
                    LOGGER.info("TEST: Trying to fetch progress data...");
                    vmicApi.progressCallback(ovfDefinitionId, null);
                    LOGGER.info("TEST: No ProgressException...");
                    break;
                } catch (ProgressException e) {
                    LOGGER.warn("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    Thread.sleep(250);
                }
            }

            double previousProgess = -1.0;
            // Poll the progress data until completion...
            while (true) {

                // We have progress data, do something with it...
                progressDataImage = (ProgressDataImage) vmicApi
                        .progressCallback(ovfDefinitionId, null);

                // We have an error so stop everything!
                if (progressDataImage.isError()) {
                    // Say what the error is...
                    LOGGER.error(progressDataImage.getException().getMessage(),
                            progressDataImage.getException());
                    // For test case to report failure correctly
                    assertFalse(progressDataImage.isError());
                    return;
                } else {
                    double currentProgress = progressDataImage
                            .getTotalProgress();
                    if (currentProgress > previousProgess) {
                        LOGGER.info("TEST: progressDataImageObject total progress is: "
                                + currentProgress);
                        LOGGER.info("TEST: progressDataImageObject history of Phase with ID: "
                                + progressDataImage
                                        .getPhaseName(progressDataImage
                                                .getCurrentPhaseId())
                                + ", % is: "
                                + progressDataImage.getHistory().get(
                                        progressDataImage.getCurrentPhaseId()));
                        previousProgess = currentProgress;
                    }
                }

                // 250ms delay between polling...
                Thread.sleep(250);

                // Test to see if upload has finished...
                if (vmicApi.progressCallback(ovfDefinitionId, null)
                        .isComplete()) {
                    LOGGER.warn("TEST: Detected image generation as completed!");
                    break;
                }
            }

            // Test the manifest that was altered
            OvfDefinition ovfDefinition2 = ((ProgressDataImage) (vmicApi
                    .progressCallback(ovfDefinitionId, null)))
                    .getOvfDefinition();

            assertFalse(ovfDefinition2.hasErrors());

            String targetDir = System.getProperty("ovfSampleDir", "target");

            java.io.File file = new java.io.File(targetDir
                    + java.io.File.separator + java.io.File.separator
                    + "gpf-ovf.vmic.xml");
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write(ovfDefinition2.toString());
            // Close the output stream
            out.close();

        } catch (Exception e) {
            LOGGER.error("TEST: Test Failed! Cause: " + e.getCause(), e);
        }

        LOGGER.warn("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
    }

}
