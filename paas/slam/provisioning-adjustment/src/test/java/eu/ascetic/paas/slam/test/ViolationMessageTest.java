package eu.ascetic.paas.slam.test;

import java.io.File;
import java.util.Calendar;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;

import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert.SlaGuaranteedState;


public class ViolationMessageTest {

	@Test
	public void createViolationMessage() throws Exception {
		ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(),"sampleApp","sampleDep");
		Alert alert = violationMessage.new Alert();
		alert.setType("violation");
		alert.setSlaUUID("sampleSlaUUID");
		Value v = new Value("free", "11");
		violationMessage.setValue(v);
		alert.setSlaAgreementTerm("power_usage_per_app");
		SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
		sgs.setGuaranteedId("power_usage_per_app");
		sgs.setGuaranteedValue(10.0);
		sgs.setOperator("less_than_or_equals");
		alert.setSlaGuaranteedState(sgs);
		violationMessage.setAlert(alert);
		
		ViolationMessageTranslator vmt = new ViolationMessageTranslator();
		String xml = vmt.toXML(violationMessage);
		System.out.println(xml);
		Assert.assertNotNull(xml);
	}
	
}
