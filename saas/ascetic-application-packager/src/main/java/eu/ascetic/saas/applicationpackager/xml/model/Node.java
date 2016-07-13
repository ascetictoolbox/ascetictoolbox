package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
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
 * This class implements a node node in a XML file
 *
 */

@XmlRootElement(name ="node")
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {
	
	/** The type. */
	@XmlAttribute(name="type")
	private String type;
	
	/** The name. */
	@XmlAttribute(name="name")
	private String name;
	
	/** The min num core. */
	@XmlAttribute(name="minNumCore")
	private int minNumCore;
	
	/** The max num core. */
	@XmlAttribute(name="maxNumCore")
	private int maxNumCore;
	
	/** The pref num core. */
	@XmlAttribute(name="prefNumCore")
	private int prefNumCore;
	
	/** The min cpu freq. */
	@XmlAttribute(name="minCPUfreq")
	private String minCpuFreq;
	
	/** The max cpu freq. */
	@XmlAttribute(name="maxCPUfreq")
	private String maxCpuFreq;
	
	/** The pref cpu freq. */
	@XmlAttribute(name="prefCPUfreq")
	private String prefCpuFreq;
	
	/** The min mem size. */
	@XmlAttribute(name="minMemSize")
	private String minMemSize;
	
	/** The max mem size. */
	@XmlAttribute(name="maxMemSize")
	private String maxMemSize;
	
	/** The pref mem size. */
	@XmlAttribute(name="prefMemSize")
	private String prefMemSize;
	
	/** The min disk size. */
	@XmlAttribute(name="minDiskSize")
	private String minDiskSize;
	
	/** The max disk size. */
	@XmlAttribute(name="maxDiskSize")
	private String maxDiskSize;
	
	/** The pref disk size. */
	@XmlAttribute(name="prefDiskSize")
	private String prefDiskSize;
	
	/** The min instance. */
	@XmlAttribute(name="minInstance")
	private String minInstance;
	
	/** The max instance. */
	@XmlAttribute(name="maxInstance")
	private String maxInstance;
	
	/** The pref instance. */
	@XmlAttribute(name="prefInstance")
	private String prefInstance;
	
	/** The base dependency. */
	@XmlElement(name="base-dependency")
    private BaseDependency baseDependency;
	
	/** The software installs. */
	@XmlElement(name="software_install")
    private ArrayList<SoftwareInstall> softwareInstalls;

	/** The vm sla info. */
	@XmlElement(name="vmSLAInfo")
    private VmSlaInfo vmSLAInfo;
	
	/** The vm adaptation rules. */
	@XmlElement(name="vmAdaptationRules")
    private VmAdaptationRules vmAdaptationRules;
	
	
	/**
	 * Gets the vm sla info.
	 *
	 * @return the vm sla info
	 */
	public VmSlaInfo getVmSLAInfo() {
		return vmSLAInfo;
	}

	/**
	 * Sets the vm sla info.
	 *
	 * @param vmSLAInfo the new vm sla info
	 */
	public void setVmSLAInfo(VmSlaInfo vmSLAInfo) {
		this.vmSLAInfo = vmSLAInfo;
	}

	/**
	 * Gets the vm adaptation rules.
	 *
	 * @return the vm adaptation rules
	 */
	public VmAdaptationRules getVmAdaptationRules() {
		return vmAdaptationRules;
	}

	/**
	 * Sets the vm adaptation rules.
	 *
	 * @param vmAdaptationRules the new vm adaptation rules
	 */
	public void setVmAdaptationRules(VmAdaptationRules vmAdaptationRules) {
		this.vmAdaptationRules = vmAdaptationRules;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

//	@XmlAttribute
	/**
 * Sets the type.
 *
 * @param type the new type
 */
public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

//	@XmlAttribute
	/**
 * Sets the name.
 *
 * @param name the new name
 */
public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the min instance.
	 *
	 * @return the min instance
	 */
	public String getMinInstance() {
		return minInstance;
	}

//	@XmlAttribute
	/**
 * Sets the min instance.
 *
 * @param minInstance the new min instance
 */
public void setMinInstance(String minInstance) {
		this.minInstance = minInstance;
	}

	/**
	 * Gets the max instance.
	 *
	 * @return the max instance
	 */
	public String getMaxInstance() {
		return maxInstance;
	}

//	@XmlAttribute
	/**
 * Sets the max instance.
 *
 * @param maxInstance the new max instance
 */
public void setMaxInstance(String maxInstance) {
		this.maxInstance = maxInstance;
	}
	
	/**
	 * Gets the software installs.
	 *
	 * @return the software installs
	 */
	public ArrayList<SoftwareInstall> getSoftwareInstalls() {
		return softwareInstalls;
	}
	
	/**
	 * Sets the software installs.
	 *
	 * @param softwareInstalls the new software installs
	 */
	public void setSoftwareInstalls(ArrayList<SoftwareInstall> softwareInstalls) {
		this.softwareInstalls = softwareInstalls;
	}
	
	/**
	 * Adds the software install.
	 *
	 * @param softwareInstall the software install
	 */
	public void addSoftwareInstall(SoftwareInstall softwareInstall) {
		if(softwareInstalls == null) softwareInstalls = new ArrayList<SoftwareInstall>();
		softwareInstalls.add(softwareInstall);
	}
	
	/**
	 * Gets the base dependency.
	 *
	 * @return the base dependency
	 */
	public BaseDependency getBaseDependency() {
		return baseDependency;
	}
	
	/**
	 * Sets the base dependency.
	 *
	 * @param baseDependency the new base dependency
	 */
	public void setBaseDependency(BaseDependency baseDependency) {
		this.baseDependency = baseDependency;
	}

	/**
	 * Gets the min num core.
	 *
	 * @return the min num core
	 */
	public int getMinNumCore() {
		return minNumCore;
	}

	/**
	 * Sets the min num core.
	 *
	 * @param minNumCore the new min num core
	 */
	public void setMinNumCore(int minNumCore) {
		this.minNumCore = minNumCore;
	}

	/**
	 * Gets the max num core.
	 *
	 * @return the max num core
	 */
	public int getMaxNumCore() {
		return maxNumCore;
	}

	/**
	 * Sets the max num core.
	 *
	 * @param maxNumCore the new max num core
	 */
	public void setMaxNumCore(int maxNumCore) {
		this.maxNumCore = maxNumCore;
	}

	/**
	 * Gets the pref num core.
	 *
	 * @return the pref num core
	 */
	public int getPrefNumCore() {
		return prefNumCore;
	}

	/**
	 * Sets the pref num core.
	 *
	 * @param prefNumCore the new pref num core
	 */
	public void setPrefNumCore(int prefNumCore) {
		this.prefNumCore = prefNumCore;
	}

	/**
	 * Gets the min cpu freq.
	 *
	 * @return the min cpu freq
	 */
	public String getMinCpuFreq() {
		return minCpuFreq;
	}

	/**
	 * Sets the min cpu freq.
	 *
	 * @param minCpuFreq the new min cpu freq
	 */
	public void setMinCpuFreq(String minCpuFreq) {
		this.minCpuFreq = minCpuFreq;
	}

	/**
	 * Gets the max cpu freq.
	 *
	 * @return the max cpu freq
	 */
	public String getMaxCpuFreq() {
		return maxCpuFreq;
	}

	/**
	 * Sets the max cpu freq.
	 *
	 * @param maxCpuFreq the new max cpu freq
	 */
	public void setMaxCpuFreq(String maxCpuFreq) {
		this.maxCpuFreq = maxCpuFreq;
	}

	/**
	 * Gets the pref cpu freq.
	 *
	 * @return the pref cpu freq
	 */
	public String getPrefCpuFreq() {
		return prefCpuFreq;
	}

	/**
	 * Sets the pref cpu freq.
	 *
	 * @param prefCpuFreq the new pref cpu freq
	 */
	public void setPrefCpuFreq(String prefCpuFreq) {
		this.prefCpuFreq = prefCpuFreq;
	}

	/**
	 * Gets the min mem size.
	 *
	 * @return the min mem size
	 */
	public String getMinMemSize() {
		return minMemSize;
	}

	/**
	 * Sets the min mem size.
	 *
	 * @param minMemSize the new min mem size
	 */
	public void setMinMemSize(String minMemSize) {
		this.minMemSize = minMemSize;
	}

	/**
	 * Gets the max mem size.
	 *
	 * @return the max mem size
	 */
	public String getMaxMemSize() {
		return maxMemSize;
	}

	/**
	 * Sets the max mem size.
	 *
	 * @param maxMemSize the new max mem size
	 */
	public void setMaxMemSize(String maxMemSize) {
		this.maxMemSize = maxMemSize;
	}

	/**
	 * Gets the pref mem size.
	 *
	 * @return the pref mem size
	 */
	public String getPrefMemSize() {
		return prefMemSize;
	}

	/**
	 * Sets the pref mem size.
	 *
	 * @param prefMemSize the new pref mem size
	 */
	public void setPrefMemSize(String prefMemSize) {
		this.prefMemSize = prefMemSize;
	}

	/**
	 * Gets the min disk size.
	 *
	 * @return the min disk size
	 */
	public String getMinDiskSize() {
		return minDiskSize;
	}

	/**
	 * Sets the min disk size.
	 *
	 * @param minDiskSize the new min disk size
	 */
	public void setMinDiskSize(String minDiskSize) {
		this.minDiskSize = minDiskSize;
	}

	/**
	 * Gets the max disk size.
	 *
	 * @return the max disk size
	 */
	public String getMaxDiskSize() {
		return maxDiskSize;
	}

	/**
	 * Sets the max disk size.
	 *
	 * @param maxDiskSize the new max disk size
	 */
	public void setMaxDiskSize(String maxDiskSize) {
		this.maxDiskSize = maxDiskSize;
	}

	/**
	 * Gets the pref disk size.
	 *
	 * @return the pref disk size
	 */
	public String getPrefDiskSize() {
		return prefDiskSize;
	}

	/**
	 * Sets the pref disk size.
	 *
	 * @param prefDiskSize the new pref disk size
	 */
	public void setPrefDiskSize(String prefDiskSize) {
		this.prefDiskSize = prefDiskSize;
	}

	/**
	 * Gets the pref instance.
	 *
	 * @return the pref instance
	 */
	public String getPrefInstance() {
		return prefInstance;
	}

	/**
	 * Sets the pref instance.
	 *
	 * @param prefMaxInstance the new pref instance
	 */
	public void setPrefInstance(String prefInstance) {
		this.prefInstance = prefInstance;
	}
}
