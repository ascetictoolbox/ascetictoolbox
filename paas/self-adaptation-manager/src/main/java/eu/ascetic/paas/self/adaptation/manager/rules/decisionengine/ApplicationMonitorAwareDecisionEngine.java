/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.rules.decisionengine;

import eu.ascetic.paas.self.adaptation.manager.activemq.listener.ApplicationManagerListener;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * This decision engine uses a more system aware approach to better decide what
 * the magnitude of an adaptation should be used will be. It may also have to
 * decide to which VM this adaptation should occur.
 *
 * @author Richard Kavanagh
 */
public class ApplicationMonitorAwareDecisionEngine extends AbstractDecisionEngine {

    ApplicationManagerListener datasource;

    public ApplicationMonitorAwareDecisionEngine() {
        try {
            this.datasource = new ApplicationManagerListener();
        } catch (JMSException ex) {
            Logger.getLogger(ApplicationMonitorAwareDecisionEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(ApplicationMonitorAwareDecisionEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Response decide(Response response) {
        /**
         * The app needs to be registered with the listener. 
         * the earlier this is done the more likely it can get the information
         * needed.
         */
        datasource.listenToApp(response.getApplicationId());
        if (response.getActionType().equals(Response.AdaptationType.ADD_VM)) {
            response = addVM(response);
        } else if (response.getActionType().equals(Response.AdaptationType.REMOVE_VM)) {
            response = deleteVM(response);
        }
        return response;
    }

    /**
     * The decision logic for adding a VM.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response deleteVM(Response response) {
        List<Integer> vmIds = getActuator().getVmIdsAvailableToRemove(response.getApplicationId(), response.getDeploymentId());
        if (!vmIds.isEmpty()) {
            String vmID = datasource.getLeastBusiestVmInApp(response.getApplicationId());
            response.setActionType(Response.AdaptationType.REMOVE_VM);
            response.setVmId(vmID + "");
            return response;
        } else {
            response.setAdaptationDetails("Could not find a VM to delete");
            response.setPossibleToAdapt(false);
        }
        return response;
    }

    /**
     * The decision logic for adding a VM.
     *
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response addVM(Response response) {
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(), response.getDeploymentId());
        if (!vmOvfTypes.isEmpty()) {
            String vmOvfType = datasource.getBusiestVmTypeInApp(response.getApplicationId());
            response.setActionType(Response.AdaptationType.ADD_VM);
            response.setAdaptationDetails(vmOvfType);
            return response;
        } else {
            response.setAdaptationDetails("Could not find a VM OVF type to add");
            response.setPossibleToAdapt(false);
            return response;
        }
    }

}
