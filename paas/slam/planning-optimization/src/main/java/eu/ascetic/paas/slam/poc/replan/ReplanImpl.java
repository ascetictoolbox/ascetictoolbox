/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
