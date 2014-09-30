package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 * POJO that represents the link object of the Provider Registry XML
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "link", namespace = PROVIDER_REGISTRY_NAMESPACE)
public class Link {
	@XmlAttribute
	private String rel;
	@XmlAttribute
	private String href;
	@XmlAttribute
	private String type;
	
	public Link() {};
	
	public Link(String rel, String href, String type) {
		this.rel = rel;
		this.href = href;
		this.type = type;
	}
	
	/**
	 * @return the relative path to this link
	 */
	public String getRel() {
		return rel;
	}
	/**
	 * Sets the relative path for this link
	 * @param rel
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}
	
	/**
	 * @return the URL for this link
	 */
	public String getHref() {
		return href;
	}
	/**
	 * @param href Sets the URL for this link
	 */
	public void setHref(String href) {
		this.href = href;
	}
	
	/**
	 * Indicates the type of data that it is comming back from this link
	 * @return the http type of data
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type of data that is going to be returned or accepted by this link
	 */
	public void setType(String type) {
		this.type = type;
	}
}

