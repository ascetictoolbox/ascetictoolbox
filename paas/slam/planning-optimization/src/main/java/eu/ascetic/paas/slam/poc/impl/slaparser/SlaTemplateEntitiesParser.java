/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.paas.slam.poc.impl.slaparser;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.DomainExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLATemplate;


public class SlaTemplateEntitiesParser {

	private final static STND GSLAM_EPR = org.slasoi.slamodel.vocab.sla.gslam_epr;
	
	public static String getRootPropertyValue(SLATemplate slat, String key) {
		STND[] rootPropKeys = slat.getPropertyKeys();
		if (rootPropKeys == null || rootPropKeys.length == 0)
			return null;
					
		for (int i = 0; i < rootPropKeys.length; i++) {
			if (rootPropKeys[i].equals(key)) {
				return slat.getPropertyValue(rootPropKeys[i]);
			}
		}
		return null;
	}


	
	public static Party getProviderParty(SLATemplate slaTemplate) {
		Party[] parties = slaTemplate.getParties();
		for (Party party : parties) {
			STND partyRole = (STND) party.getAgreementRole();
			if (partyRole != null && partyRole.equals(org.slasoi.slamodel.vocab.sla.provider)) {
				if (party.getPropertyValue(GSLAM_EPR) == null) {
					return null;
				}
				return party;
			}
		}
		return null;
	}
	
	
	public static Integer getMinimumLoAValue(SLATemplate slaTemplate) {
		AgreementTerm[] agreementTerms = slaTemplate.getAgreementTerms();
		
		for (AgreementTerm term : agreementTerms) {
    		Guaranteed[] guaranteeds = term.getGuarantees();
    		for (Guaranteed guaranteed : guaranteeds) {
    			if (!(guaranteed instanceof Guaranteed.State)) {
    				continue;
    			}
    			
    			Guaranteed.State gs = (Guaranteed.State) guaranteed;
    			if (!"MinimumLoA_for_Application".equals(gs.getId().getValue())) {
    				continue;
    			}
    				
    			LOGGER.info("MinimumLoA term found!");
    			ConstraintExpr ce = gs.getState();
    					
    			if (!(ce instanceof TypeConstraintExpr))
    				return null;

    			TypeConstraintExpr tce = (TypeConstraintExpr) ce;
    			DomainExpr de = tce.getDomain();
    			if (!(de instanceof SimpleDomainExpr))
    				return null;
    					
    			ValueExpr ve = ((SimpleDomainExpr)de).getValue();
    			if (!(ve instanceof CONST)) {
    				return null;
    			} 
    			
    			String value = ((CONST)ve).getValue();
    			
    			return Integer.parseInt(value);
    					
    		}
		}
		return null;
	}
	
	
	private static final Logger LOGGER = Logger.getLogger(SlaTemplateEntitiesParser.class);
	
}
