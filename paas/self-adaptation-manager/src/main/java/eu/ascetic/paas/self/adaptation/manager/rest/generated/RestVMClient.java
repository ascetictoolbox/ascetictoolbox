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
 * Jersey REST client generated for REST resource:VMRest
 * [/applications/{application_name}/deployments/{deployment_id}/vms]<br>
 * USAGE:
 * <pre>
 *        RestVMClient client = new RestVMClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author Richard Kavanagh
 */
public class RestVMClient {
    private WebResource webResource;
    private Client client; //http://localhost:8080//webresources
    private static final String BASE_URI = "http://192.168.3.16/application-manager/";

    public RestVMClient(String application_name, String deployment_id) {
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments/{1}/vms", new Object[]{application_name, deployment_id});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public void setResourcePath(String application_name, String deployment_id) {
        String resourcePath = java.text.MessageFormat.format("applications/{0}/deployments/{1}/vms", new Object[]{application_name, deployment_id});
        webResource = client.resource(BASE_URI).path(resourcePath);
    }

    public <T> T getEnergySample(Class<T> responseType, String vm_id, String event_id, String startTime, String interval, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (interval != null) {
            resource = resource.queryParam("interval", interval);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-sample", new Object[]{vm_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getVM(Class<T> responseType, String vm_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{vm_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public ClientResponse postVM(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }

    public <T> T getEnergyConsumption(Class<T> responseType, String vm_id, String event_id, String startTime, String endTime) throws UniformInterfaceException {
        WebResource resource = webResource;
        if (startTime != null) {
            resource = resource.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            resource = resource.queryParam("endTime", endTime);
        }
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-consumption", new Object[]{vm_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public ClientResponse deleteVM(String vm_id) throws UniformInterfaceException {
        return webResource.path(java.text.MessageFormat.format("{0}", new Object[]{vm_id})).delete(ClientResponse.class);
    }

    public <T> T getEnergyEstimation(Class<T> responseType, String vm_id, String event_id) throws UniformInterfaceException {
        WebResource resource = webResource;
        resource = resource.path(java.text.MessageFormat.format("{0}/events/{1}/energy-estimation", new Object[]{vm_id, event_id}));
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getVMs(Class<T> responseType) throws UniformInterfaceException {
        WebResource resource = webResource;
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public void close() {
        client.destroy();
    }
    
}
