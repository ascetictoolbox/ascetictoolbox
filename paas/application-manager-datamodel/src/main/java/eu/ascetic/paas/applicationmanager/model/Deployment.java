package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the deployment of an Application at PaaS level
 * @author David Garcia Perez - Atos
 */
//XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "deployment", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Deployment {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "status", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String status;
	@XmlElement(name = "price", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String price;
	@XmlElementWrapper(name = "vms", namespace = APPLICATION_MANAGER_NAMESPACE)
	@XmlElement(name = "vm", namespace = APPLICATION_MANAGER_NAMESPACE )
	private List<VM> vms;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
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
	
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	
	@Transient
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
	
	public List<VM> getVms() {
		return vms;
	}
	public void setVms(List<VM> vms) {
		this.vms = vms;
	}
	public void addVM(VM vm) {
		if(vms == null) vms = new ArrayList<VM>();
		vms.add(vm);
	}
}
