package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Application;

public class XMLBuilderTest {

	@Test
	public void addApplicationXMLInfoTest() {
		Application application = new Application();
		application.setId(1);
		
		application = XMLBuilder.addApplicationXMLInfo(application);
		
		assertEquals(1, application.getId());
		assertEquals("/applications/1", application.getHref());
		assertEquals(2, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/1", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
	}
	
	@Test
	public void getXMLApplicationTest() throws JAXBException {
		Application applicationBeforeXML = new Application();
		applicationBeforeXML.setId(1);
		String xml = XMLBuilder.getXML(applicationBeforeXML);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application application = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, application.getId());
		assertEquals("/applications/1", application.getHref());
		assertEquals(2, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/1", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
	}
}
