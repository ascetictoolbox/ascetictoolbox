
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;



public class RestDeploymentClient {

    private WebResource webResource;
    private final Client client;
   // private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";
    private static String baseUri = "http://192.168.3.16/application-manager/";

    public RestDeploymentClient(String applicationName) {
        /*try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            baseUri = config.getString("paas.self.adaptation.manager.application.manager.rest.uri", baseUri);
            config.setProperty("paas.self.adaptation.manager.application.manager.rest.uri", baseUri);
        } catch (ConfigurationException ex) {
            Logger.getLogger(RestDeploymentClient.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }*/
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{applicationName});
        webResource = client.resource(baseUri).path(resourcePath);
    }

    public void setResourcePath(String applicationName) {
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{applicationName});
        webResource = client.resource(baseUri).path(resourcePath);
    }

 
    public <T> T getEnergyEstimationForEvent(Class<T> responseType, String deploymentId, String eventId) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-estimation", new Object[]{deploymentId, eventId}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

  
    public <T> T getEnergyConsumption(Class<T> responseType, String deploymentId) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/energy-consumption", new Object[]{deploymentId}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }


}
