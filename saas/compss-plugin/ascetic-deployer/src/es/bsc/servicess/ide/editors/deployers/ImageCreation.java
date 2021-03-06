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

import integratedtoolkit.util.RuntimeConfigManager;
import static es.bsc.servicess.ide.Constants.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.PackagingUtils;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.RuntimeConfigurationSection;
import es.bsc.servicess.ide.model.Dependency;
import es.bsc.servicess.ide.model.ServiceElement;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

public class ImageCreation {


	public final static String IMAGE_DEPLOYMENT_FOLDER = "/ascetic_service/";
	public final static String IMAGE_WORKING_FOLDER = "/tmp/";
	public static final String ASCETIC_USER = "root";
	public static final String ASCETIC_CONNECTOR = "integratedtoolkit.connectors.ascetic.AsceticConnector";
	public static final String ASCETIC_SCHEDULER = "integratedtoolkit.scheduler.ascetic.AsceticScheduler";
	public static final String ASCETIC_INTERACTIONS_JAR = "interaction.jar";
	public static final String ASCETIC_CONNECTOR_JAR = "ascetic.jar";
	private final static String MOUNT_POINT_VAR = "${MOUNT_POINT}";
	private final static String WEBAPP_FOLDER_VAR = "${IMAGE_WEBAPP_FOLDER}";
	private final static String CONTEXT_FOLDER = "/mnt/context";
	private static final long CREATION_PULL_INTERVAL = 30000;

	private static Logger log = Logger.getLogger(ImageCreation.class);
	
	
	public static void uploadOrchestrationPackages(VmicApi vmic,
			String packName, String schPackage, String[] packs, IFolder packageFolder,
			ProjectMetadata pr_meta, PackageMetadata packMeta, Manifest manifest, IJavaProject project, IProgressMonitor monitor ) 
					throws InterruptedException, Exception {
		InstallationScript is = manifest.getVMICExecutionForComponent(Manifest.generateManifestName(packName));
		if (is ==null)
				is = new InstallationScript(MOUNT_POINT_VAR+IMAGE_DEPLOYMENT_FOLDER);
		generatePropertiesFile(packName, schPackage, packs, packageFolder, project, monitor);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		monitor.subTask("Uploading war file with orchestration elements");
		IFile f = packageFolder.getFile(packName + ".war");
		if (f!=null && f.exists()){
			IFile properties = packageFolder.getFile(packName+"-it.properties");
			f = packageFolder.getFile(packName + ".war");
			PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
					.toFile(), packageFolder, PackagingUtils.WAR_CLASSES_PATH, monitor);
			log.debug("Uploading " + f.getLocation().toOSString());
			uploadWar(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		f = packageFolder.getFile(packName + ".jar");
		if (f!=null && f.exists()){
			IFile properties = packageFolder.getFile(packName+"-it.properties");
			f = packageFolder.getFile(packName + ".jar");
			PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
					.toFile(), packageFolder, PackagingUtils.JAR_CLASSES_PATH, monitor);
			log.debug("Uploading " + f.getLocation().toOSString());
			uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		monitor.beginTask("Uploading files for " + packName, 12);
		// Uploading file
		monitor.subTask("Uploading runtime configuration files orchestration elements");
		f = packageFolder.getFile(PROJECT_FILENAME);
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		f = packageFolder.getFile(RESOURCES_FILENAME);
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		/*f = packageFolder.getFile("service_manifest.xml");
		log.debug("Uploading " + f.getLocation().toOSString());
		uploadFile(resource, f.getLocation().toFile(), image_id);
		monitor.worked(1);*/
		String logLevel = RuntimeConfigurationSection.getProjectITConfigManager(project).getLog4jConfiguration();
		File file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_LOG_PATH+ COMPSS_LOG4J_DEFAULT_NAME+"."+logLevel);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_XMLS_PATH+COMPSS_PROJECTS_PATH+COMPSS_PROJECT_SCHEMA_NAME);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_XMLS_PATH+COMPSS_RESOURCES_PATH+COMPSS_RESOURCE_SCHEMA_NAME);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RUNTIME_PATH + ASCETIC_INTERACTIONS_JAR);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_CONNECTORS_PATH + ASCETIC_CONNECTOR_JAR);
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		/*
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_SCRIPTS_PATH+COMPSS_SYSTEM_PATH+ "worker_java.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_SCRIPTS_PATH+COMPSS_SYSTEM_PATH+ "clean.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		file = new File(pr_meta.getRuntimeLocation()
				+ COMPSS_RT_SCRIPTS_PATH+ COMPSS_SYSTEM_PATH+ "trace.sh");
		if (f != null && f.exists()) {
			uploadAndCopy(vmic, file, manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}*/
		monitor.subTask("Setting file permissions in core elements installations");
		settingPermissions(new String[] { MOUNT_POINT_VAR+IMAGE_DEPLOYMENT_FOLDER + "*" },"+x",true,is);
		settingPermissions(new String[] { MOUNT_POINT_VAR+"/root" },"+rx",false, is);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
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
		f = packageFolder.getFile(packName + ".jar");
		if (f!=null && f.exists()){
			IFile properties = packageFolder.getFile(packName+"-it.properties");
			f = packageFolder.getFile(packName + ".jar");
			PackagingUtils.addRuntimeConfigTojar(f, properties.getLocation()
					.toFile(), packageFolder, PackagingUtils.JAR_CLASSES_PATH, monitor);
			log.debug("Uploading " + f.getLocation().toOSString());
			uploadAndCopy(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(packName + "_cores_deps.zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		String[] elements= packMeta.getElementsInPackage(packName);
		if (elements== null|| elements.length<=0){
			// TODO he borrado algo??
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		List<Dependency> packDeps = pr_meta.getAllDependencies(packMeta
				.getElementsInPackage(packName));
		deployZipDeps(vmic, packDeps, packName, 
				packageFolder, manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		deployWarDeps(vmic, packDeps, packName, 
				packageFolder, manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		deployMonitoring(vmic, pr_meta.getRuntimeLocation(), manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		manifest.addVMICExecutionInComponent(Manifest.generateManifestName(packName), is.getCommand());
		monitor.done();
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		
	}

	private static void deployMonitoring(VmicApi vmic, String runtimeLocation, 
			Manifest manifest, InstallationScript is, IProgressMonitor monitor) 
				throws 	Exception {
		File f = new File(runtimeLocation+"/Tools/monitor/apache-tomcat/webapps/"+COMPSS_WAR_NAME);
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
	 * @param manifest 
	 * @param asceticProperties 
	 * @param monitor Object to monitor the progress of the image creation
	 * @throws Exception 
	 */
	public static void generateConfigurationFiles(IJavaProject project, final String[] packs, IFolder outFolder, 
			ProjectMetadata prMeta,	Manifest manifest, AsceticProperties properties, IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Updating config file for the Ascetic Cloud", 2);
		ProjectCloudSpecification.addCloudProviderToProject(packs, outFolder, prMeta, manifest, properties);
		monitor.worked(1);
		ResourcesCloudSpecification.addCloudProviderToResources(project, packs, outFolder, prMeta, manifest, properties);
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
			IJavaProject project, IProgressMonitor monitor) throws Exception {
		
		IFile properties = outFolder.getFile(ownPack + "-it.properties");
		if (properties != null && properties.exists()) {
			properties.delete(true, monitor);
		}
		properties.create(new ByteArrayInputStream(new String("").getBytes()),
				true, monitor);
		createProperties(properties.getLocation().toFile(), ownPack, schPack, project);

	}
	
	

	
	/**
	 * Create the runtime configuration properties file
	 * 
	 * @param file Properties file
	 * @throws ConfigurationException
	 * @throws CoreException 
	 */
	private static void createProperties(File file, String packageName, String schedulerPackage, IJavaProject project) throws ConfigurationException, CoreException {
		RuntimeConfigManager config = new RuntimeConfigManager(file);
		RuntimeConfigManager oldConfig = RuntimeConfigurationSection.getProjectITConfigManager(project);
		config.setLog4jConfiguration(IMAGE_DEPLOYMENT_FOLDER + File.separator+COMPSS_LOG4J_DEFAULT_NAME+"."+oldConfig.getLog4jConfiguration());
		config.setProperty("log4j.configuration", IMAGE_DEPLOYMENT_FOLDER + File.separator+COMPSS_LOG4J_DEFAULT_NAME+"."+oldConfig.getLog4jConfiguration());
		config.setGraph(oldConfig.isGraph());
		config.setTracing(oldConfig.getTracing());
		config.setMonitorInterval(oldConfig.getMonitorInterval());
		config.setScheduler(ASCETIC_SCHEDULER);
		String commAdaptor = oldConfig.getCommAdaptor();
		if (commAdaptor!=null && !commAdaptor.isEmpty())
			config.setCommAdaptor(commAdaptor);
		config.setGATBrokerAdaptor("sshtrilead");
		config.setGATFileAdaptor("sshtrilead");
		config.setProjectFile(IMAGE_DEPLOYMENT_FOLDER + File.separator + PROJECT_FILENAME);
		config.setProjectSchema(IMAGE_DEPLOYMENT_FOLDER + File.separator + COMPSS_PROJECT_SCHEMA_NAME);
		config.setResourcesFile(IMAGE_DEPLOYMENT_FOLDER + File.separator + RESOURCES_FILENAME);
		config.setResourcesSchema(IMAGE_DEPLOYMENT_FOLDER + File.separator + COMPSS_RESOURCE_SCHEMA_NAME);
		config.setContext(CONTEXT_FOLDER);
		config.setGATAdaptor(IMAGE_DEPLOYMENT_FOLDER +"/adaptors");
		config.setWorkerCP(IMAGE_DEPLOYMENT_FOLDER);
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
			is.addCopy("${"+fileName+"}", MOUNT_POINT_VAR + IMAGE_DEPLOYMENT_FOLDER);
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
			is.addUnZip("${"+fileName+"}", MOUNT_POINT_VAR + IMAGE_DEPLOYMENT_FOLDER);
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
						manifest.addFile(manifestFileName, path+File.separator+file.getName(), format);
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
			is.addCopy("${"+fileName+"}", MOUNT_POINT_VAR+"/"+WEBAPP_FOLDER_VAR);
		}else
			throw  new AsceticDeploymentException("File "+ file.getAbsolutePath()+ " does not exist.");
		
	}

	
	
	public static void uploadCoreElementPackages(VmicApi vmic, String pack, 
			IFolder packageFolder, ProjectMetadata prMeta, PackageMetadata packMeta, 
			Manifest manifest, IJavaProject project, IProgressMonitor monitor) 
					throws InterruptedException, Exception{
		InstallationScript is = manifest.getVMICExecutionForComponent(Manifest.generateManifestName(pack));
		if (is ==null)
			is = new InstallationScript(MOUNT_POINT_VAR + IMAGE_DEPLOYMENT_FOLDER);
		monitor.beginTask("Uploading packages for " + pack, 5);
		
		IFile f = packageFolder.getFile(pack +CORE_SUFFIX+ ".jar");
		monitor.subTask("Uploading and unziping core elements");
		uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}	
		monitor.subTask("Uploading dependencies");
		f = packageFolder.getFile(pack +CORE_SUFFIX+ DEPS_EXT+".zip");
		if (f != null && f.exists()) {
			uploadAndUnzip(vmic, f.getLocation().toFile(), manifest, is, monitor);
		}
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		HashMap<String, ServiceElement> coreElements = CommonFormPage.getElements(
				prMeta.getAllOrchestrationClasses(), PackageMetadata.CORE_TYPE, 
				project, prMeta);
		List<Dependency> packDeps = PackagingUtils.getCoreElementDependencies(
				packMeta.getElementsInPackage(pack), coreElements, prMeta);
		monitor.subTask("Uploading zip dependencies");
		deployZipDeps(vmic, packDeps, pack, packageFolder, manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}	
		monitor.subTask("Uploading war dependencies");
		deployWarDeps(vmic, packDeps, pack, packageFolder, manifest, is, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}	
		// Setting file permissions
		monitor.subTask("Setting file permissions in core elements installations");
		settingPermissions(new String[] { MOUNT_POINT_VAR + IMAGE_DEPLOYMENT_FOLDER + "*" },"+x", true, is);
		monitor.worked(1);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}	
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
	private static void settingPermissions(	String[] files, String permission, boolean recursive, InstallationScript is) {
		for (String f : files) {
			is.addPermission(f, permission, recursive);
		}

	}
	
	public static void generateImages(VmicApi vmic, Manifest manifest, 
			IProgressMonitor monitor) throws InterruptedException, Exception {
		monitor.beginTask("Creating Images", 100);
		vmic.generateImage(manifest.getOVFDefinition());
		boolean complete = false;
		do{
			if (monitor.isCanceled()){
				throw new InterruptedException("Creation Cancelled");
			}
			Thread.sleep(CREATION_PULL_INTERVAL);
			ProgressDataImage pd = (ProgressDataImage) vmic.progressCallback(
					manifest.getServiceId(), null);
			if (pd.isError()){
				log.error("Error generating images", pd.getException());
				throw new AsceticDeploymentException("Error generating images");
			}else if (pd.isComplete()){
				complete = true;
				log.debug("Returned OVF: " + pd.getOvfDefinition().toString());
				manifest.updateOVFDefinition(pd.getOvfDefinition());
			}else{
				monitor.worked(pd.getCurrentPercentageCompletion().intValue());
			}
			
		}while(!complete);
		monitor.done();
	}
	
}
