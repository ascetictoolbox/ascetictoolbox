package net.atos.ari.seip.CloudManager.REST.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.atos.ari.seip.CloudManager.REST.service.ServiceService;
import net.atos.ari.seip.CloudManager.REST.utils.Paths;
import net.atos.ari.seip.CloudManager.json.ServiceDeployData;
import net.atos.ari.seip.CloudManager.utils.ManifestUtils;
import net.atos.ari.seip.CloudManager.utils.PropertiesUtils;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.optimis.manifest.api.sp.Manifest;

@Path(Paths.SERVICE)
public class ServiceAPI {

	Logger log = Logger.getLogger(this.getClass().getName());
	
	private String endpoint;
	private String secret;
	private String manifest;
	
	private String CLM_CONFIG = "CLM";
	private String CLM_LOG = "LOG";
	
	public ServiceAPI(){
		endpoint = PropertiesUtils.getProperty(CLM_CONFIG, "endpoint.url");
		secret = PropertiesUtils.getProperty(CLM_CONFIG, "secret");
		manifest = PropertiesUtils.getProperty(CLM_CONFIG, "manifestpath");
	}
	 
	@POST
	@Path("/deploy")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String DeployService(){
		ServiceService ss = new ServiceService(endpoint, secret);
		String serviceID = ss.DeployService(new ServiceService(endpoint, secret).parseSPServiceManifest(manifest));
		
		return serviceID;
	}
	
	@POST
	@Path("/deploy/network")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public String DeployServiceONNetwrok(String sddString){
		
		ObjectMapper om = new ObjectMapper();
		ServiceDeployData sdd = new ServiceDeployData();
		
		try {
			sdd = om.readValue(sddString, ServiceDeployData.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(sdd.getNetworkID());
		System.out.println(sdd.getOvf());
		return "service deployed on network";
	}
	
	@GET
	@Path("/start/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String StartService(@PathParam("serviceID") String serviceId){
		return "service started";
	}
	
	@GET
	@Path("/info/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String GetServiceInfo(@PathParam("serviceID") String serviceId){
		log.info("service info "+ serviceId);
		return "service info";
	}
	
	@GET
	@Path("/reboot/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String RebootService(@PathParam("serviceID") String serviceId){
		return "service rebooted";
	}
	
	@DELETE
	@Path("/undeploy/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String UndeployService(@PathParam("serviceID") String serviceId){
		ServiceService ss = new ServiceService(endpoint, secret);
		String result = ss.UndeployService(serviceId);
		return result;
	}
	
	@DELETE
	@Path("/delete/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String DeleteService(@PathParam("serviceID") String serviceId){
		return "service started";
	}
	
	@DELETE
	@Path("/stop/{serviceID}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String StopService(@PathParam("serviceID") String serviceId){
		return "service started";
	}
}
