/*
 *  Copyright 2011-2013 Barcelona Supercomputing Center (www.bsc.es)
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
import es.bsc.servicess.ide.ConstraintDef;
import es.bsc.servicess.ide.IDEProperties;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.BuildingDeploymentFormPage;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.model.Dependency;
import es.bsc.servicess.ide.model.MethodCoreElement;
import es.bsc.servicess.ide.model.ServiceCoreElement;
import es.bsc.servicess.ide.model.ServiceElement;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OperatingSystem;
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
	public static final String VMIC_EXEC_CONSTRAINT = "asceticVMICExecution";
	public static final String VMIC_MODE_CONSTRAINT = "asceticVmicMode";
	public static final String PM_ELEMENTS_CONSTRAINT = "asceticPMElements";
	
	private static final String DISK_SUFFIX = "-disk";
	private static final String IMAGE_SUFFIX = "-img";
	private IJavaProject project;
	private OvfDefinition ovf;
	
	
	/**
	 * Generate a new service manifest
	 * @param op_prop 
	 * @throws Exception 
	 */
	public void regeneratePackages(ProjectMetadata prMeta, PackageMetadata packMeta, 
			HashMap<String, ServiceElement> allEls, AsceticProperties prop) throws Exception {
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
			addNewComponent(p, prMeta, packMeta, project, allEls, false, prop);
		}
		if (cePacks != null && cePacks.length > 0) {
			for (String p : cePacks) {
				addNewComponent(p, prMeta, packMeta, project, allEls, false, prop);
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
	 * @param p Package name
	 * @param constEls Package elements constraints
	 * @param master flag to indicate if component is a front-end
	 * @throws Exception 
	 */
	public void setComponentDescription(VirtualSystem component,
			ProjectMetadata prMeta, PackageMetadata packMeta, String p, IJavaProject project,
			HashMap<String, ServiceElement> constEls, boolean master,
			AsceticProperties op_prop) throws Exception {
		String[] els;
		if (master) {
			Map<String, ServiceElement> map = CommonFormPage.getElements(
					prMeta.getAllOrchestrationClasses(),ORCH_TYPE, project, prMeta);
			els = map.keySet().toArray(new String[map.size()]);
		} else {
			els = packMeta.getElementsInPackage(p);
		}
		log.debug("Setting constraints");
		Map<String, Integer> minCoreInstances = prMeta.getMinElasticity(els);
		Map<String, Integer> maxCoreInstances = prMeta.getMaxElasticity(els);	
		Map<String, String> maxConstraints = new HashMap<String, String>();
		Map<String, String> maxResourcesPerMachine = prMeta.getMaxResourcesProperties();
		Map<String, Integer> minCoreInstancesPerMachine = BuildingDeploymentFormPage.
				getConstraintsElements(els, constEls, minCoreInstances, maxResourcesPerMachine, 
						maxConstraints);
		setConstraints(component, maxConstraints, prMeta);
		log.debug("Setting signatures in product");
		setInstalledElements(component, master, constEls, els, prMeta, minCoreInstancesPerMachine);
		/* TODO: Component affinity not supported by ASCETIC year 1
		component.setAffinityConstraints("Low");
		component.setAntiAffinityConstraints("Low");
		*/
		log.debug("Setting Allocation and elasticity rules");
		setAllocation(component, els, minCoreInstancesPerMachine,
				minCoreInstances, maxCoreInstances);
		/* TODO: Component elasticity not supported by ASCETIC year 1
		setElasticity(manifest, component.getComponentId(), els, minCoreInstancesPerMachine, 
				minCoreInstances, maxCoreInstances, op_prop);
		 */
	}

	private void setInstalledElements(VirtualSystem component, boolean master,
			HashMap<String, ServiceElement> constEls, String[] els,
			ProjectMetadata prMeta,
			Map<String, Integer> minCoreInstancesPerMachine) {
		ProductSection ps;
		if (component.getProductSectionArray() == null || component.getProductSectionArray().length<1){
			setAsceticProductSection(component);
		}
		ps = component.getProductSectionAtIndex(0);
		String signatures;
		if (master) {
			signatures = "master-frontend";
		}else
			signatures = generateElementSignatures(constEls, els, prMeta, minCoreInstancesPerMachine);
		ps.addNewProperty(PM_ELEMENTS_CONSTRAINT, ProductPropertyType.STRING, signatures);
		
	}


	private VirtualSystem getComponent(String component) throws Exception {
		VirtualSystemCollection vsc =  ovf.getVirtualSystemCollection();
		if (vsc == null)
			throw new Exception("There are no components in the ovf. ");
		for (VirtualSystem vs : vsc.getVirtualSystemArray()){
			log.debug("Evaluating virtual system: " +vs.toString());
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
	private static String generateElementSignatures(
			HashMap<String, ServiceElement> constEls, String[] els, 
			ProjectMetadata prMeta, Map<String, Integer> minCoreInstancesPerMachine) {
		//TODO: Modify for service methods
		String signatures = new String();
		String method_sigs = new String();
		boolean firstService = true;
		boolean firstMethod = true;
		for (String s : els) {
			ServiceElement  el = constEls.get(s);
			if (el != null){
				if (el instanceof ServiceCoreElement){
					String sig = generateServiceElementSignature(s,(ServiceCoreElement)el,prMeta);
					if (firstService) {				
						signatures = signatures.concat(sig);
						firstService = false;
					} else
						signatures = signatures.concat("," + s);
				}else if (el instanceof MethodCoreElement){
					if (firstMethod) {
						method_sigs = method_sigs.concat(s);
						firstMethod = false;
					}else
						method_sigs = method_sigs.concat(";"+s);
				}
			}else
				log.warn("Element "+s+" not found in the elements descriptions");
		}
		if (!firstMethod){
			
			if (firstService)
				signatures = signatures.concat(generateMethodElementsSignatures(method_sigs, minCoreInstancesPerMachine));
			else
				signatures = signatures.concat(","+generateMethodElementsSignatures(method_sigs, minCoreInstancesPerMachine));
		}
		return signatures;
	}

	private static String generateMethodElementsSignatures(String method_sigs, Map<String, Integer> minCoreInstancesPerMachine) {
		Integer[] min_values = minCoreInstancesPerMachine.values().toArray(new Integer[minCoreInstancesPerMachine.size()]);
		Arrays.sort(min_values);	
		return new String("[|"+method_sigs+"|"+min_values[min_values.length - 1]+"]");
	}

	private static String generateServiceElementSignature(String s,
			ServiceCoreElement el, ProjectMetadata prMeta) {
		int minElasticity = prMeta.getMinElasticity(s);
		String path = new String();
		List<Dependency> deps = prMeta.getDependencies(new String[]{s});
		for (Dependency d:deps){
			if(d.getType().equals(WAR_DEP_TYPE)){
				path = d.getOtherInfo();
			}
		}
		return new String("["+path+"|"+s+"|"+minElasticity+"]");
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
		String cpuCount = maxConstraints.get(ConstraintDef.PROC_CPU_COUNT
				.getName());
		Integer cpuc;
		if (cpuCount != null) {
			cpuc = new Integer(cpuCount);
		} else {
			String def = defResources.get(ConstraintDef.PROC_CPU_COUNT);
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
		String os = maxConstraints.get(ConstraintDef.OS.getName());
		if (os == null) {
			os = defResources.get(ConstraintDef.OS);
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
			Map<String, Integer> maxCoreInstances) throws Exception {
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
			ps.setLowerBound(
				min_values[min_values.length - 1]);
			ps.setUpperBound(
				max_values[max_values.length - 1]);
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
			HashMap<String, ServiceElement> allEls, AsceticProperties prop) throws Exception{
		Manifest m = new Manifest();
		m.project = project;
		m.regeneratePackages(pr_meta, packMeta, allEls, prop);
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
			AsceticProperties ascProp) throws Exception{
		log.debug("Creating Component for package " + p );
		String componentID = Manifest.generateManifestName(p);
		VirtualSystem component = VirtualSystem.Factory.newInstance();
		component.setId(componentID);
		component.setName(componentID);
		component.setInfo("Description of component "+ componentID);
		setComponentDescription(component, prMeta, packMeta, p, project, 
			allEls, false, ascProp);
		addComponent(component);
	}

	private static void addComponentDisk(DiskSection ds, String id) {
		Disk d = Disk.Factory.newInstance();
		d.setDiskId(id+ DISK_SUFFIX);
		d.setFileRef(id +IMAGE_SUFFIX);
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

	/* TODO Remove when generate images work
	public void generateFakeImages() {
		for (VirtualSystem vs : ovf.getVirtualSystemCollection().getVirtualSystemArray()){
			
			addFile(vs.getId()+IMAGE_SUFFIX, "/fake/image/"+vs.getId()+".qcow2","qcow2");
		}
		
	}
	*/

	public void setVMICMode(String mode) {
		VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
		if (vsc == null){
			setVirtualSystemCollection();
			vsc = ovf.getVirtualSystemCollection();
		}
		ProductSection ps;
		if (vsc.getProductSectionArray() == null || vsc.getProductSectionArray().length<1){
			setAsceticGlobalProductSection(vsc);
		}
		ps = vsc.getProductSectionAtIndex(0);
		ps.setVmicMode(mode);
	}
	
	
}
