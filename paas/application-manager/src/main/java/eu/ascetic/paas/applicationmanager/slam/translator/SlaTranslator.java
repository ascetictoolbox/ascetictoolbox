package eu.ascetic.paas.applicationmanager.slam.translator;

import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * ASCETiC SLA translator interface
 *
 */
public interface SlaTranslator {

	SLATemplate parseSlaTemplate(String xmlSlat) throws Exception;
	
	String renderSlaTemplate(SLATemplate slat) throws Exception;
	
	SLA parseSla(String xmlSla) throws Exception;
	
	String renderSla(SLA sla) throws Exception;

}