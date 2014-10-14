package eu.ascetic.paas.applicationmanager.vmmanager.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net
 * 
 * This class is the Unit test that verifies the correct work of the VM Manager Client for ASCETiC
 */
public class VmManagerClientHCTest {
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	
	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
	}
	
	@Test
	public void pojo() {
		VmManagerClient vmManager = new VmManagerClientHC();
		vmManager.setURL("url");
		assertEquals("url", vmManager.getURL());
	}

	@Test
	public void checkConstructor() {
		Configuration.vmManagerServiceUrl = "vm-manager-url";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		assertEquals("vm-manager-url", vmManager.getURL());
	}
	
	@Test
	public void getAllImagesTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String listOfImagesString = "{\"images\":" +
										"[{\"id\":\"75151aa1-4518-406e-9aec-c1e45e92db57\",\"name\":\"ascetic-pm-coreNormal-img.img\",\"status\":\"ACTIVE\"}," +
										"{\"id\":\"ce1483ab-0399-443e-9e25-3a2e77cf873f\",\"name\":\"ascetic-pm-coreOptimal-img.img\",\"status\":\"ACTIVE\"}]}";
		
		mServer.addPath("/images", listOfImagesString);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ListImagesUploaded imagesUploaded = vmManager.getAllImages();
		assertEquals(2, imagesUploaded.getImages().size());
	}
	
	@Test
	public void getImageTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String imageString = "{\"id\":\"ce1483ab-0399-443e-9e25-3a2e77cf873f\",\"name\":\"ascetic-pm-coreOptimal-img.img\",\"status\":\"ACTIVE\"}";
		
		mServer.addPath("/images/ce1483ab-0399-443e-9e25-3a2e77cf873f", imageString);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		ImageUploaded imageUploaded = vmManager.getImage("ce1483ab-0399-443e-9e25-3a2e77cf873f");
		assertEquals("ascetic-pm-coreOptimal-img.img", imageUploaded.getName());
	}
	
	@Test
	public void getAllVmsTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String listOfVmsString = "{\"vms\":[" + 
									"{\"id\":\"5bca1bfd-da97-4411-93e8-e35e8dcf2f07\",\"ipAddress\":\"10.4.0.29\",\"state\":\"ACTIVE\",\"created\":\"Oct 6, 2014 3:17:41 PM\",\"hostName\":\"asok09\",\"name\":\"ascetic-pm-coreNormal_1\",\"image\":\"75151aa1-4518-406e-9aec-c1e45e92db57\",\"cpus\":2,\"ramMb\":4096,\"diskGb\":12,\"applicationId\":\"JEPlus\"}," + 
									"{\"id\":\"fd73bcb6-ea46-46ba-837f-0ea056e992ef\",\"ipAddress\":\"10.4.0.28\",\"state\":\"ACTIVE\",\"created\":\"Oct 6, 2014 3:16:30 PM\",\"hostName\":\"asok09\",\"name\":\"ascetic-pm-coreOptimal_1\",\"image\":\"ce1483ab-0399-443e-9e25-3a2e77cf873f\",\"cpus\":4,\"ramMb\":4096,\"diskGb\":16,\"applicationId\":\"JEPlus\"}]}";
		
		mServer.addPath("/vms", listOfVmsString);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		ListVmsDeployed listVmsDeployed = vmManager.getAllVMs();
		assertEquals(2, listVmsDeployed.getVms().size());
	}
	
	@Test
	public void getVMTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String vmString = "{\"id\":\"5bca1bfd-da97-4411-93e8-e35e8dcf2f07\",\"ipAddress\":\"10.4.0.29\",\"state\":\"ACTIVE\",\"created\":\"Oct 6, 2014 3:17:41 PM\",\"hostName\":\"asok09\",\"name\":\"ascetic-pm-coreNormal_1\",\"image\":\"75151aa1-4518-406e-9aec-c1e45e92db57\",\"cpus\":2,\"ramMb\":4096,\"diskGb\":12,\"applicationId\":\"JEPlus\"}";
		
		mServer.addPath("/vms/5bca1bfd-da97-4411-93e8-e35e8dcf2f07", vmString);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		VmDeployed vmDeployed = vmManager.getVM("5bca1bfd-da97-4411-93e8-e35e8dcf2f07");
		assertEquals("ACTIVE", vmDeployed.getState());
	}
	
	@Test
	public void getVMsOfAApp() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String listVMsOfAApp = "{\"vms\":[" + 
									"{\"id\":\"10fd710d-fae5-426a-9aa0-ceff39ffbeee\",\"ipAddress\":\"10.4.0.26\",\"state\":\"ACTIVE\",\"created\":\"Oct 6, 2014 3:07:34 PM\",\"hostName\":\"asok12\",\"name\":\"ascetic-pm-coreNormal_1\",\"image\":\"734e1d5e-484a-4721-94a0-4a9ae7911d67\",\"cpus\":2,\"ramMb\":4096,\"diskGb\":12,\"applicationId\":\"JEPlus\"}," + 
									"{\"id\":\"1497ed6e-b381-419a-9096-b730a7d90007\",\"ipAddress\":\"10.4.0.25\",\"state\":\"ACTIVE\",\"created\":\"Oct 6, 2014 3:06:44 PM\",\"hostName\":\"asok12\",\"name\":\"ascetic-pm-coreOptimal_1\",\"image\":\"b564e53e-48be-463e-977a-5b5c9820b427\",\"cpus\":4,\"ramMb\":4096,\"diskGb\":16,\"applicationId\":\"JEPlus\"}]}";
		
		mServer.addPath("/vmsapp/JEPlus", listVMsOfAApp);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		ListVmsDeployed listVmsDeployed = vmManager.getVmsOfApp("JEPlus");
		assertEquals(2, listVmsDeployed.getVms().size());
	}
	
	@Test
	public void uploadImageTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String payload = "{\"id\":\"f7e36928-8e1f-472c-a2eb-6db21d8b23af\"}";
		
		mServer.addPath("/images", payload);
		
		ImageToUpload iaageToUpload = new ImageToUpload("name", "url");
		VmManagerClient vmManager = new VmManagerClientHC();
		String id = vmManager.uploadImage(iaageToUpload);
		
		assertEquals("f7e36928-8e1f-472c-a2eb-6db21d8b23af", id);
	}
	
	@Test
	public void deleteImageTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String id = "f7e36928-8e1f-472c-a2eb-6db21d8b23af";
		
		mServer.addPath("/images/f7e36928-8e1f-472c-a2eb-6db21d8b23af", "");

		VmManagerClient vmManager = new VmManagerClientHC();
		boolean deleted = vmManager.deleteImage(id);
		
		assertTrue(deleted);
	}
	
	@Test
	public void deployVmsTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String payload = "{\"ids\":[{\"id\":\"c8e488f9-8798-4294-a446-aa1c18034288\"}]}";
		
		mServer.addPath("/vms", payload);
		
		Vm virtMachine = new Vm("JMeter", "imageId", 1, 1024, 20, "initScript=/DFS/ascetic/vm-images/threeTierWebApp/jmeter.iso_1" , "DavidGIntegrationTest" );
		List<Vm> vms = new ArrayList<Vm>();
		vms.add(virtMachine);
		
		VmManagerClient vmManager = new VmManagerClientHC();
		List<String> vmsIds = vmManager.deployVMs(vms);
		
		assertEquals(1, vmsIds.size());
		assertEquals("c8e488f9-8798-4294-a446-aa1c18034288", vmsIds.get(0));
	}
	
	@Test
	public void deleteVmTest() {
		Configuration.vmManagerServiceUrl = mBaseURL;
		
		String id = "f7e36928-8e1f-472c-a2eb-6db21d8b23af";
		
		mServer.addPath("/vms/f7e36928-8e1f-472c-a2eb-6db21d8b23af", "");

		VmManagerClient vmManager = new VmManagerClientHC();
		boolean deleted = vmManager.deleteVM(id);
		
		assertTrue(deleted);
	}
}