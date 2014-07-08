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

import integratedtoolkit.types.project.ProjectFile;
import integratedtoolkit.types.resources.ResourcesFile;
import integratedtoolkit.util.RuntimeConfigManager;

import static es.bsc.servicess.ide.Constants.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

import es.bsc.servicess.ide.ConstraintDef;
import es.bsc.servicess.ide.IDEProperties;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.PackagingUtils;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.BuildingDeploymentFormPage;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.model.Dependency;
import es.bsc.servicess.ide.model.ServiceElement;

public class ImageCreation {

	private static Logger log = Logger.getLogger(ImageCreation.class);
	private final static String IMAGE_DEPLOYMENT_FOLDER = "/optimis_service/";

	private final static String CONTEXT_FOLDER = "/mnt/context";
	private static final String OPTIMIS_USER = "root";
	
	private static final String BUSY = "BUSY";
	private static final String READY = "READY";
	
	private static final String IMAGE_GAT_LOCATION = "/GAT";
	private static final String SHARED_FOLDER = null;
	

	
	
	private static void uploadOrchestrationPackages(WebResource resource,
			String packName, String schPackage, String[] packs, String image_id, IFolder packageFolder,
			ProjectMetadata pr_meta, PackageMetadata packMeta, IProgressMonitor monitor ) 
				throws Exception{

		generateConfigurationFiles(packName, schPackage, packs, packageFolder, pr_meta, monitor);
		monitor.beginTask("Creating image for " + packName, 11);
		monitor.worked(1);

		// Uploading file
		monitor.subTask("Uploading runtime configuration files orchestration elements");
		IFile f = packageFolder.getFile("project.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadFile(resource, f.getLocation().toFile(), image_id);
		
		monitor.worked(1);
		f = packageFolder.getFile("resources.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadFile(resource, f.getLocation().toFile(), image_id);
		monitor.worked(1);
		f = packageFolder.getFile("service_manifest.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadFile(resource, f.getLocation().toFile(), image_id);
		monitor.worked(1);
		File file = new File(pr_meta.getRuntimeLocation()
				+ "/../log/it-log4j");
		if (f != null && f.exists()) {
			uploadFile(resource, file, image_id);
		}
		
		monitor.worked(1);
		file = new File(pr_meta.getRuntimeLocation()
				+ "/../xml/projects/project_schema.xsd");
		if (f != null && f.exists()) {
			uploadFile(resource, file, image_id);
		}
		monitor.worked(1);
		file = new File(pr_meta.getRuntimeLocation()
				+ "/../xml/resources/resource_schema.xsd");
		if (f != null && f.exists()) {
			uploadFile(resource, file, image_id);
		}
		monitor.worked(1);
		file = new File(pr_meta.getRuntimeLocation()
				+ "/../worker/worker.sh");
		if (f != null && f.exists()) {
			uploadFile(resource, file, image_id);
		}
		monitor.worked(1);
		monitor.subTask("Setting file permissions in core elements installations");
		settingExecutablePermissions(resource,
				new String[] { IMAGE_DEPLOYMENT_FOLDER + "*.sh" },
				image_id, monitor);
		monitor.worked(1);
		monitor.subTask("Uploading war file with orchestration elements");
		f = packageFolder.getFile(packName + ".war");
		if (f!=null && f.exists()){
			IFile properties = packageFolder.getFile("it.properties");
			f = packageFolder.getFile(packName + ".war");
			PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
					.toFile(), packageFolder, PackagingUtils.WAR_CLASSES_PATH, monitor);
			log.debug("Uploading " + f.getLocation().toOSString());
			uploadFile(resource, f.getLocation().toFile(), image_id);
			monitor.worked(1);
		}
		
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(packName + "_deps.zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(resource, f.getLocation().toFile(), image_id);
		}
		String[] elements= packMeta.getElementsInPackage(packName);
		if (elements== null|| elements.length<=0){
			//
		}
		deployZipDeps(resource, pr_meta.getDependencies(packMeta
				.getElementsInPackage(packName)), image_id, packName, 
				packageFolder, monitor);
		deployWarDeps(resource, pr_meta.getDependencies(packMeta
				.getElementsInPackage(packName)), image_id, packName, 
				packageFolder, monitor);
		deployMonitoring(resource, pr_meta.getRuntimeLocation(), image_id, monitor);
		monitor.worked(1);
	}

	private static void deployMonitoring(WebResource resource,
			String runtimeLocation, String image_id, IProgressMonitor monitor) throws 
			FileNotFoundException, AsceticDeploymentException, InterruptedException {
		
		File f = new File(runtimeLocation+"/../../monitoring/Monitoring.war");
		if (f.exists()){
			uploadWar(resource, f, image_id);
		}else
			throw(new AsceticDeploymentException("Monitoring War not found in "+ f.getAbsolutePath()));
		
	}

	/**
	 * Install the dependencies zip file to the image
	 * 
	 * @param resource Image Creation Service URL's web resource
	 * @param deps Package Dependencies
	 * @param image_id Image Identifier
	 * @param monitor Object to monitor the progress of the image creation
	 * @throws Exception 
	 */
	private static void deployZipDeps(WebResource resource, List<Dependency> deps,
			String image_id, String packageName, IFolder packageFolder, IProgressMonitor monitor)
			throws Exception {
		for (Dependency d : deps) {
			if (d.getType().equalsIgnoreCase(ZIP_DEP_TYPE)) {
				if(d.isImported()){
					IFile properties = packageFolder.getFile("it.properties");
					IFile f = packageFolder.getFolder(EXTERNAL_PACKS_FOLDER).getFolder(packageName).
							getFile(PackagingUtils.getPackageNameWithExtension(d.getLocation()));
					PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
							.toFile(), packageFolder, PackagingUtils.ZIP_CLASSES_PATH, monitor);
					uploadAndUnzip(resource, f.getRawLocation().toFile(), image_id);
				}else
					uploadAndUnzip(resource, new File(d.getLocation()), image_id);
			}
		}
	}

	/**
	 * Install the dependencies war file to the image
	 * 
	 * @param resource Image Creation Service URL's web resource
	 * @param deps Package Dependencies
	 * @param image_id Image Identifier
	 * @param monitor Object to monitor the progress of the image creation
	 * @throws Exception 
	 */
	private static void deployWarDeps(WebResource resource, List<Dependency> deps,
			String image_id, String packageName, IFolder packageFolder, IProgressMonitor monitor)
			throws Exception {
		for (Dependency d : deps) {
			log.debug("Analizing dependency "+ d.getLocation()+ " (Type: " +d.getType()+")");
			if (d.getType().equalsIgnoreCase(WAR_DEP_TYPE)) {
				if(d.isImported()){
					IFile properties = packageFolder.getFile("it.properties");
					IFile f = packageFolder.getFolder(EXTERNAL_PACKS_FOLDER).getFolder(packageName).
							getFile(PackagingUtils.getPackageNameWithExtension(d.getLocation()));
					PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
							.toFile(), packageFolder, PackagingUtils.WAR_CLASSES_PATH, monitor);
					uploadWar(resource, f.getRawLocation().toFile(), image_id);
				}else
					uploadWar(resource, new File(d.getLocation()), image_id);
			}
		}
	}
	/**
	 * Generate the service configuration files which include the creation of 
	 * the resources and project files as well as the runtime properties file
	 * 
	 * @param servicename Name of the implemented service
	 * @param packs Array of service packages names 
	 * @param monitor Object to monitor the progress of the image creation
	 * @throws Exception 
	 */
	public static void generateConfigurationFiles(String ownPack,
			String schPack, final String[] packs, IFolder outFolder, ProjectMetadata prMeta,
			IProgressMonitor monitor) throws Exception {
		
		IFile properties = outFolder.getFile("it.properties");
		if (properties != null && properties.exists()) {
			properties.delete(true, monitor);
		}
		properties.create(new ByteArrayInputStream(new String("").getBytes()),
				true, monitor);
		createProperties(properties.getLocation().toFile(), ownPack, schPack);
		monitor.beginTask("Updating config file for the Ascetic Cloud", 2);
		addCloudProviderToProject(packs, outFolder, prMeta);
		monitor.worked(1);
		addCloudProviderToResources(packs, outFolder, prMeta);
		monitor.done();
		outFolder.refreshLocal(1, monitor);

	}
	
	/**
	 * Add the cloud provider description to the resources file
	 * 
	 * @param serviceName Name of the implemented service
	 * @param packs Names of the service packages
	 * @param packageFolder 
	 * @param project 
	 * @param prMeta 
	 * @throws Exception 
	 */
	private static void addCloudProviderToResources(String[] packs, IFolder packageFolder, ProjectMetadata prMeta)
			throws Exception {
		ResourcesFile res = new ResourcesFile(
				new File(packageFolder.getProject().getLocation()
						+ File.separator + File.separator
						+ prMeta.getMainPackageFolder() + File.separator+ RESOURCES_FILENAME));
		res.addCloudProvider("Ascetic");
		HashMap<String, String> shares = new HashMap<String, String>();
		res.addDisk("shared", SHARED_FOLDER);
		shares.put("shared", SHARED_FOLDER);
		for (String p : packs) {
			res.addImageToCloudProvider("Ascetic", Manifest.generateManifestName(p), shares);
		}
		File file = packageFolder.getFile("resources.xml").getRawLocation().toFile();
		if (file.exists()) {
			file.delete();
		}
		res.toFile(file);
	}

	/**
	 * Add cloud provider description in the project configuration file
	 * 
	 * @param packs Names of the service packages
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws CoreException
	 * @throws TransformerException
	 * @throws JAXBException 
	 */
	private static void addCloudProviderToProject(String[] packs, IFolder packageFolder, ProjectMetadata prMeta)
			throws ParserConfigurationException, SAXException, IOException,
			CoreException, TransformerException, JAXBException {
		ProjectFile res = new ProjectFile(new File(packageFolder.getProject().getLocation()
				+ File.separator + File.separator
				+ prMeta.getMainPackageFolder() + File.separator+ "project.xml"));
		res.addCloudProvider("Ascetic");
		for (String p : packs) {
			res.addImageToProvider("Ascetic", Manifest.generateManifestName(p), 
					OPTIMIS_USER, SHARED_FOLDER, IMAGE_DEPLOYMENT_FOLDER);
		}
		File file = packageFolder.getFile("project.xml").getRawLocation().toFile();
		if (file.exists()) {
			file.delete();
		}
		res.toFile(file);

	}

	/**
	 * Create the runtime configuration properties file
	 * 
	 * @param file Properties file
	 * @throws ConfigurationException
	 */
	private static void createProperties(File file, String packageName, String schedulerPackage) throws ConfigurationException {
		RuntimeConfigManager config = new RuntimeConfigManager(file);
		config.setLog4jConfiguration(IMAGE_DEPLOYMENT_FOLDER + "/it-log4j");
		config.setGraph(true);
		config.setTracing(false);
		config.setMonitorInterval(2000);
		config.setGATBrokerAdaptor("sshtrilead");
		config.setGATFileAdaptor("sshtrilead");
		config.setProjectFile(IMAGE_DEPLOYMENT_FOLDER + "/project.xml");
		config.setProjectSchema(IMAGE_DEPLOYMENT_FOLDER + "/project_schema.xsd");
		config.setResourcesFile(IMAGE_DEPLOYMENT_FOLDER + "/resources.xml");
		config.setResourcesSchema(IMAGE_DEPLOYMENT_FOLDER
				+ "/resource_schema.xsd");
		config.setContext(CONTEXT_FOLDER);
		config.setManifestLocation(IMAGE_DEPLOYMENT_FOLDER + "/service_manifest.xml");
		config.setGATAdaptor(IMAGE_GAT_LOCATION + "/lib/adaptors");
		config.setOptimisPeriod(15000);
		config.setCertificatesContext(IMAGE_DEPLOYMENT_FOLDER);
		config.setComponent(Manifest.generateManifestName(packageName));
		config.setSchedulerComponent(Manifest.generateManifestName(schedulerPackage));
		config.save();
	}


	/**
	 * Upload a file to the created image.
	 * 
	 * @param resource Image Creation Service URL's web resource
	 * @param file File to upload
	 * @param image_id Image Identifier
	 * @throws FileNotFoundException
	 * @throws AsceticDeploymentException
	 */
	private static void uploadFile(WebResource resource, File file, String image_id)
			throws FileNotFoundException, AsceticDeploymentException {
		FileInputStream fileStream = new FileInputStream(file);
		FormDataMultiPart part = new FormDataMultiPart();
		part.field("name", file.getName());
		part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
		log.debug(" Sending " + file.getName());
		boolean uploadOK=false;
		int retries = 0;
		ClientResponse response= null;
		while(!uploadOK && retries <5){
			try{
				response = resource.path("image").path(image_id)
				.path("file")
				.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class, part);
				uploadOK=true;
			}catch (Exception e){
				log.error("Error: "+ e.getMessage());
				retries++;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					//ignoring
				}
				fileStream = new FileInputStream(file);
				part = new FormDataMultiPart();
				part.field("name", file.getName());
				part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
			}
		}
		if (!uploadOK || response==null){
			log.error("Error uploading file");
			throw (new AsceticDeploymentException("Error uploading file "+file.getName()));
		}else if ((response.getStatus() < 200) && (response.getStatus() >= 300)) {
			log.error("response: " + response.toString()
					+ "\nreason: " + response.getEntity(String.class));
			throw (new AsceticDeploymentException(
					"Error interacting with ICS: " + response.toString()));
		}

	}

	/**
	 * Upload and extract a zip file
	 * @param resource Image Creation Service URL's web resource
	 * @param file Zip compressed file 
	 * @param image_id Image Identifier
	 * @throws FileNotFoundException
	 * @throws AsceticDeploymentException
	 */
	private static void uploadAndUnzip(WebResource resource, File file, String image_id)
			throws FileNotFoundException, AsceticDeploymentException {
		FileInputStream fileStream = new FileInputStream(file);
		FormDataMultiPart part = new FormDataMultiPart();
		part.field("name", file.getName());
		part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
		log.debug(" Sending " + file.getName());
		boolean uploadOK=false;
		int retries = 0;
		ClientResponse response= null;
		while(!uploadOK && retries <5){
			try{
				response = resource.path("image").path(image_id)
				.path("zip")
				.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
				.post(ClientResponse.class, part);
				uploadOK=true;
			}catch (Exception e){
				log.error("Error: "+ e.getMessage());
				retries++;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					//ignoring
				}
				fileStream = new FileInputStream(file);
				part = new FormDataMultiPart();
				part.field("name", file.getName());
				part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
			}
		}
		if (!uploadOK || response==null){
			log.error("Error uploading file");
			throw (new AsceticDeploymentException("Error uploading file "+file.getName()));
		}else if ((response.getStatus() < 200) && (response.getStatus() >= 300)) {
			log.error("response: " + response.toString()
					+ "\nreason: " + response.getEntity(String.class));
			throw (new AsceticDeploymentException(
					"Error interacting with ICS: " + response.toString()));
		}

	}

	/** 
	 * Upload and install a war package.
	 * 
	 * @param resource Image Creation Service URL's web resource
	 * @param file War package
	 * @param image_id Image identifier
	 * @throws FileNotFoundException
	 * @throws AsceticDeploymentException
	 */
	private static void uploadWar(WebResource resource, File file, String image_id)
			throws FileNotFoundException, AsceticDeploymentException {
		FileInputStream fileStream = new FileInputStream(file);
		FormDataMultiPart part = new FormDataMultiPart();
		part.field("name", file.getName());
		part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
		log.debug(" Sending " + file.getName());
		boolean uploadOK=false;
		int retries = 0;
		ClientResponse response= null;
		while(!uploadOK && retries <5){
			try{
				response = resource.path("image").path(image_id).path("war")
						.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
						.post(ClientResponse.class, part);
				uploadOK=true;
			}catch (Exception e){
				log.error("Error: "+ e.getMessage());
				retries++;
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					//ignoring
				}
				fileStream = new FileInputStream(file);
				part = new FormDataMultiPart();
				part.field("name", file.getName());
				part.field("file", fileStream, MediaType.TEXT_PLAIN_TYPE);
			}
		}
		if (!uploadOK || response==null){
			log.error("Error uploading file");
			throw (new AsceticDeploymentException("Error uploading file "+file.getName()));
		}else if ((response.getStatus() < 200) && (response.getStatus() >= 300)) {
			log.error("response: " + response.toString()
					+ "\nreason: " + response.getEntity(String.class));
			throw (new AsceticDeploymentException(
					"Error interacting with ICS: " + response.toString()));
		}
	}

	
	
	private static void uploadCoreElementPackages(WebResource resource, String pack,
			String image_id, IFolder packageFolder, ProjectMetadata pr_meta, 
			PackageMetadata packMeta, IProgressMonitor monitor) throws Exception{
		monitor.beginTask("Installing package " + pack, 5);
		IFile f = packageFolder.getFile(pack + ".jar");
		monitor.subTask("Uploading and unziping core elements");
		uploadAndUnzip(resource, f.getLocation().toFile(), image_id);
		monitor.worked(1);
		// Setting file permissions
		monitor.subTask("Setting file permissions in core elements installations");
		settingExecutablePermissions(resource,
				new String[] { IMAGE_DEPLOYMENT_FOLDER + "*.sh" },
				image_id, monitor);
		monitor.worked(1);
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(pack + "_deps.zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(resource, f.getLocation().toFile(), image_id);
		}
		monitor.worked(1);
		monitor.subTask("Uploading zip dependencies");
		deployZipDeps(resource, pr_meta.getDependencies(packMeta
				.getElementsInPackage(pack)), image_id, pack, packageFolder, monitor);
		monitor.worked(1);
		monitor.subTask("Uploading war dependencies");
		deployWarDeps(resource, pr_meta.getDependencies(packMeta
				.getElementsInPackage(pack)), image_id, pack, packageFolder, monitor);
		monitor.worked(1);
	}
	
	/** 
	 * Set the permissions of a file in a image
	 * 
	 * @param resource Image Creation Service URL's web resource
	 * @param files Array of file names
	 * @param image_id Image identifier
	 * @param monitor Object to report the progress of the process
	 * @throws AsceticDeploymentException
	 * @throws InterruptedException
	 */
	private static void settingExecutablePermissions(WebResource resource,
			String[] files, String image_id, IProgressMonitor monitor)
			throws AsceticDeploymentException, InterruptedException {
		ClientResponse response;
		for (String f : files) {
			FormDataMultiPart part = new FormDataMultiPart();
			part.field("file", f);
			part.field("permission", "755");
			response = resource.path("image").path(image_id).path("permission")
					.header("Content-Type", MediaType.MULTIPART_FORM_DATA)
					.put(ClientResponse.class, part);
			if ((response.getStatus() < 200) && (response.getStatus() >= 300)) {
				resource.path("image").path(image_id).delete();
				throw (new AsceticDeploymentException(
						"Error setting permissions for file: " + f
								+ "\nReason: " + response.toString()));
			}
		}

	}
	

	public static String getImageDescription(ProjectMetadata prMeta, PackageMetadata packMeta, String p, HashMap<String, ServiceElement> constEls, 
			boolean master, IJavaProject project) throws Exception {
		String[] els;
		if (master) {
			Map<String, ServiceElement> map = CommonFormPage.getElements(
					prMeta.getAllOrchestrationClasses(),ORCH_TYPE,project, prMeta);
			els = map.keySet().toArray(new String[map.size()]);
		} else {
			els = packMeta.getElementsInPackage(p);
		}
		Map<String, Integer> minCoreInstances = prMeta.getMinElasticity(els);
		Map<String, String> maxConstraints = new HashMap<String, String>();
		Map<String, String> maxResourcesPerMachine = prMeta.getMaxResourcesProperties();
		Map<String, Integer> minCoreInstancesPerMachine = BuildingDeploymentFormPage.
				getConstraintsElements(els, constEls, minCoreInstances, maxResourcesPerMachine, 
						maxConstraints);
		return generateTemplate(maxConstraints);	
	}

	private static String generateTemplate(Map<String, String> maxConstraints) {
		String imageTemplate = new String("<ImageTemplate>");
		String OS = maxConstraints.get(ConstraintDef.OS.getName());
		
		if (OS!=null&& !OS.contains(",")){
			imageTemplate = imageTemplate.concat("<operatingSystem>"+OS+"</operatingSystem>");
		}else
			imageTemplate = imageTemplate.concat("<operatingSystem>Linux</operatingSystem>");
		String arch = maxConstraints.get(ConstraintDef.PROC_ARCH.getName());
		if (arch !=null&& !arch.contains(",")){
			imageTemplate = imageTemplate.concat("<architecture>"+arch+"</architecture>");
		}
		String imageSize = maxConstraints.get(ConstraintDef.STORAGE_SIZE.getName());
		if (imageSize != null){
			int f = (int)Float.parseFloat(imageSize)/1;
			//f= f/1024;
			imageTemplate = imageTemplate.concat("<imageSize>"+f+"</imageSize>");
		}else{
			int f = (int)IDEProperties.DEFAULT_DISK/1024;
			imageTemplate = imageTemplate.concat("<imageSize>"+f+"</imageSize>");
		}
		imageTemplate = imageTemplate.concat("</ImageTemplate>");
		return imageTemplate;
	}

}
