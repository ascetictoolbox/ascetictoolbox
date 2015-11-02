package eu.ascetic.paas.applicationmanager.em.amqp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
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
 * Simple tests for the EnergyModellerMessageListener
 */
public class EnergyModellerMessageListenerTest extends AbstractTest {

	@Test
	public void onMessageTest() throws Exception {
			
		// Mock of the EnergyModellerMessageController
		EnergyModellerQueueController controller = mock(EnergyModellerQueueController.class);
		// Configuration of the listener
		Configuration.emMeasurementsTopic = "MEASUREMENTS";
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  Configuration.emMeasurementsTopic, true);
		EnergyModellerMessageListener emListener = new EnergyModellerMessageListener(controller, "aaa");
		receiver.setMessageConsumer(emListener);
		
		// We send a message to the queue
		// Message to be sent in the test.
		String message = "{" + 
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
		
		// We send the message with our AmqpProducer
		AmqpMessageProducer producer = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, Configuration.emMeasurementsTopic, true);
		producer.sendMessage(message);
		producer.close();
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		// We verify that the controller got the message
		EnergyModellerMessage emMessage = new EnergyModellerMessage();
		emMessage.setApplicationid("davidgpTestApp");
		emMessage.setDeploymentid("569");
		emMessage.setEventid("loquesea");
		emMessage.setUnit("SEC");
		emMessage.setGenerattiontimestamp("30 Sep 2015 16:29:35 GMT");
		emMessage.setReferredtimestamp("30 Sep 2015 16:29:35 GMT");
		emMessage.setValue("0.0");
		List<String> vms = new ArrayList<String>();
		vms.add("1899");
		emMessage.setVms(vms);
		verify(controller, times(1)).addMessage(emMessage, "aaa");
		
		// We clean at the end
		receiver.close();
	}
}
