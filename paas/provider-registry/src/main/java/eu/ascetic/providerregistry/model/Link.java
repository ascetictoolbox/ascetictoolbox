package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO that represents the link object of the Provider Registry XML
 * @author David Garcia Perez - ATOS
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "link", namespace = PROVIDER_REGISTRY_NAMESPACE)
public class Link {
	@XmlAttribute
	private String rel;
	@XmlAttribute
	private String href;
	@XmlAttribute
	private String type;
	
	public Link() {};
	
	public Link(String rel, String href, String type) {
		this.rel = rel;
		this.href = href;
		this.type = type;
	}
	
	/**
	 * @return the relative path to this link
	 */
	public String getRel() {
		return rel;
	}
	/**
	 * Sets the relative path for this link
	 * @param rel
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}
	
	/**
	 * @return the URL for this link
	 */
	public String getHref() {
		return href;
	}
	/**
	 * @param href Sets the URL for this link
	 */
	public void setHref(String href) {
		this.href = href;
	}
	
	/**
	 * Indicates the type of data that it is comming back from this link
	 * @return the http type of data
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type of data that is going to be returned or accepted by this link
	 */
	public void setType(String type) {
		this.type = type;
	}
}

