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

import integratedtoolkit.types.ResourceCreationRequest;

/**
 * The AddCloudNodeRequest represents a request to add a new resource ready to 
 * execute to the resource pool
 */
public class AddCloudNodeRequest extends TDRequest {

    /** Name that identifies the new resource */
    private String name;
    /** Name of the provider who owns the resource */
    private String provider;
    /** Description of the resource request that originated the creation */
    private ResourceCreationRequest request;
    /** Max amount of task that can be executing on the resource concurrently */
    private int limitOfTasks;
    /** Before adding the resource to the pool must check if it's useful */
    private boolean check;

    /** 
     * Constructs a AddCloudNodeRequest with all its parameters 
     * 
     * @param name Name that identifies the new resource
     * @param provider Description of the resource request that originated 
     * the creation
     * @param resourceRequest Description of the resource request that originated the creation
     * @param limitOfTasks Max amount of task that can be executing on the resource concurrently
     * @param check its utility must be checked before its addition
     * 
     */
    public AddCloudNodeRequest(String name, String provider, ResourceCreationRequest resourceRequest, int limitOfTasks, boolean check) {
        super(TDRequestType.ADD_CLOUD);
        this.provider = provider;
        this.limitOfTasks = limitOfTasks;
        this.name = name;
        this.request = resourceRequest;
        this.check = check;
    }

    /** 
     * Returns the name that identifies the resource
     * 
     * @return the name that identifies the resource
     */
    public String getName() {
        return name;
    }

    /** 
     * Modifies the name that identifies the new resources 
     * 
     * @param name new name for the resource
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /** 
     * Returns the max amount of task that can be executing on the resource
     * concurrently
     * 
     * @return the max amount of task that can be executing on the resource
     * concurrently
     */
    public int getLimitOfTasks() {
        return limitOfTasks;
    }

    /** 
     * Sets the max amount of task that can be executing on the resource
     * concurrently
     * 
     * @param limitOfTasks the new max amount of task that can be executing on the resource
     * concurrently value
     */
    public void setLimitOfTasks(int limitOfTasks) {
        this.limitOfTasks = limitOfTasks;
    }

    /** 
     * returns the description of the resource request that originated the
     * creation
     * 
     * @return the description of the resource request that originated the
     * creation
     */
    public ResourceCreationRequest getRequest() {
        return request;
    }

    /** 
     * Changes the description of the resource request that originated the
     * creation
     * 
     * @param request the new request description that originated the creation
     */
    public void setResourceRequest(ResourceCreationRequest request) {
        this.request = request;
    }

    /** 
     * returns true if the utility of the resource must be checked before adding
     * it to the resource pool.
     * 
     * @return true if the utility of the resource must be checked before adding
     * it to the resource pool.
     */
    public boolean getCheck() {
        return check;
    }

    /** 
     * Sets if the utility of the resource must be checked before adding
     * it to the resource pool 
     * 
     * @param check must be checked
     */
    public void setCheck(boolean check) {
        this.check = check;
    }

    /** 
     * returns the name of the provider who owns the resource
     * 
     * @return the name of the provider who owns the resource
     */
    public String getProvider() {
        return provider;
    }

    /** 
     * Sets the name of the provider who owns the resource
     * 
     * @param provider the name of the provider who owns the resource
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
}
