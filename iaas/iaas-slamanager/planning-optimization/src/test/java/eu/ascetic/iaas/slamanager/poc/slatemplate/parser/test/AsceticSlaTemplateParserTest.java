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
package eu.ascetic.iaas.slamanager.poc.slatemplate.parser.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SLATemplateParser;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.service.Interface;
import org.slasoi.slamodel.service.ResourceType;
import org.slasoi.slamodel.sla.Endpoint;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.vocab.sla;

import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.SlaTemplateBuilder;
import eu.ascetic.iaas.slamanager.poc.slatemplate.parser.AsceticSlaTemplateParser;
import eu.slaatsoi.slamodel.InterfaceResourceTypeType;
import eu.slaatsoi.slamodel.SLATemplateDocument;

public class AsceticSlaTemplateParserTest {

	private static SLATemplate slatProposal;

	@Before
	public void setUp() throws Exception {

		String proposalXml = FileUtils.readFileToString(new File("src/test/resources/slats/ASCETiC-SlaTemplateIaaSRequest.xml"));
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		
		SLASOITemplateParser tp = new SLASOITemplateParser();
		slatProposal = tp.parseTemplate(proposalXml);
		SLATemplateDocument slaTemplateRendered =
                SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slatProposal));
		System.out.println("SLA rendered as XML:");
		System.out.println(slaTemplateRendered.toString());
		
/*		ResourceType resType=new ResourceType("OVFAppliance");
		Endpoint endpoint=new Endpoint(new ID("haproxy-VM-Type"), new UUID("VM-Manager ID"), sla.HTTP);
		Endpoint[] endpoints={endpoint};
		InterfaceDeclr intDecl=new InterfaceDeclr(new ID("haproxy"), new ID("AsceticProvider"), endpoints, resType);
		System.out.println(intDecl);*/
		
	}

	@Test
	public void testParseSlaProposal() {
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(slatProposal);
		System.out.println("PROPOSAL: " + cst);
		
		SlaTemplateBuilder slaTemplateBuilder = new SlaTemplateBuilder();
		slaTemplateBuilder.setAsceticSlatemplate(cst);

		SLATemplate slaT= slaTemplateBuilder.build();
		
		System.out.println("SLA Template built:");
		System.out.println(slaT.toString());
	}
	
	

}
