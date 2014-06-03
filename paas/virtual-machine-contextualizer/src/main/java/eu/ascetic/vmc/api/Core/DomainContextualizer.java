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
package eu.ascetic.vmc.api.Core;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.Core.Libvirt.LibvirtControlBridge;
import eu.ascetic.vmc.api.DataModel.ContextData;
import eu.ascetic.vmc.api.DataModel.GlobalConfiguration;
import eu.ascetic.vmc.api.DataModel.VirtualMachine;
import eu.ascetic.vmc.api.DataModel.ContextDataTypes.EndPoint;
import eu.ascetic.vmc.api.DataModel.Image.Iso;
import eu.ascetic.vmc.api.IsoCreator.IsoImageCreation;
import eu.ascetic.vmc.libvirt.LibvirtException;
/**
 * Responds to events regarding the domain and manages the actual context changes
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public class DomainContextualizer implements DomainContextualizerMBean {

	protected static final Logger LOGGER = Logger
			.getLogger(DomainContextualizer.class);

	// FIXME: This should be taken from somewhere depending on the image type operated on
	public static final String CONTEXT_INTERFACE_NAME = "xvdc";
	public static final String RECONTEXT_INTERFACE_NAME = "xvdd";

	private String domainName;
	private VmcApi vmcApi;

	private LibvirtControlBridge libvirtControl;

	public DomainContextualizer(String domainName, VmcApi vmcApi, LibvirtControlBridge libvirtControl) {
		this.domainName = domainName;
		this.vmcApi = vmcApi;
		this.libvirtControl = libvirtControl;
	}

	@Override
	public void vmDomainStarted() {
		try {
		LOGGER.info("Got call to domainStarted for domainName: " + domainName);
		
		//Detach any existing recontext
		try {
			libvirtControl.detachIso(domainName, RECONTEXT_INTERFACE_NAME);
			LOGGER.info("Detached existing recontext info on start");
		} catch (LibvirtException e){
			LOGGER.info("Failed to detach existing recontext info");
		}
		
		//Create new recontext iso and attach it
		try {
			String oldIsoPath = libvirtControl.getExistingIsoPath(domainName, CONTEXT_INTERFACE_NAME);
			if (oldIsoPath == null) {
				LOGGER.warn("Aborting! Old ISO path could not be found in libvirt");
				return;
			}

			LOGGER.debug("oldIsoPath: " + oldIsoPath);
			String isoPath = createIso(oldIsoPath);
			LOGGER.debug("newIsoPath: " + isoPath);
			
			libvirtControl.attachIso(domainName, isoPath, RECONTEXT_INTERFACE_NAME);
			LOGGER.info("Attached new recontext info");
		} catch (LibvirtException e) {
			LOGGER.warn("Failed to attach recontext info.", e);
		} catch (IOException e) {
			LOGGER.warn("Failed to read/extract existing ISO.", e);
		} catch (SystemCallException e) {
			// TODO Auto-generated catch block
			LOGGER.warn("Failed to create Iso Image, SystemCall could not find binary", e);
		}
		
		} catch (RuntimeException r) {
			//TODO: this is just for debugging the component using JMX. This way, stuff like nullpointers are printed in logger before being thrown to JMX client
			LOGGER.error(r.getMessage(), r);
			throw r;
		}
	}

	@Override
	public void vmDomainStopped() {
		LOGGER.info("Got call to domainStopped for domainName: " + domainName);
		//Detach any existing recontext
		try {
			libvirtControl.detachIso(domainName, RECONTEXT_INTERFACE_NAME);
			LOGGER.info("Detached existing recontext info on stop");
		} catch (LibvirtException e){
			LOGGER.info("Failed to detach existing recontext info");
		}
	}

	@Override
	public void vmDomainMigrationStarted() {
		LOGGER.info("Got call to domainMigrationStarted for domainName: " + domainName);

		// TODO Remove existing context info from the domain
	}

	@Override
	public void vmDomainMigrationCompleted() {
		LOGGER.info("Got call to domainMigrationComplete for domainName: " + domainName);

		// TODO Construct context info and attach it to the domain
	}

	/**
	 * @returns true if the domainName sent as argument is the same as the domainName being managed by this DomainContextualizer
	 */
	public Boolean watchesDomainName(String domainName) {
		return this.domainName.equals(domainName);
	}

	public String getDomainName() {
		return this.domainName;
	}
	
	
	//TODO Add javadoc
	//Creates a recontext ISO and returns its path to be attached to later to a VM
	private String createIso(String existingIsoPath) throws IOException, SystemCallException {
		
		GlobalConfiguration configuration = vmcApi.getGlobalState().getConfiguration();
		
		LOGGER.debug("Exising iso path: " + existingIsoPath);
		
		//Construct for fetching data from an existing ISO image
		IsoImageCreation isoImageCreation = new IsoImageCreation(configuration, existingIsoPath);
		
		//Create Virtual Machine object
		//FIXME
		String componentId = "hardcoded_in_domContext";
		HashMap<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
		VirtualMachine virtualMachine = new VirtualMachine(componentId, 1);
		
		//Create EndPoint object and add to VM
		//TODO: Currently using the IP of the current host to differentiate the data.
		//TODO: Test and check that the address return is not "localhost"
		String localIpAddress = InetAddress.getLocalHost().getHostAddress();
		EndPoint endPoint = new EndPoint("recontext.test", localIpAddress);
		HashMap<String, EndPoint> endPoints = new HashMap<String, EndPoint>();
		endPoints.put("1", endPoint);
		
		virtualMachine.setEndPoints(endPoints);
		vms.put(componentId, virtualMachine);
		
		//Create contextData object and add VM
		ContextData contextData = new ContextData();
		contextData.setVirtualMachines(vms);
		
		//Generate the data for the ISO
		isoImageCreation.storeRecontextData(contextData, virtualMachine);
		
		//Create the ISO
		Iso recontextIso = isoImageCreation.create();

		return recontextIso.getUri();
	}
}
