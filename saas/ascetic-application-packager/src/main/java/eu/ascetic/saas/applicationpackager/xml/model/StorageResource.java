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
 * This class implements a storageResource node in a XML file
 *
 */
/**
 * The Class Memory.
 */
public class StorageResource {

	/** The capacity. */
	private int capacity;
	
	/** The units. */
	private String units;
	
	/**
	 * Instantiates a new memory.
	 */
	public StorageResource(){
		
	}
	
	/**
	 * Gets the capacity.
	 *
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * Sets the capacity.
	 *
	 * @param capacity the new capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	/**
	 * Gets the units.
	 *
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * Sets the units.
	 *
	 * @param units the new units
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	
}
