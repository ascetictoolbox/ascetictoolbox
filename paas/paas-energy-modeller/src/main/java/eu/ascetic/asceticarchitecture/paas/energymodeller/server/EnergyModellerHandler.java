package eu.ascetic.asceticarchitecture.paas.energymodeller.server;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.ascetic.asceticarchitecture.paas.energymodeller.interfaces.PaaSEnergyModeller;

@Path("/paas-em")
public class EnergyModellerHandler implements PaaSEnergyModeller{

		
		@POST
		@Path("/startModellingApplicationEnergy/{applicationid}/{providerid}")
		public Response startModellingApplicationEnergy(@PathParam("applicationid") String applicationid,@PathParam("providerid") String providerid) {
	 
			String output = "Going to measure "+applicationid;
			
			return Response.status(200).entity(output).build();
	 
		}
		
		@POST
		@Path("/energyApplicationConsumption/{applicationid}/{providerid}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response energyApplicationConsumption(@PathParam("applicationid") String applicationid,@PathParam("providerid") String providerid) {
	 
			String output = "Going to retrieve energy consumed by "+applicationid;
			
			return Response.status(200).entity(output).build();
	 
		}
		
		@POST
		@Path("/stopModellingApplicationEnergy/{applicationid}/{providerid}")
		public Response stopModellingApplicationEnergy(@PathParam("applicationid") String applicationid,@PathParam("providerid") String providerid) {
	 
			String output = "Going to stop measuring "+applicationid;
			
			return Response.status(200).entity(output).build();
	 
		}

		@GET
		@Path("/energyApplicationEstimation/{applicationid}/{providerid}")
		public Response energyEstimation(@PathParam("applicationid") String applicationid,@PathParam("providerid") String providerid) {
			String output = "Going to estimate energy consumed by "+applicationid;
			
			return Response.status(200).entity(output).build();
		}
		
}
