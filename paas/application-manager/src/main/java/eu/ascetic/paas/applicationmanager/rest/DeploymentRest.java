package eu.ascetic.paas.applicationmanager.rest;

import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_DELETED;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.bsc.vmmclient.models.VmCost;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;
import eu.ascetic.paas.applicationmanager.amonitor.ApplicationMonitorClient;
import eu.ascetic.paas.applicationmanager.amonitor.ApplicationMonitorClientHC;
import eu.ascetic.paas.applicationmanager.amonitor.model.Data;
import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerMessage;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerQueueController;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.rest.util.DateUtil;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientBSSC;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * ASCETiC Application Manager REST API to perform actions over an deployment of an Application
 *
 */

@Path("/applications/{application_name}/deployments")
@Component
@Scope("request")
public class DeploymentRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(DeploymentRest.class);
	protected VmManagerClient vmManagerClient = new VmManagerClientBSSC(Configuration.vmManagerServiceUrl);
	protected PRClient prClient = new PRClient();
	protected ApplicationMonitorClient applicationMonitorClient = new ApplicationMonitorClientHC();
	
	/**
	 * @param applicationName the name of the application for which we want to know the deployments
	 * @return a list of deployments for an application stored in the database fitting the respective query params.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML + ";qs=1")
	public Response getDeployments(@PathParam("application_name") String applicationName, @DefaultValue("")@QueryParam("status") String status) {
		logger.info("GET request to paht: /applications/" + applicationName + "/deployments?status=" + status + " [XML]");
		
		List<Deployment> deployments = getDeploymentsList(applicationName, status);
		
		// We create the XMl response
		String xml = XMLBuilder.getCollectionOfDeploymentsXML(deployments, applicationName);
		
		return buildResponse(Status.OK, xml);
	}
	
	private List<Deployment> getDeploymentsList(String applicationName, String status) {
		List<Deployment> deployments = null;
		
		if(status.equals("")) {
			// We get the deployments for an application from the DB
			deployments = applicationDAO.getByName(applicationName).getDeployments();		
		} else {
			Application application = applicationDAO.getByNameWithoutDeployments(applicationName);
			 deployments = deploymentDAO.getDeploymentsForApplicationWithStatus(application, status);
		}
		
		return deployments;
	}
	
	/**
	 * @param applicationName the name of the application for which we want to know the deployments
	 * @return a list of deployments for an application stored in the database fitting the respective query params.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON + ";qs=0.5")
	public Response getDeploymentsJSON(@PathParam("application_name") String applicationName, @DefaultValue("")@QueryParam("status") String status) {
		logger.info("GET request to paht: /applications/" + applicationName + "/deployments?status=" + status + " [JSON]");
		
		List<Deployment> deployments = getDeploymentsList(applicationName, status);
		
		// We create the JSON response
		String json = XMLBuilder.getCollectionOfDeploymentsJSON(deployments, applicationName);
		
		return buildResponse(Status.OK, json);
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
	public Response postDeployment(@PathParam("application_name") String applicationName, @QueryParam("negotiation") String negotiation,  @QueryParam("schema") String schema, String payload) {
		logger.info("POST request to path: /applications/" + applicationName + "/deployments?negotiation=" + negotiation);
		logger.info("      PAYLOAD: " + payload);
		
		return createNewDeployment(payload, negotiation, schema, true);
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response postDeploymentJSON(@PathParam("application_name") String applicationName, @QueryParam("negotiation") String negotiation,  @QueryParam("schema") String schema, String payload) {
		logger.info("POST request to path: /applications/" + applicationName + "/deployments?negotiation=" + negotiation);
		logger.info("      PAYLOAD: " + payload);
		
		return createNewDeployment(payload, negotiation, schema, false);
	}
	
	/**
	 * Returns compatible angularJS options for that deployment... 
	 * @return
	 */
	@OPTIONS
	@Path("{deployment_id}")
	public Response optionsForSpecificDeployment() {
		return options();
	}
	
	/**
	 * Returns the information of an specific deployment
	 * @param applicationName of name the application in the database
	 * @return deploymentId of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Path("{deployment_id}")
	@Produces(MediaType.APPLICATION_XML + ";qs=1")
	public Response getDeployment(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + " [in XML]");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		String xml = XMLBuilder.getDeploymentXML(deployment, applicationName);
		
		return buildResponse(Status.OK, xml);
	}
	
	
	/**
	 * Returns the information of an specific deployment in JSON format
	 * @param applicationName of name the application in the database
	 * @return deploymentId of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Path("{deployment_id}")
	@Produces(MediaType.APPLICATION_JSON + ";qs=0.5")
	public Response getDeploymentJSON(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + " [in JSON]");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		String json = XMLBuilder.getDeploymentJSON(deployment, applicationName);
		
		return buildResponse(Status.OK, json);
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
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
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
			return buildResponse(Status.NOT_FOUND, "Deployment id = " +  deploymentId + " not found in database");
		}
		
		deployment.setEndDate(DateUtil.getDateStringLogStandardFormat(new Date()));
		
		if(Configuration.emCalculateEnergyWhenDeletion != null && Configuration.emCalculateEnergyWhenDeletion.equals("yes")) {
			// TODO put this inside a try-catch
			EnergyMeasurement energyMeasurement = getEnergyConsumptionFromEM(applicationName, deployment, null, null);
			EnergyCosumed energyConsumed = new EnergyCosumed();
			energyConsumed.setAppId(applicationName);
			energyConsumed.setInstanceId(deploymentId);
			Data data = new Data();
			data.setEnd(deployment.getEndDate());
			data.setStart(deployment.getStartDate());
			data.setPower(energyMeasurement.getValue() + " Wh");
			energyConsumed.setData(data);
			applicationMonitorClient.postFinalEnergyConsumption(energyConsumed);

		}
		
		//Get the vms
		List<VM> deploymentVms = deployment.getVms();
		List<Image> images = new ArrayList<Image>();
		
		//Delete the vms from VM manager
		for (VM vm : deploymentVms){
			logger.info("DELETING VM: " + vm.getProviderVmId());
			
			vmManagerClient.deleteVM(vm.getProviderVmId());
				
			vm.setStatus(STATE_VM_DELETED);
			
			images.addAll(vm.getImages());
			
			AmqpProducer.sendVMDeletedMessage(applicationName, deployment, vm);
		}
		
		for(Image image : images) {
			if(!image.isDemo()) {
				logger.info("DELETING IMAGE: " + image.getProviderImageId());
				
				vmManagerClient.deleteImage(image.getProviderImageId());
			}
		}
		
		//set the deployment status to terminated
		deployment.setStatus(Dictionary.APPLICATION_STATUS_TERMINATED);
		
		AmqpProducer.sendDeploymentDeletedMessage(applicationName, deployment);
		
		//update deployment in database
		deploymentDAO.update(deployment);
		
		return buildResponse(Status.NO_CONTENT, "");
	}
	
	private EnergyMeasurement getEnergyConsumptionFromEM(String applicationName, Deployment deployment, String startTime, String endTime) {
		double energyConsumed = getPowerOrEnergy(applicationName, deployment, Unit.ENERGY, startTime, endTime);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		return energyMeasurement;
	}
	
	private double getPowerOrEnergy(String applicationName, Deployment deployment, Unit unit,  String startTime, String endTime) {
		// Make sure we have the right configuration
		energyModeller = getEnergyModeller();
		
		List<String> ids = getVmsIds(deployment);

		double energyOrPowerConsumed = 0.0;
		
		Timestamp startTimestamp = null;
		Timestamp endTimestamp = null;
		
		if(startTime != null) {
			startTimestamp = new Timestamp(Long.parseLong(startTime));
		}
		
		if(endTime != null) {
			endTimestamp = new Timestamp(Long.parseLong(endTime));
		}

		try {
			logger.debug("Connecting to Energy Modeller");
			energyOrPowerConsumed = energyModeller.measure(deployment.getProviderId(),  applicationName, "" + deployment.getId(), ids, null, unit, startTimestamp, endTimestamp);
		}
		catch(Exception e) {
			energyOrPowerConsumed = -1.0;
		}
		
		return energyOrPowerConsumed;
	}
	
	private PowerMeasurement getPowerConsumptionFromEM(String applicationName, Deployment deployment, String startTime, String endTime) {
		double powerConsumed = getPowerOrEnergy(applicationName, deployment, Unit.POWER, startTime, endTime);
		
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(powerConsumed);
		
		return powerMeasurement;
	}
	
	@GET
	@Path("{deployment_id}/cost-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getCost(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/cost-consumption");
		
		int deploymentIdInt = Integer.parseInt(deploymentId);
		
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		
		Map<String, List<String>> providerVMIds = providerIdsAndItsVMIds(deployment);
		
		List<VmCost> vmCosts = new ArrayList<VmCost>();
		
		for(Map.Entry<String, List<String>> entry : providerVMIds.entrySet()) {
			VmManagerClient vmManagerClient = prClient.getVMMClient(Integer.parseInt(entry.getKey()));
			logger.info("Asking for the costs of the VMs: " + entry.getValue() + " in provider: " + entry.getKey());
			vmCosts.addAll(vmManagerClient.getVMCosts(entry.getValue()));
		}
		
		double totalCost = 0.0;
		
		for(VmCost vmCost : vmCosts) {
			logger.info("IaaS Cost for VM: " + vmCost.getVmId() + " = " + vmCost.getCost());
			totalCost = totalCost + vmCost.getCost();
		}
		
		totalCost = getPriceModellerClient().getAppTotalCharges(deploymentIdInt, deployment.getSchema(), totalCost);
		
		Cost cost = new Cost();
		cost.setCharges(totalCost);
		cost.setEnergyValue(-1.0);
		cost.setPowerValue(-1.0);
		
		String xml = XMLBuilder.getCostConsumptionForADeployment(cost, applicationName, deploymentId);
		
		return  buildResponse(Status.OK, xml);
	}
	
	protected Map<String, List<String>> providerIdsAndItsVMIds(Deployment deployment) {
		Map<String, List<String>> providerVMIds = new HashMap<String, List<String>>();
		
		for(VM vm : deployment.getVms()) {
			String id = vm.getProviderId();
			
			if(id == null || id == "") {
				id="-1";
			}
			
			List<String> ids = providerVMIds.get(id);
			
			if(ids == null) {
				ids = new ArrayList<String>();
				providerVMIds.put(id, ids);
			}
			
			ids.add(vm.getProviderVmId());
		}
		
		return providerVMIds;
	}
	
	@GET
	@Path("{deployment_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyConsumption(@PathParam("application_name") String applicationName, 
										 @PathParam("deployment_id") String deploymentId,
										 @QueryParam("startTime") String startTime,
			                             @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-consumption");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		EnergyMeasurement energyMeasurement = getEnergyConsumptionFromEM(applicationName, deployment, startTime, endTime);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{deployment_id}/power-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerConsumption(@PathParam("application_name") String applicationName, 
										@PathParam("deployment_id") String deploymentId,
										@QueryParam("startTime") String startTime,
			                            @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-consumption");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		PowerMeasurement energyMeasurement = getPowerConsumptionFromEM(applicationName, deployment, startTime, endTime);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerMeasurementForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId);
				
		return buildResponse(Status.OK, xml);
	}
	
	protected List<String> getVmsIds(Deployment deployment) {
		List<String> ids = new ArrayList<String>();
		
		for(VM vm : deployment.getVms()) {
			ids.add("" + vm.getId());
		}
		
		return ids;
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
	public Response getEnergyEstimationForEvent(@PathParam("application_name") String applicationName, 
												@PathParam("deployment_id") String deploymentId, 
												@PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/energy-estimation");

		double energyConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, eventId, Unit.ENERGY, 0l);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	private double getPowerOrEnergyEstimationPerEvent(String applicationName, String deploymentId, String eventId, Unit unit, long duration) {
		energyModeller = getEnergyModeller();
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		String providerId = deployment.getProviderId();
		List<String> ids = getVmsIds(deployment);
		
		logger.debug("Connecting to Energy Modeller");
		return energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, unit, duration);
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/power-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerEstimationForEvent(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId, @PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/power-estimation");

		double powerConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, eventId, Unit.POWER, 0l);
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(powerConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerEstimationForDeploymentXMLInfo(powerMeasurement, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyMeasurementForEvent(@PathParam("application_name") String applicationName, 
												 @PathParam("deployment_id") String deploymentId, 
												 @PathParam("event_id") String eventId,
												 @QueryParam("startTime") String startTime,
					                             @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/energy-estimation");

		double energyConsumed = getPowerOrEnergyMeasurementPerEvent(applicationName, deploymentId, eventId, Unit.ENERGY, startTime, endTime);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyConsumptionForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	private double getPowerOrEnergyMeasurementPerEvent(String applicationName, String deploymentId, String eventId, Unit unit, String startTime, String endTime) {
		energyModeller = getEnergyModeller();
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		String providerId = deployment.getProviderId();
		List<String> ids = getVmsIds(deployment);
		
		Timestamp startTimestamp = null;
		Timestamp endTimestamp = null;
		
		if(startTime != null) {
			startTimestamp = new Timestamp(Long.parseLong(startTime));
		}
		
		if(endTime != null) {
			endTimestamp = new Timestamp(Long.parseLong(endTime));
		}
		
		logger.debug("Connecting to Energy Modeller");
		return energyModeller.measure(providerId,  applicationName, deploymentId, ids, eventId, unit, startTimestamp, endTimestamp);
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/power-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerConsumptionForEvent(@PathParam("application_name") String applicationName, 
												@PathParam("deployment_id") String deploymentId, 
												@PathParam("event_id") String eventId,
					                            @QueryParam("startTime") String startTime,
					                            @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/power-estimation");

		double powerConsumed = getPowerOrEnergyMeasurementPerEvent(applicationName, deploymentId, eventId, Unit.POWER, startTime, endTime);
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(powerConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerConsumptionForDeploymentXMLInfo(powerMeasurement, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("/events/{event_id}/cost-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getCostEstimation(@PathParam("application_name") String applicationName, 
			                          @PathParam("deployment_id") String deploymentId,
			                          @PathParam("event_id") String eventId) throws InterruptedException {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/cost-estimation");
				
		energyModeller = getEnergyModeller();
		
		int deploymentIdInt = Integer.parseInt(deploymentId);
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		List<String> ids = getVmsIds(deployment);
		
		logger.debug("Connecting to Energy Modeller");

		double energyEstimated = energyModeller.estimate(null,  applicationName, deploymentId, ids, eventId, Unit.ENERGY, 0l);
		double powerEstimated = energyModeller.estimate(null,  applicationName, deploymentId, ids, eventId, Unit.POWER, 0l);
		
		// Getting from the queue the necessary variables to query the Price Modeller
		String secKey = EnergyModellerQueueController.generateKey(applicationName, eventId, deploymentId, ids, EnergyModellerQueueController.SEC);
		
		Thread.sleep(1000l);
		
		EnergyModellerMessage emMessageSec = getEnergyModellerQueueController().getPredictionMessage(secKey); 
		
		Cost cost = new Cost();
		cost.setEnergyValue(energyEstimated);
		cost.setPowerValue(powerEstimated);
		
		if(emMessageSec != null) {
			logger.info("Parsing duration value of: " + emMessageSec.getValue());
			
			long duration = (long) Double.parseDouble(emMessageSec.getValue());
			
			LinkedList<VMinfo> vmInfos = new LinkedList<VMinfo>();
			for(VM vm : deployment.getVms()) {
				 VMinfo vmInfo = new VMinfo(vm.getRamActual(), 
						 					vm.getCpuActual(), 
						 					vm.getDiskActual() * 1024l,
						 					duration);
				 
				 vmInfos.add(vmInfo);
			}
						
			if(priceModellerClient == null) {
				priceModellerClient = PriceModellerClient.getInstance();
			}
			
			System.out.println("######## deploymentId: " + deploymentIdInt + " energyEstimated: " + energyEstimated + " schema: " + deployment.getSchema() + " duration: " + duration);
			
			double charges = priceModellerClient.getEventPredictedChargesOfApp(deploymentIdInt, vmInfos, energyEstimated, deployment.getSchema());
			cost.setCharges(charges);
		} else {
			cost.setCharges(-1.0d);
		}
		
		// We create the XMl response
		String xml = XMLBuilder.getCostEstimationForAnEventInADeploymentXMLInfo(cost, applicationName, deploymentId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
}
