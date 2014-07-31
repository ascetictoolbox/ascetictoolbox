package eu.ascetic.iaas.slamanager.poc.manager.resource.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;
import org.junit.Before;
import org.junit.Test;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;

public class OvfParserTest {

	private String ovfString;

	private static final Logger log = Logger.getLogger(OvfParserTest.class);

	@Before
	public void setup() {
		try {
			ovfString = FileUtils.readFileToString(new File("src/test/resources/ovf/ascetic-ovf-example.ovf"));
			log.info("Parsed ovf");
			log.info(ovfString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void parseOvfTest() {
		XmlBeanEnvelopeDocument xmlBeanEnvelopeDocument = null;
		try {
			xmlBeanEnvelopeDocument = XmlBeanEnvelopeDocument.Factory.parse(ovfString);
			OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance(xmlBeanEnvelopeDocument);
			eu.ascetic.utils.ovf.api.VirtualSystem[] vms = ovfDefinition.getVirtualSystemCollection().getVirtualSystemArray();
			for (eu.ascetic.utils.ovf.api.VirtualSystem vs : vms) {
				log.info("VM : " + vs.getId());
				log.info("VM info: " + vs.getInfo());
				log.info("VM name: " + vs.getName());
				VirtualHardwareSection vhs = vs.getVirtualHardwareSection();
				log.info("VM hw info: " + vhs.getInfo());
				try {
					log.info("VM cpu speed" + vhs.getCPUSpeed());
				} catch (NullPointerException e) {

				}
				log.info("VM memory: " + vhs.getMemorySize());
				log.info("VM vcpu: " + vhs.getNumberOfVirtualCPUs());
			}
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}
}
