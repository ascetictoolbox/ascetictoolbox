package es.bsc.vmmanagercore.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import es.bsc.vmmanagercore.model.SchedulingAlgorithm;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerDbHsql implements VmManagerDb {

    private Connection conn;
    private ArrayList<SchedulingAlgorithm> availableSchedAlg;

    // Error messages
    private final String ERROR_SETUP_DB = "There was an error while trying to set up the DB.";
    private final String ERROR_CLOSE_CONNECTION = "There was an error while trying to close"
            + " the connection to the DB.";
    private final String ERROR_CLEAN_DB = "There was an error while trying to clean the DB.";
    private final String ERROR_INSERT_VM = "There was an error while trying to insert a VM "
            + "in the DB.";
    private final String ERROR_DELETE_VM = "There was an error while trying to delete a VM "
            + "from the DB.";
    private final String ERROR_DELETE_ALL_VMS = "There was an error while trying to delete "
            + "the VMs from the DB.";
    private final String ERROR_GET_VMS_OF_APP = "There was an error while trying to get the "
            + "VMs IDs from the DB.";
    private final String ERROR_INSERT_SCHED_ALGS = "There was an error while inserting "
            + "the scheduling algorithms on the DB.";
    private final String ERROR_SET_SCHED_ALG = "There was an error while setting the scheduling "
            + "algorithm";

    public VmManagerDbHsql(String dbFileNamePrefix) throws Exception {
        // Define the available scheduling algorithms
        availableSchedAlg = new ArrayList<>();
        availableSchedAlg.add(SchedulingAlgorithm.CONSOLIDATION);
        availableSchedAlg.add(SchedulingAlgorithm.DISTRIBUTION);
        availableSchedAlg.add(SchedulingAlgorithm.RANDOM);

        // Load the HSQL Database Engine JDBC driver
        Class.forName("org.hsqldb.jdbcDriver");

        // Connect to the database. This will load the DB files and start the
        // database if it is not already running.
        conn = DriverManager.getConnection("jdbc:hsqldb:file:db/" + dbFileNamePrefix, "sa", "");
        setupDb();
    }
    
    // Use for SQL command SELECT
    private synchronized ArrayList<String> query(String expression) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        st = conn.createStatement();
        
        // Run the query
        rs = st.executeQuery(expression);    

        ArrayList<String> result = getResult(rs);
        st.close();
        return result;
    }
    
    // Use for SQL commands CREATE, DROP, INSERT, and UPDATE
    private synchronized void update(String expression) throws SQLException {
        Statement st = null;
        st = conn.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
            System.out.println("db error : " + expression);
        }
        st.close();
    }
    
    private static ArrayList<String> getResult(ResultSet rs) throws SQLException {
        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta = rs.getMetaData();
        int colmax = meta.getColumnCount();
        int i;
        Object o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        ArrayList<String> result = new ArrayList<>();
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1); // In SQL the first column is indexed with 1 not 0
                result.add(o.toString());
            }
        }
        return result;
    }
    
    private void insertAvailableSchedAlg() {
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
    
    private void setupDb() {
        try {
            update("CREATE TABLE IF NOT EXISTS virtual_machines "
                    + "(id VARCHAR(255), appId VARCHAR(255), PRIMARY KEY (id)) ");
            update("CREATE TABLE IF NOT EXISTS scheduling_alg "
                    + "(id VARCHAR(255), algorithm VARCHAR(255), selected INTEGER)");
        } catch (SQLException e) {
            System.out.println(ERROR_SETUP_DB);
        }

        // Insert algorithms just when the DB is created
        if (getAvailableSchedulingAlg().size() == 0) {
            insertAvailableSchedAlg();
        }
    }
    
    @Override
    public void closeConnection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            // DB writes out to files and performs clean shuts down
            st.execute("SHUTDOWN");
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
            update("INSERT INTO virtual_machines (id, appId) "
                    + "VALUES ('" + vmId + "', '" + appId + "')");
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
        ArrayList<String> appId = new ArrayList<>();
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
    public ArrayList<String> getAllVmIds() {
        ArrayList<String> vmIds = null;
        try {
            vmIds = query("SELECT id FROM virtual_machines");
        } catch (SQLException e) {
            System.out.println(ERROR_GET_VMS_OF_APP);
        }
        return vmIds;
    }

    @Override
    public ArrayList<String> getVmsOfApp(String appId) {
        ArrayList<String> vmIds = new ArrayList<>();
        try {
            vmIds = query("SELECT id FROM virtual_machines WHERE appId = '" + appId + "'");
        } catch (SQLException e) {
            return vmIds; // If there are no apps with the specified ID, return an empty list
        }
        return vmIds;
    }
    
    @Override
    public SchedulingAlgorithm getCurrentSchedulingAlg() {
        ArrayList<String> schedulingAlgorithms = null;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM scheduling_alg WHERE selected = 1");
        } catch (SQLException e) {
            return null;
        }
        if (schedulingAlgorithms.get(0).equals("consolidation")) {
            return SchedulingAlgorithm.CONSOLIDATION;
        }
        else if (schedulingAlgorithms.get(0).equals("distribution")) {
            return SchedulingAlgorithm.DISTRIBUTION;
        }
        else if (schedulingAlgorithms.get(0).equals("random")) {
            return SchedulingAlgorithm.RANDOM;
        }
        return null;
    }
    
    @Override
    public ArrayList<SchedulingAlgorithm> getAvailableSchedulingAlg() {
        ArrayList<String> schedulingAlgorithms = null;
        try {
            schedulingAlgorithms = query("SELECT algorithm FROM scheduling_alg");
        } catch (SQLException e) {
            return null;
        }
        ArrayList<SchedulingAlgorithm> result = new ArrayList<>();
        for (String schedAlg: schedulingAlgorithms) {
            if (schedAlg.equals("consolidation")) {
                result.add(SchedulingAlgorithm.CONSOLIDATION);
            }
            else if (schedAlg.equals("distribution")) {
                result.add(SchedulingAlgorithm.DISTRIBUTION);
            }
            else if (schedAlg.equals("random")) {
                result.add(SchedulingAlgorithm.RANDOM);
            }
        }
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
