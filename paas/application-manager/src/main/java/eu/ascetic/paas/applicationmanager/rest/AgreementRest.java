package eu.ascetic.paas.applicationmanager.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.dao.AgreementDAO;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;

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
	@Autowired
	protected AgreementDAO agreementDAO;

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
}
