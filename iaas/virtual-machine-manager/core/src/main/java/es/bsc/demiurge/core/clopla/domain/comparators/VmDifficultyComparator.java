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

package es.bsc.demiurge.core.clopla.domain.comparators;

import es.bsc.demiurge.core.clopla.domain.Vm;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class implements the "difficulty" comparison of two VMs. VMs that demand more resources are considered to be
 * more "difficult" because it is more difficult to find a host with enough resources to deploy them.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class VmDifficultyComparator implements Comparator<Vm>, Serializable {

    /**
     * This function compares the "difficulty" of two VMs.
     *
     * @param vm1 a VM
     * @param vm2 a VM
     * @return a negative number if vm1 is less difficult than vm2, a positive number if vm1 is more difficult than
     * vm2, 0 if they are equal
     */
    @Override
    public int compare(Vm vm1, Vm vm2) {
        return Double.compare(difficulty(vm1), difficulty(vm2));
    }

    /**
     * This function calculate the difficulty of a VM.
     * This is the formula used to calculate the difficulty: vm.cpus * (vm.ramMb/1000) * (vm.diskGb/100).
     * The memory and the disk capacity are divided in the formula because it would not be fair to give the same
     * weight to 1 CPU than to 1 MB or RAM.
     */
    private double difficulty(Vm vm) {
        return vm.getNcpus()*(vm.getRamMb()/1000.0)*(vm.getDiskGb()/100.0);
    }

}