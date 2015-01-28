package eu.ascetic.paas.applicationmanager.slam;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
//import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImplNoOsgi;
import eu.ascetic.utils.ovf.api.OvfDefinition;

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
 */
public class NegotiationWsClientIT {
	private static Logger logger = Logger.getLogger(NegotiationWsClientIT.class);

	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	
	private String slatFile = "ascetic-basic-slat.xml";
	private String slatXml;
	
	private static final String serverAddress = "localhost";//"10.4.0.15";

	private static final int serverPort = 8080;

	private static final String requestUrl = "/services/asceticNegotiation?wsdl";

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
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		file = new File(this.getClass().getResource( "/" + slatFile ).toURI());		
		slatXml = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		negotiationClient = new NegotiationWsClient();
		SlaTranslator slaTranslator = new SlaTranslatorImplNoOsgi();
		negotiationClient.setSlaTranslator(slaTranslator);
	}

	@Test
	public void testWorkflow() throws Exception {
		System.out.println("STARTING TEST WORKFLOW...");
		String negId = testInitiateNegotiationWs();
		
		logger.debug("  Negotiation ID: " + negId);
		
		
		String slat = testNegotiationWs(negId);
		
		SLA sla = testCreateAgreementWs(negId, slat);
		assertNotNull(sla);
		System.out.println("TEST FINISHED!");
	}

	private String testInitiateNegotiationWs() throws Exception {
		
//		SLASOITemplateParser slasoieTemplatParser = new SLASOITemplateParser();
//		SLATemplate slat = slasoieTemplatParser.parseTemplate(slatXml);
		
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager/applications/threeTierWebApp/deployments/31/ovf");

		System.out.println("Sending initiateNegotiation SOAP request...");
		String negId = negotiationClient.initiateNegotiation(endpoint, slaTemplate);

		logger.debug("  Negotiation ID: " + negId);
		assertNotNull(negId);
		return negId;
	}

	private String testNegotiationWs(String negotiationId) throws Exception {
		
//		SLASOITemplateParser slasoieTemplatParser = new SLASOITemplateParser();
//		SLATemplate slat = slasoieTemplatParser.parseTemplate(slatXml);
		
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager/applications/threeTierWebApp/deployments/31/ovf");
		
		logger.debug("Sending negotiate SOAP request...");
		logger.debug("Negotiation ID: " + negotiationId);
		SLATemplate[] slats = negotiationClient.negotiate(endpoint, slaTemplate, negotiationId);

		SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
		String xmlRetSlat = rend.renderSLATemplate(slats[0]);
		logger.debug("SLA Template:");
		logger.debug(xmlRetSlat);
		assertNotNull(xmlRetSlat);
		return xmlRetSlat;
	}

	private SLA testCreateAgreementWs(String negId, String slatXml) throws Exception {
		SLASOITemplateParser parser = new SLASOITemplateParser();
		SLATemplate slat = parser.parseTemplate(slatXml);
		System.out.println("Sending create agreement SOAP request...");
		SLA sla = negotiationClient.createAgreement(endpoint, slat, negId);
		System.out.println("SLA:");
		System.out.println(sla);
		assertNotNull(sla);
		return sla;
	}

	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
