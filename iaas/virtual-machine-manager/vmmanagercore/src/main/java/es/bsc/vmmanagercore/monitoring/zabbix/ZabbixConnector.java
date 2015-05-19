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

package es.bsc.vmmanagercore.monitoring.zabbix;

import com.google.common.collect.ImmutableMap;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ZabbixConnector {

    private final static ZabbixClient zabbixClient = new ZabbixClient();

    // Zabbix DB. I would prefer to see these constants in a config file...
    private static final String DB_URL = "jdbc:mysql://10.4.0.15/zabbix";
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_USER = "zabbix";
    private static final String DB_PASSWORD = "yxCHARvjZRJi";
    private static final String[] ZABBIX_TABLES = {"history", "history_uint"}; /* Zabbix stores values in several
                                                                                  tables */

    private static Connection connection = null;

    /* I have hardcoded the Zabbix IDs for each one of the hosts that we are using.
       This is a quick fix. This should be done querying Zabbix or in the Zabbix wrapper */
    public static final Map<String, Integer> hostIds = ImmutableMap.<String, Integer>builder()
            .put("asok09", 10105)
            .put("asok10", 10084)
            .put("asok11", 10107)
            .put("asok12", 10106)
            .build();

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

    public static ZabbixClient getZabbixClient() {
        return zabbixClient;
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
        for (String historyTable: ZABBIX_TABLES) {
            String query = ZABBIX_QUERY.replace("XXXX", historyTable);
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
                preparedStatement.setInt(1, hostId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    result.put(resultSet.getString(1), resultSet.getDouble(2)); // key at pos 1 and value at pos 2
                }
                resultSet.close();
            }
            catch (Exception e) {
                System.out.println("Could not get data from Zabbix");
            }
        }
        return result;
    }

    /**
     * Registers a VM in Zabbix. A client who makes a deployment request should not wait
     * for Zabbix to register a VM. This is why we execute the register action in a separated thread.
     *
     * @param vmId the ID of the VM
     * @param hostname the host where the VM is deployed
     * @param ipAddress the IP address of the VM
     */
    public static void registerVmInZabbix(String vmId, String hostname, String ipAddress) {
        Thread thread = new Thread(
                new RegisterZabbixVmRunnable(vmId, hostname, ipAddress),
                "registerVmInZabbixThread");
        thread.start();
    }

    /**
     * Deletes a VM from Zabbix. A client who makes a delete VM request should not wait
     * for Zabbix to delete the VM from its DB. This is why we execute the delete action in a separated thread.
     *
     * @param vmId the ID of the VM
     * @param hostname the host where the VM is deployed
     */
    public static void deleteVmFromZabbix(String vmId, String hostname) {
        Thread thread = new Thread(
                new DeleteZabbixVmRunnable(vmId, hostname),
                "deleteVmFromZabbixThread");
        thread.start();
    }

}
