package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name ="node")
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {
	
	@XmlAttribute(name="type")
	private String type;
	
	@XmlAttribute(name="name")
	private String name;
	
	@XmlAttribute(name="numCore")
	private int numCore;
	
	@XmlAttribute(name="CPUfreq")
	private String cpuFreq;
	
	@XmlAttribute(name="MemSize")
	private String memSize;
	
	@XmlAttribute(name="DiskSize")
	private String diskSize;
	
	@XmlAttribute(name="minInstance")
	private String minInstance;
	
	@XmlAttribute(name="maxInstance")
	private String maxInstance;
	
	@XmlElement(name="base-dependency")
    private BaseDependency baseDependency;
	
	@XmlElement(name="software_install")
    private ArrayList<SoftwareInstall> softwareInstalls;

	public String getType() {
		return type;
	}

//	@XmlAttribute
	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

//	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	public int getNumCore() {
		return numCore;
	}

//	@XmlAttribute
	public void setNumCore(int numCore) {
		this.numCore = numCore;
	}

	public String getCpuFreq() {
		return cpuFreq;
	}

//	@XmlAttribute
	public void setCpuFreq(String cpuFreq) {
		this.cpuFreq = cpuFreq;
	}

	public String getMemSize() {
		return memSize;
	}

//	@XmlAttribute
	public void setMemSize(String memSize) {
		this.memSize = memSize;
	}

	public String getDiskSize() {
		return diskSize;
	}

//	@XmlAttribute
	public void setDiskSize(String diskSize) {
		this.diskSize = diskSize;
	}

	public String getMinInstance() {
		return minInstance;
	}

//	@XmlAttribute
	public void setMinInstance(String minInstance) {
		this.minInstance = minInstance;
	}

	public String getMaxInstance() {
		return maxInstance;
	}

//	@XmlAttribute
	public void setMaxInstance(String maxInstance) {
		this.maxInstance = maxInstance;
	}
	
	public ArrayList<SoftwareInstall> getSoftwareInstalls() {
		return softwareInstalls;
	}
	
	public void setSoftwareInstalls(ArrayList<SoftwareInstall> softwareInstalls) {
		this.softwareInstalls = softwareInstalls;
	}
	
	public void addSoftwareInstall(SoftwareInstall softwareInstall) {
		if(softwareInstalls == null) softwareInstalls = new ArrayList<SoftwareInstall>();
		softwareInstalls.add(softwareInstall);
	}
	
	public BaseDependency getBaseDependency() {
		return baseDependency;
	}
	
	public void setBaseDependency(BaseDependency baseDependency) {
		this.baseDependency = baseDependency;
	}

}
