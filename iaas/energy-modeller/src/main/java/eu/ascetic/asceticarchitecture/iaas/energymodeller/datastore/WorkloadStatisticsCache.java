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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor.AbstractVMHistoryWorkloadEstimator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The aim of this is to generate a running average counter for various workload
 * metrics. This enables faster querying than a database oriented approach by
 * maintaining statics such as averages in memory.
 *
 * @author Richard Kavanagh
 */
public class WorkloadStatisticsCache {

    private boolean inUse = false;

    //Running averages since the last restart of the energy modeller.
    private HashMap<String, RunningAverage> tagAverage = new HashMap<>();
    private HashMap<String, RunningAverage> diskAverage = new HashMap<>();
    /**
     * The next set are for time from boot records (first field tag second boot
     * index. If the Energy modeller is restarted there is a notion that VMs
     * that have already started will not have data for earlier parts of their
     * traces, thus a hashmap is used rather than a simple list.
     */
    private static final int BOOT_BUCKET_SIZE = 3600; //time in seconds to make each bucket
    private HashMap<String, HashMap<Integer, RunningAverage>> tagBootAverage = new HashMap<>();
    private HashMap<String, HashMap<Integer, RunningAverage>> diskBootAverage = new HashMap<>();

    /**
     * The next set are for day of week records (first field tag second day of
     * week. If the Energy modeller is restarted there is a notion that VMs that
     * have already started will not have data for earlier parts of their
     * traces, thus a hashmap is used rather than a simple list.
     */
    private HashMap<String, HashMap<DayOfWeek, RunningAverage>> tagDoWAverage = new HashMap<>();
    private HashMap<String, HashMap<DayOfWeek, RunningAverage>> diskDoWAverage = new HashMap<>();

    /**
     * SingletonHolder is loaded on the first execution of
     * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {

        private static final WorkloadStatisticsCache INSTANCE = new WorkloadStatisticsCache();
    }

    /**
     * This creates a new singleton instance of the workload statistics cache.
     *
     * @return A singleton instance of a the workload statistics cache.
     */
    public static WorkloadStatisticsCache getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * This adds a set of VM measurements to the current cached values in
     * memory.
     *
     * @param vmMeasurements The measurements to cache a summary of
     */
    public void addVMToStatistics(List<VmMeasurement> vmMeasurements) {
        for (VmMeasurement measurement : vmMeasurements) {
            HashSet<String> tags = measurement.getVm().getApplicationTags();
            HashSet<VmDiskImage> disks = measurement.getVm().getDiskImages();
            double cpuUtil = measurement.getCpuUtilisation();
            long timeFromBoot = measurement.getVm().getTimeFromBoot();
            for (String tag : tags) {
                addRunningAverageForVMAppTag(tag, cpuUtil);
                addRunningBootAverageForAppTag(tag, cpuUtil, timeFromBoot);
                addRunningDoWAverageForAppTag(tag, cpuUtil);
            }
            for (VmDiskImage disk : disks) {
                addRunningAverageForVMDisk(disk, cpuUtil);
                addRunningBootAverageForVMDisk(disk, cpuUtil, timeFromBoot);
                addRunningDoWAverageForVMDisk(disk, cpuUtil);
            }
        }
    }

    /**
     * This given a VM with disk reference will find historical information
     * associated with the disk reference.
     *
     * @param vm The VM to get the information for
     * @return
     */
    public double getUtilisationforDisks(VM vm) {
        double answer = 0;
        for (VmDiskImage disk : vm.getDiskImages()) {
            answer = answer + tagAverage.get(disk.getDiskImage()).getAverage();
        }
        return answer / vm.getDiskImages().size();
    }

    /**
     * This given a VM with disk reference will find historical information
     * associated with the disk reference.
     *
     * @param vm The VM to get the information for
     * @return
     */
    public double getBootUtilisationforDisks(VM vm) {
        double answer = 0;
        int bootIndex = 0;
        if (vm instanceof VmDeployed) {
            long timeFromBoot = ((VmDeployed) vm).getTimeFromBoot();
            bootIndex = AbstractVMHistoryWorkloadEstimator.getIndexPosition(BOOT_BUCKET_SIZE, (int) timeFromBoot);
        }
        for (VmDiskImage disk : vm.getDiskImages()) {
            answer = answer + diskBootAverage.get(disk.getDiskImage()).get(bootIndex).getAverage();
        }
        return answer / vm.getDiskImages().size();
    }
    
    /**
     * This given a VM with disk reference will find historical information
     * associated with the disk reference.
     *
     * @param vm The VM to get the information for
     * @return
     */
    public double getDoWUtilisationforDisks(VM vm) {
        double answer = 0;
        DayOfWeek index = getCurrentDayOfWeek();
        for (VmDiskImage disk : vm.getDiskImages()) {
            answer = answer + diskDoWAverage.get(disk.getDiskImage()).get(index).getAverage();
        }
        return answer / vm.getDiskImages().size();
    }    

    /**
     * This given a VM with app tags will find historical information associated
     * with the app tags.
     *
     * @param vm The VM to get the information for
     * @return
     */
    public double getUtilisationforTags(VM vm) {
        double answer = 0;
        for (String tag : vm.getApplicationTags()) {
            answer = answer + tagAverage.get(tag).getAverage();
        }
        return answer / vm.getApplicationTags().size();
    }

    /**
     * This given a VM with app tags will find historical information associated
     * with the app tags.
     *
     * @param vm The VM to get the information for
     * @return The average utilisation of VMs with similar app tags.
     */
    public double getBootUtilisationforTags(VM vm) {
        double answer = 0;
        int bootIndex = 0;
        if (vm instanceof VmDeployed) {
            long timeFromBoot = ((VmDeployed) vm).getTimeFromBoot();
            bootIndex = AbstractVMHistoryWorkloadEstimator.getIndexPosition(BOOT_BUCKET_SIZE, (int) timeFromBoot);

        }
        for (String tag : vm.getApplicationTags()) {
            answer = answer + tagBootAverage.get(tag).get(bootIndex).getAverage();
        }
        return answer / vm.getApplicationTags().size();
    }
    
    /**
     * This given a VM with disk reference will find historical information
     * associated with the disk reference.
     *
     * @param vm The VM to get the information for
     * @return
     */
    public double getDoWUtilisationforTags(VM vm) {
        double answer = 0;
        DayOfWeek index = getCurrentDayOfWeek();
        for (VmDiskImage disk : vm.getDiskImages()) {
            answer = answer + tagDoWAverage.get(disk.getDiskImage()).get(index).getAverage();
        }
        return answer / vm.getDiskImages().size();
    }    

    /**
     * This subroutine is called by addVMToStatistics and adds part of the
     * metrics values obtained to the running averages.
     *
     * @param tag The application tag to add the utilisation value to
     * @param cpuUtil The amount of utilisation of the CPU
     */
    private void addRunningAverageForVMAppTag(String tag, double cpuUtil) {
        RunningAverage average = tagAverage.get(tag);
        if (average == null) {
            tagAverage.put(tag, new RunningAverage(tag, cpuUtil));
        } else {
            average.add(cpuUtil);
            tagAverage.put(tag, average);
        }
    }

    /**
     * This subroutine is called by addVMToStatistics and adds part of the
     * metrics values obtained to the running averages.
     *
     * @param disk The disk reference to add the utilisation value to
     * @param cpuUtil The amount of utilisation of the CPU
     */
    private void addRunningAverageForVMDisk(VmDiskImage disk, double cpuUtil) {
        RunningAverage average = diskAverage.get(disk.getDiskImage());
        if (average == null) {
            diskAverage.put(disk.getDiskImage(), new RunningAverage(disk.getDiskImage(), cpuUtil));
        } else {
            average.add(cpuUtil);
            diskAverage.put(disk.getDiskImage(), average);
        }
    }

    /**
     * This generates boot time averaging in discrete time blocks
     *
     * @param tag The application tag
     * @param cpuUtil The cpu utilisation to record in the average
     * @param secondsFromBoot The time which is to be used to find the correct
     * time window.
     */
    private void addRunningBootAverageForAppTag(String tag, double cpuUtil, long secondsFromBoot) {
        HashMap<Integer, RunningAverage> bootTrace = tagBootAverage.get(tag);
        if (bootTrace == null) {
            HashMap<Integer, RunningAverage> answer = new HashMap<>();
            answer.put(0, new RunningAverage(tag, cpuUtil));
            tagBootAverage.put(tag, answer);
        } else {
            //get the index position item, and add to it.
            Integer index = AbstractVMHistoryWorkloadEstimator.getIndexPosition(BOOT_BUCKET_SIZE, (int) secondsFromBoot);
            RunningAverage average = bootTrace.get(index);
            if (average == null) {
                average = new RunningAverage(tag, cpuUtil);
                bootTrace.put(index, average);
            } else {
                average.add(cpuUtil);
            }
        }
    }

    /**
     * This generates boot time averaging in discrete time blocks
     *
     * @param disk The disk reference to use
     * @param cpuUtil The cpu utilisation to record in the average
     * @param secondsFromBoot The time which is to be used to find the correct
     * time window.
     */
    private void addRunningBootAverageForVMDisk(VmDiskImage disk, double cpuUtil, long secondsFromBoot) {
        HashMap<Integer, RunningAverage> bootTrace = diskBootAverage.get(disk.getDiskImage());
        if (bootTrace == null) {
            HashMap<Integer, RunningAverage> answer = new HashMap<>();
            answer.put(0, new RunningAverage(disk.getDiskImage(), cpuUtil));
            diskBootAverage.put(disk.getDiskImage(), answer);
        } else {
            //get the index position item, and add to it.
            Integer index = AbstractVMHistoryWorkloadEstimator.getIndexPosition(BOOT_BUCKET_SIZE, (int) secondsFromBoot);
            RunningAverage average = bootTrace.get(index);
            if (average == null) {
                average = new RunningAverage(disk.getDiskImage(), cpuUtil);
                bootTrace.put(index, average);
            } else {
                average.add(cpuUtil);
            }
        }
    }

    /**
     * This generates day of week time averaging in discrete time blocks
     *
     * @param tag The application tag
     * @param cpuUtil The cpu utilisation to record in the average
     */
    private void addRunningDoWAverageForAppTag(String tag, double cpuUtil) {
        HashMap<DayOfWeek, RunningAverage> dowTrace = tagDoWAverage.get(tag);
        if (dowTrace == null) {
            HashMap<DayOfWeek, RunningAverage> answer = new HashMap<>();
            answer.put(getCurrentDayOfWeek(), new RunningAverage(tag, cpuUtil));
            tagDoWAverage.put(tag, answer);
        } else {
            //get the index position item, and add to it.
            DayOfWeek index = getCurrentDayOfWeek();
            RunningAverage average = dowTrace.get(index);
            if (average == null) {
                average = new RunningAverage(tag, cpuUtil);
                dowTrace.put(index, average);
            } else {
                average.add(cpuUtil);
            }
        }
    }
    
    /**
     * This generates day of week time averaging in discrete time blocks
     *
     * @param disk The application tag
     * @param cpuUtil The cpu utilisation to record in the average
     */
    private void addRunningDoWAverageForVMDisk(VmDiskImage disk, double cpuUtil) {
        HashMap<DayOfWeek, RunningAverage> dowTrace = diskDoWAverage.get(disk.getDiskImage());
        if (dowTrace == null) {
            HashMap<DayOfWeek, RunningAverage> answer = new HashMap<>();
            answer.put(getCurrentDayOfWeek(), new RunningAverage(disk.getDiskImage(), cpuUtil));
            diskDoWAverage.put(disk.getDiskImage(), answer);
        } else {
            //get the index position item, and add to it.
            DayOfWeek index = getCurrentDayOfWeek();
            RunningAverage average = dowTrace.get(index);
            if (average == null) {
                average = new RunningAverage(disk.getDiskImage(), cpuUtil);
                dowTrace.put(index, average);
            } else {
                average.add(cpuUtil);
            }
        }
    }    

    /**
     * @return the inUse
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * @param inUse the inUse to set
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * This class stores the running average information and enables its
     * processing.
     */
    public class RunningAverage {

        String property;
        double total;
        double count;

        /**
         *
         * @param property
         * @param total
         * @param count
         */
        public RunningAverage(String property, double total, int count) {
            this.property = property;
            this.total = total;
            this.count = (double) count;
        }

        /**
         *
         * @param property
         * @param intialValue
         */
        public RunningAverage(String property, double intialValue) {
            count = 1;
            total = intialValue;
            this.property = property;
        }

        /**
         *
         * @param value
         */
        public void add(double value) {
            count = count + 1;
            total = total + value;
        }

        /**
         *
         * @return
         */
        public double getAverage() {
            return total / count;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass().equals(this.getClass())) {
                return ((RunningAverage) obj).property.equals(this.property);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.property);
            return hash;
        }

    }

    private DayOfWeek getCurrentDayOfWeek() {
        GregorianCalendar cal = new GregorianCalendar();
        return new DayOfWeek(cal.get(GregorianCalendar.DAY_OF_WEEK), cal.get(GregorianCalendar.HOUR_OF_DAY));
    }

    /**
     * This represents the day of the week.
     */
    public class DayOfWeek {

        private final int day;
        private final int hour;

        public DayOfWeek(int day, int hour) {
            this.day = day;
            this.hour = hour;
        }

        /**
         * @return the day
         */
        public int getDay() {
            return day;
        }

        /**
         * @return the hour
         */
        public int getHour() {
            return hour;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DayOfWeek) {
                DayOfWeek other = (DayOfWeek) obj;
                return other.getDay() == day && other.getHour() == hour;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.day;
            hash = 29 * hash + this.hour;
            return hash;
        }
    }

}
