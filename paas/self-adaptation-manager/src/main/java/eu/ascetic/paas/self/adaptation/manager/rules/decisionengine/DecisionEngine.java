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

/**
 * The aim of this interface is to decide given an event that has been assessed 
 * what the magnitude of an adaptation should be used will be. It may also
 * have to decide to which VM this adaptation should occur.
 * @author Richard Kavanagh
 */
public interface DecisionEngine {
    
    /**
     * The aim of this is to decide the details such as where and by how much
     * an adaptation should occur. It may be the case that the maximum amount of
     * adaptation has already occurred so no further changes are possible. 
     * @param response The response to finalise details for.
     * @return The finalised response object
     */
    public Response decide(Response response);  
    
    /**
     * This sets the actuator to be used by the decision engine.
     * @param actuator The actuator.
     */
    public void setActuator(ActuatorInvoker actuator);
    
    /**
     * This gets the actuator to be used by the decision engine.
     * @return the actuator in use.
     */
    public ActuatorInvoker getActuator();    
    
}
