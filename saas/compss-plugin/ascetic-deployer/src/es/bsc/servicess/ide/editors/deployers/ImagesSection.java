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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.ServiceEditorSection;
import es.bsc.servicess.ide.editors.ServiceFormEditor;
import es.bsc.servicess.ide.model.ServiceElement;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

public class ImagesSection extends ServiceEditorSection {

	//private File imageMetadataFile;
	private boolean redoingImages;
	private Composite icsComposite;
	private Text icsText;
	private AsceticDeployer deployer;
	private Button icsButton;
	private Text rsyncPathText;
	//private Combo icsMode;
	private Text repoPathText;
	private Text userKeyPathText;
	private Text usernameText;
	private Text rshPathText;
	
	private static Logger log = Logger.getLogger(ImagesSection.class);
	
	/*private static final String DEFAULT_IMAGE_CREATION_MODE = "Default Mode";
	private static final String BROKER_IMAGE_CREATION_MODE = "Broker Optimization Mode";
	private static final String[] IMAGE_CREATION_MODES = 
			new String[]{DEFAULT_IMAGE_CREATION_MODE,BROKER_IMAGE_CREATION_MODE};*/
	public static final String IMAGE_CREATION_SEC_TITLE = "Image Creation";
	public static final String IMAGE_CREATION_SEC_DESC = "Define the options for the vm image creation process";
	public static final String IMAGE_CREATION_SERVICE_LABEL = "VM Image Creation Service";
	public static final String IMAGE_CREATION_HOST_LABEL = "VM Image Creation Hostname";
	public static final String IMAGE_CREATION_REPO_PATH_LABEL = "VM Image Creation Repository Path";
	public static final String IMAGE_CREATION_RSYNC_PATH_LABEL = "VM Image Creation Rsync Path";
	public static final String IMAGE_CREATION_RSH_PATH_LABEL = "VM Image Creation Remote Shell Path";
	public static final String IMAGE_CREATION_USERNAME_LABEL = "VM Image Creation Username";
	public static final String IMAGE_CREATION_USER_KEY_PATH_LABEL = "VM Image Creation User Key Path";
	public static final String IMAGE_CREATION_BUTTON = "Create Images";
		
	/** 
	 * Constructor
	 * @param form Parent's form
	 * @param toolkit Parent's toolkit
	 * @param shell Parent's shell
	 * @param editor Parent's editor
	 */
	public ImagesSection (FormToolkit toolkit, ServiceFormEditor editor, int format, 
			File packageMetadataFile, AsceticDeployer deployer) {
		super(toolkit, editor,IMAGE_CREATION_SEC_TITLE,IMAGE_CREATION_SEC_DESC , format);
		//this.imageMetadataFile = packageMetadataFile;
		this.deployer = deployer;
		this.redoingImages = false;
	}
	
	public void createSectionWidgets(Composite composite) {
		GridLayout firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 1;
		composite.setLayout(firstRow1Layout);
		icsComposite = toolkit.createComposite(composite,
				SWT.BORDER);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		icsComposite.setLayoutData(rd);
		icsComposite.setLayout(new GridLayout(2, false));
		addHostField();
		addRepoPathField();
		addRsyncPathField();
		addRemoteShellPathField();
		addUsernameField();
		addUserKeyPathField();
		
		icsButton = toolkit.createButton(icsComposite,
				IMAGE_CREATION_BUTTON, SWT.NORMAL);
		icsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				createServiceImages();
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createServiceImages();
			}
		});
	}
	
	private void addUserKeyPathField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_USER_KEY_PATH_LABEL,
				SWT.NONE);
		userKeyPathText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		userKeyPathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setVMICRshUserKeyPath(userKeyPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		userKeyPathText.setLayoutData(rd);
	}

	private void addUsernameField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_USERNAME_LABEL,
				SWT.NONE);
		usernameText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		usernameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setVMICRshUsername(usernameText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		usernameText.setLayoutData(rd);
		
	}

	private void addRemoteShellPathField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_RSH_PATH_LABEL,
				SWT.NONE);
		rshPathText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		rshPathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setVMICRshPath(rshPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		rshPathText.setLayoutData(rd);
		
	}
	
	
	private void addRsyncPathField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_RSYNC_PATH_LABEL,
				SWT.NONE);
		rsyncPathText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		rsyncPathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setVMICRsyncPath(rsyncPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		rsyncPathText.setLayoutData(rd);
		
	}

	private void addRepoPathField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_REPO_PATH_LABEL,
				SWT.NONE);
		repoPathText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		repoPathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setVMICRepoPath(repoPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		repoPathText.setLayoutData(rd);
		
	}

	private void addHostField() {
		toolkit.createLabel(icsComposite, IMAGE_CREATION_HOST_LABEL,
				SWT.NONE);
		icsText = toolkit.createText(icsComposite, "",
				SWT.SINGLE | SWT.BORDER);
		icsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setICSLocation(icsText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving ascetic properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		icsText.setLayoutData(rd);
		
	}

	/**
	 * Invokes the runnable for creating the service images
	 */
	protected void createServiceImages() {
		//ProgressMonitorDialog dialog = new ProgressMonitorDialog(editor.getSite().getShell());
		final String host = icsText.getText().trim();
		final String repoPath = repoPathText.getText().trim();
		final String rsyncPath = rsyncPathText.getText().trim();
		final String rshPath = rshPathText.getText().trim();
		final String rshUsername = usernameText.getText().trim();
		final String rshKeyPath = userKeyPathText.getText().trim();
		if (!redoingImages && !deployer.isBlocking()){

			if (host != null && host.length() > 0 && repoPath != null && repoPath.length() > 0
					&& rsyncPath != null && rsyncPath.length() > 0 	&& rshPath != null && rshPath.length() > 0
					&& rshKeyPath != null && rshKeyPath.length() > 0 && rshUsername != null && rshUsername.length() > 0){
				final String serviceID = editor.getProject().getProject().getName();
				Job job = new Job ("Creating Images for "+ serviceID){

					@Override
					public IStatus run(IProgressMonitor monitor){
						redoingImages=true;
						try {

							/*/dialog.run(false, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {*/
							redoingImages=true;
							executeImageCreation( host, repoPath, rsyncPath, rshPath, rshUsername, rshKeyPath, monitor);
							redoingImages=false;
							sendFinishMessage(serviceID);
							return Status.OK_STATUS;
							

						} catch (InterruptedException e) {
							log.error("Error creating images", e);
							Status errSt = new Status(Status.CANCEL, Activator.PLUGIN_ID,"Creation cancelled", e);
							/*ErrorDialog.openError(editor.getSite().getShell(), "Error creating images",
									e.getMessage(), errSt);*/
							redoingImages=false;
							return errSt;

						} catch (InvocationTargetException e) {
							log.error("Error creating images", e);
							Status errSt = new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception creating Images", e);
							/*ErrorDialog.openError(editor.getSite().getShell(), "Error creating images",
									e.getMessage(), );*/
							redoingImages=false;
							return errSt;

						} catch (Exception e) {
							log.error("Error creating images", e);
							Status errSt = new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception creating Images", e);	redoingImages=false;
							redoingImages=false;
							return errSt;
						}
						
					}

				};
				job.setUser(true);
				job.schedule();
			}else{
				log.error("Empty image creation service");
				ErrorDialog.openError(editor.getSite().getShell(),"Empty Location" , "Image Creation Service loaction is empty",
						new Status(Status.ERROR, Activator.PLUGIN_ID, "Image Creation Service loaction is empty"));
			}
		}else
			MessageDialog.openInformation(getShell(), "Incompatible work in Progress", 
					"There is a deployment or another image creation job in progress");
	}

	private void sendFinishMessage(final String serviceID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getShell(), "Image Creation Finished", 
						"Job for creating the images of service " + serviceID + " has finished.");
			}
		});
	}

	/**
	 * Executes the creation of the service images invoking the 
	 * Image Creation service and installing the service packages
	 * 
	 * @param location 
	 * @param monitor Object to monitor the image creation progress
	 * @throws Exception 
	 */
	protected void executeImageCreation(String location, String repoPath, String rsyncPath, 
			String rshPath, String rshUsername, String rshKeyPath, IProgressMonitor monitor)
			throws InterruptedException, Exception {
		Manifest manifest = deployer.getManifest();
		if (manifest == null) {
			if (MessageDialog.openQuestion(getShell(), AsceticDeployer.CREATE_PACKS_DEF_TITLE, 
					editor.getProject().getProject().getName() + AsceticDeployer.CREATE_PACKS_DEF_QUESTION)){
				 deployer.packSection.generate();
				 manifest = deployer.getManifest();
			}else
				return;
		}
		ProjectMetadata prMeta = new ProjectMetadata(editor
				.getMetadataFile().getRawLocation().toFile());
		PackageMetadata packMeta = deployer.getPackageMetadata();
		GlobalConfiguration gc = new GlobalConfiguration();
		//Add LocalHost VMIC configuration
		gc.setHostAddress(location);
		gc.setRepositoryPath(repoPath);
		gc.setRsyncPath(rsyncPath);
		gc.setSshPath(rshPath);
		gc.setSshUser(rshUsername);
		gc.setSshKeyPath(rshKeyPath);
		VmicApi vmic = new VmicApi(gc);
		log.debug("ovf before uploading: "+ manifest.toString());
		manifest.setVMICMode("offline");
		uploadFiles(vmic, manifest, prMeta, packMeta, monitor);
		log.debug("ovf before generation: "+ manifest.toString());
		ImageCreation.generateImages(vmic, manifest, monitor);
		log.debug("ovf final: "+ manifest.toString());
		manifest.toFile();
	}


	private void uploadFiles(VmicApi vmic, Manifest manifest,
			ProjectMetadata prMeta, PackageMetadata packMeta,
			IProgressMonitor monitor) throws Exception {
		manifest.cleanFiles();
		manifest.cleanScripts();
		log.debug("Files cleaned: "+ manifest.getString());
		String[] allPacks = packMeta.getPackages();
		String[] oePacks = packMeta.getPackagesWithOrchestration();
		String[] cePacks = packMeta.getPackagesWithCores();
		IFolder packageFolder = editor.getProject().getProject().
				getFolder(OUTPUT_FOLDER).getFolder(PACKAGES_FOLDER);
		ImageCreation.generateConfigurationFiles(allPacks, packageFolder, prMeta, monitor);
		if (monitor.isCanceled()){
			throw new InterruptedException("Creation Cancelled");
		}
		if (oePacks != null && oePacks.length > 0) {
			for (String p : oePacks) {
				ImageCreation.uploadOrchestrationPackages(vmic, p, oePacks[0], allPacks,
						packageFolder, prMeta, packMeta, manifest, editor.getProject(), monitor);
			}
		}else{
			String projectName = editor.getProject().getProject().getName();
			ImageCreation.uploadOrchestrationPackages(vmic, projectName, projectName,
					allPacks, packageFolder, prMeta, packMeta, manifest,  editor.getProject(), monitor);
		}
		log.debug("Orchestration Files uploaded: "+ manifest.getString());
		if (cePacks != null && cePacks.length > 0) {
            for (String p : cePacks) {
                    ImageCreation.uploadCoreElementPackages(vmic, p, packageFolder, prMeta,
                           packMeta, manifest, editor.getProject(), monitor);
            }
		}
		log.debug("Cores Files uploaded: "+ manifest.getString());
    }


	public boolean isBlocking() {
		return redoingImages;
	}

	@Override
	protected void updateSectionProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void enableSectionProperties(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void resetSectionProperties() {
		// TODO Auto-generated method stub
		
	}
	
	public void init() {
		icsText.setText(deployer.getProperties().getICSLocation());
		rsyncPathText.setText(deployer.getProperties().getVMICRsyncPath());
		repoPathText.setText(deployer.getProperties().getVMICRepoPath());
		rshPathText.setText(deployer.getProperties().getVMICRshPath());
		usernameText.setText(deployer.getProperties().getVMICRshUsername());
		userKeyPathText.setText(deployer.getProperties().getVMICRshUserKeyPath());
	}

}
