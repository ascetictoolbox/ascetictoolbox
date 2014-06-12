package net.atos.ari.seip.CloudManager.elasticity;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import net.atos.ari.seip.CloudManager.REST.utils.Paths;
import net.atos.ari.seip.CloudManager.json.ServiceElasticityData;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@Path(Paths.ELASTICITY_API)
public class ElasticityAPI {

	@POST
	@Path(Paths.SCALE)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServiceElasticityData scale(String sedString){
		ObjectMapper om = new ObjectMapper();
		ServiceElasticityData sed = new ServiceElasticityData();
		
		try {
			sed = om.readValue(sedString, ServiceElasticityData.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//TODO send info to the elasticity controller to react to the elasticity call.
		return sed;
	}
	
}
