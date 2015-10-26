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
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

    private static final String DB_URL = "jdbc:mysql://" + VmManagerConfiguration.getInstance().zabbixDbIp + "/zabbix";
    private static final String DB_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String DB_USER = VmManagerConfiguration.getInstance().zabbixDbUser;
    private static final String DB_PASSWORD = VmManagerConfiguration.getInstance().zabbixDbPassword;
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
            .put("wally152", 10140)
            .put("wally153", 10141)
            .put("wally154", 10142)
            .put("wally155", 10143)
            .put("wally157", 10112)
            .put("wally158", 10113)
            .put("wally159", 10114)
            .put("wally160", 10115)
            .put("wally161", 10116)
            .put("wally162", 10117)
            .put("wally163", 10118)
            .put("wally164", 10119)
            .put("wally165", 10120)
            .put("wally166", 10121)
            .put("wally167", 10122)
            .put("wally168", 10123)
            .put("wally169", 10124)
            .put("wally170", 10125)
            .put("wally171", 10126)
            .put("wally172", 10127)
            .put("wally173", 10128)
            .put("wally174", 10129)
            .put("wally175", 10130)
            .put("wally176", 10131)
            .put("wally177", 10132)
            .put("wally178", 10133)
            .put("wally179", 10134)
            .put("wally180", 10135)
            .put("wally181", 10136)
            .put("wally182", 10137)
            .put("wally193", 10111)
            .put("wally195", 10110)
            .put("wally196", 10109)
            .put("wally197", 10108)
            .put("wally198", 10107)
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
				if(!doNotSpam)
					Logger.getLogger(ZabbixConnector.class).warn("Could not get data from Zabbix",e);
				doNotSpam = true;
            }
        }
        return result;
    }
	private static boolean doNotSpam = false;

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

	public static void migrateVmInZabbix(final String vmId, final String ipAddress) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				LogManager.getLogger(getClass()).warn("TO DO: Zabbix should refresh quicker: " + vmId);

//				zabbixClient.deleteVM(vmId);
//				zabbixClient.createVM(vmId, ipAddress);
			}
		}, "migrateVmInZabbixThread").start();
	}

}
