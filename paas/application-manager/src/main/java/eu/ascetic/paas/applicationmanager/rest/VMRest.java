package eu.ascetic.paas.applicationmanager.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.rest.util.EnergyModellerConverter;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;

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
