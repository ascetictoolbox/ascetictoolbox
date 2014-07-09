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

import org.apache.xmlbeans.XmlException;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.exceptions.InvalidDocumentException;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinitionFactory {

	/**
	 * Generates an empty instance of an OVF. 
	 * 
	 * @return A new instance of OvfDefinitio
	 */
	public OvfDefinition newInstance() {
		return new OvfDefinition(XmlBeanEnvelopeDocument.Factory.newInstance());
	}
	
	/**
	 * Generates a new instance of the 3Tier WebApp using a template.
	 * 
	 * @param applicationId The ID of the Application
	 * @param imageRepository The directory of the image repository
	 * @return A new instance of OvfDefinition
	 */
	public OvfDefinition newInstance(String applicationId, String imageRepository) {
		TemplateLoader loader = new TemplateLoader();
		return new OvfDefinition(loader.loadOvfDefinitionTemplate(
				applicationId, imageRepository));
	}

	/**
	 * Creates an instance of the API using an XMLBeanEnvelopeDocument as its starting point.
	 * 
	 * @param ovfDefinitionAsXmlBeans The XMLBeans representation of an OVF definition
	 * @return A new instance of OvfDefinition
	 */
	public OvfDefinition newInstance(
			XmlBeanEnvelopeDocument ovfDefinitionAsXmlBeans) {
		if (!ovfDefinitionAsXmlBeans.validate()) {
			throw new InvalidDocumentException(
					"Document to be imported is invalid!",
					ovfDefinitionAsXmlBeans);
		}
		return new OvfDefinition(ovfDefinitionAsXmlBeans);
	}

	/**
	 * Creates an instance of the API using an XMLBeanEnvelopeDocument as its starting point.
	 * 
	 * @param ovfDefinitionAsString The String representation of an OVF definition 
	 * @return A new instance of OvfDefinition
	 */
	public OvfDefinition newInstance(String ovfDefinitionAsString) {
		XmlBeanEnvelopeDocument newDoc;
		try {
			newDoc = XmlBeanEnvelopeDocument.Factory
					.parse(ovfDefinitionAsString);
		} catch (XmlException e) {
			throw new RuntimeException(
					"Problem parsing ovfDefinition from String.", e);
		}

		if (!newDoc.validate()) {
			throw new InvalidDocumentException(
					"Document to be imported is invalid!", newDoc);
		}

		return new OvfDefinition(newDoc);
	}

}
