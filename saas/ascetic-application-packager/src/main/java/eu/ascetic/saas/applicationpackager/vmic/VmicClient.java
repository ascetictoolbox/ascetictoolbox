package eu.ascetic.saas.applicationpackager.vmic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs.VmicCallProgressBarDialog;
import eu.ascetic.saas.applicationpackager.ovf.OVFUtils;
import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

// TODO: Auto-generated Javadoc
/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class implements a client to communicate with Virtual Image Constructor component
 *
 */

public class VmicClient {
	
	/**
	 * Instantiates a new vmic client.
	 */
	public VmicClient(){
		
	}
    
    /**
     * Get current method name.
     *
     * @return Method Name
     */
    public String getCurrentMethodName() {
        StackTraceElement stackTraceElements[] = (new Throwable())
                .getStackTrace();
        return stackTraceElements[1].toString();
    }
    
	/** The Constant LOGGER. */
	protected final static Logger LOGGER = Logger.getLogger(VmicClient.class);

	/**
	 * Testing sending an OVF to VMIC
	 * @return
	 */
	public String testGenerateImageWorkflowTest(){
		String ovfDefinitionAsString = null;
		try {
			ovfDefinitionAsString = Utils.readFile(
					"C:\\data\\projects\\ARI\\it\\ASCETiC\\svn\\trunk\\saas\\ascetic-application-packager"
					+ "\\src\\main\\resources\\atc-single-feb2016.ovf");
		 } catch (IOException e) {
            e.printStackTrace();
		}
       
		return  testGenerateImageWorkflow(ovfDefinitionAsString, null);
	}

	
	/**
	 * Test generate image workflow.
	 *
	 * @param ovfDefinitionAsString the ovf definition as string
	 * @return the string
	 */
	public String testGenerateImageWorkflow(String ovfDefinitionAsString, VmicCallProgressBarDialog dialog) {
        
		String ovfResult = null;
		if (dialog != null){
			dialog.updateProgressBar(1);
		}
        LOGGER.info("### TEST: " + getCurrentMethodName() + " STARTED ###");
        System.out.println("### TEST: " + getCurrentMethodName() + " STARTED ###");
        if (dialog != null){
        	dialog.addLogMessage("### OVF code generation from VMIC STARTED ###");
        }
        try {
            ProgressDataImage progressDataImage = null;

            // Initialise the VMIC's configuration
            GlobalConfiguration globalConfiguration = new GlobalConfiguration(            
//        "C:\\data\\projects\\ARI\\it\\ASCETiC\\svn\\trunk\\saas\\ascetic-application-packager\\src\\main\\resources\\cfg.properties");
    		"/home/ubuntu/ascetic/saas/app-packager/cfg.properties");
            System.out.println("Config file assigned");
            VmicApi vmicApi = new VmicApi(globalConfiguration);
            System.out.println("Configs parameters loaded");
            
            System.out.println("Generating new instance from ovfDefinitionFactory");
            OvfDefinition ovfDefinition = OvfDefinition.Factory
                    .newInstance(ovfDefinitionAsString);
            
            System.out.println("Retrieving ID from Virtual System Collection");
            String ovfDefinitionId = ovfDefinition.getVirtualSystemCollection()
                    .getId();

            System.out.println("Ready to generate image");
            vmicApi.generateImage(ovfDefinition);

            // Wait until the file upload has been registered with the VMIC
            // before polling the progress data...
            while (true) {
                try {
                    LOGGER.info("TEST: Trying to fetch progress data...");
                    System.out.println("TEST: Trying to fetch progress data...");
                    if (dialog != null){
                    	dialog.addLogMessage("Trying to fetch progress data...");
                    }
                    vmicApi.progressCallback(ovfDefinitionId, null);
                    LOGGER.info("TEST: No ProgressException...");
                    System.out.println("TEST: No ProgressException...");
                    if (dialog != null){
                    	dialog.addLogMessage("No ProgressException...");
                    }
                    break;
                } catch (ProgressException e) {
                    LOGGER.warn("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    System.out.println("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    if (dialog != null){
                    	dialog.addLogMessage("Caught ProgressException due to: "
                                + e.getMessage());
                    }
                    Thread.sleep(250);
                }
            }

//
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
                    System.out.println(progressDataImage.getException().getMessage());
                    if (dialog != null){
                    	dialog.addLogMessage(progressDataImage.getException().getMessage());
                    }
                    return ovfResult;
                } else {
                    double currentProgress = progressDataImage
                            .getTotalProgress();
                    
                    if (dialog != null){
                    	dialog.updateProgressBar(currentProgress);
                    }
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
                        System.out.println("TEST: progressDataImageObject total progress is: " + currentProgress);
                        System.out.println("TEST: progressDataImageObject history of Phase with ID: "
	                                + progressDataImage
	                                .getPhaseName(progressDataImage
	                                        .getCurrentPhaseId())
	                        + ", % is: "
	                        + progressDataImage.getHistory().get(
	                                progressDataImage.getCurrentPhaseId()));
                        if (dialog != null){
                        	dialog.addLogMessage("progressDataImageObject total progress is: " + currentProgress);
                        	dialog.addLogMessage("progressDataImageObject history of Phase with ID: "
	                                + progressDataImage
	                                .getPhaseName(progressDataImage
	                                        .getCurrentPhaseId())
	                        + ", % is: "
	                        + progressDataImage.getHistory().get(
	                                progressDataImage.getCurrentPhaseId()));
                        }
                        previousProgess = currentProgress;
                    }
                }

                // 250ms delay between polling...
                Thread.sleep(250);

                // Test to see if upload has finished...
                if (vmicApi.progressCallback(ovfDefinitionId, null)
                        .isComplete()) {
                    LOGGER.warn("TEST: Detected image generation as completed!");
                    System.out.println("TEST: Detected image generation as completed!");
                    if (dialog != null){
                    	dialog.addLogMessage("Detected image generation has been completed!");
//                    	dialog.updateProgressBar(100.0);
//                    	dialog.updateWidgetsProcessFinished();
                    }
                    break;
                }
            }

            //Test the manifest that was altered
            OvfDefinition ovfDefinition2 = ((ProgressDataImage) (vmicApi.progressCallback(
                    ovfDefinitionId, null))).getOvfDefinition();
            
            ovfResult = ovfDefinition2.toString();
            
//            String targetDir = System.getProperty("ovfSampleDir", "target");
//
//            java.io.File file = new java.io.File(targetDir
//                    + java.io.File.separator + java.io.File.separator
//                    + "3tier-webapp.ovf.vmic.xml");
//            FileWriter fstream = new FileWriter(file);
//            BufferedWriter out = new BufferedWriter(fstream);
//    out.write(ovfDefinition2.toString());
            
            System.out.println("******************************************************");
            System.out.println("*****************  VMIC response  ********************");
            System.out.println("******************************************************");
            System.out.println(ovfResult);
            System.out.println("******************************************************");
            
            
            
            // Close the output stream
//            out.close();
            
        } catch (Exception e) {
            LOGGER.error("TEST: Test Failed! Cause: " + e.getCause(), e);
            System.out.println("TEST: Test Failed! Cause: " + e.getCause());
            System.out.println("TEST: Test Failed! Cause: " + e.getMessage());
            if (dialog != null){
            	dialog.addLogMessage("Process Failed! Cause: " + e.getMessage());
            }
            
        }
        
        LOGGER.warn("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
        System.out.println("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
        if (dialog != null){
        	dialog.addLogMessage("### OVF code generation from VMIC COMPLETE ###");
        	
        	dialog.updateProgressBar(100.0);
        	dialog.updateWidgetsProcessFinished();
        }
        
        return ovfResult;
    }
	
}
