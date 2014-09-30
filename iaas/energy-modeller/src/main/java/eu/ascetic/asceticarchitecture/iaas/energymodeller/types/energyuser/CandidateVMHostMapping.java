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

import java.util.Objects;

/**
 * This gives the energy usage for a potential mapping between a VM and its
 * underlying resource.
 *
 * @author Richard
 */
public class CandidateVMHostMapping extends EnergyUsageSource {

    private final VM vm;
    private final Host host;

    /**
     * This creates a mapping between a VM and a candidate underlying resource.
     *
     * @param vm The vm to be part of the mapping.
     * @param host The host to be used as part of the mapping.
     */
    public CandidateVMHostMapping(VM vm, Host host) {
        this.vm = vm;
        this.host = host;
    }

    /**
     * This returns the VM that is part of this mapping.
     * @return the vm used in this mapping.
     */
    public VM getVm() {
        return vm;
    }

    /**
     * This returns the host that is part of this mapping.
     * @return the host that is part of the mapping.
     */
    public Host getHost() {
        return host;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CandidateVMHostMapping) {
            CandidateVMHostMapping other = (CandidateVMHostMapping) obj;
            if ((this.host.getHostName().equals(other.getHost().getHostName()))
                    && (this.vm.getCpus() == other.getVm().getCpus())
                    && (this.vm.getRamMb() == other.getVm().getRamMb())
                    && (this.vm.getDiskGb() == other.getVm().getDiskGb())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.vm);
        hash = 43 * hash + Objects.hashCode(this.host);
        return hash;
    }

}
