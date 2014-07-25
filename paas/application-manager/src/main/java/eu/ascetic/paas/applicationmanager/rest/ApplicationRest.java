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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
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

	/**
	 * @return a list of applications stored in the database fitting the respective query params, by default this does not return the terminated applications
	 */
	@GET
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
	 * @param payload Inputed OVF deployment of an application
	 * @return The application reference stored in the database with its assigned autogenerated id.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postApplication(String payload) {
		logger.info("POST request to path: /applications");
		logger.info("      PAYLOAD: " + payload);
		
		// We get the name of the application:
		String name = OVFUtils.getApplicationName(payload);
		
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
		} 

		// We add a new deployment to the application
		Deployment deployment = createDeploymentToApplication(payload);
		application.addDeployment(deployment);

		if(alreadyInDB) {
			applicationDAO.update(application);
		} else {
			applicationDAO.save(application);
		}

		// So we know the id the DB has given to it
		application = applicationDAO.getByName(name);
		
		return buildResponse(Status.CREATED, XMLBuilder.getApplicationXML(application));
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
