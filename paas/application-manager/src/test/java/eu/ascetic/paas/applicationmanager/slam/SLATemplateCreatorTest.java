package eu.ascetic.paas.applicationmanager.slam;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.slaatsoi.slamodel.SLATemplateDocument;

public class SLATemplateCreatorTest {

	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void verifyPartyIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		
		assertEquals("AsceticProvider", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyCostumerIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		
		assertEquals("ASCETiCUser", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyAddInterfaceDclr() {
		SLATemplate slaTemplate = new SLATemplate();
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		
		SLATemplateCreator.addInterfaceDclr(slaTemplate, ovfDefinition, "OVF_URL");
		
		// TODO after I know what I'm doing creating the template
		
		System.out.println(slaTemplate);
		
	}
	
	@Test
	public void verifyGenerateSLATemplate() throws Exception {
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);

		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "REST-URL");

		assertEquals("ASCETiC-SLaTemplate-Example-01", slaTemplate.getUuid().getValue());
		//assertEquals("1", slaTemplate.getModelVersion());

		System.out.println(slaTemplate);

//		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
//		SLATemplateDocument slaTemplateRendered =
//				SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));
//
//		System.out.println("SLA rendered as XML:");
//		System.out.println(slaTemplateRendered.toString());
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
