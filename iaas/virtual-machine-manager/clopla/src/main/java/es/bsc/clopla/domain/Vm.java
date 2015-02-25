/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.clopla.domain;

import es.bsc.clopla.domain.comparators.HostStrengthComparator;
import es.bsc.clopla.domain.comparators.VmDifficultyComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * This class represents a virtual machine.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
@PlanningEntity(difficultyComparatorClass = VmDifficultyComparator.class)
public class Vm extends AbstractPersistable {

    private int ncpus;
    private int ramMb;
    private int diskGb;
    private String appId;
    private Host host; // The host where the Vm should be deployed according to the planner.
    private String alphaNumericId; /* This might be needed in some cases. For example, OpenStack uses alphanumeric
                                   IDs, and optaplanner needs an ID of type long. */

    public Vm() { } // OptaPlanner needs no arg constructor to clone

    public static class Builder {
        
        // Required parameters
        private final Long id;
        private final int ncpus;
        private final int ramMb;
        private final int diskGb;
        
        // Optional parameters
        private String appId;
        private String alphaNumericId;
        
        public Builder (Long id, int ncpus, int ramMb, int diskGb) {
            this.id = id;
            this.ncpus = ncpus;
            this.ramMb = ramMb;
            this.diskGb = diskGb;
        }
        
        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }
        
        public Builder alphaNumericId(String alphaNumericId) {
            this.alphaNumericId = alphaNumericId;
            return this;
        }
        
        public Vm build() {
            return new Vm(this);
        }
        
    }
    
    private Vm(Builder builder) {
        id = builder.id;
        ncpus = builder.ncpus;
        ramMb = builder.ramMb;
        diskGb = builder.diskGb;
        appId = builder.appId;
        alphaNumericId = builder.alphaNumericId;
    }

    /**
     * Checks whether this VM is deployed in the same host as the given one.
     *
     * @param vm the given VM
     * @return True if the two VMs are deployed in the same host, False otherwise.
     */
    public boolean isInTheSameHost(Vm vm) {
        if (vm.getHost() == null && host == null) {
            return true;
        }
        else if (host != null && vm.getHost() != null) {
            return host.getId().equals(vm.getHost().getId());
        }
        return false;
    }
    
    public int getNcpus() {
        return ncpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public String getAppId() {
        return appId;
    }

    public String getAlphaNumericId() {
        return alphaNumericId;
    }

    @PlanningVariable(valueRangeProviderRefs = {"hostRange"}, strengthComparatorClass = HostStrengthComparator.class)
    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "VM - ID:" + id.toString() + ", cpus:" + ncpus + ", ram:" + ramMb + ", disk:" + diskGb
                + ", app:" + appId;
    }

}
