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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryWeekRecord;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This looks at disk references and returns the average CPU workload induced
 * by VMs as its estimate of CPU workload. It looks at the day and time of week
 * in order to produce this estimate.
 *
 * @author Richard Kavanagh
 */
public class DoWAverageCpuWorkloadPredictorDisk extends AbstractWorkloadEstimator {

    private int bootHistoryBucketSize = 500;

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
     * This gets the average CPU utilisation for a VM given the disk references 
     * that it has.
     *
     * @param vm The VM to get the average utilisation for.
     * @return The average utilisation of all disk references that a VM has.
     */
    public double getAverageCpuUtilisastion(VM vm) {
        double answer = 0.0;
        if (vm.getDiskImages().isEmpty()) {
            return answer;
        }
        for (VmDiskImage disk : vm.getDiskImages()) {
            if (vm.getClass().equals(VmDeployed.class)) {
                List<VmLoadHistoryWeekRecord> weekRecord = database.getAverageCPUUtilisationWeekTraceForDisk(
                        disk.getDiskImage());
                answer = answer + getLoad(weekRecord);
            } else {
                answer = answer + database.getAverageCPUUtilisationDisk(disk.getDiskImage());
            }
        }
        return answer / vm.getDiskImages().size();
    }

    /**
     * This gets the load for the current day and hour of the week.
     * @param records The records to search through to get the hour and day of
     * the week.
     * @return The average load for the given day and time of week. If this
     * record is not found the average of all values in the record set is 
     * returned instead.
     */
    public double getLoad(List<VmLoadHistoryWeekRecord> records) {
        double sum = 0;
        GregorianCalendar cal = new GregorianCalendar();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        for (VmLoadHistoryWeekRecord record : records) {
            if (record.getDayOfWeek() == dayOfWeek && record.getHourOfDay() == hourOfDay) {
                return record.getLoad();
            }
            sum = sum + record.getLoad();
        }
        return sum / records.size();
    }

    /**
     * This sets the boot history discrete time bucket size.
     *
     * @return the bootHistoryBucketSize
     */
    public int getBootHistoryBucketSize() {
        return bootHistoryBucketSize;
    }

    /**
     * This sets the boot history discrete time bucket size.
     *
     * @param bootHistoryBucketSize The bucket size is the time in seconds that
     * each discrete time bucket represents.
     */
    public void setBootHistoryBucketSize(int bootHistoryBucketSize) {
        this.bootHistoryBucketSize = bootHistoryBucketSize;
    }
    
    @Override
    public boolean requiresVMInformation() {
        return true;
    }    

}
