package eu.ascetic.paas.slam.poc.replan;

import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.gslam.core.negotiation.INegotiation.OperationNotPossibleException;
import org.slasoi.gslam.core.negotiation.INegotiation.SLANotFoundException;
import org.slasoi.slamodel.primitives.UUID;

import eu.ascetic.paas.slam.poc.provision.PlanHandlerImpl;

/**
 * The implementation of ReplanImpl.
 * 
 */
public class ReplanImpl {
    public void rePlan(UUID uuid){
        try {
            String message = PlanHandlerImpl.context.getProtocolEngine().getINegotiation().renegotiate(uuid);
            // handle message?
        }
        catch (SLANotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (OperationNotPossibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SLAManagerContextException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
