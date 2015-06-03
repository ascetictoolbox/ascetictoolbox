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
package eu.ascetic.paas.self.adaptation.manager.rules;

import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;

/**
 * This class represents a valid response that the self-adaptation manager can
 * use in order to respond to the incoming events.
 * @author Richard Kavanagh
 */
public class Response {

    private ActuatorInvoker actuator;
    private EventData eventCause;
    private String message;

    /**
     * This creates a standard response object. It indicates which actuator
     * to use and which message to send to it.
     * @param actuator The actuator used to invoke the change.
     * @param eventCause A copy of the incoming event that caused the response to 
     * fire.
     * @param message 
     */
    public Response(ActuatorInvoker actuator, EventData eventCause, String message) {
        this.actuator = actuator;
        this.eventCause = eventCause;
        this.message = message;
    }
    
    /**
     * This gets the time of arrival of the event that caused the response.
     * @return 
     */
    public long getTime() {
        return eventCause.getTime();
    }
    
}
