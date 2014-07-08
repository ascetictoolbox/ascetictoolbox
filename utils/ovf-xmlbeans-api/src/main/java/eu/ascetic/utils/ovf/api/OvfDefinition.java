package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class OvfDefinition extends AbstractElement<XmlBeanEnvelopeDocument> {
	/**
	 * Factory for creating new OvfDefinition instances.
	 */
	// CHECKSTYLE:OFF
	public static OvfDefinitionFactory Factory = new OvfDefinitionFactory();
	// CHECKSTYLE:ON

	public OvfDefinition(XmlBeanEnvelopeDocument base) {
		super(base);
	}

	public References getReferences() {
		return new References(delegate.getEnvelope().getReferences());
	}
	
	public void setReferences(References references) {
		delegate.getEnvelope().setReferences(references.getXmlObject());
	}

	// FIXME: The type returned should be checked? (Unless the schema specifies
	// the locality here?)
	public DiskSection getDiskSection() {
		return new DiskSection((XmlBeanDiskSectionType) delegate.getEnvelope()
				.getSectionArray(0));
	}
	
	public void setDiskSection(DiskSection diskSection) {
		delegate.getEnvelope().setSectionArray(0, diskSection.getXmlObject());
	}

	// FIXME: The type returned should be checked? (Unless the schema specifies
	// the locality here?)
	public NetworkSection getNetworkSection() {
		return new NetworkSection((XmlBeanNetworkSectionType) delegate
				.getEnvelope().getSectionArray(1));
	}
	
	public void setNetworkSection(NetworkSection networkSection) {
		delegate.getEnvelope().setSectionArray(1, networkSection.getXmlObject());
	}

	public VirtualSystemCollection getVirtualSystemCollection() {
		return new VirtualSystemCollection(
				(XmlBeanVirtualSystemCollectionType) delegate.getEnvelope()
						.getContent());
	}
	
	public void setVirtualSystemCollection(VirtualSystemCollection virtualSystemCollection) {
		delegate.getEnvelope().setContent(virtualSystemCollection.getXmlObject());
	}
}
