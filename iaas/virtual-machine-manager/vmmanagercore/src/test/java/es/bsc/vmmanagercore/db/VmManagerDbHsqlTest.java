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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the VmManagerDbHsql class.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerDbHsqlTest {
	
	private static VmManagerDb db;
	
	@BeforeClass
	public static void setUpBeforeClass() {
        db = VmManagerDbFactory.getDb("tesDb");
        db.deleteAllVms();
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
		List<String> vmIds = db.getAllVmIds();
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
		List<String> vmIdsOfApp = db.getVmsOfApp("appId1");
		assertTrue(vmIdsOfApp.contains("vmId1") && vmIdsOfApp.contains("vmId2"));
	}
	
	@Test
	public void getVmsIdsOfAnAppWithNoVms() {
		assertTrue(db.getVmsOfApp("appId1").isEmpty());
	}
	
}
