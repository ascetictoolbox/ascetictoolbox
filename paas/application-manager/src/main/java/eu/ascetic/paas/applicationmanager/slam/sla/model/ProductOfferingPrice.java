package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.SLA_XMLNS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 * e-mail david.garciaperez@atos.net
 * 
 * This class represents an object from the XML SLA Agreement, to be specific
 * an ProductOfferingPrice inside the SLA Agreement
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ProductOfferingPrice", namespace = SLA_XMLNS)
public class ProductOfferingPrice {
	@XmlElement(name = "ID", namespace = SLA_XMLNS)
	private String id;
	@XmlElement(name = "ComponentProdOfferingPrice", namespace = SLA_XMLNS)
	private ComponentProdOfferingPrice componentProdOfferingPrice;
	
	public ComponentProdOfferingPrice getComponentProdOfferingPrice() {
		return componentProdOfferingPrice;
	}

	public void setComponentProdOfferingPrice(
			ComponentProdOfferingPrice componentProdOfferingPrice) {
		this.componentProdOfferingPrice = componentProdOfferingPrice;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
