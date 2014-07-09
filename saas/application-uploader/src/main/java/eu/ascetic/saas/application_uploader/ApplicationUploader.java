package eu.ascetic.saas.application_uploader;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.VM;

public class ApplicationUploader {
	public static final String APPLICATIONS_PATH = "applications";
	public static final String DEPLOYMENTS_PATH = "deployments";
	public static final String AGREEMENT_PATH = "agreement";
	public static final String ACCEPT_QP = "accept";
	public static final String YES_VALUE = "yes";
	public static final String NO_VALUE = "no";
	
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
	public int createAndDeployAplication(String ovf) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).accept(MediaType.APPLICATION_XML_TYPE)
				.header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML).post(ClientResponse.class, ovf);
		if (response.getStatus() == ClientResponse.Status.CREATED.getStatusCode()) {
			return response.getEntity(Application.class).getDeployments().get(0).getId();
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
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(Deployment.class).getStatus();
		}else
			throw new ApplicationUploaderException("Error getting deployment status. Returned code is "+ response.getStatus());
	}
	
	/** Get deployment agreement
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public String getDeploymentAgreement(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENT_PATH).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(Deployment.class).getStatus();
		}else
			throw new ApplicationUploaderException("Error getting deployment agreement. Returned code is "+ response.getStatus());
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
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENT_PATH).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return getVMsFromDeployment(response.getEntity(Deployment.class));
		}else
			throw new ApplicationUploaderException("Error getting deployment agreement. Returned code is "+ response.getStatus());
	}
	
	private Map<String, Map<String, String>> getVMsFromDeployment(
			Deployment deployment) {
		HashMap<String, Map<String, String>> vms = new  HashMap<String, Map<String, String>>();
		for (VM vm:deployment.getVms()){
			String provider = vm.getProviderId();
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
	 * @return deployment status
	 * @throws ApplicationUploaderException
	 */
	public void acceptAgreement(String applicationID, String deploymentID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENT_PATH).queryParam(ACCEPT_QP, YES_VALUE).put(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
			throw new ApplicationUploaderException("Error accepting deployment agreement. Returned code is "+ response.getStatus());
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
				.path(DEPLOYMENTS_PATH).path(deploymentID).path(AGREEMENT_PATH)
				.queryParam(ACCEPT_QP, NO_VALUE).put(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
			throw new ApplicationUploaderException("Error accepting deployment agreement. Returned code is "+ response.getStatus());
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
		if (response.getStatus() != ClientResponse.Status.NO_CONTENT.getStatusCode()) {
			throw new ApplicationUploaderException("Error deleting deployment. Returned code is "+ response.getStatus());
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
				.accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(Deployment.class).getId();
		}else
			throw new ApplicationUploaderException("Error getting deployment status. Returned code is "+ response.getStatus());
	}
	
	/** Destroy an application
	 * @param applicationID Application Identifier
	 * @throws ApplicationUploaderException
	 */
	public void destroyApplication(String applicationID) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID).delete(ClientResponse.class);
		if (response.getStatus() != ClientResponse.Status.NO_CONTENT.getStatusCode()) {
			throw new ApplicationUploaderException("Error deleting deployment. Returned code is "+ response.getStatus());
		}
	}
	
}
