package eu.ascetic.paas.slam.pac.applicationmanager.model;

import static eu.ascetic.paas.slam.pac.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * POJO Representing a VM at Application Manager level
 * 
 */

// XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vm", namespace = APPLICATION_MANAGER_NAMESPACE)
@Entity
@Table(name="vms")
@NamedQueries( { 
	@NamedQuery(name="VM.findAll", query="SELECT p FROM VM p")
} )
public class VM {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "ovf-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String ovfId;
	@XmlElement(name = "provider-vm-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerVmId;
	@XmlElement(name = "provider-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerId;
	@XmlElement(name = "sla-agreement", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String slaAgreement;
	@XmlElement(name = "status", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String status;
	@XmlElement(name = "ip", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String ip;
	@XmlElementWrapper(name = "images", namespace = APPLICATION_MANAGER_NAMESPACE)
	@XmlElement(name = "image", namespace = APPLICATION_MANAGER_NAMESPACE )
	private List<Image> images;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "vm_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Transient
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
	@Column(name = "ovf_id", nullable = true)
	public String getOvfId() {
		return ovfId;
	}
	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}
	
	@Column(name = "provider_vm_id", nullable = true)
	public String getProviderVmId() {
		return providerVmId;
	}
	public void setProviderVmId(String providerVmId) {
		this.providerVmId = providerVmId;
	}
	
	@Column(name = "provider_id", nullable = true)
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	
	@Column(name = "sla_agreement", nullable = true)
	public String getSlaAgreement() {
		return slaAgreement;
	}
	public void setSlaAgreement(String slaAgreement) {
		this.slaAgreement = slaAgreement;
	}
	
	@Column(name = "ip_address", nullable = true)
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void addImage(Image image) {
		if(images == null) images = new ArrayList<Image>(); 
		images.add(image);
	}
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	@JoinTable(name = "vms_images", joinColumns = { 
			@JoinColumn(name = "vm_id", nullable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "image_id", nullable = false) })
	public List<Image> getImages() {
		return images;
	}
	public void setImages(List<Image> images) {
		this.images = images;
	}
	
	@Transient
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	public void addLink(Link link) {
		if(links == null) links = new ArrayList<Link>();
		links.add(link);
	}
	
	@Column(name = "status", nullable = true)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
