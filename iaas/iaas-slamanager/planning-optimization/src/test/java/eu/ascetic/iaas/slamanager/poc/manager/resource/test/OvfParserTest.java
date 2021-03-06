/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
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
package eu.ascetic.iaas.slamanager.poc.manager.resource.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.ascetic.iaas.slamanager.poc.manager.resource.OvfResourceParser;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;

public class OvfParserTest {

	private String ovfString;
	
	private OvfResourceParser ovfParser;

	private static final Logger log = Logger.getLogger(OvfParserTest.class);

	@Before
	public void setup() {

		try {
			ovfString = FileUtils.readFileToString(new File("src/test/resources/ovf/ascetic-ovf-example.ovf"));
			ovfParser=new OvfResourceParser("src/test/resources/ovf/ascetic-ovf-example.ovf");
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Parsed ovf:" + ovfString);
		log.info(ovfString);
	}

	@Test
	public void parseOvfTest() {
		OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance(ovfString);
		DiskSection diskSection = ovfDefinition.getDiskSection();
		
		for (VirtualSystem vs : ovfParser.getVirtualSystems()) {
			log.info("VM id: " + vs.getId());
			log.info("VM info: " + vs.getInfo());
			log.info("VM name: " + vs.getName());
			VirtualHardwareSection vhs = vs.getVirtualHardwareSection();
			for( Item item : vhs.getItemArray()){
				switch(item.getResourceType()){
				case PROCESSOR:
					if(item.getResourceSubType()!=null && item.getResourceSubType().equals("cpuspeed")){
						log.info("VM cpuSpeed: " + item.getReservation());
					}
					else{
					log.info("VM vcpu: " + item.getVirtualQuantity());
					}
					break;
				case MEMORY:
					log.info("VM memory: " + item.getVirtualQuantity());
					break;
				case DISK_DRIVE:
					for(String s : item.getHostResourceArray()){
						if(s.startsWith("ovf:/disk/")){
							String diskId=s.substring(10);
							log.info("VM Disk ID: "+diskId);
							log.info("VM Disk capacity: "+ovfParser.getDiskCapacity(diskId));	
							break;
						}
					}
					break;
				}
			}
		}
	}
}
