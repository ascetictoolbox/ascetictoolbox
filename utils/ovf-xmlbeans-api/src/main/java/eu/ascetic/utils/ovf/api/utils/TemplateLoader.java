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
package eu.ascetic.utils.ovf.api.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.xmlbeans.XmlException;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

/**
 * A class for loading OVF documents from velocity templates and populating them
 * with variables.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class TemplateLoader {

	/**
	 * Default location of the OVF velocity template.<br>
	 * <br>
	 * TODO: Make this a getter/setter so any template can be used
	 */
	private static final String TEMPLATE = "/ovf.template.xml";

	/**
	 * Default key to use when populating the application ID within an OVF
	 * template.
	 */
	public static final String APPLICATION_ID_KEY = "applicationId";

	/**
	 * Default key to use when populating the image repository URI within an OVF
	 * template.
	 */
	public static final String IMAGE_REPOSITORY_KEY = "imageRepository";

	/**
	 * Storage for default Properties object instance.
	 */
	private Properties defaultProperties;

	/**
	 * Default constructor.
	 */
	public TemplateLoader() {
		// Set template root directory "src/main/resources" through classpath
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());

		// Initialise velocity
		Velocity.init();

		// Load the default properties object
		loadDefaultProperties();
	}

	/**
	 * Loads the default OVF template with default properties (currently
	 * representing the Three Tier Webapp) returning it as an instance of
	 * {@link XmlBeanEnvelopeDocument}.
	 * 
	 * @param applicationId
	 *            The application ID to set in the template
	 * @param imageRepository
	 *            The image repository URI to set in the template
	 * @return The XmlBeanEnvelopeDocument
	 */
	public XmlBeanEnvelopeDocument loadOvfDefinitionTemplate(
			String applicationId, String imageRepository) {

		defaultProperties.put(APPLICATION_ID_KEY, applicationId);
		defaultProperties.put(IMAGE_REPOSITORY_KEY, imageRepository);

		return loadOvfDefinitionTemplate(TEMPLATE, defaultProperties);
	}

	/**
	 * Loads a Template populated with a provided Properties object returning it as an instance of
	 * {@link XmlBeanEnvelopeDocument}.<br>
	 * <br>
	 * 
	 * @param templateUri The location of the template
	 * @param properties The properties object to use.
	 * @return The XmlBeanEnvelopeDocument
	 */
	public XmlBeanEnvelopeDocument loadOvfDefinitionTemplate(String templateUri, Properties properties) {
		Template template = Velocity.getTemplate(templateUri);
		VelocityContext ctx = createVelocityContext(properties);

		// Add all properties to the velocity context
		putPropertiesToVelocityContext(properties, ctx);

		Writer writer = new StringWriter();
		template.merge(ctx, writer);

		XmlBeanEnvelopeDocument xmlBeanEnvelopeDocument;
		try {
			xmlBeanEnvelopeDocument = XmlBeanEnvelopeDocument.Factory
					.parse(writer.toString());
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
		return xmlBeanEnvelopeDocument;
	}

	/**
	 * Loads the default properties file.
	 */
	private void loadDefaultProperties() {
		if (defaultProperties == null) {
			defaultProperties = new TemplateDefaultProperties();
		}
	}

	/**
	 * Creates a new velocity context object from a list of properties.
	 * 
	 * @param properties
	 *            The properties
	 * @return The VelocityContext
	 */
	private VelocityContext createVelocityContext(Properties properties) {
		VelocityContext ctx = new VelocityContext();
		putPropertiesToVelocityContext(properties, ctx);
		return ctx;
	}

	/**
	 * Puts properties into a velocity context object.
	 * 
	 * @param properties
	 *            The properties to put into the VelocirtContext hash map.
	 * @param ctx
	 *            The VelocityContext object to use
	 */
	private void putPropertiesToVelocityContext(Properties properties,
			VelocityContext ctx) {
		for (Object key : properties.keySet()) {
			ctx.put(key.toString(), properties.get(key));
		}
	}
}
