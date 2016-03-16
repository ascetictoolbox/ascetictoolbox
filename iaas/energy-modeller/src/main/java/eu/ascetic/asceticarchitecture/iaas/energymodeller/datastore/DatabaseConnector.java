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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryBootRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.VmLoadHistoryWeekRecord;
import java.util.Collection;
import java.util.List;

/**
 * This interface for connecting to the background database with the aim of
 * returning historical information and host calibration data.
 *
 * @author Richard Kavanagh
 */
public interface DatabaseConnector {

    /**
     * This list all the hosts the energy modeller has data for in its backing
     * store.
     *
     * @return The list of hosts
     */
    public Collection<Host> getHosts();

    /**
     * This list all the vms the energy modeller has data for in its backing
     * store.
     *
     * @return The list of hosts
     */
    public Collection<VmDeployed> getVms();

    /**
     * This adds set of vms to the database. If the vm already exists the values
     * contained will be overwritten.
     *
     * @param vms The set of vms to write to the database.
     */
    public void setVms(Collection<VmDeployed> vms);

    /**
     * This gets from the database for a VM its profile data that was originally
     * obtained from the VMs description data. This can be information such as
     * which are the main applications running on the VM.
     *
     * @param vm The VM to get the profile data for.
     * @return The VM with its profile data defined.
     */
    public VmDeployed getVMProfileData(VmDeployed vm);

    /**
     * This gets from the database for a set of VM their profile data that was
     * originally obtained from the VMs description data. This can be
     * information such as which are the main applications running on the VM.
     *
     * @param vms The VMs to get the profile data for.
     * @return The VM with its profile data defined.
     */
    public Collection<VmDeployed> getVMProfileData(Collection<VmDeployed> vms);

    /**
     * This writes to the database for a VM its profile data that was originally
     * obtained from the VMs description data. This can be information such as
     * which are the main applications running on the VM.
     *
     * @param vm The vm to set the profile data for.
     */
    public void setVMProfileData(VmDeployed vm);

    /**
     * This adds set of host machines to the database. If the host already
     * exists the values contained will be overwritten.
     *
     * @param hosts The set of hosts to write to the database.
     */
    public void setHosts(Collection<Host> hosts);

    /**
     * This gets the calibration data that indicates the performance properties
     * of a given set of host machines.
     *
     * @param hosts The set of hosts to get the data for.
     * @return The calibration data for the named hosts.
     */
    public Collection<Host> getHostCalibrationData(Collection<Host> hosts);

    /**
     * This gets the calibration data that indicates the performance properties
     * of a given host machine.
     *
     * @param host The host to get the data for.
     * @return The host with its calibration data defined.
     */
    public Host getHostCalibrationData(Host host);

    /**
     * This writes to the database for a named host its calibration data
     *
     * @param host The host to set the calibration data for.
     */
    public void setHostCalibrationData(Host host);

    /**
     * This gets the profile data that indicates the performance properties of a
     * given host machine.
     *
     * @param host The host to get the data for.
     * @return The host with its profile data defined.
     */
    public Host getHostProfileData(Host host);

    /**
     * This writes to the database for a named host its profile data obtained by
     * benchmarking.
     *
     * @param host The host to set the profile data for.
     */
    public void setHostProfileData(Host host);

    /**
     * This writes historic data for a given host to the database.
     *
     * @param host The host to write the data for
     * @param time The time when the measurement was taken.
     * @param power The power reading for the host.
     * @param energy The current reading for the energy used. Note: this value
     * is to be treated like a meter reading for an energy firm. The point at
     * which 0 energy usage occurred is an arbritrary point in the past. Two
     * historical values can therefore be used to indicate the energy used
     * between the two points in time.
     */
    public void writeHostHistoricData(Host host, long time, double power, double energy);

    /**
     * This returns the historic data for a given host, in a specified time
     * period.
     *
     * @param host The host machine to get the data for.
     * @param timePeriod The start and end period for which to query for. If
     * null all records will be returned.
     * @return The energy readings taken for a given host.
     */
    public List<HostEnergyRecord> getHostHistoryData(Host host, TimePeriod timePeriod);

    /**
     * This writes VM utilisation data for a given physical host to the
     * database.
     *
     * @param host The host to set the vm load information for
     * @param time The time when the measurements were taken.
     * @param load The summary of the VM load data on the host.
     */
    public void writeHostVMHistoricData(Host host, long time, HostVmLoadFraction load);

    /**
     * This gets VM utilisation data for a given physical host from the
     * database.
     *
     * @param host The host to get the vm load information for
     * @param timePeriod The start and end period for which to query for. If
     * null all records will be returned.
     * @return The load readings taken for a given host.
     */
    public Collection<HostVmLoadFraction> getHostVmHistoryLoadData(Host host, TimePeriod timePeriod);

    /**
     * This returns the overall average CPU utilisation for a given tag.
     *
     * @param tagName The application tag to get the cpu usage for.
     * @return The average CPU usage generated by VMs with a given application
     * tag.
     */
    public VmLoadHistoryRecord getAverageCPUUtilisationTag(String tagName);

    /**
     * This returns the overall average CPU utilisation for a given disk
     * reference.
     *
     * @param diskRefStr The disk reference to get the cpu usage for.
     * @return The average CPU usage generated by VMs with a given vm disk
     * reference
     */
    public VmLoadHistoryRecord getAverageCPUUtilisationDisk(String diskRefStr);

    /**
     * This returns the overall average CPU utilisation for a given tag.
     *
     * @param tagName The application tag to get the cpu usage for.
     * @return The cpu utilisation trace data.
     */
    public List<VmLoadHistoryWeekRecord> getAverageCPUUtilisationWeekTraceForTag(String tagName);

    /**
     * This returns the overall average CPU utilisation for a given tag.
     *
     * @param diskRef The disk reference to get the cpu usage for.
     * @return The cpu utilisation trace data.
     */
    public List<VmLoadHistoryWeekRecord> getAverageCPUUtilisationWeekTraceForDisk(String diskRef);

    /**
     * This returns for a given VM the current index value for discrete time of
     * a a set size.
     *
     * @param vm The VM to get the current index for
     * @param windowSize The size of the time window to be used, in seconds.
     * @return The current index value.
     */
    public double getVMCurrentBootTraceIndex(VmDeployed vm, int windowSize);

    /**
     * This returns the boot time trace for the average CPU utilisation for a
     * given application tag for VMs since boot. It is reported in a set of
     * discrete time intervals as specified by a window size parameter.
     *
     * @param tagName The application tag to get the cpu usage for.
     * @param windowSize The time in seconds to group each discrete time block
     * by.
     * @return The cpu utilisation trace data.
     */
    public List<VmLoadHistoryBootRecord> getAverageCPUUtilisationBootTraceForTag(String tagName, int windowSize);

    /**
     * This returns the boot time trace for the average CPU utilisation for a
     * disk reference for VMs since boot. It is reported in a set of discrete
     * time intervals as specified by a window size parameter.
     *
     * @param diskName The application tag to get the cpu usage for.
     * @param windowSize The size of the time window to be used, in seconds.
     * @return The cpu utilisation trace data.
     */
    public List<VmLoadHistoryBootRecord> getAverageCPUUtilisationBootTraceForDisk(String diskName, int windowSize);

    /**
     * This closes the database connection. It will be reopened if a query is
     * called.
     */
    public void closeConnection();

}
