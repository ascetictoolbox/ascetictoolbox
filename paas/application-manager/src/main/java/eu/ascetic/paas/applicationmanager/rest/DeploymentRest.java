package eu.ascetic.paas.applicationmanager.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;

/**
 * ASCETiC Application Manager REST API to perform actions over an deployment of an Application
 * @author David Garcia Perez - Atos
 *
 */
@Path("/applications/{application_id}/deployments")
@Component
@Scope("request")
public class DeploymentRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(DeploymentRest.class);
	
	/**
	 * @param applicationId the id of the application for which we want to know the deployments
	 * @return a list of deployments for an application stored in the database fitting the respective query params.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeployments(@PathParam("application_id") String applicationId) {
		logger.info("GET request to paht: /applications/" + applicationId + "/deployments");
		
		// We get the deployments for an application from the DB
		List<Deployment> deployments = applicationDAO.getById(Integer.parseInt(applicationId)).getDeployments();
		
		// We create the XMl response
		String xml = XMLBuilder.getCollectionOfDeploymentsXML(deployments, applicationId);
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Creates a new deployment for an Application in the Application Manager
	 * The input for this REST service must be a parsaable OVF ASCETiC document (although we are using standard OVF)
	 * @param applicationId the id of the application for which we want to start a new deployment based in a new OVF file
	 * @return The deployment reference stored in the database with its assigned autogenerated id.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postDeployment(@PathParam("application_id") String applicationId) {
		logger.info("POST request to path: /applications/" + applicationId + "/deployments");
		// TODO
		// TODO we need to import the XML Beans of the OVF developed by DJango
		return buildResponse(Status.CREATED, "Method not implemented yet");
	}
	
	/**
	 * Returns the information of an specific deployment
	 * @param applicationId of the application in the database
	 * @return deploymentId of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Path("{deployment_id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeployment(@PathParam("application_id") String applicationId, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationId + "/deployments/" + deploymentId);
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		String xml = XMLBuilder.getDeploymentXML(deployment, Integer.parseInt(applicationId));
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Returns the agreement information between PaaS and IaaS layer for that specific deployment
	 * @param applicationId of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the stored agreement in its actual state 
	 */
	@GET
	@Path("{deployment_id}/agreement")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeploymentAgreement(@PathParam("application_id") String applicationId, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationId + "/deployments/" + deploymentId + "/agreement");
		// TODO
		// TODO 
		return buildResponse(Status.OK, "Method not implemented yet");
	}
	
	/**
	 * Returns the original submitted OVF that created this deployment for this application
	 * @param applicationId of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the stored ovf 
	 */
	@GET
	@Path("{deployment_id}/ovf")
	@Produces(MediaType.APPLICATION_XML)
	public Response getApplicationOvf(@PathParam("application_id") String applicationId, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationId + "/deployments/" + deploymentId + "/ovf");
		// TODO
		// TODO we need to think a bit about how we are going to store the OVF in the database, 
		//      I have not made my mind about it yet
		return buildResponse(Status.OK, "Method not implemented yet");
	}
	
	/**
	 * Accepts or rejects and agreement between PaaS and IaaS
	 * @param applicationId of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the updated agreement information
	 */
	@PUT
	@Path("{deployment_id}/agreement")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putDeploymentAgreement(@PathParam("application_id") String applicationId, @PathParam("deployment_id") String deploymentId) {
		logger.info("PUT request to path: /applications/" + applicationId + "/deployments/" + deploymentId + "/agreement");
		// TODO
		// TODO 
		return buildResponse(Status.ACCEPTED, "Method not implemented yet");
	}
	
	/**
	 * Puts an deployment to terminated state and deletes any resource that this application has been used in the IaaS layer
	 * @param applicationId of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return ok if the termination process is possible
	 */
	@DELETE
	@Path("{deployment_id}")
	public Response deleteDeployment(@PathParam("application_id") String applicationId, @PathParam("deployment_id") String deploymentId) {
		logger.info("DELETE request to path: /applications/" + applicationId + "/deployments/" + deploymentId);

		
		int intDeploymentId = 0;
		try{
			intDeploymentId = Integer.parseInt(deploymentId);
		}
		catch (NumberFormatException e){
			return buildResponse(Status.BAD_REQUEST, "The deployment id must be a number");
		}
		
		Deployment deployment = deploymentDAO.getById(intDeploymentId);
		
		if (deployment == null){
			return buildResponse(Status.BAD_REQUEST, "Deployment id = " +  deploymentId + " not found in database");
		}
		//Get the vms
		List<VM> deploymentVms = deployment.getVms();
		
		//Delete the vms from VM manager
		VmManagerClientHC vmManagerClient = new VmManagerClientHC();
		//boolean deleted = true;
		for (VM vm : deploymentVms){
			if (!vmManagerClient.deleteVM(vm.getProviderVmId())){
				return buildResponse(Status.BAD_REQUEST, "VM with provider vm id = " +  vm.getProviderVmId() + " cannot be deleted in VM manager");
			}
		}
		
		//set the deployment status to terminated
		deployment.setStatus(Dictionary.APPLICATION_STATUS_TERMINATED);
		
		//update deployment in database
		deploymentDAO.update(deployment);
		
		return buildResponse(Status.OK, "Deployment terminated successfully");
	}
}
