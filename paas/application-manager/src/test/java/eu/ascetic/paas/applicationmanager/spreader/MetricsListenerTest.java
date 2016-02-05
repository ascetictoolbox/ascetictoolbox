package eu.ascetic.paas.applicationmanager.spreader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;

/**
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * Tests Message Listener to each one of the ActiveMQ IaaS messages
 */
public class MetricsListenerTest extends AbstractTest {

	@Test
	public void amqpUrlParserTest() {
		String amqpUrl1 = "amqp://guest:guest@iaas-vm-dev:5673";
		String amqUrl2 = "asdfasdfasd://guasdfasdst:gu131312est@iaaasdfasdfasdfas-vm-dev:5673";
		
		// Checking first
		MetricsListener ml = new MetricsListener(amqpUrl1, "");
		
		assertEquals("guest", ml.user);
		assertEquals("guest", ml.password);
		assertEquals("iaas-vm-dev:5673", ml.host);
		
		// Checking second
		ml = new MetricsListener(amqUrl2, "");
				
		assertEquals("guasdfasdst", ml.user);
		assertEquals("gu131312est", ml.password);
		assertEquals("iaaasdfasdfasdfas-vm-dev:5673", ml.host);
		
		// Checking null
		ml = new MetricsListener(null, "");
		
		assertEquals(null, ml.user);
		assertEquals(null, ml.password);
		assertEquals(null, ml.host);
	}
	
	@Test
	public void onMessageTest() throws Exception {
		MetricsListener metricsListener = new MetricsListener("amqp://" + Configuration.amqpUsername + ":" + Configuration.amqpPassword + "@" + Configuration.amqpAddress, 
															  "vm.*.item.power");
		
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "testing", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		// We send the message with our AmqpProducer
		String json = "{\n" 
				+ "   \"name\" : \"name\",\n" 
				+ "   \"value\" : 0.1,\n"
				+ "   \"units\" : \"units\",\n"
				+ "   \"timestamp\" : 22\n"
			+ "}";
		AmqpMessageProducer producer = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, "vm.489c0769-2ad3-4f76-a5d0-dedff0877f09.item.power", true);
		producer.sendMessage(json);
		producer.close();
		
		// We verify the response was correclty sended...
		Thread.sleep(2000l);
		
		assertEquals(1, listener.getTextMessages().size());
		
		receiver.close();
		metricsListener.close();
	}
}
