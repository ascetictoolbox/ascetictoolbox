package eu.ascetic.paas.applicationmanager.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;
import eu.ascetic.providerregistry.model.Provider;

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
 * Unit test for the MonitoringBean class
 *
 */
public class MonitoringBeanTest {
	private static Logger logger = Logger.getLogger(MonitoringBeanTest.class);
	private static int selectedPort1;
	private static int selectedPort2;
	private static int minPort = 11000;
	private static int maxPort = 13000;
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";

	@BeforeClass
	public static void activeMQStartUp() throws Exception {
		selectedPort1 = randInt();
		
		BrokerService broker1 = new BrokerService();
		broker1.setBrokerName("broker1");
		broker1.setPersistent(false);
		broker1.addConnector("amqp://0.0.0.0:" + selectedPort1);
		broker1.start();
		
		logger.info("Creating ActiveMQ testing server at address: amqp://0.0.0.0:" + selectedPort1);
		
		selectedPort2 = randInt();
		
		BrokerService broker2 = new BrokerService();
		broker1.setBrokerName("broker2");
		broker2.setPersistent(false);
		broker2.addConnector("amqp://0.0.0.0:" + selectedPort2);
		broker2.start();
		
		logger.info("Creating ActiveMQ testing server at address: amqp://0.0.0.0:" + selectedPort2);
	}
	
	/**
	 * Returns a psuedo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimim value
	 * @param max Maximim value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	private static int randInt() {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((maxPort - minPort) + 1) + minPort;

	    return randomNum;
	}

	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
		
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
							"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
								"<items offset=\"0\" total=\"1\">" +
									"<provider href=\"/1\">" +
										"<id>1</id>" +
										"<name>default</name>" +
										"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
										"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
										"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
										"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
										"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
									"</provider>" +
								"</items>" +
								"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
							"</collection>";
		
		mServer.addPath("/", collection);
	}
	
	@Test
	public void constructorTest() {
		MonitoringBean monitoringBean = new MonitoringBean();
		List<Provider> providers = monitoringBean.providers;
		
		assertEquals(1, providers.size());
		assertEquals("default", providers.get(0).getName());
	}
}
