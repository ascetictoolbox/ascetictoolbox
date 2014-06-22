package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root REST query element
 * @author David Garcia Perez - Atos
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "root", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Root {
	@XmlAttribute
	private String href;
	@XmlElement(name = "version", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String version;
	@XmlElement(name = "timestamp", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String timestamp;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	public void addLink(Link link) {
		if(links==null) links = new ArrayList<Link>();
		links.add(link);
	}
}
