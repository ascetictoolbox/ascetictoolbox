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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    /**
     * The list of hosts the energy modeller knows about
     *
     * @return
     */
    @Override
    public Collection<Host> getHosts() {
        Collection<Host> answer = new HashSet<>();
        connection = getConnection(connection);
        try {
            // PreparedStatements can use variables and are more efficient
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM hosts");
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.executeQuery();
            //TODO Cast result set into a set of host objects
            throw new UnsupportedOperationException("Not supported yet.");
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
        connection = getConnection(connection);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Host getHostCalibrationData(Host host) {
        connection = getConnection(connection);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
