/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.rest.generated;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Jersey REST client generated for REST resource:DeploymentRest
 * [/applications/{application_name}/deployments]<br>
 * USAGE:
 * <pre>
 *        RestDeploymentClient client = new RestDeploymentClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author Richard Kavanagh
 */
public class RestDeploymentClient {

    private WebResource webResource;
    private final Client client;
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";
    private static String baseUri = "http://192.168.3.16/application-manager/";

    public RestDeploymentClient(String applicationName) {
        try {
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
        }
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{applicationName});
        webResource = client.resource(baseUri).path(resourcePath);
    }

    public void setResourcePath(String application_name) {
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{application_name});
        webResource = client.resource(baseUri).path(resourcePath);
    }

    public <T> T getDeployment(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getPowerConsumption(Class<T> responseType, String deployment_id, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/power-consumption", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeploymentJSON(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
    }

    public ClientResponse deleteDeployment_XML(Object requestEntity, String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id})).type(javax.ws.rs.core.MediaType.APPLICATION_XML).delete(ClientResponse.class, requestEntity);
    }

    public ClientResponse deleteDeployment_JSON(Object requestEntity, String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id})).type(javax.ws.rs.core.MediaType.APPLICATION_JSON).delete(ClientResponse.class, requestEntity);
    }

    public ClientResponse deleteDeployment_TEXT(Object requestEntity, String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id})).type(javax.ws.rs.core.MediaType.TEXT_PLAIN).delete(ClientResponse.class, requestEntity);
    }

    public <T> T getCostEstimation(Class<T> responseType, String event_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("events/{0}/cost-estimation", new Object[]{event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getPowerEstimationForEvent(Class<T> responseType, String deployment_id, String event_id, String duration) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (duration != null) {
            resource = resource.queryParam("duration", duration);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/power-estimation", new Object[]{deployment_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeploymentOvf(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/ovf", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getPowerConsumptionForEvent(Class<T> responseType, String deployment_id, String event_id, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/power-consumption", new Object[]{deployment_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeployments(Class<T> responseType, String status) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (status != null) {
            resource = resource.queryParam("status", status);
        }
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeploymentAgreement(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/agreement", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getEnergyEstimation(Class<T> responseType, String deployment_id, String duration) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (duration != null) {
            resource = resource.queryParam("duration", duration);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}//energy-estimation", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeploymentsJSON(Class<T> responseType, String status) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (status != null) {
            resource = resource.queryParam("status", status);
        }
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T getSLALimits(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/sla-limits", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public ClientResponse postDeploymentJSON(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }

    public <T> T getEnergyEstimationForEvent(Class<T> responseType, String deployment_id, String event_id, String duration) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (duration != null) {
            resource = resource.queryParam("duration", duration);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-estimation", new Object[]{deployment_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getPowerEstimation(Class<T> responseType, String deployment_id, String duration) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (duration != null) {
            resource = resource.queryParam("duration", duration);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/power-estimation", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public ClientResponse postDeployment(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }

    public <T> T getEnergyConsumption(Class<T> responseType, String deployment_id, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/energy-consumption", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getEnergyMeasurementForEvent(Class<T> responseType, String deployment_id, String event_id, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-consumption", new Object[]{deployment_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }
    
    public <T> T getCost(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/cost-consumption", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }    

    public ClientResponse putDeploymentAgreement(Object requestEntity, String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}/agreement", new Object[]{deployment_id})).type(javax.ws.rs.core.MediaType.APPLICATION_XML).put(ClientResponse.class, requestEntity);
    }

    public void close() {
        client.destroy();
    }

}
