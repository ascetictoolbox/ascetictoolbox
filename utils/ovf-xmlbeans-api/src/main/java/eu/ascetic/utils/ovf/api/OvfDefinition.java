/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.factories.OvfDefinitionFactory;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinition extends AbstractElement<XmlBeanEnvelopeDocument> {

	/**
	 * A static reference to the {@link OvfDefinitionFactory} class for
	 * generating new instances of this object.
	 */
	public static OvfDefinitionFactory Factory = new OvfDefinitionFactory();

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
		delegate.getEnvelope()
				.setSectionArray(1, networkSection.getXmlObject());
	}

	public VirtualSystemCollection getVirtualSystemCollection() {
		return new VirtualSystemCollection(
				(XmlBeanVirtualSystemCollectionType) delegate.getEnvelope()
						.getContent());
	}

	public void setVirtualSystemCollection(
			VirtualSystemCollection virtualSystemCollection) {
		delegate.getEnvelope().setContent(
				virtualSystemCollection.getXmlObject());
	}
}
