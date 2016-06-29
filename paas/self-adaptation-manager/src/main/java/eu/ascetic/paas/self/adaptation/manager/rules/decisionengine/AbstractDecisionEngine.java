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

import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.List;

/**
 * The aim of this class is to decide given an event that has been assessed what
 * the magnitude of an adaptation should be used will be. It may also have to
 * decide to which VM this adaptation should occur.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractDecisionEngine implements DecisionEngine {

    /**
     * The actuator is to be used as an information source, with a set of
     * standard questions that can be asked about how adaptation may occur. This
     * avoids the decision engine having to have interface specific code i.e.
     * Rest or ActiveMQ, it also prevents having to maintain multiple
     * connections for different purposes.
     */
    private ActuatorInvoker actuator;

    public AbstractDecisionEngine() {
    }

    @Override
    public void setActuator(ActuatorInvoker actuator) {
        this.actuator = actuator;
    }

    @Override
    public ActuatorInvoker getActuator() {
        return actuator;
    }

    /**
     * This tests to see if the power consumption limit will be breached or not
     * as well as the OVF boundaries.
     * @param response The response type to check
     * @param vmOvfType The OVF type to add to
     * @return If the VM is permissible to add.
     */
    public boolean getCanVmBeAdded(Response response, String vmOvfType) {
        if (actuator == null) {
            return false;
        }
        double averagePower = actuator.getAveragePowerUsage(response.getApplicationId(), response.getDeploymentId(), vmOvfType);
        double totalMeasuredPower = actuator.getTotalPowerUsage(response.getApplicationId(), response.getDeploymentId());
        List<String> vmOvfTypes = getActuator().getVmTypesAvailableToAdd(response.getApplicationId(), 
                response.getDeploymentId());
        if (!vmOvfTypes.contains(vmOvfType)) {
            return false;
        }
        //TODO compare to the guaranteed term for power.
        if (totalMeasuredPower + averagePower > Double.MAX_VALUE) {
            return false;
        }
        return true;
    }

}
