package eu.ascetic.paas.applicationmanager.rest;

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

import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * Retrieving deployments by name.
 * 
 * Not prefered implementation, needs to be deleted once the component is moved to a diferent project
 *
 */

@Path("/applications/{application_name}/deploymentname/{deployment_name}")
@Component
@Scope("request")
public class DeploymentNameRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(DeploymentNameRest.class);
	
	/**
	 * Returns the information of an specific deployment in XML format
	 * @param applicationName of name the application in the database
	 * @param deploymentName of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML + ";qs=1")
	public Response getDeploymentXML(@PathParam("application_name") String applicationName, @PathParam("deployment_name") String deploymentName) {
		logger.info("GET request to path: /applications/" + applicationName + "/deploymentname/" + deploymentName + " [in JSON]");
		
		Deployment deployment = deploymentDAO.getDeployment(deploymentName);
		
		String json = XMLBuilder.getDeploymentXML(deployment, applicationName);
		
		return buildResponse(Status.OK, json);
	}
	
	/**
	 * Returns the information of an specific deployment in JSON format
	 * @param applicationName of name the application in the database
	 * @param deploymentName of the Deployment for the previously specify application
	 * @return the stored deployment information 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON + ";qs=0.5")
	public Response getDeploymentJSON(@PathParam("application_name") String applicationName, @PathParam("deployment_name") String deploymentName) {
		logger.info("GET request to path: /applications/" + applicationName + "/deploymentname/" + deploymentName + " [in JSON]");
		
		Deployment deployment = deploymentDAO.getDeployment(deploymentName);
		
		String json = XMLBuilder.getDeploymentJSON(deployment, applicationName);
		
		return buildResponse(Status.OK, json);
	}
}
