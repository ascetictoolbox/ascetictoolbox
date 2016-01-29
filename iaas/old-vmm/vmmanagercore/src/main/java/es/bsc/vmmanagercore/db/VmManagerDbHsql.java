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

import com.google.gson.Gson;
import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;
import es.bsc.vmmanagercore.selfadaptation.options.SelfAdaptationOptions;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the interaction of the VM Manager with a HSQL database.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerDbHsql implements VmManagerDb {

    // This class needs a refactor. I should probably use something like jOOQ.

    private Connection conn;
	private Logger log;

    private final Gson gson = new Gson(); // Using JSON provisionally

    // Error messages
    private static final String ERROR_DB_CONNECTION = "There was an error while connecting to the DB";
    private static final String ERROR_SETUP_DB = "There was an error while trying to set up the DB.";
    private static final String ERROR_CLOSE_CONNECTION = "There was an error while closing the connection to the DB.";
    private static final String ERROR_CLEAN_DB = "There was an error while trying to clean the DB.";
    private static final String ERROR_INSERT_VM = "There was an error while inserting a VM in the DB.";
    private static final String ERROR_DELETE_VM = "There was an error while trying to delete a VM from the DB.";
    private static final String ERROR_DELETE_ALL_VMS = "There was an error while deleting the VMs from the DB.";
    private static final String ERROR_GET_VMS_OF_APP = "There was an error while getting the VMs IDs from the DB.";

    public VmManagerDbHsql(String dbFileNamePrefix) {
		log = Logger.getLogger(getClass());
        try {
            // Load the HSQL Database Engine JDBC driver
            Class.forName("org.hsqldb.jdbcDriver");
            // Connect to the database. This will load the DB files and start the DB if it is not already running
            conn = DriverManager.getConnection("jdbc:hsqldb:file:db/" + dbFileNamePrefix, "sa", "");
        } catch (Exception e) {
            log.error(ERROR_DB_CONNECTION + "jdbc:hsqldb:file:db/" + dbFileNamePrefix +" --> " + e.getMessage(),e);
			e.printStackTrace();
        }
        setupDb();
    }
    
    // Use for SQL command SELECT
    private synchronized List<String> query(String expression) throws SQLException {
        Statement st = conn.createStatement();
        
        // Run the query
        ResultSet rs = st.executeQuery(expression);

        List<String> result = getResult(rs);
        st.close();
        return result;
    }
    
    // Use for SQL commands CREATE, DROP, INSERT, and UPDATE
    private synchronized void update(String expression) throws SQLException {
        Statement st = conn.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
			log.error("db error : " + expression);
        }
        st.close();
    }
    
    private static List<String> getResult(ResultSet rs) throws SQLException {
        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta = rs.getMetaData();
        int colmax = meta.getColumnCount();

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        List<String> result = new ArrayList<>();
        for (; rs.next(); ) {
            for (int i = 0; i < colmax; ++i) {
                Object o = rs.getObject(i + 1); // In SQL the first column is indexed with 1 not 0
                result.add(o.toString());
            }
        }
        return result;
    }
    
    private synchronized void setupDb() {
        try {
            update("CREATE TABLE IF NOT EXISTS virtual_machines "
                    + "(id VARCHAR(255), appId VARCHAR(255), ovfId VARCHAR(255), slaId VARCHAR(255), "
                    + "PRIMARY KEY (id)) ");
            update("CREATE TABLE IF NOT EXISTS current_scheduling_alg " +
                    "(algorithm VARCHAR(255), PRIMARY KEY (algorithm))");
            update("CREATE TABLE IF NOT EXISTS self_adaptation_options "
                    + "(options LONGVARCHAR) ");
        } catch (SQLException e) {
			log.error(ERROR_SETUP_DB, e);
        }
    }
    
    @Override
    public void closeConnection() {
        try {
            conn.createStatement().execute("SHUTDOWN"); // DB writes out to files and performs clean shuts down
            conn.close(); // if there are no other open connections
        } catch (SQLException e) {
			log.error(ERROR_CLOSE_CONNECTION, e);
        }
    }
    
    public void cleanDb() {
        try {
            update("DROP TABLE virtual_machines");
            update("DROP TABLE current_scheduling_alg");
        } catch (SQLException e) {
			log.error(ERROR_CLEAN_DB, e);
        }
    }
    
    @Override
    public void insertVm(String vmId, String appId, String ovfId, String slaId) {
        try {
            update("INSERT INTO virtual_machines (id, appId, ovfId, slaId) "
                    + "VALUES ('" + vmId + "', '" + appId + "', '" + ovfId + "', '" + slaId + "')");
        } catch (SQLException e) {
			log.error(ERROR_INSERT_VM, e);
        }
    }

    @Override
    public void deleteVm(String vmId) {
        try {
            update("DELETE FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
			log.error(ERROR_DELETE_VM,e);
        }
    }
    
    @Override
    public void deleteAllVms() {
        try {
            update("DELETE FROM virtual_machines");
        } catch (SQLException e) {
			log.error(ERROR_DELETE_ALL_VMS, e);
        }
    }
    
    // Returns "" if the VM does not have an app associated or if a VM with the given ID does not exist
    @Override
    public String getAppIdOfVm(String vmId) {
        List<String> appId = new ArrayList<>();
        try {
            appId = query("SELECT appId FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
            appId.add("");
        }
        if (appId.isEmpty()) {
            appId.add("");
        }
        return appId.get(0);
    }

    // Returns "" if the VM does not have an OVF ID associated or if a VM with the given ID does not exist
    @Override
    public String getOvfIdOfVm(String vmId) {
        List<String> appId = new ArrayList<>();
        try {
            appId = query("SELECT ovfId FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
            appId.add("");
        }
        if (appId.isEmpty()) {
            appId.add("");
        }
        return appId.get(0);
    }

    // Returns "" if the VM does not have an SLA ID associated or if a VM with the given ID does not exist
    @Override
    public String getSlaIdOfVm(String vmId) {
        List<String> appId = new ArrayList<>();
        try {
            appId = query("SELECT slaId FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
            appId.add("");
        }
        if (appId.isEmpty()) {
            appId.add("");
        }
        return appId.get(0);
    }

    @Override
    public List<String> getAllVmIds() {
        List<String> vmIds = new ArrayList<>();
        try {
            vmIds = query("SELECT id FROM virtual_machines");
        } catch (SQLException e) {
            System.out.println(ERROR_GET_VMS_OF_APP);
        }
        return vmIds;
    }

    @Override
    public List<String> getVmsOfApp(String appId) {
        List<String> vmIds = new ArrayList<>();
        try {
            vmIds = query("SELECT id FROM virtual_machines WHERE appId = '" + appId + "'");
        } catch (SQLException e) {
            return vmIds; // If there are no apps with the specified ID, return an empty list
        }
        return vmIds;
    }
    
    @Override
    public SchedAlgorithmNameEnum getCurrentSchedulingAlg() {
        List<String> schedulingAlgorithms;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM current_scheduling_alg");
        } catch (SQLException e) {
            return null;
        }
        if (schedulingAlgorithms.size() != 0) {
            return SchedAlgorithmNameEnum.fromName(schedulingAlgorithms.get(0));
        }
        // If a scheduling alg. has not been selected, return Distribution by default
        return SchedAlgorithmNameEnum.DISTRIBUTION;
    }
    
    @Override
    public void setCurrentSchedulingAlg(SchedAlgorithmNameEnum alg) {
        try {
            update("DELETE FROM current_scheduling_alg");
            update("INSERT INTO current_scheduling_alg (algorithm) VALUES ('" + alg.getName() + "')");
        } catch (SQLException e) {
            // I think the INSERT may violate the PRIMARY_KEY restriction because the time it takes the DB to
            // execute the instructions, but it does not affect us
			log.error(e.getMessage(),e);
        }
    }

    @Override
    public void saveSelfAdaptationOptions(SelfAdaptationOptions options) {
        //This function uses JSON provisionally. Just because it's easier and we will change the DB soon.
        String optionsJson = gson.toJson(options, SelfAdaptationOptions.class);

        try {
            update("DELETE FROM self_adaptation_options");
            update("INSERT INTO self_adaptation_options (options) VALUES ('" + optionsJson + "')");
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }

    }

    @Override
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        //This function uses JSON provisionally. Just because it's easier and we will change the DB soon.
        List<String> optionsJson;
        try {
            optionsJson = query("SELECT options FROM self_adaptation_options");
        } catch (SQLException e) {
            return null;
        }
        if (optionsJson.size() != 0) {
            return gson.fromJson(optionsJson.get(0), SelfAdaptationOptions.class);
        }
        return null;
    }

}
