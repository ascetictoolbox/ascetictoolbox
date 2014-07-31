package eu.ascetic.iaas.slamanager.poc.slatemplate.parser.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.parser.AsceticSlaTemplateParser;

public class AsceticSlaTemplateParserTest {
	
	private static SLATemplate slatProposal;

	@Before
	public void setUp() throws Exception {

		String proposalXml = FileUtils.readFileToString(new File("src/test/resources/slats/ASCETiC-SlaTemplateIaaSRequest.xml"));
		SLASOITemplateParser tp = new SLASOITemplateParser();
		slatProposal = tp.parseTemplate(proposalXml);
	}

	@Test
	public void testParseSlaProposal() {
		AsceticSlaTemplate cst = AsceticSlaTemplateParser.getAsceticSlat(slatProposal);
		System.out.println("PROPOSAL: " + cst);
	}

}
