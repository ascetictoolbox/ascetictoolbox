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

import integratedtoolkit.types.ResourceDestructionRequest;

/**
 * The RemoveCloudNodeRequest represents a notification of a resource
 * destruction that was part of the resource pool
 */
public class RemoveCloudNodeRequest extends TDRequest {

    /**
     * Description of the resource request that originated the creation
     */
    private ResourceDestructionRequest request;

    /**
     * Constructs a RemoveCloudNodeRequest with all its parameters
     *
     * @param resourceRequest Description of the resource request that has been
     * destroyed
     *
     */
    public RemoveCloudNodeRequest(ResourceDestructionRequest resourceRequest) {
        super(TDRequestType.REMOVE_CLOUD);
        this.request = resourceRequest;
    }

    /**
     * returns the resource destruction request that has ben accomplished
     *
     * @return The request the resource destruction request that has ben
     * accomplished
     */
    public ResourceDestructionRequest getRequest() {
        return request;
    }

    /**
     * Changes the resource destruction request that has ben accomplished
     *
     * @param request the resource destruction request that has ben accomplished
     */
    public void setRequest(ResourceDestructionRequest request) {
        this.request = request;
    }

}
