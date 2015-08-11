package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name ="base-dependency")
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseDependency {

	@XmlAttribute(name="OS")
	private String os;

	public String getOs() {
		return os;
	}

//	@XmlAttribute
	public void setOs(String os) {
		this.os = os;
	}
	
	
	
	
}
