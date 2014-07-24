package eu.ascetic.paas.applicationmanager.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;

/**
 * ASCETiC Application Manager REST API to perform actions over an application
 * @author David Garcia Perez - Atos
 *
 */
@Path("/applications")
@Component
@Scope("request")
public class ApplicationRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(ApplicationRest.class);
	@Autowired
	protected ApplicationDAO applicationDAO;
	
	/**
	 * @return a list of applications stored in the database fitting the respective query params, by default this does not return the terminated applications
	 */
	@GET
	//@Path("/applications")
	@Produces(MediaType.APPLICATION_XML)
	public Response getApplications() {
		logger.info("GET request to path: /applications");
		// TODO it is necessary to implement a lot of query params here
		
		// We get the applications from the DB
		List<Application> applications = applicationDAO.getAll();
		
		// We create the XMl response
		String xml = XMLBuilder.getCollectionApplicationsXML(applications);
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * Creates a new application inside the Application Manager
	 * The input for this REST service must be a parsaable OVF ASCETiC document (although we are using standard OVF)
	 * @return The application reference stored in the database with its assigned autogenerated id.
	 */
	@POST
	//@Path("/applications")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postApplication() {
		logger.info("POST request to path: /applications");
		// TODO
		// TODO we need to import the XML Beans of the OVF developed by DJango
		return buildResponse(Status.CREATED, "Method not implemented yet");
	}
	
	/**
	 * Returns the application information stored inside the Application Manager for an specific application
	 * @param id of the application in the database
	 * @return the stored application information 
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getApplication(@PathParam("id") String id) {
		logger.info("GET request to path: /applications/" + id);
		
		Application application = applicationDAO.getById(Integer.parseInt(id));
		String xml = XMLBuilder.getApplicationXML(application);
		
		return buildResponse(Status.OK, xml);
	}
	
	/**
	 * DELETES and application and all its deployments from the database
	 * @param id of the application to be terminated
	 * @return ok if the termination process is possible
	 */
	@DELETE
	@Path("{id}")
	public Response deleteApplication(@PathParam("id") String id) {
		logger.info("DELETE request to path: /applications/" + id);
		// TODO
		// TODO this does not really deletes the application from the database, simply it puts
		//      the application in terminated state and deletes any resource associated to it
		return buildResponse(Status.ACCEPTED, "Method not implemented yet"); //TODO check that it is the right media type I should return... 
	}
}
