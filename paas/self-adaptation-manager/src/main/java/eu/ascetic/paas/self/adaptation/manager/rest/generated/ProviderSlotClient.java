/**
 * Copyright 2016 University of Leeds
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
 * Jersey REST client generated for REST resource:ProviderSlot [/{id}/slots]<br>
 * USAGE:
 * <pre>
 *        ProviderSlotClient client = new ProviderSlotClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author Richard Kavanagh
 */
public class ProviderSlotClient {
    private WebResource webResource;
    private Client client;
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";
    private static String baseUri = "http://192.168.3.16/provider-registry/";

    public ProviderSlotClient(String id) {
try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            baseUri = config.getString("paas.self.adaptation.manager.provider.registry.rest.uri", baseUri);
            config.setProperty("paas.self.adaptation.manager.provider.registry.rest.uri", baseUri);
        } catch (ConfigurationException ex) {
            Logger.getLogger(ProviderSlotClient.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }        
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        String resourcePath = java.text.MessageFormat.format("{0}/slots", new Object[]{id});
        webResource = client.resource(baseUri).path(resourcePath);
    }

    public void setResourcePath(String id) {
        String resourcePath = java.text.MessageFormat.format("{0}/slots", new Object[]{id});
        webResource = client.resource(baseUri).path(resourcePath);
    }

    public <T> T getSlot(Class<T> responseType) throws UniformInterfaceException {
        WebResource resource = webResource;
        return resource.get(responseType);
    }

    public ClientResponse postSlots(Object requestEntity) throws UniformInterfaceException {
        return webResource.type(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(ClientResponse.class, requestEntity);
    }

    public void close() {
        client.destroy();
    }
    
}
