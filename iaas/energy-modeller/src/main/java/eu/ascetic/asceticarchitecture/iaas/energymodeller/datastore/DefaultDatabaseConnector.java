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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostProfileData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This connects to the background database to return historical information and
 * host calibration data.
 *
 * @author Richard Kavanagh
 */
public class DefaultDatabaseConnector extends MySqlDatabaseConnector implements DatabaseConnector {

    private Connection connection;

    /**
     * This creates a new database connector for use. It establishes a database
     * connection immediately ready for use.
     */
    public DefaultDatabaseConnector() {
        try {
            connection = getConnection();
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Establishes a connection to the database.
     *
     * @return Connection object representing the connection
     * @throws IOException if properties file cannot be accessed
     * @throws SQLException if connection fails
     * @throws ClassNotFoundException if the database driver class is not found
     */
    @Override
    protected final Connection getConnection() throws IOException, SQLException, ClassNotFoundException {
        // Define JDBC driver
        System.setProperty("jdbc.drivers", Configuration.databaseDriver);
        //Ensure that the driver has been loaded
        Class.forName(Configuration.databaseDriver);
        return DriverManager.getConnection(Configuration.databaseURL,
                Configuration.databaseUser,
                Configuration.databasePassword);
    }

    /**
     * This list all the hosts the energy modeller has data for in its backing
     * store.
     *
     * @return The list of hosts
     */
    @Override
    public Collection<Host> getHosts() {
        Collection<Host> answer = new HashSet<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT host_id , host_name  FROM host");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> hostData : results) {
                Host host = new Host((Integer) hostData.get(0), (String) hostData.get(1));
                host = getHostCalibrationData(host);
                answer.add(host);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    /**
     * This list all the vms the energy modeller has data for in its backing
     * store.
     *
     * @return The list of hosts
     */
    @Override
    public Collection<VmDeployed> getVms() {
        Collection<VmDeployed> answer = new HashSet<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT vm_id , vm_name, deployment_id FROM vm");
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> vmData : results) {
                VmDeployed vm = new VmDeployed((Integer) vmData.get(0), (String) vmData.get(1));
                vm.setDeploymentID((String) vmData.get(2));
                answer.add(vm);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    /**
     * This gets the calibration data that indicates the performance properties
     * of a given set of host machines.
     *
     * @param hosts The set of hosts to get the data for.
     * @return The calibration data for the named hosts.
     */
    @Override
    public Collection<Host> getHostCalibrationData(Collection<Host> hosts) {
        for (Host host : hosts) {
            host = getHostCalibrationData(host);
        }
        return hosts;
    }

    /**
     * This gets the calibration data that indicates the performance properties
     * of a given host machine.
     *
     * @param host The host to get the data for.
     * @return The host with its calibration data defined.
     */
    @Override
    public Host getHostCalibrationData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT calibration_id, host_id, cpu, memory, energy FROM host_calibration_data WHERE host_id = ?")) {
            preparedStatement.setInt(1, host.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> result = resultSetToArray(resultSet);
                for (ArrayList<Object> calibrationData : result) {
                    host.addCalibrationData(new HostEnergyCalibrationData(
                            (Double) calibrationData.get(2),
                            (Double) calibrationData.get(3),
                            (Double) calibrationData.get(4)));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return host;
    }

    @Override
    public Host getHostProfileData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT profile_id, host_id, type, value FROM host_profile_data WHERE host_id = ?")) {
            preparedStatement.setInt(1, host.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> result = resultSetToArray(resultSet);
                for (ArrayList<Object> profileData : result) {
                    host.addProfileData(new HostProfileData(
                            (String) profileData.get(2),
                            (Double) profileData.get(3)));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return host;
    }

    /**
     * This adds set of host machines to the database. If the host already
     * exists the values contained will be overwritten.
     *
     * @param hosts The set of hosts to write to the database.
     */
    @Override
    public void setHosts(Collection<Host> hosts) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO host (host_id, host_name) VALUES (?,?) ON DUPLICATE KEY UPDATE host_name=VALUES(`host_name`);")) {
            for (Host host : hosts) {
                preparedStatement.setInt(1, host.getId());
                preparedStatement.setString(2, host.getHostName());
                preparedStatement.executeUpdate();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This adds set of vms to the database. If the vm already exists the values
     * contained will be overwritten.
     *
     * @param vms The set of vms to write to the database.
     */
    @Override
    public void setVms(Collection<VmDeployed> vms) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm (vm_id, vm_name, deployment_id) VALUES (?,?,?) ON DUPLICATE KEY UPDATE vm_name=VALUES(`vm_name`), deployment_id=COALESCE(VALUES(`deployment_id`), deployment_id);")) {
            for (VmDeployed vm : vms) {
                preparedStatement.setInt(1, vm.getId());
                preparedStatement.setString(2, vm.getName());
                preparedStatement.setString(3, vm.getDeploymentID());
                preparedStatement.executeUpdate();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public VmDeployed getVMProfileData(VmDeployed vm) {
        //get the app tag data
        vm = getVmAppTags(vm);
        //get the disk data
        vm = getVmDisks(vm);
        return vm;
    }

    @Override
    public Collection<VmDeployed> getVMProfileData(Collection<VmDeployed> vms) {
        for (VmDeployed vm : vms) {
            getVMProfileData(vm);
        }
        return vms;
    }

    /**
     * For the named vm this populates the app tags list of the VM.
     *
     * @param vm The VM to get the tags into the database for
     * @return The VM with its application tags set
     */
    private VmDeployed getVmAppTags(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT vm.vm_id, vm_app_tag.tag_name "
                + "FROM vm, vm_app_tag, vm_app_tag_arr "
                + "WHERE vm.vm_id = ? AND "
                + "vm.vm_id = vm_app_tag_arr.vm_id AND "
                + "vm_app_tag_arr.vm_app_tag_id = vm_app_tag.vm_app_tag_id")) {
            preparedStatement.setInt(1, vm.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> vmData : results) {
                vm.addApplicationTag((String) vmData.get(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vm;
    }

    /**
     * For the named vm this populates the disk list of the VM.
     *
     * @param vm The VM to get the disk list from the database
     * @return The VM with its disk values set
     */
    private VmDeployed getVmDisks(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT vm.vm_id, vm_disk.disk_name "
                + "FROM vm, vm_disk, vm_disk_arr "
                + "WHERE vm.vm_id = ? AND "
                + "vm.vm_id = vm_disk_arr.vm_id AND "
                + "vm_disk_arr.vm_disk_id = vm_disk.vm_disk_id")) {
            preparedStatement.setInt(1, vm.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> vmData : results) {
                vm.addApplicationTag((String) vmData.get(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vm;
    }

    @Override
    public void setVMProfileData(VmDeployed vm) {
        //set the list of app tags
        setVMAppTags(vm);
        //assign the tags to the given vm
        setVMAppTagArray(vm);
        //set the list of disks
        setDiskInformation(vm);
        //set the references to each disk for the VM
        setDiskInformationArray(vm);
    }

    /**
     * This sets the association between a VM and its app tags.
     *
     * @param vm The VM to save the tags into the database for
     */
    private void setVMAppTagArray(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm_app_tag_arr (?, vm_app_tag_arr.vm_app_tag_id) "
                + "SELECT 1 as vm_id, vm_app_tag.vm_app_tag_id "
                + "FROM vm_app_tag WHERE vm_app_tag.tag_name = ?")) {
            for (String appTag : vm.getApplicationTags()) {
                preparedStatement.setString(1, appTag);
                preparedStatement.setInt(2, vm.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This sets the association between a VM and its disks.
     *
     * @param vm The VM to save the disk information into the database for
     */
    private void setDiskInformationArray(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm_disk_arr (?, vm_disk_arr.vm_disk_id) "
                + "SELECT 1 as vm_id, vm_disk.vm_disk_id "
                + "FROM vm_disk WHERE vm_disk.disk_name = ?")) {
            for (VmDiskImage diskImage : vm.getDiskImages()) {
                preparedStatement.setString(1, diskImage.toString());
                preparedStatement.setInt(2, vm.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * For a given VM this records all the identified tags into the database.
     *
     * @param vm The VM to save the tags into the database for
     */
    private void setVMAppTags(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm_app_tag (tag_name) VALUES (?) ON DUPLICATE KEY UPDATE tag_name=VALUES(tag_name)")) {
            for (String appTag : vm.getApplicationTags()) {
                preparedStatement.setString(1, appTag);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * For a given VM this records all the references to disks into the
     * database.
     *
     * @param vm The VM to save the disk information into the database for
     */
    private void setDiskInformation(VmDeployed vm) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm_disk (disk_name) VALUES (?) ON DUPLICATE KEY UPDATE disk_name=VALUES(disk_name)")) {
            for (VmDiskImage diskImage : vm.getDiskImages()) {
                preparedStatement.setString(1, diskImage.toString());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This writes to the database for a named host its calibration data
     *
     * @param host The host to set the calibration data for.
     */
    @Override
    public void setHostCalibrationData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO host_calibration_data (host_id, cpu, memory, energy) VALUES (?, ?, ? , ?) "
                + " ON DUPLICATE KEY UPDATE host_id=VALUES(`host_id`), cpu=VALUES(`cpu`), memory=VALUES(`memory`), energy=VALUES(`energy`);")) {
            preparedStatement.setInt(1, host.getId());
            for (HostEnergyCalibrationData data : host.getCalibrationData()) {
                preparedStatement.setInt(1, host.getId());
                preparedStatement.setDouble(2, data.getCpuUsage());
                preparedStatement.setDouble(3, data.getMemoryUsage());
                preparedStatement.setDouble(4, data.getWattsUsed());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setHostProfileData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO host_profile_data (host_id, type, value) VALUES (?, ?, ?);")) {
            preparedStatement.setInt(1, host.getId());
            for (HostProfileData data : host.getProfileData()) {
                preparedStatement.setString(2, data.getType());
                preparedStatement.setDouble(3, data.getValue());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
    @Override
    public void writeHostHistoricData(Host host, long time, double power, double energy) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO host_measurement (host_id, clock, energy, power) VALUES (?, ?, ? , ?);")) {
            preparedStatement.setInt(1, host.getId());
            preparedStatement.setLong(2, time);
            preparedStatement.setDouble(3, energy);
            preparedStatement.setDouble(4, power);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This returns the historic data for a given host, in a specified time
     * period.
     *
     * @param host The host machine to get the data for.
     * @param timePeriod The start and end period for which to query for. If
     * null all records will be returned.
     * @return The energy readings taken for a given host.
     */
    @Override
    public List<HostEnergyRecord> getHostHistoryData(Host host, TimePeriod timePeriod) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        List<HostEnergyRecord> answer = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            if (timePeriod != null) {
                long start = timePeriod.getStartTimeInSeconds();
                long end = timePeriod.getEndTimeInSeconds();
                preparedStatement = connection.prepareStatement(
                        "SELECT host_id, clock, energy, power FROM host_measurement WHERE host_id = ? "
                        + " AND clock >= ? AND clock <= ?;");
                preparedStatement.setLong(2, start);
                preparedStatement.setLong(3, end);
            } else {
                preparedStatement = connection.prepareStatement(
                        "SELECT host_id, clock, energy, power FROM host_measurement WHERE host_id = ?;");
            }
            preparedStatement.setInt(1, host.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                for (ArrayList<Object> hostMeasurement : results) {
                    answer.add(new HostEnergyRecord(
                            host,
                            (long) hostMeasurement.get(1), //clock is the 1st item
                            (double) hostMeasurement.get(3), //power 3rd item
                            (double) hostMeasurement.get(2))); //energy is 2nd item
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return answer;
    }

    @Override
    public void writeHostVMHistoricData(Host host, long time, HostVmLoadFraction load) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO vm_measurement (host_id, vm_id, clock, cpu_load) VALUES (?, ?, ? , ?);")) {
            preparedStatement.setInt(1, host.getId());
            for (VmDeployed vm : load.getVMs()) {
                preparedStatement.setInt(2, vm.getId());
                preparedStatement.setLong(3, time);
                preparedStatement.setDouble(4, load.getFraction(vm));
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This is part of a caching mechanism for Vms when getting historic load
     * data, the aim is to create less vm objects (and thus reduce the footprint
     * of this method).
     *
     * @param id The VM id.
     * @param name The name of the VM
     * @param host The host it is running from.
     * @return The reference to the VM.
     */
    private VmDeployed getVM(int id, String name, Host host, HashMap<String, VmDeployed> vmCache) {
        VmDeployed vm = vmCache.get(name);
        if (vm == null || !vm.getAllocatedTo().equals(host)) {
            vm = new VmDeployed(id, name);
            vm.setAllocatedTo(host);
            vmCache.put(vm.getName(), vm);
        }
        return vm;
    }

    @Override
    public Collection<HostVmLoadFraction> getHostVmHistoryLoadData(Host host, TimePeriod timePeriod) {
        HashMap<String, VmDeployed> vmCache = new HashMap<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        List<HostVmLoadFraction> answer = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            if (timePeriod != null) {
                long start = timePeriod.getStartTimeInSeconds();
                long end = timePeriod.getEndTimeInSeconds();
                preparedStatement = connection.prepareStatement(
                        "SELECT host_id, vm_measurement.vm_id, vm_name, clock, cpu_load FROM vm_measurement, vm "
                        + "WHERE vm_measurement.vm_id = vm.vm_id "
                        + "and vm_measurement.host_id = ? "
                        + " AND clock >= ? AND clock <= ?;");
                preparedStatement.setLong(2, start);
                preparedStatement.setLong(3, end);
            } else {
                preparedStatement = connection.prepareStatement(
                        "SELECT host_id, vm_measurement.vm_id, vm_name, clock, cpu_load FROM vm_measurement, vm "
                        + "WHERE vm_measurement.vm_id = vm.vm_id "
                        + "and vm_measurement.host_id = ?;");
            }
            preparedStatement.setInt(1, host.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                long lastClock = Long.MIN_VALUE;
                long currentClock;
                HostVmLoadFraction currentHostLoadFraction = null;
                for (ArrayList<Object> measurement : results) {
                    currentClock = (long) measurement.get(3); //clock is the 3rd item)
                    if (currentClock != lastClock || currentHostLoadFraction == null) {
                        currentHostLoadFraction = new HostVmLoadFraction(host, currentClock);
                        VmDeployed vm = getVM((int) measurement.get(1), (String) measurement.get(2), host, vmCache);
                        currentHostLoadFraction.addFraction(vm, (double) measurement.get(4)); //load is the fourth item
                        answer.add(currentHostLoadFraction);
                    } else {
                        VmDeployed vm = getVM((int) measurement.get(1), (String) measurement.get(2), host, vmCache);
                        currentHostLoadFraction.addFraction(vm, (double) measurement.get(4)); //load is the fourth item
                    }
                    lastClock = currentClock;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return answer;
    }

    /**
     * This closes the database connection. It will be reopened if a query is
     * called.
     */
    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, "The connection close operation failed.", ex);
        }
    }
}
