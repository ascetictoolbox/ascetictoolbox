package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.SLA_XMLNS;

import java.util.List;

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
 * This class represents the SLA Template to send to the PaaS SLA Manager
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="SLATemplate", namespace=SLA_XMLNS)
public class SLATemplate {
	@XmlElement(name="UUID", namespace=SLA_XMLNS)
	private String UUID;
	@XmlElement(name="ModelVersion", namespace=SLA_XMLNS)
	private String modelVersion;
	@XmlElement(name="Properties", namespace=SLA_XMLNS)
	private Properties properties;
	@XmlElement(name="Party", namespace=SLA_XMLNS)
	private List<Party> parties;
	@XmlElement(name="InterfaceDeclr", namespace=SLA_XMLNS)
	private List<InterfaceDeclr> interfaceDeclrs;
	@XmlElement(name="AgreementTerm", namespace=SLA_XMLNS)
	private List<AgreementTerm> agreemenTerms;
	
	public List<AgreementTerm> getAgreemenTerms() {
		return agreemenTerms;
	}

	public void setAgreemenTerms(List<AgreementTerm> agremmenTerms) {
		this.agreemenTerms = agremmenTerms;
	}

	public List<InterfaceDeclr> getInterfaceDeclrs() {
		return interfaceDeclrs;
	}

	public void setInterfaceDeclrs(List<InterfaceDeclr> interfaceDeclrs) {
		this.interfaceDeclrs = interfaceDeclrs;
	}

	public List<Party> getParties() {
		return parties;
	}

	public void setParties(List<Party> parties) {
		this.parties = parties;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}
}
