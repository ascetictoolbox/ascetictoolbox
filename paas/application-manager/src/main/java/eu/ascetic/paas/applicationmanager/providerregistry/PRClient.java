package eu.ascetic.paas.applicationmanager.providerregistry;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.model.Collection;
import eu.ascetic.providerregistry.model.Items;
import eu.ascetic.paas.applicationmanager.conf.Configuration;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * It creates a REST client to the ASCETiC Provider Registry
 */
public class PRClient {
	private static Logger logger = Logger.getLogger(PRClient.class);
	private WebResource webResource;

	public PRClient() {
		Client client = Client.create();
		webResource = client.resource(Configuration.providerRegistryEndpoint);
	}
	
	/**
	 * Performs the REST query to the Provider Registry and validates the response.
	 * @return
	 */
	public List<Provider> getProviders() {
		ClientResponse response = webResource.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);

		if (response.getStatus() != 200) {
		   logger.info("ERROR getting list of providers: " + response.getEntity(String.class));
		   return new ArrayList<Provider>();
		}
		
		Collection collection;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(response.getEntity(String.class)));
		} catch(Exception exception) {
			logger.info("Imposible to parse this list of providers");
			logger.info("Exception: " + exception.toString());
			return new ArrayList<Provider>();
		}
		
		if(collection != null) {
			Items items = collection.getItems();
			if(items != null) {
				return items.getProviders();
			} else {
				return new ArrayList<Provider>();
			}
		} else {
			return new ArrayList<Provider>();
		}
	}
}
