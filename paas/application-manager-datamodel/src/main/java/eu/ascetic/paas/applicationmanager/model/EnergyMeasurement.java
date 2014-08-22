package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Basic XML representation for any Energy Measurement
 * @author David Garcia Perez - Atos
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "energy-measurement", namespace = APPLICATION_MANAGER_NAMESPACE)
public class EnergyMeasurement {
	@XmlAttribute
	private String href;
	@XmlElement(name = "value", namespace = APPLICATION_MANAGER_NAMESPACE)
	private Double value;
	@XmlElement(name = "description", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String description;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;

	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
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
