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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This connects to the background database to return historical information and
 * host calibration data.
 *
 * @author Richard
 */
public class DefaultDatabaseConnector implements DatabaseConnector {

    private Connection connection;

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
    private static Connection getConnection() throws IOException, SQLException, ClassNotFoundException {
        // Define JDBC driver
        System.setProperty("jdbc.drivers", Configuration.databaseDriver);
        //Ensure that the driver has been loaded
        Class.forName(Configuration.databaseDriver);
        return DriverManager.getConnection(Configuration.databaseURL,
                Configuration.databaseUser,
                Configuration.databasePassword);
    }

    /**
     * This tests and sets the connection to make sure that it is established.
     *
     * @param connection The connection to test
     * @return The connection passed in or a new one if it is null or otherwise
     * closed. If a connection cannot be created null is returned.
     */
    private static Connection getConnection(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                return getConnection();
            }
            return connection;
        } catch (SQLException | IOException | ClassNotFoundException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This converts a result set into an array list structure that has alll
     * the objects precast and ready for use.
     * @param results The result set to convert
     * @return The ArrayList representing the object.
     * @throws SQLException Thrown if there is errors in the meta data or if the
     * type specified in the meta data is not found.
     */
    private ArrayList<ArrayList> resultSetToArray(ResultSet results) throws SQLException {
        ArrayList<ArrayList> table = new ArrayList<>();
        ResultSetMetaData metaData = results.getMetaData();

        int numberOfColumns = metaData.getColumnCount();

        // Loop through the result set
        while (results.next()) {
            ArrayList row = new ArrayList();
            for (int i = 1; i <= numberOfColumns; i++) {
                if (results.getMetaData().getColumnType(i) == Types.BOOLEAN) {
                    row.add(results.getBoolean(i));
                } else if (results.getMetaData().getColumnType(i) == Types.BIGINT) {
                    row.add(new Long(results.getLong(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.INTEGER) {
                    row.add(new Integer(results.getInt(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.DECIMAL) {
                    row.add(new Double(results.getDouble(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.DOUBLE) {
                    row.add(new Double(results.getDouble(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.TINYINT) {
                    row.add(new Integer(results.getInt(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.VARCHAR) {
                    row.add(results.getString(i));
                } else if (results.getMetaData().getColumnType(i) == Types.NULL) {
                    row.add(null);
                } else if (results.getMetaData().getColumnTypeName(i).compareTo("datetime") == 0) {
                    row.add(results.getDate(i));
                } else {
                    throw new SQLException("Error processing SQl datatye:" + results.getMetaData().getColumnTypeName(i));
                }
            }
            table.add(row);
        }
        return table;
    }

    /**
     * This list all the hosts the energy modeller has data for in its backing 
     * store.
     * @return The list of hosts
     */
    @Override
    public Collection<Host> getHosts() {
        Collection<Host> answer = new HashSet<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT host_id , host_name  FROM host");
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList> results = resultSetToArray(resultSet);
            for (ArrayList hostData : results) {
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
     * This returns the historic data for a VM
     *
     * @param VM The VM to get the historic data for
     * @return The list of historical data for the named VM
     */
    @Override
    public HistoricUsageRecord getVmHistoryData(VmDeployed VM) {
        connection = getConnection(connection);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This gets the calibration data that indicates the performance properties
     * of a given set of host machines.
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
     * @param host The host to get the data for.
     * @return 
     */
    @Override
    public Host getHostCalibrationData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT calibration_id, host_id, cpu, memory, energy FROM host_calibration_data WHERE host_id = ?");
            preparedStatement.setInt(1, host.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList> result = resultSetToArray(resultSet);
            for (ArrayList calibrationData : result) {
                host.addCalibrationData(new HostEnergyCalibrationData(
                        (Double) calibrationData.get(2),
                        (Double) calibrationData.get(3),
                        (Double) calibrationData.get(4)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return host;
    }

    /**
     * This adds set of host machines to the database. If the host already
     * exists the values contained will be overwritten.
     * @param hosts The set of hosts to write to the database.
     */
    @Override
    public void setHosts(Collection<Host> hosts) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO host (host_id, host_name) VALUES (?,?) ON DUPLICATE KEY UPDATE host_name=VALUES(`host_name`);");
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
     * This writes to the database for a named host its calibration data
     * @param host The host to set the calibration data for.
     */
    @Override
    public void setHostCalibrationData(Host host) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO host_calibration_data (host_id, cpu, memory, energy) VALUES (?, ?, ? , ?) "
                    + " ON DUPLICATE KEY UPDATE host_id=VALUES(`host_id`) cpu=VALUES(`cpu`) memory=VALUES(`memory`) energy=VALUES(`energy`);");
            preparedStatement.setInt(1, host.getId());
            for (HostEnergyCalibrationData data : host.getCalibrationData()) {
                preparedStatement.setDouble(1, host.getId());
                preparedStatement.setDouble(2, data.getCpuUsage());
                preparedStatement.setDouble(3, data.getMemoryUsage());
                preparedStatement.setDouble(4, data.getWattsUsed());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This writes historic data for a given host to the database.
     * @param host The host to write the data for
     * @param time The time when the measurement was taken.
     * @param power The power reading for the host.
     * @param energy The current reading for the energy used. Note: this value is 
     * to be treated like a meter reading for an energy firm. The point at which 0
     * energy usage occurred is an arbritrary point in the past. Two historical values
     * can therefore be used to indicate the energy used between the two points in time.
     */
    @Override
    public void writeHostHistoricData(Host host, long time, double power, double energy) {
        connection = getConnection(connection);
        if (connection == null) {
            return;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO host_measurement (host_id, clock, energy, power) VALUES (?, ?, ? , ?);");
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
     * This returns the historic data for a given host, in a specified time period.
     * @param host The host machine to get the data for.
     * @param timePeriod The start and end period for which to query for. If
     * null all records will be used.
     * @return The energy readings taken for a given host.
     */
    @Override
    public List<HostEnergyRecord> getHostHistoryData(Host host, TimePeriod timePeriod) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        List<HostEnergyRecord> answer = new ArrayList<>();
        try {
            PreparedStatement preparedStatement;
            if (timePeriod != null) {
                long start = timePeriod.getStartTime().getTimeInMillis() / 1000;
                long end = timePeriod.getEndTime().getTimeInMillis() / 1000;
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
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList> results = resultSetToArray(resultSet);
            for (ArrayList hostMeasurement : results) {
                answer.add(new HostEnergyRecord(
                        host,
                        (long) hostMeasurement.get(1), //clock is the 1st item
                        (double) hostMeasurement.get(3), //power 3rd item
                        (double) hostMeasurement.get(2))); //energy is 2nd item
            }
        } catch (SQLException ex) {
            Logger.getLogger(DefaultDatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return answer;
    }

    /**
     * This closes the database connection. It will be reopened if a query is called.
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
