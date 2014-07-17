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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVSSDType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.factories.SystemFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to the optional System element. The System element specifies
 * a virtual system type identifier, which is an implementation defined string
 * that uniquely identifies the type of the virtual system. For example, a
 * virtual 684 system type identifier could be "vmx-4" for VMware’s
 * fourth-generation virtual hardware or "xen-3" for Xen's 685 third-generation
 * virtual hardware. The virtual system type identifiers specified in
 * vssd:VirtualSystemType elements are expected to be matched against the values
 * of property VirtualSystemTypesSupported of CIM class
 * CIM_VirtualSystemManagementCapabilities. (See <a
 * href="@link http://www.dmtf.org/standards/cim/cim_schema_v2191">http:/
 * /www.dmtf.org/standards/cim/cim_schema_v2191</a> and <a href=
 * "https://www.vmware.com/support/developer/cim-sdk/smash/u2/ga/apirefdoc/CIM_VirtualSystemSettingData.html"
 * >https://www.vmware.com/support/developer/cim-sdk/smash/u2/ga/apirefdoc/
 * CIM_VirtualSystemSettingData.html</a>)<br>
 * <br>
 * TODO: Implement enumeration class of possible VirtualSystemTypesSupported.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class System extends AbstractElement<XmlBeanVSSDType> {

	/**
	 * A static reference to the {@link SystemFactory} class for generating new
	 * instances of this object.
	 */
	public static SystemFactory Factory = new SystemFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public System(XmlBeanVSSDType base) {
		super(base);
	}

	/**
	 * Gets the human readable name of the element. The OVF specification
	 * provides an example of "Virtual System Type".
	 * 
	 * @return The element name
	 */
	public String getElementName() {
		return delegate.getElementName().getStringValue();
	}

	/**
	 * Sets the human readable name of the element. The OVF specification
	 * provides an example of "Virtual System Type".
	 * 
	 * @param elementName
	 *            The element name to set
	 */
	public void setElementName(String elementName) {
		delegate.setElementName(XmlSimpleTypeConverter.toCimString(elementName));
	}

	/**
	 * Gets the instances ID that provides an unique identifier for this
	 * VirtualSystemType. The OVF specification is unclear on its use. It is
	 * assumed that this will always be "0" as multiple virtual system type
	 * identifiers are supported in {@link System#setVirtualSystemType(String)
	 * through white space separation}.
	 * 
	 * @return The instance ID
	 */
	public String getInstanceID() {
		return delegate.getInstanceID().getStringValue();
	}

	/**
	 * Sets the instances ID that provides an unique identifier for this
	 * VirtualSystemType. The OVF specification is unclear on its use. It is
	 * assumed that this will always be "0" as multiple virtual system type
	 * identifiers are supported in {@link System#setVirtualSystemType(String)
	 * through white space separation}.
	 * 
	 * @param instanceID
	 *            The instance ID to set
	 */
	public void setInstanceID(String instanceID) {
		delegate.setInstanceID(XmlSimpleTypeConverter.toCimString(instanceID));
	}

	/**
	 * Gets the virtual system type. Zero or more virtual system type
	 * identifiers may be specified separated by a single white space character.
	 * In order for the OVF virtual system to be deployable on a target
	 * platform, the virtual machine on the target platform should support at
	 * least one of the virtual system types identified in this element. The
	 * virtual system type identifiers specified in this element are expected to
	 * be matched against the values of property VirtualSystemTypesSupported of
	 * CIM class CIM_VirtualSystemManagementCapabilities (see {@link System}.
	 * 
	 * @return The virtual system type(s)
	 */
	public String getVirtualSystemType() {
		return delegate.getVirtualSystemType().getStringValue();
	}

	/**
	 * Sets the virtual system type. Zero or more virtual system type
	 * identifiers may be specified separated by a single white space character.
	 * In order for the OVF virtual system to be deployable on a target
	 * platform, the virtual machine on the target platform should support at
	 * least one of the virtual system types identified in this element. The
	 * virtual system type identifiers specified in this element are expected to
	 * be matched against the values of property VirtualSystemTypesSupported of
	 * CIM class CIM_VirtualSystemManagementCapabilities (see {@link System}.
	 * 
	 * @param virtualSystemType
	 *            The virtual system type(s) to set
	 */
	public void setVirtualSystemType(String virtualSystemType) {
		if (!delegate.isSetVirtualSystemType()) {
			delegate.addNewVirtualSystemType();
		}
		delegate.setVirtualSystemType(XmlSimpleTypeConverter
				.toCimString(virtualSystemType));
	}
}
