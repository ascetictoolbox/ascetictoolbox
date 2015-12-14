package eu.ascetic.providerregistry.rest;

import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_XML;
import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ascetic.providerregistry.model.Collection;
import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;
import eu.ascetic.providerregistry.xml.Converter;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @email david.garciaperez@atos.net 
 * 
 * Unit test to verify all the methods and functions of the ProviderAPI.class
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/provider-registry-db-JPA-test-context.xml")
public class ProviderAPITest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	protected ProviderDAO providerDAO;
	
	@Before
	public void setUp() {
		Provider provider1 = new Provider();
		provider1.setName("Provider 1");
		provider1.setVmmUrl("http://provider1.ascetic.com");
		provider1.setSlamUrl("http21...");
		
		Provider provider2 = new Provider();
		provider2.setName("Provider 2");
		provider2.setVmmUrl("http://provider2.ascetic.com");
		provider2.setSlamUrl("http22...");
		
		boolean saved = providerDAO.save(provider1);
		assertTrue(saved);
		saved = providerDAO.save(provider2);
		assertTrue(saved);
	}
	
	@Test
	public void getProvidersJSONTest() throws ParseException {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		Response response = providerAPI.getProvidersJSON();
		
		assertEquals(200, response.getStatus());
		
		String providersJSON = (String) response.getEntity();
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(providersJSON);
		JSONObject jsonObject = (JSONObject) obj;
		
		jsonObject = (JSONObject) jsonObject.get("items");
		JSONArray providersArray = (JSONArray) jsonObject.get("provider");
		jsonObject = (JSONObject) providersArray.get(0);
		assertEquals("Provider 1", jsonObject.get("name"));
		jsonObject = (JSONObject) providersArray.get(1);
		assertEquals("Provider 2", jsonObject.get("name"));
	}
	
	@Test
	public void getProvidersTest() throws Exception {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		Response response = providerAPI.getProviders();
		
		assertEquals(200, response.getStatus());
		
		String providersXML = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(providersXML));
		
		assertEquals("/", collection.getHref());
		assertEquals(1, collection.getLinks().size());
		assertEquals("self", collection.getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, collection.getLinks().get(0).getType());
		assertEquals("/", collection.getLinks().get(0).getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals("Provider 1", collection.getItems().getProviders().get(0).getName());
		assertEquals("http://provider1.ascetic.com", collection.getItems().getProviders().get(0).getVmmUrl());
		assertEquals(2, collection.getItems().getProviders().get(0).getLinks().size());
		assertEquals("Provider 2", collection.getItems().getProviders().get(1).getName());
		assertEquals("http://provider2.ascetic.com", collection.getItems().getProviders().get(1).getVmmUrl());
		assertEquals(2, collection.getItems().getProviders().get(1).getLinks().size());
		assertEquals("self", collection.getItems().getProviders().get(0).getLinks().get(1).getRel());
		assertEquals(CONTENT_TYPE_XML, collection.getItems().getProviders().get(0).getLinks().get(1).getType());
		assertEquals("parent", collection.getItems().getProviders().get(0).getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, collection.getItems().getProviders().get(0).getLinks().get(0).getType());
		assertEquals("/", collection.getItems().getProviders().get(0).getLinks().get(0).getHref());
		assertEquals("self", collection.getItems().getProviders().get(1).getLinks().get(1).getRel());
		assertEquals(CONTENT_TYPE_XML, collection.getItems().getProviders().get(0).getLinks().get(1).getType());
		assertEquals("parent", collection.getItems().getProviders().get(1).getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, collection.getItems().getProviders().get(1).getLinks().get(0).getType());
		assertEquals("/", collection.getItems().getProviders().get(1).getLinks().get(0).getHref());
	}
	
	@Test
	public void getProviderJSONTest() throws Exception {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		List<Provider> providers = providerDAO.getAll();
		int id = providers.get(0).getId();
		
		Response response = providerAPI.getProviderJSON(id);
		
		assertEquals(200, response.getStatus());
		String providerJSON = (String) response.getEntity();
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(providerJSON);
		JSONObject jsonObject = (JSONObject) obj;
		
		assertEquals("Provider 1", jsonObject.get("name"));
	}
	
	@Test
	public void getProviderTest() throws Exception {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		List<Provider> providers = providerDAO.getAll();
		int id = providers.get(0).getId();
		
		Response response = providerAPI.getProvider(id);
		
		assertEquals(200, response.getStatus());
		String providerXML = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Provider provider = (Provider) jaxbUnmarshaller.unmarshal(new StringReader(providerXML));
		assertEquals("/" + id, provider.getHref());
		assertEquals("Provider 1", provider.getName());
		assertEquals("http://provider1.ascetic.com", provider.getVmmUrl());
		assertEquals(2, provider.getLinks().size());
		assertEquals("self", provider.getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(0).getType());
		assertEquals("/" + id, provider.getLinks().get(0).getHref());
		assertEquals("parent", provider.getLinks().get(1).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(1).getType());
		assertEquals("/", provider.getLinks().get(1).getHref());
		
		response = providerAPI.getProvider(Integer.MAX_VALUE);
		assertEquals(404, response.getStatus());
		providerXML = (String) response.getEntity();
		assertEquals("No provider by that id found in the databae.", providerXML);
	}
	
	@Test
	public void postProviderTest() throws Exception {
		String providerRequestXML = "<provider xmlns=\"" + PROVIDER_REGISTRY_NAMESPACE + "\" >" +
										"<name>Name</name>" +
										"<vmm-url>http...</vmm-url>" + 
										"<slam-url>http2...</slam-url>" +
									"</provider>";
		
		List<Provider> providers = providerDAO.getAll();
		int id = providers.get(providers.size() - 1).getId();
		id = id + 1;
		
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		Response response = providerAPI.postProvider(providerRequestXML);
		assertEquals(201, response.getStatus());
		
		String providerXML = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Provider provider = (Provider) jaxbUnmarshaller.unmarshal(new StringReader(providerXML));
		assertEquals("/" + id, provider.getHref());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getVmmUrl());
		assertEquals(2, provider.getLinks().size());
		assertEquals("self", provider.getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(0).getType());
		assertEquals("/" + id, provider.getLinks().get(0).getHref());
		assertEquals("parent", provider.getLinks().get(1).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(1).getType());
		assertEquals("/", provider.getLinks().get(1).getHref());
		
		response = providerAPI.postProvider("");
		assertEquals(400, response.getStatus());
		assertEquals("Wrong provider XML request.", response.getEntity());
	}
	
	@Test
	public void putProviderTest() throws Exception {
		String providerRequestXML = "<provider xmlns=\"" + PROVIDER_REGISTRY_NAMESPACE + "\" >" +
										"<name>Name</name>" +
										"<vmm-url>http...</vmm-url>" + 
										"<slam-url>http2...</slam-url>" +
									"</provider>";
		
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		List<Provider> providers = providerDAO.getAll();
		int id = providers.get(0).getId();
		
		Response response = providerAPI.putProvider(id, providerRequestXML);
		assertEquals(202, response.getStatus());
		
		String providerXML = (String) response.getEntity();
		JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Provider provider = (Provider) jaxbUnmarshaller.unmarshal(new StringReader(providerXML));
		assertEquals("/" + id, provider.getHref());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getVmmUrl());
		assertEquals("http2...", provider.getSlamUrl());
		assertEquals(2, provider.getLinks().size());
		assertEquals("self", provider.getLinks().get(0).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(0).getType());
		assertEquals("/" + id, provider.getLinks().get(0).getHref());
		assertEquals("parent", provider.getLinks().get(1).getRel());
		assertEquals(CONTENT_TYPE_XML, provider.getLinks().get(1).getType());
		assertEquals("/", provider.getLinks().get(1).getHref());
		
		response = providerAPI.putProvider(id, "");
		assertEquals(400, response.getStatus());
		assertEquals("Wrong provider XML request.", response.getEntity());
		
		response = providerAPI.putProvider(Integer.MAX_VALUE, providerRequestXML);
		assertEquals(400, response.getStatus());
		assertEquals("No provider by that id.", response.getEntity());
	}
	
	@Test
	public void deleteProvider() {
		ProviderAPI providerAPI = new ProviderAPI();
		providerAPI.providerDAO = providerDAO;
		
		List<Provider> providers = providerDAO.getAll();
		int id = providers.get(0).getId();
		
		Response response = providerAPI.deleteProvider(id);
		assertEquals(204, response.getStatus());
		assertEquals(1, providerDAO.getAll().size());
		
		response = providerAPI.deleteProvider(Integer.MAX_VALUE);
		assertEquals(400, response.getStatus());
		assertEquals("No provider by that id to be deleted.", response.getEntity());
	}
}
