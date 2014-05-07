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
package eu.ascetic.vmc.api.DataModel;

import java.util.HashMap;
import java.util.Map;

import eu.ascetic.vmc.api.DataModel.ContextDataTypes.EndPoint;
import eu.ascetic.vmc.api.DataModel.ContextDataTypes.LicenseToken;
import eu.ascetic.vmc.api.DataModel.ContextDataTypes.OperatingSystem;
import eu.ascetic.vmc.api.DataModel.ContextDataTypes.SoftwareDependency;
import eu.ascetic.vmc.api.DataModel.Image.HardDisk;
import eu.ascetic.vmc.api.DataModel.Image.Iso;

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
	private Map<String, LicenseToken> licenseTokens;
	private boolean hasVPNKey = false;
	private boolean hasSSHKey = true;
	private boolean hasBTKey = false;
	private boolean hasIPS = false;
	private boolean hasDMKey = false;

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
		licenseTokens = new HashMap<String, LicenseToken>();
	}

	/**
	 * @return the componentId
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * @param componentId the componentId to set
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
	 * @return the licenseTokens
	 */
	public Map<String, LicenseToken> getLicenseTokens() {
		return licenseTokens;
	}

	/**
	 * @param licenseTokens
	 *            the licenseTokens to set
	 */
	public void setLicenseTokens(Map<String, LicenseToken> licenseTokens) {
		this.licenseTokens = licenseTokens;
	}

	/**
	 * @return the hasVPNKey
	 */
	public boolean isHasVPNKey() {
		return hasVPNKey;
	}

	/**
	 * @param hasVPNKey
	 *            the hasVPNKey to set
	 */
	public void setHasVPNKey(boolean hasVPNKey) {
		this.hasVPNKey = hasVPNKey;
	}

	/**
	 * @return the hasSSHKey
	 */
	public boolean isHasSSHKey() {
		return hasSSHKey;
	}

	/**
	 * @param hasSSHKey
	 *            the hasSSHKey to set
	 */
	public void setHasSSHKey(boolean hasSSHKey) {
		this.hasSSHKey = hasSSHKey;
	}

	/**
	 * @return the hasBTKey
	 */
	public boolean isHasBTKey() {
		return hasBTKey;
	}

	/**
	 * @param hasBTKey the hasBTKey to set
	 */
	public void setHasBTKey(boolean hasBTKey) {
		this.hasBTKey = hasBTKey;
	}

	/**
	 * @return the hasIPS
	 */
	public boolean isHasIPS() {
		return hasIPS;
	}

	/**
	 * @param hasIPS the hasIPS to set
	 */
	public void setHasIPS(boolean hasIPS) {
		this.hasIPS = hasIPS;
	}

	/**
	 * @return the hasDMKey
	 */
	public boolean isHasDMKey() {
		return hasDMKey;
	}

	/**
	 * @param hasDMKey the hasDMKey to set
	 */
	public void setHasDMKey(boolean hasDMKey) {
		this.hasDMKey = hasDMKey;
	}
}
