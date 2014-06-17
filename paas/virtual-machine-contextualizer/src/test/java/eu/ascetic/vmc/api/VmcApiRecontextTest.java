/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author django
 * 
 */
public class VmcApiRecontextTest extends TestCase {

	protected final static Logger log = Logger
			.getLogger(VmcApiRecontextTest.class);

	/**
	 * @param name
	 */
	public VmcApiRecontextTest(String testName) {
		super(testName);
	}

	/**
	 * @return The suite of tests being tested.
	 */
	public static Test suiteRecontext() {
		return new TestSuite(VmcApiRecontextTest.class);
	}

	/**
	 * Get current method name
	 * 
	 * @return Method Name
	 */
	public String getCurrentMethodName() {
		StackTraceElement stackTraceElements[] = (new Throwable())
				.getStackTrace();
		return stackTraceElements[1].toString();
	}

	/**
	 * Tests the progress object callback function when we are 100% sure the
	 * state data has been initialised.
	 */
	public void testRecontextualize() {
		log.info("### TEST: " + getCurrentMethodName() + " STARTED ###");

		String configFilePath = System.getProperty("configFilePath");
		
		log.warn("Using default recontextualization values for testing purposes...");
		recontextualize(configFilePath, false, "xen:///", "someDomain");

		log.warn("### TEST: " + getCurrentMethodName() + " COMPLETE ###");
	}

	/**
	 * Method to run the recontextualization process. Can be run in standalone
	 * mode or exit immediately for unit testing
	 * 
	 * @param standalone
	 *            Whether to continue execution when called
	 */
	public void recontextualize(String configFilePath, boolean standalone, String hypervisorUri, String domainName) {
		try {
			// Initialise the VMC's configuration providing the path of the
			// config.properties file...
			GlobalConfiguration globalConfiguration;
			if (configFilePath != null) {
				globalConfiguration = new GlobalConfiguration(configFilePath);
			} else {
				globalConfiguration = new GlobalConfiguration();
			}
			VmcApi vmcApi = new VmcApi(globalConfiguration);

			log.info("Attempting to start recontextualization with Hypervisor URI: " + hypervisorUri);
			vmcApi.recontextualize(hypervisorUri);

			// TODO Do something useful with the status returned like output it
			if (vmcApi.recontextualizeStatus()) {
				log.info("Recontextualization Started!");
			}
			
			log.info("Attempting to listen for events from domain: " + domainName);
			vmcApi.startRecontext(domainName);

			// For the test case
			if (standalone == false) {
				Thread.sleep(1000); // Wait for thread to be initialised before
									// we try to stop it in the unit test
				vmcApi.recontextualizeStop();
			}

			vmcApi.recontextualizeJoin();
			log.info("Recontextualization Terminated!");

		} catch (Exception e) {
			if (standalone) {
				log.error("Exception! Cause: " + e.getCause(), e);
			} else {
				log.warn("Recontextualization failed, unit test without libvirt installed?");
			}
		}
	}
}
