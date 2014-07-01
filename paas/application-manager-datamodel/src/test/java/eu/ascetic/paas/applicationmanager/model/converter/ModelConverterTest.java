package eu.ascetic.paas.applicationmanager.model.converter;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Items;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.Root;

/**
 * Verifies the right work of the class responsible of converting XML representations and viceversa for the Application Manager
 * @author David Garcia Perez - Atos
 */
public class ModelConverterTest {
	
	@Test
	public void objectRootToXMLNullTest() {
		String xml = ModelConverter.objectRootToXML(null);
		assertEquals(null, xml);
	}
	
	@Test
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
					+ "</items>"
					+ "<link href=\"/\" rel=\"self\" type=\"application/xml\"/>"
				+ "</collection>";
		
		Collection collection = ModelConverter.xmlCollectionToObject(collectionXML);
		assertEquals(2, collection.getItems().getApplications().size());
		assertEquals(102, collection.getItems().getApplications().get(1).getId());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals("application/xml", collection.getLinks().get(0).getType());
	}
	
	@Test
	public void objectApplicationToXMLNullTest() {
		String xml = ModelConverter.objectApplicationToXML(null);
		assertEquals(null, xml);
	}

	@Test
	public void objectApplicationToXMLTest() throws JDOMException, IOException {
		Application application = new Application();
		application.setHref("href");
		application.setId(1);
		
		Link link = new Link();
		link.setRel("self");
		link.setType("application/xml");
		link.setHref("href2");
		application.addLink(link);
		
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
		assertEquals("href", element.getAttributeValue("href"));

		XPath xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", APPLICATION_MANAGER_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
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
	public void xmlApplicationToObjectNullTest() {
		String applicationXML = "<something_else />";
		
		Application application = ModelConverter.xmlApplicationToObject(applicationXML);
		assertEquals(null, application);
	}
	
	@Test
	public void xmlCApplicationToObjectTest() {
		String applicationXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
								+ "<application xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/101\">"
									+ "<id>101</id>"
									+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
									+ "<link rel=\"self\" href=\"/101\" type=\"application/xml\" />"
								+ "</application>";
		
		Application application = ModelConverter.xmlApplicationToObject(applicationXML);
		assertEquals("/101", application.getHref());
		assertEquals(101, application.getId());
		assertEquals(2, application.getLinks().size());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals("/", application.getLinks().get(0).getHref());
		assertEquals("application/xml", application.getLinks().get(0).getType());
		assertEquals("self", application.getLinks().get(1).getRel());
		assertEquals("/101", application.getLinks().get(1).getHref());
		assertEquals("application/xml", application.getLinks().get(1).getType());
	}
	
}
