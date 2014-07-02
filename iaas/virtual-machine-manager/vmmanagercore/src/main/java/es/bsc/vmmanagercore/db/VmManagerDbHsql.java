package es.bsc.vmmanagercore.db;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class implements the interaction of the VM Manager with a HSQL database.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerDbHsql implements VmManagerDb {

    /* NOTE: For now, we are using DBHSQL. In the future, it may be needed to use a different DB. */

    private Connection conn;
    private List<SchedulingAlgorithm> availableSchedAlg = new ArrayList<>();

    // Error messages
    private static final String ERROR_SETUP_DB = "There was an error while trying to set up the DB.";
    private static final String ERROR_CLOSE_CONNECTION = "There was an error while closing the connection to the DB.";
    private static final String ERROR_CLEAN_DB = "There was an error while trying to clean the DB.";
    private static final String ERROR_INSERT_VM = "There was an error while inserting a VM in the DB.";
    private static final String ERROR_DELETE_VM = "There was an error while trying to delete a VM from the DB.";
    private static final String ERROR_DELETE_ALL_VMS = "There was an error while deleting the VMs from the DB.";
    private static final String ERROR_GET_VMS_OF_APP = "There was an error while getting the VMs IDs from the DB.";
    private static final String ERROR_INSERT_SCHED_ALGS = "There was an error saving the sched. algorithms on the DB.";
    private static final String ERROR_SET_SCHED_ALG = "There was an error while setting the scheduling algorithm";

    public VmManagerDbHsql(String dbFileNamePrefix) throws Exception {
        // Add the available scheduling algorithms
        availableSchedAlg.add(SchedulingAlgorithm.CONSOLIDATION);
        availableSchedAlg.add(SchedulingAlgorithm.DISTRIBUTION);
        availableSchedAlg.add(SchedulingAlgorithm.ENERGY_AWARE);
        availableSchedAlg.add(SchedulingAlgorithm.GROUP_BY_APP);
        availableSchedAlg.add(SchedulingAlgorithm.RANDOM);

        // Load the HSQL Database Engine JDBC driver
        Class.forName("org.hsqldb.jdbcDriver");

        // Connect to the database. This will load the DB files and start the DB if it is not already running.
        conn = DriverManager.getConnection("jdbc:hsqldb:file:db/" + dbFileNamePrefix, "sa", "");
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
            System.out.println("db error : " + expression);
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
    
    private synchronized void insertAvailableSchedAlg() {
        try {
            for (SchedulingAlgorithm schedAlg: availableSchedAlg) {
                update("INSERT INTO scheduling_alg (algorithm, selected) VALUES "
                        + " ('" + schedAlg.getAlgorithm() + "', 0)");
            }
        } catch (SQLException e) {
            System.out.println(ERROR_INSERT_SCHED_ALGS);
        }
        try {
            update("UPDATE scheduling_alg SET selected = 1 WHERE algorithm = 'distribution'");
        } catch (SQLException e) {
            System.out.println(ERROR_INSERT_SCHED_ALGS);
        }
    }
    
    private synchronized void setupDb() {
        try {
            update("CREATE TABLE IF NOT EXISTS virtual_machines "
                    + "(id VARCHAR(255), appId VARCHAR(255), PRIMARY KEY (id)) ");
            update("CREATE TABLE IF NOT EXISTS scheduling_alg "
                    + "(id VARCHAR(255), algorithm VARCHAR(255), selected INTEGER)");
        } catch (SQLException e) {
            System.out.println(ERROR_SETUP_DB);
        }

        // Insert algorithms just when the DB is created
        // TODO The if is executed more than once. This needs to be fixed.
        if (getAvailableSchedulingAlg().size() == 0) {
            insertAvailableSchedAlg();
        }
    }
    
    @Override
    public void closeConnection() {
        try {
            Statement st = conn.createStatement();
            st.execute("SHUTDOWN"); // DB writes out to files and performs clean shuts down
            conn.close(); // if there are no other open connections
        } catch (SQLException e) {
            System.out.println(ERROR_CLOSE_CONNECTION);
        }
    }
    
    public void cleanDb() {
        try {
            update("DROP TABLE virtual_machines");
            update("DROP TABLE scheduling_alg");
        } catch (SQLException e) {
            System.out.println(ERROR_CLEAN_DB);
        }
    }
    
    @Override
    public void insertVm(String vmId, String appId) {
        try {
            update("INSERT INTO virtual_machines (id, appId) VALUES ('" + vmId + "', '" + appId + "')");
        } catch (SQLException e) {
            System.out.println(ERROR_INSERT_VM);
        }
    }

    @Override
    public void deleteVm(String vmId) {
        try {
            update("DELETE FROM virtual_machines WHERE id = '" + vmId + "'");
        } catch (SQLException e) {
            System.out.println(ERROR_DELETE_VM);
        }
    }
    
    @Override
    public void deleteAllVms() {
        try {
            update("DELETE FROM virtual_machines");
        } catch (SQLException e) {
            System.out.println(ERROR_DELETE_ALL_VMS);
        }
    }
    
    // Returns "" if the VM does not have an app associated or if a VM with that ID does not exist
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
    public SchedulingAlgorithm getCurrentSchedulingAlg() {
        List<String> schedulingAlgorithms;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM scheduling_alg WHERE selected = 1");
        } catch (SQLException e) {
            return null;
        }
        String currentSchedAlg = schedulingAlgorithms.get(0);
        switch (currentSchedAlg) {
            case "consolidation":
                return SchedulingAlgorithm.CONSOLIDATION;
            case "distribution":
                return SchedulingAlgorithm.DISTRIBUTION;
            case "energyAware":
                return SchedulingAlgorithm.ENERGY_AWARE;
            case "groupByApp":
                return SchedulingAlgorithm.GROUP_BY_APP;
            case "random":
                return SchedulingAlgorithm.RANDOM;
        }
        return null;
    }
    
    @Override
    public List<SchedulingAlgorithm> getAvailableSchedulingAlg() {
        List<String> schedulingAlgorithms;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM scheduling_alg");
        } catch (SQLException e) {
            return null;
        }
        List<SchedulingAlgorithm> result = new ArrayList<>();
        for (String schedAlg: schedulingAlgorithms) {
            switch (schedAlg) {
                case "consolidation":
                    result.add(SchedulingAlgorithm.CONSOLIDATION);
                    break;
                case "distribution":
                    result.add(SchedulingAlgorithm.DISTRIBUTION);
                    break;
                case "energyAware":
                    result.add(SchedulingAlgorithm.ENERGY_AWARE);
                    break;
                case "groupByApp":
                    result.add(SchedulingAlgorithm.GROUP_BY_APP);
                    break;
                case "random":
                    result.add(SchedulingAlgorithm.RANDOM);
                    break;
            }
        }

        // Return the list without duplicates.
        // TODO there should not be duplicates. I need to fix the setupDb function.
        HashSet<SchedulingAlgorithm> hashSet = new HashSet<>();
        hashSet.addAll(result);
        result.clear();
        result.addAll(hashSet);
        return result;
    }
    
    @Override
    public void setCurrentSchedulingAlg(SchedulingAlgorithm alg) {
        String schedAlg = alg.getAlgorithm();
        try {
            update("UPDATE scheduling_alg SET selected = 0");
            update("UPDATE scheduling_alg SET selected = 1 WHERE algorithm = '" + schedAlg + "'");
        } catch (SQLException e) {
            System.out.println(ERROR_SET_SCHED_ALG);
        }
    }
}
