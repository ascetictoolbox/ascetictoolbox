package eu.ascetic.saas.application_uploader.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

public class ApplicationUploaderTest {

	@Test
	public void uploadApptest() throws ApplicationUploaderException {
		/*ApplicationUploader uploader = new ApplicationUploader("http://10.4.0.16/application-manager");
		InputStream is = ApplicationUploader.class.getResourceAsStream("/service_manifest.xml");
		BufferedReader br = null; 
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
					sb.append(line);
			}

			 
		} catch (IOException e) {
					e.printStackTrace();
					fail();
		} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
							fail();
						}
					}
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
							fail();
						}
					}
				}
		int id = uploader.createAndDeployAplication(sb.toString());
		System.out.println("Deployment ID : "+ id);
		String applicationID = "HMMERpfam";
		String status = uploader.getDeploymentStatus(applicationID, Integer.toString(id));
		System.out.println("Status: "+ status);
		String agreement = uploader.getDeploymentAgreement(applicationID, Integer.toString(id));
		System.out.println("Agreement: "+ agreement);
		uploader.acceptAgreement(applicationID, Integer.toString(id));
		Map<String, Map<String, String>> vms = uploader.getDeployedVMs(applicationID, Integer.toString(id));
		for (Entry<String,Map<String,String>>provs:vms.entrySet()){
			for (Entry<String,String>e:provs.getValue().entrySet()){
				System.out.println("Prov: "+ provs.getKey()+ " vm: "+e.getKey()+" ip: "+e.getValue());
			}
		}
		System.out.println("undeploying");
		uploader.undeploy(applicationID, Integer.toString(id));
		System.out.println("undeploying");
		id = uploader.submitApplicationDeployment(applicationID, sb.toString());
		uploader.destroyApplication(applicationID);*/
	}
	
	@Test
	public void getVMS() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://10.4.0.16/application-manager");
		String applicationID = "HMMERpfam";
		String deploymentID = "35";
		Map<String, Map<String, String>> vms = uploader.getDeployedVMs(applicationID, deploymentID);
		for (Entry<String,Map<String,String>>provs:vms.entrySet()){
			for (Entry<String,String>e:provs.getValue().entrySet()){
				System.out.println("Prov: "+ provs.getKey()+ " ip: "+e.getKey()+" vm: "+e.getValue());
			}
		}
	}
	
	@Test
	public void getDeploymentEnergyConsumption() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://10.4.0.16/application-manager");
		String applicationID = "HMMERpfam";
		String deploymentID = "35";
		Double meas = uploader.getDeploymentEnergyConsumption(applicationID, deploymentID);
		System.out.println("Energy measurement for app " + applicationID + " deployment "+ deploymentID+ " is "+ meas.toString());		
	}
	
	@Test
	public void getEventEnergyConsumption() throws ApplicationUploaderException {
		/*ApplicationUploader uploader = new ApplicationUploader("http://10.4.0.16/application-manager");
		String applicationID = "HMMERpfam";
		String deploymentID = "35";
		String eventID = "event";
		Double meas = uploader.getDeploymentEnergyConsumption(applicationID, deploymentID);
		System.out.println("Energy measurement for app " + applicationID + " deployment "+ deploymentID+ " is "+ meas.toString());*/		
	}

}