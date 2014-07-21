package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
//JPA annotations:
@Entity
@Table(name="deployments")
@NamedQueries( { 
	@NamedQuery(name="Deployment.findAll", query="SELECT p FROM Deployment p")
} )
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
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "deployment_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name = "status", nullable = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Transient
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

	@Column(name = "price", nullable = true)
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
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "deployment_id", referencedColumnName="deployment_id", nullable = true)
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
