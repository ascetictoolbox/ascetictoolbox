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

package es.bsc.vmmanagercore.monitoring;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ZabbixConnector {

    private final static ZabbixClient zabbixClient = new ZabbixClient();

    public static ZabbixClient getZabbixClient() {
        return zabbixClient;
    }

    // Zabbix DB. I would prefer these constants to be in the VmManagerConfiguration class...
    private static final String DB_URL = "jdbc:mysql://10.4.0.15/zabbix";
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_USER = "zabbix";
    private static final String DB_PASSWORD = "yxCHARvjZRJi";
    private static final String[] ZABBIX_TABLES = {"history", "history_uint"}; /* Zabbix stores values in several
                                                                                  tables */

    private static Connection connection = null;

    /* I have hardcoded the Zabbix IDs for each one of the hosts that we are using.
       This is a quick fix. This should be done querying Zabbix */
    public static final int ASOK09_ID = 10105;
    public static final int ASOK10_ID = 10084;
    public static final int ASOK11_ID = 10107;
    public static final int ASOK12_ID = 10106;

    // Note: the keys that we need (system.cpu.num, vm.memory.size[total], etc.) should not be hardcoded in the query
    private static final String ZABBIX_QUERY = "SELECT i.key_, h.value "
            + "FROM items i, XXXX h, "
            + "(SELECT hs.itemid, max(hs.clock) AS mostrecent FROM XXXX hs GROUP BY hs.itemid) ms "
            + "WHERE i.key_ IN ('system.cpu.num', 'system.cpu.load[all,avg1]', 'vm.memory.size[total]', " +
            " 'vm.memory.size[available]', 'vm.memory.size[available]', " +
            " 'vfs.fs.size[/var/lib/nova/instances,total]', 'vfs.fs.size[/var/lib/nova/instances,used]', 'power') AND "
            + "h.itemid = ms.itemid AND "
            + "h.clock = mostrecent AND "
            + "h.itemid = i.itemid AND "
            + "h.itemid IN (select it.itemid from hosts, items it "
            + "WHERE hosts.hostid = it.hostid AND "
            + "hosts.hostid = ?);";

    // Suppress default constructor for non-instantiability
    private ZabbixConnector() {
        throw new AssertionError();
    }

    /**
     * Establishes a connection to the database.
     *
     * @return Connection object representing the connection
     */
    private static Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        else {
            System.setProperty("jdbc.drivers", DB_DRIVER); // Define JDBC driver
            try {
                Class.forName(DB_DRIVER); // Ensure that the driver has been loaded
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return connection;
        }
    }

    /**
     * Returns the latest values available for the metrics (called items in Zabbix) of a host.
     *
     * @param hostId the Zabbix ID of the host
     * @return the latest values available for the metrics
     */
    public static Map<String, Double> getHostItems(int hostId) {
        Map<String, Double> result = new HashMap<>();
        try {
            for (String historyTable: ZABBIX_TABLES) {
                String query = ZABBIX_QUERY.replace("XXXX", historyTable);
                try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
                    preparedStatement.setInt(1, hostId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String key = resultSet.getString(1);
                        double value = resultSet.getDouble(2);
                        result.put(key, value);
                    }
                    resultSet.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
