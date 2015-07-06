package eu.ascetic.paas.applicationmanager.amqp;

import java.util.Random;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import eu.ascetic.paas.applicationmanager.conf.Configuration;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * This class creates on the fly an ActiveMQ broker to use for testing proposes.
 */
public class AbstractTest {
	private static Logger logger = Logger.getLogger(AbstractTest.class);
	private static int minPort = 11000;
	private static int maxPort = 13000;
	private static int selectedPort = 7673;
	
	@BeforeClass
	public static void activeMQStartUp() throws Exception {
		selectedPort = randInt();
		
		BrokerService broker = new BrokerService();
		broker.setPersistent(false);
		broker.addConnector("amqp://0.0.0.0:" + selectedPort);
		broker.start();
		
		logger.info("Creating ActiveMQ testing server at address: amqp://0.0.0.0:" + selectedPort);
		
		Configuration.amqpAddress = "localhost:" + selectedPort;
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
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
}

