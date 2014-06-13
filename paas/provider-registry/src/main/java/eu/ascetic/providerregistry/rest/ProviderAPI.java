package eu.ascetic.providerregistry.rest;

import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_XML;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.providerregistry.service.ProviderDAO;

/**
 * Provider Registry REST service facade providing basic CRUD actions over the
 * Provider entries in the database.
 * @author David Garcia Perez - Atos
 */
@Path("/")
@Component
@Scope("request")
public class ProviderAPI {
	@Autowired
	public ProviderDAO providerDAO;
	
	@GET
	@Path("/experiments/")
	@Produces(CONTENT_TYPE_XML)
	public Response getProviders() {
		
		return null;
	}
}
