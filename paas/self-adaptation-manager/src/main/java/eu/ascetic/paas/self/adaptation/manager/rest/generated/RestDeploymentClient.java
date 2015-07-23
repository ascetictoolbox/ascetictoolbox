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
    private Client client;
    private static final String BASE_URI = "http://192.168.3.16/application-manager/";

    public RestDeploymentClient(String application_name) {
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{application_name});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public void setResourcePath(String application_name) {
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments", new Object[]{application_name});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public <T> T getDeployment(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getEnergyEstimationForEvent(Class<T> responseType, String deployment_id, String event_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-estimation", new Object[]{deployment_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public ClientResponse deleteDeployment(String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}", new Object[]{deployment_id})).delete(ClientResponse.class);
    }

    public ClientResponse postDeployment(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }

    public <T> T getEnergyConsumption(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/energy-consumption", new Object[]{deployment_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getDeploymentOvf(Class<T> responseType, String deployment_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/ovf", new Object[]{deployment_id}));
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

    public ClientResponse putDeploymentAgreement(Object requestEntity, String deployment_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}/agreement", new Object[]{deployment_id})).type(javax.ws.rs.core.MediaType.APPLICATION_XML).put(ClientResponse.class, requestEntity);
    }

    public void close() {
        client.destroy();
    }
    
}
