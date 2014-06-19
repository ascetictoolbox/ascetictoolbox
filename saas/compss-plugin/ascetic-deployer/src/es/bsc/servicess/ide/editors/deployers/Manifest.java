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

import static es.bsc.servicess.ide.Constants.ORCH_TYPE;
import static es.bsc.servicess.ide.Constants.WAR_DEP_TYPE;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.jdt.core.IJavaProject;
import org.xml.sax.SAXException;

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

public class Manifest {
	private static Logger log = Logger.getLogger(Manifest.class);
	public static final String ASCETIC_PREFIX = "ascetic-pm-";
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
	public void setComponentDescription(String component,
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
		/* TODO: Change to ascetic
		 * ProductSection product = component.getOVFDefinition()
				.getVirtualSystem().getProductSection();
		if (master) {
			signatures = "master-frontend";
		}else
			signatures = generateElementSignatures(constEls, els, prMeta, minCoreInstancesPerMachine);
		product.setProduct(signatures);
		*/
		/* TODO: Default intra-components affinity
		component.setAffinityConstraints("Low");
		component.setAntiAffinityConstraints("Low");
		*/
		log.debug("Setting Allocation and elasticity rules");
		/* TODO: Change to ascetic
		setAllocation(component, els, minCoreInstancesPerMachine,
				minCoreInstances, maxCoreInstances);
		
		setElasticity(manifest, component.getComponentId(), els, minCoreInstancesPerMachine, 
				minCoreInstances, maxCoreInstances, op_prop);
		 */
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
		String component,Map<String, String> maxConstraints, 
		ProjectMetadata prMeta) throws ConfigurationException {
		/* TODO Change to Ascetic manifest
		Map<String, String> defResources = prMeta.getDefaultResourcesProperties();
		Long ds = getDiskSize(maxConstraints, defResources);
		log.debug("Setting Storage to " + ds);
		component.getOVFDefinition().getDiskSection().getImageDisk()
				.setCapacity(ds.toString());
		
		VirtualHardwareSection hardwareSection = component.getOVFDefinition()
				.getVirtualSystem().getVirtualHardwareSection();
		Float ms = getMemSize(maxConstraints, defResources);
		log.debug("Setting Memory to " + ms);
		hardwareSection.setMemorySize(ms.intValue());
		
		Integer cpuc = getCPUCount(maxConstraints, defResources);
		log.debug("Setting CPU count to " + cpuc);
		hardwareSection.setNumberOfVirtualCPUs(cpuc.intValue());
		*/
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
	private void setAllocation(String component, String[] els,
			Map<String, Integer> minCoreInstancesPerMachine,
			Map<String, Integer> minCoreInstances,
			Map<String, Integer> maxCoreInstances) throws Exception {
		/*TODO Change to Ascetic component
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
			component.getAllocationConstraints().setLowerBound(
				min_values[min_values.length - 1]);
			component.getAllocationConstraints().setInitial(
				min_values[min_values.length - 1]);
			component.getAllocationConstraints().setUpperBound(
				max_values[max_values.length - 1]);
		}else
			throw(new Exception("Array of elements is null"));
			*/
	}

	/**
	 * Set the elasticity section for a component
	 * 
	 * @param manifest2 service manifest
	 * @param component component name
	 * @param els element names
	 * @param minCoreInstancesPerMachine Map with the minimum core element instances per machine 
	 * @param minCoreInstances Map with the minimum total core element instances
	 * @param maxCoreInstances Map with the maximum total core element instances 
	 */
	private void setElasticity(String component,
			String[] els, Map<String, Integer> minCoreInstancesPerMachine,
			Map<String, Integer> minCoreInstances, Map<String, Integer> maxCoreInstances,
			AsceticProperties op_prop) {
		/* TODO Change to ascetic manifest  
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
		*/

	}

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

	/** Set the image URL in the manifest 
	 * @param map Component imageURL map
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws AsceticDeploymentException
	 */
	public  void setImages(Map<String, String> map)
			throws SAXException, IOException, ParserConfigurationException,
			AsceticDeploymentException {
		for (Entry<String, String> es : map.entrySet()) {
			/* TODO Change to ascetic manifest
			VirtualMachineComponent component = manifest
					.getVirtualMachineDescriptionSection()
					.getVirtualMachineComponentById(OPTIMIS_PREFIX + es.getKey());
			if (component != null)
				component.getOVFDefinition().getReferences().getImageFile()
						.setHref(es.getValue());
			else
				throw (new AsceticDeploymentException("Component "
						+ OPTIMIS_PREFIX + es.getKey() + " not found in the manifest"));
			*/
		}
	}

	

	private static String[] getCapacityValues(
			HashMap<String, ServiceElement> allEls, ProjectMetadata prMeta) {
		String[] vals = new String[2];
		String[] els = allEls.keySet().toArray(new String[allEls.size()]);
		Map<String, Integer> minCoreInstances = prMeta.getMinElasticity(els);	
		Map<String, String> maxConstraints = new HashMap<String, String>();
		Map<String, String> maxResourcesPerMachine = prMeta.getMaxResourcesProperties();
		Map<String, Integer> minCoreInstancesPerMachine = BuildingDeploymentFormPage.
				getConstraintsElements(els, allEls, minCoreInstances, maxResourcesPerMachine, 
						maxConstraints);
		vals[0] = maxConstraints.get(ConstraintDef.ENC_STORAGE);
		vals[1] = maxConstraints.get(ConstraintDef.SHARED_STORAGE);
		return vals;
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
		// TODO Auto-generated method stub
		
	}

	public static Manifest newInstance(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Manifest newInstance(StringBuffer manifestData) {
		// TODO Auto-generated method stub
		return null;
	}

}
