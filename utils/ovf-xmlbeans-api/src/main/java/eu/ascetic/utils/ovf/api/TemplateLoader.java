package eu.ascetic.utils.ovf.api;

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

public class TemplateLoader {
	private static final String TEMPLATE = "/ovf.template";

	public static final String SERVICE_ID_KEY = "serviceId";

	public static final String COMPONENT_ID_KEY = "componentId";

	private Properties defaultProperties;

	protected TemplateLoader() {
		// Has to be set, otherwise template loading may fail
		// template root directory : src/main/resources
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init();
		
		// Load the default properties
		loadDefaultProperties();
	}

	public XmlBeanEnvelopeDocument loadOvfDefinitionTemplate(String serviceId,
			String componentId) {

		return loadOvfDefinitionTemplate(serviceId, componentId,
				defaultProperties);
	}

	public XmlBeanEnvelopeDocument loadOvfDefinitionTemplate(String serviceId,
			String componentId, Properties properties) {
		Template t = Velocity.getTemplate(TEMPLATE);
		VelocityContext ctx = createVelocityContext(properties);
		ctx.put(COMPONENT_ID_KEY, componentId);
		// FIXME Add service ID somewhere in the template
		ctx.put(SERVICE_ID_KEY, serviceId);

		// Add all properties to the velocity context
		putPropertiesToVelocityContext(properties, ctx);

		Writer writer = new StringWriter();
		t.merge(ctx, writer);

		XmlBeanEnvelopeDocument xmlBeanEnvelopeDocument;
		try {
			xmlBeanEnvelopeDocument = XmlBeanEnvelopeDocument.Factory
					.parse(writer.toString());
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
		return xmlBeanEnvelopeDocument;
	}
	
	private void loadDefaultProperties() {
		if (defaultProperties == null) {
			defaultProperties = new OvfDefinitionProperties();
		}
	}

	private VelocityContext createVelocityContext(Properties properties) {
		VelocityContext ctx = new VelocityContext();
		putPropertiesToVelocityContext(properties, ctx);
		return ctx;
	}
	
	private void putPropertiesToVelocityContext(Properties properties,
			VelocityContext ctx) {
		for (Object key : properties.keySet()) {
			ctx.put(key.toString(), properties.get(key));
		}
	}
}
