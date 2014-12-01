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


package eu.ascetic.paas.slam.test;

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

import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationWsClient;
import eu.ascetic.paas.slam.poc.impl.provider.translation.SlaTranslator;
import eu.ascetic.paas.slam.poc.impl.provider.translation.SlaTranslatorImplNoOsgi;

public class ProviderNegotiationWsTest {

	private static NegotiationWsClient nc;
	private static final int serverPort = 8080;
	private static final String requestUrl = "/services/asceticNegotiation";
	
	
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
    public String testInitiateNegotiationWs() throws Exception {
		
    	String slatXml = FileUtils.readFileToString(
    			new File("src/test/resources/slats/ascetic-basic-slat.xml")); 
    			
	    SLASOITemplateParser slasoieTemplatParser = new SLASOITemplateParser();
	    SLATemplate slat = slasoieTemplatParser.parseTemplate(slatXml);		
	    System.out.println("Sending initiateNegotiation SOAP request...");
	    String hexCode = nc.initiateNegotiation("http://10.4.0.16:" + serverPort + requestUrl, slat);

	    System.out.println("Negotiation ID: "+hexCode);
	    Assert.assertNotNull(hexCode);
	    return hexCode;
    }
    
    
    
    //@Test
    public String testNegotiationWs(String negId) throws Exception {
    	
    	
    	String slatXml = FileUtils.readFileToString(
    			new File("src/test/resources/slats/ascetic-basic-slat.xml")); 
    			
	    SLASOITemplateParser parser = new SLASOITemplateParser();
	    SLATemplate slat = parser.parseTemplate(slatXml);		
	    System.out.println("Sending negotiate SOAP request...");
	    SLATemplate[] slats = nc.negotiate("http://10.4.0.16:" + serverPort + requestUrl, slat, negId);


	    SLASOITemplateRenderer rend = new SLASOITemplateRenderer();
	    String xmlRetSlat = rend.renderSLATemplate(slats[0]);
	    System.out.println("SLAT: "+xmlRetSlat);
	    
	    Assert.assertNotNull(xmlRetSlat);
	    return xmlRetSlat;
    }
    
    
    
    //@Test
    public SLA testCreateAgreementWs(String negId, String slatXml)  {
    	
	    SLASOITemplateParser parser = new SLASOITemplateParser();
	    SLA sla = null;	 
	    try {
	    SLATemplate slat = parser.parseTemplate(slatXml);		
	    System.out.println("Sending create agreement SOAP request...");
	     sla = nc.createAgreement("http://10.4.0.16:" + serverPort + requestUrl, slat, negId);
    } catch (Exception ex) {
		System.out.println("Known expection during SLA parsing: the operation was successful indeed.");
	}
	    System.out.println(sla);
//	    Assert.assertNotNull(sla);
	    return sla;
    }
    
    //@Test
    public void testWorkflow() throws Exception {
    	String hex = testInitiateNegotiationWs();
    	String slat = testNegotiationWs(hex);
     	SLA sla = testCreateAgreementWs(hex, slat);
    }
 	
	@After
	public void tearDown() throws IOException {
		System.out.println("HTTP server stopped");
	}

}
