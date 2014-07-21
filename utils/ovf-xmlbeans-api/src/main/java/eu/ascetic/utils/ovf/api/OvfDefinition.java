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

/**
 * Provides an entry point into an OVF document and access to the Envelope
 * element. The Envelope element describes all metadata for the virtual machines
 * (including virtual hardware), as well as the structure of the OVF package
 * itself. A OVF document can contain the following sections (ordered
 * preferable):<br>
 * <br>
 * {@link References} - Specifies a list of file references to all external
 * files that are part of the OVF package.<br>
 * {@link DiskSection} - Describes meta-information about all virtual disks in
 * the package.<br>
 * {@link NetworkSection} - Describes logical networks used in the package.<br>
 * DeploymentOptionSection - TODO: Specifies a discrete set of intended resource
 * requirements.<br>
 * {@link VirtualSystem} - TODO: Specifies a single virtual machine
 * configurations. <br>
 * {@link VirtualSystemCollection} (0-n instances) - Specifies a container of
 * multiple {@link VirtualSystem}s<br>
 * <br>
 * TODO: Implement support for OVF documents that describe a single virtual
 * machine in a root {@link VirtualSystem} element without a
 * {@link VirtualSystemCollection} section as a wrapper.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinition extends AbstractElement<XmlBeanEnvelopeDocument> {

    /**
     * A static reference to the {@link OvfDefinitionFactory} class for
     * generating new instances of this object.
     */
    public static OvfDefinitionFactory Factory = new OvfDefinitionFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public OvfDefinition(XmlBeanEnvelopeDocument base) {
        super(base);
    }

    /**
     * Gets the {@link References} element of the OVF document. It describes a
     * list of file references to all external files that are part of the OVF
     * document, defined by the {@link References} element and its {@link File}
     * child elements. These are typically virtual disk files, ISO images, and
     * other resources.
     * 
     * @return The References
     */
    public References getReferences() {
        return new References(delegate.getEnvelope().getReferences());
    }

    /**
     * Sets the {@link References} section of the OVF document. It describes a
     * list of file references to all external files that are part of the OVF
     * document, defined by the {@link References} element and its {@link File}
     * child elements. These are typically virtual disk files, ISO images, and
     * other resources.
     * 
     * @param references
     *            The References to set
     */
    public void setReferences(References references) {
        delegate.getEnvelope().addNewReferences();
        delegate.getEnvelope().setReferences(references.getXmlObject());
    }

    /**
     * Gets the {@link DiskSection} section of the OVF document. It describes
     * meta-information about all virtual disks.
     * 
     * @return The DiskSection
     */
    public DiskSection getDiskSection() {
        // FIXME: The type returned should be checked? (Unless the schema
        // specifies the locality here?)
        return new DiskSection((XmlBeanDiskSectionType) delegate.getEnvelope()
                .getSectionArray(0));
    }

    /**
     * Gets the {@link DiskSection} section of the OVF document. It describes
     * meta-information about all virtual disks.
     * 
     * @param diskSection
     *            The DiskSection to set.
     */
    public void setDiskSection(DiskSection diskSection) {
        if (delegate.getEnvelope().getSectionArray().length < 1) {
            delegate.getEnvelope().addNewSection();
        }
        delegate.getEnvelope().setSectionArray(0, diskSection.getXmlObject());
    }

    /**
     * Gets the {@link NetworkSection} section of the OVF document. It describes
     * logical networks used by virtual machines.
     * 
     * @return The NetworkSection
     */
    public NetworkSection getNetworkSection() {
        // FIXME: The type returned should be checked? (Unless the schema
        // specifies the locality here?)
        return new NetworkSection((XmlBeanNetworkSectionType) delegate
                .getEnvelope().getSectionArray(1));
    }

    /**
     * Sets the {@link NetworkSection} section of the OVF document. It describes
     * logical networks used by virtual machines.
     * 
     * @param networkSection
     *            The NetworkSection to set
     */
    public void setNetworkSection(NetworkSection networkSection) {
        if (delegate.getEnvelope().getSectionArray().length < 2) {
            delegate.getEnvelope().addNewSection();
        }
        delegate.getEnvelope()
                .setSectionArray(1, networkSection.getXmlObject());
    }

    /**
     * Gets the {@link VirtualSystemCollection} section of the OVF document.
     * Provides a description of the OVF document content as collection of
     * multiple virtual machines (a VirtualSystemCollection element).
     * 
     * @return The VirtualSystemCollection
     */
    public VirtualSystemCollection getVirtualSystemCollection() {
        return new VirtualSystemCollection(
                (XmlBeanVirtualSystemCollectionType) delegate.getEnvelope()
                        .getContent());
    }

    /**
     * Sets the {@link VirtualSystemCollection} section of the OVF document.
     * Provides a description of the OVF document content as collection of
     * multiple virtual machines (a VirtualSystemCollection element).
     * 
     * @param virtualSystemCollection
     *            The VirtualSystemCollection to set
     */
    public void setVirtualSystemCollection(
            VirtualSystemCollection virtualSystemCollection) {
        delegate.getEnvelope().addNewContent();
        delegate.getEnvelope().setContent(
                virtualSystemCollection.getXmlObject());
    }
}
