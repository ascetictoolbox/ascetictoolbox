/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package integratedtoolkit.types.request.td;

import integratedtoolkit.types.ScheduleDecisions;

/**
 * The SetNewScheduleRequest describes the optimizations on the scheduling and 
 * on the resource Management that the Scheduling optimizer suggest to the Task 
 * Dispatcher
 */
public class SetNewScheduleRequest extends TDRequest {

    /** Optimizer suggestions */
    private ScheduleDecisions newState;

    /**
     * Constructs a new SetNewScheduleRequest
     * @param newState Changes on the task schedule and the resource management
     */
    public SetNewScheduleRequest(ScheduleDecisions newState) {
        super(TDRequestType.SET_STATE);
        this.newState = newState;
    }

    /**
     * Describes the scheduling and resource management suggestions
     * @return Representation of the changes on the task schedule and the 
     * resource management
     */
    public ScheduleDecisions getNewState() {
        return newState;
    }

    /**
     * Modifies the scheduling and resource management suggestions
     * @param newState Changes on the task schedule and the resource management
     */
    public void setNewState(ScheduleDecisions newState) {
        this.newState = newState;
    }
}
