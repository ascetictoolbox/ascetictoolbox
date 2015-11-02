package eu.ascetic.paas.applicationmanager.providerregistry.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

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
public class Provider {
	@XmlAttribute
	private String href;
	@XmlElement(name="id", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private int id;
	@XmlElement(name="name", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String name;
	@XmlElement(name="vmm-url", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String vmmUrl;
	@XmlElement(name="slam-url", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String slamUrl;
	@XmlElement(name="amqp-url", namespace=PROVIDER_REGISTRY_NAMESPACE)
	private String amqpUrl;
	@XmlElement(name="link", namespace = PROVIDER_REGISTRY_NAMESPACE)
	private List<Link> links;


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
	
	public String getVmmUrl() {
		return vmmUrl;
	}
	public void setVmmUrl(String vmmUrl) {
		this.vmmUrl = vmmUrl;
	}

	public String getSlamUrl() {
		return slamUrl;
	}
	public void setSlamUrl(String slamUrl) {
		this.slamUrl = slamUrl;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getAmqpUrl() {
		return amqpUrl;
	}
	public void setAmqpUrl(String amqpUrl) {
		this.amqpUrl = amqpUrl;
	}

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
