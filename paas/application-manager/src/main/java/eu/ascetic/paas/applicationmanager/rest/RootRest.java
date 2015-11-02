package eu.ascetic.paas.applicationmanager.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.Root;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 * Entry Point of the Application Manager ASCETiC REST API
 *
 */

@Path("/")
@Component
@Scope("request")
public class RootRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(RootRest.class);
	
	/**
	 * Root element of the Application Manager REST API
	 * @return a list of links to the different functions in the API
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getRoot() {
		logger.info("REQUEST to Path: /");
		
		Root root = new Root();
		root.setHref("/");
		root.setTimestamp("" + System.currentTimeMillis());
		root.setVersion("0.1-SNAPSHOT");
		
		Link link = new Link();
		link.setRel("applications");
		link.setType(MediaType.APPLICATION_XML);
		link.setHref("/applications");
		root.addLink(link);
		
		return buildResponse(Status.OK, ModelConverter.objectRootToXML(root));
	}
}
