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
import java.util.Objects;

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

    private int id;
    private String name;
    private String ipAddress;
    private Host allocatedTo;
    private String state;
    private Calendar created;

    /**
     * This creates a new VM Deployed
     *
     * @param id The id of the VM as known by the source of information by the
     * energy modeller.
     * @param name The name of the VM as known by the source of information by
     * the energy modeller.
     */
    public VmDeployed(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * This takes a previously uninstantiated VM and adds the additional
     * information to represent the newly created VM.
     *
     * @param id The id of the VM as known by the source of information by the
     * energy modeller.
     * @param vm The previously uninstantiated VM.
     * @param ipAddress The ip address where the VM is located.
     * @param state The state of the VM
     * @param created The creation date of the VM
     * @param allocatedTo The host resource that the VM is allocated to.
     */
    public VmDeployed(int id, VM vm, String ipAddress, String state,
            Calendar created, Host allocatedTo) {
        super(vm);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = created;
        this.allocatedTo = allocatedTo;
    }

    /**
     * This gets the id associated with this VM.
     *
     * @return The vms id.
     */
    public int getId() {
        return id;
    }

    /**
     * This sets the id associated with this VM.
     *
     * @param id The vms id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * This gets the name this vm is known by.
     *
     * @return The VMs name
     */
    public String getName() {
        return name;
    }

    /**
     * This sets the name this vm is known by
     *
     * @param name The VMs name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This gets the ip address this vm is known by.
     *
     * @return The vms ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * This sets the ip address this vm is known by.
     *
     * @param ipAddress The vms ip address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * This gets the VMs state. The vms state.
     *
     * @return The vms state
     */
    public String getState() {
        return state;
    }

    /**
     * This sets the VMs state.
     *
     * @param state The vms state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * This gets the date the VM was instantiated.
     *
     * @return The boot time of the VM.
     */
    public Calendar getCreated() {
        return created;
    }

    /**
     * This sets the date the VM was instantiated.
     *
     * @param created The boot time of the VM.
     */
    public void setCreated(Calendar created) {
        this.created = created;
    }

    /**
     * This indicates which host this VM is allocated to.
     *
     * @return the allocatedTo The host this vm is allocated to
     */
    public Host getAllocatedTo() {
        return allocatedTo;
    }

    /**
     * This sets which host this VM is allocated to.
     *
     * @param allocatedTo The host this vm is allocated to
     */
    public void setAllocatedTo(Host allocatedTo) {
        this.allocatedTo = allocatedTo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VmDeployed) {
            VmDeployed vm = (VmDeployed) obj;
            return name.equals(vm.getName()) && id == vm.getId();
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }

}
