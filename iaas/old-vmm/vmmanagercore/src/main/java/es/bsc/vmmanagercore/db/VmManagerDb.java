/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.db;

import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;
import es.bsc.vmmanagercore.selfadaptation.options.SelfAdaptationOptions;

import java.util.List;

/**
 * Interface for the connection of the VM Manager with the DB.
 * The DB is used to store the state of the VMM.
 * For the moment, the state includes information of the VMs that is not stored in the Cloud Middleware,
 * such as: the ID of the application to which the VM belongs, the SLA ID, and the OVF ID.
 * The state also includes the scheduling policy and the self-adaptation options selected.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public interface VmManagerDb {

    /**
     * Closes the connection with the DB.
     */
    void closeConnection();

    /**
     * Deletes all the record from the DB.
     */
    void cleanDb();

    /**
     * Inserts a VM on the DB.
     *
     * @param vmId the ID of the VM to be inserted
     * @param appId the ID of the application to which the VM belongs to.
     * @param ovfId the OVF ID of the VM
     * @param slaId the SLA ID of the VM
     */
    void insertVm(String vmId, String appId, String ovfId, String slaId);

    /**
     * Deletes a VM from the DB.
     *
     * @param vmId the ID of the VM to be deleted
     */
    void deleteVm(String vmId);

    /**
     * Returns the ID of the application to which a specific VM belongs to.
     *
     * @param vmId the ID of the VM
     * @return the ID of the application
     */
    String getAppIdOfVm(String vmId);

    /**
     * Returns the OVF ID of a VM
     *
     * @param vmId the ID of the VM
     * @return the OVF ID
     */
    String getOvfIdOfVm(String vmId);

    /**
     * Returns the SLA ID of a VM
     *
     * @param vmId the ID of the VM
     * @return the SLA ID
     */
    String getSlaIdOfVm(String vmId);

    /**
     * Deletes all the VMs from the DB.
     */
    void deleteAllVms();

    /**
     * Returns the IDs of the VMs present in the DB.
     *
     * @return IDs of the VMs present in the DB
     */
    List<String> getAllVmIds();

    /**
     * Returns the IDs of the VMs that belong to an application.
     *
     * @param appId the ID of the application
     * @return the IDs of the VMs that belong to the application
     */
    List<String> getVmsOfApp(String appId);

    /**
     * Returns the scheduling algorithm being used.
     *
     * @return the scheduling algorithm
     */
    SchedAlgorithmNameEnum getCurrentSchedulingAlg();

    /**
     * Changes the current scheduling algorithm.
     *
     * @param alg the scheduling algorithm to be used
     */
    void setCurrentSchedulingAlg(SchedAlgorithmNameEnum alg);

    /**
     * Changes the options for the self-adaptation capabilities.
     *
     * @param options the options
     */
    void saveSelfAdaptationOptions(SelfAdaptationOptions options);

    /**
     * Returns the options for the self-adaptation capabilities.
     *
     * @return the options
     */
    SelfAdaptationOptions getSelfAdaptationOptions();

}