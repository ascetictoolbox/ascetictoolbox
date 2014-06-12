package eu.ascetic.application.manager.rest.api;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
//	delete	/application/{id}
}
