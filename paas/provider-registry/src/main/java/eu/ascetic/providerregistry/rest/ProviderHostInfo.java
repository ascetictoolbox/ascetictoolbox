package eu.ascetic.providerregistry.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;

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
 * @email david.garciaperez@atos.net 
 * 
 * Provider Host Information.
 * 
 */

@Path("/{id}/hostinfo")
@Component
@Scope("request")
public class ProviderHostInfo extends RestAbstract {
	private static Logger logger = Logger.getLogger(ProviderHostInfo.class);
	@Autowired
	protected ProviderDAO providerDAO;

	@GET
	public Response getHostInfo(@PathParam("id") int id) throws UnirestException {
		logger.info("GET /" + id + "/hostinfo");
		
		Provider provider = providerDAO.getById(id);
		
		if(provider != null) {
			HttpResponse<String> response = Unirest.get(provider.getVmmUrl() + "/nodes").header("accept", "application/json").asString();
			String payload = response.getBody();
			return buildResponse(Status.OK, payload);
		} else {
			logger.debug("RESPONSE: 404");
			return buildResponse(Status.NOT_FOUND, "No provider by that id found in the databae.");
		}
	}
}
