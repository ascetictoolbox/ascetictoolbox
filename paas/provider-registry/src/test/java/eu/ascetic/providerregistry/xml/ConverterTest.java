package eu.ascetic.providerregistry.xml;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import eu.ascetic.providerregistry.model.Link;
import eu.ascetic.providerregistry.model.Provider;

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
 * Test the correct work of the different static methods that convert Java objects to XML
 */
public class ConverterTest {

	@Test
	public void getProviderObjectTest() {
		String providerXML = "<provider xmlns=\"" + PROVIDER_REGISTRY_NAMESPACE + "\" href=\"href...\">" +
								"<id>111</id>" +
								"<name>Name</name>" +
								"<vmm-url>http...</vmm-url>" + 
								"<slam-url>http2...</slam-url>" +
								"<link rel=\"parent\" href=\"/\" type=\"application/xml\" />" +
								"<link rel=\"self\" href=\"/111\" type=\"application/xml\" />" +
							  "</provider>";
		
		Provider provider = Converter.getProviderObject(providerXML);
		
		assertEquals(111l, provider.getId());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getVmmUrl());
		assertEquals("http2...", provider.getSlamUrl());
		assertEquals("href...", provider.getHref());
		assertEquals(2, provider.getLinks().size());
		assertEquals("self", provider.getLinks().get(1).getRel());
		
		// If the parsing it is not possible, it should return a null value
		providerXML = "<xml/>";
		provider = Converter.getProviderObject(providerXML);
		assertEquals(null, provider);
	}
	
	@Test
	public void getProviderObjectFromJSONTest() {
		String json = "{" +
						 "\"href\" : \"/11\"," + 
						 "\"id\" : 11," +
						 "\"name\" : \"Nome\", " +
						 "\"vmm-url\" : \"Punto final\", " +
						 "\"link\" : [ { " +
						 	"\"rel\" : \"self\"," +
						 	"\"href\" : \"/11\"," +
						 	"\"type\" : \"http://provider-registry.ascetic.eu/doc/schemas/xml\"" +
						"}, {" +
							"\"rel\" : \"parent\"," +
							"\"href\" : \"/\"," +
							"\"type\" : \"http://provider-registry.ascetic.eu/doc/schemas/xml\"" +
						"} ]" +
					"}";
		
		Provider provider = Converter.getProviderFromJSON(json);
		
		assertEquals(11l, provider.getId());
		assertEquals("Nome", provider.getName());
		assertEquals("Punto final", provider.getVmmUrl());
	}
	
	@Test
	public void getProviderJSON() throws ParseException {
		Provider provider = new Provider();
		provider.setHref("/11");
		provider.setId(11);
		provider.setName("Nome");
		provider.setVmmUrl("Punto final");
		Link linkParent = new Link();
		linkParent.setRel("parent");
		linkParent.setHref("/");
		linkParent.setType(PROVIDER_REGISTRY_NAMESPACE);
		Link linkSelf = new Link();
		linkSelf.setRel("self");
		linkSelf.setHref("/11");
		linkSelf.setType(PROVIDER_REGISTRY_NAMESPACE);
		provider.addLink(linkSelf);
		provider.addLink(linkParent);
		
		String json = Converter.getProviderJSON(provider);
		
		System.out.println(json);
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);
		JSONObject jsonObject = (JSONObject) obj;
		assertEquals("/11", (String) jsonObject.get("href"));
		assertEquals("Punto final", (String) jsonObject.get("vmm-url"));
	}
	
	@Test
	public void getProviderXML() throws JDOMException, IOException {
		Provider provider = new Provider();
		provider.setHref("/11");
		provider.setId(11);
		provider.setName("Nome");
		provider.setVmmUrl("Punto final");
		Link linkParent = new Link();
		linkParent.setRel("parent");
		linkParent.setHref("/");
		linkParent.setType(PROVIDER_REGISTRY_NAMESPACE);
		Link linkSelf = new Link();
		linkSelf.setRel("self");
		linkSelf.setHref("/11");
		linkSelf.setType(PROVIDER_REGISTRY_NAMESPACE);
		provider.addLink(linkSelf);
		provider.addLink(linkParent);
		
		String xml = Converter.getProviderXML(provider);
			
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:provider");
		xpath.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/11", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("11", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:name");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("Nome", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:vmm-url");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("Punto final", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(2, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
	}
	
	@Test
	public void getCollectionJSONTest() throws Exception {
		List<Provider> providers = new ArrayList<Provider>();
		Provider provider1 = new Provider();
		provider1.setId(1);
		provider1.setName("provider1");
		provider1.setVmmUrl("http://1");
		providers.add(provider1);
		Provider provider2 = new Provider();
		provider2.setId(2);
		provider2.setName("provider2");
		provider2.setVmmUrl("http://2");
		providers.add(provider2);
		
		String json = Converter.getRootCollectionJSON(providers);
		
		System.out.println(json);
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);
		JSONObject jsonObject = (JSONObject) obj;
		
		jsonObject = (JSONObject) jsonObject.get("items");
		JSONArray providersArray = (JSONArray) jsonObject.get("provider");
		jsonObject = (JSONObject) providersArray.get(0);
		assertEquals("provider1", jsonObject.get("name"));
		jsonObject = (JSONObject) providersArray.get(1);
		assertEquals("provider2", jsonObject.get("name"));
	}
	
	@Test
	public void getCollectionXMLTest() throws Exception {
		List<Provider> providers = new ArrayList<Provider>();
		Provider provider1 = new Provider();
		provider1.setId(1);
		provider1.setName("provider1");
		provider1.setVmmUrl("http://1");
		providers.add(provider1);
		Provider provider2 = new Provider();
		provider2.setId(2);
		provider2.setName("provider2");
		provider2.setVmmUrl("http://2");
		providers.add(provider2);
		
		String xml = Converter.getRootCollectionXML(providers);
		
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:collection");
		xpath.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:items");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("0", element.getAttributeValue("offset"));
		assertEquals("2", element.getAttributeValue("total"));
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(5, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:provider");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(2, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("/1", element.getAttributeValue("href"));
		element = (Element) listxpathName.get(1);
		assertEquals("/2", element.getAttributeValue("href"));
		
		xpathName = XPath.newInstance("//bnf:name");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(2, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("provider1", element.getValue());
		element = (Element) listxpathName.get(1);
		assertEquals("provider2", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:vmm-url");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(2, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("http://1", element.getValue());
		element = (Element) listxpathName.get(1);
		assertEquals("http://2", element.getValue());
	}
	
	@Test
	public void getCollectionXMLNullTest() throws Exception {
		String xml = Converter.getRootCollectionXML(null);
		
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:collection");
		xpath.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:items");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(0, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:provider");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(0, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:name");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(0, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:vmm-url");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(0, listxpathName.size());
	}
}
