package eu.ascetic.paas.applicationmanager.em.amqp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.conf.Configuration;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit test that verifies the right work of the class EnergyModellerController
 *
 */
public class EnergyModellerQueueControllerTest extends AbstractTest {

	@Test
	public void createLRUMapTest() {
		Map<String, String> mapFixedSize = EnergyModellerQueueController.createLRUMap(4);
			
		mapFixedSize.put("a", "1");
		mapFixedSize.put("b", "2");
		mapFixedSize.put("c", "3");
		mapFixedSize.put("d", "4");
		
		assertEquals(4, mapFixedSize.size());
		assertEquals("1", mapFixedSize.get("a"));
		assertEquals("2", mapFixedSize.get("b"));
		assertEquals("3", mapFixedSize.get("c"));
		assertEquals("4", mapFixedSize.get("d"));
		
		mapFixedSize.put("e", "5");
		
		assertEquals(4, mapFixedSize.size());
		assertEquals(null, mapFixedSize.get("a"));
		assertEquals("2", mapFixedSize.get("b"));
		assertEquals("3", mapFixedSize.get("c"));
		assertEquals("4", mapFixedSize.get("d"));
		assertEquals("5", mapFixedSize.get("e"));
		
		// Checking LRU (Last resource used) behaviour... 
		mapFixedSize.put("b", "7");
		assertEquals(4, mapFixedSize.size());
		assertEquals(null, mapFixedSize.get("a"));
		
		mapFixedSize.put("f", "6");
		assertEquals(4, mapFixedSize.size());
		assertEquals(null, mapFixedSize.get("a"));
		assertEquals(null, mapFixedSize.get("c"));
	}
	
	@Test
	public void generateKeyByParametersTest() {
		String applicationId = "appID";
		String eventId = "eventId";
		String deploymentId = "deploymentId";
		String vm1 = "vm1";
		String vm2 = "vm2";
		String unit = "unit";
		
		List<String> vms = new ArrayList<String>();
		vms.add(vm1);
		vms.add(vm2);
		
		String key = EnergyModellerQueueController.generateKey(applicationId, eventId, deploymentId, vms, unit);

		assertEquals(applicationId + eventId + deploymentId + vm1 + vm2 + unit, key);
		
		key = EnergyModellerQueueController.generateKey(applicationId, eventId, deploymentId, null, unit);
		
		assertEquals(applicationId + eventId + deploymentId + unit, key);
	}
	
	@Test
	public void generateKeyWithObjectTest() {
		EnergyModellerMessage emMessage = new EnergyModellerMessage();
		emMessage.setApplicationid("appID");
		emMessage.setEventid("eventId");
		emMessage.setDeploymentid("deploymentId");
		emMessage.setUnit("unit");
		emMessage.setProvider("provider");
		emMessage.setValue("0.0");
		emMessage.setGenerattiontimestamp("generation");
		emMessage.setReferredtimestamp("referal");
		
		String vm1 = "vm1";
		String vm2 = "vm2";
		List<String> vms = new ArrayList<String>();
		vms.add(vm1);
		vms.add(vm2);
		
		emMessage.setVms(vms);
		
		String key = EnergyModellerQueueController.generateKey(emMessage);

		assertEquals("appID" + "eventId" + "deploymentId" + "vm1" + "vm2" + "unit", key);
		
		emMessage.setVms(null);
		
		key = EnergyModellerQueueController.generateKey(emMessage);

		assertEquals("appID" + "eventId" + "deploymentId" + "unit", key);
	}
	
	@Test
	public void addAndGetMessages() throws Exception {
		// Setup
		EnergyModellerQueueController.MAX_ENTRIES_CACHE = 4;
		Configuration.emMeasurementsTopic = "MEASUREMENTS";
		Configuration.emPredictionsTopic = "PREDICTIONS";
		
		EnergyModellerQueueController controller = new EnergyModellerQueueController();
		controller.afterPropertiesSet();
		
		// We send a message to the queue
		// Message to be sent in the test.
		String message1 = "{" + 
				"\"provider\":null," + 
				"\"applicationid\":\"davidgpTestApp\"," + 
				"\"eventid\":\"loquesea\"," + 
				"\"deploymentid\":\"569\"," + 
				"\"vms\":[\"1899\"]," + 
				"\"unit\":\"SEC\"," + 
				"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"value\":0.0" +
			"}";
		
		String key1 = "davidgpTestApp" +  "loquesea" + "569" + "1899" + "SEC";
		
		// We send the message with our AmqpProducer
		AmqpMessageProducer producerMeasurements = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, Configuration.emMeasurementsTopic, true);
		producerMeasurements.sendMessage(message1);
		
		AmqpMessageProducer producerPredictions = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, Configuration.emPredictionsTopic, true);
		producerPredictions.sendMessage(message1);
		
		// We wait one second just in case
		Thread.sleep(2000l);
		
		EnergyModellerMessage messageRetrived1 = controller.getMeasurementMessage(key1);
		assertEquals("davidgpTestApp", messageRetrived1.getApplicationid());
		
		messageRetrived1 = controller.getPredictionMessage(key1);
		assertEquals("davidgpTestApp", messageRetrived1.getApplicationid());
		
		String message2 = "{" + 
				"\"provider\":null," + 
				"\"applicationid\":\"davidgpTestApp\"," + 
				"\"eventid\":\"loquesea\"," + 
				"\"deploymentid\":\"569\"," + 
				"\"vms\":[\"1899\"]," + 
				"\"unit\":\"WATT\"," + 
				"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"value\":0.0" +
			"}";
		
		producerMeasurements.sendMessage(message2);
		producerPredictions.sendMessage(message2);
		
		String message3 = "{" + 
				"\"provider\":null," + 
				"\"applicationid\":\"davidgpTestApp\"," + 
				"\"eventid\":\"loquesea1\"," + 
				"\"deploymentid\":\"569\"," + 
				"\"vms\":[\"1899\"]," + 
				"\"unit\":\"WATT\"," + 
				"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"value\":0.0" +
			"}";
		
		producerMeasurements.sendMessage(message3);
		producerPredictions.sendMessage(message3);
		
		
		String message4 = "{" + 
				"\"provider\":null," + 
				"\"applicationid\":\"davidgpTestApp\"," + 
				"\"eventid\":\"loquesea4\"," + 
				"\"deploymentid\":\"569\"," + 
				"\"vms\":[\"1899\"]," + 
				"\"unit\":\"WATT\"," + 
				"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"value\":0.0" +
			"}";
		
		producerMeasurements.sendMessage(message4);
		producerPredictions.sendMessage(message4);
		
		String message5 = "{" + 
				"\"provider\":null," + 
				"\"applicationid\":\"davidgpTestApp\"," + 
				"\"eventid\":\"loquesea5\"," + 
				"\"deploymentid\":\"569\"," + 
				"\"vms\":[\"1899\"]," + 
				"\"unit\":\"WATT\"," + 
				"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
				"\"value\":0.0" +
			"}";
		
		producerMeasurements.sendMessage(message5);
		producerPredictions.sendMessage(message5);
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		messageRetrived1 = controller.getMeasurementMessage(key1);
		assertEquals(null, messageRetrived1);
		
		messageRetrived1 = controller.getPredictionMessage(key1);
		assertEquals(null, messageRetrived1);
		
		//Cleaning... 
		producerMeasurements.close();
		producerPredictions.close();
	}
}
