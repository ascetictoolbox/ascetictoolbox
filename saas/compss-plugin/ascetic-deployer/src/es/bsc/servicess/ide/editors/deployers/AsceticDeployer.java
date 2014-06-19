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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import static es.bsc.servicess.ide.Constants.*;
import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.ImageMetadata;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.BuildingDeploymentFormPage;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.Deployer;
import es.bsc.servicess.ide.editors.KeyValueTableComposite;
import es.bsc.servicess.ide.editors.SaveResetButtonComposite;
import es.bsc.servicess.ide.editors.ScopedListsComposite;
import es.bsc.servicess.ide.editors.ServiceFormEditor;
import es.bsc.servicess.ide.model.ServiceElement;
import es.bsc.servicess.ide.views.DeployedApplicationSection;
import es.bsc.servicess.ide.views.DeploymentChecker;
import es.bsc.servicess.ide.views.ServiceDataComposite;
import es.bsc.servicess.ide.views.ServiceManagerView;


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
	
	private File propFile;
	private File optimisMetaFile;
	private Text cliPropText;
	private Button cliPropButton;
	
	private PackagesSection packSection;
	private ImagesSection imageSection;
	private AffinitySection affinitySection;

	public AsceticDeployer(){
		super();
		toBeUpdated = true;
	}
	@Override
	public void bind(BuildingDeploymentFormPage page){
		super.bind(page);
		propFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(PROPERTIES_FILENAME).getRawLocation().toFile();
		optimisMetaFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(METADATA_FILENAME).getRawLocation().toFile();
		packSection = new PackagesSection(page.getToolkit(), page.getForm(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile);
		imageSection = new ImagesSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile,this);
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile, this);
	}
	
	public AsceticDeployer(ServiceFormEditor editor, IWorkbenchWindow window,
			BuildingDeploymentFormPage page) {
		super(editor, window, page);
		toBeUpdated = true;
		propFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(PROPERTIES_FILENAME).getRawLocation().toFile();
		optimisMetaFile = editor.getProject().getProject().getFolder(METADATA_FOLDER)
				.getFile(METADATA_FILENAME).getRawLocation().toFile();
		packSection = new PackagesSection(page.getToolkit(), page.getForm(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile);
		imageSection = new ImagesSection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile, this);
		affinitySection = new AffinitySection(page.getToolkit(), editor, 
				Section.TWISTIE | Section.DESCRIPTION, optimisMetaFile, this);
		
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
			else
				generateNewManifest();
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
		
		String serviceID = editor.getProject().getProject().getName();
		// TODO: Check if images created
		// check if manifest already created
		if (manifest == null) {
			generateNewManifest();
		}
		manifest.setServiceId(serviceID);

		String location = serverText.getText().trim();
		if (location != null && location.length() > 0) {
			monitor.beginTask("Deploying application", 100);
			Client c = Client.create();
			WebResource resource = c.resource(location);
			if (resource != null) {
				/*TODO Make call to application manager to deploy the service and Check the progress
				ClientResponse response = resource.path("deploy").post(
						ClientResponse.class, part);
				if ((response.getStatus() >= 200)
						&& (response.getStatus() < 300)) {
					// Check the progress
					monitorProgress(resource, serviceID, monitor);
					// response =
					// resource.path(serviceID).path("status").get(ClientResponse.class);

				} else {
					log.debug("Response: " + response.toString()
							+ "\nreason: " + response.getEntity(String.class));
					throw (new AsceticDeploymentException(
							"Error deploying service because of "
									+ response.getEntity(String.class)));
				}*/
				// Add the view
				ServiceManagerView smview = (ServiceManagerView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("es.bsc.servicess.ide.views.ServiceManagerView");
				DeploymentChecker dc = new AsceticDeploymentChecker(op_prop.getSMLocation());
				DeployedApplicationSection das = new ServiceDataComposite(serviceID, 
						dc, DeploymentChecker.PENDING, smview, false, this.getShell());//always services
				smview.addNewDeployement(serviceID, das);

			} else {
				throw (new AsceticDeploymentException(
						"Error creating SD client"));
			}
			monitor.done();
		} else {
			throw (new AsceticDeploymentException("Error incorrect location"));
		}

	}

	/** 
	 * Monitor the progress of the service deployment process
	 * 
	 * @param resource Deployment Service web resource URL
	 * @param serviceID Service Identifier
	 * @param monitor Progress monitor
	 * @throws InterruptedException
	 * @throws AsceticDeploymentException
	 */
	private void monitorProgress(WebResource resource, String serviceID,
			IProgressMonitor monitor) throws InterruptedException,
			AsceticDeploymentException {
		/*
		 *  TODO make monitoring progress with Application Manager
		int retries = 0;
		int progress = 0;
		ClientResponse response;
		while (progress >= 0 && progress < 100 & retries < 30) {
			Thread.sleep(10000);
			response = resource.path(serviceID).path("status")
					.get(ClientResponse.class);
			if (response.getStatus() == com.sun.jersey.api.client.ClientResponse.Status.OK.getStatusCode()) {
				String resp = response.getEntity(String.class);
				if (resp.contains("ERROR")) {
					throw (new AsceticDeploymentException(resp));
				} else if (resp.contains("PROGRESS")) {
					int st = resp.indexOf(":", resp.indexOf("MESSAGE")) + 2;
					String prog = resp.substring(st, resp.indexOf("%", st));
					try {
						int new_progress = Integer.parseInt(prog);
						if (new_progress <= 40) {
							monitor.subTask("Evaluating TREC and Selecting Provider");
						} else if (new_progress > 40 && new_progress <= 45) {
							monitor.subTask("Contectualizing service VM");
						} else if (new_progress > 45 && new_progress < 95) {
							monitor.subTask("Uploading images");
						} else if (new_progress >= 95 && new_progress < 100) {
							monitor.subTask("Creating Agreement");
						}
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
			} else {
				throw (new AsceticDeploymentException(
						"Error getting service deployment status: "
								+ response.toString() + "\nreason: "
								+ response.getEntity(String.class)));
			}
		}
		*/
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
			writeManifestToFile();
			IFile sm = getProject().getProject()
					.getFolder(OUTPUT_FOLDER)
					.getFolder(PACKAGES_FOLDER)
					.getFile(AsceticProperties.SERVICE_MANIFEST);
			IDE.openEditor(this.getWorkbenchPage(), sm);
		} catch (PartInitException e) {
			log.error("Exception opening manifest", e);
			ErrorDialog.openError(getShell(), "Opening service manifest",
					e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/**
	 * Generate a new service manifest
	 * @throws Exception 
	 */
	protected void generateNewManifest() throws Exception {
		
		/* TODO Generate ascetic manifest*/
		ProjectMetadata pr_meta = new ProjectMetadata(super.getEditor()
				.getMetadataFile().getRawLocation().toFile());
		HashMap<String, ServiceElement> allEls = CommonFormPage.getElements(
				pr_meta.getAllOrchestrationClasses(), BOTH_TYPE, super.getProject(), pr_meta);
		//String frontend_id = ManifestCreation.generateManifestName(editor.getProject().getProject().getName());
		InputStream in = this.getClass().getResourceAsStream(
				"sm_default.properties");
		Properties properties = new Properties();
		properties.load(in);
		in.close();
		
		Manifest oldManifest = manifest;
		PackageMetadata packMeta = packSection.getPackageMetadata();
		String[] oePacks = packMeta.getPackagesWithOrchestration();
		if (oePacks == null || oePacks.length <= 0) {
			oePacks = new String[]{editor.getProject().getProject().getName()};
		}
			
			manifest = Manifest.newInstance(editor.getProject().getProject().getName());
			for (String p : oePacks) {
				log.debug("Creating Component for package " + p );
				String componentID = Manifest.generateManifestName(p);
				manifest.setComponentDescription(componentID, pr_meta, packMeta, p, super.getProject(), 
					 allEls, false, this.op_prop);
				
			
			}
		String[] cePacks = packMeta.getPackagesWithCores();
		if (cePacks != null && cePacks.length > 0) {
			for (String p : cePacks) {
				log.debug("Creating Component for package " + p );
				String componentID = Manifest.generateManifestName(p);
				
				manifest.setComponentDescription(componentID, pr_meta, packMeta, p, super.getProject(), 
					allEls, false, this.op_prop);
				
			}
		}else{
			log.warn("No packages found generating only master");
		}
		
		manifest.setImages(getImagesMetadata().getImageURLPackagesMap());
		
		writeManifestToFile();
	}

	/**
	 * Write the service manifest to a file
	 */
	protected void writeManifestToFile() {
		try {
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					getShell());
			final IFile sm = getProject().getProject().getFolder(OUTPUT_FOLDER)
					.getFolder(PACKAGES_FOLDER).getFile(AsceticProperties.SERVICE_MANIFEST);
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					try {
						if (sm.exists()) {
							sm.delete(true, monitor);
						}
						if (manifest == null) {
							generateNewManifest();
						}
						log.debug("writing the manifest in the file ");
						sm.create(new ByteArrayInputStream(manifest.toString()
								.getBytes()), true, monitor);
					} catch (Exception e) {
						log.debug("Exception writing manifest");
						throw (new InvocationTargetException(e));
					}
				}
			});
		} catch (InvocationTargetException e) {
			log.error("Exception writing manifest", e);
			ErrorDialog.openError(getShell(), "Error writing manifest",
					e.getMessage(),new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invocation Exception", e) );
		} catch (InterruptedException e) {
			log.error("Exception writing manifest", e);
			ErrorDialog.openError(getShell(), "Building interrumped",
					e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Interruption Exception", e));
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
		manifest = Manifest.newInstance(manifestData);
	}
	
	public PackageMetadata getPackageMetadata() 
			throws SAXException, IOException, ParserConfigurationException{
		return packSection.getPackageMetadata();
	}
	
	public ImageMetadata getImagesMetadata() 
			throws SAXException, IOException, ParserConfigurationException{
		return imageSection.getImageMetadata();
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