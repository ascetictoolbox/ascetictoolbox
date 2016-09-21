package eu.ascetic.paas.applicationmanager.spreader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
		MetricsListener ml = new MetricsListener(amqpUrl1, "", "providerId", null);
		
		assertEquals("guest", ml.user);
		assertEquals("guest", ml.password);
		assertEquals("iaas-vm-dev:5673", ml.host);
		
		// Checking second
		ml = new MetricsListener(amqUrl2, "", "providerId", null);
				
		assertEquals("guasdfasdst", ml.user);
		assertEquals("gu131312est", ml.password);
		assertEquals("iaaasdfasdfasdfas-vm-dev:5673", ml.host);
		
		// Checking null
		ml = new MetricsListener(null, "", "providerId", null);
		
		assertEquals(null, ml.user);
		assertEquals(null, ml.password);
		assertEquals(null, ml.host);
	}
	
	@Test
	public void onMessageTest() throws Exception {
		VMDAO vmDAO = mock(VMDAO.class);
		
		Application application = new Application();
		application.setName("SUPER_APP");
		Deployment deployment = new Deployment();
		deployment.setApplication(application);
		deployment.setId(22);
		eu.ascetic.paas.applicationmanager.model.VM vm = new eu.ascetic.paas.applicationmanager.model.VM();
		vm.setProviderId("providerId");
		vm.setId(111);
		vm.setProviderVmId("489c0769-2ad3-4f76-a5d0-dedff0877f09");
		vm.setDeployment(deployment);
		vm.setCpuActual(2);
		vm.setRamActual(1024);
		vm.setSwapActual(122);
		vm.setDiskActual(23);
		vm.setPriceSchema(2l);
		when(vmDAO.getVMWithProviderVMId("489c0769-2ad3-4f76-a5d0-dedff0877f09", "providerId")).thenReturn(vm);
		
		MetricsListener metricsListener = new MetricsListener("amqp://" + Configuration.amqpUsername + ":" + Configuration.amqpPassword + "@" + Configuration.amqpAddress, 
															  "vm.*.item.power" , "providerId", vmDAO);

		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.SUPER_APP.DEPLOYMENT.22.VM.111.METRIC.power", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		// We send the message with our AmqpProducer
		String json = "{\n" 
				+ "   \"name\" : \"power\",\n" 
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
		assertEquals("APPLICATION.SUPER_APP.DEPLOYMENT.22.VM.111.METRIC.power", listener.getTextMessages().get(0).getJMSDestination().toString());
		ApplicationManagerMessage message = ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText());
		assertEquals("SUPER_APP", message.getApplicationId());
		assertEquals("22", message.getDeploymentId());
		assertEquals("111", message.getVms().get(0).getVmId());
		assertEquals("power", message.getVms().get(0).getMetricName());
		assertEquals("489c0769-2ad3-4f76-a5d0-dedff0877f09", message.getVms().get(0).getIaasVmId());
		assertEquals("providerId", message.getVms().get(0).getProviderId());
		assertEquals(0.1, message.getVms().get(0).getValue(), 0.0001);
		assertEquals("units", message.getVms().get(0).getUnits());
		assertEquals(22l, message.getVms().get(0).getTimestamp());
		assertEquals(2, message.getVms().get(0).getCpu());
		assertEquals(1024, message.getVms().get(0).getRam());
		assertEquals(122, message.getVms().get(0).getSwap());
		assertEquals(2, message.getVms().get(0).getPriceSchema());
		assertEquals(23, message.getVms().get(0).getDisk());
				
		producer = new AmqpMessageProducer(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword, "vm.489c0769-2ad3-4f76-a5d0-dedff0877f0.item.power", true);
		producer.sendMessage(json);
		producer.close();
		
		// We verify the response was correclty sended...
		Thread.sleep(2000l);
		
		assertEquals(1, listener.getTextMessages().size());
		
		receiver.close();
		metricsListener.close();
	}
}
