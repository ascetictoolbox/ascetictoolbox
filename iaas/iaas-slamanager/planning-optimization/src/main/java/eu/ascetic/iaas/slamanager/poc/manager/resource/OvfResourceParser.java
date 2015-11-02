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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;

public class OvfResourceParser {

	private OvfDefinition ovfDefinition;

	public OvfResourceParser(String ovfFilePath) {
		try {
			String ovfString = FileUtils.readFileToString(new File(ovfFilePath));
			ovfDefinition = OvfDefinition.Factory.newInstance(ovfString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VirtualSystem[] getVirtualSystems() {
		return ovfDefinition.getVirtualSystemCollection().getVirtualSystemArray();
	}

	public Disk[] getDisks() {
		return ovfDefinition.getDiskSection().getDiskArray();
	}

	public long getDiskCapacity(String diskId) {
		long capacity = -1;
		for (Disk d : getDisks()) {
			if (d.getDiskId().equals(diskId)) {
				capacity = new Long(d.getCapacity()).longValue();
				break;
			}
		}
		return capacity;
	}

	public HashMap<String, Double> getVirtualSystemNeed(VirtualSystem vs) {
		VirtualHardwareSection vhs = vs.getVirtualHardwareSection();
		if (vhs != null) {
			HashMap<String, Double> vsNeed = new HashMap<String, Double>();
			// init map
			// TO DO set cpu speed
			vsNeed.put("cpu_speed", (double) -1);
			vsNeed.put("memory", (double) -1);
			vsNeed.put("vm_cores", (double) -1);
			vsNeed.put("disk_size", (double) -1);
			for( Item item : vhs.getItemArray()){
				switch(item.getResourceType()){
				case PROCESSOR:
					if(item.getResourceSubType()!=null && item.getResourceSubType().equals("cpuspeed")){
						vsNeed.put("cpu_speed", new Double(item.getReservation().doubleValue()));
					}
					else{
						vsNeed.put("vm_cores", new Double(item.getVirtualQuantity().doubleValue()));
					}
					break;
				case MEMORY:
					vsNeed.put("memory", new Double(item.getVirtualQuantity().doubleValue()));
					break;
				case DISK_DRIVE:
					for(String s : item.getHostResourceArray()){
						if(s.startsWith("ovf:/disk/")){
							String diskId=s.substring(10);
							vsNeed.put("disk_size",new Double(getDiskCapacity(diskId)));	
							break;
						}
					}
					break;
				}
			}
			return vsNeed;
		} else {
			return null;
		}
	}
}
