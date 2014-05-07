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
package eu.ascetic.vmc.api.Core.Libvirt;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.Core.DomainContextualizer;
import eu.ascetic.vmc.api.Core.DomainStartedListener;
import eu.optimis.vc.libvirt.Connect;
import eu.optimis.vc.libvirt.Connect.DomainEvent.LifecycleCallback;
import eu.optimis.vc.libvirt.Domain;
import eu.optimis.vc.libvirt.LibvirtException;

/**
 * Event bridge converting from Libvirtevents to more easily managed events in the LibvirtEventListener
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public class LibvirtEventBridge implements LifecycleCallback {
	
	protected static final Logger LOGGER = Logger
			.getLogger(LibvirtEventBridge.class);
	
	private List<Integer> callbackIds;
	private Connect connection;

	private List<DomainContextualizer> domainContextualizers;
	private List<DomainStartedListener> domainStartedListeners;
	
	/**
	 * Create a new bridge for a specific Libvirt connection
	 * @param connection The connection to listen to
	 * @throws LibvirtException 
	 */
	public LibvirtEventBridge(Connect connection) throws LibvirtException {
		this.connection = connection;
		this.callbackIds = new ArrayList<Integer>(1);
		this.domainContextualizers = new ArrayList<DomainContextualizer>(1);
		this.domainStartedListeners = new ArrayList<DomainStartedListener>(1);
		
		//Register for events
		int cb1 = connection.domainEventRegister(this);
		callbackIds.add(cb1);
		
		LOGGER.info("Received callbackid: " + cb1);
	}
	
	/**
	 * Add a domaincontextualizer (with inherit VM-Domain) to signal when events are found
	 * @param domainContextualizer The new domaincontextualizer
	 */
	public void addListener(DomainContextualizer domainContextualizer) {
		domainContextualizers.add(domainContextualizer);
		LOGGER.info("Added listener for domain: " + domainContextualizer.getDomainName());
	}
	
	
	/**
	 * Add a domainStatedListener to signal when new domains are detected
	 * @param domainStatedListener The new domainStatedListener
	 */
	public void addDomainStartedListener(DomainStartedListener domainStatedListener) {
		domainStartedListeners.add(domainStatedListener);
		LOGGER.info("Added domainStartedListener");
	}
	
	
	/**
	 * Remove a domainStatedListener
	 * @param domainStatedListener The domainStatedListener to remove
	 */
	public void removeDomainStartedListener(DomainStartedListener domainStatedListener) {
		boolean result = domainStartedListeners.remove(domainStatedListener);
		if (result) {
			LOGGER.info("Removed domainStartedListener.");
		} else {
			LOGGER.warn("Failed to remove domainStartedListener");
		}
	}
	
	/**
	 * Remove a domaincontextualizer (with inherit VM-Domain) from the set to signal when events are found
	 * @param domainName The domaincontextualizer to remove
	 * @return True if the listerner was successfully removed, false otherwise
	 */
	public boolean removeListener(String domainName) {
		DomainContextualizer contextualizer = null;
		for (DomainContextualizer contextCandidate: domainContextualizers) {
			if (contextCandidate.watchesDomainName(domainName)) {
				contextualizer = contextCandidate;
				break;
			}
		}
		
		if (contextualizer != null) {
			return domainContextualizers.remove(contextualizer);
		}
		
		return false;
	}
	
	public void close() {
		//Unregister events
		for (int callbackIdentifier : callbackIds) {
			try {
				connection.domainEventDeregister(callbackIdentifier);
			} catch (LibvirtException e) {
				LOGGER.warn("Failed to deregister event, cbID: " + callbackIdentifier + " on LibVirt connection", e);
			}
		}
	}

	/*
	 * Called by libvirt to indicate a change in lifecycle
	 * @see eu.optimis.vc.libvirt.Connect.DomainEvent.LifecycleCallback#onLifecycleChange(eu.optimis.vc.libvirt.Connect, eu.optimis.vc.libvirt.Domain, eu.optimis.vc.libvirt.Connect.DomainEvent.LifecycleCallback.Event, int)
	 */
	@Override
	public void onLifecycleChange(Connect connect, Domain domain, Event event,
			int detail) {
		
		//TODO: change to logger.debug
		LOGGER.info("lifecycle change: " + domain + " " + event + " " + detail);

		
		String domainName;
		try {
			domainName = domain.getName();
		} catch (LibvirtException e) {
			LOGGER.info("No DomainContextualizer found for domain with unresolvable name.");
			LOGGER.info("Event ignored: Domain object seems broken");
			return;
		}
		
		
		/* Signal domainStartedListener that a new Domain has been created. This gives it a change
		 * to create a new DomainContextualizer for this domain if there isn't one already
		 */
		if ((event.equals(Event.DEFINED) || event.equals(Event.STARTED)) && detail == 0) {
			for (DomainStartedListener dListener: domainStartedListeners) {
				dListener.vmDomainCreated(domainName);
			}
		}
		
		
		//Fetch associated DomainContextualizer
		DomainContextualizer contextualizer = null;
		for (DomainContextualizer contextCandidate: domainContextualizers) {
			if (contextCandidate.watchesDomainName(domainName)) {
				contextualizer = contextCandidate;
				break;
			}
		}
		
		//If no DomainContextualizer is registered for this domain, return
		if (contextualizer == null) {
			try {
				LOGGER.info("No DomainContextualizer found for domain: " + domain.getName() + ", ignoring lifecycle event");
			} catch (LibvirtException e) {
				LOGGER.info("No DomainContextualizer found for domain with unresolvable name.");
			}
			return;
		}
		
		//These events are the same as used in Python (Hopefully!)
		if (event.equals(Event.RESUMED) && detail == 1) {
			contextualizer.vmDomainMigrationCompleted();
		} else if (event.equals(Event.SUSPENDED) && detail == 1) {
			contextualizer.vmDomainMigrationStarted();
		} else if (event.equals(Event.STARTED) && detail == 0) {
			contextualizer.vmDomainStarted();
		}  else if (event.equals(Event.STOPPED) && detail == 0) {
			contextualizer.vmDomainStopped();
		} 
	}
}
