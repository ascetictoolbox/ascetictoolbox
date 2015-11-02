package eu.ascetic.paas.applicationmanager.slam;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.slasoi.gslam.core.negotiation.INegotiation;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.syntaxconverter.SLASOIParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SyntaxConverterDelegator;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CancelNegotiation;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreement;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Negotiate;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Provision;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Renegotiate;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Terminate;
import eu.ascetic.applicationmanager.slam.stub.InvalidNegotiationIDExceptionException;
import eu.ascetic.applicationmanager.slam.stub.OperationInProgressExceptionException;
import eu.ascetic.applicationmanager.slam.stub.OperationNotPossibleExceptionException;
import eu.ascetic.applicationmanager.slam.stub.ProvisioningExceptionException;
import eu.ascetic.applicationmanager.slam.stub.SLACreationExceptionException;
import eu.ascetic.applicationmanager.slam.stub.SLANotFoundExceptionException;

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
 * Application Manager client to the ASCETiC PaaS SLA Manager server
 *
 */
public class SLAMClient implements INegotiation {
	private BZNegotiationStub stub;
	protected SyntaxConverterDelegator delegator;
	
    public SLAMClient(String epr) throws AxisFault {
        super();
        this.stub = new BZNegotiationStub(epr);
        this.delegator = new SyntaxConverterDelegator(SyntaxConverterType.SLASOISyntaxConverter);
    }
	
	public String initiateNegotiation(SLATemplate slaTemplate) throws OperationNotPossibleException {
        InitiateNegotiation inParam = new InitiateNegotiation();
        try {
            if (delegator != null) {
                inParam.setSlaTemplate(delegator.renderSLATemplate(slaTemplate));
                return this.stub.initiateNegotiation(inParam).get_return();
            }
            else {
                inParam.setSlaTemplate(new SLASOITemplateRenderer().renderSLATemplate(slaTemplate));
                return this.stub.initiateNegotiation(inParam).get_return();
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (OperationNotPossibleExceptionException e) {
            e.printStackTrace();
            throw new OperationNotPossibleException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
	@Override
	public SLATemplate[] negotiate(String negotiationID, SLATemplate slaTemplate) throws OperationInProgressException, OperationNotPossibleException, InvalidNegotiationIDException {
		Negotiate inParam = new Negotiate();
        inParam.setNegotiationID(negotiationID);
        try {

            if (delegator != null) {
                inParam.setSlaTemplate(delegator.renderSLATemplate(slaTemplate));
                String[] templateStrings = this.stub.negotiate(inParam).get_return();
                SLATemplate[] templates = delegator.parseSLATemplates(templateStrings);

                return templates;
            }
            else {
                inParam.setSlaTemplate(new SLASOITemplateRenderer().renderSLATemplate(slaTemplate));
                String[] templateStrings = this.stub.negotiate(inParam).get_return();
                SLATemplate[] templates = new SLATemplate[templateStrings.length];

                for (int i = 0; i < templates.length; i++) {
                    templates[i] = new SLASOITemplateParser().parseTemplate(templateStrings[i]);
                }

                return templates;
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (OperationInProgressExceptionException e) {
            e.printStackTrace();
            throw new OperationInProgressException(e.getMessage());
        }
        catch (OperationNotPossibleExceptionException e) {
            e.printStackTrace();
            throw new OperationNotPossibleException(e.getMessage());
        }
        catch (InvalidNegotiationIDExceptionException e) {
            e.printStackTrace();
            throw new InvalidNegotiationIDException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public SLA createAgreement(String negotiationID, SLATemplate slaTemplate) throws OperationInProgressException, SLACreationException, InvalidNegotiationIDException {
        CreateAgreement inParam = new CreateAgreement();
        inParam.setNegotiationID(negotiationID);

        try {
            if (delegator != null) {
                inParam.setSlaTemplate(delegator.renderSLATemplate(slaTemplate));
                String slaAsString = stub.createAgreement(inParam).get_return();
                return (SLA) delegator.parseSLA(slaAsString);
            }
            else {
                inParam.setSlaTemplate(new SLASOITemplateRenderer().renderSLATemplate(slaTemplate));
                String slaAsString = stub.createAgreement(inParam).get_return();
                return new SLASOIParser().parseSLA(slaAsString);
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (OperationInProgressExceptionException e) {
            e.printStackTrace();
            throw new OperationInProgressException(e.getMessage());
        }
        catch (SLACreationExceptionException e) {
            e.printStackTrace();
            throw new SLACreationException(e.getMessage());
        }
        catch (InvalidNegotiationIDExceptionException e) {
            e.printStackTrace();
            throw new InvalidNegotiationIDException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
	}
	
	@Override
	public boolean cancelNegotiation(String negotiationID, List<CancellationReason> cancellationReason) throws 
																		OperationInProgressException, OperationNotPossibleException, InvalidNegotiationIDException {
		CancelNegotiation inParam = new CancelNegotiation();
        
        inParam.setNegotiationID(negotiationID);
        String[] reasons = new String[cancellationReason.size()];
        for (int i = 0; i < cancellationReason.size(); i++) {
            CancellationReason reason = cancellationReason.get(i);
            if (reason.equals(CancellationReason.LOST_CONVERGENCE_HOPE)) {
                reasons[i] = "LOST_CONVERGENCE_HOPE";
            }
            else if (reason.equals(CancellationReason.PARALLEL_NEGOTIATION_SUCCEEDED)) {
                reasons[i] = "PARALLEL_NEGOTIATION_SUCCEEDED";
            }
            else if (reason.equals(CancellationReason.STATUS_COMPROMISED)) {
                reasons[i] = "STATUS_COMPROMISED";
            }
            else if (reason.equals(CancellationReason.STATUS_COMPROMISED)) {
                reasons[i] = "STATUS_COMPROMISED";
            }
            else if (reason.equals(CancellationReason.WILL_RETURN_LATER)) {
                reasons[i] = "WILL_RETURN_LATER";
            }
        }
        inParam.setCancellationReason(reasons);

        try {
            return this.stub.cancelNegotiation(inParam).get_return();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (OperationInProgressExceptionException e) {
            e.printStackTrace();
            throw new OperationInProgressException(e.getMessage());
        }
        catch (OperationNotPossibleExceptionException e) {
            e.printStackTrace();
            throw new OperationNotPossibleException(e.getMessage());
        }
        catch (InvalidNegotiationIDExceptionException e) {
            e.printStackTrace();
            throw new InvalidNegotiationIDException(e.getMessage());
        }

        return false;
	}

	@Override
	public Customization customize(String arg0, Customization arg1) throws NegotiationException {
		// Webservice access of customize not made available yet. 
		return null;
	}



	@Override
	public SLA provision(UUID slaID) throws SLANotFoundException, ProvisioningException {
		 Provision inParam = new Provision();
	        inParam.setSlaID(slaID.getValue());
	        try {
	            if (delegator != null) {
	                return (SLA) delegator.parseSLA(this.stub.provision(inParam).get_return());
	            }
	            else {
	                return new SLASOIParser().parseSLA(this.stub.provision(inParam).get_return());
	            }
	        }
	        catch (RemoteException e) {
	            e.printStackTrace();
	        }
	        catch (ProvisioningExceptionException e) {
	            e.printStackTrace();
	            throw new ProvisioningException(e.getMessage());
	        }
	        catch (SLANotFoundExceptionException e) {
	            e.printStackTrace();
	            throw new SLANotFoundException(e.getMessage());
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	}

	@Override
	public String renegotiate(UUID slaID) throws SLANotFoundException, OperationNotPossibleException {
		Renegotiate inParam = new Renegotiate();
        inParam.setSlaID(slaID.getValue());
        try {
            return this.stub.renegotiate(inParam).get_return();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (OperationNotPossibleExceptionException e) {
            e.printStackTrace();
            throw new OperationNotPossibleException(e.getMessage());
        }
        catch (SLANotFoundExceptionException e) {
            e.printStackTrace();
            throw new SLANotFoundException(e.getMessage());
        }
        return null;
	}

	@Override
	public boolean terminate(UUID slaID, List<TerminationReason> terminationReason) throws SLANotFoundException {
        Terminate inParam = new Terminate();
        inParam.setSlaID(slaID.getValue());
        String[] reasons = new String[terminationReason.size()];
        for (int i = 0; i < terminationReason.size(); i++) {
            TerminationReason reason = terminationReason.get(i);
            if (reason.equals(TerminationReason.BUSINESS_DECISION)) {
                reasons[i] = "BUSINESS_DECISION";
            }
            else if (reason.equals(TerminationReason.COST)) {
                reasons[i] = "COST";
            }
            else if (reason.equals(TerminationReason.CUSTOMER_INITIATED)) {
                reasons[i] = "CUSTOMER_INITIATED";
            }
            else if (reason.equals(TerminationReason.DEMAND_DECREASED)) {
                reasons[i] = "DEMAND_DECREASED";
            }
            else if (reason.equals(TerminationReason.DEMAND_INCREASED)) {
                reasons[i] = "DEMAND_INCREASED";
            }
            else if (reason.equals(TerminationReason.FOUND_BETTER_CUSTOMER)) {
                reasons[i] = "FOUND_BETTER_CUSTOMER";
            }
            else if (reason.equals(TerminationReason.FOUND_BETTER_PROVIDER)) {
                reasons[i] = "FOUND_BETTER_PROVIDER";
            }
            else if (reason.equals(TerminationReason.PROVIDER_INITIATED)) {
                reasons[i] = "PROVIDER_INITIATED";
            }
            else if (reason.equals(TerminationReason.STATUS_COMPROMISED)) {
                reasons[i] = "STATUS_COMPROMISED";
            }
            else if (reason.equals(TerminationReason.UNSATISFACTORY_QUALITY)) {
                reasons[i] = "UNSATISFACTORY_QUALITY";
            }
        }

        inParam.setTerminationReason(reasons);

        try {
            return this.stub.terminate(inParam).get_return();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (SLANotFoundExceptionException e) {
            e.printStackTrace();
            throw new SLANotFoundException(e.getMessage());
        }
        return false;
	}
}
