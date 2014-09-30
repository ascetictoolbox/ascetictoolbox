package eu.ascetic.paas.applicationmanager.rest;

import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
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
 * @email david.garciaperez@atos.net 
 * 
 * ASCETiC Application Manager REST API to perform actions over an VMs of an Application
 *
 */
@Path("/applications/{application_name}/deployments/{deployment_id}/vms")
@Component
@Scope("request")
public class VMRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(VMRest.class);
	private static final Random fRandom = new Random();

	@GET
	@Path("{vm_id}/events/{event_id}/energy-estimation")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEnergyConsumption(@PathParam("application_name") String applicationName, 
			                             @PathParam("deployment_id") String deploymentId,
			                             @PathParam("vm_id") String vmId,
			                             @PathParam("event_id") String eventId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/energy-estimation");
				
		// Fake Energy Measurement
		double mean = 100.0f; 
	    double variance = 5.0f;
	    
	    double energyValue = mean + fRandom.nextGaussian() * variance;
	    
	    EnergyMeasurement energyMeasurement = new EnergyMeasurement();
	    energyMeasurement.setValue(energyValue);
		
		// We create the XMl response
		String xml = XMLBuilder.getEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, 
																		  applicationName, 
																		  deploymentId, 
																		  vmId, 
																		  eventId);
				
		return buildResponse(Status.OK, xml);
	}
}
