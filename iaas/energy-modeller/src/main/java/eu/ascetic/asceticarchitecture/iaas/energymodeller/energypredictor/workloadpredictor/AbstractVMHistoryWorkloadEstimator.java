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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryBootRecord;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Derivative of this class look at properties of the VMs in order to help
 * forecast the workload of the physical host.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractVMHistoryWorkloadEstimator extends AbstractWorkloadEstimator {

    /**
     * This gets the average CPU utilisation for a VM given the app tags that it
     * has.
     *
     * @param vm The VM to get the average utilisation for.
     * @return The average utilisation of all application tags that a VM has.
     */
    public abstract VmLoadHistoryRecord getAverageCpuUtilisation(VM vm);

    @Override
    public boolean requiresVMInformation() {
        return true;
    }

    /**
     * This indicates if all VMs in the collection have application tags.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * application tags.
     * @return If the VMs all have application tags or not. True only if all VMs
     * have tags or the collection is empty.
     */
    public static boolean hasAppTags(Collection<VM> virtualMachines) {
        for (VM vm : virtualMachines) {
            if (vm.getApplicationTags().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This indicates if all VMs in the collection have application tags.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * application tags.
     * @param validList The list of app tags that should belong to this set of
     * VMs
     * @return If the VMs all have application tags or not. True only if all VMs
     * have tags or the collection is empty.
     */
    public static boolean hasAppTags(Collection<VM> virtualMachines, HashSet<String> validList) {
        for (VM vm : virtualMachines) {
            if (vm.getApplicationTags().isEmpty() && !Collections.disjoint(vm.getApplicationTags(), validList)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This gets a list of all application tags that a collection of VMs has
     *
     * @param virtualMachines The virtual machines to get the application tags
     * from.
     * @return The set of all application tags known belonging to the VMs.
     */
    public static HashSet<String> getAppTags(Collection<VM> virtualMachines) {
        HashSet<String> answer = new HashSet<>();
        for (VM vm : virtualMachines) {
            answer.addAll(vm.getApplicationTags());
        }
        return answer;
    }

    /**
     * This indicates if all VMs in the collection have disk references.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * disk references.
     * @return If the VMs all have disk references or not. True only if all VMs
     * have disk references or the collection is empty.
     */
    public static boolean hasDiskReferences(Collection<VM> virtualMachines) {
        for (VM vm : virtualMachines) {
            if (vm.getDiskImages().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This indicates if all VMs in the collection have disk references.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * disk references.
     * @param validList The list of disk references that should belong to this
     * set of VMs
     * @return If the VMs all have disk references or not. True only if all VMs
     * have disk references or the collection is empty.
     */
    public static boolean hasDiskReferences(Collection<VM> virtualMachines, HashSet<String> validList) {
        for (VM vm : virtualMachines) {
            HashSet<VmDiskImage> images = vm.getDiskImages();
            HashSet<String> imageRefs = new HashSet<>();
            for (VmDiskImage image : images) {
                imageRefs.add(image.getDiskImage());
            }
            if (vm.getDiskImages().isEmpty() && !Collections.disjoint(imageRefs, validList)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This gets a list of all disk references that a collection of VMs has
     *
     * @param virtualMachines The virtual machines to get the disk references
     * from.
     * @return The set of all disk references known belonging to the VMs.
     */
    public static HashSet<VmDiskImage> getDiskReferences(Collection<VM> virtualMachines) {
        HashSet<VmDiskImage> answer = new HashSet<>();
        for (VM vm : virtualMachines) {
            answer.addAll(vm.getDiskImages());
        }
        return answer;
    }

    /**
     * In a list such as the boot history, that is divided into discrete time,
     * this method indicates which bucket a given time interval is contained
     * inside.
     *
     * @param bucketSize The size in seconds for each bucket
     * @param timeValue the time which the correct bucket is to be found for
     * @return The correct bucket for the specified time value
     */
    public static int getIndexPosition(int bucketSize, int timeValue) {
        return timeValue / bucketSize;
    }

    /**
     * This gets the boot history position of a given record.
     *
     * @param bootHistory The entire boot history of a VM
     * @param bucketSize The size in seconds of each discrete time bucket
     * @param vm The vm that has been deployed and has its created date set.
     * @return The boot history record for the provided index
     */
    public static VmLoadHistoryBootRecord getBootHistoryValue(List<VmLoadHistoryBootRecord> bootHistory, int bucketSize, VmDeployed vm) {
        long created = TimeUnit.MILLISECONDS.toSeconds(vm.getCreated().getTimeInMillis());
        long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        return bootHistory.get(getIndexPosition(bucketSize, (int) (created - now)));
    }

    /**
     * This gets the boot history position of a given record.
     *
     * @param bootHistory The entire boot history of a VM
     * @param bucketSize The size in seconds of each discrete time bucket
     * @param timeValue the time which the correct bucket is to be found for
     * @return The boot history record for the provided index
     */
    public static VmLoadHistoryBootRecord getBootHistoryValue(List<VmLoadHistoryBootRecord> bootHistory, int bucketSize, int timeValue) {
        return bootHistory.get(getIndexPosition(bucketSize, timeValue));
    }

    /**
     * This gets the boot history position of a given record.
     *
     * @param bootHistory The entire boot history of a VM
     * @param position The position in the index to return.
     * @return The boot history record for the provided index.
     */
    public static VmLoadHistoryBootRecord getBootHistoryValue(List<VmLoadHistoryBootRecord> bootHistory, int position) {
        return bootHistory.get(position);
    }
    
    /**
     * This gets the user defined name for this workload estimator
     * @return The name of this predictor
     */
    public abstract String getName();
       
}
