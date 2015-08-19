package eu.ascetic.paas.applicationmanager.model.converter;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.Items;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.Root;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Verifies the right work of the class responsible of converting XML representations and viceversa for the Application Manager
 * 
 */
public class ModelConverterTest {
	
	@Test
	public void objectRootToXMLNullTest() {
		String xml = ModelConverter.objectRootToXML(null);
		assertEquals(null, xml);
	}
	
	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectRootToXMLTest() throws JDOMException, IOException {
		Root root = new Root();
		root.setHref("/");
		root.setTimestamp("111");
		root.setVersion("0.1-SNAPSHOOT");
		
		Link link = new Link();
		link.setRel("applications");
		link.setType("application/xml");
		link.setHref("/applications");
		root.addLink(link);
		
		String xml = ModelConverter.objectRootToXML(root);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:root");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:version");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("0.1-SNAPSHOOT", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:timestamp");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("111", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("applications", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications", element.getAttributeValue("href"));
	}
	
	@Test
	public void objectCollectionToXMLNullTest() {
		String xml = ModelConverter.objectCollectionToXML(null);
		assertEquals(null, xml);
	}

	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectCollectionToXMLTest() throws JDOMException, IOException {
		Application application = new Application();
		application.setHref("href");
		application.setId(1);
		
		Items items = new Items();
		items.setOffset(1);
		items.setTotal(2);
		items.addApplication(application);
		
		Collection collection = new Collection();
		collection.setHref("href1");
		collection.setItems(items);
		
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("href2");
		collection.addLink(link);
		
		String xml = ModelConverter.objectCollectionToXML(collection);
		
		System.out.println("XML: " + xml);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:collection");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("href1", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:items");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("1", element.getAttributeValue("offset"));
		assertEquals("2", element.getAttributeValue("total"));
		
		xpathName = XPath.newInstance("//bnf:application");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("href", element.getAttributeValue("href"));
		
		xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("1", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("href2", element.getAttributeValue("href"));
	}
	
	@Test
	public void xmlCollectionToObjectNullTest() {
		String collectionXML = "<something_else />";
		
		Collection collection = ModelConverter.xmlCollectionToObject(collectionXML);
		assertEquals(null, collection);
	}
	
	@Test
	public void xmlCollectionToObjectTest() {
		String collectionXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<collection xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/\">"
					+ "<items offset=\"0\" total=\"2\">"
						+ "<application href=\"/101\">"
							+ "<id>101</id>"
							+ "<state>STATE1</state>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/101\" type=\"application/xml\" />"
						+ "</application>"
						+ "<application href=\"/102\">"
							+ "<id>102</id>"
							+ "<state>STATE2</state>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/102\" type=\"application/xml\" />"
						+ "</application>"
						+ "<deployment href=\"yyy\">"
						+ "</deployment>"
						+ "<vm href=\"xxx\">"
						+ "</vm>"
						+ "<agreement href=\"zzz\" />"
					+ "</items>"
					+ "<link href=\"/\" rel=\"self\" type=\"application/xml\"/>"
				+ "</collection>";
		
		Collection collection = ModelConverter.xmlCollectionToObject(collectionXML);
		assertEquals(2, collection.getItems().getApplications().size());
		assertEquals(102, collection.getItems().getApplications().get(1).getId());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals("application/xml", collection.getLinks().get(0).getType());
		assertEquals(1, collection.getItems().getDeployments().size());
		assertEquals("yyy", collection.getItems().getDeployments().get(0).getHref());
		assertEquals(1, collection.getItems().getVms().size());
		assertEquals("xxx", collection.getItems().getVms().get(0).getHref());
		assertEquals(1, collection.getItems().getAgreements().size());
		assertEquals("zzz", collection.getItems().getAgreements().get(0).getHref());
	}
	
	@Test
	public void objectApplicationToXMLNullTest() {
		String xml = ModelConverter.objectApplicationToXML(null);
		assertEquals(null, xml);
	}
	
	@Test
	public void objectApplicationToJSONNullTest() {
		String json = ModelConverter.objectApplicationToJSON(null);
		assertEquals(null, json);
	}

	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectApplicationToXMLTest() throws JDOMException, IOException {
		Application application = new Application();
		application.setHref("/applications/101");
		application.setId(101);
		application.setName("SaaS Application");
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setHref("/applications/101/deployments/1");
		deployment1.setPrice("222");
		deployment1.setStatus("DELETED");
		Link linkSelf = new Link();
		linkSelf.setRel("self");
		linkSelf.setType("application/xml");
		linkSelf.setHref("/applications/101/deployments/1");
		deployment1.addLink(linkSelf);
		Link linkApplication = new Link();
		linkApplication.setRel("application");
		linkApplication.setType("application/xml");
		linkApplication.setHref("/applications/101");
		deployment1.addLink(linkApplication);
	
		application.addDeployment(deployment1);
		
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101");
		application.addLink(link);
		Link linkParent = new Link();
		linkParent.setRel("parent");
		linkParent.setType("application/xml");
		linkParent.setHref("/applications");
		application.addLink(linkParent);
		
		VM vm1 = new VM();
		vm1.setId(33);
		vm1.setHref("/applications/101/deployments/1/vms/33");
		vm1.setOvfId("vm ovf id");
		vm1.setProviderId("IaaS vm Id");
		vm1.setProviderVmId("IaaS provider Id");
		vm1.setStatus("IaaS status of the VM");
		vm1.setIp("172.0.0.1");
		Image image1 = new Image();
		image1.setHref("hrefImage1");
		image1.setId(111);
		image1.setOvfId("333");
		image1.setProviderImageId("444");
		image1.setOvfHref("dfs//");
		image1.setProviderId("provider-id");
		vm1.addImage(image1);
		vm1.setSlaAgreement("sla agreement reference");
		Link linkVm1Self = new Link();
		linkVm1Self.setRel("self");
		linkVm1Self.setType("application/xml");
		linkVm1Self.setHref("/applications/101/deployments/1/vms/33");
		vm1.addLink(linkVm1Self);
		Link linkVm1Parent = new Link();
		linkVm1Parent.setRel("deployment");
		linkVm1Parent.setType("application/xml");
		linkVm1Parent.setHref("/applications/101/deployments/1");
		vm1.addLink(linkVm1Parent);
		deployment1.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(44);
		vm2.setHref("/applications/101/deployments/1/vms/44");
		vm2.setOvfId("vm ovf id");
		vm2.setProviderId("IaaS vm Id");
		vm2.setProviderVmId("IaaS provider Id");
		vm2.setStatus("IaaS status of the VM");
		vm2.setIp("172.0.0.2");
		vm2.setSlaAgreement("sla agreement reference");
		Image image2 = new Image();
		image2.setHref("hrefImage2");
		image2.setId(111);
		image2.setOvfId("333");
		image2.setProviderImageId("444");
		vm2.addImage(image2);
		Link linkVm2Self = new Link();
		linkVm2Self.setRel("self");
		linkVm2Self.setType("application/xml");
		linkVm2Self.setHref("/applications/101/deployments/1/vms/44");
		vm2.addLink(linkVm2Self);
		Link linkVm2Parent = new Link();
		linkVm2Parent.setRel("deployment");
		linkVm2Parent.setType("application/xml");
		linkVm2Parent.setHref("/applications/101/deployments/1");
		vm2.addLink(linkVm2Parent);
		deployment1.addVM(vm2);
		
		String xml = ModelConverter.objectApplicationToXML(application);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:application");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/applications/101", element.getAttributeValue("href"));

		XPath xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(6, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("101", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:deployment");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("/applications/101/deployments/1", element.getAttributeValue("href"));
		
		xpathName = XPath.newInstance("//bnf:image");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(2, listxpathName.size());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(8, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications/101/deployments/1/vms/33", element.getAttributeValue("href"));
	}
	
	@Test
	public void objectApplicationToJSONTest() throws JDOMException, IOException, ParseException {
		Application application = new Application();
		application.setHref("/applications/101");
		application.setId(101);
		application.setName("SaaS Application");
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setHref("/applications/101/deployments/1");
		deployment1.setPrice("222");
		deployment1.setStatus("DELETED");
		Link linkSelf = new Link();
		linkSelf.setRel("self");
		linkSelf.setType("application/xml");
		linkSelf.setHref("/applications/101/deployments/1");
		deployment1.addLink(linkSelf);
		Link linkApplication = new Link();
		linkApplication.setRel("application");
		linkApplication.setType("application/xml");
		linkApplication.setHref("/applications/101");
		deployment1.addLink(linkApplication);
	
		application.addDeployment(deployment1);
		
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101");
		application.addLink(link);
		Link linkParent = new Link();
		linkParent.setRel("parent");
		linkParent.setType("application/xml");
		linkParent.setHref("/applications");
		application.addLink(linkParent);
		
		VM vm1 = new VM();
		vm1.setId(33);
		vm1.setHref("/applications/101/deployments/1/vms/33");
		vm1.setOvfId("vm ovf id");
		vm1.setProviderId("IaaS vm Id");
		vm1.setProviderVmId("IaaS provider Id");
		vm1.setStatus("IaaS status of the VM");
		vm1.setIp("172.0.0.1");
		Image image1 = new Image();
		image1.setHref("hrefImage1");
		image1.setId(111);
		image1.setOvfId("333");
		image1.setProviderImageId("444");
		image1.setOvfHref("dfs//");
		image1.setProviderId("provider-id");
		vm1.addImage(image1);
		vm1.setSlaAgreement("sla agreement reference");
		Link linkVm1Self = new Link();
		linkVm1Self.setRel("self");
		linkVm1Self.setType("application/xml");
		linkVm1Self.setHref("/applications/101/deployments/1/vms/33");
		vm1.addLink(linkVm1Self);
		Link linkVm1Parent = new Link();
		linkVm1Parent.setRel("deployment");
		linkVm1Parent.setType("application/xml");
		linkVm1Parent.setHref("/applications/101/deployments/1");
		vm1.addLink(linkVm1Parent);
		deployment1.addVM(vm1);
		
		VM vm2 = new VM();
		vm2.setId(44);
		vm2.setHref("/applications/101/deployments/1/vms/44");
		vm2.setOvfId("vm ovf id");
		vm2.setProviderId("IaaS vm Id");
		vm2.setProviderVmId("IaaS provider Id");
		vm2.setStatus("IaaS status of the VM");
		vm2.setIp("172.0.0.2");
		vm2.setSlaAgreement("sla agreement reference");
		Image image2 = new Image();
		image2.setHref("hrefImage2");
		image2.setId(111);
		image2.setOvfId("333");
		image2.setProviderImageId("444");
		vm2.addImage(image2);
		Link linkVm2Self = new Link();
		linkVm2Self.setRel("self");
		linkVm2Self.setType("application/xml");
		linkVm2Self.setHref("/applications/101/deployments/1/vms/44");
		vm2.addLink(linkVm2Self);
		Link linkVm2Parent = new Link();
		linkVm2Parent.setRel("deployment");
		linkVm2Parent.setType("application/xml");
		linkVm2Parent.setHref("/applications/101/deployments/1");
		vm2.addLink(linkVm2Parent);
		deployment1.addVM(vm2);
		
		String json = ModelConverter.objectApplicationToJSON(application);
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);
		JSONObject jsonObject = (JSONObject) obj;
		//jsonObject = (JSONObject) jsonObject.get("application");
		
		assertEquals("/applications/101", (String) jsonObject.get("href"));
		assertEquals(101l, jsonObject.get("id"));
		assertEquals("SaaS Application", (String) jsonObject.get("name"));
		
		jsonObject = (JSONObject) jsonObject.get("deployments");
		JSONArray deployments = (JSONArray) jsonObject.get("deployment");
		jsonObject = (JSONObject) deployments.get(0);
		assertEquals("/applications/101/deployments/1", (String) jsonObject.get("href"));
	}
	

	@Test
	public void xmlApplicationToObjectNullTest() {
		String applicationXML = "<something_else />";
		
		Application application = ModelConverter.xmlApplicationToObject(applicationXML);
		assertEquals(null, application);
	}
	
	@Test
	public void jsonApplicationToObjectTest() {
		String applicationJSON = "{" +
									//"\"application\": {" +
										"\"href\": \"/applications/101\"," +
										"\"id\": 101," +
										"\"name\": \"SaaS Application\"," +
										"\"deployments\": {" +
											"\"deployment\": [ {" +
												"\"href\": \"/applications/101/deployments/1\"," +
												"\"id\": 1," +
												"\"status\": \"DELETED\"," +
												"\"price\": \"222\"" +
											"} ]" + 
            							"}," +
      									"\"link\": [ {" +
      										"\"rel\": \"self\"," +
      										"\"href\": \"/applications/101\"," +
      										"\"type\": \"application/xml\"" +
      									"}, {" +
      										"\"rel\": \"parent\","+
      										"\"href\": \"/applications\"," +
      										"\"type\": \"application/xml\"" +
      									"} ]" +
									//"}" +
								"}";	
		
		Application application = ModelConverter.jsonApplicationToObject(applicationJSON);
		assertEquals("/applications/101", application.getHref());
		assertEquals(101, application.getId());
		assertEquals("SaaS Application", application.getName());
		assertEquals(1, application.getDeployments().size());
		assertEquals("/applications/101/deployments/1", application.getDeployments().get(0).getHref());
		assertEquals(1, application.getDeployments().get(0).getId());
		assertEquals(2, application.getLinks().size());
		assertEquals("parent", application.getLinks().get(1).getRel());
		assertEquals("/applications", application.getLinks().get(1).getHref());
		assertEquals("application/xml", application.getLinks().get(1).getType());
		assertEquals("self", application.getLinks().get(0).getRel());
		assertEquals("/applications/101", application.getLinks().get(0).getHref());
		assertEquals("application/xml", application.getLinks().get(0).getType());
	}
	
	@Test
	public void xmlApplicationToObjectTest() {
		String applicationXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
								+ "<application xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101\">"
									+ "<id>101</id>"
									+ "<name>Name</name>"
									+ "<deployments>"
										+ "<deployment href=\"/applications/101/deployments/1\">"
											+ "<id>1</id>"
										+ "</deployment>"
										+ "<deployment href=\"/applications/101/deployments/2\">"
											+ "<id>2</id>"
										+ "</deployment>"
									+ "</deployments>"
									+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
									+ "<link rel=\"self\" href=\"/101\" type=\"application/xml\" />"
								+ "</application>";
		
		Application application = ModelConverter.xmlApplicationToObject(applicationXML);
		assertEquals("/applications/101", application.getHref());
		assertEquals(101, application.getId());
		assertEquals("Name", application.getName());
		assertEquals(2, application.getDeployments().size());
		assertEquals("/applications/101/deployments/1", application.getDeployments().get(0).getHref());
		assertEquals(1, application.getDeployments().get(0).getId());
		assertEquals("/applications/101/deployments/2", application.getDeployments().get(1).getHref());
		assertEquals(2, application.getDeployments().get(1).getId());
		assertEquals(2, application.getLinks().size());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals("/", application.getLinks().get(0).getHref());
		assertEquals("application/xml", application.getLinks().get(0).getType());
		assertEquals("self", application.getLinks().get(1).getRel());
		assertEquals("/101", application.getLinks().get(1).getHref());
		assertEquals("application/xml", application.getLinks().get(1).getType());
	}
	
	@Test
	public void xmlDeploymentToObjectTest() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					 + "<deployment xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22\">"
					 	+ "<id>101</id>"
					 	+ "<status>RUNNING</status>"
					 	+ "<price>222</price>"
					 	+ "<vms>"
					 		+ "<vm href=\"/applications/101/deployments/22/vms/33\">"
					 			+ "<id>33</id>"
						 		+ "<ovf-id>101</ovf-id>"
						 		+ "<provider-vm-id>vm-id</provider-vm-id>"
						 		+ "<provider-id>222</provider-id>"
						 		+ "<sla-aggrement-id>sla</sla-aggrement-id>"
						 		+ "<ip>127.0.0.1</ip>"
						 		+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
						 		+ "<link rel=\"self\" href=\"/applications/101/deployments/22/vms/33\" type=\"application/xml\" />"
						 	+ "</vm>"
					 		+ "<vm href=\"/applications/101/deployments/22/vms/34\">"
					 			+ "<id>34</id>"
					 			+ "<ovf-id>101</ovf-id>"
					 			+ "<provider-vm-id>vm-id</provider-vm-id>"
					 			+ "<provider-id>222</provider-id>"
					 			+ "<sla-aggrement-id>sla</sla-aggrement-id>"
					 			+ "<ip>127.0.0.1</ip>"
					 			+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
					 			+ "<link rel=\"self\" href=\"/applications/101/deployments/22/vms/33\" type=\"application/xml\" />"
					 		+ "</vm>"
					 	+ "</vms>"
					 	+ "<link rel=\"application\" href=\"/applications/101\" type=\"application/xml\" />"
						+ "<link rel=\"self\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
					 + "</deployment>";
		
		Deployment deployment = ModelConverter.xmlDeploymentToObject(xml);
		assertEquals("/applications/101/deployments/22", deployment.getHref());
		assertEquals(101, deployment.getId());
		assertEquals("RUNNING", deployment.getStatus());
		assertEquals(2, deployment.getLinks().size());
		assertEquals("application", deployment.getLinks().get(0).getRel());
		assertEquals("/applications/101", deployment.getLinks().get(0).getHref());
		assertEquals("application/xml", deployment.getLinks().get(0).getType());
		assertEquals("self", deployment.getLinks().get(1).getRel());
		assertEquals("/applications/101/deployments/22", deployment.getLinks().get(1).getHref());
		assertEquals("application/xml", deployment.getLinks().get(1).getType());
		assertEquals(2, deployment.getVms().size());
		assertEquals(33, deployment.getVms().get(0).getId());
		assertEquals(34, deployment.getVms().get(1).getId());
	}
	
	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectDeploymentToXMLTest() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setHref("/applications/101/deployments/2");
		deployment.setId(2);
		deployment.setPrice("222");
		deployment.setOvf("<ovf>assdasdf</ovf>");
		deployment.setStatus("RUNNING");
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101/deployments/2");
		deployment.addLink(link);
		
		String xml = ModelConverter.objectDeploymentToXML(deployment);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:deployment");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/applications/101/deployments/2", element.getAttributeValue("href"));

		XPath xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("2", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:status");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("RUNNING", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications/101/deployments/2", element.getAttributeValue("href"));
	}
	
	@Test
	public void xmlEnergyMeasurementToObjectTest() {
		String xml = "<energy-measurement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"href\">" +
						"<value>22.0</value>" +
						"<description>description</description>" +
						"<link rel=\"self\" href=\"/applications/101/deployments/2/vms/111/events/111/energy-measurement\" type=\"application/xml\"/>" +
					  "</energy-measurement>";
		
		EnergyMeasurement energyMeasurement = ModelConverter.xmlEnergyMeasurementToObject(xml);
		assertEquals("href", energyMeasurement.getHref());
		assertEquals(22.0d, energyMeasurement.getValue(), 0.0001);
		assertEquals("description", energyMeasurement.getDescription());
		assertEquals(1, energyMeasurement.getLinks().size());
		assertEquals("self", energyMeasurement.getLinks().get(0).getRel());
		assertEquals("/applications/101/deployments/2/vms/111/events/111/energy-measurement", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("application/xml", energyMeasurement.getLinks().get(0).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectEnergyMeasurementToXMLTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement(); 
		energyMeasurement.setHref("href");
		energyMeasurement.setValue(22d);
		energyMeasurement.setDescription("description");
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101/deployments/2/vms/111/events/111/energy-measurement");
		energyMeasurement.addLink(link);
		
		String xml = ModelConverter.objectEnergyMeasurementToXML(energyMeasurement);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:energy-measurement");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("href", element.getAttributeValue("href"));

		XPath xpathName = XPath.newInstance("//bnf:value");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("22.0", element.getValue());

		xpathName = XPath.newInstance("//bnf:description");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("description", element.getValue());

		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications/101/deployments/2/vms/111/events/111/energy-measurement", element.getAttributeValue("href"));
	}
	
	@Test
	public void xmlVMToObjectTest() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					 + "<vm xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/vms/33\">"
					 	+ "<id>33</id>"
					 	+ "<ovf-id>101</ovf-id>"
					 	+ "<provider-vm-id>vm-id</provider-vm-id>"
					 	+ "<provider-id>222</provider-id>"
					 	+ "<sla-agreement>sla</sla-agreement>"
					 	+ "<status>XXX</status>"
					 	+ "<ip>127.0.0.1</ip>"
	                    + "<images>"
                        	+ "<image href=\"hrefImage2\">"
                            	+ "<id>111</id>"
                            	+ "<ovf-id>333</ovf-id>"
                            	+ "<provider-image-id>444</provider-image-id>"
                            + "</image>"
                        + "</images>"
					 	+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
						+ "<link rel=\"self\" href=\"/applications/101/deployments/22/vms/33\" type=\"application/xml\" />"
					 + "</vm>";
		
		VM vm = ModelConverter.xmlVMToObject(xml);
		assertEquals("/applications/101/deployments/22/vms/33", vm.getHref());
		assertEquals(33, vm.getId());
		assertEquals("101", vm.getOvfId());
		assertEquals("vm-id", vm.getProviderVmId());
		assertEquals("222", vm.getProviderId());
		assertEquals("sla", vm.getSlaAgreement());
		assertEquals("XXX", vm.getStatus());
		assertEquals(1, vm.getImages().size());
		assertEquals("hrefImage2", vm.getImages().get(0).getHref());
		assertEquals(111, vm.getImages().get(0).getId());
		assertEquals("333", vm.getImages().get(0).getOvfId());
		assertEquals("444", vm.getImages().get(0).getProviderImageId());
		assertEquals(2, vm.getLinks().size());
		assertEquals("deployment", vm.getLinks().get(0).getRel());
		assertEquals("/applications/101/deployments/22", vm.getLinks().get(0).getHref());
		assertEquals("application/xml", vm.getLinks().get(0).getType());
		assertEquals("self", vm.getLinks().get(1).getRel());
		assertEquals("/applications/101/deployments/22/vms/33", vm.getLinks().get(1).getHref());
		assertEquals("application/xml", vm.getLinks().get(1).getType());
	}
	
	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectVMToXMLTest() throws Exception {
		VM vm = new VM();
		vm.setId(33);
		vm.setHref("href");
		vm.setOvfId("ovfId");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setStatus("XXX");
		vm.setIp("172.0.0.1");
		vm.setSlaAgreement("slaAggrementId");
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101/deployments/2/vms/111");
		vm.addLink(link);
		
		String xml = ModelConverter.objectVMToXML(vm);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:vm");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("href", element.getAttributeValue("href"));

		XPath xpathName = XPath.newInstance("//bnf:ovf-id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("ovfId", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:provider-id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("provider-id", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:provider-vm-id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("provider-vm-id", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:status");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("XXX", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:ip");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("172.0.0.1", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("33", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:sla-agreement");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("slaAggrementId", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications/101/deployments/2/vms/111", element.getAttributeValue("href"));
	}
	
	@Test
	public void xmlAgreementToObjectTest() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					 + "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
					 	+ "<id>33</id>"
					 	+ "<price>222</price>"
					 	+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
					 	+ "<provider-id>provider-id</provider-id>"
					 	+ "<accepted>false</accepted>"
					 	+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
						+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
					 + "</agreement>";
		
		Agreement agreement = ModelConverter.xmlAgreementToObject(xml);
		assertEquals("/applications/101/deployments/22/agreements/44", agreement.getHref());
		assertEquals(33, agreement.getId());
		assertEquals("222", agreement.getPrice());
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals("provider-id", agreement.getProviderId());
		assertFalse(agreement.isAccepted());
	}
	
	@Test
	@SuppressWarnings(value = { "rawtypes" }) 
	public void objectAgreementToXMLTest() throws Exception {
		Agreement agreement = new Agreement();
		agreement.setId(44);
		agreement.setHref("/applications/101/deployments/22/agreements/44");
		agreement.setPrice("333");
		agreement.setSlaAgreementId("sla-id");
		agreement.setProviderId("provider-id");
		agreement.setValidUntil(new Timestamp(System.currentTimeMillis()));
		
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("/applications/101/deployments/22/agreements/44");
		agreement.addLink(link);
		
		String xml = ModelConverter.objectAgreementToXML(agreement);
		
		//We now verify the XML has the right format... a bit a pain in the a**...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:agreement");
		xpath.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		Element element = (Element) listxpath.get(0);
		assertEquals("/applications/101/deployments/22/agreements/44", element.getAttributeValue("href"));
		
		XPath xpathName = XPath.newInstance("//bnf:price");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("333", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:sla-agreement-id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("sla-id", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:provider-id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("provider-id", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("44", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:link");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("self", element.getAttributeValue("rel"));
		assertEquals("application/xml", element.getAttributeValue("type"));
		assertEquals("/applications/101/deployments/22/agreements/44", element.getAttributeValue("href"));
	}
	
	@Test
	public void applicationManagerMessageToJSON() throws ParseException {
		ApplicationManagerMessage amMessage = new ApplicationManagerMessage();
		amMessage.setApplicationId("app-id");
		amMessage.setDeploymentId("deplo-id");
		amMessage.setStatus("status");
		
		eu.ascetic.paas.applicationmanager.amqp.model.VM vm1 = new eu.ascetic.paas.applicationmanager.amqp.model.VM();
		vm1.setIaasMonitoringVmId("aaa");
		vm1.setIaasVmId("bbb");
		vm1.setOvfId("ccc");
		vm1.setStatus("ddd");
		vm1.setVmId("eee");
		
		eu.ascetic.paas.applicationmanager.amqp.model.VM vm2 = new eu.ascetic.paas.applicationmanager.amqp.model.VM();
		vm2.setIaasMonitoringVmId("zzz");
		vm2.setIaasVmId("yyy");
		vm2.setOvfId("xxx");
		vm2.setStatus("ttt");
		vm2.setVmId("www");
		
		amMessage.addVM(vm1);
		amMessage.addVM(vm2);
		
		String output = ModelConverter.applicationManagerMessageToJSON(amMessage);
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(output);
		JSONObject jsonObject = (JSONObject) obj;
		assertEquals("app-id", (String) jsonObject.get("applicationId"));
		assertEquals("deplo-id", (String) jsonObject.get("deploymentId"));
		assertEquals("status", (String) jsonObject.get("status"));
		
		JSONArray vms = (JSONArray) jsonObject.get("vms");
		jsonObject = (JSONObject) vms.get(0);
		assertEquals("eee", (String) jsonObject.get("vmId"));
		assertEquals("bbb", (String) jsonObject.get("iaasVmId"));
		assertEquals("aaa", (String) jsonObject.get("iaasMonitoringVmId"));
		assertEquals("ccc", (String) jsonObject.get("ovfId"));
		assertEquals("ddd", (String) jsonObject.get("status"));
		
		jsonObject = (JSONObject) vms.get(1);
		assertEquals("www", (String) jsonObject.get("vmId"));
		assertEquals("yyy", (String) jsonObject.get("iaasVmId"));
		assertEquals("zzz", (String) jsonObject.get("iaasMonitoringVmId"));
		assertEquals("xxx", (String) jsonObject.get("ovfId"));
		assertEquals("ttt", (String) jsonObject.get("status"));
	}
	
	@Test
	public void fromJSONtoApplicationManagerMessage() {
		String json = "{" +
				 "\"applicationId\" : \"app-id\"," +
				 "\"deploymentId\" : \"deplo-id\"," + 
				 "\"status\" : \"status\"," +
				 "\"vms\" : [ {" +
				     "\"vmId\" : \"eee\"," +
				     "\"iaasVmId\" : \"bbb\"," +
				     "\"iaasMonitoringVmId\" : \"aaa\"," +
				     "\"ovfId\" : \"ccc\"," +
				     "\"status\" : \"ddd\"" +
				 "}, {" +
				     "\"vmId\" : \"www\"," +
				     "\"iaasVmId\" : \"yyy\"," +
				     "\"iaasMonitoringVmId\" : \"zzz\"," +
				     "\"ovfId\" : \"xxx\"," +
				     "\"status\" : \"ttt\"" +
				 "} ]" +
			  "}";
		
		ApplicationManagerMessage amMessage = ModelConverter.jsonToApplicationManagerMessage(json);
		
		assertEquals("app-id", amMessage.getApplicationId());
		assertEquals("deplo-id", amMessage.getDeploymentId());
		assertEquals("status", amMessage.getStatus());

		assertEquals("eee", amMessage.getVms().get(0).getVmId());
		assertEquals("bbb", amMessage.getVms().get(0).getIaasVmId());
		assertEquals("aaa", amMessage.getVms().get(0).getIaasMonitoringVmId());
		assertEquals("ccc", amMessage.getVms().get(0).getOvfId());
		assertEquals("ddd", amMessage.getVms().get(0).getStatus());
		
		assertEquals("www", amMessage.getVms().get(1).getVmId());
		assertEquals("yyy", amMessage.getVms().get(1).getIaasVmId());
		assertEquals("zzz", amMessage.getVms().get(1).getIaasMonitoringVmId());
		assertEquals("xxx", amMessage.getVms().get(1).getOvfId());
		assertEquals("ttt", amMessage.getVms().get(1).getStatus());
	}
}
