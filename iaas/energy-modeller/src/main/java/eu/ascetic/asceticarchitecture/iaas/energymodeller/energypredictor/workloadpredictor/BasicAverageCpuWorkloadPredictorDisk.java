/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import java.util.Collection;

/**
 * This looks at a disk reference and returns the average CPU workload induced 
 * by VMs as its estimate of CPU workload.
 *
 * @author Richard Kavanagh
 */
public class BasicAverageCpuWorkloadPredictorDisk extends AbstractWorkloadEstimator {

    @Override
    public double getCpuUtilisation(Host host, Collection<VM> virtualMachines) {
        double vmCount = 0; //vms with disk refs
        double sumCpuUtilisation = 0;
        if (AbstractWorkloadEstimator.hasDiskReferences(virtualMachines)) {
            for (VM vm : virtualMachines) {
                if (!vm.getDiskImages().isEmpty()) {
                    sumCpuUtilisation = sumCpuUtilisation + getAverageCpuUtilisastion(vm);
                    vmCount = vmCount + 1;
                }
            }
            return sumCpuUtilisation / vmCount;
        } else {
            return 0;
        }
    }

    /**
     * This gets the average CPU utilisation for a VM given the disk image that it
     * has.
     *
     * @param vm The VM to get the average utilisation for.
     * @return The average utilisation of all disk images that a VM has.
     */
    public double getAverageCpuUtilisastion(VM vm) {
        double answer = 0.0;
        if (vm.getDiskImages().isEmpty()) {
            return answer;
        }
        for (VmDiskImage disk : vm.getDiskImages()) {
            answer = answer + database.getAverageCPUUtilisationDisk(disk.getDiskImage());
        }
        return answer / vm.getDiskImages().size();
    }

}