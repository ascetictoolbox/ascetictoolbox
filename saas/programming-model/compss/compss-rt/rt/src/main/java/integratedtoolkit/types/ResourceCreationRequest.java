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
package integratedtoolkit.types;

/**
 *
 * @author flordan
 */
public class ResourceCreationRequest {

    private final ResourceDescription requested;
    private final String provider;
    private final int[] requestedSimultaneousTaskCount;
    private ResourceDescription granted;

    public ResourceCreationRequest(ResourceDescription requestedResource, int[] simultaneousTasks, String provider) {
        requested = requestedResource;
        this.provider = provider;
        requestedSimultaneousTaskCount = simultaneousTasks;
        granted = new ResourceDescription();

    }

    public void grant(ResourceDescription grantedResource) {
        granted = grantedResource;
    }

    public int[] requestedSimultaneousTaskCount() {
        return requestedSimultaneousTaskCount;
    }

    public ResourceDescription getRequested() {
        return requested;
    }

    public ResourceDescription getGranted() {
        return granted;
    }

    public void setGranted(ResourceDescription granted) {
        this.granted = granted;
    }

    public String getProvider() {
        return provider;
    }

}
