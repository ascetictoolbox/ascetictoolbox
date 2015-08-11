package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name ="software_install")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoftwareInstall {

	@XmlAttribute(name="name")
	private String name;
	
	@XmlAttribute(name="chefURI")
	private String chefUri;

	public String getName() {
		return name;
	}

//	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	public String getChefUri() {
		return chefUri;
	}

//	@XmlAttribute
	public void setChefUri(String chefUri) {
		this.chefUri = chefUri;
	}
	
	
}
