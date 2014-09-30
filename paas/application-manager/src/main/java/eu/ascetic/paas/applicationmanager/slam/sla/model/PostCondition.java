package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.SLA_XMLNS;

import java.util.List;

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
 * @email david.garciaperez@atos.net
 * 
 * This class represents an object from the XML SLA Agreement, to be specific
 * an PostCondition inside the SLA Agreement
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Postcondition", namespace = SLA_XMLNS)
public class PostCondition {
	@XmlElement(name = "ProductOfferingPrice", namespace = SLA_XMLNS )
	private ProductOfferingPrice productOfferingPrice;

	public ProductOfferingPrice getProductOfferingPrice() {
		return productOfferingPrice;
	}

	public void setProductOfferingPrice(ProductOfferingPrice productOfferingPrice) {
		this.productOfferingPrice = productOfferingPrice;
	}
}
