package eu.ascetic.paas.applicationmanager.slam.sla;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.slam.sla.model.AgreementTerm;
import eu.ascetic.paas.applicationmanager.slam.sla.model.SLA;
import eu.ascetic.paas.applicationmanager.slam.sla.model.State;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * It helps to extract information from an SLA Agrement document.
 *
 */
public class SLAAgreementHelper {
	private static Logger logger = Logger.getLogger(SLAAgreementHelper.class);
	private static String powerUsagePerApp = "Power_Usage_per_app";
	private SLA sla;
	
	/**
	 * The SLAAgreementHelper needs the SLA Agreement document to be
	 * parsed to extract information from it
	 * @param slaString String representing the SLA Agreement XML document.
	 */
	public SLAAgreementHelper(String slaString) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(SLA.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			sla = (SLA) jaxbUnmarshaller.unmarshal(new StringReader(slaString));
		} catch(JAXBException jaxbExpcetion) {
			logger.warn("Error opening the SLA Agreement!!!");
			logger.warn("agreement: " + slaString);
			jaxbExpcetion.printStackTrace();
		}
	}

	/**
	 * @return the SLA parsed at construction time
	 */
	public SLA getSla() {
		return sla;
	}

	/**
	 * @return the maximum power usage of an Application... 
	 */
	public double getPowerUsagePerApp() {

		return getPowerUsage(powerUsagePerApp);
	}
	
	private double getPowerUsage(String searchId) {
		List<AgreementTerm> agreementTerms = sla.getAgreementTerms();
		
		double powerUsage = 0.0;
		
		for(AgreementTerm agreementTerm : agreementTerms) {
			State state = agreementTerm.getGuaranteed().getState();
			System.out.println("AgreementTerm " + agreementTerm.getId());
			if(state != null && state.getId().equals(searchId)) {
				powerUsage = Double.parseDouble(state.getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
				break;
			}
		}
		
		return powerUsage;
	}

	/**
	 * Returns the maximum power usage of a VM per OVF ID
	 * @param ovfId OVF Id of that type of VM
	 * @return the power usage in Watts
	 */
	public double getPowerUsagePerOVFId(String ovfId) {
		return getPowerUsage("Power_Usage_for_" + ovfId);
	}
}
