package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java representation of the Collections ECO2Clouds XML
 * @author David Garcia Perez - AtoS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "collection", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Collection {
	@XmlAttribute
	private String href;
	@XmlElement(namespace = APPLICATION_MANAGER_NAMESPACE)
	private Items items;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private ArrayList<Link> links;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
	public Items getItems() {
		return items;
	}
	public void setItems(Items items) {
		this.items = items;
	}
	
	public ArrayList<Link> getLinks() {
		return links;
	}
	public void setLinks(ArrayList<Link> links) {
		this.links = links;
	}
	public void addLink(Link link) {
		if(links == null) links = new ArrayList<Link>();
		links.add(link);
	}
}

