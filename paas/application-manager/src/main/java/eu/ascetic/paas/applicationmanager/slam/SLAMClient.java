package eu.ascetic.paas.applicationmanager.slam;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SyntaxConverterDelegator;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.applicationmanager.slam.stub.OperationNotPossibleExceptionException;
/**
 * Application Manager client to the ASCETiC PaaS SLA Manager server
 * @author David Garcia Perez - Atos
 *
 */
public class SLAMClient {
	private BZNegotiationStub stub;
	protected SyntaxConverterDelegator delegator;
	
    public SLAMClient(String epr) throws AxisFault {
        super();
        this.stub = new BZNegotiationStub(epr);
        this.delegator = new SyntaxConverterDelegator(SyntaxConverterType.SLASOISyntaxConverter);
    }
	
	public String initiateNegotiation(SLATemplate slaTemplate) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
