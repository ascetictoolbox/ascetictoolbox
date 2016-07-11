package eu.ascetic.paas.applicationmanager.ovf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmclient.models.ImageToUpload;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;

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
 * 
 * Collection of unit tests to validate the correct functionality of the OVFUtils class
 *
 */
public class OVFUtilsTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	private String threeTierWebAppDEMOOvfFile = "3tier-webapp.ovf.vmc.xml";
	private String threeTierWebAppDEMOOvfString;
	private String threeTierWebAppWithUpperLimitsFile = "3tier-webapp.ovf-only-upper-bounds.xml";
	private String threeTierWebAppWithUpperLimitsString;
	private String ovfSelfAdaptationFile = "output-file-ovf-appPackager.ovf";
	private String ovfSelfAdaptationString;
	private String atcOVFFile="atc-ovf.xml";
	private String atcOVFString;
	private OvfDefinition ovfDocument;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		// Reading the OVF file with DEMO tags...
		file = new File(this.getClass().getResource( "/" + threeTierWebAppDEMOOvfFile ).toURI());		
		threeTierWebAppDEMOOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		// Reading the OVF file with DEMO tags...
		file = new File(this.getClass().getResource( "/" + threeTierWebAppWithUpperLimitsFile ).toURI());		
		threeTierWebAppWithUpperLimitsString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		// Reading the OVF file with ATC OVF ...
		file = new File(this.getClass().getResource( "/" + atcOVFFile ).toURI());		
		atcOVFString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		// Reading ovf with self adatpation
		file = new File(this.getClass().getResource( "/" + ovfSelfAdaptationFile ).toURI());		
		ovfSelfAdaptationString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void getAppSlaTerms() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(ovfSelfAdaptationString);
		AsceticSLAInfo info = OVFUtils.getAppSlaInfo(ovfDocument, "app_energy_consumption");
		
		assertEquals("app_energy_consumption", info.getTerm());
		assertEquals("violation", info.getType());
		assertEquals("2000", info.getBoundaryValue());
		assertEquals("LT", info.getComparator());
		assertEquals("WattHour", info.getMetricUnit());
		
		info = OVFUtils.getAppSlaInfo(ovfDocument, "app_charges");
		
		assertEquals("app_charges", info.getTerm());
		assertEquals("violation", info.getType());
		assertEquals("50", info.getBoundaryValue());
		assertEquals("LTE", info.getComparator());
		assertEquals("Euro", info.getMetricUnit());
		
		info = OVFUtils.getAppSlaInfo(ovfDocument, "XXXX");
		assertNull(info);
		
		ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppDEMOOvfString);
		info = OVFUtils.getAppSlaInfo(ovfDocument, "app_energy_consumption");
		assertNull(info);
	}
	
	@Test
	public void getDeploymentName() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/saas.ovf" ).toURI());
		String ovf = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		assertEquals("SuperDeploymentName", OVFUtils.getDeploymentName(ovf));
		assertNull(OVFUtils.getDeploymentName(threeTierWebAppDEMOOvfString));
	}
	
	@Test
	public void determineIfAVirtualSystemHasACacheImage() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppDEMOOvfString);
		VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
		
		VirtualSystem virtualSystem1 = vsc.getVirtualSystemArray()[0];
		assertFalse(OVFUtils.usesACacheImage(virtualSystem1));
		
		VirtualSystem virtualSystem2 = vsc.getVirtualSystemArray()[1];
		assertTrue(OVFUtils.usesACacheImage(virtualSystem2));
		
		VirtualSystem virtualSystem3 = vsc.getVirtualSystemArray()[2];
		assertFalse(OVFUtils.usesACacheImage(virtualSystem3));
		
		VirtualSystem virtualSystem4 = vsc.getVirtualSystemArray()[3];
		assertTrue(OVFUtils.usesACacheImage(virtualSystem4));
	}
	
	@Test
	public void integrationTest() throws Exception {
		File file = new File(this.getClass().getResource( "/saas.ovf" ).toURI());
		String ovf = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		ovfDocument = OvfDefinition.Factory.newInstance(ovf);
		System.out.println("OVF: " + ovfDocument);
		
		VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
		ProductSection productSection = vsc.getProductSectionAtIndex(0);
		assertEquals("SuperDeploymentName", productSection.getDeploymentName());
		
		for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
			VirtualSystem virtSystem = vsc.getVirtualSystemAtIndex(i);
			System.out.println("Virtual System: " + virtSystem);
			
			int asceticUpperBound = virtSystem.getProductSectionAtIndex(0).getUpperBound();
			String vmName = virtSystem.getName();
			int cpus = virtSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
			int ramMb = virtSystem.getVirtualHardwareSection().getMemorySize();
			int diskSize = getDiskSizeFromVm(getDiskId(virtSystem.getVirtualHardwareSection()), ovfDocument);
			String isoPath = getIsoPathFromVm(virtSystem.getVirtualHardwareSection(), ovfDocument);
			
			System.out.println("THINGS: " + cpus + " " + ramMb + " " + diskSize + " " + isoPath + " " + vmName + " " + asceticUpperBound);
			
			ImageToUpload imgToUpload = getImageToUpload(getImgFileRefOvfDocument(virtSystem.getVirtualHardwareSection(), 
					ovfDocument));
			
			System.out.println("IMAGES: " + imgToUpload.getName());
			System.out.println("OVF-ID: " + virtSystem.getId());
		}
	}
	
	private String getImgFileRefOvfDocument(VirtualHardwareSection virtualHardwareSection, 
			OvfDefinition ovfDefinition) {
		String urlImg = null;
		String fileId = getFileIdFromDiskId(getDiskId(virtualHardwareSection), ovfDefinition);
		System.out.println("FILE ID: " + fileId);
		eu.ascetic.utils.ovf.api.File[] files = ovfDefinition.getReferences().getFileArray();
		if (files != null && files.length>0){
			eu.ascetic.utils.ovf.api.File file = null;
			for (int i = 0; i<files.length; i++){
				file = files[i];
				if (file.getId().equalsIgnoreCase(fileId)){
					return file.getHref();
				}
			}
		}
		else {
			System.out.println("No references section available in OVF!!");
		}
		
		return urlImg;
	}
	
	private ImageToUpload getImageToUpload(String imgFileRefOvfDocument) {
		System.out.println("imgFileRefOvfDocument " + imgFileRefOvfDocument);
		ImageToUpload imgToUpload = null;
		if (!imgFileRefOvfDocument.equalsIgnoreCase("")){
			String name = imgFileRefOvfDocument.substring(imgFileRefOvfDocument.lastIndexOf("/")+1, imgFileRefOvfDocument.length());
			imgToUpload = new ImageToUpload(name, imgFileRefOvfDocument);
		}
		return imgToUpload;
	}
	
	private String getIsoId(VirtualHardwareSection virtHwSection){
		String isoId = "";
		Item item = null;
		for (int i=0; i<virtHwSection.getItemArray().length; i++){
			item = virtHwSection.getItemAtIndex(i);
			if (item.getDescription().equalsIgnoreCase("VM CDROM")){
				String list[] = item.getHostResourceArray();
				String hostResource = "";
				if (list!=null && list.length >0){
					hostResource = list[0];
					isoId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					return isoId;
				}				
			}
		}
		return isoId;
	}
	
	private String getFileIdFromDiskId(String diskId, OvfDefinition ovfDocument){
		String fileId = null;
		if (!diskId.equalsIgnoreCase("")){
			Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
			if (diskList != null && diskList.length>0){
				Disk disk = null;
				for (int i = 0; i<diskList.length; i++){
					disk = diskList[i];
					if (disk.getDiskId().equalsIgnoreCase(diskId)){
						return disk.getFileRef();
					}
				}
			}
			else {
				System.out.println("No disk section available in OVF!!");
			}
		}
		return fileId;
	}
	
	
	private String getIsoPathFromVm(VirtualHardwareSection virtHwSection, OvfDefinition ovfDocument){
		String isoPath = null;
		String fileId = getFileIdFromDiskId(getIsoId(virtHwSection), ovfDocument);
		eu.ascetic.utils.ovf.api.File[] files = ovfDocument.getReferences().getFileArray();
		if (files != null && files.length>0){
			eu.ascetic.utils.ovf.api.File file = null;
			for (int i = 0; i<files.length; i++){
				file = files[i];
				if (file.getId().equalsIgnoreCase(fileId)){
					return file.getHref();
				}
			}
		}
		else {
			System.out.println("No references section available in OVF!!");
		}
		
		return isoPath;
	}
	
	private String getDiskId(VirtualHardwareSection virtHwSection){
		String diskId = "";
		Item item = null;
		for (int i=0; i<virtHwSection.getItemArray().length; i++){
			item = virtHwSection.getItemAtIndex(i);
			if (item.getResourceType().getNumber() == 17){
				String list[] = item.getHostResourceArray();
				String hostResource = "";
				if (list!=null && list.length >0){
					hostResource = list[0];
					diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					return diskId;
				}				
			}
		}
		System.out.println("DISK ID: " + diskId);
		return diskId;
	}
	
	private  int getDiskSizeFromVm(String diskId, OvfDefinition ovfDocument){
		int diskSize = 0;
		Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
		if (diskList != null && diskList.length>0){
			Disk disk = null;
			for (int i = 0; i<diskList.length; i++){
				disk = diskList[i];
				if (disk.getDiskId().equalsIgnoreCase(diskId)){
					return Integer.parseInt(disk.getCapacity());
				}
			}
		}
		else {
			System.out.println("No disk section available in OVF!!");
		}
		return diskSize;
	}
	

	@Test
	public void getApplicationNameTest() throws IOException {
		System.out.println(threeTierWebAppOvfString);
		String name = OVFUtils.getApplicationName(threeTierWebAppOvfString);
		assertEquals("threeTierWebApp", name);
		
		name = OVFUtils.getApplicationName("sdadad");
		assertEquals(null, name);
	}
	
	@Test
	public void containsVMWithThatOvfIdTest() {
		boolean response = OVFUtils.containsVMWithThatOvfId(threeTierWebAppOvfString, "haproxy");
		assertTrue(response);
		
		response = OVFUtils.containsVMWithThatOvfId(threeTierWebAppOvfString, "pepito");
		assertFalse(response);
		
		response = OVFUtils.containsVMWithThatOvfId(threeTierWebAppOvfString, null);
		assertFalse(response);
	}
	
	@Test
	public void determineVMLimits() {
		// Without lowerBound
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppWithUpperLimitsString);
		ProductSection productSection = ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getProductSectionAtIndex(0);
		
		VMLimits vmLimits = OVFUtils.getUpperAndLowerVMlimits(productSection);
		
		assertEquals(1, vmLimits.getUpperNumberOfVMs());
		assertEquals(1, vmLimits.getLowerNumberOfVMs());
		
		// With lowerBound
		ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppDEMOOvfString);
		productSection = ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getProductSectionAtIndex(0);
		
		vmLimits = OVFUtils.getUpperAndLowerVMlimits(productSection);
		
		assertEquals(2, vmLimits.getUpperNumberOfVMs());
		assertEquals(1, vmLimits.getLowerNumberOfVMs());
	}
	
	@Test
	public void getProductionSectionForOvfIDTest() {
		ProductSection productSection = OVFUtils.getProductionSectionForOvfID(threeTierWebAppOvfString, "XXX");
		assertEquals(null, productSection);
		
		productSection = OVFUtils.getProductionSectionForOvfID(threeTierWebAppOvfString, "haproxy");
		assertEquals(1, productSection.getUpperBound());
		
		productSection = OVFUtils.getProductionSectionForOvfID(threeTierWebAppOvfString, null);
		assertEquals(null, productSection);
	}
	
	@Test
	public void getDiskIdTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppWithUpperLimitsString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		assertEquals("haproxy-img-disk", diskId);
	}
	
	@Test
	public void getDiskIdATCUCTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(atcOVFString);
		String diskId  = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		assertEquals("NA-HAProxy-img-disk", diskId);
	}
	
	@Test
	public void getCapacityTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppWithUpperLimitsString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		int capacity = OVFUtils.getCapacity(ovfDocument, diskId);
		assertEquals(20, capacity);
	}
	
	@Test
	public void getFileIdTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppWithUpperLimitsString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());

		
		String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
		assertEquals("haproxy-img", fileId);
	}
	
	@Test
	public void getFileATCUCTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(atcOVFString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		
		VirtualSystem virtualSystem = ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0);
		String vmName = virtualSystem.getName();
		System.out.println("VM NAME: " + vmName);
		
		String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
		assertEquals("NA-HAProxy-img", fileId);
	}
	
	@Test
	public void getUrlImgTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(threeTierWebAppWithUpperLimitsString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
		String url = OVFUtils.getUrlImg(ovfDocument, fileId);
		
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", url);
	}
	
	@Test
	public void getUrlImgATCTest() {
		OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(atcOVFString);
		String diskId = OVFUtils.getDiskId(ovfDocument.getVirtualSystemCollection().getVirtualSystemAtIndex(0).getVirtualHardwareSection());
		String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
		String url = OVFUtils.getUrlImg(ovfDocument, fileId);
		
		assertEquals("/mnt/cephfs/ascetic/vmic/base-images/test/news-asset/na-haproxy.raw.img", url);
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
