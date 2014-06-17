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
package eu.ascetic.vmc.api.core;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.core.libvirt.LibvirtControlBridge;
import eu.ascetic.vmc.api.core.libvirt.LibvirtEventBridge;
import eu.ascetic.vmc.libvirt.Connect;
import eu.ascetic.vmc.libvirt.Domain;
import eu.ascetic.vmc.libvirt.LibvirtException;

/**
 * Core logic of the Recontextualizer.
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public class VirtualMachineRecontextualizer implements Runnable, DomainStartedListener {

	private static final String JMX_FAILED = "JMX failed: ";

	private static final int CONNECTION_COUNT = 10;

	private static final int CONNECTION_INTERVAL = 3;

	protected static final Logger LOGGER = Logger
			.getLogger(VirtualMachineRecontextualizer.class);

	private VmcApi vmcApi;
	
	//Libvirt connection, domain and list of registered callbacks
	private Connect connection;

	private LibvirtEventBridge eventBridge;
	
	//Map where domain names for recontextualization is stored
	private Map<String, Boolean> recontextDomains;

	private LibvirtControlBridge controlBridge;
	
	//Create libvirt default event loop once
	static {
		try {
			Connect.initEventLoop();
		} catch (LibvirtException e) {
			LOGGER.error("Failed to initiate event loop", e);
		} catch (Error e) {
			//This probably means either we're doing a unit test or libvirt can't be found
			LOGGER.error("Failed to to initiate event loop", e); 
		}
	}

	/**
	 * Constructor that provides access to VMC state and config.
	 * @throws IOException If a connection to libvirt cannot be successfully established
	 * 
	 * @param vmcApi ?
	 * @param hypervisorUri The URL to the hypervisor (e.q.: qemu:///system)
	 * @throws IOException If a connection to Libvirt cannot be established
	 *  
	 */
	public VirtualMachineRecontextualizer(VmcApi vmcApi, String hypervisorUri) throws IOException {
		this.vmcApi = vmcApi;
		this.recontextDomains = new HashMap<String, Boolean>();

		try {
			connection = new Connect(hypervisorUri);
			connection.setKeepAlive(CONNECTION_INTERVAL, CONNECTION_COUNT);
		} catch (LibvirtException e) {
			LOGGER.info("Failed to connect Recontextualizer to Libvirt", e);
			throw new IOException(e);
		} catch (Error e) {
			LOGGER.info("Failed to connect Recontextualizer to Libvirt", e);
		}
		
		//Will throw LibvirtException if it fails
		try {
			this.eventBridge = new LibvirtEventBridge(connection);
			this.eventBridge.addDomainStartedListener(this);
		} catch (LibvirtException e) {
			LOGGER.info("Failed to initiate LibvirtEventBridge", e);
			throw new IOException(e);
		}
		
		this.controlBridge = new LibvirtControlBridge(connection);
		
		LOGGER.info("Exposing LibvirtControlBridge as an MBean");
		this.registerMBean(this.controlBridge, "eu.ascetic.vmc.api.Core:type=LibvirtControlBridgeMBean");
		
		LOGGER.info("Finished initialising Recontextualizer...");
	}
	
	/**
	 * Enables recontextualization for a specific domain. If the domain does not yet exist, it will be added once the domain is started
	 * 
	 * @param domainName The domain name of the VM that should be recontextualized (as seen by the hypervisor)
	 * @throws IOException If a connection to the domain cannot be established
	 */
	public synchronized void startRecontextualization(String domainName) throws IOException {
		
		//Attempt to connect, and put a failed in the recontextDomain map if necessary
		try {
			this.activateRecontextualization(domainName);
		} catch (IOException e) {
			LOGGER.debug("Failed to activate right away, put domainName in queue: " + domainName);
			this.recontextDomains.put(domainName, Boolean.FALSE);
		}
		
	}
	
	/**
	 * Tries to activate recontextualization for a specific domain.
	 * @param domainName The domain to activate for
	 * @throws IOException If activation fails
	 */
	private void activateRecontextualization(String domainName) throws IOException {
		
		try {
			Domain vmDomain = connection.domainLookupByName(domainName);
			LOGGER.info("Connected to Domain:" + vmDomain.getName() + " id "
					+ vmDomain.getID() + " running "
					+ vmDomain.getOSType());
			DomainContextualizer dContext = new DomainContextualizer(domainName, vmcApi, controlBridge);
			eventBridge.addListener(dContext);
			this.registerMBean(dContext, "eu.ascetic.vmc.api.Core:type=DomainContextualizerMBean");

			//Put a successful result into the map
			this.recontextDomains.put(domainName, Boolean.TRUE);
		} catch (Exception e) {
			LOGGER.info("Failed to connect to domain: " + domainName);
			throw new IOException(e);
		}
	}
	

	/**
	 * Called by EventBridge to indicate that a new domain has started. Check list of domains
	 * that are due for recontextualization and see if this is one of the missing domains that
	 * we're waiting for
	 */
	public synchronized void vmDomainCreated(String domainName) {
		if (recontextDomains.containsKey(domainName)) {
			LOGGER.info("Found recontextDomain: " + domainName);
			
			if (recontextDomains.get(domainName)) {
				LOGGER.info("Newly started domain already activated, ignoring.");
				return;
			}
			
			//Attempt to connect. This will update the status in the Map if successful
			try {
				this.activateRecontextualization(domainName);
			} catch (IOException e) {
				LOGGER.info("(Non-fatal): failed to activate recontext for domain: " + domainName, e);
			}
		} else {
			LOGGER.info("Detected new domain, but domainName not in recontext-list: " + domainName);
		}
	}
	
	/**
	 * Register object with MBean server
	 * @param object Object to register
	 * @param nameString Object name
	 */
	private void registerMBean(Object object, String nameString) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName(nameString);		
			mbs.registerMBean(object, name);
		} catch (MalformedObjectNameException e) {
			LOGGER.info(JMX_FAILED, e);
		} catch (InstanceAlreadyExistsException e) {
			LOGGER.info(JMX_FAILED, e);
		} catch (MBeanRegistrationException e) {
			LOGGER.info(JMX_FAILED, e);
		} catch (NotCompliantMBeanException e) {
			LOGGER.info(JMX_FAILED, e);
		}
	}
	
	/**
	 * Deactivates recontextualization for a specific domain
	 * 
	 * @param domainName The domain name of the VM that no longer should be recontextualized (as seen by the hypervisor)
	 * @return True if the listener was found and removed, false if the listener could not be found
	 */
	public synchronized boolean stopRecontextualization(String domainName) throws IOException {
		boolean activated = recontextDomains.get(domainName);
		this.recontextDomains.remove(domainName);
		
		//Remove bridge if previously activated
		if (activated) { 
			return eventBridge.removeListener(domainName);
		} else {
			return true;
		}
	}

	/**
	 * Starts the recontextualization process in a thread (useful for async API
	 * access)
	 * 
	 */
	public void run() {
		// TODO Improve what pertinent information is held on what the
		// Recontextualizer is doing in GlobalState
		vmcApi.getGlobalState().setRecontextRunning(true);
		while (true) {
			if (Thread.interrupted()) {
				LOGGER.info("I has interupt, bye bye!");
				break;
			}
			try {
				//Steps the Event loop
				if (connection.isAlive()) {
					connection.processEvent();
				}

			} catch (LibvirtException e) {
				LOGGER.warn("Exception from libvirt: ", e);
				break;
			}
		}
		
		//Close the connection to libvirt when done
		close();
	}
	
	/**
	 * Unregister for events and close the connection to libvirt when done
	 */
	public synchronized void close() {
		if (connection != null) {
			//Unregister all events by closing the bridge
			eventBridge.close();
				
			//Close connection
			try {
				connection.close();
				connection = null;
			} catch (LibvirtException e) {
				LOGGER.warn("Failed to close LibVirt connection", e);
			}
		}
	}
	
	/*
	 * Make sure connection is really closed before GC-ing this object
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() throws Throwable {
		try {
			if (connection != null) {
				LOGGER.warn("LibVirt connection still open during finalize, please add a call to close()");
				try {
					close();
				} catch (RuntimeException e) {
					LOGGER.warn("Failed to close LibVirt connection", e);
				}
			}
		} finally {
			super.finalize();
		}
	}
}
