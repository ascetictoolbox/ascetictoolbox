package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageParser {

	@BeforeClass
	public static void setup() {
		
		
	}
	
	
	
	@Test
	public void testMeasureAllPoweryInterface() {
		
		String payload = "{ \"applicationId\" : \"JEPlus\", \"deploymentId\" : \"498\", \"status\" : \"DEPLOYING\",\"vms\" : [ {\"vmId\" : \"1797\",\"iaasVmId\" : \"2b7a486f-0081-424a-a520-4821285ad5f1\",\"ovfId\" : \"ascetic-pm-JEPlus\",\"status\" : \"ACTIVE\"}]}";
				
				
	    ObjectMapper jmapper = new ObjectMapper();
	    Map<String, Object> userData;
		try {
			userData = jmapper.readValue(payload, Map.class);
		
		
	    String vms = userData.get("vms").toString();
		if (vms==null){
			System.out.println("Unable to parse AMQP deployment message, missing iaas id");
			return;
		}
		System.out.println("Received "+vms.toString());
		//ArrayList<String> list = (ArrayList<String,String) userData.get("vms");
	    //Map<String,Object> vmData = jmapper.readValue(vms, Map.class);
	
		JsonNode test2 = jmapper.readValue(payload, JsonNode.class);
      
        
        
	   
		
		System.out.println("Received DEPLOYED message"+ test2.findValue("iaasVmId"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

	
}


