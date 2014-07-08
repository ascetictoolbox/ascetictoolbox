package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO Representing an application at Application Manager level
 * @author David Garcia Perez - Atos
 */
// XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "application", namespace = APPLICATION_MANAGER_NAMESPACE)
// JPA annotations:
@Entity
@Table(name="applications")
@NamedQueries( { 
	@NamedQuery(name="Application.findAll", query="SELECT p FROM Application p")
} )
public class Application {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "name", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String name;
	@XmlElementWrapper(name = "deployments", namespace = APPLICATION_MANAGER_NAMESPACE)
	@XmlElement(name = "deployment", namespace = APPLICATION_MANAGER_NAMESPACE )
	private List<Deployment> deployments;
	//TODO Add OVF description...
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "application_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	@Transient
	public List<Deployment> getDeployments() {
		return deployments;
	}
	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}
	public void addDeployment(Deployment deployment) {
		if(deployments==null) deployments = new ArrayList<Deployment>();
		deployments.add(deployment);
	}
	
	@Transient
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
}
