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

package integratedtoolkit.types.request.tp;

import integratedtoolkit.types.request.Request;

/**
 * The TPRequest class represents any interacction with the TaskProcessor
 * component.
 */
public class TPRequest extends Request {

    /**
     * Contains the different types of request that the Task Processor can 
     * response.
     */
    public enum TPRequestType {

        ANALYSE_TASK,
        UPDATE_GRAPH,
        WAIT_FOR_TASK,
        END_OF_APP,
        ALREADY_ACCESSED,
        REGISTER_DATA_ACCESS,
        NEW_VERSION_SAME_VALUE,
        IS_OBJECT_HERE,
        SET_OBJECT_VERSION_VALUE,
        GET_LAST_RENAMING,
        GET_LAST_DATA_ACCESS,
        BLOCK_AND_GET_RESULT_FILES,
        UNBLOCK_RESULT_FILES,
        SHUTDOWN,
        GRAPHSTATE,
        TASKSTATE,
        DELETE_FILE,        
        DEBUG
    }
    /** Type of the request instance. */
    private TPRequestType requestType;

    /** 
     * Constructs a new TPrequest for that type
     * @param requestType new request type name
     * 
     */
    public TPRequest(TPRequestType requestType) {
        this.requestType = requestType;
    }

    /** 
     * returns the type of request for this instance 
     * @result return the request type name of this instance
     * 
     */
    public TPRequestType getRequestType() {
        return requestType;
    }
}
