package eu.ascetic.paas.applicationmanager.amqp.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;


/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * 
 * Unit test to verify the correct behaviour of the class ApplicationManagerMessage
 * 
 */
public class ApplicationManagerMessageTest {

	@Test
	public void pojo() {
		ApplicationManagerMessage amMessage = new ApplicationManagerMessage();
		amMessage.setApplicationId("app-id");
		amMessage.setDeploymentId("deplo-id");
		amMessage.setStatus("status");
		List<VM> vms = new ArrayList<VM>();
		amMessage.setVms(vms);
		
		assertEquals(vms, amMessage.getVms());
		assertEquals("app-id", amMessage.getApplicationId());
		assertEquals("deplo-id", amMessage.getDeploymentId());
		assertEquals("status", amMessage.getStatus());
	}
	
	@Test
	public void addVms() {
		ApplicationManagerMessage amMessage = new ApplicationManagerMessage();
		
		assertEquals(null, amMessage.getVms());
		
		VM vm1= new VM();
		
		amMessage.addVM(vm1);
		
		assertEquals(1, amMessage.getVms().size());
		assertEquals(vm1, amMessage.getVms().get(0));
		
		VM vm2 = new VM();
		amMessage.addVM(vm2);
		
		assertEquals(2, amMessage.getVms().size());
		assertEquals(vm1, amMessage.getVms().get(0));
		assertEquals(vm2, amMessage.getVms().get(1));
	}
	
	@Test
	public void toJSON() throws Exception {
	     // Create a JaxBContext
		JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {ApplicationManagerMessage.class, VM.class}, null);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
	   // marshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
		 
		ApplicationManagerMessage amMessage = new ApplicationManagerMessage();
		amMessage.setApplicationId("app-id");
		amMessage.setDeploymentId("deplo-id");
		amMessage.setStatus("status");
		
		VM vm1 = new VM();
		vm1.setIaasMonitoringVmId("aaa");
		vm1.setIaasVmId("bbb");
		vm1.setOvfId("ccc");
		vm1.setStatus("ddd");
		vm1.setVmId("eee");
		
		VM vm2 = new VM();
		vm2.setIaasMonitoringVmId("zzz");
		vm2.setIaasVmId("yyy");
		vm2.setOvfId("xxx");
		vm2.setStatus("ttt");
		vm2.setVmId("www");
		
		amMessage.addVM(vm1);
		amMessage.addVM(vm2);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(amMessage, out);
		String output = out.toString();
		
		//We verify the output format
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(output);
		JSONObject jsonObject = (JSONObject) obj;
		assertEquals("app-id", (String) jsonObject.get("applicationId"));
		assertEquals("deplo-id", (String) jsonObject.get("deploymentId"));
		assertEquals("status", (String) jsonObject.get("status"));
		
		JSONArray vms = (JSONArray) jsonObject.get("vms");
		jsonObject = (JSONObject) vms.get(0);
		assertEquals("eee", (String) jsonObject.get("vmId"));
		assertEquals("bbb", (String) jsonObject.get("iaasVmId"));
		assertEquals("aaa", (String) jsonObject.get("iaasMonitoringVmId"));
		assertEquals("ccc", (String) jsonObject.get("ovfId"));
		assertEquals("ddd", (String) jsonObject.get("status"));
		
		jsonObject = (JSONObject) vms.get(1);
		assertEquals("www", (String) jsonObject.get("vmId"));
		assertEquals("yyy", (String) jsonObject.get("iaasVmId"));
		assertEquals("zzz", (String) jsonObject.get("iaasMonitoringVmId"));
		assertEquals("xxx", (String) jsonObject.get("ovfId"));
		assertEquals("ttt", (String) jsonObject.get("status"));
	}
	
	@Test
	public void FromJson() throws Exception {
		String json = "{" +
						 "\"applicationId\" : \"app-id\"," +
						 "\"deploymentId\" : \"deplo-id\"," + 
						 "\"status\" : \"status\"," +
						 "\"vms\" : [ {" +
						     "\"vmId\" : \"eee\"," +
						     "\"iaasVmId\" : \"bbb\"," +
						     "\"iaasMonitoringVmId\" : \"aaa\"," +
						     "\"ovfId\" : \"ccc\"," +
						     "\"status\" : \"ddd\"" +
						 "}, {" +
						     "\"vmId\" : \"www\"," +
						     "\"iaasVmId\" : \"yyy\"," +
						     "\"iaasMonitoringVmId\" : \"zzz\"," +
						     "\"ovfId\" : \"xxx\"," +
						     "\"status\" : \"ttt\"" +
						 "} ]" +
					  "}";
		
        // Create a JaxBContext
		JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {ApplicationManagerMessage.class, VM.class}, null);
        
        // Create the Unmarshaller Object using the JaxB Context
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        
        StreamSource jsonSource = new StreamSource(new StringReader(json));  
        ApplicationManagerMessage amMessage = unmarshaller.unmarshal(jsonSource, ApplicationManagerMessage.class).getValue();

		assertEquals("app-id", amMessage.getApplicationId());
		assertEquals("deplo-id", amMessage.getDeploymentId());
		assertEquals("status", amMessage.getStatus());

		assertEquals("eee", amMessage.getVms().get(0).getVmId());
		assertEquals("bbb", amMessage.getVms().get(0).getIaasVmId());
		assertEquals("aaa", amMessage.getVms().get(0).getIaasMonitoringVmId());
		assertEquals("ccc", amMessage.getVms().get(0).getOvfId());
		assertEquals("ddd", amMessage.getVms().get(0).getStatus());
		
		assertEquals("www", amMessage.getVms().get(1).getVmId());
		assertEquals("yyy", amMessage.getVms().get(1).getIaasVmId());
		assertEquals("zzz", amMessage.getVms().get(1).getIaasMonitoringVmId());
		assertEquals("xxx", amMessage.getVms().get(1).getOvfId());
		assertEquals("ttt", amMessage.getVms().get(1).getStatus());
	}
}

