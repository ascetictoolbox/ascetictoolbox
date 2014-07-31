/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.iaas.slamanager.poc.manager.resource;

import java.util.HashMap;

import org.apache.xmlbeans.XmlException;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;

public class OvfResourceParser {

	private OvfDefinition ovfDefinition;

	public OvfResourceParser(String ovfFile) {
		XmlBeanEnvelopeDocument xmlBeanEnvelopeDocument=null;
		try {
			xmlBeanEnvelopeDocument = XmlBeanEnvelopeDocument.Factory
					.parse(ovfFile);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ovfDefinition = OvfDefinition.Factory.newInstance(xmlBeanEnvelopeDocument);
	}

	public VirtualSystem[] getVirtualSystems() {
		return ovfDefinition.getVirtualSystemCollection().getVirtualSystemArray();
	}

	public Disk[] getSharedDisks() {
		DiskSection discSection=ovfDefinition.getDiskSection();
		return discSection.getDiskArray();
	}

	public HashMap<String, Double> getVirtualSystemNeed(VirtualSystem vs) {
		VirtualHardwareSection hardware = vs.getVirtualHardwareSection();
			HashMap<String, Double> vsNeed = new HashMap<String, Double>();
			// init map
			try {
				vsNeed.put("cpu_speed", (double)hardware.getCPUSpeed());
			} catch (NullPointerException e) {
			}
			try {
				vsNeed.put("memory", (double)hardware.getMemorySize());
			} catch (NullPointerException e) {
			}
			try {
				vsNeed.put("vm_cores", (double)hardware.getNumberOfVirtualCPUs());
			} catch (NullPointerException e) {
			}
			return vsNeed;
	}
}
