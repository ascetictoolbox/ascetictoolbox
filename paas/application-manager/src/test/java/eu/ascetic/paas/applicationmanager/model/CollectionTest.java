package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

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
		String collectionXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<collection xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/\">"
					+ "<items offset=\"0\" total=\"2\">"
						+ "<application href=\"/101\">"
							+ "<id>101</id>"
							+ "<state>STATE1</state>"
							+ "<deployment-plan-id>d1</deployment-plan-id>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/101\" type=\"application/xml\" />"
						+ "</application>"
						+ "<application href=\"/102\">"
							+ "<id>102</id>"
							+ "<state>STATE2</state>"
							+ "<deployment-plan-id>d2</deployment-plan-id>"
							+ "<link rel=\"parent\" href=\"/\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/102\" type=\"application/xml\" />"
						+ "</application>"
					+ "</items>"
					+ "<link href=\"/\" rel=\"self\" type=\"application/xml\"/>"
				+ "</collection>";

		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(collectionXML));

		assertEquals(2, collection.getItems().getApplications().size());
		//assertEquals(2, collection.getItems().getProviders().get(0).getLinks().size());
		assertEquals(102, collection.getItems().getApplications().get(1).getId());
	}
}