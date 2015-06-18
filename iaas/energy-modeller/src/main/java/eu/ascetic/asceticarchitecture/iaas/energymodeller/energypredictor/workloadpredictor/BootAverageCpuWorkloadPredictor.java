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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryBootRecord;
import java.util.Collection;
import java.util.List;

/**
 * This looks at an application tag and returns the average CPU workload induced
 * by VMs as its estimate of CPU workload.
 *
 * @author Richard Kavanagh
 */
public class BootAverageCpuWorkloadPredictor extends AbstractWorkloadEstimator {

    private int bootHistoryBucketSize = 500;
    
    @Override
    public double getCpuUtilisation(Host host, Collection<VM> virtualMachines) {
        double vmCount = 0; //vms with app tags
        double sumCpuUtilisation = 0;
        if (AbstractWorkloadEstimator.hasAppTags(virtualMachines)) {
            for (VM vm : virtualMachines) {
                if (!vm.getApplicationTags().isEmpty()) {
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
     * This gets the average CPU utilisation for a VM given the app tags that it
     * has.
     *
     * @param vm The VM to get the average utilisation for.
     * @return The average utilisation of all application tags that a VM has.
     */
    public double getAverageCpuUtilisastion(VM vm) {
        double answer = 0.0;
        if (vm.getApplicationTags().isEmpty()) {
            return answer;
        }
        for (String tag : vm.getApplicationTags()) {
            if (vm.getClass().equals(VmDeployed.class)) {
                List<VmLoadHistoryBootRecord> bootRecord = database.getAverageCPUUtilisationBootTraceForTag(
                        tag,
                        bootHistoryBucketSize);
                answer = answer + AbstractWorkloadEstimator.getBootHistoryValue(bootRecord,
                        bootHistoryBucketSize,
                        (VmDeployed) vm).getLoad();
            } else {
                answer = answer + database.getAverageCPUUtilisationTag(tag);
            }
        }
        return answer / vm.getApplicationTags().size();
    }
    
    /**
     * This sets the boot history discrete time bucket size.
     * @return the bootHistoryBucketSize
     */
    public int getBootHistoryBucketSize() {
        return bootHistoryBucketSize;
    }

    /**
     * This sets the boot history discrete time bucket size.
     * @param bootHistoryBucketSize The bucket size is the time in
     * seconds that each discrete time bucket represents.
     */
    public void setBootHistoryBucketSize(int bootHistoryBucketSize) {
        this.bootHistoryBucketSize = bootHistoryBucketSize;
    }    

}