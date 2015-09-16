package eu.ascetic.paas.applicationmanager.rest;

import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.datamodel.convert.ApplicationConverter;
import eu.ascetic.paas.applicationmanager.em.EnergyModellerBean;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
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
	
	
	protected Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		// To Allow Javascripts apps to connect to the server
		builder.header("Access-Control-Allow-Origin", "*"); 
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
	 * Creates a new deployment for an Application and associated it to an specific ovf file
	 * @param ovf the ovf file associated to this deployment
	 */
	protected Deployment createDeploymentToApplication(String ovf) {
		Deployment deployment = new Deployment();
	
	    deployment.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		
		deployment.setOvf(ovf);
		String startDate = DateUtil.getDateStringLogStandardFormat(new Date());
		deployment.setStartDate(startDate);
		
		return deployment;
	}
	
	/**
	 * Creates a new application with deployment if the application does not exists in the DB
	 * If the applications exists, it just adds a new deployment to it
	 * @param ovf
	 * @return the XML response of the new deployment
	 */
	protected Response createNewDeployment(String ovf, boolean automaticNegotiation) {
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
		
		return buildResponse(Status.CREATED, XMLBuilder.getApplicationXML(application));
	}
}
