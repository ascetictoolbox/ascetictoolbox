package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * POJO that represents a collection terms of an SLA of an Application
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sla_application_terms", namespace = APPLICATION_MANAGER_NAMESPACE)
public class SLAApplicationTerms {
	@XmlElement(name = "sla_application_info_term", namespace = APPLICATION_MANAGER_NAMESPACE)
	public List<SLAInfoTerm> slaInfoTerms;

	public List<SLAInfoTerm> getSlaInfoTerms() {
		return slaInfoTerms;
	}

	public void setSlaInfoTerms(List<SLAInfoTerm> slaInfoTerms) {
		this.slaInfoTerms = slaInfoTerms;
	}
}
