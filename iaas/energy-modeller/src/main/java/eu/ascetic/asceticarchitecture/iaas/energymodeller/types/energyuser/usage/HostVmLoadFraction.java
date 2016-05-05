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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a record of what fraction of the load on a host was caused by a given
 * VM. There is the opportunity to add and additional amount, that is power
 * attributed from other sources such as cooling, DFS or others factors in the
 * data center.
 *
 * @author Richard Kavanagh
 */
public class HostVmLoadFraction implements Comparable<HostVmLoadFraction> {

    private final Host host;
    private final long time;
    private HashMap<VmDeployed, Double> fraction = new HashMap<>();
    private double hostPowerOffset = 0;

    /**
     * This creates a new host vm load fraction record.
     *
     * @param host The host the record is for
     * @param time The time that this record is for, in UTC time.
     */
    public HostVmLoadFraction(Host host, long time) {
        this.host = host;
        this.time = time;
    }

    /**
     * The host that this record represents.
     *
     * @return The host this load fraction data is about
     */
    public Host getHost() {
        return host;
    }

    /**
     * This returns the set of VMs that this fraction of host load describes.
     *
     * @return The vms that have been deployed on this vm.
     */
    public Collection<VmDeployed> getVMs() {
        return fraction.keySet();
    }

    /**
     * The time that this record represents, in UTC time.
     *
     * @return the time for this load fraction record.
     */
    public long getTime() {
        return time;
    }

    /**
     * This adds fraction information for a specified VM
     *
     * @param vm The vm to specify the usage fraction for
     * @param fraction The fraction to allocate to a given vm.
     */
    public void addFraction(VmDeployed vm, double fraction) {
        this.fraction.put(vm, fraction);
    }

    /**
     * This takes a collection of vm usage (load data) and determines the
     * fraction of the overall load which a VM is responsible for.
     *
     * @param load The load that was induced upon the host, by the set of Vms
     */
    public void setFraction(Collection<VmUsageRecord> load) {
        fraction = getFraction(load);
    }

    /**
     * This takes a collection of vm load measurement data (load data) and
     * determines the fraction of the overall load which a VM is responsible
     * for.
     *
     * @param load The load that was induced upon the host, by the set of Vms
     */
    public void setFraction(List<VmMeasurement> load) {
        fraction = getFraction(load);
    }

    /**
     * This takes a set of vm usage records and calculates the fraction of
     * system load each vm is responsible for.
     *
     * @param load The load that was induced on the system.
     * @return The fraction of the load associated with each deployed vm.
     */
    public static HashMap<VmDeployed, Double> getFraction(Collection<VmUsageRecord> load) {
        HashMap<VmDeployed, Double> answer = new HashMap<>();
        double totalLoad = 0.0;
        for (VmUsageRecord vmUsageRecord : load) {
            totalLoad = totalLoad + vmUsageRecord.getLoad();
        }
        for (VmUsageRecord vmUsageRecord : load) {
            answer.put(vmUsageRecord.getVm(), (vmUsageRecord.getLoad() / totalLoad));
        }
        return answer;
    }

    /**
     * This takes a set of vm usage records and calculates the fraction of
     * system load each vm is responsible for.
     *
     * @param load The load that was induced on the system.
     * @return The fraction of the load associated with each deployed vm.
     */
    public static HashMap<VmDeployed, Double> getFraction(List<VmMeasurement> load) {
        return getFraction(load, false);
    }

    /**
     * This takes a set of vm usage records and calculates the fraction of
     * system load each vm is responsible for.
     *
     * @param load The load that was induced on the system.
     * @param considerCoreCount If the fraction should consider the count of
     * cores or not.
     * @return The fraction of the load associated with each deployed vm.
     */
    public static HashMap<VmDeployed, Double> getFraction(List<VmMeasurement> load, boolean considerCoreCount) {
        HashMap<VmDeployed, Double> answer = new HashMap<>();
        double totalLoad = 0.0;
        try {
            for (VmMeasurement loadMeasure : load) {
                totalLoad = totalLoad + loadMeasure.getCpuUtilisation();
                Logger.getLogger(HostVmLoadFraction.class.getName()).log(Level.FINE, "VM: {0} CPU: {1}", new Object[]{loadMeasure.getVm().getName(), loadMeasure.getCpuUtilisation()});
            }
        } catch (NullPointerException ex) {
            Logger.getLogger(HostVmLoadFraction.class.getName()).log(Level.WARNING, "Using fallback due to no CPU load information.");
            /**
             * This occurs if Zabbix provides no CPU utilisation information for
             * a VM.
             */
            for (VmMeasurement loadMeasure : load) {
                answer.put(loadMeasure.getVm(), 1.0);
            }
            return answer;
        }
        /**
         * This is an error handling state, if the data indicates no load was
         * induced whatsoever. Avoids divide by zero errors.
         */
        if (totalLoad == 0) {
            Logger.getLogger(HostVmLoadFraction.class.getName()).log(Level.WARNING, "Using fallback due to no CPU total load been equal to zero.");
            double count = load.size();
            for (VmMeasurement loadMeasure : load) {
                answer.put(loadMeasure.getVm(), (1 / count));
            }
            return answer;
        }
        for (VmMeasurement loadMeasure : load) {
            if (considerCoreCount) {
                double vmCoreCount = (loadMeasure.getVm().getCpus() >= 1 ? loadMeasure.getVm().getCpus() : 1);
                answer.put(loadMeasure.getVm(),vmCoreCount * (loadMeasure.getCpuUtilisation() / totalLoad));
            } else {
                answer.put(loadMeasure.getVm(), (loadMeasure.getCpuUtilisation() / totalLoad));
            }
        }
        return answer;
    }

    /**
     * This utility function goes through a list of HostVmLoadFraction and lists
     * the VMs that were involved.
     *
     * @param fractionData The collection of load fraction data to parse.
     * @return The list of VMs listed in the load fraction data.
     */
    public static HashSet<VmDeployed> getVMs(Collection<HostVmLoadFraction> fractionData) {
        HashSet<VmDeployed> answer = new HashSet<>();
        for (HostVmLoadFraction hostVmLoadFraction : fractionData) {
            answer.addAll(hostVmLoadFraction.getVMs());
        }
        return answer;
    }

    /**
     * This provides all the data that shows the fraction of load each VM is
     * responsible for.
     *
     * @return The fraction data for this record.
     */
    public HashMap<VmDeployed, Double> getFraction() {
        return fraction;
    }

    /**
     * This provides the data that shows the fraction of load a specific VM is
     * responsible for.
     *
     * @param vm The vm to get the load information for
     * @return The fraction between 0..1 of how much load was induced and 0 if
     * the VM was not found.
     */
    public double getFraction(VmDeployed vm) {
        Double answer = fraction.get(vm);
        if (answer == null) {
            return 0;
        } else {
            return answer.doubleValue();
        }
    }

    /**
     * This comparison orders load fraction records by time.
     *
     * @param loadFraction the load fraction record to compare to.
     * @return -1 if the before, 0 if at the same time 1 if in the future.
     */
    @Override
    public int compareTo(HostVmLoadFraction loadFraction) {
        return Long.valueOf(this.time).compareTo(loadFraction.getTime());
    }

    /**
     * This sets and additional offset based upon cooling and other sources,
     * power consumption that is attributed to the data center as a whole. i.e.
     * Distributed file system nodes, cooling etc.
     *
     * @return The offset which to apply to this host power measurement
     */
    public double getHostPowerOffset() {
        return hostPowerOffset;
    }

    /**
     * This sets and additional offset based upon cooling and other sources,
     * power consumption that is attributed to the data center as a whole. i.e.
     * Distributed file system nodes, cooling etc.
     *
     * @param offset The offset
     */
    public void setHostPowerOffset(double offset) {
        hostPowerOffset = offset;
    }

    /**
     * This returns the host idle power consumption.
     *
     * @return The host idle power consumption, based upon evenly fractioning
     * out the hosts power consumption based upon the amount of VMs running upon
     * it.
     */
    public double getVmIdlePower() {
        return host.getIdlePowerConsumption() / ((double) fraction.size());
    }

}
