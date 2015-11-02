package eu.ascetic.paas.slam.pac.applicationmanager.model;

import static eu.ascetic.paas.slam.pac.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

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
 * e-mail: david.garciaperez@atos.net 
 * 
 * POJO Representing a VM at Application Manager level
 * 
 */

// XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "image", namespace = APPLICATION_MANAGER_NAMESPACE)
@Entity
@Table(name="images")
@NamedQueries( { 
	@NamedQuery(name="Image.findAll", query="SELECT p FROM Image p")
} )
public class Image {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "ovf-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String ovfId;
	@XmlElement(name = "provider-image-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerImageId;
	@XmlElement(name = "ovf-href", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String ovfHref;
	@XmlElement(name = "demo", namespace = APPLICATION_MANAGER_NAMESPACE)
	private boolean demo = false;
	@XmlElement(name = "provider-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String providerId;
	@XmlTransient
	private Application application;
	
	@Transient
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "image_id", unique = true, nullable = false)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name = "ovf_id", nullable = true)
	public String getOvfId() {
		return ovfId;
	}
	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}
	
	@Column(name = "provider_image_id", nullable = true)
	public String getProviderImageId() {
		return providerImageId;
	}
	public void setProviderImageId(String providerImageId) {
		this.providerImageId = providerImageId;
	}
	
	@Column(name = "ovf_href", nullable = true)
	public String getOvfHref() {
		return ovfHref;
	}
	public void setOvfHref(String ovfHref) {
		this.ovfHref = ovfHref;
	}
	
	@Column(name = "demo", nullable = false)
	public boolean isDemo() {
		return demo;
	}
	public void setDemo(boolean demo) {
		this.demo = demo;
	}
	
	@Column(name = "provider_id", nullable = true)
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = true)
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}
	
	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof Image))
            return false;
        if (obj == this)
            return true;

        Image image = (Image) obj;
        
        if(this.ovfId == null) {
        	if(image.getOvfId() != null) {
        		return false;
        	}
        } else if(!this.ovfId.equals(image.ovfId)) {
        	return false;
        }
        
        if(this.href == null) {
        	if(image.getHref() != null) {
        		return false;
        	}
        } else if(!this.href.equals(image.getHref())) {
        	return false;
        }
        
        if(this.providerImageId == null) {
        	if(image.getProviderImageId() != null ) {
        		return false;
        	}
        } else if(!this.providerImageId.equals(image.getProviderImageId())) {
        	return false;
        }
        
        if(this.providerId == null) {
        	if(image.getProviderId() != null) {
        		return false;
        	}
        } else if(!this.providerId.equals(image.getProviderId())) {
        	return false;
        }
        
        if(this.ovfHref == null) {
        	if(image.getOvfHref() != null) {
        		return false;
        	}
        } else if(!this.ovfHref.equals(image.getOvfHref())) {
        	return false;
        }
        
        if(this.id == image.getId()
           && this.demo == image.isDemo()) {
        	return true;
        } else {
        	return false;
        }
	}
}
