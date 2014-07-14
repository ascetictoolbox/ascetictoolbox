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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
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
                    row.add(new Integer(results.getInt(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.INTEGER) {
                    row.add(new Integer(results.getInt(i)));
                } else if (results.getMetaData().getColumnType(i) == Types.DECIMAL) {
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
     * The list of hosts the energy modeller knows about
     *
     * @return
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
            ArrayList<ArrayList> result = resultSetToArray(resultSet);
            for (ArrayList hostData : result) {
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
     * @return
     */
    @Override
    public HistoricUsageRecord getVMHistoryData(VmDeployed VM) {
        connection = getConnection(connection);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Host> getHostCalibrationData(Collection<Host> hosts) {
        for (Host host : hosts) {
            host = getHostCalibrationData(host);
        }
        return hosts;
    }

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

}
