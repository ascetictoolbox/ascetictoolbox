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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This holds basic functions that are useful for any MySQL database access.
 *
 * @author Richard
 */
public abstract class MySqlDatabaseConnector {

    protected abstract Connection getConnection() throws IOException, SQLException, ClassNotFoundException;

    /**
     * This tests and sets the connection to make sure that it is established.
     *
     * @param connection The connection to test
     * @return The connection passed in or a new one if it is null or otherwise
     * closed. If a connection cannot be created null is returned.
     */
    protected Connection getConnection(Connection connection) {
        try {
            if (connection == null || connection.isValid(30)) {
                return getConnection();
            }
            return connection;
        } catch (SQLException | IOException | ClassNotFoundException ex) {
            Logger.getLogger(MySqlDatabaseConnector.class.getName()).log(Level.SEVERE, "Failed to establish the connection to the database", ex);
            try {
                if (connection == null || connection.isValid(30)) {
                    return getConnection();
                }
            } catch (SQLException | IOException | ClassNotFoundException ex1) {
                Logger.getLogger(MySqlDatabaseConnector.class.getName()).log(Level.SEVERE, "Failed to connect to the database on the second attempt", ex1);
            }
        }
        return null;
    }    
    
    /**
     * This converts a result set into an array list structure that has all the
     * objects precast and ready for use.
     *
     * @param results The result set to convert
     * @return The ArrayList representing the object.
     * @throws SQLException Thrown if there is errors in the meta data or if the
     * type specified in the meta data is not found.
     */
    protected ArrayList<ArrayList<Object>> resultSetToArray(ResultSet results) throws SQLException {
        ArrayList<ArrayList<Object>> table = new ArrayList<>();
        ResultSetMetaData metaData = results.getMetaData();

        int numberOfColumns = metaData.getColumnCount();

        // Loop through the result set
        while (results.next()) {
            ArrayList<Object> row = new ArrayList<>();
            for (int i = 1; i <= numberOfColumns; i++) {
                if (results.getMetaData().getColumnType(i) == Types.BOOLEAN) {
                    row.add(results.getBoolean(i));
                } else if (results.getMetaData().getColumnType(i) == Types.BIGINT) {
                    row.add(new Long(results.getLong(i)));
                } else if (isIntegerType(results.getMetaData(), i)) {
                    row.add(new Integer(results.getInt(i)));
                } else if (isDoubleType(results.getMetaData(), i)) {
                    row.add(new Double(results.getDouble(i)));
                } else if (isStringType(results.getMetaData(), i)) {
                    row.add(results.getString(i));
                } else if (results.getMetaData().getColumnType(i) == Types.NULL) {
                    row.add(null);
                } else if (results.getMetaData().getColumnTypeName(i).compareTo("datetime") == 0) {
                    row.add(results.getDate(i));
                } else {
                    throw new SQLException("Error processing SQL datatype:" + results.getMetaData().getColumnTypeName(i));
                }
            }
            table.add(row);
        }
        return table;
    }

    /**
     * This examines the metadata and determines if a named column should be
     * cast into an integer.
     *
     * @param metaData The mySQL metadata giving sql type information
     * @param column The column to determine type information for.
     * @return if the type should be an integer or not.
     * @throws SQLException if a database access error occurs
     */
    private static boolean isIntegerType(ResultSetMetaData metaData, int column) throws SQLException {
        return metaData.getColumnType(column) == Types.INTEGER || metaData.getColumnType(column) == Types.TINYINT;
    }

    /**
     * This examines the metadata and determines if a named column should be
     * cast into an double.
     *
     * @param metaData The mySQL metadata giving sql type information
     * @param column The column to determine type information for.
     * @return if the type should be an double or not.
     * @throws SQLException if a database access error occurs
     */
    private static boolean isDoubleType(ResultSetMetaData metaData, int column) throws SQLException {
        return metaData.getColumnType(column) == Types.DECIMAL || metaData.getColumnType(column) == Types.DOUBLE;
    }

    /**
     * This examines the metadata and determines if a named column should be
     * cast into an String.
     *
     * @param metaData The mySQL metadata giving SQL type information
     * @param column The column to determine type information for.
     * @return if the type should be an String or not.
     * @throws SQLException if a database access error occurs
     */
    private static boolean isStringType(ResultSetMetaData metaData, int column) throws SQLException {
        return metaData.getColumnType(column) == Types.VARCHAR || metaData.getColumnType(column) == Types.LONGVARCHAR;
    }

}
