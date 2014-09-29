package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Jaxb Pojo object that represents an entry in the Provider Registry database
 * 
 */
//XML Annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="provider", namespace=PROVIDER_REGISTRY_NAMESPACE)
// JPA Annotations:
@Entity
@Table(name="providers")
@NamedQueries( { 
  @NamedQuery(name="Provider.findAll", query="SELECT p FROM Provider p")
} )
public class Provider {
	@XmlAttribute
	private String href;
	@XmlElement(name="id", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private int id;
	@XmlElement(name="name", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String name;
	@XmlElement(name="endpoint", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String endpoint;
	@XmlElement(name="link", namespace = PROVIDER_REGISTRY_NAMESPACE)
	private List<Link> links;

	@Transient
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
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
}
