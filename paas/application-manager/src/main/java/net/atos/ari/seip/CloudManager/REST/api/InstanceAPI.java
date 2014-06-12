package net.atos.ari.seip.CloudManager.REST.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.atos.ari.seip.CloudManager.REST.utils.Paths;

@Path(Paths.INSTANCE)
public class InstanceAPI {

	@GET
	@Path("/{instanceID}/{action}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String ChangeInstanceStatus(@PathParam("instanceID") String instanceID, @PathParam("action") String action){
		return action;		
	}
	
	@GET
	@Path("/info/{instanceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String GetInstanceInfo(@PathParam("instanceID") String instanceID){
		return instanceID;
	}
	
	
}
