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

import static es.bsc.servicess.ide.Constants.METADATA_FOLDER;
import static es.bsc.servicess.ide.Constants.OUTPUT_FOLDER;
import static es.bsc.servicess.ide.Constants.PACKAGES_FOLDER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import org.xml.sax.SAXException;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.PackagingUtils;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.BuildingDeploymentFormPage;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.Deployer;
import es.bsc.servicess.ide.editors.ServiceFormEditor;
import es.bsc.servicess.ide.editors.deployers.dialogs.AgreementSelectionDialog;
import es.bsc.servicess.ide.model.ServiceElement;
import es.bsc.servicess.ide.views.DeployedApplicationSection;
import es.bsc.servicess.ide.views.DeploymentChecker;
import es.bsc.servicess.ide.views.ServiceDataComposite;
import es.bsc.servicess.ide.views.ServiceManagerView;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;


/**
 * Implements the Deployer class for the Optimis cloud
 * @author Jorge Ejarque (Barcelona Supercomputing Center)
 *
 */
public class AsceticDeployer extends Deployer {

	private boolean created;
	private boolean toBeUpdated;
	private boolean generating;
	private AsceticProperties op_prop;
	
	//Manifest
	private Hyperlink link;
	/*Change to new manifest type*/
	private Manifest manifest;
	//*/
		
	/*private Section DS_section;
	private Text serverText;
	private Composite options;*/	
	private static Logger log = Logger.getLogger(AsceticDeployer.class);

	
	private static final String PROPERTIES_FILENAME = "ascetic.properties";
	private static final String METADATA_FILENAME = "optimis-metadata.xml";
	public static final String CREATE_PACKS_DEF_TITLE = "Package Creation";
	public static final String CREATE_PACKS_DEF_QUESTION = " project has not packages created. Do you want to create it automatically?";
	private static final String CREATE_IMAGES_DEF_TITLE = "Image creation";;
	private static final String CREATE_IMAGES_DEF_QUESTION = " project has not images created. Do you want to create it automatically?";
	
	public static final String BUNDLE_NAME = "ascetic.deployer.plugin";
	public static final String JSCH_JAR_NAME = "jsch-0.1.42.jar";
	
	
	private File propFile;
	private File asceticMetaFile;
	private Text cliPropText;
	private Button cliPropButton;
	
	protected PackagesSection packSection;
	protected ImagesSection imageSection;
	protected DeploymentSection deploymentSection;
	
	//protected AffinitySection affinitySection;
	
	
	public AsceticDeployer(){
		super();
		toBeUpdated = true;
		generating = false;
	}
	@Override
	public void bind(BuildingDeploymentFormPage page){
		super.bind(page);
		propFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(PROPERTIES_FILENAME).getRawLocation().toFile();
		asceticMetaFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(METADATA_FILENAME).getRawLocation().toFile();
		packSection = new PackagesSection(page.getToolkit(), page.getForm(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		imageSection = new ImagesSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile,this);
		/*
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		*/
		deploymentSection = new DeploymentSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, this);
	}
	
	public AsceticDeployer(ServiceFormEditor editor, IWorkbenchWindow window,
			BuildingDeploymentFormPage page) {
		super(editor, window, page);
		toBeUpdated = true;
		propFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(PROPERTIES_FILENAME).getRawLocation().toFile();
		asceticMetaFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(METADATA_FILENAME).getRawLocation().toFile();
		packSection = new PackagesSection(page.getToolkit(), page.getForm(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		imageSection = new ImagesSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		/*
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		*/
		deploymentSection = new DeploymentSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, this);
	}
	
	/* (non-Javadoc)
	 * @see es.bsc.servicess.ide.editors.Deployer#createComposite(org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.swt.widgets.Composite, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Composite createComposite(FormToolkit toolkit,
			Composite deploymentOptions, Composite old_composite) {
		composite = page.getToolkit().createComposite(deploymentOptions,
				SWT.NONE);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		// rd.grabExcessHorizontalSpace = true;
		composite.setLayoutData(rd);
		composite.setLayout(new GridLayout(1, false));
		packSection.createComposite(composite);
		imageSection.createComposite(composite);
		deploymentSection.createComposite(composite);
		//affinitySection.createComposite(composite);
		//createDeploymentSection(composite);

		link = toolkit.createHyperlink(composite, "View Service Manifest",
				SWT.NONE);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				openServiceManifest();
			}
		});
		link.setLayoutData(rd);

		created = true;
		if (old_composite != null) {
			old_composite.dispose();
		}
		old_composite = composite;
		return composite;
	}
	
	public void update() {
		if (toBeUpdated) {

		}
	}
	
	public void initiate() {
		// TODO add to be updated;
		try {
			op_prop = new AsceticProperties(propFile);
			if (getProject().getProject().getFolder(OUTPUT_FOLDER)
					.getFolder(PACKAGES_FOLDER).getLocation()
					.append(AsceticProperties.SERVICE_MANIFEST).toFile().exists())
				readManifestFromFile();
			packSection.init();
			imageSection.init();
			deploymentSection.init();
			//serverText.setText(op_prop.getDSLocation());
	
		} catch (Exception e) {
			log.error("Error loading service manifest file", e);
			
		}
	
	}


	/*private void updatePackagesInScope(Scope scope,
			ScopedListsComposite scopedList) {
		if (scope.getComponentIdArray()!=null)
		for (String s: scope.getComponentIdArray()){
			scope.removeComponentId(s);
		}
		for (String s: ManifestCreation.generateManifestNames(scopedList.getSelectedPackages())){
			scope.addComponentId(s);
		}
		
	}*/

	
	
	

	//***************************** Deployment section ****************************************
	/**
	 * Create the deployment section
	 * 
	 * @param composite Parents composite
	 */
	/*private void createDeploymentSection(Composite composite) {
		DS_section = page.getToolkit().createSection(composite,
				Section.TWISTIE | Section.DESCRIPTION | SWT.BORDER);
		DS_section.setText("Deployment Service");
		DS_section
				.setDescription("Define location and parameters for the Deployment Service");
		GridData rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		DS_section.setLayoutData(rd);
		DS_section.setLayout(new GridLayout(1, true));
		options = page.getToolkit().createComposite(DS_section, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		options.setLayout(new GridLayout(3, false));
		options.setLayoutData(rd);
		
		page.getToolkit().createLabel(options, "Application Manager", SWT.NONE);
		serverText = page.getToolkit().createText(options, "",
				SWT.SINGLE | SWT.BORDER);
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		serverText.setLayoutData(rd);
		serverText.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				op_prop.setDSLocation(serverText.getText().trim());
				try {
					op_prop.save();
				} catch (ConfigurationException e) {
					log.error("Exception svaing properties", e);
					ErrorDialog.openError(getShell(),
							"Saving optimis properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		DS_section.setClient(options);
		DS_section.setExpanded(true);
		DS_section.setExpanded(false);
	}*/


	/** 
	 * Generate the section name from prefix and number
	 * 
	 * @param number Number of section
	 * @param prefix Prefix of the section name
	 * @return
	 */
	public static String[] generateSectionNames(int number, String prefix) {
		String[] sections = new String[number];
		for (int i = 0; i < number; i++) {
			sections[i] = prefix + (i + 1);
		}
		return sections;
	}
	
	
	/**
	 * Get the number from a section name
	 * 
	 * @param prefix Section prefix
	 * @param name Section name
	 * @return
	 */
	public static int getNumberFromName(String prefix, String name) {
		int num = Integer.parseInt(name.substring(name.indexOf(prefix)
				+ prefix.length()));
		return num - 1;
	}

	/* (non-Javadoc)
	 * @see es.bsc.servicess.ide.editors.Deployer#deploy()
	 */
	@Override
	public void deploy() {
		generating = true;
		try{
			ProjectMetadata prMetadata = new ProjectMetadata(super.getEditor()
					.getMetadataFile().getRawLocation().toFile());
			HashMap<String, ServiceElement> coreEls = CommonFormPage.getElements(
					prMetadata.getAllOrchestrationClasses(), PackageMetadata.CORE_TYPE, 
					super.getProject(), prMetadata);
			HashMap<String, ServiceElement> orchEls = CommonFormPage.getElements(
					prMetadata.getAllOrchestrationClasses(), PackageMetadata.ORCH_TYPE, 
					super.getProject(), prMetadata);
			final String serviceID = editor.getProject().getProject().getName();
			// check if manifest already created
			if (manifest == null) {
				//Manifest not created so packages not generated.Ask if do it automatically
				if (MessageDialog.openQuestion(getShell(), CREATE_PACKS_DEF_TITLE, 
						serviceID + CREATE_PACKS_DEF_QUESTION)){
					packSection.generate();
				}else{
					generating =false;
					return;
				}
			}
			PackageMetadata packMetadata = packSection.getPackageMetadata();
			//Check if packages contains all the elements defined
			if (PackagingUtils.checkAllElementsInPackages(orchEls.keySet(), coreEls.keySet(), 
					prMetadata, packMetadata)){

				if (!manifest.hasImages()){
					if (MessageDialog.openQuestion(getShell(), CREATE_IMAGES_DEF_TITLE, 
							serviceID + CREATE_IMAGES_DEF_QUESTION)){
						imageSection.createServiceImages();
					}else{
						generating = false;
						return;
					}
				}
				manifest.setServiceId(serviceID);
				boolean executable = false;
				final String mainClass = prMetadata.getMainClass();
				if (mainClass!= null && !mainClass.isEmpty()){
					executable = true;
				}
				String location = deploymentSection.getServerLocation();
				if (location != null && !location.isEmpty()) {

					final ApplicationUploader appUploader = new ApplicationUploader(location);
					String monLoc = deploymentSection.getMonitorLocation();
					if (monLoc != null && !monLoc.isEmpty()) {

						manifest.setApplicationMonitorEPR(monLoc);
						deploymentSection.setDeploymentOptionsInManifest(manifest);
						final int vms = manifest.getVMsToDeploy();
						deploymentSection.setApplicationSecurityInManifest(manifest);
						
						manifest.setApplicationMangerEPR(location);
						final String ovf = manifest.getString();
						System.out.println(ovf);
						final boolean manual = deploymentSection.isManual();
						final boolean ex = executable;
						final AsceticDeployer deployer = this;
						//String deploymentID = "116";
						/*ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
						dialog.setBlockOnOpen(false);
						dialog.setCancelable(true);
							try {
								dialog.run(true, true, new IRunnableWithProgress() {*/
							
							Job job = new Job ("Deploying "+ serviceID ){
									@Override
									public IStatus run(IProgressMonitor monitor){
										//throws InvocationTargetException, InterruptedException {
										try {
											String deploymentID = Integer.toString(appUploader.createAndDeployAplication(ovf, manual));
											
											monitorProgress(appUploader, serviceID, deploymentID, vms, manual, monitor);
											
											openSMView(appUploader, serviceID, deploymentID, mainClass, ex, deployer);
											generating = false;
											return Status.OK_STATUS;
										} catch (ApplicationUploaderException e) {
											generating = false;
											String message = "Creation error:  " + e.getMessage();
											log.debug(message);
											return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error contacting the Application Manager", e );
										} catch (InterruptedException e) {
											generating = false;
											String message = "Deployment interrumped:  " + e.getMessage();
											log.debug(message);
											return Status.CANCEL_STATUS;
										} catch (AsceticDeploymentException e) {
											generating = false;
											String message = e.getCause().getMessage();
											log.error("Error monitoring: " + message,e.getCause());
											return new Status(IStatus.ERROR,Activator.PLUGIN_ID,
															message, e.getCause());
										}
									}
								};
							
								job.setUser(true);
								job.schedule();
							/*} catch (InterruptedException e) {
									generating = false;
									String message = "Deployment interrumped:  " + e.getMessage();
									log.debug(message);
									ErrorDialog.openError(getShell(), "Service deployment cancelled",
											"Deployment Cancelled.", new Status(IStatus.ERROR,Activator.PLUGIN_ID,
													message, e));
							} catch (InvocationTargetException e) {
									generating = false;
									String message = e.getCause().getMessage();
									log.error("Error message: " + message,e.getCause());
									ErrorDialog.openError(getShell(), "Error deploying the application",
											message, new Status(IStatus.ERROR,Activator.PLUGIN_ID,
													message, e.getCause()));
							}*/
					} else {
						generating = false;
						log.error("Error application monitor location not defined");
						ErrorDialog.openError(super.getShell(), "Error deploying the application",
								"Application Monitor location not defined",
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Incorrect Application Monitor location"));
					}

				} else {
					generating = false;
					log.error("Error application manager location not defined");
					ErrorDialog.openError(super.getShell(), "Error deploying the application",
							"Application Manager location not defined",
							new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									"Incorrect application manager location"));
				}
			}else{
				generating = false;
				log.error("Created packages don't include all the elements. " +
						"Review the resources properties or element constraints.\n");
				ErrorDialog.openError(super.getShell(), "Error deploying the application",
						"Created packages don't include all the elements.\n"
								+ "Review the resources properties or element constraints.", 
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Incorrect packaging."));
			}
		}catch(Exception e){
			generating = false;
			String message = e.getMessage();
			log.error("Execption when preparing the deployment ",e);
			ErrorDialog.openError(super.getShell(), "Error preparing the application deployment",
					"Execption when preparing the deployment.", 
					new Status(IStatus.ERROR,Activator.PLUGIN_ID, message, e));
		}
	}
	
	private void openSMView(final ApplicationUploader appUploader, final String serviceID, 
			final String deploymentID, final String mainClass, final boolean executable, 
			final AsceticDeployer deployer) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try{
					ServiceManagerView smview = (ServiceManagerView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("es.bsc.servicess.ide.views.ServiceManagerView");
					DeploymentChecker dc = new AsceticDeploymentChecker(appUploader, serviceID, 
						mainClass, null, serviceID, deployer);
					DeployedApplicationSection das = new ServiceDataComposite(deploymentID, 
						dc, DeploymentChecker.BOOTING, smview, executable, getShell());
					smview.addNewDeployement(deploymentID, das);
				} catch (PartInitException e) {
					generating = false;
					String message = e.getMessage();
					log.error("Error opening the Service Manager view ",e);
					ErrorDialog.openError(getShell(), "Error deploying the application",
							"Service was deployed but an error has occurred opening the Service Manager View", 
							new Status(IStatus.ERROR,Activator.PLUGIN_ID,message, e));
				}
			}
		});
	}
	

	/** 
	 * Monitor the progress of the service deployment process
	 * 
	 * @param appUploader ApplicationUploader object
	 * @param applicationID Application Identifier
	 * @param deploymentID Deployment Identifier
	 * @param monitor Progress monitor
	 * @throws InterruptedException
	 * @throws AsceticDeploymentException
	 * @throws ApplicationUploaderException 
	 */
	private void monitorProgress(final ApplicationUploader appUploader, final String applicationID,
			final String deploymentID, final int vms, final boolean manual, IProgressMonitor monitor) throws InterruptedException, AsceticDeploymentException, ApplicationUploaderException {
		
		
		/*ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		dialog.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {*/
				try {
					monitor.beginTask("Deploying application", 100);
					int retries = 0;
					int progress = 0;
					boolean accepted = false;
					while (progress >= 0 && progress < 100 & retries < 30) {
						if (monitor.isCanceled()){
							appUploader.undeploy(applicationID, deploymentID);
							throw new InterruptedException("Deployment Cancelled");
						}
						int new_progress = 0;
						Thread.sleep(10000);
						String resp = appUploader.getDeploymentStatus(applicationID, deploymentID);
						if (resp.contains(Dictionary.APPLICATION_STATUS_ERROR)) {
							throw (new AsceticDeploymentException(resp));
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_TERMINATED)) {
							throw (new AsceticDeploymentException("Deployment was canceled"));
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_SUBMITTED)) {
							new_progress = 5;
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_NEGOTIATING)) {
							new_progress = 10;
							
							//TODO: Check if it is the place to ask for accepting or rejecting the agreements
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_NEGOTIATIED)) {
							new_progress = 15;
							if(manual){
								if (!accepted){
									accepted = handleManualAgreement(appUploader, applicationID, deploymentID);
										if (!accepted){
											//TODO: Maybe this doesn't work
											//appUploader.undeploy(applicationID, deploymentID);
											return;
										}
								}									
								
							}
							log.debug("Agreement at negotiated:"+ appUploader.getDeploymentAgreements(applicationID, deploymentID));
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING)) {
							new_progress = 20;
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION)) {
							new_progress = 20;
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED)) {
							new_progress = 30;	
						} else if (resp.contains(Dictionary.APPLICATION_STATUS_DEPLOYING)) {
							new_progress = calculateProgress(appUploader, applicationID, deploymentID, vms);

						} else if (resp.contains(Dictionary.APPLICATION_STATUS_DEPLOYED)) {
							new_progress = 100;
						} else {
							throw (new AsceticDeploymentException("Unknown response: "
									+ resp));
						}
						monitor.subTask(resp);
						monitor.worked(new_progress - progress);
						progress = new_progress;
						log.debug("Progressing...(" + progress + ")");
						if (progress >= 100)
							monitor.done();
					}
				} catch (ApplicationUploaderException e) {
					//appUploader.undeploy(applicationID, deploymentID);
					throw (e);
					
				}
	/*		}
		});*/
	}

	private class ManualAgreementHandler implements Runnable{
		ApplicationUploader appUploader;
		String applicationID; 
		String deploymentID;
		boolean accepted = false;
		
		public ManualAgreementHandler(ApplicationUploader appUploader, String applicationID, String deploymentID){
			this.appUploader = appUploader;
			this.applicationID = applicationID;
			this.deploymentID = deploymentID;
		}
		@Override
		public void run() {
			try {
				List<Agreement> agrs = appUploader.getDeploymentAgreements(applicationID, deploymentID);
				AgreementSelectionDialog agreementSelection = new AgreementSelectionDialog(getShell(), agrs);
				agreementSelection.open();
				String selectedAgreement = agreementSelection.getSelectedAgreement();
				while(selectedAgreement == null){
					if (MessageDialog.openQuestion(getShell(), "No agreement selected", 
							" No agreement has been selected for acceptance it implies that the application "
									+ "deployment will be canceled. Do you want to proceed with the cancelation?")){
						accepted =false;
					}else{
						agreementSelection = new AgreementSelectionDialog(getShell(), agrs);
						agreementSelection.open();
						selectedAgreement = agreementSelection.getSelectedAgreement();
					}
				}
			
				appUploader.acceptAgreement(applicationID, deploymentID,Integer.toString(agrs.iterator().next().getId()));
				accepted = true;
			} catch (ApplicationUploaderException e) {
				log.error("Exception handling agreement.", e);
				e.printStackTrace();
				accepted = false;
			}
			
		}
		public boolean isAccepted() {
			return accepted;
		}
	}
	
	private boolean handleManualAgreement(final ApplicationUploader appUploader,
			final String applicationID, final String deploymentID) throws ApplicationUploaderException {
		
		ManualAgreementHandler mah = new ManualAgreementHandler(appUploader, applicationID, deploymentID);
		Display.getDefault().syncExec(mah); 
		return mah.isAccepted();
		
	}
	
	
	private int calculateProgress(ApplicationUploader appUploader,
			String applicationID, String deploymentID, int vms) {
		float vmIncrement = 70/vms;
		try{
			List<VM> vmDescs = appUploader.getDeploymentVMDescriptions(applicationID, deploymentID);
			if (vmDescs == null || vmDescs.isEmpty())
				return 30;
			log.debug("Deployed " + vmDescs.size() + " of " + vms+ " VMs");
			return (int) (30 +(vmIncrement*vmDescs.size()));
		}catch(Exception e){
			log.warn("Exception getting VMs");
			return 30;
		}
		
	}
	@Override
	public void diposeComposite() {
		/*
		 * link.dispose(); //sdo_location.dispose(); serverLabel.dispose();
		 * serverText.dispose(); ics_location.dispose(); icsLabel.dispose();
		 * icsText.dispose(); icsButton.dispose();
		 * trec_type_composite.dispose(); trec_type_composite.layout(true);
		 * trec_composite.dispose(); trec_composite.layout(true);
		 */
		// composite.dispose();
		// composite.layout(true);
	}

	/**
	 * Get all the generated service packages
	 * 
	 * @return Array of package names
	 */
	public String[] getAllPackages() {
		try {
			PackageMetadata packMeta = packSection.getPackageMetadata();
			String[] packs = packMeta.getPackages();
			if (packs!= null && packs.length>0){
				String[] oe_packs = packMeta.getPackagesWithOrchestration();
				String[] all_packs;
				if (oe_packs!=null&& oe_packs.length>0)
					all_packs = packs;
				else{
					all_packs = new String[packs.length + 1];
					all_packs[0] = editor.getProject().getProject().getName();
					for (int i = 0; i < packs.length; i++) {
						all_packs[i + 1] = packs[i];
					}
				}
				return all_packs;
			}else{
				log.warn("No elements found.");
				return new String[0];
			}
				
		} catch (Exception e) {
			log.error("Exception getting elements.",e);
			return new String[0];
		}
	}

	/**
	 * Open the current service manifest file for editing
	 */
	protected void openServiceManifest() {
		
		generating = true;
		try {
			if (manifest==null){
				final String serviceID = editor.getProject().getProject().getName();
				if (MessageDialog.openQuestion(getShell(), CREATE_PACKS_DEF_TITLE, 
						serviceID + CREATE_PACKS_DEF_QUESTION)){
					packSection.generate();
				}
			}
			manifest.toFile();
			IFile sm = getProject().getProject()
					.getFolder(OUTPUT_FOLDER)
					.getFolder(PACKAGES_FOLDER)
					.getFile(AsceticProperties.SERVICE_MANIFEST);
			IDE.openEditor(this.getWorkbenchPage(), sm);
			generating = false;
			
		} catch (Exception e) {
			generating = false;
			log.error("Exception opening manifest", e);
			ErrorDialog.openError(getShell(), "Opening service manifest",
					e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		} 
	}

	



	/**
	 * Read the service manifest form a existing file
	 * 
	 * @throws IOException
	 */
	protected void readManifestFromFile() throws IOException {
		log.debug("Reading manifest file");
		StringBuffer manifestData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(getProject()
				.getProject().getFolder(OUTPUT_FOLDER).getFolder(PACKAGES_FOLDER).getLocation()
				.append(AsceticProperties.SERVICE_MANIFEST).toFile()));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			manifestData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		manifest = Manifest.newInstance(getProject(), manifestData);
	}
	
	public PackageMetadata getPackageMetadata() 
			throws SAXException, IOException, ParserConfigurationException, TransformerException{
		return packSection.getPackageMetadata();
	}
	
	public Manifest getManifest(){
		return this.manifest;
	}
	
	public AsceticProperties getProperties(){
		return op_prop;
	}
	
	@Override
	public void updateComposite() {
		try {
			packSection.refreshData();
		} catch (PartInitException e) {
			log.error("Error refreshing data", e);
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean isBlocking() {
		return packSection.isBlocking()||imageSection.isBlocking()|| generating;
	}
	
	protected  void setManifest(PackageMetadata packMeta,
			HashMap<String, ServiceElement> allEls) throws Exception {
		this.manifest = Manifest.newInstance(getProject(), editor.getProjectMetadata(), packMeta, allEls, op_prop);
		this.manifest.toFile();
	}
	
	

}