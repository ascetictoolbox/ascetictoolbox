package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Test;
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
 * JUnit tests for Collection class.
 */
public class CollectionTest {

	@Test
	public void pojoTest() {
		Collection collection = new Collection();
		collection.setHref("/href...");
		ArrayList<Link> links = new ArrayList<Link>();
		collection.setLinks(links);
		Items items = new Items();
		collection.setItems(items);

		assertEquals(links, collection.getLinks());
		assertEquals(items, collection.getItems());
		assertEquals("/href...", collection.getHref());
	}

	@Test
	public void collectionOfExperiments() throws Exception {
		String testbedsXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">"
					+ "<items offset=\"0\" total=\"2\">"
						+ "<provider href=\"/101\">"
							+ "<id>101</id>"
							+ "<name>provider1</name>"
							+ "<endpoint>http1</endpoint>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/101\" type=\"application/xml\" />"
						+ "</provider>"
						+ "<provider href=\"/102\">"
							+ "<id>102</id>"
							+ "<name>provider2</name>"
							+ "<endpoint>http2</endpoint>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/102\" type=\"application/xml\" />"
						+ "</provider>"
					+ "</items>"
					+ "<link href=\"/\" rel=\"self\" type=\"application/xml\"/>"
				+ "</collection>";

		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(testbedsXML));

		assertEquals(2, collection.getItems().getProviders().size());
		//assertEquals(2, collection.getItems().getProviders().get(0).getLinks().size());
		assertEquals(102, collection.getItems().getProviders().get(1).getId());
	}
}