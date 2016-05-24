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

package es.bsc.demiurge.core.db;

import com.google.gson.Gson;
import es.bsc.demiurge.core.auth.JdbcUserDao;
import es.bsc.demiurge.core.auth.UserDao;
import es.bsc.demiurge.core.models.vms.VmRequirements;
import es.bsc.demiurge.core.selfadaptation.options.SelfAdaptationOptions;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * This class implements the interaction of the VM Manager with a HSQL database.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */

// TODO: this class is full of SQL injections risks. Solve this issue
public class VmManagerDbHsql implements VmManagerDb {

    // This class needs a refactor. I should probably use something like jOOQ.

    private Connection conn;
	private Logger log;

    private final Gson gson = new Gson(); // Using JSON provisionally

	private UserDao userDao;
    private List<Integer> lastRequirementsAdded;

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
			log.debug("DB connection to jdbc:hsqldb:file:db/" + dbFileNamePrefix);
            conn = DriverManager.getConnection("jdbc:hsqldb:file:db/" + dbFileNamePrefix, "sa", "");
        } catch (Exception e) {
            log.error(ERROR_DB_CONNECTION + "jdbc:hsqldb:file:db/" + dbFileNamePrefix +" --> " + e.getMessage(),e);
			e.printStackTrace();
        }
		userDao = new JdbcUserDao(conn);
        lastRequirementsAdded = new ArrayList<>();

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
			BufferedReader br = new BufferedReader(new InputStreamReader(VmManagerDb.class.getResourceAsStream("/createDb.sql")));
			StringBuffer sqlScript = new StringBuffer();
			String lastLine = null;
			do {
				lastLine = br.readLine();
				if(lastLine != null) {
					sqlScript.append(lastLine).append(' ');
				}
			} while(lastLine != null);

			Statement script = conn.createStatement();
			for(String sql : sqlScript.toString().split(";")) {
				log.debug("Adding script to batch: " + sql);
				script.addBatch(sql);
			}
			script.executeBatch();
			script.close();
			conn.commit();
			if(userDao.countUsers() == 0) {
				userDao.insertUser("admin","changeme");
			}

        } catch (Exception e) {
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
    public void insertVm(String vmId, String appId, String ovfId, String slaId, VmRequirements vmRequirements) {
        this.insertVm(vmId, appId, ovfId, slaId);
        //TODO: Include cpu, ram, disk, swap, requirements. Now they are included via OpenStack and cannot be changed.
        for(Entry<String, String> req : vmRequirements.getOptionalRequirements().entrySet()){
            insertRequirement(vmId, req.getKey(), req.getValue());
        }
    }

    @Override
    public void deleteVm(String vmId) {
        try {
            update("DELETE FROM vm_requirements WHERE fk_id_virtual_machines = '" + vmId + "'");
            update("DELETE FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
			log.error(ERROR_DELETE_VM,e);
        }
    }
    
    @Override
    public void deleteAllVms() {
        try {
            update("DELETE FROM vm_requirements");
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
    public String getRequirementValue(String vmId, String requirement) {
        List<String> requirementValue = new ArrayList<>();
        try {
            requirementValue = 
                query("SELECT requirement_value FROM vm_requirements WHERE fk_id_virtual_machines = '" + vmId + "' AND requirement = '" + requirement + "'");
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }
        
        if (!requirementValue.isEmpty()) {
            return requirementValue.get(requirementValue.size()-1);
        }
        
        return null;
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
    public String getCurrentSchedulingAlg() {
        List<String> schedulingAlgorithms;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM current_scheduling_alg");
        } catch (SQLException e) {
            return null;
        }
        if (schedulingAlgorithms.size() != 0) {
            return schedulingAlgorithms.get(0);
        }
        // If a scheduling alg. has not been selected, return Distribution by default
		// quick hack. TODO: allow configuring default
        return "distribution";
    }
    
    @Override
    public void setCurrentSchedulingAlg(String alg) {
        try {
            update("DELETE FROM current_scheduling_alg");
            update("INSERT INTO current_scheduling_alg (algorithm) VALUES ('" + alg + "')");
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

	@Override
	public UserDao getUserDao() {
		return userDao;
	}
    
    private int getLastId(String table) {
        List<String> lastId = new ArrayList<>();
        try {
            lastId = query("SELECT max(id) FROM " + table);
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }
        if (!lastId.isEmpty()) {
            return Integer.parseInt(lastId.get(0));
        }
        return -1;
    }
    
    /**
     * Adds a new requirement to an existing VM. The id must be saved in case we need to rollback.
     * 
     * @param vmId
     * @param requirement
     * @param value
     * @return 
     */
    @Override
    public int insertRequirement(String vmId, String requirement, String value) {
        try {
            update("INSERT INTO vm_requirements (fk_id_virtual_machines, requirement, requirement_value) VALUES ('" + vmId + "', '" + requirement + "', '" + value + "')");
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
            return -1;
        }
        
        int id = getLastId("vm_requirements");
        lastRequirementsAdded.add(id);
        
        return id;
    }
    
    /**
     * Inserts a set of requirements for a set of VMs.
     * 
     * @param newRequirements a map where each key is a vmId and the value a map with all requirements and its values.
     */
    @Override
    public void insertRequirements(Map<String,Map<String, String>> newRequirements){
        if(newRequirements == null || newRequirements.isEmpty()){
            return;
        }
        for(String vmId : newRequirements.keySet()){
            for(Entry<String, String> requirement : newRequirements.get(vmId).entrySet()){
                insertRequirement(vmId, requirement.getKey(), requirement.getValue());
            }
        }
    }
    
    private int deleteRequirement(int id) {
        try {
            update("DELETE FROM vm_requirements WHERE id = " + id);
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }
        return 1;
    }
    
    /**
     * Rollbacks last requirements added to VMs. For that we use the list lastRequirementsAdded which is mantained by insertRequirement method.
     * TODO: Check situations where this data can become corrupt i.e. VMM turning off in a middle of a rollback.
     */
    @Override
    public void rollbackRequirements() {
        for(int id : lastRequirementsAdded){
            deleteRequirement(id);
        }
    }
    
    /**
     * We commit last requirements by erasing list of last requirements added (so rollback become impossible after this)
     */
    @Override
    public void commitRequirements() {
        lastRequirementsAdded.clear();
    }
}
