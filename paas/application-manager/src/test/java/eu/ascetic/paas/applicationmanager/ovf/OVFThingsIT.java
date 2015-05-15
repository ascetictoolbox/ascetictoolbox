package eu.ascetic.paas.applicationmanager.ovf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmclient.models.ImageToUpload;
import es.bsc.vmmclient.models.Vm;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.File;

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
 * e-mail david.garciaperez@atos.net
 */

public class OVFThingsIT {

	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.vmc.xml";
	private String threeTierWebAppOvfString;
	private OvfDefinition ovfDocument;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		java.io.File file = new java.io.File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	//@Test
	public void getApplicationNameTest() throws IOException {
		System.out.println(threeTierWebAppOvfString);
		String name = OVFUtils.getApplicationName(threeTierWebAppOvfString);
		assertEquals("threeTierWebApp", name);
		
		name = OVFUtils.getApplicationName("sdadad");
		assertEquals(null, name);
	}
	
	@Test
	public void getImagesToUpload() throws IOException {
		ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
		
		// We check all the Virtual Systems in the OVF file
		for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
			
			VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
			String ovfID = virtualSystem.getId();
			
			System.out.println(" Virtual System: " + virtualSystem.getName());
			
			VirtualHardwareSection virtualHardwareSection = virtualSystem.getVirtualHardwareSection();
			
			// We find the disk id for each resource... 
			String diskId = "";
			for (int j=0; j<virtualHardwareSection.getItemArray().length; j++) {
				Item item = virtualHardwareSection.getItemAtIndex(j);
				if (item.getResourceType().getNumber() == 17){
					String list[] = item.getHostResourceArray();
					
					if (list!=null && list.length >0){
						String hostResource = list[0];
						System.out.println("Host Resource: " + hostResource);
						diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
						System.out.println("Disk Id: " + diskId);
					}				
				}
			}
			
			String fileId = "";
			int capacity = 0;
			// We find the file id for each resource
			Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
			if (diskList != null && diskList.length>0) {
				for (int k = 0; k<diskList.length; k++) {
					Disk disk = diskList[k];
					if (disk.getDiskId().equalsIgnoreCase(diskId)) {
						fileId = disk.getFileRef();
						String units = disk.getCapacityAllocationUnits();
						capacity = Integer.parseInt(disk.getCapacity());
						capacity = OVFUtils.getDiskCapacityInGb(capacity, units);
						System.out.println("Disk reference: " + fileId);
					}
				}
			}
			
			// We get the images urls... 
			String urlImg = null;
			File[] files = ovfDocument.getReferences().getFileArray();
			if (files != null && files.length>0){
				for (int j = 0; j < files.length; j++){
					File file = files[j];
					if (file.getId().equalsIgnoreCase(fileId)){
						urlImg = file.getHref();
						System.out.println("URL to image: " + urlImg);
					}
				}
			}
			else {
				System.out.println("No references section available in OVF!!");
			}
			
			 
			String name = urlImg.substring(urlImg.lastIndexOf("/")+1, urlImg.length());
			ImageToUpload imgToUpload = new ImageToUpload(name, urlImg);
			System.out.println("Image to upload name: " + imgToUpload.getName() + " url " + imgToUpload.getUrl());
			
			//Now we have the image... lets see what it is the rest to build the VM to Upload...
			String ovfVirtualSystemID = virtualSystem.getId();
			int asceticUpperBound = virtualSystem.getProductSectionAtIndex(0).getUpperBound();
			String vmName = virtualSystem.getName();
			int cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
			int ramMb = virtualSystem.getVirtualHardwareSection().getMemorySize();
			String isoPath = OVFUtils.getIsoPathFromVm(virtualSystem.getVirtualHardwareSection(), ovfDocument);
			
			System.out.println(" OVF-ID: " + ovfVirtualSystemID + " #VMs: " + asceticUpperBound + " Name: " + vmName + " CPU: " + cpus + " RAM: " + ramMb + " Disk capacity: " + capacity + " ISO Path: " + isoPath);
			
			String suffix = "_1";
			Vm virtMachine = new Vm(vmName + suffix, "imgId", cpus, ramMb, capacity, 0,  isoPath + suffix , ovfDocument.getVirtualSystemCollection().getId(), ovfID, "");
			System.out.println("virtMachine: " + virtMachine);
		}
	}
	
	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
