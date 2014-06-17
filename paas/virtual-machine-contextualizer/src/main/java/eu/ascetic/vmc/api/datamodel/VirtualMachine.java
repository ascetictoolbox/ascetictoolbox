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
package eu.ascetic.vmc.api.datamodel;

import java.util.HashMap;
import java.util.Map;

import eu.ascetic.vmc.api.datamodel.contextdatatypes.EndPoint;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.OperatingSystem;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.SoftwareDependency;
import eu.ascetic.vmc.api.datamodel.image.HardDisk;
import eu.ascetic.vmc.api.datamodel.image.Iso;

/**
 * Class for storing data related to a given virtual machine.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class VirtualMachine {

	private String componentId;
	private Map<String, HardDisk> hardDisks;
	private Map<String, Iso> isoImages;
	private int upperBound;
	private Map<String, EndPoint> endPoints;
	private OperatingSystem operatingSystem;
	private Map<String, SoftwareDependency> softwareDependencies;
	private boolean hasKey = true;

	/**
	 * Constructor creates a {@link HashMap} containers for context data related
	 * to a specific VM
	 * 
	 * @param upperBound
	 *            The upper bound on the number of virtual machine instances.
	 */
	public VirtualMachine(String id, int upperBound) {
		this.componentId = id;
		this.upperBound = upperBound;
		hardDisks = new HashMap<String, HardDisk>();
		isoImages = new HashMap<String, Iso>();
		endPoints = new HashMap<String, EndPoint>();
		operatingSystem = new OperatingSystem();
		softwareDependencies = new HashMap<String, SoftwareDependency>();
	}

	/**
	 * @return the componentId
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * @param componentId
	 *            the componentId to set
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * @return the hardDisks
	 */
	public Map<String, HardDisk> getHardDisks() {
		return hardDisks;
	}

	/**
	 * @param hardDisks
	 *            the hardDisks to set
	 */
	public void setHardDisks(Map<String, HardDisk> hardDisks) {
		this.hardDisks = hardDisks;
	}

	/**
	 * @return the isoImages
	 */
	public Map<String, Iso> getIsoImages() {
		return isoImages;
	}

	/**
	 * @param isoImages
	 *            the isoImages to set
	 */
	public void setIsoImages(Map<String, Iso> isoImages) {
		this.isoImages = isoImages;
	}

	/**
	 * @return the upperBound
	 */
	public int getUpperBound() {
		return upperBound;
	}

	/**
	 * @param upperBound
	 *            the upperBound to set
	 */
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * @return the endPoints
	 */
	public Map<String, EndPoint> getEndPoints() {
		return endPoints;
	}

	/**
	 * @param endPoints
	 *            the endPoints to set
	 */
	public void setEndPoints(Map<String, EndPoint> endPoints) {
		this.endPoints = endPoints;
	}

	/**
	 * @return the operatingSystem
	 */
	public OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * @param operatingSystem
	 *            the operatingSystem to set
	 */
	public void setOperatingSystem(OperatingSystem operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	/**
	 * @return the softwareDependencies
	 */
	public Map<String, SoftwareDependency> getSoftwareDependencies() {
		return softwareDependencies;
	}

	/**
	 * @param softwareDependencies
	 *            the softwareDependencies to set
	 */
	public void setSoftwareDependencies(
			Map<String, SoftwareDependency> softwareDependencies) {
		this.softwareDependencies = softwareDependencies;
	}

	/**
	 * @return the hasKey
	 */
	public boolean isHasKey() {
		return hasKey;
	}

	/**
	 * @param hasKey
	 *            the hasSSHKey to set
	 */
	public void setHasKey(boolean hasKey) {
		this.hasKey = hasKey;
	}
}
