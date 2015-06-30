package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.ascetic.paas.applicationmanager.model.adapter.TimestampAdapter;

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
 * POJO Representing an agreement between PaaS and IaaS layer at Application Manager level
 *
 */
// XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "agreement", namespace = APPLICATION_MANAGER_NAMESPACE)
@Entity
@Table(name="agreements")
@NamedQueries( { 
	@NamedQuery(name="Agreement.findAll", query="SELECT p FROM Agreement p")
} )
public class Agreement {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlTransient
	private Deployment deployment;
	@XmlElement(name = "price", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String price;
	@XmlTransient
	private String slaAgreement;
	@XmlElement(name = "provider-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerId;
	@XmlElement(name = "sla-agreement-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String slaAgreementId;
	@XmlElement(name = "negotiation-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String negotiationId;
	@XmlTransient
	private int orderInArray;
	@XmlElement(name = "valid-until", namespace = APPLICATION_MANAGER_NAMESPACE)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp validUntil;
	@XmlElement(name = "accepted", namespace = APPLICATION_MANAGER_NAMESPACE)
	private boolean accepted;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
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
	@Column(name = "agreement_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="deployment_id")
	public Deployment getDeployment() {
		return deployment;
	}
	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}
	
	@Column(name = "price", nullable = true)
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	
	@Column(name = "sla_agreement", length=900000)
	public String getSlaAgreement() {
		return slaAgreement;
	}
	public void setSlaAgreement(String slaAgreement) {
		this.slaAgreement = slaAgreement;
	}
	
	@Column(name = "provider_id", nullable = true)
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	
	@Column(name = "sla_agreement_id", nullable = true)
	public String getSlaAgreementId() {
		return slaAgreementId;
	}
	public void setSlaAgreementId(String slaAgreementId) {
		this.slaAgreementId = slaAgreementId;
	}
	
	@Column(name = "negotiation_id", nullable = true)
	public String getNegotiationId() {
		return negotiationId;
	}
	public void setNegotiationId(String negotiationId) {
		this.negotiationId = negotiationId;
	}
	
	@Column(name = "accepted", nullable = true)
	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
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
	
	@Column(name = "order_in_array", nullable = true)
	public int getOrderInArray() {
		return orderInArray;
	}
	public void setOrderInArray(int orderInArray) {
		this.orderInArray = orderInArray;
	}
	
	@Column(name = "valid_until", nullable = true)
	public Timestamp getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(Timestamp validUntil) {
		this.validUntil = validUntil;
	}
}
