package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jaxb Pojo object that represents an entry in the Provider Registry database
 * @author David Garcia Perez - Atos
 */
//XML Annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="provider", namespace=PROVIDER_REGISTRY_NAMESPACE)
// JPA Annotations:
@Entity
@Table(name="provider")
@NamedQueries( { 
  @NamedQuery(name="Provider.findAll", query="SELECT p FROM Provider p")
} )
public class Provider {
	@XmlElement(name="id", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private int id;
	@XmlElement(name="name", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String name;

	@XmlElement(name="endpoint", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String endpoint;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name = "endpoint", nullable = false)
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
