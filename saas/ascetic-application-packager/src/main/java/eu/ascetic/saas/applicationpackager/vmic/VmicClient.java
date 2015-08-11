package eu.ascetic.saas.applicationpackager.vmic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

public class VmicClient {
	
	public VmicClient(){
		
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
    
	protected final static Logger LOGGER = Logger.getLogger(VmicClient.class);

	public String testGenerateImageWorkflow(String ovfDefinitionAsString) {
        
		String ovfResult = null;
		
        LOGGER.info("### TEST: " + getCurrentMethodName() + " STARTED ###");
        System.out.println("### TEST: " + getCurrentMethodName() + " STARTED ###");

        try {
            ProgressDataImage progressDataImage = null;

            // Initialise the VMIC's configuration
            GlobalConfiguration globalConfiguration = new GlobalConfiguration();
            VmicApi vmicApi = new VmicApi(globalConfiguration);

            /** I pass the ovf text directly as method attribute **/
            // Read the testing OVF
//          URL url = getClass().getClassLoader().getResource("gpf-ovf.xml");
//          URL url = getClass().getClassLoader().getResource("test-ovf.ovf");
//          URL url = getClass().getClassLoader().getResource("test-ovf-may2015.ovf");
//            String ovfDefinitionAsString = null;
//            try {
//                ovfDefinitionAsString = new String(Files.readAllBytes(Paths
//                        .get(url.toURI())));
//                LOGGER.info(ovfDefinitionAsString);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
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
                    System.out.println("TEST: Trying to fetch progress data...");
                    vmicApi.progressCallback(ovfDefinitionId, null);
                    LOGGER.info("TEST: No ProgressException...");
                    System.out.println("TEST: No ProgressException...");
                    break;
                } catch (ProgressException e) {
                    LOGGER.warn("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    System.out.println("TEST: Caught ProgressException due to: "
                            + e.getMessage());
                    Thread.sleep(250);
                }
            }

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
                    return ovfResult;
                } else {
                    LOGGER.info("TEST: progressDataImageObject total progress is: "
                            + progressDataImage.getTotalProgress());
                    System.out.println("TEST: progressDataImageObject total progress is: "
                            + progressDataImage.getTotalProgress());
                    LOGGER.info("TEST: progressDataImageObject history of Phase with ID: "
                            + progressDataImage.getPhaseName(progressDataImage
                                    .getCurrentPhaseId())
                            + ", % is: "
                            + progressDataImage.getHistory().get(
                                    progressDataImage.getCurrentPhaseId()));
                    System.out.println("TEST: progressDataImageObject history of Phase with ID: "
                            + progressDataImage.getPhaseName(progressDataImage
                                    .getCurrentPhaseId())
                            + ", % is: "
                            + progressDataImage.getHistory().get(
                                    progressDataImage.getCurrentPhaseId()));
                }

                // 250ms delay between polling...
                Thread.sleep(250);

                // Test to see if upload has finished...
                if (vmicApi.progressCallback(ovfDefinitionId, null)
                        .isComplete()) {
                    LOGGER.warn("TEST: Detected image generation as completed!");
                    System.out.println("TEST: Detected image generation as completed!");
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
        }
        
        LOGGER.warn("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
        System.out.println("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
        
        return ovfResult;
    }
	
}
