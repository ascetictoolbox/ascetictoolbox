package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class Network extends AbstractElement<XmlBeanNetworkSectionType.Network> {
	
	public static NetworkFactory Factory = new NetworkFactory();
	
	public Network(XmlBeanNetworkSectionType.Network base) {
		super(base);
	}

	public String getDescription() {
		return delegate.getDescription().getStringValue();
	}

	public void setDescription(String description) {
		delegate.setDescription(XmlSimpleTypeConverter.toMsgType(description));
	}

	public String getName() {
		return delegate.getName();
	}

	public void setName(String name) {
		delegate.setName(name);
	}

}
