
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;



public class PMRestClient {
	
	private WebResource webResource;
    private Client client;
 
	
	public PMRestClient (String applicationName){
		Client client = Client.create();
		String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{applicationName});
		webResource = client.resource("http://localhost/application-manager").path(resourcePath);
		//webResource.accept("application/json").get(ClientResponse.class);
	}
   
	 public void setResourcePath(String applicationName) {
	        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{applicationName});
	        webResource = client.resource("http://192.168.3.16/application-manager/").path(resourcePath);
	    }
	 
	 
	public <T> T  getEnergy(Class<T> responseType, String deploymentId, String startTime, String endTime){
	
	 WebResource resource = webResource;
	 if (startTime != null) {
         resource = resource.queryParam("startTime", startTime);
     }
     if (endTime != null) {
         resource = resource.queryParam("endTime", endTime);
     }
     try{
    	 resource = resource.path(java.text.MessageFormat.format("{0}/energy-consumption", new Object[]{deploymentId}));

    	 T response = resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    	// System.out.println("Output from Server .... \n");
    	 return response;
     }
     catch (Exception e) {
         System.out.println("PM could not take message from EM");
        // logger.error("PM could not take message from EM");
     }
    return null;
	}
	
	

}
