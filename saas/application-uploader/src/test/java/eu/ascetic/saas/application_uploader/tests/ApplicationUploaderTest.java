/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package eu.ascetic.saas.application_uploader.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

public class ApplicationUploaderTest {
	private static String DEPLOYMENT_ID= "630";
	private static String VM_ID= "2005";
	/*@Test
	public void uploadApptest() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://10.4.0.16/application-manager");
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
		uploader.destroyApplication(applicationID);
	}
	
	@Test
	public void getVMS() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = "517";
		Map<String, Map<String, String>> vms = uploader.getDeployedVMs(applicationID, deploymentID);
		for (Entry<String,Map<String,String>>provs:vms.entrySet()){
			for (Entry<String,String>e:provs.getValue().entrySet()){
				System.out.println("Prov: "+ provs.getKey()+ " ip: "+e.getKey()+" vm: "+e.getValue());
			}
		}
	}*/
	
	@Test
	public void testThreads() throws InterruptedException{
		long init = System.currentTimeMillis();
		int N_THREADS = 2;
		Thread[] threads = new Thread[N_THREADS];
        for ( int i= 0; i<N_THREADS; i++){
        	final int run = i;
        	
    		threads[i] = new Thread(){
    			public void run(){
       
    				long st = System.currentTimeMillis();
    				long diff = 0l;
    				while(diff<30000){
    					for (int i=0; i<200;i++){
    						Math.atan(Math.tan(Math.atan(Math.tan(0.5))));
    					}
    					diff = System.currentTimeMillis()-st;
    				}
    			}
    		};
    	
    		System.out.println("Thread "+ i + " started...");
    		threads[i].start();
        }
    	for ( int i= 0; i<N_THREADS; i++){
	           	System.out.println("Waiting for thread "+ i + " started...");
	           	threads[i].join();
	    }
    	long total = System.currentTimeMillis()- init;
    	System.out.println("Total time :" + total);
    
	}
	
	@Test
	public void getVMDescriptions() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = DEPLOYMENT_ID;
		List<VM> vms = uploader.getDeploymentVMDescriptions(applicationID, deploymentID);
		for (VM vm:vms){
			System.out.println("Prov: "+ vm.getProviderId()+ " ip: " + vm.getIp() 
					+ " vm-id: " + vm.getId() + " vm-provider-id: " + vm.getProviderVmId()+" ovf-id: "+vm.getOvfId());
		}
	}
	
	@Test
	public void getDeploymentEnergyConsumption() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = DEPLOYMENT_ID;
		Double meas = uploader.getDeploymentEnergyConsumption(applicationID, deploymentID);
		System.out.println("Energy measurement for app " + applicationID + " deployment "+ deploymentID+ " is "+ meas.toString());		
	}
	
	@Test
	public void getEventEnergyConsumption() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = DEPLOYMENT_ID;
		String eventID = "core0impl0";
		Double meas = uploader.getEventEnergyEstimation(applicationID, deploymentID, eventID);
		System.out.println("Energy estimation for app " + applicationID + " deployment "+ deploymentID+ " event " + eventID +" is "+ meas.toString());	
		eventID = "core0impl1";
		meas = uploader.getEventEnergyEstimation(applicationID, deploymentID, eventID);
		System.out.println("Energy estimation for app " + applicationID + " deployment "+ deploymentID+ " event " + eventID +" is "+ meas.toString());
	}
	
	@Test
	public void getEventEnergyConsumptionInVM() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = DEPLOYMENT_ID;
		String eventID = "core0impl0";
		String vmID = VM_ID;
		Double meas = uploader.getEventEnergyEstimationInVM(applicationID, deploymentID, eventID, vmID);
		System.out.println("Energy estimation for app " + applicationID + " deployment "+ deploymentID+ " vm "+ vmID + " event " + eventID +" is "+ meas.toString());
		eventID = "core0impl1";
		meas = uploader.getEventEnergyEstimationInVM(applicationID, deploymentID, eventID, vmID);
		System.out.println("Energy estimation for app " + applicationID + " deployment "+ deploymentID+ " vm "+ vmID + " event " + eventID +" is "+ meas.toString());
	}
	
	@Test
	public void getEventCostConsumptionInVM() throws ApplicationUploaderException {
		ApplicationUploader uploader = new ApplicationUploader("http://192.168.3.16/application-manager");
		String applicationID = "JEPlus";
		String deploymentID = DEPLOYMENT_ID;
		String eventID = "core0impl0";
		String vmID = VM_ID;
		Cost cost = uploader.getEventCostEstimationInVM(applicationID, deploymentID, eventID, vmID);
		System.out.println("Estimations for app " + applicationID + " deployment "+ deploymentID+ " vm "+ vmID + " event " + eventID +" are: Energy-> "+ cost.getEnergyValue().toString()+" Power-> "+ cost.getPowerValue().toString()+ " Charges-> "+ cost.getCharges().toString());
		eventID = "core0impl1";
		cost = uploader.getEventCostEstimationInVM(applicationID, deploymentID, eventID, vmID);
		System.out.println("Estimations for app " + applicationID + " deployment "+ deploymentID+ " vm "+ vmID + " event " + eventID +" are: Energy-> "+ cost.getEnergyValue().toString()+" Power-> "+ cost.getPowerValue().toString()+ " Charges-> "+ cost.getCharges().toString());
	}

}
