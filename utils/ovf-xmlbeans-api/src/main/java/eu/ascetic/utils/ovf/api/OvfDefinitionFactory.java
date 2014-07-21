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

import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.velocity.Template;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.utils.OvfInvalidDocumentException;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;
import eu.ascetic.utils.ovf.api.utils.TemplateLoader;

/**
 * Provides factory methods for creating instances of {@link OvfDefinition}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinitionFactory {

    /**
     * Generates an empty instance of {@link OvfDefinition} with null internal
     * object references. All contained objects (e.g.
     * {@link VirtualSystemCollection}) must therefore be instantiated and setup
     * with the appropriate member setter methods before use. This includes
     * instantiation and setup of child containers objects. You are advised to
     * avoid using such a method to create a basic OVF skeleton and instead use
     * one of the other newInstance(...) methods in this class.
     * 
     * @return A new instance of OvfDefinition
     */
    public OvfDefinition newInstance() {
        XmlBeanEnvelopeDocument xmlBeanEnvelopeDocument = XmlBeanEnvelopeDocument.Factory.newInstance();
        xmlBeanEnvelopeDocument.addNewEnvelope();
        
        XmlCursor cursor = xmlBeanEnvelopeDocument.newCursor();
        if (cursor.toFirstChild()) {
            cursor.setAttributeText(new QName(
                    "http://www.w3.org/2001/XMLSchema-instance",
                    "schemaLocation"),
                    "http://schemas.dmtf.org/ovf/envelope/1 ../dsp8023.xsd");
        }
        
        return new OvfDefinition(xmlBeanEnvelopeDocument);
    }

    /**
     * Generates a new instance of the Three Tier WebApp using the API's default
     * template and properties
     * 
     * @param applicationId
     *            The ID of the Application
     * @param imageRepository
     *            The directory of the image repository
     * @return A new instance of OvfDefinition
     */
    public OvfDefinition newInstance(String applicationId,
            String imageRepository) {
        TemplateLoader loader = new TemplateLoader();
        return new OvfDefinition(loader.loadOvfDefinitionTemplate(
                applicationId, imageRepository));
    }

    /**
     * Generates a new instance using a {@link Template} URI and
     * {@link Properties} object.
     * 
     * @param templateUri
     *            The location of the Template
     * @param properties
     *            The properties object
     * @return A new instance of OvfDefinition
     */
    public OvfDefinition newInstance(String templateUri, Properties properties) {
        TemplateLoader loader = new TemplateLoader();
        return new OvfDefinition(loader.loadOvfDefinitionTemplate(templateUri,
                properties));
    }

    /**
     * Creates an instance of the API using an XMLBeanEnvelopeDocument as its
     * starting point.
     * 
     * @param ovfDefinitionAsXmlBeans
     *            The XMLBeans representation of an OVF definition
     * @return A new instance of OvfDefinition
     */
    public OvfDefinition newInstance(
            XmlBeanEnvelopeDocument ovfDefinitionAsXmlBeans) {
        if (!ovfDefinitionAsXmlBeans.validate()) {
            throw new OvfInvalidDocumentException(
                    "Document to be imported is invalid!",
                    ovfDefinitionAsXmlBeans);
        }
        return new OvfDefinition(ovfDefinitionAsXmlBeans);
    }

    /**
     * Creates an instance of the API using an XMLBeanEnvelopeDocument as its
     * starting point.
     * 
     * @param ovfDefinitionAsString
     *            The String representation of an OVF definition
     * @return A new instance of OvfDefinition
     */
    public OvfDefinition newInstance(String ovfDefinitionAsString) {
        XmlBeanEnvelopeDocument newDoc;
        try {
            newDoc = XmlBeanEnvelopeDocument.Factory
                    .parse(ovfDefinitionAsString);
        } catch (XmlException e) {
            throw new OvfRuntimeException(
                    "Problem parsing ovfDefinition from String.", e);
        }

        if (!newDoc.validate()) {
            throw new OvfInvalidDocumentException(
                    "Document to be imported is invalid!", newDoc);
        }

        return new OvfDefinition(newDoc);
    }

}
