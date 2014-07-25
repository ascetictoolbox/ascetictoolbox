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

package eu.ascetic.paas.slam.poc.impl.slaparser;

import java.util.ArrayList;
import java.util.List;



public class AsceticSlaTemplate {

	private List<AsceticVirtualSystem> virtualSystems;

	public AsceticSlaTemplate() {
		virtualSystems = new ArrayList<AsceticVirtualSystem>();
	}

	public List<AsceticVirtualSystem> getVirtualSystems() {
		return virtualSystems;
	}

	
	public void setVirtualSystems(List<AsceticVirtualSystem> virtualSystems) {
		this.virtualSystems = virtualSystems;
	}

	public void addVirtualSystem(AsceticVirtualSystem vs) {
		virtualSystems.add(vs);
	}

	public void removeVirtualSystem(AsceticVirtualSystem vs) {
		virtualSystems.remove(vs);
	}

	public AsceticVirtualSystem getVirtualSystem(String id) {
		for (AsceticVirtualSystem cvs : virtualSystems) {
			if (id.equals(cvs.getId()))
				return cvs;
		}
		return null;
	}
	
	public String toString() {
		String s = "\nVirtual system list: ";
		for (AsceticVirtualSystem vs : virtualSystems)
			s += ("\n" + vs);
		return s;
	}
}
