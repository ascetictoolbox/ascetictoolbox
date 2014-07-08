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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
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
import eu.ascetic.utils.ovf.api.ProductPropertyType;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;

public class Manifest {
	private static Logger log = Logger.getLogger(Manifest.class);
	public static final String ASCETIC_PREFIX = "ascetic-pm-";
	private IJavaProject project;
	private OvfDefinition ovf;
	public static final String VMIC_FILE = "VMIC_File";
	
	/**
	 * Generate a new service manifest
	 * @param op_prop 
	 * @throws Exception 
	 */
	protected void generateNewPackages(ProjectMetadata pr_meta, PackageMetadata packMeta, 
			HashMap<String, ServiceElement> allEls, AsceticProperties prop) throws Exception {
		
		
		String[] oePacks = packMeta.getPackagesWithOrchestration();
		if (oePacks == null || oePacks.length <= 0) {
			throw new Exception("No orchestration packages defined");
		}
			
		ovf = null; 
		for (String p : oePacks) {
			log.debug("Creating Component for package " + p );
			String componentID = Manifest.generateManifestName(p);
			if (ovf == null){
				OvfDefinition.Factory.newInstance(project.getProject().getName(), componentID);
			}else{
				VirtualSystem component = VirtualSystem.Factory.newInstance();
				component.setId(componentID);
				setComponentDescription(component, pr_meta, packMeta, p, project, 
					 allEls, false, prop);
				ovf.getVirtualSystemCollection().addVirtualSystem(component);
			}
		}
		String[] cePacks = packMeta.getPackagesWithCores();
		if (cePacks != null && cePacks.length > 0) {
			for (String p : cePacks) {
				log.debug("Creating Component for package " + p );
				String componentID = Manifest.generateManifestName(p);
				VirtualSystem component = VirtualSystem.Factory.newInstance();
				component.setId(componentID);
				setComponentDescription(component, pr_meta, packMeta, p, project, 
					allEls, false, prop);
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
		//TODO Change to new ascetic manifest
		String signatures;
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
		ProductSection product = component.getProductSectionAtIndex(0);
		if (master) {
			signatures = "master-frontend";
		}else
			signatures = generateElementSignatures(constEls, els, prMeta, minCoreInstancesPerMachine);
		product.addNewProperty("PM_Elements", ProductPropertyType.STRING, signatures);
		/* TODO: Default intra-components affinity
		component.setAffinityConstraints("Low");
		component.setAntiAffinityConstraints("Low");
		*/
		log.debug("Setting Allocation and elasticity rules");
		setAllocation(component, els, minCoreInstancesPerMachine,
				minCoreInstances, maxCoreInstances);
		/*
		setElasticity(manifest, component.getComponentId(), els, minCoreInstancesPerMachine, 
				minCoreInstances, maxCoreInstances, op_prop);
		 */
	}

	private VirtualSystem getComponent(String component) throws Exception {
		VirtualSystemCollection vsc =  ovf.getVirtualSystemCollection();
		for (VirtualSystem vs : vsc.getVirtualSystemArray()){
			if (vs.getId().equals(component)){
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
		
		Map<String, String> defResources = prMeta.getDefaultResourcesProperties();
		Long ds = getDiskSize(maxConstraints, defResources);
		log.debug("Setting Storage to " + ds);
		//TODO Add storage OS, architecture
		VirtualHardwareSection hardwareSection = component.getVirtualHardwareSection();
		Float ms = getMemSize(maxConstraints, defResources);
		log.debug("Setting Memory to " + ms);
		hardwareSection.setMemorySize(ms.intValue());
		
		Integer cpuc = getCPUCount(maxConstraints, defResources);
		log.debug("Setting CPU count to " + cpuc);
		hardwareSection.setNumberOfVirtualCPUs(cpuc.intValue());
		
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
			component.getProductSectionAtIndex(0).setLowerBound(
				min_values[min_values.length - 1]);
			component.getProductSectionAtIndex(0).setUpperBound(
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
		ovf.getVirtualSystemCollection().setId(serviceID);
		
	}

	public static Manifest newInstance(IJavaProject project, String componentID) {
		Manifest m = new Manifest();
		m.project = project;
		m.ovf = OvfDefinition.Factory.newInstance(project.getProject().getName(), componentID);
		return m;
	}

	public static Manifest newInstance(IJavaProject project, StringBuffer manifestData) {
		Manifest m = new Manifest();
		m.project = project;
		m.ovf = OvfDefinition.Factory.newInstance(manifestData.toString());
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


	public OvfDefinition getOVFDefinition() {
		return ovf;
	}
	
	public void updateOVFDefinition(OvfDefinition ovf) {
		this.ovf = ovf;
	}

	public boolean hasImages() {
		if (ovf.getReferences().getFileArray().length>0){
			return true;
		}else
			return false;
	}

	public void addFiles(String name, String href, String format){
		File f = File.Factory.newInstance(name, href);
		if (format!= null)
			f.setCompression(format);
		ovf.getReferences().addFile(f);
	}
	
	public void addVMICFileInComponent(String componentID, String name) throws Exception{
		VirtualSystem vs = getComponent(componentID);
		vs.getProductSectionAtIndex(0).addNewProperty(VMIC_FILE , ProductPropertyType.STRING, name);
		
		
	}
}
