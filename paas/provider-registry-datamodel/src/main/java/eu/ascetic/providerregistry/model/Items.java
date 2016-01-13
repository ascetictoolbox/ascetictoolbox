package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.model.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

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
 * POJO representation of the items inside a Provider Registry Collection
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "items", namespace = PROVIDER_REGISTRY_NAMESPACE)
public class Items {
	@XmlAttribute
	private int offset;
	@XmlAttribute
	private int total;
	
	@XmlElement(name="provider", namespace = PROVIDER_REGISTRY_NAMESPACE)
    private List<Provider> providers;
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	

    public List<Provider> getProviders() {
		return providers;
	}
	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}
	public void addProvider(Provider provider) {
		if(providers == null) providers = new ArrayList<Provider>();
		providers.add(provider);
	}
}
