package eu.ascetic.iaas.slamanager.poc.negotiation.ws.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.iaas.slamanager.poc.negotiation.ws.NegotiationClient;

public class ProviderNegotiationWsTest {

	private static final String serverAddress = "localhost";
	private static final int serverPort = 8080;
	private static final String requestUrl = "/services/asceticNegotiation?wsdl";
	private String negId = null;

	@BeforeClass
	public static void configXMLunit() throws Exception {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setNormalizeWhitespace(true);
	}

	@Before
	public void setUp() throws Exception {
	}

	//@Test
	public void testInitiateNegotiationWs() throws Exception {

		String slatXml = FileUtils.readFileToString(new File("src/test/resources/slats/ASCETiC-SlaTemplateIaaSRequest.xml"));

		SLASOITemplateParser slasoiTemplateParser = new SLASOITemplateParser();
		SLATemplate slat = slasoiTemplateParser.parseTemplate(slatXml);
		System.out.println("Sending initiateNegotiation SOAP request...");
		String endpoint = "http://" + serverAddress + ":" + serverPort + requestUrl;

		NegotiationClient nc = new NegotiationClient(endpoint);
		negId = nc.initiateNegotiation(slat);

		System.out.println("Negotiation ID: " + negId);
	}

	//@Test
	public void testNegotiationWs() throws Exception {

		String slatXml = FileUtils.readFileToString(new File("src/test/resources/slats/ASCETiC-SlaTemplateIaaSRequest.xml"));

		SLASOITemplateParser slasoiTemplateParser = new SLASOITemplateParser();
		SLATemplate slat = slasoiTemplateParser.parseTemplate(slatXml);
		System.out.println("Sending initiateNegotiation SOAP request...");
		String endpoint = "http://" + serverAddress + ":" + serverPort + requestUrl;

		NegotiationClient nc = new NegotiationClient(endpoint);
		SLATemplate[] slaTemplates = nc.negotiate(negId, slat);

		for (SLATemplate sla : slaTemplates) {
			System.out.println("SLa returned:");
			System.out.println(sla);
		}
	}

	@After
	public void tearDown() throws IOException {
	}

}
