package eu.ascetic.test.paas.applicationmanager.amqp;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.test.conf.Configuration;

/**
 * 
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
 *  
 * Unit tests to see if the List listener is bahaving as expected... 
 */
public class AppManagerAmqpReceiverUnitTests extends AbstractTests {

	@Test
	public void onMessage() throws Exception {
		Configuration.queueSize = 2;
		Configuration.paasActiveMQUrl="amqp://guest:guest@localhost:5672";
		
		AppManagerAmqpReceiver receiver = new AppManagerAmqpReceiver();
		
		ApplicationManagerMessage appMessage = new ApplicationManagerMessage();
		appMessage.setApplicationId("appId");
		appMessage.setDeploymentId("deployID");
		appMessage.setStatus("DEPLOYED");
		
		AmqpMessageProducer producer = new AmqpMessageProducer("localhost:5672", "guest", "guest", "APPLICATION.appId.DEPLOYMENT.deployID", true);
		producer.sendMessage(ModelConverter.applicationManagerMessageToJSON(appMessage));
		
		Thread.sleep(1000l);
		
		AppManagerAmqpMessage message = new AppManagerAmqpMessage();
		message.setApplicationId("appId");
		message.setDeploymentId("deployID");
		message.setStatus("DEPLOYED");
		
		assertTrue(receiver.contains(message));
		
		receiver.close();
		producer.close();
	}
}
