package eu.ascetic.test.paas.applicationmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
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
	private String threeTierWebAppOvfFile = "webapp-ovf.xml";
	private String threeTierWebAppOvfString;
	
	private void setup(boolean deploys, String testName) {
		this.testName = testName;
		this.deploys = deploys;
		
		logger.info(" STARTING TEST: " + this.testName + ", CREATES A DEPLOYMENT? " + this.deploys); 
	}

	@Before
	public void loadOvfFile() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
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
	public void applicationLifecycle() throws InterruptedException {
		setup(true, "Lifecycle of a simple application");
		
		logger.info("###### SETTING UP THE LISTENER FOR THE QUEUE ######");
		AppManagerAmqpReceiver receiver = new AppManagerAmqpReceiver();
		
		logger.info("###### DEPLOYING APPLICATION #######");
		Application application =  ApplicationManagerClient.createDeployment(threeTierWebAppOvfString);
		Deployment deployment = application.getDeployments().get(0);
		logger.info("Deployment with ID: " + deployment.getId());
		this.deploymentId = deployment.getId();
		
		logger.info("###### CHECKING SUBMITTED STATUS ########");
		boolean wasSubmitted = waitingForState(application, "SUBMITTED", receiver);
		if(!wasSubmitted) {
			fail("Application was not submitted after waiting for 5 minutes");
		}
		
		logger.info("###### CHECKING NEGOTIATED STATUS ########");
		boolean wasNegotiated = waitingForState(application, "NEGOTIATED", receiver);
		if(!wasNegotiated) {
			fail("Application was not NEGOTIATED after waiting at least for 5 minutes after SUBMITTED");
		}
		
		logger.info("###### CHECKING CONTEXTUALIZED STATUS ########");
		boolean wasContextualized = waitingForState(application, "CONTEXTUALIZED", receiver);
		if(!wasContextualized) {
			fail("Application was not CONTEXTUALIZED after waiting at least for 5 minutes after NEGOTIATED");
		}
		
		logger.info("###### CHECKING DEPLOYED STATUS ########");
		boolean wasDeployed = waitingForState(application, "DEPLOYED", receiver);
		if(!wasDeployed) {
			fail("Application was not DEPLOYED after waiting at least for 5 minutes after CONTEXTUALIZED");
		}
		
		logger.info("###### DELETING THE DEPLOYMENT ########");
		ApplicationManagerClient.deleteDeployment(application.getName(), "" + deploymentId);
	
		logger.info("###### CHECKING TERMINATED STATUS ########");
		boolean wasTerminated = waitingForState(application, "TERMINATED", receiver);
		if(!wasTerminated) {
			fail("Application was not TERMINATED after waiting at least for 5 minutes");
		}
	}
	
	private boolean waitingForState(Application application, String state, AppManagerAmqpReceiver receiver) throws InterruptedException {
		boolean reachedThatState = false;
		
		AppManagerAmqpMessage message = new AppManagerAmqpMessage();
		message.setApplicationId(application.getName());
		message.setDeploymentId("" + deploymentId);
		message.setStatus(state);
		
		for(int i = 0; i <= 20; i++) {
			if(receiver.contains(message)) {
				logger.info("Found message for state " + state);
				reachedThatState = true;
				i = 100;
			}
			
			Thread.sleep(30000l);
		}
		
		return reachedThatState;
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
	
	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
