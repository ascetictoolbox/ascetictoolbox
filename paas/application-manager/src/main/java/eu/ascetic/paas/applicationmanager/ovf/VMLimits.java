package eu.ascetic.paas.applicationmanager.ovf;

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
 * POJO Object to pass the different limits of a VM between objects
 *
 */
public class VMLimits {
	private long lowerNumberOfVMs;
	private long upperNumberOfVMs;
	private int maxNumberCPUs = 0;
	private int minNumberCPUs = 0;
	
	public long getUpperNumberOfVMs() {
		return upperNumberOfVMs;
	}
	public void setUpperNumberOfVMs(long upperNumberOfVMs) {
		this.upperNumberOfVMs = upperNumberOfVMs;
	}
	
	public long getLowerNumberOfVMs() {
		return lowerNumberOfVMs;
	}
	public void setLowerNumberOfVMs(long lowerNumberOfVMs) {
		this.lowerNumberOfVMs = lowerNumberOfVMs;
	}
	
	public int getMaxNumberCPUs() {
		return maxNumberCPUs;
	}
	public void setMaxNumberCPUs(int maxNumberCPUs) {
		this.maxNumberCPUs = maxNumberCPUs;
	}
	
	public int getMinNumberCPUs() {
		return minNumberCPUs;
	}
	public void setMinNumberCPUs(int minNumberCPUs) {
		this.minNumberCPUs = minNumberCPUs;
	}
}
