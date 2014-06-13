package eu.ascetic.providerregistry.model;

import static eu.ascetic.providerregistry.Dictionary.PROVIDER_REGISTRY_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO representation of the items inside a Provider Registry Collection
 * @author David Garcia Perez - AtoS
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
