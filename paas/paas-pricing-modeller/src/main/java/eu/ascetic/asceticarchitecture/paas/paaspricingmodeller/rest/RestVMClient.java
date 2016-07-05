package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;


public class RestVMClient {
    private WebResource webResource;
    private final Client client; 
    private static final String BASE_URI = "http://192.168.3.16/application-manager/";

    public RestVMClient(String applicationName ,String deploymentId ){
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments/{1}/vms", new Object[]{applicationName, deploymentId});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public void setResourcePath(String applicationName, String deploymentId) {
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments/{1}/vms", new Object[]{applicationName, deploymentId});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public <T> T getEnergyConsumption(Class<T> responseType, String vmId, String eventId, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-consumption", new Object[]{vmId, eventId}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

  
    public <T> T getEnergyEstimation(Class<T> responseType, String vmId, String eventId, String duration) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.queryParam("duration", duration);
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-estimation", new Object[]{vmId, eventId}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

   
}
