package eu.ascetic.iaas.slamanager.poc.negotiation.ws.test;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.iaas.slamanager.poc.negotiation.ws.NegotiationWsClient;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.SlaTranslator;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.SlaTranslatorImplNoOsgi;

public class ProviderNegotiationWsTest {

	private static NegotiationWsClient nc;
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
		nc = new NegotiationWsClient();
		SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
		nc.setSlaTranslator(slaTranslator);
	}

	//@Test
	public void testInitiateNegotiationWs() throws Exception {

		String slatXml = FileUtils.readFileToString(new File("src/test/resources/slats/ASCETiC-SlaTemplateIaaSRequest.xml"));

		SLASOITemplateParser slasoiTemplateParser = new SLASOITemplateParser();
		SLATemplate slat = slasoiTemplateParser.parseTemplate(slatXml);
		System.out.println("Sending initiateNegotiation SOAP request...");
		negId = nc.initiateNegotiation("http://localhost:" + serverPort + requestUrl, slat);
		System.out.println("Negotiation ID: " + negId);
	}

	// @Test
	public void testNegotiationWs() throws Exception {

		String slatXml = FileUtils.readFileToString(new File("src/test/resources/slats/contrail-basic-slat.xml"));

		SLASOITemplateParser parser = new SLASOITemplateParser();
		SLATemplate slat = parser.parseTemplate(slatXml);
		System.out.println("Sending negotiate SOAP request...");
		SLATemplate[] slats = nc.negotiate("http://localhost:" + serverPort + requestUrl, slat, negId);

		SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
		String xmlRetSlat = rend.renderSLATemplate(slats[0]);
		Assert.assertNotNull(xmlRetSlat);
	}

	@After
	public void tearDown() throws IOException {
	}

}
