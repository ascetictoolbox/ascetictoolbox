package eu.ascetic.iaas.slamanager.poc.slatemplate.parser.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SLATemplateParser;
import org.slasoi.slamodel.service.Interface;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;

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
