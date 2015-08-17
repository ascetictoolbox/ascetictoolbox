package eu.ascetic.paas.slam.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert.SlaGuaranteedState;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;


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
        ViolationMessage message = (ViolationMessage) vmt.fromXML(xml);
        System.out.println(message.getAppId());
                
	}


	@Test
	public void applicationMonitorMessageTest() {
		//		{"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}
		String message = "{\"ApplicationId\":\"SinusApp\",\"Timestamp\":1431592067367,\"Terms\":{\"power_usage_per_app\":9.862471417356321, \"energy_usage_per_app\":11.8699999}}";


		ObjectMapper mapper = new ObjectMapper(); 
		ObjectNode msgBody = null;
		try {
			msgBody = (ObjectNode) mapper.readTree(message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Iterator<String> rootNames = msgBody.fieldNames();
		while(rootNames.hasNext()){
			String fieldName = rootNames.next();
			String fieldValue = msgBody.get(fieldName).asText();
			if (fieldName.equalsIgnoreCase("ApplicationId")) {
				System.out.println("APP "+fieldValue);
			}
			if (fieldName.equalsIgnoreCase("Timestamp")) {
				System.out.println("TS "+fieldValue);
			}
		}

		JsonNode termsJson = msgBody.get("Terms");
		Map<String,String> measuredTerms = new HashMap<String,String>();

		Iterator<String> fieldNames = termsJson.fieldNames();
		while(fieldNames.hasNext()){
			String fieldName = fieldNames.next();
			String fieldValue = termsJson.get(fieldName).asText();
			System.out.println(fieldName+" : "+fieldValue);
			measuredTerms.put(fieldName, fieldValue);
		}

		Assert.assertNotNull(measuredTerms);
	}


}
