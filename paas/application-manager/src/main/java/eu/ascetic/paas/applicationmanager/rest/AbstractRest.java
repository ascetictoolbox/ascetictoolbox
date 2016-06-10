package eu.ascetic.paas.applicationmanager.rest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.datamodel.convert.ApplicationConverter;
import eu.ascetic.paas.applicationmanager.em.EnergyModellerBean;
import eu.ascetic.paas.applicationmanager.em.amqp.EnergyModellerQueueController;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.rest.util.ApplicationContextHolder;
import eu.ascetic.paas.applicationmanager.rest.util.DateUtil;
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
 * Common methods for all the rest APIs
 *
 */
public abstract class AbstractRest {
	private static Logger logger = Logger.getLogger(AbstractRest.class);
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected VMDAO vmDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;
	//@Autowired
	//protected static EnergyModellerBean energyModellerBean;
	protected static PaaSEnergyModeller energyModeller;
	protected static EnergyModellerQueueController energyModellerQueueController;
	protected static PriceModellerClient priceModellerClient;
	
	
	protected Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		// To Allow Javascripts apps to connect to the server
		builder.header("Access-Control-Allow-Origin", "*"); 
		builder.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		return builder.build();
	}
	
	/**
	 * Constructs the EnergyModeller with an specific configuration if necessary
	 * @return the new EnergyModeller or a previous created object
	 */
	protected static PaaSEnergyModeller getEnergyModeller() {
		if(energyModeller == null) {
			EnergyModellerBean energyModellerBean = ApplicationContextHolder.getContext().getBean(EnergyModellerBean.class);
			logger.info("Getting Energy Modeller...");
			energyModeller = energyModellerBean.getEnergyModeller();
			return energyModeller;
		}
		else {
			return energyModeller;
		}
	}
	
	/**
	 * Constructs the EnergyModellerQueueController with an specific configuration if necessary
	 * @return the new EnergyModellerQueueController or a previous created object
	 */
	protected static EnergyModellerQueueController getEnergyModellerQueueController() {
		if(energyModellerQueueController == null) {
			energyModellerQueueController = ApplicationContextHolder.getContext().getBean(EnergyModellerQueueController.class);

			return energyModellerQueueController;
		}
		else {
			return energyModellerQueueController;
		}
	}
	
	/**
	 * Constructs the PriceModellerClient with an specific configuration if necessary
	 * @return the new PriceModellerClient or a previous created object
	 */
	protected static PriceModellerClient getPriceModellerClient() {
		if(priceModellerClient == null) {
			priceModellerClient = PriceModellerClient.getInstance();

			return priceModellerClient;
		}
		else {
			return priceModellerClient;
		}
	}
	
	/**
	 * Creates a new deployment for an Application and associated it to an specific ovf file
	 * @param ovf the ovf file associated to this deployment
	 */
	protected Deployment createDeploymentToApplication(String ovf) {
		Deployment deployment = new Deployment();
	
	    deployment.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		
		deployment.setOvf(ovf);
		String startDate = DateUtil.getDateStringLogStandardFormat(new Date());
		deployment.setStartDate(startDate);
		
		// We check if the deployment has a deploymentName
		String deploymentName = OVFUtils.getDeploymentName(ovf);
		if(ovf != null) {
			deployment.setDeploymentName(deploymentName);
		}
		
		return deployment;
	}
	
	/**
	 * Creates a new application with deployment if the application does not exists in the DB
	 * If the applications exists, it just adds a new deployment to it
	 * @param ovf
	 * @return the XML response of the new deployment
	 */
	protected Response createNewDeployment(String ovf, String negotiation, String schema, boolean isXML) {
		
		boolean automaticNegotiation = true;
		
		if(negotiation != null && negotiation.equals("manual")) {
			automaticNegotiation = false;
		}
		
		int priceSchema = 1;
		
		// We check the price schema
		if(schema != null) {
			try {
				priceSchema = Integer.parseInt(schema);
			} catch (NumberFormatException e) {
				return buildResponse(Status.BAD_REQUEST, "Invalid price schema format: " + schema + ". Please enter an integer value");
			}
		}
		
		// We get the name of the application:
		String name = OVFUtils.getApplicationName(ovf);
		
		// If the name is null, it means an invalid OVF, we return HTTP code 400 (BAD REQUEST)
		if(name == null) {
			return buildResponse(Status.BAD_REQUEST, "Invalid OVF");
		}
		
		// Now we check if the application exits in the database
		Application application = applicationDAO.getByName(name);
		
		boolean alreadyInDB = true;
	
		if(application == null) {
			application = new Application();
			application.setName(name);
			alreadyInDB = false;
			
			// We sent the message that a new application has been added to the AMQP
			AmqpProducer.sendNewApplicationMessage(application);
		} 

		// We add a new deployment to the application
		Deployment deployment = createDeploymentToApplication(ovf);
		deployment.setSchema(priceSchema);
		application.addDeployment(deployment);

		if(alreadyInDB) {
			applicationDAO.update(application);
		} else {
			applicationDAO.save(application);
		}

		// So we know the id the DB has given to it
		application = applicationDAO.getByName(name);
		// We update the deployment with the information from the DB so we know the Id
		deployment = application.getDeployments().get(application.getDeployments().size() - 1);
		
		//We notify that the deployment has been created
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setApplicationName(application.getName());
		deploymentEvent.setDeploymentId(deployment.getId());
		deploymentEvent.setDeploymentStatus(deployment.getStatus());
		deploymentEvent.setAutomaticNegotiation(automaticNegotiation);
		deploymentEventService.fireDeploymentEvent(deploymentEvent);
		
		Application applicationToBeShown = ApplicationConverter.withoutDeployments(application);
		applicationToBeShown.addDeployment(deployment);
		
		// We notify to the AMQP that the deployment has been created
		AmqpProducer.sendDeploymentSubmittedMessage(applicationToBeShown);
		
		if(isXML) {
			return buildResponse(Status.CREATED, XMLBuilder.getApplicationXML(applicationToBeShown));
		} else {
			return buildResponse(Status.CREATED, XMLBuilder.getApplicationJSON(applicationToBeShown));
		}
	}
	
	protected double getEnergyOrPowerMeasurement(String providerId, String applicationName, String deploymentId, List<String> vmIds, String eventId, Unit unit, long startTime, long endTime) {
		// Make sure we have the right configuration
		energyModeller = getEnergyModeller();

		double energyConsumed = 0.0;

		logger.debug("Connecting to Energy Modeller");

		if(startTime == 0) {
			energyConsumed = energyModeller.measure(providerId,  applicationName, deploymentId , vmIds, eventId, unit, null, null); 

		} else if(endTime == 0) {
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(System.currentTimeMillis());

			energyConsumed = energyModeller.measure(providerId,  applicationName, deploymentId, vmIds, eventId, unit, startStamp, endStamp); 
		} else {
			Timestamp startStamp = new Timestamp(startTime);
			Timestamp endStamp = new Timestamp(endTime);

			energyConsumed = energyModeller.measure(providerId,  applicationName, deploymentId, vmIds, eventId, unit, startStamp, endStamp); 
		}

		return energyConsumed;
	}
	
	/**
	 * It returns the CORS options for AJAX based applications... 
	 * @return
	 */
	@OPTIONS
	public Response options() {
	    return Response
	            .status(Response.Status.OK)
	            .header("Access-Control-Allow-Origin", "*")
	            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT")
	            .header("Access-Control-Allow-Credentials",true)
	            .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
	            .build();
	}
}
