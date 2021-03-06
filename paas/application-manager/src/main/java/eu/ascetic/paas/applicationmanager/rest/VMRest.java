package eu.ascetic.paas.applicationmanager.rest;

import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_DELETED;
import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_ACTIVE;
import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_STATUS_DEPLOYED; 

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerMessage;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerQueueController;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.ovf.VMLimits;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.rest.util.EnergyModellerConverter;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.slam.sla.SLAAgreementHelper;
import eu.ascetic.paas.applicationmanager.vmmanager.client.ImageUploader;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientBSSC;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystem;

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
 * ASCETiC Application Manager REST API to perform actions over an VMs of an Application
 *
 */
@Path("/applications/{application_name}/deployments/{deployment_id}/vms")
@Component
@Scope("request")
public class VMRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(VMRest.class);
	protected VmManagerClient vmManagerClient = new VmManagerClientBSSC(Configuration.vmManagerServiceUrl);
	@Autowired
	protected ImageDAO imageDAO;
	

	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getVMs(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to /applications/" + applicationName + "/deployments/" + deploymentId + "/vms");
		
		List<VM> vms = deploymentDAO.getById(Integer.parseInt(deploymentId)).getVms();
		
		String xml = XMLBuilder.getCollectionOfVMs(vms, applicationName, Integer.parseInt(deploymentId));
		
		return buildResponse(Status.OK, xml);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postVM(@PathParam("application_name") String applicationName, 
						   @PathParam("deployment_id") String deploymentId,
						   @DefaultValue("false") @QueryParam("force") boolean force,
						   String payload) {
		logger.info("POST request to /applications/" + applicationName + "/deployments/" + deploymentId + "/vms?force=" + force);
		logger.info("With payload: " + payload);
		
		// We verify that the payload and URL are ok.
		// I need to verify the payload it is the correct one. Or return a malformed message. Return 400 Bad Request
		VM vm = ModelConverter.xmlVMToObject(payload);

		if(vm == null) {
			return buildResponse(Status.BAD_REQUEST, "Malformed XML request!!!");
		} else if (vm.getOvfId() == null) {
			return buildResponse(Status.BAD_REQUEST, "Missing ovf-id!!!");
		}

		int deploymentIdInt = 0;
		// I need to get the OVF definition
		try {
			deploymentIdInt = Integer.parseInt(deploymentId);
		} catch(NumberFormatException ex) {
			return buildResponse(Status.BAD_REQUEST, "Invalid deploymentID number!!!");
		}

		Deployment deployment = deploymentDAO.getById(deploymentIdInt);

		if(deployment == null) {
			return buildResponse(Status.BAD_REQUEST, "No deployment by that ID in the DB!!!");
		}

		if(!deployment.getStatus().equals(APPLICATION_STATUS_DEPLOYED)) {
			return buildResponse(Status.BAD_REQUEST, "No Active deployment!!!");
		}

		String ovf = deployment.getOvf();

		boolean ovfIdPresentInOvf = OVFUtils.containsVMWithThatOvfId(ovf, vm.getOvfId());

		if(!ovfIdPresentInOvf) {
			return buildResponse(Status.BAD_REQUEST, "No VM avaiblabe by that ovf-id for this deployment!!!");
		}

		// Now we determine if we are inside the limtis to create a new VM
		VMLimits vmLimits = OVFUtils.getUpperAndLowerVMlimits(OVFUtils.getVirtualSystemForOvfId(deployment.getOvf(), vm.getOvfId()));
		List<VM> vms = vmDAO.getVMsWithOVfIdForDeploymentNotDeleted(vm.getOvfId(), deploymentIdInt);

		if(vms.size() >= vmLimits.getUpperNumberOfVMs()) {
			return buildResponse(Status.BAD_REQUEST, vm.getOvfId() + " number of VMs already at its maximum!!!");
		}
		
		if(force == true) {
			return createVM(ovf, vm, vms, applicationName, vmLimits, deployment);
		} else {
			// If there is no agreement in the database we continue as usual... 
			Agreement agreement = agreementDAO.getAcceptedAgreement(deployment);

			if(agreement == null) {
				return createVM(ovf, vm, vms, applicationName, vmLimits, deployment);
			}
			
			SLAAgreementHelper helper = new SLAAgreementHelper(agreement.getSlaAgreement());
			
			double slaPowerApp = helper.getPowerUsagePerApp();
			double slaPowerVM = helper.getPowerUsagePerOVFId(vm.getOvfId());
			
			if(slaPowerApp == 0.0 && slaPowerVM == 0.0) {
				return createVM(ovf, vm, vms, applicationName, vmLimits, deployment);
			}
			
			List<VM> activeVMs = vmDAO.getVMsWithOvfIdAndActive(deploymentIdInt, vm.getOvfId());
			
			if(activeVMs.size() == 0) {
				return createVM(ovf, vm, vms, applicationName, vmLimits, deployment);
			}
			
			// We get the estimation of creating a VM of that type
			double powerEstimation = getEnergyOrPowerEstimation(applicationName, deploymentId, "" + activeVMs.get(0).getId(), null, Unit.POWER, 0l);
			
			// We check if pass over the violation of the specifci type of VM
			if(powerEstimation > slaPowerApp) {
				return buildResponse(Status.FORBIDDEN, "Over the limit of the SLA agreement per this VM type... !!!");
			}
			
			List<String> ids = getVmsIds(deployment);
			powerEstimation = energyModeller.estimate(deployment.getProviderId(),  applicationName, deploymentId, ids, null,  Unit.POWER, 0l);
			// We check if pass over the violation of the total of the VM
			if(powerEstimation > slaPowerApp) {
				return buildResponse(Status.FORBIDDEN, "Over the limit of the SLA agreement per application... !!!");
			}
			
			return createVM(ovf, vm, vms, applicationName, vmLimits, deployment);
		}
	}
	
	private Response createVM(String ovf, VM vm, List<VM> vms, String applicationName, VMLimits vmLimits, Deployment deployment) {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(ovf);
		VirtualSystem virtualSystem = OVFUtils.getVirtualSystemForOvfId(ovf, vm.getOvfId());
		String diskId = OVFUtils.getDiskId(virtualSystem.getVirtualHardwareSection());
		// We find the file id and for each resource // ovfId
		String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
		// We get the images urls... // ovfHref 
		String urlImg = OVFUtils.getUrlImg(ovfDocument, fileId);
		
		Image image = null;
		
		if(vms.size() > 0 ) {
			image = vms.get(0).getImages().get(0);
			logger.info("IMAGE 1 " + image);
		} else {
			// We need to upload the image first.
			if(OVFUtils.usesACacheImage(virtualSystem)) {
				logger.info("This virtual system uses a cache demo image");
				image = imageDAO.getDemoCacheImage(fileId, urlImg);
				if(image == null) {
					logger.info("The image was not cached, we need to uplaod first");
					image = ImageUploader.uploadImage(urlImg, fileId, true, applicationName, vmManagerClient, applicationDAO, imageDAO);
				}
			} else {
				image = ImageUploader.uploadImage(urlImg, fileId, false, applicationName, vmManagerClient, applicationDAO, imageDAO);
			}
		}
		
		// VM variables:
		String vmName = virtualSystem.getName();
		int cpus = 0;
		
		// We check if we create the VM with different CPUs than the default.
		if(vm.getCpuActual() == 0) {
			cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
		} else if(vm.getCpuActual() >= vmLimits.getMinNumberCPUs() && vm.getCpuActual() <= vmLimits.getMaxNumberCPUs()) {
			cpus = vm.getCpuActual();
		} else {
			cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
		} 
		
		int ramMb = virtualSystem.getVirtualHardwareSection().getMemorySize();
		String isoPath = OVFUtils.getIsoPathFromVm(virtualSystem.getVirtualHardwareSection(), ovfDocument);
		int capacity = OVFUtils.getCapacity(ovfDocument, diskId);
		
		int suffixCounter = vms.size() + 1;
		String suffix = "_" + suffixCounter;
		String iso = "";
		if(isoPath != null) iso = isoPath + suffix;
		
		int priceSchema = OVFUtils.getPriceSchema(virtualSystem.getProductSectionAtIndex(0));
		
		// We create the VM to Deploy
		Vm virtMachine;
		
		if(vm.getPhysicalHost() == null || vm.getPhysicalHost().equals("")) {
			virtMachine = new Vm(vmName + suffix, image.getProviderImageId(), cpus, ramMb, capacity, 0, iso , ovfDocument.getVirtualSystemCollection().getId(), vm.getOvfId(), ""/*deployment.getSlaAgreement()*/ );
		} else {
		    virtMachine = new Vm(vmName + suffix, image.getProviderImageId(), cpus, ramMb, capacity, iso, ovfDocument.getVirtualSystemCollection().getId(), vm.getOvfId(), "", vm.getPhysicalHost());
		}
	    
		logger.debug("virtMachine: " + virtMachine);
		
		// We deploy the VM
		List<Vm> vmsToDeploy = new ArrayList<Vm>();
		vmsToDeploy.add(virtMachine);
		List<String> vmIds = vmManagerClient.deployVMs(vmsToDeploy);
		
		VM vmToDB = null;
		
		for(String id : vmIds) {
			VmDeployed vmDeployed = vmManagerClient.getVM(id);
			
			vmToDB = new VM();
			vmToDB.setIp(vmDeployed.getIpAddress());
			vmToDB.setOvfId(vm.getOvfId());
			vmToDB.setStatus(vmDeployed.getState());
			vmToDB.setProviderVmId(id);
			// TODO I need to update this to get it from the table Agreements... 
			//vmToDB.setSlaAgreement(deployment.getSlaAgreement());
			vmToDB.setNumberVMsMax(vmLimits.getUpperNumberOfVMs());
			vmToDB.setNumberVMsMin(vmLimits.getLowerNumberOfVMs());
			vmToDB.setCpuMin(cpus);
			vmToDB.setCpuActual(cpus);
			vmToDB.setCpuMax(cpus);
			vmToDB.setDiskMin(capacity);
			vmToDB.setDiskActual(capacity);
			vmToDB.setDiskMax(capacity);
			vmToDB.setRamMin(ramMb);
			vmToDB.setRamActual(ramMb);
			vmToDB.setRamMax(ramMb);
			vmToDB.setSwapMax(0);
			vmToDB.setSwapActual(0);
			vmToDB.setSwapMin(0);
			vmToDB.setProviderId(deployment.getProviderId());
			if(priceSchema != -1) {
				vmToDB.setPriceSchema(new Long(priceSchema));
			}
			vmDAO.save(vmToDB);
			
			logger.info("IMAGE 3 " + image);
			
			vmToDB.addImage(image);
			vmDAO.update(vmToDB);
			
			logger.info("After the VM update");
			
			deployment.addVM(vmToDB);
			deploymentDAO.update(deployment);
			//deployment = deploymentDAO.getById(deployment.getId());
			
			logger.info("After the Deployment update");
			
//			getEnergyModeller().writeDeploymentRequest("" +vmToDB.getProviderId(), 
//					                                   applicationName, 
//					                                   "" + deployment.getId(), 
//					                                   "" + vmToDB.getId(), 
//					                                   vmToDB.getProviderVmId(), 
//					                                   Dictionary.APPLICATION_STATUS_DEPLOYED);
			
			AmqpProducer.sendVMDeployedMessage(applicationName, deployment, vmToDB);
		}
		
		return buildResponse(Status.OK, ModelConverter.objectVMToXML(vmToDB));
	}
	
	@DELETE
	@Path("{vm_id}")
	public Response deleteVM(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId, @PathParam("vm_id") String vmId) {
		
		int deploymentIdInt = 0;
		// I need to get the OVF definition
		try {
			deploymentIdInt = Integer.parseInt(deploymentId);
		} catch(NumberFormatException ex) {
			return buildResponse(Status.BAD_REQUEST, "Invalid deploymentID number!!!");
		}
		
		Deployment deployment = deploymentDAO.getById(deploymentIdInt);
		
		if(deployment == null) {
			return buildResponse(Status.BAD_REQUEST, "No deployment by that ID in the DB!!!");
		}
		
		if(!deployment.getStatus().equals(APPLICATION_STATUS_DEPLOYED)) {
			return buildResponse(Status.BAD_REQUEST, "No Active deployment!!!");
		}
		
		int vmIdInt = 0;
		try {
			vmIdInt = Integer.parseInt(vmId);
		} catch(NumberFormatException ex) {
			return buildResponse(Status.BAD_REQUEST, "Invalid vmId number!!!");
		}
		
		VM vm = vmDAO.getById(vmIdInt);
		
		if(vm == null) {
			return buildResponse(Status.BAD_REQUEST, "No VM by that Id in the database!!!");
		}
		
		// Now we determine if we are inside the limtis to create a new VM
		VMLimits vmLimits = OVFUtils.getUpperAndLowerVMlimits(OVFUtils.getVirtualSystemForOvfId(deployment.getOvf(), vm.getOvfId()));
		List<VM> vms = vmDAO.getVMsWithOVfIdForDeploymentNotDeleted(vm.getOvfId(), deploymentIdInt);
		
		if(vms.size() <= vmLimits.getLowerNumberOfVMs()) {
			return buildResponse(Status.BAD_REQUEST, vm.getOvfId() + " number of VMs already at its minimum!!!");
		}
		
		logger.info("DELETING VM: " + vm.getProviderVmId());
		
		vmManagerClient.deleteVM(vm.getProviderVmId());
			
		vm.setStatus(STATE_VM_DELETED);
		
		vmDAO.update(vm);
		
		getEnergyModeller().writeDeploymentRequest(vm.getProviderId(), 
				                                   applicationName, 
				                                   "" + deployment.getId(), 
				                                   "" + vm.getId(), 
				                                   vm.getProviderVmId(), 
				                                   vm.getStatus());
		
		AmqpProducer.sendVMDeletedMessage(applicationName, deployment, vm);
		
		return buildResponse(Status.NO_CONTENT, "");
	}
	
	@GET
	@Path("{vm_id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getVM(@PathParam("application_name") String applicationName,  @PathParam("deployment_id") String deploymentId, @PathParam("vm_id") String vmId) {
		logger.info("POST request to /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId);
		
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		
		String xml = XMLBuilder.getVMXML(vm, applicationName, Integer.parseInt(deploymentId));
		
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimation(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @DefaultValue("0") @QueryParam("duration") long duration) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/energy-estimation");
				
		double energyConsumed = getEnergyOrPowerEstimation(applicationName, deploymentId, vmId, null, Unit.ENERGY, duration);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, null);
				
		return buildResponse(Status.OK, xml);
	}

	@GET
	@Path("{vm_id}/events/{event_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimationForAnEvent(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId,
			                             @DefaultValue("0") @QueryParam("duration") long duration) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/energy-estimation");
				
		double energyConsumed = getEnergyOrPowerEstimation(applicationName, deploymentId, vmId, eventId, Unit.ENERGY, duration);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/power-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerEstimation(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/energy-estimation");
				
		double energyConsumed = getEnergyOrPowerEstimation(applicationName, deploymentId, vmId, null, Unit.POWER, 0l);
		
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerEstimationForAnEventInAVMXMLInfo(powerMeasurement, applicationName, deploymentId, vmId, null);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/power-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerEstimationForAnEvent(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/energy-estimation");
				
		double energyConsumed = getEnergyOrPowerEstimation(applicationName, deploymentId, vmId, eventId, Unit.POWER, 0l);
		
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerEstimationForAnEventInAVMXMLInfo(powerMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	private double getEnergyOrPowerEstimation(String applicationName, String deploymentId, String vmId, String eventId, Unit unit, long duration) {
		energyModeller = getEnergyModeller();
		
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		String providerId = vm.getProviderId();
		
		List<String> ids = new ArrayList<String>();
		ids.add("" + vm.getId());
		
		logger.debug("Connecting to Energy Modeller");

		return energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, unit, duration);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/cost-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getCostEstimation(@PathParam("application_name") String applicationName, 
			                          @PathParam("deployment_id") String deploymentId,
			                          @PathParam("vm_id") String vmId,
			                          @PathParam("event_id") String eventId) throws InterruptedException {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/cost-estimation");
				
		energyModeller = getEnergyModeller();
		
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		List<String> ids = new ArrayList<String>();
		ids.add("" + vm.getId());
		
		String providerId = vm.getProviderId();
		
		logger.debug("Connecting to Energy Modeller");

		double energyEstimated = energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, Unit.ENERGY, 0l);
		double powerEstimated = energyModeller.estimate(providerId,  applicationName, deploymentId, ids, eventId, Unit.POWER, 0l);
		
		// Getting from the queue the necessary variables to query the Price Modeller
		String secKey = EnergyModellerQueueController.generateKey(applicationName, eventId, deploymentId, ids, EnergyModellerQueueController.SEC);
		String countKey = EnergyModellerQueueController.generateKey(applicationName, eventId, deploymentId, ids, EnergyModellerQueueController.COUNT);
		
		Thread.sleep(1000l);
		
		EnergyModellerMessage emMessageSec = getEnergyModellerQueueController().getPredictionMessage(secKey); 
		EnergyModellerMessage emMessageCount = getEnergyModellerQueueController().getPredictionMessage(countKey); 
		
		Cost cost = new Cost();
		cost.setEnergyValue(energyEstimated);
		cost.setPowerValue(powerEstimated);
		
		if(emMessageSec != null && emMessageCount != null) {
			logger.info("Parsing duration value of: " + emMessageSec.getValue());
			logger.info("Parsig number events: " + emMessageCount.getValue());
			
			long duration = (long) Double.parseDouble(emMessageSec.getValue());
			int numberOfevents = (int) Double.parseDouble(emMessageCount.getValue());
			
			int cpu = vm.getCpuActual();
			int ram = (int) vm.getRamActual();
			
			double storage = (double) vm.getDiskActual();
			
			if(priceModellerClient == null) {
				priceModellerClient = PriceModellerClient.getInstance();
			}
			
			int deploymentIdInt = Integer.parseInt(deploymentId);
			Deployment deployment = deploymentDAO.getById(deploymentIdInt);
			logger.info("id: " + deploymentIdInt + " cpu: " + cpu + " ram: " + ram + " Storage: " +  storage + ", Energy estimated: " + energyEstimated + ", Schema: " + deployment.getSchema() + ", duration: " +  duration + ", numberOfEvents: " + numberOfevents);
			double charges = priceModellerClient.getEventPredictedCharges(deploymentIdInt, cpu, ram, storage, energyEstimated, deployment.getSchema(), duration, numberOfevents);
			cost.setCharges(charges);
		} else {
			cost.setCharges(-1.0d);
		}
		
		// We create the XMl response
		String xml = XMLBuilder.getCostEstimationForAnEventInAVMXMLInfo(cost, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEventEnergyConsumption(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId,
			                             @DefaultValue("0") @QueryParam("startTime") long startTime,
			                             @DefaultValue("0") @QueryParam("endTime") long endTime) {
		
		logger.info("GET request to path: /applications/" + applicationName 
				                            + "/deployments/" + deploymentId 
				                            + "/vms/" + vmId 
				                            + "/events/" + eventId 
				                            + "/energy-consumption?"
				                            + "startTime=" + startTime
				                            + "&endTime=" + endTime);


		
		return getEnergyConsumptionInGeneral(applicationName, deploymentId, vmId, eventId, startTime, endTime); 
	}
	
	protected List<String> getVmsIds(Deployment deployment) {
		List<String> ids = new ArrayList<String>();
		
		for(VM vm : deployment.getVms()) {
			if(vm.getStatus() == STATE_VM_ACTIVE) {
				ids.add("" + vm.getId());
			}
		}
		
		return ids;
	}
	
	private Response getEnergyConsumptionInGeneral(String applicationName, String deploymentId, String vmId, String eventId, long startTime, long endTime) {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		
		double energyConsumed = getEnergyOrPowerMeasurement(applicationName, deploymentId, vmId, eventId, Unit.ENERGY, startTime, endTime);
		
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/power-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEventPowerConsumption(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId,
			                             @QueryParam("startTime") long startTime,
			                             @QueryParam("endTime") long endTime,
			                             @Context UriInfo uriInfo) {
		
		logger.info("GET request to path: /applications/" + applicationName 
				                            + "/deployments/" + deploymentId 
				                            + "/vms/" + vmId 
				                            + "/events/" + eventId 
				                            + "/power-consumption?"
				                            + "startTime=" + startTime
				                            + "&endTime=" + endTime);


		return getPowerComsumptionForVM(applicationName, deploymentId, vmId, eventId, startTime, endTime);
	}
	
	private Response getPowerComsumptionForVM(String applicationName, String deploymentId, String vmId, String eventId, long startTime, long endTime) {
		
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		
		double powerConsumed = getEnergyOrPowerMeasurement(applicationName, deploymentId, vmId, eventId, Unit.POWER, startTime, endTime);
		
		powerMeasurement.setValue(powerConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getPowerConsumptionForAnEventInAVMXMLInfo(powerMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyConsumption(@PathParam("application_name") String applicationName, 
										@PathParam("deployment_id") String deploymentId,
										@PathParam("vm_id") String vmId,
										@DefaultValue("0") @QueryParam("startTime") long startTime,
										@DefaultValue("0") @QueryParam("endTime") long endTime) {
		
		logger.info("GET request to path: /applications/" + applicationName 
                	 + "/deployments/" + deploymentId 
                	 + "/vms/" + vmId 
                	 + "/energy-consumption?"
                	 + "startTime=" + startTime
                	 + "&endTime=" + endTime);
		
		return getEnergyConsumptionInGeneral(applicationName, deploymentId, vmId, null, startTime, endTime); 
	}
	
	@GET
	@Path("{vm_id}/power-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPowerConsumption(@PathParam("application_name") String applicationName, 
										@PathParam("deployment_id") String deploymentId,
										@PathParam("vm_id") String vmId,
										@DefaultValue("0") @QueryParam("startTime") long startTime,
										@DefaultValue("0") @QueryParam("endTime") long endTime,
										@Context UriInfo uriInfo) {
		
		logger.info("GET request to path: /applications/" + applicationName 
                + "/deployments/" + deploymentId 
                + "/vms/" + vmId 
                + "/energy-consumption?"
                + "startTime=" + startTime
                + "&endTime=" + endTime);
		
		logger.info("######### 1");
		logger.info("URI Info: " +  uriInfo);
		if(uriInfo != null) {
			logger.info("###################");
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters(); 
		    String startTime2 = queryParams.getFirst("startTime");
		    String endTime2 = queryParams.getFirst("endTime");
		    logger.info("Start Time: " + startTime2 + "End Time:  " + endTime2);
		}
		
		return getPowerComsumptionForVM(applicationName, deploymentId, vmId, null, startTime, endTime);
	}
	
	private double getEnergyOrPowerMeasurement(String applicationName, String deploymentId, String vmId, String eventId, Unit unit, long startTime, long endTime) {
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		List<String> ids = new ArrayList<String>();
		ids.add("" + vm.getId());
		
		String providerId = vm.getProviderId();
			
		return getEnergyOrPowerMeasurement(providerId, applicationName, deploymentId, ids, eventId, unit, startTime, endTime);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/energy-sample")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergySample(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId,
			                             @DefaultValue("0") @QueryParam("startTime") long startTime,
			                             @DefaultValue("0") @QueryParam("endTime") long endTime,
			                             @DefaultValue("0") @QueryParam("interval") long interval) {
		
		logger.info("GET request to path: /applications/" + applicationName 
							                + "/deployments/" + deploymentId 
							                + "/vms/" + vmId 
							                + "/events/" + eventId 
							                + "/energy-sample?"
							                + "startTime=" + startTime
							                + "&endTime=" + endTime 
							                + "&interval=" + interval);
		
		
		
		if(startTime == 0 || endTime == 0) {
			return  buildResponse(Status.BAD_REQUEST, "It is mandatory to specify startTime and endTime!!!");
		} else {
			String payload = null;
			// We get the id of the VM
			VM vm = vmDAO.getById(Integer.parseInt(vmId));
			List<String> vmIds = new ArrayList<String>();
			vmIds.add("" + vm.getId());
			
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(endTime);
			
			if(interval == 0) {
				List<eu.ascetic.paas.applicationmanager.model.EventSample> samples = null;
				// Going for energyApplicationConsumptionData
				List<EventSample> eSamples = energyModeller.eventsData(null, applicationName, deploymentId, vmIds, eventId, startStamp, endStamp);
				
				samples = EnergyModellerConverter.convertList(eSamples);
				
				payload = XMLBuilder.getEventSampleCollectionXMLInfo(samples, applicationName, deploymentId, vmId, eventId);
			} 
			
			return  buildResponse(Status.OK, payload);
		}
	}
}
