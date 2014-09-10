/*
 *  Copyright 2011-2012 Barcelona Supercomputing Center (www.bsc.es)
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



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.dialogs.JarExecutionDialog;
import es.bsc.servicess.ide.views.DeploymentChecker;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.saas.application_uploader.ApplicationUploader;

public class AsceticDeploymentChecker implements DeploymentChecker {
	private ApplicationUploader AMClient;
	private String applicationID;
	private String executionID;
	private String status;
	private String mainClass;
	private String[] jars;
	private String masterPackage;
	private AsceticDeployer deployer;
	private Map<String, Map<String, String>> currentVMs;
	
	private static String MAX_PERM = "1024m";
	private static String MS_MEM = "1024m";
	private static String MX_MEM = "2048m";
	
	private static Logger log = Logger.getLogger(AsceticDeploymentChecker.class);
	
	public AsceticDeploymentChecker(ApplicationUploader appUploader, String applicationID, 
			String mainClass, String[] jars, String masterPackage, AsceticDeployer deployer) {
		this.AMClient = appUploader;
		this.applicationID = applicationID;
		this.mainClass = mainClass;
		this.jars = jars;
		this.deployer = deployer;
		this.masterPackage = masterPackage;
		this.status = DeploymentChecker.PENDING;
	}

	@Override
	public String getStatus(String deploymentID) {
		if (status.equals(DeploymentChecker.RUNNING)){
			try{
				if (isJarExecutionFinished()){
					this.status = DeploymentChecker.FINISHED;
				}
			}catch(CoreException e){
				log.error("Error getting jar execution status", e);
				this.status = DeploymentChecker.RUN_FAILED;
			}
		}else if (status.equals(DeploymentChecker.PENDING)){
			try {
				String st = AMClient.getDeploymentStatus(applicationID,
						deploymentID);
				if (st.equals(Dictionary.APPLICATION_STATUS_ERROR)) {
					log.error("Error deploying application " + applicationID
							+ " (deploymentID: " + deploymentID + ")");
					this.status = DeploymentChecker.DEPLOYMENT_FAILED;
				} else if (st.equals(Dictionary.APPLICATION_STATUS_DEPLOYED)) {
					this.status = DeploymentChecker.DEPLOYED;
				} else if (st.equals(Dictionary.APPLICATION_STATUS_TERMINATED)) {
					this.status = DeploymentChecker.UNDEPLOYED;
				} else {
					this.status = DeploymentChecker.PENDING;
				}
			} catch (Exception e) {
				log.error("Error getting deployment status", e);
				this.status = DeploymentChecker.DEPLOYMENT_FAILED;
			}	
		}
		return this.status;
	}

	@Override
	public Map<String, Map<String, String>> getMachines(String deploymentID) {
		try {
			currentVMs = AMClient.getDeployedVMs(applicationID, deploymentID);
		} catch (Exception e) {
			log.error("Error getting machines from application manager", e);
		}
		return currentVMs;
	}


	@Override
	public void undeploy(String serviceID, boolean keepData) {
		try {
			AMClient.undeploy(serviceID, applicationID);
		}catch (Exception e) {
			log.error("Error undeploying application", e);
		}
	}

	
	@Override
	public void stop(String serviceID){
		try{
			status = DeploymentChecker.CANCELED;
			stopJarExecution();
		} catch (CoreException e) {
			status = DeploymentChecker.CANCEL_FAILED;
			String message = e.getCause().getMessage();
			ErrorDialog.openError(deployer.getShell(), "Error",
					"Stopping the application: "+ message, e.getStatus());
		}		

	}

	@Override
	public void start(String serviceID)  {
		try{
			executeJar();
			status = DeploymentChecker.RUNNING;
		} catch (CoreException e) {
			status = DeploymentChecker.RUN_FAILED;
			String message = e.getCause().getMessage();
			ErrorDialog.openError(deployer.getShell(), "Error",
					"Starting the application: "+ message, e.getStatus());
		}
	}
	
	public void executeJar() throws CoreException {
		IJavaProject project = deployer.getProject();
		executionID = UUID.randomUUID().toString();
		/*String projectName = project.getProject().getName();*/
		String hostname = getMasterNode();
		String ceLocation = ImageCreation.IMAGE_DEPLOYMENT_FOLDER;
		String username = ImageCreation.ASCETIC_USER;
		String bundle, jsch_file, target= null;
		try{
			jsch_file = new File (FileLocator.toFileURL(
					Platform.getBundle(AsceticDeployer.BUNDLE_NAME).getEntry("/lib/jsch-0.1.42.jar")).toURI()).getAbsolutePath();
			target = new File (FileLocator.toFileURL(
					Platform.getBundle(AsceticDeployer.BUNDLE_NAME).getEntry("/target/classes/")).toURI()).getAbsolutePath();
			bundle = FileLocator.getBundleFile(Platform.getBundle(AsceticDeployer.BUNDLE_NAME))
				.getAbsolutePath();
			
			
		}catch(Exception e) {
			throw (new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.getMessage(),e)));
		}
		
		if (ceLocation != null && !ceLocation.isEmpty()) {
			JarExecutionDialog dialog = new JarExecutionDialog(deployer.getShell(), masterPackage);
			if (mainClass!=null)
				dialog.setMainClass(mainClass);
			if (dialog.open() != Window.OK){
				throw (new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Execution information not introduced")));
			}
			
			ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configurations = manager
					.getLaunchConfigurations(type);
			for (int i = 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration = configurations[i];
				if (configuration.getName().equals(getLaunchName(applicationID+"-"+executionID))) {
					configuration.delete();
					break;
				}
			}
			String arguments = generateArguments(username, hostname, ceLocation, 
					new HashMap<String, String>(), new HashMap<String, String>(), 
					dialog.getMainClass(), dialog.getArguments());
					//TODO add data staging "deployer.getDataStageIn(), deployer.getDataStageOut(), dialog.getMainClass(), dialog.getArguments());
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null,getLaunchName(applicationID+"-"+executionID));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					"es.bsc.servicess.ide.editors.deployers.JSCHExecutionUtils");
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
					arguments);
			List<String> classpath = new ArrayList<String>();
			File jdkHome = JavaRuntime.getVMInstall(project).getInstallLocation();
			IPath toolsPath = new Path(jdkHome.getAbsolutePath()).append("lib")
					.append("tools.jar");
			IRuntimeClasspathEntry toolsEntry = JavaRuntime
					.newArchiveRuntimeClasspathEntry(toolsPath);
			toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			log.debug("Adding classpath: " + toolsEntry.getMemento());
			classpath.add(toolsEntry.getMemento());
			IPath jarPath = new Path(jsch_file);
			IRuntimeClasspathEntry jarPathEntry = JavaRuntime
					.newArchiveRuntimeClasspathEntry(jarPath);
			jarPathEntry
					.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);	
			log.debug("Adding class path: " + jarPathEntry.getMemento());
			classpath.add(jarPathEntry.getMemento());
			jarPath = new Path(bundle);
			jarPathEntry = JavaRuntime
					.newArchiveRuntimeClasspathEntry(jarPath);
			jarPathEntry
					.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);	
			log.debug("Adding class path: " + jarPathEntry.getMemento());
			classpath.add(jarPathEntry.getMemento());
			jarPath = new Path(target);
			jarPathEntry = JavaRuntime
					.newArchiveRuntimeClasspathEntry(jarPath);
			jarPathEntry
					.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);	
			log.debug("Adding class path: " + jarPathEntry.getMemento());
			classpath.add(jarPathEntry.getMemento());
			IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
			IRuntimeClasspathEntry systemLibsEntry = JavaRuntime
					.newRuntimeContainerClasspathEntry(systemLibsPath,
							IRuntimeClasspathEntry.STANDARD_CLASSES);
			log.debug("Adding class path: " + systemLibsEntry.getMemento());
			classpath.add(systemLibsEntry.getMemento());

			String javaOpts = "-XX:MaxPermSize=" + MAX_PERM + " -Xms" + MS_MEM
					+ " -Xmx" + MX_MEM ;
			
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, javaOpts);
			/*File workingDir = new File(serverLocation + "/bin");
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					workingDir.getAbsolutePath());*/
			ILaunchConfiguration configuration = workingCopy.doSave();
			DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
		} else
			throw (new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Element deployment location not found")));
	}
	
	private String getMasterNode() throws CoreException {
		if (currentVMs!=null && !currentVMs.isEmpty()){ 
			for (Map<String, String> prov: currentVMs.values()){
				String masterIP = prov.get(Manifest.generateManifestName(masterPackage));
				if (masterIP !=null)
					return masterIP;
			}
			throw(new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID, "Master VM not found.")));
		}else
			throw(new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Current deployment doesn't have VMs")));
	}

	private String generateArguments(String username, String hostname,
			String ceLocation, HashMap<String, String> stageIns, HashMap<String, String> stageOuts, String mainClass, String arguments) {
		String args = new String(username + " " + hostname);
		if (stageIns == null ||stageIns.isEmpty())
			args = args.concat(" " + 0);
		else
			args = args.concat(" " + stageIns.size());
		if (stageOuts == null ||stageOuts.isEmpty())
			args = args.concat(" " + 0);
		else
			args = args.concat(" " + stageOuts.size());
		if (stageIns != null && !stageIns.isEmpty()){	
			for (Entry<String, String> e:stageIns.entrySet()){
				args = args.concat(" " + e.getKey()+" "+ e.getValue());
			}
		}
		if (stageIns != null && !stageOuts.isEmpty()){	
			for (Entry<String, String> e:stageOuts.entrySet()){
				args = args.concat(" " + e.getKey()+" "+ e.getValue());
			}
		}
		args = args.concat(" " + ceLocation + " " + mainClass + " " + arguments);
		return args;
	}

	public boolean isJarExecutionFinished() throws CoreException{
		//String projectName = deployer.getProject().getProject().getName();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunch launch : manager.getLaunches()) {
			log.debug("Evaluating launch"
					+ launch.getLaunchConfiguration().getName());
			if (launch.getLaunchConfiguration().getName()
					.equals(getLaunchName(applicationID+"-"+executionID))) {
				return launch.isTerminated();
			}
		}
		throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,
				"Launch for " + applicationID+"-"+executionID + " not found."));
	}
	
	public void stopJarExecution()throws CoreException{
		//String projectName = deployer.getProject().getProject().getName();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunch launch : manager.getLaunches()) {
			log.debug("Evaluating launch"
					+ launch.getLaunchConfiguration().getName());
			if (launch.getLaunchConfiguration().getName()
					.equals(getLaunchName(applicationID+"-"+executionID))) {
				launch.terminate();
				return;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,
				"Launch for " + applicationID+"-"+executionID + " not found."));
	}
	
	public static String getLaunchName(String projectName){
		return "Ascetic "+projectName+ " execution" ;
	}
	
	

}
