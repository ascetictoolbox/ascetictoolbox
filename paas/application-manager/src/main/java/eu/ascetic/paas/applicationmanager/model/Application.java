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
 * POJO Representing an application at Application Manager level
 * @author David Garcia Perez - Atos
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "application", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Application {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "state", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String state;
	@XmlElement(name = "deployment-plan-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String deploymentPlanId;
	//TODO Add OVF description...
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDeploymentPlanId() {
		return deploymentPlanId;
	}
	public void setDeploymentPlanId(String deploymentPlanId) {
		this.deploymentPlanId = deploymentPlanId;
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
