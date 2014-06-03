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
import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.Core.DomainContextualizer;
import eu.ascetic.vmc.api.Core.SystemCall;
import eu.ascetic.vmc.api.Core.SystemCallException;
import eu.ascetic.vmc.libvirt.Connect;
import eu.ascetic.vmc.libvirt.Domain;
import eu.ascetic.vmc.libvirt.LibvirtException;

/**
 * Control bridge wrapping the calls to libvirt and/or system processes
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public class LibvirtControlBridge implements LibvirtControlBridgeMBean {
	
	//4 should be VIR_DOMAIN_DEVICE_MODIFY_FORCE
	private static final int VIR_DOMAIN_DEVICE_MODIFY_FORCE = 4;

	private static final int THREAD_SLEEP_TIME = 500;

	private static final int STRING_BUILDER_CAPACITY = 100;

	protected static final Logger LOGGER = Logger
			.getLogger(LibvirtControlBridge.class);

	private Connect connection;
	
	/**
	 * Create a new control bridge for a specific Libvirt connection
	 * @param connection The connection to use when sending commands
	 */
	public LibvirtControlBridge(Connect connection) {
		this.connection = connection;
		LOGGER.info("Created ControlBridge");
	}

	/**
	 * Attach an iso to a domain
	 * @param domainName The name of the Domain
	 * @param interfaceName The interface name to attach
	 * @throws LibvirtException Thrown if connecting or attach fails
	 */
	@Override
	public void attachIso(String domainName, String isoPath, String interfaceName) throws LibvirtException {

		Domain domain = connection.domainLookupByName(domainName);

		LOGGER.info("Running detachIso on domainName: " + domainName + ", isoPath: " + isoPath +  "interfaceName: " + interfaceName);
		
		//Xen doesn't work with libvirt attachDevice
		if (!isXenRunning(domain)) {
			String isoXML = getIsoXML(isoPath, interfaceName, true);
			domain.attachDevice(isoXML);
		} else {
			//Hack for Xen
			String workingDir = System.getProperty("user.dir");
			SystemCall systemCall = new SystemCall(workingDir);
			
			String commandName = "xm";
			ArrayList<String> arguments = new ArrayList<String>();
 
			arguments.add("block-attach");
			arguments.add(domainName);
			arguments.add("file:" + isoPath);
			//arguments.add(interfaceName + ":cdrom"); //FIXME
			//FIXME: This needs to be different for XEN PV vs HVM images e.g. "xvda:cdrom"
			arguments.add("/dev/" + interfaceName);
			arguments.add("r");
			
			// Executed command looks like so:
			// xm block-attach deb-squeeze.qcow2.hvm file:/mnt/glusterfs-nfs/images/user-images/django/recontext.iso hdd:cdrom r
			try {
				systemCall.runCommand(commandName, arguments);

				//While not done executing
				while (systemCall.getReturnValue() == -1) {
					try {
						Thread.sleep(THREAD_SLEEP_TIME);
					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			} catch (SystemCallException e) {
				LOGGER.warn("Exception trying to run xen attach command", e);
			}

			if (systemCall.getReturnValue() != 0) {
				LOGGER.warn("Xen attach command returned 'FAIL'");
			}
		}
	}
	
	/**
	 * Detach an iso from a domain
	 * @param domainName The name of the Domain
	 * @param interfaceName The interface name to detach
	 * @throws LibvirtException Thrown if connecting or detaching fails
	 */
	@Override
	public void detachIso(String domainName, String interfaceName) throws LibvirtException {

		Domain domain = connection.domainLookupByName(domainName);
		
		LOGGER.info("Running detachIso on domainName: " + domainName + ", interfaceName: " + interfaceName);
		
		//Xen doesn't work with libvirt attachDevice
		if (!isXenRunning(domain)) {
			String isoXML = getIsoXML(null, interfaceName, false);
			domain.updateDeviceFlags(isoXML, VIR_DOMAIN_DEVICE_MODIFY_FORCE);
		} else {
			//Hack for Xen
			String workingDir = System.getProperty("user.dir");
			SystemCall systemCall = new SystemCall(workingDir);
			
			String commandName = "xm";
			ArrayList<String> arguments = new ArrayList<String>();
			
			arguments.add("block-detach");
			arguments.add(domainName);
			arguments.add(interfaceName);
			arguments.add("--force");
			
			// Executed command looks like so:
			// xm block-detach deb-squeeze.qcow2.hvm hdd --force
			try {
				systemCall.runCommand(commandName, arguments);

				//While not done executing
				while (systemCall.getReturnValue() == -1) {
					try {
						Thread.sleep(THREAD_SLEEP_TIME);
					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			} catch (SystemCallException e) {
				LOGGER.warn("Exception trying to run xen detach command", e);
			}

			if (systemCall.getReturnValue() != 0) {
				LOGGER.warn("Xen detach command returned 'FAIL'");
			}
		}
	}

	/**
	 * Returns true if Xen is used as hypervisor, false otherwise
	 * @param domain 
	 * @throws LibvirtException If failing to look capabilities up
	 */
	private boolean isXenRunning(Domain domain) throws LibvirtException {
		String domXML = domain.getXMLDesc(0);
		String firstLine = domXML.substring(domXML.indexOf('<'), domXML.indexOf('>'));
		return firstLine.contains("xen");
	}

	/*
	 * Construct an XML device representation for attaching/detaching
	 * @param isoPath The iso file path to attach (may be null when detaching)
	 * @param interfaceName The name of the interface where the ISO should be mounted
	 * @param attaching True when attaching, false when detaching
	 */
	private String getIsoXML(String isoPath, String interFaceName, boolean attaching) {
		StringBuilder xml = new StringBuilder(STRING_BUILDER_CAPACITY);
		xml.append("<disk type='file' device='cdrom'>");
		xml.append("<target dev='" + interFaceName + "'/>");
		if (attaching) {
			xml.append("<source file='" + isoPath + "'/>");
		}
		xml.append("<readonly/>");
		xml.append("</disk>");
		
		LOGGER.info("Using XML: " + xml.toString());
		return xml.toString();
	}
	
	/**
	 * Get the existing iso file path of a device mounted at an interface
	 * @param domainName Domain to query
	 * @param interfaceName The name of the interface of interest 
	 */	
	@Override
	public String getExistingIsoPath(String domainName, String interfaceName) throws LibvirtException {
		Domain domain = connection.domainLookupByName(domainName);
		String xmlDesc = domain.getXMLDesc(0);
		LOGGER.debug("Got XML: " +xmlDesc);
		return this.parseXML(xmlDesc);
	}
		
	private String parseXML(String xmlDesc) {
		
		String deviceBlock = xmlDesc.substring(xmlDesc.indexOf("<devices>"), xmlDesc.indexOf("</devices>"));
		int startIndex = 0;
		while (true) {
			int diskStartIndex = deviceBlock.indexOf("<disk", startIndex);
			int diskEndIndex = deviceBlock.indexOf("</disk>", diskStartIndex);

			String diskBlock = deviceBlock.substring(diskStartIndex, diskEndIndex);

			LOGGER.debug("diskBlock: " +diskBlock);
			//Check for correct device
			if (diskBlock.contains(DomainContextualizer.CONTEXT_INTERFACE_NAME)) {
				int sourceIndex = diskBlock.indexOf("<source");
				LOGGER.debug("sourceIndex: " + sourceIndex);
				String fileBlock = diskBlock.substring(sourceIndex, diskBlock.indexOf("/>", sourceIndex));
				LOGGER.debug("fileBlock: " +diskBlock);
				String fileName = fileBlock.substring(fileBlock.indexOf('\'') + 1, fileBlock.lastIndexOf('\''));
				LOGGER.debug("fileName: " +diskBlock);
				return fileName;
			} else {
				LOGGER.debug("Diskblock doesn't contain: " + DomainContextualizer.RECONTEXT_INTERFACE_NAME);
			}
			
			int newStartIndex = deviceBlock.indexOf("</disk>", startIndex);
			if (newStartIndex == startIndex) {
				LOGGER.debug("No new startIndex found, aborting");
				return null;
			} else {
				startIndex = newStartIndex;
				LOGGER.debug("New startIndex found: " + startIndex);
			}
			
		}
	}
	
	public static void main(String[] args) {
		LibvirtControlBridge bridge = new LibvirtControlBridge(null);
		String xml = "<domain type='xen' id='385'> <name>deb-squeeze.qcow2.hvm</name> <uuid>4ca957be-7f57-15b5-c37b-ab16faebf81d</uuid> <memory unit='KiB'>1048576</memory> <currentMemory unit='KiB'>1048576</currentMemory> <vcpu placement='static'>1</vcpu> <os> <type>hvm</type> <loader>/usr/lib64/xen-4.0/boot/hvmloader</loader> <boot dev='hd'/> <boot dev='cdrom'/> </os> <features> <acpi/> <apic/> <pae/> <hap/> </features> <clock offset='variable' adjustment='0' basis='localtime'> <timer name='hpet' present='no'/> </clock> <on_poweroff>destroy</on_poweroff> <on_reboot>restart</on_reboot> <on_crash>restart</on_crash> <devices> <emulator>/usr/lib64/xen-4.0/bin/qemu-dm</emulator> <disk type='file' device='disk'> <driver name='tap' type='qcow2'/> <source file='/mnt/glusterfs-nfs/images/user-images/django/deb-squeeze.qcow2.img'/> <target dev='hda' bus='ide'/> </disk> <disk type='file' device='cdrom'> <driver name='file'/> <source file='/mnt/glusterfs-nfs/images/user-images/django/context.iso'/> <target dev='hdc' bus='ide'/> <readonly/> </disk> <interface type='bridge'> <mac address='00:16:0a:0a:0a:0a'/> <source bridge='xenbr0'/> <script path='/etc/xen/scripts/vif-bridge'/> <target dev='vif385.0'/> <model type='e1000'/> </interface> <serial type='pty'> <source path='/dev/pts/11'/> <target port='0'/> </serial> <console type='pty' tty='/dev/pts/11'> <source path='/dev/pts/11'/> <target type='serial' port='0'/> </console> <input type='tablet' bus='usb'/> <input type='mouse' bus='ps2'/> <graphics type='vnc' port='5900' autoport='yes' listen='0.0.0.0' keymap='en-gb'> <listen type='address' address='0.0.0.0'/> </graphics> </devices> </domain>"; 
		String result = bridge.parseXML(xml);
		LOGGER.debug("Got result: " + result);
	}
}
