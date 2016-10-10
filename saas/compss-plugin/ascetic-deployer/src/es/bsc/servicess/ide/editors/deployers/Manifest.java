/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package es.bsc.servicess.ide.editors.deployers;

import static es.bsc.servicess.ide.Constants.*;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.xmlbeans.XmlOptions;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionDocument;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionDocument;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionDocument;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemDocument;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import es.bsc.servicess.ide.Constants;
import es.bsc.servicess.ide.ConstraintDef;
import es.bsc.servicess.ide.IDEProperties;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.ProjectMetadataUtils;
import es.bsc.servicess.ide.Titles;
import es.bsc.servicess.ide.editors.BuildingDeploymentFormPage;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.model.Dependency;
import es.bsc.servicess.ide.model.MethodCoreElement;
import es.bsc.servicess.ide.model.ServiceCoreElement;
import es.bsc.servicess.ide.model.ServiceElement;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OperatingSystem;
import eu.ascetic.utils.ovf.api.ProductProperty;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;

public class Manifest {
	private static Logger log = Logger.getLogger(Manifest.class);
	public static final String ASCETIC_PREFIX = "ascetic-pm-";
	public static final String VMIC_SIZE_CONSTRAINT ="asceticVMICImagesize";
	public static final String VMIC_OS_CONSTRAINT = "asceticVMICImageOS";
	public static final String VMIC_ARC_CONSTRAINT ="asceticVMICImaceArchitecture";
	public static final String VMIC_FILE_PREFIX = "asceticVMICFile";
	//private static final String VMIC_EXEC_CONSTRAINT = "asceticVMICExecution";
	public static final String VMIC_MODE_CONSTRAINT = "asceticVmicMode";
	public static final String VMIC_SCRIPT_PROPERTY = "asceticVmicScript";
	public static final String PM_ELEMENTS_CONSTRAINT = "asceticPMElements";
	public static final String PM_INSTALL_DIR_CONSTRAINT ="asceticPMInstallDir";
	public static final String PM_APP_DIR_CONSTRAINT ="asceticPMAppDir";
	public static final String PM_WORKING_DIR_CONSTRAINT ="asceticPMWorkingDir";
	public static final String PM_USER_CONSTRAINT = "asceticPMUser";
	public static final String PM_DEFAULT_METRICS = "asceticPMDefaultMetric";
	
	private static final String DISK_SUFFIX = "-disk";
	private static final String IMAGE_SUFFIX = "-img";
	private static final String ASCETIC_APPMAN_PROP = "asceticAppManagerURL";
	private static final String ASCETIC_APPMON_PROP = "asceticAppMonitorURL";
	private static final String ASCETIC_IMAGE_CACHE_PROP = "asceticCacheImage";
	private static final String POWER_APP_SLA_TERM = "app_power_consumption";
	private static final String PRICE_APP_SLA_TERM = "app_price_for_next_hour";
	private static final String ASCETIC_SLA_INFO_NUMBER = "asceticSlaInfoNumber";
	//private static final int SLA_MAX_TERMS = 2;
	private IJavaProject project;
	private OvfDefinition ovf;
	
	/**
	 * Generate a new service manifest
	 * @param op_prop 
	 * @throws Exception 
	 */
	public void regeneratePackages(ProjectMetadata prMeta, PackageMetadata packMeta, 
			HashMap<String, ServiceElement> allEls, AsceticProperties prop, ApplicationProfile profile) throws Exception {
		String[] oePacks = packMeta.getPackagesWithOrchestration();
		String[] cePacks = packMeta.getPackagesWithCores();
		if (oePacks == null || oePacks.length <= 0) {
			throw new Exception("No orchestration packages defined");
		}
		if (cePacks == null || cePacks.length <= 0) {
			throw new Exception("No core packages defined");
		}
		createNewEmptyOVF(); 
		for (String p : oePacks) {
			addNewComponent(p, prMeta, packMeta, project, allEls, false, prop, profile);
		}
		if (cePacks != null && cePacks.length > 0) {
			for (String p : cePacks) {
				addNewComponent(p, prMeta, packMeta, project, allEls, false, prop, profile);
			}
		}else{
			log.warn("No packages found generating only master");
		}	
		toFile();
	}
	
	
	/**
	 * Set component description in the service manifest
	 * 
	 * @param component Virtual Machine component
	 * @param prMeta Project metadata
	 * @param packName Package name
	 * @param constEls Package elements constraints
	 * @param master flag to indicate if component is a front-end
	 * @throws Exception 
	 */
	public void setComponentDescription(VirtualSystem component,
			ProjectMetadata prMeta, PackageMetadata packMeta, String packName, IJavaProject project,
			HashMap<String, ServiceElement> constEls, boolean master,
			AsceticProperties op_prop, ApplicationProfile profile) throws Exception {
		String[] els;
		if (master) {
			Map<String, ServiceElement> map = CommonFormPage.getElements(
					prMeta.getAllOrchestrationClasses(),ORCH_TYPE, project, prMeta);
			els = map.keySet().toArray(new String[map.size()]);
		} else {
			els = packMeta.getElementsInPackage(packName);
		}
		log.debug("Setting constraints");
		Map<String, Integer> minCoreInstances = ProjectMetadataUtils.getMinElasticity(prMeta, constEls, els);
		Map<String, Integer> maxCoreInstances = ProjectMetadataUtils.getMaxElasticity(prMeta, constEls, els);	
		Map<String, String> maxConstraints = new HashMap<String, String>();
		Map<String, String> maxResourcesPerMachine = prMeta.getMaxResourcesProperties();
		Map<String, Integer> minCoreInstancesPerMachine = BuildingDeploymentFormPage.
				getConstraintsElements(els, constEls, minCoreInstances, maxResourcesPerMachine, 
						maxConstraints);
		setConstraints(component, maxConstraints, prMeta);
		log.debug("Setting signatures in product");
		boolean addElements = true;
		String type = packMeta.getPackageType(packName);
		if (master)
			addElements = false;
		else{
			
			log.debug("Package "+ packName +" has type "+ type);
			if (type.equals(Constants.ORCH_PACK_TYPE))
				addElements = false;
		}
				
		setPMProperties(packName, component, addElements, els, profile);
		/* TODO: Component affinity not supported by ASCETIC year 1
		component.setAffinityConstraints("Low");
		component.setAntiAffinityConstraints("Low");
		*/
		log.debug("Setting Allocation and elasticity rules");
		boolean isMaster = type.equals(Constants.ORCH_PACK_TYPE) || type.equals(Constants.ALL_PACK_TYPE);
		setAllocation(component, els, minCoreInstancesPerMachine,
				minCoreInstances, maxCoreInstances, isMaster);
		/* TODO: Component elasticity not supported by ASCETIC year 1
		setElasticity(manifest, component.getComponentId(), els, minCoreInstancesPerMachine, 
				minCoreInstances, maxCoreInstances, op_prop);
		 */
	}

	private void setPMProperties(String packName, VirtualSystem component, boolean addElements, 
			String[] els, ApplicationProfile profile) {
		ProductSection ps;
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			setAsceticProductSection(component);
		}
		ps = component.getProductSectionAtIndex(0);
		String signatures;
		log.debug("Add elements flags is" + addElements);
		if (!addElements) {
			log.debug("Skipping element signatures generation");
			signatures = "";
		}else{
			signatures = generateElementSignatures(packName, els, profile);
			log.debug("Seting element signatures: "+ signatures);
		}
		ps.setAssociatePublicIp(true);
		ps.addNewProperty(PM_ELEMENTS_CONSTRAINT, ProductPropertyType.STRING, signatures);
		ps.addNewProperty(PM_INSTALL_DIR_CONSTRAINT, ProductPropertyType.STRING, ImageCreation.IMAGE_DEPLOYMENT_FOLDER);
		ps.addNewProperty(PM_APP_DIR_CONSTRAINT, ProductPropertyType.STRING, ImageCreation.IMAGE_DEPLOYMENT_FOLDER);
		ps.addNewProperty(PM_WORKING_DIR_CONSTRAINT, ProductPropertyType.STRING, ImageCreation.IMAGE_WORKING_FOLDER);
		ps.addNewProperty(PM_USER_CONSTRAINT, ProductPropertyType.STRING, ImageCreation.ASCETIC_USER);
		ps.addNewProperty(PM_DEFAULT_METRICS, ProductPropertyType.STRING, profile.getDefaultMetrics(packName));
	}


	public VirtualSystem getComponent(String component) throws Exception {
		VirtualSystemCollection vsc =  ovf.getVirtualSystemCollection();
		if (vsc == null)
			throw new Exception("There are no components in the ovf. ");
		for (VirtualSystem vs : vsc.getVirtualSystemArray()){
			//log.debug("Evaluating virtual system: " +vs.toString());
			if (vs.getName().equals(component)){
				return vs;
			}
		}
		throw new Exception("Component "+ component +" not found");
	}


	/** Generate element Signature
	 * @param constEls Elements with constrains
	 * @param els Name of elements in the package
	 * @param minCoreInstancesPerMachine 
	 * @param pr_meta 
	 * @return Combined signature for all the elements 
	 */
	private static String generateElementSignatures(String component, String[] els, ApplicationProfile profile) {
		String signatures = new String();
		boolean firstElement = true;
		for (String s : els) {
			if (firstElement){
				signatures = signatures.concat(
						s+"@"+profile.getWeight(s)+"@"+profile.getImplementationProfile(component, s));
				firstElement = false;
			}else
				signatures = signatures.concat(
						";"+s+"@"+profile.getWeight(s)+"@"+profile.getImplementationProfile(component, s));
		}
		return signatures;
	}

	/** Set Core Element constraint in the service manifest component
	 * @param component Virtual Machine Component in the service manifest 
	 * @param els Elements in packages
	 * @param constEls Elements description
	 * @param minNumber Minimum number of instances per element
	 * @param maxResourcesPerMachine 
	 * @return Minimum number of instances per element in the generated machine 
	 * @throws ConfigurationException 
	 */
	private void setConstraints(
		VirtualSystem component,Map<String, String> maxConstraints, 
		ProjectMetadata prMeta) throws ConfigurationException {
		
		//Create product section
		ProductSection ps;
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			setAsceticProductSection(component);
		}
		ps = component.getProductSectionAtIndex(0);
		
		//Create hardware section
		VirtualHardwareSection hardwareSection = component.getVirtualHardwareSection();
		if (hardwareSection ==null){
			setHardwareSection(component);
			hardwareSection = component.getVirtualHardwareSection();		
		}
		Map<String, String> defResources = prMeta.getDefaultResourcesProperties();
		
		//Set Disk
		Long ds = getDiskSize(maxConstraints, defResources);
		
		if (ds>0){ 
			if (ds<6000){
				ds = new Long(6000);
			}
			setDiskDescription(ps, hardwareSection, component.getId(), ds);
		}
		
		//Set Ethernet Adaptor
		if (getEthernetAdaptor(hardwareSection) == null){
			addEthernetAdaptor(hardwareSection);
		}
		
		//Setting OS
		String os = getOperatingSystem(maxConstraints, defResources);
		if (os!=null){
			addOperatingSystem(component, os);
			log.debug("Setting OS to " + os);
			ps.addNewProperty(VMIC_OS_CONSTRAINT, ProductPropertyType.STRING, os);
		}
		
		//Setting Processor
		String arch = getArchitecture(maxConstraints, defResources);
		if (arch !=null){
			log.debug("Setting processorArch to " + os);
			ps.addNewProperty(VMIC_ARC_CONSTRAINT, ProductPropertyType.STRING, arch);
		}
		
		Integer cpuc = getCPUCount(maxConstraints, defResources);
		log.debug("Setting CPU count to " + cpuc);
		if (!hardwareSection.setNumberOfVirtualCPUs(cpuc.intValue())){
			addCPUCountItem(hardwareSection, cpuc.intValue());;
		}
		
		Integer cpus = getCPUSpeed(maxConstraints, defResources);
		log.debug("Setting CPU speed to" + cpus);
		if (!hardwareSection.setCPUSpeed(cpus.intValue())){
			addCPUSpeedItem(hardwareSection, cpus.intValue());
		}
		
		//Setting Memory
		Float ms = getMemSize(maxConstraints, defResources);
		log.debug("Setting Memory to " + ms);
		if (!hardwareSection.setMemorySize(ms.intValue())){
			addMemoryItem(hardwareSection, ms.intValue());
		}
		
		
	}


	private void setDiskDescription(ProductSection ps,
			VirtualHardwareSection hardwareSection, String id, Long ds) {
		Disk d = getComponentDisk(id);
		log.debug("Setting Storage to " + ds);
		d.setCapacityAllocationUnits("byte * 2^20");
        d.setCapacity(ds.toString());
		ps.addNewProperty(VMIC_SIZE_CONSTRAINT, ProductPropertyType.REAL32, Long.toString(ds));
		if (getDiskDrive(hardwareSection, d) == null){
			addDiskDrive(hardwareSection, d);
		}
		
	}
	
	private Item getEthernetAdaptor(VirtualHardwareSection hardwareSection){
		Item[] itemArray = hardwareSection.getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (ResourceType.ETHERNET_ADAPTER.equals(itemArray[i].getResourceType())) {
                return itemArray[i];
            }
        }
        return null;
	}
	
	private void addEthernetAdaptor(VirtualHardwareSection hardwareSection){
		Item itemNetwork = Item.Factory.newInstance();
        itemNetwork.setDescription("Virtual Network");
        itemNetwork.addConnection("network");
        itemNetwork.setElementName("Ethernet adapter on network");
        itemNetwork.setInstanceId("3");
        itemNetwork.setResourceType(ResourceType.ETHERNET_ADAPTER);
        itemNetwork.setAutomaticAllocation(true);
        hardwareSection.addItem(itemNetwork);
	}
	
	
	private Item getDiskDrive(VirtualHardwareSection hardwareSection, Disk d){
		String diskName = "ovf:/disk/"+d.getDiskId();
		Item[] itemArray = hardwareSection.getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (ResourceType.DISK_DRIVE.equals(itemArray[i].getResourceType())
                    && diskName.equals(itemArray[i].getHostResourceAtIndex(0))) {
                return itemArray[i];
            }
        }
        return null;
	}
	
	private void addDiskDrive(VirtualHardwareSection hardwareSection, Disk d){
		 Item itemDisk = Item.Factory.newInstance();
	     itemDisk.setDescription("VM Disk for "+ d.getDiskId());
	     itemDisk.setElementName("VM Disk Drive "+ d.getDiskId());
	     itemDisk.setInstanceId("4");
	     itemDisk.setResourceType(ResourceType.DISK_DRIVE);
	     itemDisk.addHostResource("ovf:/disk/"+d.getDiskId());
	     hardwareSection.addItem(itemDisk);
	}


	private void addCPUSpeedItem(VirtualHardwareSection hardwareSection,
			int intValue) {
		 Item itemCpuSpeed = Item.Factory.newInstance();
	     itemCpuSpeed.setDescription("CPU Speed");
	     itemCpuSpeed.setElementName( intValue +" MHz CPU speed reservation");
	     itemCpuSpeed.setInstanceId("1");
	     itemCpuSpeed.setResourceType(ResourceType.PROCESSOR);
	     itemCpuSpeed.setResourceSubType("cpuspeed");
	     itemCpuSpeed.setAllocationUnits("hertz * 2^20");
	     itemCpuSpeed.setReservation(new BigInteger(Integer.toString(intValue)));
	     hardwareSection.addItem(itemCpuSpeed);
		
	}


	private void addCPUCountItem(VirtualHardwareSection hardwareSection,
			int intValue) {
		Item itemCpuNumber = Item.Factory.newInstance();
        itemCpuNumber.setDescription("Number of virtual CPUs");
        itemCpuNumber.setElementName(intValue + " virtual CPUs");
        itemCpuNumber.setInstanceId("1");
        itemCpuNumber.setResourceType(ResourceType.PROCESSOR);
        itemCpuNumber.setVirtualQuantity(new BigInteger(Integer.toString(intValue)));
        hardwareSection.addItem(itemCpuNumber);
		
	}


	private void addMemoryItem(VirtualHardwareSection hardwareSection,
			int intValue) {
		Item itemMemory = Item.Factory.newInstance();
        itemMemory.setDescription("Memory Size");
        itemMemory.setElementName(intValue +" MB of memory");
        itemMemory.setInstanceId("2");
        itemMemory.setResourceType(ResourceType.MEMORY);
        itemMemory.setAllocationUnits("byte * 2^20");
        itemMemory.setVirtualQuantity(new BigInteger(Integer.toString(intValue)));
        hardwareSection.addItem(itemMemory);
	}


	private Disk getComponentDisk(String id) {
		DiskSection ds =  ovf.getDiskSection();
		if (ds != null){
			Disk[] dArray = ds.getDiskArray();
			if (dArray!=null){
				for (Disk d : dArray){
					if (d.getDiskId().equals(id+DISK_SUFFIX)){
						return d;
					}
				}
			}
			addComponentDisk(ds, id);
			
		}else{
			ds = DiskSection.Factory.newInstance();
			ds.setInfo("Disk section for application " + project.getProject().getName());
			addComponentDisk(ds, id);
			ovf.setDiskSection(ds);
		}
		return getComponentDisk(id);
	}


	private void addOperatingSystem(VirtualSystem component, String os) {
		OperatingSystem opSys= component.getOperatingSystem();
		if (opSys == null){
			opSys = OperatingSystem.Factory.newInstance();
		}
		opSys.setId(OperatingSystemType.valueOf(os));
		opSys.setInfo("Operating System for component "+ component.getId());
		component.setOperatingSystem(opSys);
		
	}


	private static Integer getCPUCount(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		String cpuCount = maxConstraints.get(ConstraintDef.PROC_CU.getName());
		Integer cpuc;
		if (cpuCount != null) {
			cpuc = new Integer(cpuCount);
		} else {
			String def = defResources.get(ConstraintDef.PROC_CU);
			if (def!=null){
					cpuc = new Integer(def);
			}else
				cpuc = new Integer(IDEProperties.DEFAULT_NUM_CORES);
		}
		return cpuc;
	}
	
	private static Integer getCPUSpeed(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		String cpuCount = maxConstraints.get(ConstraintDef.PROC_SPEED
				.getName());
		Integer cpuc;
		if (cpuCount != null) {
			cpuc = new Integer(cpuCount);
		} else {
			String def = defResources.get(ConstraintDef.PROC_SPEED);
			if (def!=null){
					cpuc = new Integer(def);
			}else
				cpuc = new Integer(IDEProperties.DEFAULT_PROC_SPEED);
		}
		return cpuc;
	}

	private static Float getMemSize(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		String mem_size = maxConstraints.get(ConstraintDef.MEM_SIZE
				.getName());
		Float ms;
		if (mem_size != null) {
			ms = new Float(Float.parseFloat(mem_size) * 1024);
		} else{
			String def = defResources.get(ConstraintDef.MEM_SIZE);
			if (def!=null){
				ms = new Float(Float.parseFloat(def));
			}else{
				ms = new Float(IDEProperties.DEFAULT_MEM);
			}
		}
		return ms;
	}

	private static Long getDiskSize(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		Long ds;
		String disk_size = maxConstraints.get(ConstraintDef.STORAGE_SIZE
				.getName());
		if (disk_size != null) {
			ds = new Float(Float.parseFloat(disk_size) * 1024).longValue();
	
		} else {
			String def = defResources.get(ConstraintDef.STORAGE_SIZE);
			if (def!=null){
				ds = new Float(Float.parseFloat(def)).longValue();
			}else{
				ds = IDEProperties.DEFAULT_DISK;
			}
		}
		return ds;
	}
	
	private static String getOperatingSystem(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		String os = maxConstraints.get(ConstraintDef.OS_TYPE.getName());
		if (os == null) {
			os = defResources.get(ConstraintDef.OS_TYPE);
		}
		return os;
	}
	
	private static String getArchitecture(Map<String, String> maxConstraints,
			Map<String, String> defResources) {
		String arch = maxConstraints.get(ConstraintDef.PROC_ARCH.getName());
		if (arch == null) {
			arch = defResources.get(ConstraintDef.PROC_ARCH);
		}
		return arch;
	}

	/**
	 * Set the allocation parameters for a component in the service manifest
	 * 
	 * @param component Virtual machine component element of the service machine
	 * @param els Element names which compose the component
	 * @param minCoreInstancesPerMachine Map with the minimum core element instances per machine 
	 * @param minCoreInstances Map with the minimum total core element instances
	 * @param maxCoreInstances Map with the maximum total core element instances
	 * @throws Exception 
	 */
	private void setAllocation(VirtualSystem component, String[] els,
			Map<String, Integer> minCoreInstancesPerMachine,
			Map<String, Integer> minCoreInstances,
			Map<String, Integer> maxCoreInstances, boolean isMaster) throws Exception {
		if (els!=null){
			int[] min_values = new int[els.length];
			int[] max_values = new int[els.length];
			for (int i = 0; i < els.length; i++) {
				if (minCoreInstances.get(els[i])!=null &&  minCoreInstancesPerMachine.get(els[i])!= null){
					min_values[i] = minCoreInstances.get(els[i])
						/ minCoreInstancesPerMachine.get(els[i]);
					max_values[i] = maxCoreInstances.get(els[i])
						/ minCoreInstancesPerMachine.get(els[i]);
				}else
					throw(new Exception("Minimum core instances or core isntances per machine are null for element " +els[i]));
			}
			Arrays.sort(min_values);
			Arrays.sort(max_values);
			ProductSection ps;
			if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
				setAsceticProductSection(component);
			}
			ps = component.getProductSectionAtIndex(0);
			if (!isMaster){
				ps.setLowerBound(
						min_values[min_values.length - 1]);
				ps.setUpperBound(
						max_values[max_values.length - 1]);
			}else{
				ps.setLowerBound(1);
				ps.setUpperBound(1);
			}
		}else
			throw(new Exception("Array of elements is null"));
			
	}

	/* TODO Not currently supported in Ascetic  
	 * Set the elasticity section for a component
	 * 
	 * @param manifest2 service manifest
	 * @param component component name
	 * @param els element names
	 * @param minCoreInstancesPerMachine Map with the minimum core element instances per machine 
	 * @param minCoreInstances Map with the minimum total core element instances
	 * @param maxCoreInstances Map with the maximum total core element instances 
	 
	 * private void setElasticity(String component,
			String[] els, Map<String, Integer> minCoreInstancesPerMachine,
			Map<String, Integer> minCoreInstances, Map<String, Integer> maxCoreInstances,
			AsceticProperties op_prop) {
		
		if (requiresScalability(els, minCoreInstances, maxCoreInstances)) {
			int quota = 1;
			for (String e : els) {
				if (quota < minCoreInstancesPerMachine.get(e).intValue())
					quota = minCoreInstancesPerMachine.get(e).intValue();
			}

			
			ElasticityRule rule = manifest2.getElasticitySection().addNewRule(
					component, "coreCount-"+component+",*,coreTime-"+component+",/,coreVMDeploymentTime-"+component);
			rule.setWindow("P1M");
			rule.setFrequency(1);
			rule.setTolerance((int) (quota * op_prop.getToleranceFactor()));
			if (quota < 1){
				log.warn("quota less than 1");
				quota=1;
			}
			rule.setQuota(quota);
		}
		

	}*/

	/**
	 * Check if a component requires elasticity
	 * 
	 * @param els Element names which compose a component
	 * @param minCoreInstances Map with the minimum core element instances per element
	 * @param maxCoreInstances Map with the maximum core element instances per element
	 * @return True is requires elasticity, otherwise false.
	 */
	private static boolean requiresScalability(String[] els,
			Map<String, Integer> minCoreInstances,
			Map<String, Integer> maxCoreInstances) {

		for (String e : els) {
			if (maxCoreInstances.get(e).intValue() > minCoreInstances.get(e)
					.intValue()) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkPackageHasConstraint(String name,
			String[] elementsInPackage, HashMap<String, ServiceElement> allEls) {
		for (String el:elementsInPackage){
				String cons = allEls.get(el).getConstraints().get(name);
			if (cons !=null){
				return true;
			}
		}
		return false;
	}
	
	/** Generate the package names for the service manifest
	 * @param selectedPackages Selected packages
	 * @return Package names for the service manifest
	 */
	public static String[] generateManifestNames(String[] selectedPackages) {
		if (selectedPackages!= null){
			String[] packs = new String[selectedPackages.length];
			for (int i = 0; i < selectedPackages.length; i++) {
				packs[i] = generateManifestName(selectedPackages[i]);
			}
			return packs;
		}else{
			return new String[0];
		}
	}
	
	/** Generate the package name for the service manifest
	 * @param selectedPackage Selected package
	 * @return Package name for the service manifest
	 */
	public static String generateManifestName(String selectedPackage) {
				return ASCETIC_PREFIX + selectedPackage;
	}
	
	/** Generate the package name for the service manifest
	 * @param selectedPackage Selected package
	 * @return Package name for the service manifest
	 */
	public static String generateManifestImageName(String selectedPackage) {
				return ASCETIC_PREFIX + selectedPackage + IMAGE_SUFFIX;
	}
	
	/** 
	 * Get package names from the service manifest
	 * 
	 * @param componentIdArray
	 * @return Array of package names
	 */
	public static String[] getPackageNames(String[] componentIdArray) {
		String[] packs = new String[componentIdArray.length];
		for (int i = 0; i < componentIdArray.length; i++) {
			packs[i] = componentIdArray[i].substring(componentIdArray[i]
					.indexOf(ASCETIC_PREFIX) + ASCETIC_PREFIX.length());
		}
		return packs;
	}
	/**** TODO Not implemented for Y1******/
	
	public int getNumberAffinityRules() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberAntiAffinityRules() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String[] getAffinityRuleScope(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAffinityRuleLevel(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getAntiAffinityRuleScope(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAntiAffinityRuleLevel(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAffinityLevel(String component) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAntiAffinityLevel(String component) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addAffinityRule(String[] generatedManifestNames, String trim) {
		// TODO Auto-generated method stub
	}

	public void setAntiAffinityRule(int selectionIndex,
			String[] generatedManifestNames, String trim) {
		// TODO Auto-generated method stub
		
	}

	public void setComponentAffinity(String component, String trim) {
		// TODO Auto-generated method stub
		
	}

	public void addAntiAffinityRule(String[] generateManifestNames, String trim) {
		// TODO Auto-generated method stub
		
	}

	public void setComponentAntiAffinity(String component, String trim) {
		// TODO Auto-generated method stub
		
	}
	
	/***********/

	public void setServiceId(String serviceID) {
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc == null){
			setVirtualSystemCollection();
			vsc = ovf.getVirtualSystemCollection();
		}
		vsc.setId(serviceID);
		
	}

	public static Manifest newInstance(IJavaProject project) {
		Manifest m = new Manifest();
		m.project = project;
		m.createNewEmptyOVF();
		return m;
	}

	public static Manifest newInstance(IJavaProject project, StringBuffer manifestData) {
		Manifest m = new Manifest();
		m.project = project;
		m.ovf = OvfDefinition.Factory.newInstance(manifestData.toString());
		m.setServiceId(project.getProject().getName());
		return m;
	}
	
	public static Manifest newInstance(IJavaProject project, ProjectMetadata pr_meta, PackageMetadata packMeta, 
			HashMap<String, ServiceElement> allEls, AsceticProperties prop, ApplicationProfile profile) throws Exception{
		Manifest m = new Manifest();
		m.project = project;
		m.regeneratePackages(pr_meta, packMeta, allEls, prop, profile);
		m.setServiceId(project.getProject().getName());
		return m;
	}

	public String getString() {
		return ovf.getXmlObject().toString();
	}
	
	public void toFile() throws CoreException{
		IFile sm = project.getProject().getFolder(OUTPUT_FOLDER).getFolder(PACKAGES_FOLDER)
					.getFile(AsceticProperties.SERVICE_MANIFEST);
		if (sm.exists()) {
							sm.delete(true, null);
						}
						log.debug("writing the manifest in the file ");
						sm.create(new ByteArrayInputStream(getString()
								.getBytes()), true, null);
	}

	public String toString() {
		return ovf.toString();
	}
	
	public OvfDefinition getOVFDefinition() {
		return ovf;
	}
	
	public void updateOVFDefinition(OvfDefinition ovf) {
		this.ovf = ovf;
	}

	public boolean hasImages() throws Exception {
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc !=null){
			VirtualSystem[] vsArray = vsc.getVirtualSystemArray();
			if (vsArray !=null && vsArray.length>0){
				for (VirtualSystem vs : vsArray){
					if (!fileExists(vs.getId()+IMAGE_SUFFIX)){
						return false;
					}
				}
				return true;
			}else{
				throw(new Exception("There are no components defined"));
			}
		}else{
			throw(new Exception("There are no components defined"));
		}
		
	}


	public boolean fileExists(String id) {
		References refs = ovf.getReferences();
		if (refs == null){
			return false;
		}
		File[] fileArray = refs.getFileArray();
		if (fileArray == null|| fileArray.length<= 0){
			return false;
		}
		for (File f : fileArray){
			if (f.getId().equals(id))
				return true;
		}
		return false;
	}


	public void addFile(String name, String href, String format){
		File f = File.Factory.newInstance(name, href);
		if (format!= null)
			f.setCompression(format);
		References refs = ovf.getReferences();
		if (refs == null){
			log.debug("References are null. Adding new");
			ovf.getXmlObject().getEnvelope().addNewReferences();
			refs = ovf.getReferences();
		}
		refs.addFile(f);
	}
	
	public InstallationScript getVMICExecutionForComponent(String componentID) throws Exception{
		VirtualSystem component = getComponent(componentID);
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			log.debug("No VMICScript because no section array");
			return null;
		}
		ProductSection ps = component.getProductSectionAtIndex(0);
		if (ps.getPropertyByKey(VMIC_SCRIPT_PROPERTY)!= null){
			String script =ps.getVmicScript();
			if (script!=null && !script.isEmpty()){
				InstallationScript is = new InstallationScript();
				is.setScript(script);
				return is;
			}else{
				log.debug("No VMICScript because no property defined or empty");
				return null;
			}
		}else{
			log.debug("No VMICScript because no product section");
			return null;
		}
	}
	
	public void cleanScripts(){
		VirtualSystemCollection vsc =  ovf.getVirtualSystemCollection();
		if (vsc != null)
			for (VirtualSystem vs : vsc.getVirtualSystemArray()){
				if (vs.getProductSectionArray() != null && vs.getProductSectionArray().length>0){
					ProductSection ps = vs.getProductSectionAtIndex(0);
					if (ps.getPropertyByKey(VMIC_SCRIPT_PROPERTY)!= null){
						ps.removePropertyByKey(VMIC_SCRIPT_PROPERTY);
						log.debug("Removing scripts");
					}
				}
		}
	}
	
	public void addVMICExecutionInComponent(String componentID, String commands) throws Exception{
		VirtualSystem component = getComponent(componentID);
		ProductSection ps;
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			setAsceticProductSection(component);
		}
		ps = component.getProductSectionAtIndex(0);
		ps.setVmicScript(commands);
		
	}
	
	public void addVMICFileInComponent(String componentID, String name) throws Exception{
		VirtualSystem component = getComponent(componentID);
		ProductSection ps;
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			setAsceticProductSection(component);
		}
		ps = component.getProductSectionAtIndex(0);
		ps.addNewProperty(VMIC_FILE_PREFIX , ProductPropertyType.STRING, name);
		
	}
	
	private void createNewEmptyOVF(){
		ovf = OvfDefinition.Factory.newInstance();
		log.debug("Created ovf:"+ ovf.toString());
		setVirtualSystemCollection();
		ovf.setReferences(References.Factory.newInstance());
		setServiceId(project.getProject().getName());
	}


	private void addComponent(VirtualSystem component){
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc == null){
			setVirtualSystemCollection();
			vsc = ovf.getVirtualSystemCollection();
		}
		vsc.addVirtualSystem(component);
	}
	
	private void addNewComponent(String p, ProjectMetadata prMeta, PackageMetadata packMeta, IJavaProject project,
			HashMap<String, ServiceElement> allEls, boolean master,
			AsceticProperties ascProp, ApplicationProfile profile) throws Exception{
		
		String componentID = Manifest.generateManifestName(p);
		try{
			VirtualSystem component = this.getComponent(componentID);
			log.debug("Component "+ componentID + " already exists");
		}catch(Exception e){
			log.debug("Creating Component for package " + p );
			VirtualSystem component = VirtualSystem.Factory.newInstance();
			component.setId(componentID);
			component.setName(componentID);
			component.setInfo("Description of component "+ componentID);
			setComponentDescription(component, prMeta, packMeta, p, project, 
					allEls, master, ascProp, profile);
			addComponent(component);
		}
		
	}

	private static void addComponentDisk(DiskSection ds, String id) {
		Disk d = Disk.Factory.newInstance();
		d.setDiskId(id+ DISK_SUFFIX);
		d.setFileRef(id +IMAGE_SUFFIX);
		d.setFormat(DiskFormatType.QCOW2);
		ds.addDisk(d);
	}

	public String getServiceId() {
		return ovf.getVirtualSystemCollection().getId();
		
	}
	
	private void setAsceticProductSection(VirtualSystem component){
		log.debug("Creating new Product Section for component " + component.getId());
		ProductSection ps = ProductSection.Factory.newInstance();
		ps.setInfo("Ascetic Extensions for component " + component.getId());
		component.addProductSection(ps);
	}
	
	private void setAsceticGlobalProductSection(VirtualSystemCollection vsc){
		log.debug("Creating new Application Product Section");
		ProductSection ps = ProductSection.Factory.newInstance();
		ps.setInfo("Ascetic Application Wide Extensions");
		vsc.addProductSection(ps);
	}
	
	private void setVirtualSystemCollection(){
		log.debug("Creating new Virtual System Collection for "+ project.getProject().getName() + " application.");
		VirtualSystemCollection v = VirtualSystemCollection.Factory.newInstance();
		v.setInfo("Virtual Systems for "+ project.getProject().getName() + " application");
		ovf.setVirtualSystemCollection(v);
	}
	
	private void setHardwareSection(VirtualSystem component){
		log.debug("Creating new Virtual Hardware Section for component " + component.getId());
		VirtualHardwareSection hardwareSection = VirtualHardwareSection.Factory.newInstance();
		hardwareSection.setInfo("Hardware Description for component " + component.getId());
		component.setVirtualHardwareSection(hardwareSection);
	}
	
	public String getVMICFileName(String fileName){
		return VMIC_FILE_PREFIX+"-"+getServiceId()+"-"+fileName;
	}


	public void cleanFiles() {
		References refs = ovf.getReferences();
		if (refs == null){
			log.debug("Adding empty references");
			ovf.getXmlObject().getEnvelope().addNewReferences();
		}else{
			refs.setFileArray(new File[0]);
		}
	}

	public void setVMICMode(String mode) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setVmicMode(mode);
	}


	public void setApplicationSecurity(String privateKey, String publicKey) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setPrivateSshKey(privateKey);
		ps.setPublicSshKey(publicKey);
	}
	
	public void removeApplicationSecurity(){
		ProductSection ps = getOrCreateGlobalProductSection();
		if (ps.getPropertyByKey("asceticSecurityKey")!=null){
			ps.removePropertyByKey("asceticSecurityKey");
		}
		if (ps.getPropertyByKey("asceticSshPrivateKey")!=null){
			ps.removePropertyByKey("asceticSshPrivateKey");
		}
		if (ps.getPropertyByKey("asceticSshPublicKey")!=null){
			ps.removePropertyByKey("asceticSshPublicKey");
		}
	}
	
	private ProductSection getOrCreateGlobalProductSection(){
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc == null){
			setVirtualSystemCollection();
			vsc = ovf.getVirtualSystemCollection();
		}
		ProductSection ps;
		if (vsc.getProductSectionArray() == null || vsc.getProductSectionArray().length<1){
			setAsceticGlobalProductSection(vsc);
		}
		return vsc.getProductSectionAtIndex(0);
		
	}
	
	public void setApplicationMangerEPR(String appManURI){
		ProductSection ps = getOrCreateGlobalProductSection();
		ProductProperty pp = ps.getPropertyByKey(ASCETIC_APPMAN_PROP);
		if (pp != null){
			pp.setValue(appManURI);
		}else
			ps.addNewProperty(ASCETIC_APPMAN_PROP, ProductPropertyType.STRING, appManURI);
	}


	public void setApplicationMonitorEPR(String monLoc) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ProductProperty pp = ps.getPropertyByKey(ASCETIC_APPMON_PROP);
		if (pp != null){
			pp.setValue(monLoc);
		}else
			ps.addNewProperty(ASCETIC_APPMON_PROP, ProductPropertyType.STRING, monLoc);
		
	}


	public int getVMsToDeploy(boolean max) {
		int vms = 0;
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc == null){
			return 0;
		}
		VirtualSystem[] vsa = vsc.getVirtualSystemArray();
		for (VirtualSystem vs :vsa){
			ProductSection ps = vs.getProductSectionAtIndex(0);
			if (ps == null)
				vms ++;
			else
				if (max){
					vms = vms + ps.getUpperBound();
				}
				vms = vms + ps.getLowerBound();
		}
		return vms;
	}
	
	public void setImageCaching(boolean enabled){
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc!=null && vsc.getVirtualSystemArray()!=null){
			for (VirtualSystem component :vsc.getVirtualSystemArray()){
				ProductSection ps;
				if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
					setAsceticProductSection(component);
				}
				ps = component.getProductSectionAtIndex(0);
				ProductProperty prop = ps.getPropertyByKey(ASCETIC_IMAGE_CACHE_PROP);
				if (prop != null){
					if (enabled)
						prop.setValue("1");
					else
						prop.setValue("0");
				}else{
					if (enabled)
						ps.addNewProperty(ASCETIC_IMAGE_CACHE_PROP, ProductPropertyType.UINT32, "1");	
				}
			}
		}

	}

	public void deleteSlaTerms(){
		ProductSection ps = getOrCreateGlobalProductSection();
		int sla_numbers = getSLATermsNumber(ps);
		for (int index=sla_numbers-1; index >= 0; index--){
			log.debug("Removing SLA term "+index);
			ps.removeSlaInfo(index);
		}
	}
	
	public void setPowerBoundary(String power) {
		ProductSection ps = getOrCreateGlobalProductSection();
		log.debug("Adding Power SLA term ");
		ps.addSlaInfo(POWER_APP_SLA_TERM, "Watt", "LTE", power, "violation");
		ps.setPowerRequirement(power);
		
	}
	
	public void setPriceBoundary(String price) {
		ProductSection ps = getOrCreateGlobalProductSection();
		log.debug("Adding Price SLA term ");
		ps.addSlaInfo(PRICE_APP_SLA_TERM, "EUR", "LTE", price, "violation");
		ps.setPriceRequirement(price);
	}
	private int getSLATermsNumber(ProductSection ps) {
        ProductProperty productProperty = ps.getPropertyByKey(ASCETIC_SLA_INFO_NUMBER);
        if (productProperty == null) {
            return 0;
        } else {
            return ((Integer) productProperty.getValueAsJavaObject());
        }
        
    }

	
	public void setAppDuration(String duration) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setPerformanceOptimizationBoundary(duration);
		
	}
	
	public void setAppEnergy(String energy) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setEnergyOptimizationBoundary(energy);
		
	}
	
	public void setAppCost(String cost) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setCostOptimizationBoundary(cost);
		
	}
	
	public void setOptimizationParameter(String param) {
		ProductSection ps = getOrCreateGlobalProductSection();
		ps.setOptimizationParameter(param);
		
	}


	public Integer getMaxVMs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
