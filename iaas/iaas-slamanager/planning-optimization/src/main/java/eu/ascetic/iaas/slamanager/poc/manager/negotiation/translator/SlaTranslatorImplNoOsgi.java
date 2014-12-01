/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
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
package eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator;

import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SLATemplateParser;
import org.slasoi.gslam.syntaxconverter.SLATemplateRenderer;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public class SlaTranslatorImplNoOsgi implements SlaTranslator{

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


	public SlaTranslatorImplNoOsgi() {
		parser = new SLASOITemplateParser();
		renderer = new SLASOITemplateRenderer();
	}
	
	private static SLATemplateParser parser;
	private static SLATemplateRenderer renderer;
	
}
