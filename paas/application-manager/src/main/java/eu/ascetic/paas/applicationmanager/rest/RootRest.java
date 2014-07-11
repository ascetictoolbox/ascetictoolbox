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
 * Entry Point of the Application Manager ASCETiC REST API
 * @author David Garcia Perez - Atos
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
	@Path("/")
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
