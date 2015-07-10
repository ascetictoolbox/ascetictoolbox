package eu.ascetic.paas.applicationmanager.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.ovf.VMLimits;
import eu.ascetic.paas.applicationmanager.rest.util.EnergyModellerConverter;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
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
	protected VmManagerClient vmManagerClient = new VmManagerClientBSSC();
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
	public Response postVM(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId, String payload) {
		logger.info("POST request to /applications/" + applicationName + "/deployments/" + deploymentId + "/vms");
		logger.info("With payload: " + payload);
		
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
		
		String ovf = deployment.getOvf();
		
		boolean ovfIdPresentInOvf = OVFUtils.containsVMWithThatOvfId(ovf, vm.getOvfId());
		
		if(!ovfIdPresentInOvf) {
			return buildResponse(Status.BAD_REQUEST, "No VM avaiblabe by that ovf-id for this deployment!!!");
		}
		
		// Now we determine if we are inside the limtis to create a new VM
		VMLimits vmLimits = OVFUtils.getUpperAndLowerVMlimits(OVFUtils.getProductionSectionForOvfID(ovf,  vm.getOvfId()));
		List<VM> vms = vmDAO.getVMsWithOVfIdForDeploymentNotDeleted(vm.getOvfId(), deploymentIdInt);
		
		if(vms.size() >= vmLimits.getUpperNumberOfVMs()) {
			return buildResponse(Status.BAD_REQUEST, vm.getOvfId() + " number of VMs already at its maximum!!!");
		}
		
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
		int cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
		int ramMb = virtualSystem.getVirtualHardwareSection().getMemorySize();
		String isoPath = OVFUtils.getIsoPathFromVm(virtualSystem.getVirtualHardwareSection(), ovfDocument);
		int capacity = OVFUtils.getCapacity(ovfDocument, diskId);
		
		int suffixCounter = vms.size() + 1;
		String suffix = "_" + suffixCounter;
		String iso = "";
		if(isoPath != null) iso = isoPath + suffix;
		
		// We create the VM to Deploy
		Vm virtMachine = new Vm(vmName + suffix, image.getProviderImageId(), cpus, ramMb, capacity, 0, iso , ovfDocument.getVirtualSystemCollection().getId(), vm.getOvfId(), ""/*deployment.getSlaAgreement()*/ );
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
			vmDAO.save(vmToDB);
			
			vmToDB.addImage(image);
			vmDAO.update(vmToDB);
			
			deployment.addVM(vmToDB);
			deploymentDAO.update(deployment);
			//deployment = deploymentDAO.getById(deployment.getId());
			
			AmqpProducer.sendVMDeployedMessage(applicationName, deployment, vmToDB);
		}
		
		return buildResponse(Status.OK, ModelConverter.objectVMToXML(vmToDB));
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
	@Path("{vm_id}/events/{event_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyEstimation(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/energy-estimation");
				
		energyModeller = getEnergyModeller();
		
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		List<String> ids = new ArrayList<String>();
		ids.add(vm.getProviderVmId());
		
		logger.debug("Connecting to Energy Modeller");

		double energyConsumed = energyModeller.measure(null,  applicationName, ids, eventId, Unit.ENERGY, null, null);
		
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{vm_id}/events/{event_id}/energy-consumption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyConsumption(@PathParam("application_name") String applicationName, 
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

		// Make sure we have the right configuration
		energyModeller = getEnergyModeller();
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		double energyConsumed = 0.0;
		
		VM vm = vmDAO.getById(Integer.parseInt(vmId));
		List<String> ids = new ArrayList<String>();
		ids.add(vm.getProviderVmId());
		
		logger.debug("Connecting to Energy Modeller");
		
		if(startTime == 0) {
			energyConsumed = energyModeller.measure(null,  applicationName, ids, eventId, Unit.ENERGY, null, null); 
		} else if(endTime == 0) {
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(System.currentTimeMillis());
			
			energyConsumed = energyModeller.measure(null,  applicationName, ids, eventId, Unit.ENERGY, startStamp, endStamp); 
		} else {
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(endTime);
			
			energyConsumed = energyModeller.measure(null,  applicationName, ids, eventId, Unit.ENERGY, startStamp, endStamp); 
		}
		
		energyMeasurement.setValue(energyConsumed);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, eventId);
				
		return buildResponse(Status.OK, xml);
	}
	
//	@GET
//	@Path("{vm_id}/events/{event_id}/power-consumption")
//	@Produces(MediaType.APPLICATION_XML)
//	public Response getPowerConsumption(@PathParam("application_name") String applicationName, 
//			                             @PathParam("deployment_id") String deploymentId,
//			                             @PathParam("vm_id") String vmId,
//			                             @PathParam("event_id") String eventId,
//			                             @DefaultValue("0") @QueryParam("startTime") long startTime,
//			                             @DefaultValue("0") @QueryParam("endTime") long endTime) {
//		logger.info("GET request to path: /applications/" + applicationName 
//				                            + "/deployments/" + deploymentId 
//				                            + "/vms/" + vmId 
//				                            + "/events/" + eventId 
//				                            + "/energy-consumption?"
//				                            + "startTime=" + startTime
//				                            + "&endTime=" + endTime);
//
//		// Make sure we have the right configuration
//		energyModeller = getEnergyModeller();
//		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
//		double energyConsumed = 0.0;
//		
//		VM vm = vmDAO.getById(Integer.parseInt(vmId));
//		List<String> ids = new ArrayList<String>();
//		ids.add(vm.getProviderVmId());
//		
//		logger.debug("Connecting to Energy Modeller");
//		
//		if(startTime == 0) {
//			energyConsumed = energyModeller.energyApplicationConsumption(null, applicationName, ids, eventId);
//		} else if(endTime == 0) {
//			Timestamp startStamp = new Timestamp(startTime);
//			Timestamp endStamp = new Timestamp(System.currentTimeMillis());
//			
//			energyConsumed = energyModeller.applicationConsumptionInInterval(null, applicationName, ids, eventId, Unit.ENERGY, startStamp, endStamp);
//		} else {
//			Timestamp startStamp = new Timestamp(startTime);
//			Timestamp endStamp = new Timestamp(endTime);
//			
//			energyConsumed = energyModeller.applicationConsumptionInInterval(null, applicationName, ids, eventId, Unit.ENERGY, startStamp, endStamp);
//		}
//		
//		energyMeasurement.setValue(energyConsumed);
//		
//		// We create the XMl response
//		String xml = XMLBuilder.getEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, applicationName, deploymentId, vmId, eventId);
//				
//		return buildResponse(Status.OK, xml);
//	}
	
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
			String providerVMId = vm.getProviderVmId();
			List<String> vmIds = new ArrayList<String>();
			vmIds.add(providerVMId);
			
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(endTime);
			
			if(interval == 0) {
				List<eu.ascetic.paas.applicationmanager.model.EventSample> samples = null;
				// Going for energyApplicationConsumptionData
				List<EventSample> eSamples = energyModeller.eventsData(null, applicationName, vmIds, eventId, startStamp, endStamp);
				
				samples = EnergyModellerConverter.convertList(eSamples);
				
				payload = XMLBuilder.getEventSampleCollectionXMLInfo(samples, applicationName, deploymentId, vmId, eventId);
			} 
			
			return  buildResponse(Status.OK, payload);
		}
	}
}
