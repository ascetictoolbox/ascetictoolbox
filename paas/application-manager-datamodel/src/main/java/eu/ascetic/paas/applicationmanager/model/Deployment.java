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
import javax.persistence.ManyToOne;
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
import javax.xml.bind.annotation.XmlTransient;

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
 * Defines the deployment of an Application at PaaS level
 * 
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
	@XmlElement(name = "deploymen_name", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String deploymentName;
	@XmlElement(name = "status", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String status;
	@XmlElement(name = "price", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String price;
	@XmlElement(name = "start-date", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String startDate;
	@XmlElement(name = "end-date", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String endDate;
	@XmlElement(name = "ovf", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String ovf;
	@XmlElement(name = "schema", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int schema=1;
	@XmlElement(name = "provider_id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerId;
	@XmlElement(name = "sla_agreement", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String slaUUID;
	@XmlElementWrapper(name = "vms", namespace = APPLICATION_MANAGER_NAMESPACE)
	@XmlElement(name = "vm", namespace = APPLICATION_MANAGER_NAMESPACE )
	private List<VM> vms;
	@XmlTransient
	private List<Agreement> agreements;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	@XmlTransient
	private Application application;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "deployment_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name = "deploymentName", nullable = true, unique = true)
	public String getDeploymentName() {
		return deploymentName;
	}
	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
	
	@Column(name = "provider_id", nullable = true, unique = true)
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	
	@Column(name = "status", nullable = false)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Column(name = "sla_uuid", nullable = true)
	public String getSlaUUID() {
		return slaUUID;
	}
	public void setSlaUUID(String slaUUID) {
		this.slaUUID = slaUUID;
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
	
	@Column(name = "price_schema", nullable = true)
	public int getSchema() {
		return schema;
	}
	public void setSchema(int schema) {
		this.schema = schema;
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

	@Column(name = "ovf", length=900000)
	public String getOvf() {
		return ovf;
	}
	public void setOvf(String ovf) {
		this.ovf = ovf;
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
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "deployment_id", referencedColumnName="deployment_id", nullable = true)
	public List<Agreement> getAgreements() {
		return agreements;
	}
	public void setAgreements(List<Agreement> agreements) {
		this.agreements = agreements;
	}
	public void addAgreement(Agreement agreement) {
		if(agreements == null) agreements = new ArrayList<Agreement>();
		agreements.add(agreement);
	}
	
	@Column(name = "start_date", nullable = true)
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	@Column(name = "end_date", nullable = true)
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="application_id")
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}
}
