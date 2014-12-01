/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.iaas.slamanager.poc.manager.negotiation.client.NegotiationWsClient;
import eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator.SlaTranslator;
import eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator.SlaTranslatorImplNoOsgi;

public class ProviderNegotiationWsTest {

	private static final String serverAddress = "10.4.0.15";

	private static final int serverPort = 8080;

	private static final String requestUrl = "/services/asceticNegotiation";

	private static final String filepath = "src/test/resources/slats/ASCETiC-SlaTemplateIaaSOffer.xml";

	private NegotiationWsClient negotiationClient;

	private static final String endpoint = "http://" + serverAddress + ":" + serverPort + requestUrl;

	@BeforeClass
	public static void configXMLunit() throws Exception {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setNormalizeWhitespace(true);
	}

	@Before
	public void setUp() throws Exception {
		negotiationClient = new NegotiationWsClient();
		SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
		negotiationClient.setSlaTranslator(slaTranslator);
	}

	//@Test
	public void testWorkflow() throws Exception {
		System.out.println("STARTING TEST WORKFLOW...");
		String negId = testInitiateNegotiationWs();
		String slat = testNegotiationWs(negId);
		SLA sla = testCreateAgreementWs(negId, slat);
		Assert.assertNotNull(sla);
		System.out.println("TEST FINISHED!");
	}

	private String testInitiateNegotiationWs() throws Exception {

		String slatXml = FileUtils.readFileToString(new File(filepath));

		SLASOITemplateParser slasoieTemplatParser = new SLASOITemplateParser();
		SLATemplate slat = slasoieTemplatParser.parseTemplate(slatXml);
		System.out.println("Sending initiateNegotiation SOAP request...");
		String negId = negotiationClient.initiateNegotiation(endpoint, slat);

		System.out.println("Negotiation ID: " + negId);
		Assert.assertNotNull(negId);
		return negId;
	}

	private String testNegotiationWs(String negotiationId) throws Exception {
		String slatXml = FileUtils.readFileToString(new File(filepath));
		SLASOITemplateParser parser = new SLASOITemplateParser();
		SLATemplate slat = parser.parseTemplate(slatXml);
		System.out.println("Sending negotiate SOAP request...");
		System.out.println("Negotiation ID: " + negotiationId);
		SLATemplate[] slats = negotiationClient.negotiate(endpoint, slat, negotiationId);

		SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
		String xmlRetSlat = rend.renderSLATemplate(slats[0]);
		System.out.println("SLA Template:");
		System.out.println(xmlRetSlat);
		Assert.assertNotNull(xmlRetSlat);
		return xmlRetSlat;
	}

	private SLA testCreateAgreementWs(String negId, String slatXml) throws Exception {
		SLASOITemplateParser parser = new SLASOITemplateParser();
		SLATemplate slat = parser.parseTemplate(slatXml);
		System.out.println("Sending create agreement SOAP request...");
		SLA sla = negotiationClient.createAgreement("http://10.4.0.15:" + serverPort + requestUrl, slat, negId);
		System.out.println("SLA:");
		System.out.println(sla);
		Assert.assertNotNull(sla);
		return sla;
	}

	@After
	public void tearDown() throws IOException {
	}

}
