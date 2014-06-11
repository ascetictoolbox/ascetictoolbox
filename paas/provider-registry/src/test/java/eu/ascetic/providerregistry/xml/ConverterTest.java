package eu.ascetic.providerregistry.xml;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;
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

import eu.ascetic.providerregistry.model.Provider;

/**
 * Test the correct work of the different static methods that convert Java objects to XML
 * @author David Garcia Perez - Atos
 */
public class ConverterTest {

	@Test
	public void getProviderObjectTest() {
		String providerXML = "<provider xmlns=\"" + PROVIDER_REGISTRY_NAMESPACE + "\">" +
								"<id>111</id>" +
								"<name>Name</name>" +
								"<endpoint>http...</endpoint>" + 
							  "</provider>";
		
		Provider provider = Converter.getProviderObject(providerXML);
		
		assertEquals(111l, provider.getId());
		assertEquals("Name", provider.getName());
		assertEquals("http...", provider.getEndpoint());
		
		// If the parsing it is not possible, it should return a null value
		providerXML = "<xml/>";
		provider = Converter.getProviderObject(providerXML);
		assertEquals(null, provider);
	}
	
	@Test
	public void getProviderXML() throws JDOMException, IOException {
		Provider provider = new Provider();
		provider.setId(11);
		provider.setName("Nome");
		provider.setEndpoint("Punto final");
		
		String xml = Converter.getProviderXML(provider);
			
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document xmldoc = builder.build(new StringReader(xml));
		XPath xpath = XPath.newInstance("//bnf:provider");
		xpath.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpath = xpath.selectNodes(xmldoc);
		assertEquals(1, listxpath.size());
		
		XPath xpathName = XPath.newInstance("//bnf:id");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		List listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		Element element = (Element) listxpathName.get(0);
		assertEquals("11", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:name");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("Nome", element.getValue());
		
		xpathName = XPath.newInstance("//bnf:endpoint");
		xpathName.addNamespace("bnf", PROVIDER_REGISTRY_NAMESPACE);
		listxpathName = xpathName.selectNodes(xmldoc);
		assertEquals(1, listxpathName.size());
		element = (Element) listxpathName.get(0);
		assertEquals("Punto final", element.getValue());
	}
}
