package es.bsc.vmmanagercore.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerDbHsqlTest {
	
	private static VmManagerDbHsql db;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void setUpBeforeClass() {
		//create a test DB
		try {
			db = new VmManagerDbHsql("testDb");
			db.deleteAllVms();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		//remove everything from the DB so the next test can start with a fresh DB
		db.cleanDb();
		
		//close the connection to the DB to save changes
		db.closeConnection();
	}

	@After
	public void tearDown() {
		//delete all the VMs created during the test.
		db.deleteAllVms();
	}
	
	@Test
	public void insertVm() {
		db.insertVm("vmId1", "appId1");
		assertTrue(db.getAllVmIds().contains("vmId1"));
	}
	
	@Test
	public void deleteExistingVm() {
		db.insertVm("vmId1", "appId1");
		db.deleteVm("vmId1");
		assertFalse(db.getAllVmIds().contains("vmId1"));
	}
	
	@Test
	public void deleteAllVmsWhenThereAreTwo() {
		db.insertVm("vmId1", "appId1");
		db.insertVm("vmId2", "appId2");
		db.deleteAllVms();
		assertFalse(db.getAllVmIds().contains("vmId1"));
		assertFalse(db.getAllVmIds().contains("vmId2"));
	}
	
	@Test
	public void deleteAllVmsWhenThereAreNoVms() {
		db.deleteAllVms();
		assertTrue(db.getAllVmIds().isEmpty());
	}
	
	@Test
	public void getAppIdOfExistingVm() {
		db.insertVm("vmId1", "appId1");
		assertEquals("appId1", db.getAppIdOfVm("vmId1"));
	}
	
	@Test
	public void getAppIdOfNonExistingVmReturnsEmptyString() {
		assertEquals("", db.getAppIdOfVm("vmId1"));
	}
	
	@Test
	public void getAllVmsIds() {
		db.insertVm("vmId1", "appId1");
		db.insertVm("vmId2", "appId2");
		ArrayList<String> vmIds = db.getAllVmIds();
		assertTrue(vmIds.contains("vmId1") && vmIds.contains("vmId2"));
	}
	
	@Test
	public void getAllVmsIdsWhenThereAreNoVms() {
		assertTrue(db.getAllVmIds().isEmpty());
	}
	
	@Test
	public void getVmsIdsOfAnAppWithVms() {
		db.insertVm("vmId1", "appId1");
		db.insertVm("vmId2", "appId1");
		ArrayList<String> vmIdsOfApp = db.getVmsOfApp("appId1");
		assertTrue(vmIdsOfApp.contains("vmId1") && vmIdsOfApp.contains("vmId2"));
	}
	
	@Test
	public void getVmsIdsOfAnAppWithNoVms() {
		assertTrue(db.getVmsOfApp("appId1").isEmpty());
	}
	
}
