package net.atos.ari.seip.CloudManager.REST.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.atos.ari.seip.CloudManager.REST.utils.Paths;

@Path(Paths.NETWORK)
public class NetworkAPI {

	@GET
	@Path("/create")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String CreateNetwork(){
		return "network created";
	}
	
	@GET
	@Path("/info/{networkID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String GetNetworkInfo(@PathParam("networkID") String networkID){
		return "network info";
	}
	
	@DELETE
	@Path("/destroy/{networkID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String DestroyNetwork(@PathParam("networkID") String networkID){
		return "network destroyed";
	}
	
}
