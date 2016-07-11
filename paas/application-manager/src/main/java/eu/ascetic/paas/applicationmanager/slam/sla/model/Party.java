package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.SLA_XMLNS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * e-mail david.garciaperez@atos.net
 * 
 * Represents the Party entry inside an SLA Template
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="Party", namespace=SLA_XMLNS)
public class Party {
	@XmlElement(name="Properties", namespace=SLA_XMLNS)
	private Properties properties;
	@XmlElement(name="ID", namespace=SLA_XMLNS)
	private String id;
	@XmlElement(name="Role", namespace=SLA_XMLNS)
	private String role;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
