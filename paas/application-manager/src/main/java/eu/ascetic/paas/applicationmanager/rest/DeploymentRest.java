package eu.ascetic.paas.applicationmanager.rest;

import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_DELETED;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.sla.SLATemplate;
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
import eu.ascetic.paas.applicationmanager.event.deployment.NegotiationEventHandler;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.SLAApplicationTerms;
import eu.ascetic.paas.applicationmanager.model.SLALimits;
import eu.ascetic.paas.applicationmanager.model.SLAVMLimits;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.ovf.VMLimits;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.rest.util.DateUtil;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;
import eu.ascetic.paas.applicationmanager.slam.sla.SLAAgreementHelper;
import eu.ascetic.paas.applicationmanager.slam.sla.model.SLA;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImplNoOsgi;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientBSSC;
import eu.ascetic.utils.ovf.api.OvfDefinition;

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
			
			getEnergyModeller().writeDeploymentRequest(vm.getProviderId(), 
													   applicationName, 
													   "" + deployment.getId(), 
													   "" + vm.getId(), 
													   vm.getProviderVmId(), 
													   vm.getStatus());
			AmqpProducer.sendVMDeletedMessage(applicationName, deployment, vm);
		}
		
		Set<String> imageIds = new HashSet<String>();
		
		for(Image image : images) {
			if(!image.isDemo()) {
				
				String id = image.getProviderImageId();
				
				logger.info("TRYING TO DELETE IMAGE: " + id);
				
				if(!imageIds.contains(id)) {
					logger.info("DELETING IMAGE: " + id);
					
					imageIds.add(id);
					vmManagerClient.deleteImage(id);
				} else {
					logger.info("IMAGE WITH ID: " + id + " WAS ALREADY PREVIOUSLY DELETED");
				}
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
		
		try {
			for(Map.Entry<String, List<String>> entry : providerVMIds.entrySet()) {
				VmManagerClient vmManagerClient = prClient.getVMMClient(Integer.parseInt(entry.getKey()));
				logger.info("Asking for the costs of the VMs: " + entry.getValue() + " in provider: " + entry.getKey());
				vmCosts.addAll(vmManagerClient.getVMCosts(entry.getValue()));
			}
		} catch(NullPointerException ex) {
			logger.warn("Null pointer exception getting IaaS Costs for VMs");
			logger.warn(ex.getStackTrace());
			logger.warn(ex.getMessage());
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
	@Produces(MediaType.APPLICATION_XML + ";qs=1")
	public Response getEnergyConsumptionXML(@PathParam("application_name") String applicationName, 
										 @PathParam("deployment_id") String deploymentId,
										 @QueryParam("startTime") String startTime,
			                             @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-consumption" + " [XML]");
		
		EnergyMeasurement energyMeasurement = getEnergyConsuption(applicationName, deploymentId, startTime, endTime);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId);
				
		return buildResponse(Status.OK, xml);
	}
	
	private EnergyMeasurement getEnergyConsuption(String applicationName, 
			 									   String deploymentId,
			 									   String startTime,
			 									   String endTime) {
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));

		EnergyMeasurement energyMeasurement = getEnergyConsumptionFromEM(applicationName, deployment, startTime, endTime);
		
		return energyMeasurement;
	}
	
	@GET
	@Path("{deployment_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_JSON + ";qs=0.5")
	public Response getEnergyConsumptionJSON(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId, @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
				logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-consumption" + " [JSON]");

				EnergyMeasurement energyMeasurement = getEnergyConsuption(applicationName, deploymentId, startTime, endTime);

				// We create the JSON response
				String json = ModelConverter.objectEnergyMeasurementToJSON(energyMeasurement);

				return buildResponse(Status.OK, json);
	}
	
	@GET
	@Path("{deployment_id}/power-consumption")
	@Produces(MediaType.APPLICATION_XML + ";qs=1")
	public Response getPowerConsumptionXML(@PathParam("application_name") String applicationName, 
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
	
	@GET
	@Path("{deployment_id}/power-consumption")
	@Produces(MediaType.APPLICATION_JSON + ";qs=0,5")
	public Response getPowerConsumptionJSON(@PathParam("application_name") String applicationName, 
										@PathParam("deployment_id") String deploymentId,
										@QueryParam("startTime") String startTime,
			                            @QueryParam("endTime") String endTime) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-consumption");
		
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		PowerMeasurement powerMeasurement = getPowerConsumptionFromEM(applicationName, deployment, startTime, endTime);
		
		// We create the JSON response
		String json = ModelConverter.objectPowerMeasurementToJSON(powerMeasurement);
				
		return buildResponse(Status.OK, json);
	}
	
	
	protected List<String> getVmsIds(Deployment deployment) {
		List<String> ids = new ArrayList<String>();
		
		for(VM vm : deployment.getVms()) {
			ids.add("" + vm.getId());
		}
		
		return ids;
	}
	
	/**
	 * Returns the provider id of a deployment
	 * @param deployment
	 * @return It returns -1 if no VMs deployed, by defualt 1 or the provider id specified in the VMs
	 */
	protected int getProviderId(Deployment deployment) {
		
		List<VM> vms = deployment.getVms();
		
		if(vms == null || vms.size() == 0) {
			return -1;
		}
		
		VM vm = vms.get(0);
		String providerId = vm.getProviderId();
		
		if(providerId == null) {
			return 1;
		}
		
		if(providerId.matches("\\d+")) {
			return Integer.parseInt(providerId);
		} else {
			return 1;
		}
	}
	
	protected List<String> getVmsProviderIds(Deployment deployment) {
		List<String> ids = new ArrayList<String>();
		
		for(VM vm : deployment.getVms()) {
			ids.add(vm.getProviderVmId());
		}
		
		return ids;
	}
	
	@GET
	@Path("{deployment_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimation(@PathParam("application_name") String applicationName, 
										@PathParam("deployment_id") String deploymentId,
										@QueryParam("duration") String durationQuery) {
		
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/energy-estimation");
		
		long duration = 0l;
		
		if(durationQuery != null) {
			duration = Long.parseLong(durationQuery);
		}

		double energyConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, null, Unit.ENERGY, duration);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForDeploymentXMLInfo(energyMeasurement, applicationName, deploymentId, null);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{deployment_id}/power-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerEstimation(@PathParam("application_name") String applicationName, 
									   @PathParam("deployment_id") String deploymentId,
										@QueryParam("duration") String durationQuery) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/power-estimation");

		long duration = 0l;
		
		if(durationQuery != null) {
			duration = Long.parseLong(durationQuery);
		}
		
		double powerConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, null, Unit.POWER, duration);
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(powerConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerEstimationForDeploymentXMLInfo(powerMeasurement, applicationName, deploymentId, null);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimationForEvent(@PathParam("application_name") String applicationName, 
												@PathParam("deployment_id") String deploymentId, 
												@PathParam("event_id") String eventId,
												@QueryParam("duration") String durationQuery) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/energy-estimation");
		
		long duration = 0l;
		
		if(durationQuery != null) {
			duration = Long.parseLong(durationQuery);
		}

		double energyConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, eventId, Unit.ENERGY, duration);
		
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
	public Response getPowerEstimationForEvent(@PathParam("application_name") String applicationName, 
											   @PathParam("deployment_id") String deploymentId, 
											   @PathParam("event_id") String eventId,
											   @QueryParam("duration") String durationQuery) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/power-estimation");

		long duration = 0l;
		
		if(durationQuery != null) {
			duration = Long.parseLong(durationQuery);
		}
		
		double powerConsumed = getPowerOrEnergyEstimationPerEvent(applicationName, deploymentId, eventId, Unit.POWER, duration);
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
	@Path("{deployment_id}/predicted-price-for-next-period")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPredictedPriceForNextPeriod(@PathParam("application_name") String applicationName, 
			                          @PathParam("deployment_id") String deploymentId,
			                          @QueryParam("duration") String duration) throws InterruptedException {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/predicted-price-for-next-period?duration=" + duration);
		
		if(!isNumeric(duration)) {
			return buildResponse(Status.BAD_REQUEST, "Duration must be a positive number!!!");
		}
		
		double totalCost = priceModellerClient.predictPriceForNextPeriod(Integer.parseInt(deploymentId), Double.parseDouble(duration));
		
		Cost cost = new Cost();
		cost.setCharges(totalCost);
		cost.setEnergyValue(-1.0);
		cost.setPowerValue(-1.0);
		
		String xml = XMLBuilder.getCostConsumptionForADeployment(cost, applicationName, deploymentId);
		
		return  buildResponse(Status.OK, xml);
	}
	
	private boolean isNumeric(String str)
	{
	  return str.matches("\\d+(\\.\\d+)?");  //match a number and decimal.
	}
	
	@GET
	@Path("{deployment_id}/events/{event_id}/cost-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getCostEstimation(@PathParam("application_name") String applicationName, 
			                          @PathParam("deployment_id") String deploymentId,
			                          @PathParam("event_id") String eventId) throws InterruptedException {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/events/" + eventId + "/cost-estimation");
				
		energyModeller = getEnergyModeller();
		
		int deploymentIdInt = Integer.parseInt(deploymentId);
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		List<String> ids = getVmsIds(deployment);
		String providerId = deployment.getProviderId();
		
		logger.debug("Connecting to Energy Modeller");

		double energyEstimated = energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, Unit.ENERGY, 0l);
		double powerEstimated = energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, Unit.POWER, 0l);
		
		// Getting from the queue the necessary variables to query the Price Modeller
		String secKey = "none";
		if(ids != null && ids.size() > 0) {
			List<String> vmIds = new ArrayList<String>();
			vmIds.add(ids.get(0));
			secKey = EnergyModellerQueueController.generateKey(applicationName, eventId, deploymentId, vmIds, EnergyModellerQueueController.SEC);
		}
		
		logger.info("secKey: " + secKey);
		
		Thread.sleep(1000l);
		
		EnergyModellerMessage emMessageSec = getEnergyModellerQueueController().getPredictionMessage(secKey); 
		
		logger.info("############################################################################################");
		logger.info("Message recevied by energy modeller in cost-estimation: " + emMessageSec);
		
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
						 					vm.getDiskActual(),
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
	
	@GET
	@Path("{deployment_id}/sla-limits")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSLALimits(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/sla-limits");
		
		int deploymentIdInt = Integer.parseInt(deploymentId);
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		
		Agreement agreement = agreementDAO.getAcceptedAgreement(deployment);
		
		SLALimits slaLimits = new SLALimits();
		String xml;
		
		if(agreement != null) {
			// We need to looks for the guarantees of the Application
			SLAAgreementHelper helper = new SLAAgreementHelper(agreement.getSlaAgreement());
			
			// Power Usage per App
			double powerUsagePerApp = helper.getPowerUsagePerApp();
			if(powerUsagePerApp > 0.0) {
				slaLimits.setPower("" + powerUsagePerApp);
				String units = helper.getPowerUsagePerAppUnits();
				slaLimits.setPowerUnit(units);
			}
			
			// Now we get the ides of the VMs
			List<SLAVMLimits> vmLimits = new ArrayList<SLAVMLimits>();
			slaLimits.setVmLimits(vmLimits);
			
			OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(deployment.getOvf());
			List<String> ids = OVFUtils.getOVFVMIds(ovfDocument);
			for(String id : ids) {
				SLAVMLimits vmLimit = new SLAVMLimits();
				vmLimit.setVmId(id);
				
				//Power
				double vmPower = helper.getPowerUsagePerOVFId(id);
				if(vmPower >= 0.0) {
					vmLimit.setPower("" + vmPower);
					vmLimit.setPowerUnit(helper.getPowerUnitsPerOVFId(id));
				}
				
				//VMLimits
				VMLimits maxMin = OVFUtils.getUpperAndLowerVMlimits(OVFUtils.getVirtualSystemForOvfId(deployment.getOvf(), id));
				
				vmLimit.setMax("" + maxMin.getUpperNumberOfVMs());
				vmLimit.setMin("" + maxMin.getLowerNumberOfVMs());
				
				vmLimits.add(vmLimit);
			}
		}
		
		xml = ModelConverter.slaLitmitsToXML(slaLimits);
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
	@Path("{deployment_id}/renegotiate")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response renegotiateWithPayload(@PathParam("application_name") String applicationName,@PathParam("deployment_id") String deploymentId, String payload) {
		logger.info("POST request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/renegotiate");
		
		//Trying to parse payload
		SLAApplicationTerms appTerms = ModelConverter.xmlSLAApplicationTermsToObject(payload);
		
		if(appTerms != null) {
			return renegotiate(applicationName, deploymentId, appTerms);
		} else {
			return buildResponse(Status.BAD_REQUEST, "Wrong payload: " + payload);
		}
	}
	
	private Response renegotiate(String applicationName, String deploymentId, SLAApplicationTerms appTerms) {
		Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
		
		if(deployment == null) {
			return buildResponse(Status.NOT_FOUND, "Deployment not found!");
		} 
		
		AmqpProducer.sendDeploymentRenegotiatingMessage(applicationName, deployment);
		
		Agreement agreement = agreementDAO.getAcceptedAgreement(deployment);
		
		if(agreement == null) {
			AmqpProducer.sendDeploymentRenegotiatedMessage(applicationName, deployment);
			return buildResponse(Status.OK, "No renegotiation possible!");
		} else if(!Configuration.enableSLAM.equals("yes")) {
			AmqpProducer.sendDeploymentRenegotiatedMessage(applicationName, deployment);
			return buildResponse(Status.OK, "PaaS SLAM disabled!!!");
		}

		String agreementId = agreement.getSlaAgreementId();

		// We create a client to the SLAM
		NegotiationWsClient client = new NegotiationWsClient();
		SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
		client.setSlaTranslator(slaTranslator);
		
		agreementId = client.renegotiate(Configuration.slamURL, agreementId);
		
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(deployment.getOvf());
		
		try {
			SLATemplate[] slats = NegotiationEventHandler.negiotate(agreementId, ovfDefinition, applicationName, deployment.getId(), client, appTerms);
			
			if(slats == null || slats.length == 0) {
				AmqpProducer.sendDeploymentRenegotiatedMessage(applicationName, deployment);
				return buildResponse(Status.OK, "No new agreement possible");
			}
			
			SLATemplate slat = slats[0];
			
			SLA slaAgreement = client.createAgreement(Configuration.slamURL, slat, slat.getUuid().getValue());
			
			if(slaAgreement == null) {
				AmqpProducer.sendDeploymentRenegotiatedMessage(applicationName, deployment);
				return buildResponse(Status.OK, "Not possible to reach an agreement");
			}
			
			// We set the old agreement to false
			agreement.setAccepted(false);
			agreementDAO.update(agreement);
			
			//We update the SLA ID for the deployment:
			deployment = deploymentDAO.getById(deployment.getId());
			deployment.setSlaUUID(slaAgreement.getUuid());
			deploymentDAO.update(deployment);
			
			//We add the new agreement to the db
			Agreement agreement2 = new Agreement(); 
			agreement2.setAccepted(true);
			agreement2.setDeployment(deployment);
			SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
			String xmlRetSlat = rend.renderSLATemplate(slat);
			agreement2.setSlaAgreement(xmlRetSlat);
			agreement2.setNegotiationId(agreementId);
			agreement2.setSlaAgreementId(slat.getUuid().getValue());
			agreement2.setOrderInArray(0);
			
			deployment.addAgreement(agreement);
			deploymentDAO.update(deployment);
			
		} catch(Exception ex) {
			logger.error("Error during the renegotiation process");
			logger.error(ex.getMessage());
			logger.error(ex.getStackTrace());
		}
		
		return null;
	}
	
	@GET
	@Path("{deployment_id}/renegotiate")
	@Produces(MediaType.APPLICATION_XML)
	public Response renegotiate(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/renegotiate");
		
		return renegotiate(applicationName, deploymentId, null);
	}
	
	@GET
	@Path("{deployment_id}/predict-price-next-hour")
	@Produces(MediaType.APPLICATION_XML)
	public Response predictPriceNextHour(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		int deploymentIdInt = Integer.parseInt(deploymentId);
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		
		if(deployment == null) {
			return buildResponse(Status.NOT_FOUND, "Deployment not found!");
		} 
		
		LinkedList<VMinfo> vmInfos = new LinkedList<VMinfo>();
		
		for(VM vm : deployment.getVms()) {
			Long schema = vm.getPriceSchema();
			int priceSchema = 0;
			
			if(schema != null) {
				priceSchema = schema.intValue();
			} else {
				priceSchema = deployment.getSchema();
			}
			
			VMinfo vmInfo = new VMinfo(vm.getId(), vm.getRamActual(), vm.getCpuActual(), vm.getDiskActual(), priceSchema, vm.getProviderId());
			vmInfos.add(vmInfo);
		}
		
		double predictedPrice = priceModellerClient.predictPriceForNextHour(deploymentIdInt, vmInfos);
		
		Cost cost = new Cost();
		cost.setCharges(predictedPrice);
		cost.setEnergyValue(-1.0);
		cost.setPowerValue(-1.0);
		
		String xml = XMLBuilder.getCostConsumptionForADeployment(cost, applicationName, deploymentId);
		
		return  buildResponse(Status.OK, xml);
	}
}
