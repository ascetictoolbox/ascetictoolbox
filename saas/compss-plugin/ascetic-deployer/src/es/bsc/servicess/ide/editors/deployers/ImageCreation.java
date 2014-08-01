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
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

public class ImageCreation {

	private static Logger log = Logger.getLogger(ImageCreation.class);
	private final static String IMAGE_DEPLOYMENT_FOLDER = "/ascetic_service/";
	private final static String CONTEXT_FOLDER = "/mnt/context";
	private static final String ASCETIC_USER = "root";
	private static final String IMAGE_GAT_LOCATION = "/GAT";
	private static final long CREATION_PULL_INTERVAL = 30000;
	//private static final String SHARED_FOLDER = null;
	
	
	public static void uploadOrchestrationPackages(VmicApi vmic,
			String packName, String schPackage, String[] packs, IFolder packageFolder,
			ProjectMetadata pr_meta, PackageMetadata packMeta, Manifest manifest, IProgressMonitor monitor ) 
				throws Exception{
		InstallationScript is = new InstallationScript(IMAGE_DEPLOYMENT_FOLDER);
		generatePropertiesFile(packName, schPackage, packs, packageFolder, monitor);
		monitor.beginTask("Uploading files for " + packName, 12);
		
		// Uploading file
		monitor.subTask("Uploading runtime configuration files orchestration elements");
		IFile f = packageFolder.getFile("project.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		
		f = packageFolder.getFile("resources.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		
		/*f = packageFolder.getFile("service_manifest.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadFile(resource, f.getLocation().toFile(), image_id);
		monitor.worked(1);*/
		
		File file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_LOG_PATH+ COMPSS_LOG4J_DEFAULT_NAME);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_XMLS_PATH+COMPSS_PROJECTS_PATH+COMPSS_PROJECT_SCHEMA_NAME);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_XMLS_PATH+COMPSS_RESOURCES_PATH+COMPSS_RESOURCE_SCHEMA_NAME);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_SCRIPTS_PATH + COMPSS_SYSTEM_PATH+ "worker.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_SCRIPTS_PATH+COMPSS_SYSTEM_PATH+ "worker_java.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_SCRIPTS_PATH+COMPSS_SYSTEM_PATH+ "clean.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_SCRIPTS_PATH+ COMPSS_SYSTEM_PATH+ "trace.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		
		monitor.subTask("Setting file permissions in core elements installations");
		settingExecutablePermissions(new String[] { IMAGE_DEPLOYMENT_FOLDER + "*.sh" },is);
		monitor.worked(1);
		
		monitor.subTask("Uploading war file with orchestration elements");
		f = packageFolder.getFile(packName + ".war");
		if (f!=null && f.exists()){
			IFile properties = packageFolder.getFile(packName+"-it.properties");
			f = packageFolder.getFile(packName + ".war");
			PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
					.toFile(), packageFolder, PackagingUtils.WAR_CLASSES_PATH, monitor);
			log.debug("Uploading " + f.getLocation().toOSString());
			uploadWar(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(packName + "_deps.zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		String[] elements= packMeta.getElementsInPackage(packName);
		if (elements== null|| elements.length<=0){
			// TODO he borrado algo??
		}
		monitor.worked(1);
		
		deployZipDeps(vmic, pr_meta.getDependencies(packMeta
				.getElementsInPackage(packName)), packName, 
				packageFolder, manifest, is, monitor);
		monitor.worked(1);
		
		deployWarDeps(vmic, pr_meta.getDependencies(packMeta
				.getElementsInPackage(packName)), packName, 
				packageFolder, manifest, is, monitor);
		monitor.worked(1);
		
		deployMonitoring(vmic, pr_meta.getRuntimeLocation(), manifest, is, monitor);
		monitor.worked(1);
		
		manifest.addVMICExecutionInComponent(Manifest.generateManifestName(packName), is.getCommand());
		monitor.done();
	}

	private static void deployMonitoring(VmicApi vmic, String runtimeLocation, 
			Manifest manifest, InstallationScript is, IProgressMonitor monitor) 
				throws 	Exception {
		File f = new File(runtimeLocation+"/Monitor/"+COMPSS_WAR_NAME);
		if (f.exists()){
			uploadWar(vmic, f, manifest, is, monitor);
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
	private static void deployZipDeps(VmicApi vmic, List<Dependency> deps,
			String packageName, IFolder packageFolder, Manifest manifest, 
			InstallationScript is, IProgressMonitor monitor) throws Exception {
		for (Dependency d : deps) {
			if (d.getType().equalsIgnoreCase(ZIP_DEP_TYPE)) {
				if(d.isImported()){
					IFile properties = packageFolder.getFile(packageName +"-it.properties");
					IFile f = packageFolder.getFolder(EXTERNAL_PACKS_FOLDER).getFolder(packageName).
							getFile(PackagingUtils.getPackageNameWithExtension(d.getLocation()));
					PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
							.toFile(), packageFolder, PackagingUtils.ZIP_CLASSES_PATH, monitor);
					uploadAndUnzip(vmic, f.getRawLocation().toFile(), manifest, is, monitor);
				}else{
					uploadAndUnzip(vmic, new File(d.getLocation()), manifest, is, monitor);
				}
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
	private static void deployWarDeps(VmicApi vmic, List<Dependency> deps,
			String packageName, IFolder packageFolder, Manifest manifest, 
			InstallationScript is, IProgressMonitor monitor) throws Exception {
		for (Dependency d : deps) {
			log.debug("Analizing dependency "+ d.getLocation()+ " (Type: " +d.getType()+")");
			if (d.getType().equalsIgnoreCase(WAR_DEP_TYPE)) {
				if(d.isImported()){
					IFile properties = packageFolder.getFile(packageName +"-it.properties");
					IFile f = packageFolder.getFolder(EXTERNAL_PACKS_FOLDER).getFolder(packageName).
							getFile(PackagingUtils.getPackageNameWithExtension(d.getLocation()));
					PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
							.toFile(), packageFolder, PackagingUtils.WAR_CLASSES_PATH, monitor);
					uploadWar(vmic, f.getRawLocation().toFile(), manifest, is, monitor);
				}else
					uploadWar(vmic, new File(d.getLocation()), manifest, is, monitor);
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
	public static void generateConfigurationFiles(final String[] packs, IFolder outFolder, 
			ProjectMetadata prMeta,	IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Updating config file for the Ascetic Cloud", 2);
		addCloudProviderToProject(packs, outFolder, prMeta);
		monitor.worked(1);
		addCloudProviderToResources(packs, outFolder, prMeta);
		monitor.done();
		outFolder.refreshLocal(1, monitor);

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
	public static void generatePropertiesFile(String ownPack,
			String schPack, final String[] packs, IFolder outFolder,
			IProgressMonitor monitor) throws Exception {
		
		IFile properties = outFolder.getFile(ownPack + "-it.properties");
		if (properties != null && properties.exists()) {
			properties.delete(true, monitor);
		}
		properties.create(new ByteArrayInputStream(new String("").getBytes()),
				true, monitor);
		createProperties(properties.getLocation().toFile(), ownPack, schPack);

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
		//TODO ADD Other Cloud Provider definition
		/*HashMap<String, String> shares = new HashMap<String, String>();
		res.addDisk("shared", SHARED_FOLDER);
		shares.put("shared", SHARED_FOLDER);
		for (String p : packs) {
			res.addImageToCloudProvider("Ascetic", Manifest.generateManifestName(p), shares);
		}*/
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
					ASCETIC_USER, null, IMAGE_DEPLOYMENT_FOLDER);
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
	private static void uploadAndCopy(VmicApi vmic, File file,Manifest manifest, 
			InstallationScript is, IProgressMonitor monitor) throws Exception { 
		if (file.exists()){
			String fileName = uploadFile(vmic, manifest, file, "war", monitor);
			is.addCopy("${"+fileName+"}", IMAGE_DEPLOYMENT_FOLDER);
		}else
			throw  new AsceticDeploymentException("File "+ file.getAbsolutePath()+ " does not exist.");

	}

	/**
	 * Upload and extract a zip file
	 * @param resource Image Creation Service URL's web resource
	 * @param file Zip compressed file 
	 * @param image_id Image Identifier
	 * @throws FileNotFoundException
	 * @throws AsceticDeploymentException
	 * @throws InterruptedException 
	 */
	private static void uploadAndUnzip(VmicApi vmic, File file, Manifest manifest, 
			InstallationScript is, IProgressMonitor monitor) throws Exception {
		if (file.exists()){
			String fileName = uploadFile(vmic, manifest, file, "zip", monitor);
			is.addUnZip("${"+fileName+"}", IMAGE_DEPLOYMENT_FOLDER);
		}else
			throw  new AsceticDeploymentException("File "+ file.getAbsolutePath()+ " does not exist.");

	}

	private static String uploadFile(VmicApi vmic, Manifest manifest, File file, String format,
			IProgressMonitor monitor) throws Exception {
		String manifestFileName = manifest.getVMICFileName(file.getName());
		//manifest.addFiles(manifestFileName, "/repo/path/"+file.getName(), format);
		//if (!manifest.fileExists(manifestFileName)){
			log.debug("Uploading file "+ file.getName() + " from app "+ manifest.getServiceId());
			vmic.uploadFile(manifest.getServiceId(), file);
			String path = null;
			do{
				Thread.sleep(10000);
				ProgressDataFile pdf = (ProgressDataFile) vmic.progressCallback(manifest.getServiceId(), file);
					if (pdf.isError()){
						log.error("Error uploading file "+ file.getName() , pdf.getException());
						throw new AsceticDeploymentException("Error uploading file "+ file.getName());
					}else if (pdf.isComplete()){
						path = pdf.getRemotePath();
						manifest.addFile(manifestFileName, path, format);
					}else{
						monitor.worked(pdf.getCurrentPercentageCompletion().intValue());
					}		
			}while (path == null);
		/*}else{
			log.debug("File already uploaded");
		}*/
		return manifestFileName;
		
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
	private static void uploadWar(VmicApi vmic, File file, Manifest manifest, 
			InstallationScript is, IProgressMonitor monitor)
			throws Exception {
		if (file.exists()){
			String fileName = uploadFile(vmic, manifest, file, "war", monitor);
			is.addCopy("${"+fileName+"}", "${IMAGE_WEBAPP_FOLDER}");
		}else
			throw  new AsceticDeploymentException("File "+ file.getAbsolutePath()+ " does not exist.");
		
	}

	
	
	public static void uploadCoreElementPackages(VmicApi vmic, String pack, 
			IFolder packageFolder, ProjectMetadata prMeta, 
			PackageMetadata packMeta, Manifest manifest, IProgressMonitor monitor) throws Exception{
		InstallationScript is = new InstallationScript(IMAGE_DEPLOYMENT_FOLDER);
		monitor.beginTask("Uploading packages for " + pack, 5);
		
		IFile f = packageFolder.getFile(pack + ".jar");
		monitor.subTask("Uploading and unziping core elements");
		uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		
		// Setting file permissions
		monitor.subTask("Setting file permissions in core elements installations");
		settingExecutablePermissions(new String[] { IMAGE_DEPLOYMENT_FOLDER + "*.sh" },is);
		monitor.worked(1);
		
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(pack + "_deps.zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		
		monitor.subTask("Uploading zip dependencies");
		deployZipDeps(vmic, prMeta.getDependencies(packMeta
				.getElementsInPackage(pack)), pack, packageFolder, manifest, is, monitor);
		monitor.worked(1);
		
		monitor.subTask("Uploading war dependencies");
		deployWarDeps(vmic, prMeta.getDependencies(packMeta
				.getElementsInPackage(pack)), pack, packageFolder, manifest, is, monitor);
		monitor.worked(1);
		
		manifest.addVMICExecutionInComponent(Manifest.generateManifestName(pack), is.getCommand());
		monitor.done();
	}
	
	/** 
	 * Set the permissions of a file in a image
	 * 
	 * @param files Array of file names
	 * @param image_id Image identifier
	 * @param monitor Object to report the progress of the process
	 * @throws AsceticDeploymentException
	 * @throws InterruptedException
	 */
	private static void settingExecutablePermissions(	String[] files, InstallationScript is) {
		for (String f : files) {
			is.addExecutablePermission(f);
		}

	}
	
	public static void generateImages(VmicApi vmic, Manifest manifest, 
			IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Creating Images", 100);
		//TODO: Remove when execution with VMIC is done
		manifest.generateFakeImages();
		/*vmic.generateImage(manifest.getOVFDefinition());
		boolean complete = false;
		do{
			Thread.sleep(CREATION_PULL_INTERVAL);
			ProgressDataImage pd = (ProgressDataImage) vmic.progressCallback(
					manifest.getServiceId(), null);
			if (pd.isError()){
				log.error("Error generating images", pd.getException());
				throw new AsceticDeploymentException("Error generating images");
			}else if (pd.isComplete()){
				complete = true;
				manifest.updateOVFDefinition(pd.getOvfDefinition());
			}else{
				monitor.worked(pd.getCurrentPercentageCompletion().intValue());
			}
		}while(!complete);*/
		monitor.done();
	}
	
}
