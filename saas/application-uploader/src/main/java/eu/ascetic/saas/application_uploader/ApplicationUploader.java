/*
 *  Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es)
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

package eu.ascetic.saas.application_uploader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;

public class ApplicationUploader {
	public static final String APPLICATIONS_PATH = "applications";
	public static final String DEPLOYMENTS_PATH = "deployments";
	public static final String AGREEMENTS_PATH = "agreements";
	public static final String ACCEPT_QP = "accept";
	public static final String YES_VALUE = "yes";
	public static final String NO_VALUE = "no";
	private static final String ENERGY_CONSUM = "energy-consumption";
	private static final String COST_CONSUM = "cost-consumption";
	private static final String ENERGY_ESTIM = "energy-estimation";
	private static final String COST_ESTIM = "cost-estimation";
	private static final String EVENTS = "events";
	private static final String VMS = "vms";
	
	Client client;
	WebResource resource;
	
	/** Constructor
	 * @param rootURI URI to application manager root resource
	 */
	public ApplicationUploader(String rootURI){
		super();
		client = Client.create();
		resource = client.resource(rootURI);
	}
	
	/** Deploys and a plication and creates if does not exists
	 * @param ovf Application deployment OVF description (contains the application id)
	 * @return DeploymentID
	 * @throws ApplicationUploaderException
	 */
	public int createAndDeployAplication(String ovf, boolean manual) throws ApplicationUploaderException{
		WebResource apps = resource.path(APPLICATIONS_PATH);
		if (manual){
			apps = apps.queryParam("negotiation","manual");
		}
		ClientResponse response = apps.header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML).post(ClientResponse.class, ovf);
		if (response.getStatus() == ClientResponse.Status.CREATED.getStatusCode()) {
			List<Deployment> deployments = response.getEntity(Application.class).getDeployments();
			if (deployments.size()-1>=0){
				return deployments.get(deployments.size()-1).getId();
			}else
				throw new ApplicationUploaderException("No deployents found");
		}else
			throw new ApplicationUploaderException("Error creating application. Returned code is "+ response.getStatus());
	}
	
	/** Get deployment status
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public String getDeploymentStatus(String applicationID, String deploymentID) throws ApplicationUploaderException{
		return getDeployment(applicationID,deploymentID).getStatus();
	}
	
	public List<VM> getDeploymentVMDescriptions(String applicationID, String deploymentID) throws ApplicationUploaderException{
		return getDeployment(applicationID,deploymentID).getVms();
	}
	
	public Double getDeploymentEnergyConsumption(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(ENERGY_CONSUM).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			EnergyMeasurement measurement = response.getEntity(EnergyMeasurement.class);
			return measurement.getValue();
		}else
			throw new ApplicationUploaderException("Error getting deployment energy measurement. \nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
	}
	
	public Double getDeploymentCostConsumption(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(COST_CONSUM).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			Cost measurement = response.getEntity(Cost.class);
			return measurement.getCharges();
		}else
			throw new ApplicationUploaderException("Error getting deployment cost measurement."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
	}
	
	public Double getEventEnergyEstimation(String applicationID, String deploymentID, String eventID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(EVENTS).path(eventID).path(ENERGY_ESTIM).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			EnergyMeasurement measurement = response.getEntity(EnergyMeasurement.class);
			return measurement.getValue();
		}else
			throw new ApplicationUploaderException("Error getting deployment energy estimation."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
	}
	
	public Double getEventEnergyEstimationInVM(String applicationID, String deploymentID, String eventID, String vmID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(VMS).path(vmID)
				.path(EVENTS).path(eventID).path(ENERGY_ESTIM)
				.accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			EnergyMeasurement measurement = response.getEntity(EnergyMeasurement.class);
			return measurement.getValue();
		}else
			throw new ApplicationUploaderException("Error getting deployment energy estimation for event "+eventID+" in VM "+vmID+". "
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
	}
	
	public Cost getEventCostEstimationInVM(String applicationID, String deploymentID, String eventID, String vmID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(VMS).path(vmID)
				.path(EVENTS).path(eventID).path(COST_ESTIM)
				.accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			 Cost measurement = response.getEntity(Cost.class);
			return measurement;
		}else
			throw new ApplicationUploaderException("Error getting deployment cost estimation for event "+eventID+"in VM "+vmID+"."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));			

	}
	
	/** Get deployment agreements
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public List<Agreement> getDeploymentAgreements(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENTS_PATH).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			Collection agreemsCollection = response.getEntity(eu.ascetic.paas.applicationmanager.model.Collection.class);
			List<Agreement> agrs = agreemsCollection.getItems().getAgreements();
			if (agrs !=null){
				return agrs;
			}else{
				throw new ApplicationUploaderException("No aggreements found");
			}
		}else
			throw new ApplicationUploaderException("Error getting deployment agreement."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
	}
	
	/** Get deployed vms
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @return deployed vms in different providers 
	 *   [providerX->[ip_vm_1->ovf_vm_type_a,..., ip_vm_n->ovf_vm_type_b],
	 *    ...
	 *    providerY->[ip_vm_m->ovf_vm_type_c,..., ip_vm_z->ovf_vm_type_d]] 
	 * @throws ApplicationUploaderException
	 */
	public Map<String,Map<String,String>> getDeployedVMs(String applicationID, String deploymentID) throws ApplicationUploaderException{
			return getVMsFromDeployment(getDeployment(applicationID,deploymentID));
	}
	
	private Map<String, Map<String, String>> getVMsFromDeployment(
			Deployment deployment) {
		HashMap<String, Map<String, String>> vms = new  HashMap<String, Map<String, String>>();
		for (VM vm:deployment.getVms()){
			String provider = vm.getProviderId();
			if (provider == null)
				provider = "ASCETIC Cloud";
			Map<String, String> vm_provider = vms.get(provider);
			if (vm_provider==null){
				vm_provider = new HashMap<String, String>();
				vms.put(provider, vm_provider);
			}
			vm_provider.put(vm.getIp(), vm.getOvfId());
			
		}
		return vms;
	}
	

	/** Accept deployment agreement
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @param agreementID Agreement Identifier
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public void acceptAgreement(String applicationID, String deploymentID, String agreementID) throws ApplicationUploaderException{
		Agreement ag = new Agreement();
		ag.setAccepted(true);
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENTS_PATH).path(agreementID).put(ClientResponse.class, ag);
		if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
			throw new ApplicationUploaderException("Error accepting deployment agreement."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
		}
	}
	
	/** Reject deployment agreement
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public void rejectAgreement(String applicationID, String deploymentID) throws ApplicationUploaderException{
		
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENTS_PATH)
				.queryParam(ACCEPT_QP, NO_VALUE).put(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
			throw new ApplicationUploaderException("Error accepting deployment agreement.\n Returned code is "+ response.getStatus()
					+ "\n"+ response.getEntity(String.class));
		}
	}
	
	/** Undeploy an application deployment
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @throws ApplicationUploaderException
	 */
	public void undeploy(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).delete(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			throw new ApplicationUploaderException("Error deleting deployment. \nReturned code is "+ response.getStatus()+
					"\n"+ response.getEntity(String.class));
		}
	}
	
	/** Submits a new deployment for an existing application
	 * @param applicationID Application Identifier
	 * @param ovf Application OVF description
	 * @return Deployment Identifier
	 * @throws ApplicationUploaderException
	 */
	public int submitApplicationDeployment(String applicationID, String ovf) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML)
				.accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class, ovf);
		if (response.getStatus() == ClientResponse.Status.CREATED.getStatusCode()) {
			return response.getEntity(Deployment.class).getId();
		}else
			throw new ApplicationUploaderException("Error submitting applciation. \nReturned code is "+ response.getStatus()
					+ "\n"+ response.getEntity(String.class));
	}
	
	
	public VM addNewVM(String applicationID, String deploymentId, String ovfId) throws ApplicationUploaderException {
		String vms = "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\">\n"+
		   "\t<ovf-id>"+ovfId+"</ovf-id>\n"+
		   "</vm>";
		System.out.println(vms);
		VM vm = new VM();
		vm.setOvfId(ovfId);
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentId).path(VMS).header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML)
				.post(ClientResponse.class, vms);
		
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(VM.class);
		}else
			throw new ApplicationUploaderException("Error adding a new VM (" + ovfId + ").\nReturned code is "+ response.getStatus() 
					+ "\n"+ response.getEntity(String.class));
	}
	
	public void deleteVM(String applicationID, String deploymentId, String vmId) throws ApplicationUploaderException {
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentId).path(VMS).path(vmId).delete(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			throw new ApplicationUploaderException("Error deleting VM "+vmId+".\n Returned code is "+ response.getStatus()+ 
					"\n"+ response.getEntity(String.class));
		}
		
	}
	
	/** Destroy an application
	 * @param applicationID Application Identifier
	 * @throws ApplicationUploaderException
	 */
	public void destroyApplication(String applicationID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID).delete(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
			throw new ApplicationUploaderException("Error deleting deployment. Returned code is "+ response.getStatus());
		}
	}
	
	private Deployment getDeployment(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(Deployment.class);
		}else{
			throw new ApplicationUploaderException("Error getting deployment status. Returned code is "+ response.getStatus());
		}
	}

	public VM getVM(String applicationId, String deploymentId, String vmId) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationId)
				.path(DEPLOYMENTS_PATH).path(deploymentId).path(VMS).path(vmId).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(eu.ascetic.paas.applicationmanager.model.VM.class);	
		}else{
			throw new ApplicationUploaderException("Error getting VM "+vmId+"."
					+ "\nReturned code is "+ response.getStatus()+"\nEntity:"+response.getEntity(String.class));
		}
	}
}
