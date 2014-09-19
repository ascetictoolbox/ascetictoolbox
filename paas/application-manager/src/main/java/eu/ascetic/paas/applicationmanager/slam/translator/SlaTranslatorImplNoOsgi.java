package eu.ascetic.paas.applicationmanager.slam.translator;

import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SLATemplateParser;
import org.slasoi.gslam.syntaxconverter.SLATemplateRenderer;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public class SlaTranslatorImplNoOsgi implements SlaTranslator {
	private static SLATemplateParser parser;
	private static SLATemplateRenderer renderer;
	
	public SlaTranslatorImplNoOsgi() {
		parser = new SLASOITemplateParser();
		renderer = new SLASOITemplateRenderer();
	}

	@Override
	public SLATemplate parseSlaTemplate(String xmlSlat) throws Exception {
		return (SLATemplate) parser.parseTemplate(xmlSlat);
	}

	@Override
	public String renderSlaTemplate(SLATemplate slat) throws Exception {
		return renderer.renderSLATemplate(slat);
	}

	@Override
	public SLA parseSla(String xmlSla) throws Exception {
		return (SLA) parser.parseTemplate(xmlSla);
	}

	@Override
	public String renderSla(SLA sla) throws Exception {
		return renderer.renderSLATemplate(sla);
	}
}
