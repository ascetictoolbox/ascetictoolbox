/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

import integratedtoolkit.types.ResourceCreationRequest;

/**
 * The RefuseCloudWorkerRequest class represents the notification of the
 * impossibility to create a requested resource
 */
public class RefuseCloudWorkerRequest extends TDRequest {

    private ResourceCreationRequest request;
    /**/
    private String provider;

    /**
     * Contructs a new RefuseCloudWorkerRequest
     *
     * @param request description of the request of resources
     * @param provider provider that dealed with the request
     */
    public RefuseCloudWorkerRequest(ResourceCreationRequest request, String provider) {
        super(TDRequestType.REFUSE_CLOUD);
        this.request = request;
        this.provider = provider;
    }

    /**
     * Returns the description of the request of resources
     *
     * @return the description of the request of resources
     */
    public ResourceCreationRequest getRequest() {
        return request;
    }

    /**
     * Sets adescription of the request of resources
     *
     * @param request description of the request of resources
     */
    public void setRequestedTaskCount(ResourceCreationRequest request) {
        this.request = request;
    }

    /**
     * Returns the provider associated to the refused request
     *
     * @return name of the provider associated to the redused request
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * Sets the provider associated to the refused request
     *
     * @param provider name of the provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
}
