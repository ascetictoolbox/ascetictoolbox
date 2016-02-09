package eu.ascetic.test.paas.applicationmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.test.conf.Configuration;
import eu.ascetic.test.paas.applicationmanager.amqp.AppManagerAmqpMessage;
import eu.ascetic.test.paas.applicationmanager.amqp.AppManagerAmqpReceiver;
import eu.ascetic.test.paas.applicationmanager.client.ApplicationManagerClient;

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
 *  It contains all the Integration/System Tests for the Application Manager
 */

public class ApplicationManagerTest {
	private static Logger logger = Logger.getLogger(ApplicationManagerTest.class);
	private int deploymentId;
	private boolean deploys;
	private String testName;
	
	private void setup(boolean deploys, String testName) {
		this.testName = testName;
		this.deploys = deploys;
		
		logger.info(" STARTING TEST: " + this.testName + ", CREATES A DEPLOYMENT? " + this.deploys); 
	}

	
	/**
	 * This test checks before executing any tests that App Manager and PaaS ActiveMQ are running.
	 * If they fail, the test is not going to be executed... 
	 */
	@Before
	public void checkRunning() {
		isAppManagerRunning();
		isActiveMQRunning();
	}
	
	/**
	 * If verifies if the Application Manager is active
	 * It performs a GET over the /application-manager path
	 */
	private void isAppManagerRunning() {
		setup(false, "is Application Manager RUNNING?");
		
		String version = ApplicationManagerClient.getApplicationManagerVersion();
		assertEquals("It is impossible to connect to the Application Manager in this address: " + Configuration.applicationManagerURL, "0.1-SNAPSHOT", version);
	}
	
	/**
	 * It verifies if the ActiveMQ Broker is running.
	 */
	private void isActiveMQRunning() {
		setup(false, "is PaaS AMQP Broker RUNNING?");
		AppManagerAmqpReceiver receiver = new AppManagerAmqpReceiver();
		
		ApplicationManagerMessage appMessage = new ApplicationManagerMessage();
		appMessage.setApplicationId("testAppId");
		appMessage.setDeploymentId("deployID");
		appMessage.setStatus("DEPLOYED");
		
		try {
			AmqpMessageProducer producer = new AmqpMessageProducer(receiver.getUrl(), 
																   receiver.getUser(), 
																   receiver.getPassword(), 
																   "APPLICATION.testAppId", 
																   true);
			producer.sendMessage(ModelConverter.applicationManagerMessageToJSON(appMessage));
		
			Thread.sleep(2000l);
		
			AppManagerAmqpMessage message = new AppManagerAmqpMessage();
			message.setApplicationId("testAppId");
			message.setDeploymentId("deployID");
			message.setStatus("DEPLOYED");
		
			assertTrue("Error getting message from AMQP Broker: " + Configuration.paasActiveMQUrl, receiver.contains(message));
			
			receiver.close();
			producer.close();
		} catch(Exception e) {
			logger.info("Error connecting to AMQP Broker");
			logger.info("Stack trace: " + e.getStackTrace());
			fail("Imposible to connect to the AMQP Broker: " + Configuration.paasActiveMQUrl);
		} 
	}
	
	@Test
	public void deployAnApplication() {
		setup(true, "Deployment of a simple application");
		logger.info("I will see this message if everything is ok...");
	}
	
	@After
	public void cleanUp() {
		logger.info("Cleaning up after the execution of test : "  + testName);
		
		if(deploys) {
			logger.info("Deleting the created deployment");
		} else {
			logger.info("Test did not deploy anything, no need to clean up... ");
		}
	}
}
