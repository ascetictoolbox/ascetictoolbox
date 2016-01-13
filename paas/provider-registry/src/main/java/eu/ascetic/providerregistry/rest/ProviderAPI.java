package eu.ascetic.providerregistry.rest;

import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_XML;
import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_JSON;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.providerregistry.model.Link;
import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;
import eu.ascetic.providerregistry.xml.Converter;
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
 * Provider Registry REST service facade providing basic CRUD actions over the
 * Provider entries in the database.
 * 
 */
@Path("/")
@Component
@Scope("request")
public class ProviderAPI {
	private static Logger logger = Logger.getLogger(ProviderAPI.class);
	@Autowired
	protected ProviderDAO providerDAO;
	
	@GET
	@Path("/")
	@Produces(CONTENT_TYPE_XML + ";qs=1")
	public Response getProviders() {
		logger.info("GET / [XML]");

		String xmlRepresentation = Converter.getRootCollectionXML(getProvidersFromDB());
		logger.debug("RESPONSE: " + xmlRepresentation);
		return buildResponse(Status.OK, xmlRepresentation);
	}
	
	private List<Provider> getProvidersFromDB() {
		return providerDAO.getAll();
	}
	
	@GET
	@Path("/")
	@Produces(CONTENT_TYPE_JSON + ";qs=.5")
	public Response getProvidersJSON() {
		logger.info("GET / [JSON]");
		String jsonRepresentation = Converter.getRootCollectionJSON(getProvidersFromDB());
		logger.debug("RESPONSE: " + jsonRepresentation);
		return buildResponse(Status.OK, jsonRepresentation);
	}
	
	@GET
	@Path("/{id}")
	@Produces(CONTENT_TYPE_XML + ";qs=1")
	public Response getProvider(@PathParam("id") int id) {
		logger.info("GET /" + id + " [XML]");

		return prepareProviderResponse(id, false);
	}
	
	private Response prepareProviderResponse(int id, boolean json) {
		Provider provider = providerDAO.getById(id);
		if( provider != null) {
			setProviderExtraInfo(provider);
			
			String text; 
			
			if(json) {
				text = Converter.getProviderJSON(provider);
			} else {
				text = Converter.getProviderXML(provider);
			}
			
			logger.debug("RESPONSE: " + text);
			return buildResponse(Status.OK, text);
		} else {
			logger.debug("RESPONSE: 404");
			return buildResponse(Status.NOT_FOUND, "No provider by that id found in the databae.");
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces(CONTENT_TYPE_JSON + ";qs=.5")
	public Response getProviderJSON(@PathParam("id") int id) {
		logger.info("GET /" + id + " [JSON]");
		
		return prepareProviderResponse(id, true);
	}
	
	@POST
	@Path("/")
	@Consumes(CONTENT_TYPE_XML)
	@Produces(CONTENT_TYPE_XML)
	public Response postProvider(String payload) {
		logger.info("POST / PAYLOAD: " + payload);
		Provider provider = Converter.getProviderObject(payload);
		
		if(provider != null) {
			providerDAO.save(provider);
			List<Provider> providers = providerDAO.getAll();
			Provider providerInDB = providers.get(providers.size() - 1);
			setProviderExtraInfo(providerInDB);
			String xml = Converter.getProviderXML(providerInDB);
			logger.debug("RESPONSE: " + xml);
			return buildResponse(Status.CREATED, xml);
		} else {
			logger.info("Bad provider storing request: " + payload);
			return buildResponse(Status.BAD_REQUEST, "Wrong provider XML request.");
		}
	}
	
	@DELETE
	@Path("/{id}")
	public Response deleteProvider(@PathParam("id") int id) {
		logger.info("DELETE /" + id);
		Provider provider = providerDAO.getById(id);
		if(provider != null) {
			providerDAO.delete(provider);
			logger.debug("Provider deleted.");
			return Response.status(Status.NO_CONTENT).build();
		} else {
			logger.debug("Provider not found in database, impossible to delete");
			return buildResponse(Status.BAD_REQUEST, "No provider by that id to be deleted.");
		}
	}
	
	@PUT
	@Path("/{id}")
	@Consumes(CONTENT_TYPE_XML)
	@Produces(CONTENT_TYPE_XML)
	public Response putProvider(@PathParam("id") int id, String payload) {
		logger.info("PUT / PAYLOAD: " + payload);
		Provider newProviderInfo = Converter.getProviderObject(payload);
		
		if(newProviderInfo != null) {
			Provider provider = providerDAO.getById(id);
			
			if(provider != null) {
				provider.setName(newProviderInfo.getName());
				provider.setVmmUrl(newProviderInfo.getVmmUrl());
				providerDAO.update(provider);
				
				newProviderInfo.setId(id);
				setProviderExtraInfo(newProviderInfo);
				String xml = Converter.getProviderXML(newProviderInfo);
				logger.debug("RESPONSE: " + xml);
				return buildResponse(Status.ACCEPTED, xml);
			} else {
				logger.info("Bad provider id: " + id);
				return buildResponse(Status.BAD_REQUEST, "No provider by that id.");
			}
		} else {
			logger.info("Bad provider updating request: " + payload);
			return buildResponse(Status.BAD_REQUEST, "Wrong provider XML request.");
		}
	}
		
	private void setProviderExtraInfo(Provider provider) {
		provider.setHref("/" + provider.getId());
		
		Link linkSelf = new Link();
		linkSelf.setType(CONTENT_TYPE_XML);
		linkSelf.setRel("self");
		linkSelf.setHref(provider.getHref());
		provider.addLink(linkSelf);
		
		Link linkParent = new Link();
		linkParent.setType(CONTENT_TYPE_XML);
		linkParent.setRel("parent");
		linkParent.setHref("/");
		provider.addLink(linkParent);
	}
	
	private Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		return builder.build();
	}
}
