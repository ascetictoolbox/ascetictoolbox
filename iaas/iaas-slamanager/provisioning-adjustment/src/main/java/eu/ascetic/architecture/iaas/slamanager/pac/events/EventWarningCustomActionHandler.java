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

package eu.ascetic.architecture.iaas.slamanager.pac.events;

import org.apache.log4j.Logger;
import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.gslam.core.negotiation.SLARegistry.InvalidUUIDException;
import org.slasoi.gslam.pac.events.Message;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.SLA;

import eu.ascetic.architecture.iaas.slamanager.pac.ProvisioningAdjustmentImpl;
import eu.ascetic.architecture.iaas.slamanager.pac.action.ActionInvocation;

public class EventWarningCustomActionHandler extends EventHandler {

	private static final Logger logger = Logger.getLogger(EventViolationFederationForwardingHandler.class.getName());

	public EventWarningCustomActionHandler() {

	}

	public EventWarningCustomActionHandler(Message m) {
		super(m);
	}

	@Override
	public void process() {
		System.out.println("Event Warning Custom Action invoked");
		ViolationMessage violationMessage = (ViolationMessage) message;
		String slaId = ((ViolationMessage) violationMessage).getAlert().getSlaUUID();
		UUID uuid = new UUID(slaId);
		UUID[] uuids = new UUID[1];
		uuids[0] = uuid;
		SLA[] slas = null;
		SLA sla = null;
		try {
			slas = ProvisioningAdjustmentImpl.getSLAManagerContext().getSLARegistry().getIQuery().getSLA(uuids);
		} catch (InvalidUUIDException e) {
			logger.debug(e.getMessage());
		} catch (SLAManagerContextException e) {
			logger.debug(e.getMessage());
		}
		if (slas.length != 0) {
			sla = slas[0];
			logger.debug("SLA loaded from SLARegistry:");
			ActionInvocation action = new ActionInvocation();
			action.init();
			action.invokeActionFromSLA(sla, violationMessage.getAlert().getSlaAgreementTerm(), violationMessage.getAlert().getSlaGuaranteedState().getGuaranteedId());
		}
	}

}
