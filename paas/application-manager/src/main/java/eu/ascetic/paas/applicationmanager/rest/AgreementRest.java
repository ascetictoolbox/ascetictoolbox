package eu.ascetic.paas.applicationmanager.rest;

import java.sql.Timestamp;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.slasoi.gslam.syntaxconverter.SLASOIRenderer;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.rest.util.TimeStampComparator;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;
import eu.ascetic.paas.applicationmanager.slam.sla.model.SLA;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImplNoOsgi;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * ASCETiC Application Manager REST API to perform actions over an agreements
 *
 */

@Path("/applications/{application_name}/deployments/{deployment_id}/agreements")
@Component
@Scope("request")
public class AgreementRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(AgreementRest.class);
	protected SLASOIRenderer rendeder = new SLASOIRenderer();
	protected NegotiationWsClient client = new NegotiationWsClient();

	/**
	 * Returns the different agreements for a deployment
	 * @param applicationName of name the application in the database
	 * @param deploymentId of the Deployment for the previously specify application
	 * @return an collection of agreements
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getAgreements(@PathParam("application_name") String applicationName, @PathParam("deployment_id") String deploymentId) {
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/agreements");
		
		List<Agreement> agreements = deploymentDAO.getById(Integer.parseInt(deploymentId)).getAgreements();
		String xml = XMLBuilder.getCollectionOfAgreements(agreements, applicationName, Integer.parseInt(deploymentId));
		
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{agreement_id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getAgreement(@PathParam("application_name") String applicationName, 
			                     @PathParam("deployment_id") String deploymentId,
			                     @PathParam("agreement_id") String agreementId) {
		
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/agreements/" + agreementId);

		Agreement agreement = agreementDAO.getById(Integer.parseInt(agreementId));
		String xml = XMLBuilder.getAgreementXML(agreement, applicationName, Integer.parseInt(deploymentId));
		
		return buildResponse(Status.OK, xml);
	}
	
	@GET
	@Path("{agreement_id}/sla")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSlaAgreement(@PathParam("application_name") String applicationName, 
			                     @PathParam("deployment_id") String deploymentId,
			                     @PathParam("agreement_id") String agreementId) {
		
		logger.info("GET request to path: /applications/" + applicationName + "/deployments/" + deploymentId + "/agreements/" + agreementId);

		Agreement agreement = agreementDAO.getById(Integer.parseInt(agreementId));
		
		return buildResponse(Status.OK, agreement.getSlaAgreement());
	}
	
	@PUT
	@Path("{agreement_id}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response acceptSlaAgreement(@PathParam("application_name") String applicationName, 
									   @PathParam("deployment_id") String deploymentId,
									   @PathParam("agreement_id") String agreementId,
									   String payload) throws Exception {
		
		Agreement agreement = ModelConverter.xmlAgreementToObject(payload);
		
		// TODO The schema ID needs to be entered by the REST API
		PriceModellerClient.getInstance().initializeApplication(applicationName, Integer.parseInt(deploymentId), 1);
		
		if(agreement.isAccepted()) {
			Deployment deployment = deploymentDAO.getById(Integer.parseInt(deploymentId));
			List<Agreement> agreements = deployment.getAgreements();
			
			Agreement agreementInDB = deploymentContainsAgreement(agreements, Integer.parseInt(agreementId));
			
			if(agreementInDB != null) {
				
				if(deploymentDosNotContainsAcceptedAgreement(agreements)) {
					Timestamp now = new Timestamp(System.currentTimeMillis());
					
					if(TimeStampComparator.isInTheFuture(now, agreementInDB.getValidUntil())) {
						// TODO I have some duplication of code here with AcceptAgreementEventHandler class
						SLASOITemplateParser parser = new SLASOITemplateParser();
						SLATemplate slat = parser.parseTemplate(agreementInDB.getSlaAgreement());
						
						// We create a client to the SLAM
						SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
						client.setSlaTranslator(slaTranslator);
						
						logger.debug("Sending create agreement SOAP request...");
						SLA slaAgreement = client.createAgreement(Configuration.slamURL, slat, agreementInDB.getNegotiationId());
						logger.debug("SLA:");
						logger.debug(slaAgreement);  
						
						// TODO for new workflow of Price Modeller... 
						// We calculate the new price, since we are not updating the SLATemplate Price I'm doing this here:
						//double price = PriceModellerClient.calculatePrice(1, deployment.getId(), 100.0);
						//deployment.setPrice("" + price);
						
						// TODO remove this when it is not null
						// We store the new agreement in the db:
						
						agreementInDB.setAccepted(true);
						agreementInDB.setSlaAgreement(slaAgreement.getUuid());
						//agreementInDB.setPrice("" + price);
						
						agreementDAO.update(agreementInDB);
						
						// We generate the event:
						deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
						
						DeploymentEvent deploymentEvent = new DeploymentEvent();
						deploymentEvent.setApplicationName(applicationName);
						deploymentEvent.setDeploymentId(deployment.getId());
						deploymentEvent.setDeploymentStatus(deployment.getStatus());
						
						// We save the changes to the DB
						deploymentDAO.update(deployment);
						
						//We notify that the deployment has been modified
						deploymentEventService.fireDeploymentEvent(deploymentEvent);
						
						return buildResponse(Status.ACCEPTED, XMLBuilder.getAgreementXML(agreementInDB, applicationName, Integer.parseInt(deploymentId)));
						
					} else {
						return buildResponse(Status.FORBIDDEN, "The selected agreement has already expired. It is necessary to restart the deployment process.");
					}
				} else {
					return buildResponse(Status.FORBIDDEN, "This agreement or a different one from the deployment has been previously accepted.");
				}
				
			} else {
				return buildResponse(Status.FORBIDDEN, "Or agreement it is not registered in the Application Manager DB or it does not belong to the requested deployment.");
			}
		} else {
			return buildResponse(Status.BAD_REQUEST, "The only valid change of state it is <accepted>true</accepted>");
		}
	}
	
	private Agreement deploymentContainsAgreement(List<Agreement> agreements, int agreementId) {
		if(agreements != null) {
			for(Agreement agreement : agreements) {
				if(agreement.getId() == agreementId) {
					return agreement;
				}
			}
		}
		
		return null;
	}
	
	private boolean deploymentDosNotContainsAcceptedAgreement(List<Agreement> agreements) {
		
		for(Agreement agreement : agreements) {
			if(agreement.isAccepted()) {
				return false;
			}
		}
		
		return true;
	}
}
