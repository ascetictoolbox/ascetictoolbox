package eu.ascetic.paas.applicationmanager.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * This Unit test verifies the correct behaviour of the POJO Root
 * @author David Garcia Perez - Atos
 */
public class RootTest {

	@Test
	public void testPojo() {
		Root root = new Root();
		root.setHref("/");
		root.setTimestamp("111");
		root.setVersion("0.1-SNAPSHOOT");
		List<Link> links = new ArrayList<Link>();
		root.setLinks(links);
		
		assertEquals("/", root.getHref());
		assertEquals("0.1-SNAPSHOOT", root.getVersion());
		assertEquals("111", root.getTimestamp());
		assertEquals(links, root.getLinks());
	}
	
	@Test
	public void addLinkTest() {
		Root root = new Root();
		
		assertEquals(null, root.getLinks());
		
		Link link = new Link();
		root.addLink(link);
		assertEquals(link, root.getLinks().get(0));
	}
}
