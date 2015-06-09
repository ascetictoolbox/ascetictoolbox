package eu.ascetic.paas.applicationmanager.amqp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 * This class test the correct behaviour of the AmqpProducer class
 *
 */
public class AmqpProducerTest extends AbstractTest {

	@Test
	public void sendMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We create the message to be sent
		ApplicationManagerMessage messageToBeSent = new ApplicationManagerMessage();
		messageToBeSent.setApplicationId("111");
		messageToBeSent.setDeploymentId("222");
		messageToBeSent.setStatus("SOMETHING");
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "application.111.deployment.222", true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		// We send the message with our AmqpProducer
		AmqpProducer.sendMessage("application.111.deployment.222", messageToBeSent);
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		// We verify the results
		assertEquals("application.111.deployment.222", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("111", amMessageReceived.getApplicationId());
		assertEquals("222", amMessageReceived.getDeploymentId());
		assertEquals("SOMETHING", amMessageReceived.getStatus());
		
		receiver.close();
	}
	
	@Test
	public void sendNewApplicationMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.pepito.ADDED", true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		// We create the Application object
		Application application = new Application();
		application.setName("pepito");
		
		AmqpProducer.sendNewApplicationMessage(application);
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		// We verify the results
		assertEquals("APPLICATION.pepito.ADDED", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		
		receiver.close();
	}
	
	@Test
	public void sendDeploymentSubmittedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				                                               Configuration.amqpUsername, 
				                                               Configuration.amqpPassword,
				                                               "APPLICATION.pepito.DEPLOYMENT.23.SUBMITTED",
				                                               true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		// We create the Application object
		Application application = new Application();
		application.setName("pepito");
		
		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("SUBMITTED");
		application.addDeployment(deployment);
		
		AmqpProducer.sendDeploymentSubmittedMessage(application);
		
		// We wait one second just in case
		Thread.sleep(1000l);
		
		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.SUBMITTED", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("SUBMITTED", amMessageReceived.getStatus());
		
		receiver.close();
	}
	
	@Test
	public void sendDeploymentNegotiatingMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				                                               Configuration.amqpUsername, 
				                                               Configuration.amqpPassword,
				                                               "APPLICATION.pepito.DEPLOYMENT.23.NEGOTIATING",
				                                               true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("NEGOTIATING");
		
		AmqpProducer.sendDeploymentNegotiatingMessage("pepito", deployment);
		
		// We wait one second just in case
		Thread.sleep(700l);
		
		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.NEGOTIATING", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("NEGOTIATING", amMessageReceived.getStatus());
		
		receiver.close();
	}
	
	@Test
	public void sendDeploymentNegotiatedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				                                               Configuration.amqpUsername, 
				                                               Configuration.amqpPassword,
				                                               "APPLICATION.pepito.DEPLOYMENT.23.NEGOTIATED",
				                                               true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		
		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("NEGOTIATED");
		
		AmqpProducer.sendDeploymentNegotiatedMessage("pepito", deployment);
		
		// We wait one second just in case
		Thread.sleep(700l);
		
		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.NEGOTIATED", listener.getDestination());
		
		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("NEGOTIATED", amMessageReceived.getStatus());
		
		receiver.close();
	}
	
	@Test
	public void sendDeploymentContextualizingMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.CONTEXTUALIZING",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("CONTEXTUALIZING");

		AmqpProducer.sendDeploymentContextualizingMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.CONTEXTUALIZING", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("CONTEXTUALIZING", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendDeploymentContextualizedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.CONTEXTUALIZED",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("CONTEXTUALIZED");

		AmqpProducer.sendDeploymentContextualizedMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.CONTEXTUALIZED", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("CONTEXTUALIZED", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendDeploymentDeployingMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.DEPLOYING",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DEPLOYING");

		AmqpProducer.sendDeploymentDeployingMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.DEPLOYING", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DEPLOYING", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendDeploymentDeployedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.DEPLOYED",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DEPLOYED");

		AmqpProducer.sendDeploymentDeployedMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.DEPLOYED", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DEPLOYED", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendDeploymentErrorMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.ERROR",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("ERROR");

		AmqpProducer.sendDeploymentErrorMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.ERROR", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("ERROR", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendDeploymentDeletedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.TERMINATED",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DELETED");

		AmqpProducer.sendDeploymentDeletedMessage("pepito", deployment);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.TERMINATED", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DELETED", amMessageReceived.getStatus());

		receiver.close();
	}
	
	@Test
	public void sendVMDeployingMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.VM.44.DEPLOYING",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DEPLOYING");
		
		VM vmToTheMessage = new VM();
		vmToTheMessage.setId(44);
		vmToTheMessage.setHref("href4");
		vmToTheMessage.setOvfId("ovfId4");
		vmToTheMessage.setProviderId("provider-id4");
		vmToTheMessage.setProviderVmId("provider-vm-id4");
		vmToTheMessage.setStatus("XXX4");
		vmToTheMessage.setIp("172.0.0.14");
		vmToTheMessage.setSlaAgreement("slaAggrementId4");

		AmqpProducer.sendVMDeployingMessage("pepito", deployment, vmToTheMessage);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.VM.44.DEPLOYING", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DEPLOYING", amMessageReceived.getStatus());
		assertEquals(1, amMessageReceived.getVms().size());
		assertEquals("provider-vm-id4", amMessageReceived.getVms().get(0).getIaasVmId());
		assertEquals("ovfId4", amMessageReceived.getVms().get(0).getOvfId());
		assertEquals("XXX4", amMessageReceived.getVms().get(0).getStatus());
		assertEquals("44", amMessageReceived.getVms().get(0).getVmId());

		receiver.close();
	}
	
	@Test
	public void sendVMDeployedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.VM.44.DEPLOYED",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DEPLOYING");
		
		VM vmToTheMessage = new VM();
		vmToTheMessage.setId(44);
		vmToTheMessage.setHref("href4");
		vmToTheMessage.setOvfId("ovfId4");
		vmToTheMessage.setProviderId("provider-id4");
		vmToTheMessage.setProviderVmId("provider-vm-id4");
		vmToTheMessage.setStatus("XXX4");
		vmToTheMessage.setIp("172.0.0.14");
		vmToTheMessage.setSlaAgreement("slaAggrementId4");

		AmqpProducer.sendVMDeployedMessage("pepito", deployment, vmToTheMessage);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.VM.44.DEPLOYED", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DEPLOYING", amMessageReceived.getStatus());
		assertEquals(1, amMessageReceived.getVms().size());
		assertEquals("provider-vm-id4", amMessageReceived.getVms().get(0).getIaasVmId());
		assertEquals("ovfId4", amMessageReceived.getVms().get(0).getOvfId());
		assertEquals("XXX4", amMessageReceived.getVms().get(0).getStatus());
		assertEquals("44", amMessageReceived.getVms().get(0).getVmId());

		receiver.close();
	}
	
	@Test
	public void sendVMDeletedMessageTest() throws Exception {
		// We manually modify the configuration object to be able to use personally for this test
		Configuration.amqpAddress = "localhost:7672";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";

		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, 
				Configuration.amqpUsername, 
				Configuration.amqpPassword,
				"APPLICATION.pepito.DEPLOYMENT.23.VM.44.DELETED",
				true);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);

		Deployment deployment = new Deployment();
		deployment.setId(23);
		deployment.setStatus("DELETED");
		
		VM vmToTheMessage = new VM();
		vmToTheMessage.setId(44);
		vmToTheMessage.setHref("href4");
		vmToTheMessage.setOvfId("ovfId4");
		vmToTheMessage.setProviderId("provider-id4");
		vmToTheMessage.setProviderVmId("provider-vm-id4");
		vmToTheMessage.setStatus("XXX4");
		vmToTheMessage.setIp("172.0.0.14");
		vmToTheMessage.setSlaAgreement("slaAggrementId4");

		AmqpProducer.sendVMDeletedMessage("pepito", deployment, vmToTheMessage);

		// We wait one second just in case
		Thread.sleep(700l);

		// We verify the results
		assertEquals("APPLICATION.pepito.DEPLOYMENT.23.VM.44.DELETED", listener.getDestination());

		// We parse the received message and verify its values
		ApplicationManagerMessage amMessageReceived = ModelConverter.jsonToApplicationManagerMessage(listener.getMessage());
		assertEquals("pepito", amMessageReceived.getApplicationId());
		assertEquals("23", amMessageReceived.getDeploymentId());
		assertEquals("DELETED", amMessageReceived.getStatus());
		assertEquals(1, amMessageReceived.getVms().size());
		assertEquals("provider-vm-id4", amMessageReceived.getVms().get(0).getIaasVmId());
		assertEquals("ovfId4", amMessageReceived.getVms().get(0).getOvfId());
		assertEquals("XXX4", amMessageReceived.getVms().get(0).getStatus());
		assertEquals("44", amMessageReceived.getVms().get(0).getVmId());

		receiver.close();
	}
}
