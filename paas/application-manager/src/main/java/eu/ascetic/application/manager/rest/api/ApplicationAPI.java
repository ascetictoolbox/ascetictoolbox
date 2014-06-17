package eu.ascetic.application.manager.rest.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;

import net.atos.ari.seip.CloudManager.REST.utils.Paths;

@Path(Paths.APPLICATION)
public class ApplicationAPI {

	Logger log = Logger.getLogger(this.getClass().getName());
	
	//get		/application/{id}
	@GET
	@Path("/application/{id}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String GetApplicationInfo(@PathParam("id") String id){
		log.info("application info "+ id);
		return "application info";
	}
	
//	put		/application/{id}
	@PUT
	@Path("/update/{id}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateApplication(@PathParam("id") int id,
			@Context HttpHeaders hh, String payload) throws IOException,
			JAXBException {
		log.info("UPDATE /application");		
		return buildResponse(200, "UPDATE /application");
	}
	
//	delete	/application/{id}
	@DELETE
	@Path("/destroy/{id}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String DestroyApplication(@PathParam("id") String id){
		return "application destroyed";
	}
	
	public Response buildResponse(int code, String payload) {
		ResponseBuilder builder = Response.status(code);
		builder.entity(payload);

		return builder.build();
	}
	
}
