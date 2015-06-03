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
import eu.ascetic.paas.self.adaptation.manager.EventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the standard interface for all event assessors.
 *
 * @author Richard Kavanagh
 */
public interface EventAssessor {

    /**
     * This assesses an event and decides if a response is required. If no
     * response is required then null is returned.
     *
     * @param event The SLA event to assess
     * @param sequence The sequence of similar events that have been assessed.
     * @param recentAdaptation A list of recent relevant adaptations that have
     * occurred.
     * @return A response object in cases where an adaptive response is
     * required.
     */
    public Response assessEvent(EventData event, List<EventData> sequence, List<Response> recentAdaptation);

    /**
     * This assesses an event and decides if a response is required. If no
     * response is required then null is returned. Calling this is equivalent to
     * calling the method assessEvent(EventData event, List sequence) but in
     * this case the event sequence list is maintained by the event assessor.
     *
     * @param event The SLA event to assess
     * @return A response object in cases where an adaptive response is
     * required.
     */
    public Response assessEvent(EventData event);

    /**
     * This gets the event assessors internal list of event listeners
     *
     * @return the list of event listeners the event assessor uses
     */
    public ArrayList<EventListener> getListeners();

    /**
     * This sets the event assessors internal list of event listeners
     *
     * @param listeners the listeners to set
     */
    public void setListeners(ArrayList<EventListener> listeners);

    /**
     * This adds a listener to the event assessors internal list of event
     * listeners
     *
     * @param listener The listener to add
     */
    public void addListeners(EventListener listener);

    /**
     * This clears the event assessors internal list of event listeners
     */
    public void clearListeners();

    /**
     * This gets the event assessors internal list of actuators
     *
     * @return the list of actuators the event assessor uses
     */
    public ArrayList<ActuatorInvoker> getActuators();

    /**
     * This sets the event assessors internal list of actuators
     *
     * @param actuators the actuators to set
     */
    public void setActuators(ArrayList<ActuatorInvoker> actuators);

    /**
     * This adds an actuator to the event assessors internal list of actuators
     *
     * @param actuator The actuator to add
     */
    public void addActuators(ActuatorInvoker actuator);

    /**
     * This clears the event assessors internal list of actuators
     */
    public void clearActuators();

    /**
     * This starts the event history maintenance routines in the event assessor.
     */
    public void start();

    /**
     * This stops the event history maintenance routines in the event assessor.
     */
    public void stop();

}
