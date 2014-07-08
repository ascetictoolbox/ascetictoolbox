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

import static es.bsc.servicess.ide.Constants.METADATA_FOLDER;
import static es.bsc.servicess.ide.Constants.OUTPUT_FOLDER;
import static es.bsc.servicess.ide.Constants.PACKAGES_FOLDER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import es.bsc.servicess.ide.model.ServiceElement;
import es.bsc.servicess.ide.views.DeployedApplicationSection;
import es.bsc.servicess.ide.views.DeploymentChecker;
import es.bsc.servicess.ide.views.ServiceDataComposite;
import es.bsc.servicess.ide.views.ServiceManagerView;
import eu.ascetic.saas.application_uploader.ApplicationUploader;


/**
 * Implements the Deployer class for the Optimis cloud
 * @author Jorge Ejarque (Barcelona Supercomputing Center)
 *
 */
public class AsceticDeployer extends Deployer {

	private boolean created;
	private boolean toBeUpdated;
	private AsceticProperties op_prop;
	
	//Manifest
	private Hyperlink link;
	/*Change to new manifest type*/
	private Manifest manifest;
	//*/
		
	private Section DS_section;
	private Text serverText;
	private Composite options;
		
	private static Logger log = Logger.getLogger(AsceticDeployer.class);

	
	private static final String PROPERTIES_FILENAME = "ascetic.properties";
	private static final String METADATA_FILENAME = "optimis-metadata.xml";
	public static final String CREATE_PACKS_DEF_TITLE = "Package Creation";
	public static final String CREATE_PACKS_DEF_QUESTION = " project has not packages created. Do you want to create it automatically?";
	private static final String CREATE_IMAGES_DEF_TITLE = "Image creation";;
	private static final String CREATE_IMAGES_DEF_QUESTION = " project has not images created. Do you want to create it automatically?";
	
	private File propFile;
	private File asceticMetaFile;
	private Text cliPropText;
	private Button cliPropButton;
	
	protected PackagesSection packSection;
	protected ImagesSection imageSection;
	protected AffinitySection affinitySection;
	
	
	public AsceticDeployer(){
		super();
		toBeUpdated = true;
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
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
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
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, asceticMetaFile, this);
		
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
		affinitySection.createComposite(composite);
		createDeploymentSection(composite);

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
			imageSection.setServiceLocation(op_prop.getICSLocation());
			serverText.setText(op_prop.getDSLocation());
	
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
	private void createDeploymentSection(Composite composite) {
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
	}


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
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(false, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						executeDeployment(monitor);
					} catch (Exception e) {
						throw (new InvocationTargetException(e));
					}
				}
			});
		} catch (InterruptedException e) {
			String message = e.getMessage();
			log.error("Error message: " + message, e);
			ErrorDialog.openError(super.getShell(), "Error",
					"Deploying the service: "+ message, new Status(IStatus.ERROR,Activator.PLUGIN_ID,
							message, e));
		} catch (InvocationTargetException e) {
			String message = e.getMessage();
			log.error("Error message: " + message,e);
			ErrorDialog.openError(super.getShell(), "Error",
					"Deploying the service: "+ message, new Status(IStatus.ERROR,Activator.PLUGIN_ID,
							message, e));
		}
	}

	/**
	 * Perform the service deployment contacting the Deployment service 
	 * 
	 * @param monitor Progress monitor
	 * @throws Exception 
	 */
	public void executeDeployment(IProgressMonitor monitor)
			throws Exception {
		//TODO Change to ascetic
		PackagingUtils.initPackageFolder(super.getProject(), monitor);
		ProjectMetadata prMetadata = new ProjectMetadata(super.getEditor()
				.getMetadataFile().getRawLocation().toFile());
		HashMap<String, ServiceElement> coreEls = CommonFormPage.getElements(
				prMetadata.getAllOrchestrationClasses(), PackageMetadata.CORE_TYPE, 
				super.getProject(), prMetadata);
		HashMap<String, ServiceElement> orchEls = CommonFormPage.getElements(
				prMetadata.getAllOrchestrationClasses(), PackageMetadata.ORCH_TYPE, 
				super.getProject(), prMetadata);
		String serviceID = editor.getProject().getProject().getName();
		// check if manifest already created
		if (manifest == null) {
			//Manifest not created so packages not generated.Ask if do it automatically
			if (MessageDialog.openQuestion(getShell(), CREATE_PACKS_DEF_TITLE, serviceID + CREATE_PACKS_DEF_QUESTION)){
				 packSection.generate();
			}else
				return;
		}
		PackageMetadata packMetadata = packSection.getPackageMetadata();
		//Check if packages contains all the elements defined
		if (PackagingUtils.checkAllElementsInPackages(orchEls.keySet(), coreEls.keySet(), prMetadata, packMetadata)){
		
			if (!manifest.hasImages()){
				if (MessageDialog.openQuestion(getShell(), CREATE_IMAGES_DEF_TITLE, serviceID + CREATE_IMAGES_DEF_QUESTION)){
					imageSection.createServiceImages();
				}else
					return;
			}
			manifest.setServiceId(serviceID);
			boolean executable = false;
			if (!prMetadata.getMainClass().isEmpty()){
				executable = true;
			}
			String location = serverText.getText().trim();
			if (location != null && location.length() > 0) {
				monitor.beginTask("Deploying application", 100);
				ApplicationUploader appUploader = new ApplicationUploader(location);
				String deploymentID = appUploader.createAndDeployAplication(manifest.getString());			
				monitorProgress(appUploader, serviceID, deploymentID, monitor);
				ServiceManagerView smview = (ServiceManagerView) PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("es.bsc.servicess.ide.views.ServiceManagerView");
				DeploymentChecker dc = new AsceticDeploymentChecker(appUploader, serviceID);
				DeployedApplicationSection das = new ServiceDataComposite(deploymentID, 
						dc, DeploymentChecker.PENDING, smview, executable, this.getShell());
				smview.addNewDeployement(deploymentID, das);
				monitor.done();
			} else {
				throw (new AsceticDeploymentException("Error incorrect location"));
			}
		}
		

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
	 */
	private void monitorProgress(ApplicationUploader appUploader, String applicationID,
			String deploymentID, IProgressMonitor monitor) throws AsceticDeploymentException {
		
		int retries = 0;
		int progress = 0;
		
		while (progress >= 0 && progress < 100 & retries < 30) {
			try{
				Thread.sleep(10000);
				String resp = appUploader.getDeploymentStatus(applicationID, deploymentID);
				if (resp.contains("ERROR")) {
					throw (new AsceticDeploymentException(resp));
				} else if (resp.contains("PROGRESS")) {
					int st = resp.indexOf(":", resp.indexOf("MESSAGE")) + 2;
					String prog = resp.substring(st, resp.indexOf("%", st));
					try {
						int new_progress = Integer.parseInt(prog);
						monitor.subTask(resp);
						monitor.worked(new_progress - progress);
						progress = new_progress;
						log.debug("Progressing...(" + progress + ")");
					} catch (Exception e) {
						log.error("Error getting progress from "
								+ prog + " Response is: " + resp);
						throw (new AsceticDeploymentException(
								"Error getting progress from " + prog
										+ ". Response is: " + resp));
					}
				} else {
					throw (new AsceticDeploymentException("Unknown response: "
							+ resp));

				}
			}catch(Exception e){
				throw (new AsceticDeploymentException(e));
			}

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
		try {
			manifest.toFile();
			IFile sm = getProject().getProject()
					.getFolder(OUTPUT_FOLDER)
					.getFolder(PACKAGES_FOLDER)
					.getFile(AsceticProperties.SERVICE_MANIFEST);
			IDE.openEditor(this.getWorkbenchPage(), sm);
		} catch (Exception e) {
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
			throws SAXException, IOException, ParserConfigurationException{
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
		return packSection.isBlocking()||imageSection.isBlocking();
	}
	
	

}