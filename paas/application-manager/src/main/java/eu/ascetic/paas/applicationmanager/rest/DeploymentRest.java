package eu.ascetic.paas.applicationmanager.rest;

import java.util.ArrayList;
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

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.EnergyModellerSimple;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * ASCETiC Application Manager REST API to perform actions over an deployment of an Application
 *
 */

@Path("/applications/{application_name}/deployments")
@Component
@Scope("request")
public class DeploymentRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(DeploymentRest.class);
	protected static PaaSEnergyModeller energyModeller; 
	
	/**
	 * Constructs the EnergyModeller with an specific configuration if necessary
	 * @return the new EnergyModeller or a previous created object
	 */
	protected static PaaSEnergyModeller getEnergyModeller() {
		if(energyModeller == null) {
			logger.debug("Initializing Energy Modeller...");
			return new EnergyModellerSimple("/etc/ascetic/paas/em/config.properties");
		}
		else {
			return energyModeller;
		}
	}
	
	/**
	 * @param applicationName the name of the application for which we want to know the deployments
	 * @return a list of deployments for an application stored in the database fitting the respective query params.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeployments(@PathParam("application_name") String applicationName) {
		logger.info("GET request to paht: /applications/" + applicationName + "/deployments");
		
		// We get the deployments for an application from the DB
		List<Deployment> deployments = applicationDAO.getByName(applicationName).getDeployments();
		
		// We create the XMl response
		String xml = XMLBuilder.getCollectionOfDeploymentsXML(deployments, applicationName);
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Creates a new deployment for an Application in the Application Manager
	 * The input for this REST service must be a parsaable OVF ASCETiC document (although we are using standard OVF)
	 * @param applicationName the name of the application for which we want to start a new deployment based in a new OVF file
	 * @param payload OVF file with the deployment information
	 * @return The deployment reference stored in the database with its assigned autogenerated id.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postDeployment(@PathParam("application_name") String applicationName, String payload) {
		logger.info("POST request to path: /applications/" + applicationName + "/deployments");
		logger.info("      PAYLOAD: " + payload);
		
		return createNewDeployment(payload);
	}
	
	/**
	 * Returns the information of an specific deployment
	 * @param applicationName of name the application in the database
	 * @return deploymentId of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Path("{deployment_id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeployment(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId);
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		String xml = XMLBuilder.getDeploymentXML(deployment, applicationName);
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Returns the agreement information between PaaS and IaaS layer for that specific deployment
	 * @param applicationName of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the stored agreement in its actual state 
	 */
	@GET
	@Path("{deployment_id}/agreement")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeploymentAgreement(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/agreement");
		// TODO
		// TODO 
		return buildResponse(Status.OK, "Method not implemented yet");
	}
	
	/**
	 * Returns the original submitted OVF that created this deployment for this application
	 * @param applicationName of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the stored ovf 
	 */
	@GET
	@Path("{deployment_id}/ovf")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeploymentOvf(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/ovf");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		String xml = deployment.getOvf();
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Accepts or rejects and agreement between PaaS and IaaS
	 * @param applicationName of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return the updated agreement information
	 */
	@PUT
	@Path("{deployment_id}/agreement")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putDeploymentAgreement(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("PUT request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/agreement");
		// TODO
		// TODO 
		return buildResponse(Status.ACCEPTED, "Method not implemented yet");
	}
	
	/**
	 * Puts an deployment to terminated state and deletes any resource that this application has been used in the IaaS layer
	 * @param applicationName of the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return ok if the termination process is possible
	 */
	@DELETE
	@Path("{deployment_id}")
	public Response deleteDeployment(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("DELETE request to path: /applications/" + applicationName + "/deployments/" + deploymentId);

		
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
	
	@GET
	@Path("{deployment_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyConsumption(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-measurement");
		// Make sure we have the right configuration
		energyModeller = getEnergyModeller();
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		List<String> ids = getVmsProviderIds(deployment);
		
		logger.debug("Connecting to Energy Modeller");
		double energyConsumed = energyModeller.energyApplicationConsumption(null, applicationName, ids, null);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId);
				
		return buildResponse(Status.OK, xml);
	}
	
	protected List<String> getVmsProviderIds(Deployment deployment) {
		List<String> ids = new ArrayList<String>();
		
		for(VM vm : deployment.getVms()) {
			ids.add(vm.getProviderVmId());
		}
		
		return ids;
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimationForEvent(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId, @PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/energy-estimation");
		// Make sure we have the right configuration
		energyModeller = getEnergyModeller();
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		List<String> ids = getVmsProviderIds(deployment);
		
		logger.debug("Connecting to Energy Modeller");
		double energyConsumed = energyModeller.energyEstimation(null, applicationName, ids, eventId);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
}
