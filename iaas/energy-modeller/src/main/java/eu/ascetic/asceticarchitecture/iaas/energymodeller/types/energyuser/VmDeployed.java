/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import java.util.Calendar;

/**
 * This class represents an energy user of the ASCETiC project and in particular
 * a VM that has been deployed.
 *
 * It is somehow similar in nature to:
 *
 * @see es.bsc.vmmanagercore.model.VmDeployed
 * @author Richard Kavanagh
 *
 */
public class VmDeployed extends VM {

    private String id;
    private String ipAddress;
    private Host allocatedTo;
    private String state;
    private Calendar created;

    /**
     *
     * @param name
     * @param image
     * @param cpus
     * @param ramMb
     * @param diskGb
     * @param id
     * @param ipAddress
     * @param state
     * @param created
     */
    public VmDeployed(String name, String image, int cpus, int ramMb,
            int diskGb, String initScript, String applicationId, String id,
            String ipAddress, String state, Calendar created, Host allocatedTo) {
        super(name, image, cpus, ramMb, diskGb);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = created;
        this.allocatedTo = allocatedTo;
    }

    /**
     * This takes a previously uninstantiated VM and adds the additional 
     * information to represent the newly created VM.
     * @param vm
     * @param id
     * @param ipAddress
     * @param state
     * @param created
     * @param allocatedTo 
     */
    public VmDeployed(VM vm, String id, String ipAddress, String state, 
            Calendar created, Host allocatedTo) {
        super(vm);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = created;
        this.allocatedTo = allocatedTo;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     *
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     * The date the VM was instantiated.
     *
     * @return
     */
    public Calendar getCreated() {
        return created;
    }

    /**
     * @return the allocatedTo
     */
    public Host getAllocatedTo() {
        return allocatedTo;
    }

    /**
     * @param allocatedTo the allocatedTo to set
     */
    public void setAllocatedTo(Host allocatedTo) {
        this.allocatedTo = allocatedTo;
    }
}
