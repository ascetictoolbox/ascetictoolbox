package eu.ascetic.saas.application_uploader;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;

public class ApplicationUploader {
	public static final String APPLICATIONS_PATH = "applications";

	public static final String DEPLOYMENTS_PATH = "deployments";
	
	Client client;
	WebResource resource;
	
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
	public String createAndDeployAplication(String ovf) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).accept(MediaType.APPLICATION_XML_TYPE)
				.header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML).post(ClientResponse.class, ovf);
		if (response.getStatus() == ClientResponse.Status.CREATED.getStatusCode()) {
			return response.getEntity(Application.class).getDeployments().get(0).getDeploymentPlanId();
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
	public String submitApplicationDeployment(String applicationID, String ovf) throws ApplicationUploaderException{
		ClientResponse response = resource.path(APPLICATIONS_PATH).path(applicationID)
				.path(DEPLOYMENTS_PATH).header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_XML)
				.accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			return response.getEntity(Deployment.class).getDeploymentPlanId();
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
