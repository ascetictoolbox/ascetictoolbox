package es.bsc.vmmanagercore.db;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;

import java.util.ArrayList;

/**
 * Interface for the connection of the VM Manager with the DB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public interface VmManagerDb {

    /**
     * Closes the connection with the DB.
     */
    public void closeConnection();

    /**
     * Deletes all the record from the DB.
     */
    public void cleanDb();

    /**
     * Inserts a VM on the DB.
     *
     * @param vmId the ID of the VM to be inserted
     * @param appId the ID of the application to which the VM belongs to.
     */
    public void insertVm(String vmId, String appId);

    /**
     * Deletes a VM from the DB.
     *
     * @param vmId the ID of the VM to be deleted
     */
    public void deleteVm(String vmId);

    /**
     * Returns the ID of the application to which a specific VM belongs to.
     *
     * @param vmId the ID of the VM
     * @return the ID of the application
     */
    public String getAppIdOfVm(String vmId);

    /**
     * Deletes all the VMs from the DB.
     */
    public void deleteAllVms();

    /**
     * Returns the IDs of the VMs present in the DB.
     *
     * @return IDs of the VMs present in the DB
     */
    public ArrayList<String> getAllVmIds();

    /**
     * Returns the IDs of the VMs that belong to an application.
     *
     * @param appId the ID of the application
     * @return the IDs of the VMs that belong to the application
     */
    public ArrayList<String> getVmsOfApp(String appId);

    /**
     * Returns the scheduling algorithm being used.
     *
     * @return the scheduling algorithm
     */
    public SchedulingAlgorithm getCurrentSchedulingAlg();

    /**
     * Returns all the available scheduling algorithms.
     *
     * @return the scheduling algorithms
     */
    public ArrayList<SchedulingAlgorithm> getAvailableSchedulingAlg();

    /**
     * Changes the current scheduling algorithm.
     *
     * @param alg the scheduling algorithm to be used
     */
    public void setCurrentSchedulingAlg(SchedulingAlgorithm alg);
}