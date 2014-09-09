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

import integratedtoolkit.types.request.Request;

/**
 * The TDRequest class represents any interacction with the TaskDispatcher
 * component.
 */
public class TDRequest extends Request {

    /**
     * Contains the different types of request that the Task Dispatcher can 
     * response.
     */
    public enum TDRequestType {
    	UPDATE_LOCAL_CEI,
        SCHEDULE_TASKS,
        FINISHED_TASK,
        RESCHEDULE_TASK,
        NEW_WAITING_TASK,
        NEW_DATA_VERSION,
        TRANSFER_OPEN_FILE,
        TRANSFER_RAW_FILE,
        TRANSFER_OBJECT,
        TRANSFER_RESULT_FILES,
        TRANSFER_TRACE_FILES,
        DELETE_INTERMEDIATE_FILES,
        GET_STATE,
        SET_STATE,
        ADD_CLOUD,
        REMOVE_OBSOLETES,
        REFUSE_CLOUD,
        MONITOR_DATA,
        SHUTDOWN,
        REMOVE_CLOUD,
        DEBUG
    }
    /** Type of the request instance. */
    private TDRequestType requestType;

    /** 
     * Cosntructs a new TDRequest for that kind of notification 
     * @param requestType new request type name
     * 
     */
    public TDRequest(TDRequestType requestType) {
        this.requestType = requestType;
    }

    /** 
     * returns the type of request for this instance 
     * @result return the request type name of this instance
     * 
     */
    public TDRequestType getRequestType() {
        return requestType;
    }
}
