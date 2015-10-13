package eu.ascetic.saas.applicationpackager.xml.model;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class implements a cpuSpeed node in a XML file
 *
 */
/**
 * The Class CpuSpeed.
 */
public class CpuSpeed {

	/** The speed. */
	private String speed;
	
	/** The allocation units. */
	private String allocationUnits;
	
	/**
	 * Instantiates a new cpu speed.
	 */
	public CpuSpeed(){
		
	}

	/**
	 * Gets the speed.
	 *
	 * @return the speed
	 */
	public String getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed.
	 *
	 * @param speed the new speed
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
	}

	/**
	 * Gets the allocation units.
	 *
	 * @return the allocation units
	 */
	public String getAllocationUnits() {
		return allocationUnits;
	}

	/**
	 * Sets the allocation units.
	 *
	 * @param allocationUnits the new allocation units
	 */
	public void setAllocationUnits(String allocationUnits) {
		this.allocationUnits = allocationUnits;
	}
	
	
}
