package es.bsc.servicess.ide.editors.deployers;

import static es.bsc.servicess.ide.Constants.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.ImageMetadata;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.ServiceEditorSection;
import es.bsc.servicess.ide.editors.ServiceFormEditor;
import es.bsc.servicess.ide.model.ServiceElement;

public class ImagesSection extends ServiceEditorSection {

	private File imageMetadataFile;
	private boolean redoingImages;
	private Composite ics_location;
	private Text icsText;
	private AsceticDeployer deployer;
	private Button icsButton;
	private Combo icsMode;
	
	private static Logger log = Logger.getLogger(PackagesSection.class);
	
	private static final String DEFAULT_IMAGE_CREATION_MODE = "Default Mode";
	private static final String BROKER_IMAGE_CREATION_MODE = "Broker Optimization Mode";
	private static final String[] IMAGE_CREATION_MODES = 
			new String[]{DEFAULT_IMAGE_CREATION_MODE,BROKER_IMAGE_CREATION_MODE};
	public static final String IMAGE_CREATION_SEC_TITLE = "Image Creation";
	public static final String IMAGE_CREATION_SEC_DESC = "Define the options for the vm image creation process";
	
	
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
		this.imageMetadataFile = packageMetadataFile;
		this.deployer = deployer;
		this.redoingImages = false;
	}
	
	public void createSectionWidgets(Composite composite) {
		ics_location = toolkit.createComposite(composite,
				SWT.BORDER);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		ics_location.setLayoutData(rd);
		ics_location.setLayout(new GridLayout(3, false));
		toolkit.createLabel(ics_location, "Image Creation Service",
				SWT.NONE);
		icsText = toolkit.createText(ics_location, "",
				SWT.SINGLE | SWT.BORDER);
		icsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setICSLocation(icsText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Error modifiying optimis properties", e);
					ErrorDialog.openError(editor.getSite().getShell(),"Saving optimis properties", 
							e.getMessage(), new Status(IStatus.ERROR,Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		icsText.setLayoutData(rd);
		icsButton = toolkit.createButton(ics_location,
				"Create Images", SWT.NORMAL);
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
		toolkit.createLabel(ics_location, "Image Creation Mode",
				SWT.NONE);
		icsMode =  new Combo(ics_location, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		icsMode.setLayoutData(rd);
		icsMode.setItems(IMAGE_CREATION_MODES);
	}
	
	/**
	 * Invokes the runnable for creating the service images
	 */
	protected void createServiceImages() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(editor.getSite().getShell());
		try {
			dialog.run(false, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						redoingImages=true;
						executeImageCreation(monitor);
						redoingImages=false;
					} catch (Exception e) {
						redoingImages=false;
						throw (new InvocationTargetException(e));
					}
				}
			});
		} catch (InterruptedException e) {
			log.error("Error creating images", e);
			ErrorDialog.openError(editor.getSite().getShell(), "Error creating images",
					e.getMessage(), new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception creating Images", e));
		} catch (InvocationTargetException e) {
			log.error("Error creating images", e);
			ErrorDialog.openError(editor.getSite().getShell(), "Error creating images",
					e.getMessage(), new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception creating Images", e));
		}
	}

	/**
	 * Executes the creation of the service images invoking the 
	 * Image Creation service and installing the service packages
	 * 
	 * @param monitor Object to monitor the image creation progress
	 * @throws Exception 
	 */
	protected void executeImageCreation(IProgressMonitor monitor)
			throws Exception {
		String location = icsText.getText().trim();
		if (location != null && location.length() > 0) {
			Client c = Client.create();
			WebResource resource = c.resource(location);
			if (resource != null) {
				ProjectMetadata pr_meta = new ProjectMetadata(editor
						.getMetadataFile().getRawLocation().toFile());
				PackageMetadata packMeta = deployer.getPackageMetadata();
				ImageMetadata imgMeta = getImageMetadata();
				imgMeta.removeAllImages();
				HashMap<String, ServiceElement> allEls = CommonFormPage.getElements(
						pr_meta.getAllOrchestrationClasses(), BOTH_TYPE, 
						editor.getProject(), pr_meta);
				String[] allPacks = packMeta.getPackages();
				String[] oePacks = packMeta.getPackagesWithOrchestration();
				String[] cePacks = packMeta.getPackagesWithCores();
				IFolder packageFolder = editor.getProject().getProject().
						getFolder(OUTPUT_FOLDER).getFolder(PACKAGES_FOLDER);
				if (deployer.getManifest() == null) {
					deployer.generateNewManifest();
				}
				deployer.writeManifestToFile();
				if (icsMode.getItem(icsMode.getSelectionIndex()).
						equalsIgnoreCase(BROKER_IMAGE_CREATION_MODE)){
					log.debug("Broker Mode");
					String[] id_url;
					//Y2 ICS
					//String imageDescription = "OrchestrationElement";
					//Y3 ICS
					String imageDescription = ImageCreation.getFullImageDescription(pr_meta, allEls);
					//"<ImageTemplate><operatingSystem>CentOS</operatingSystem><imageSize>9</imageSize></ImageTemplate>";
					log.debug("Requesting image creation: " +imageDescription);
					if (oePacks != null && oePacks.length > 0){ 
						id_url = ImageCreation.createFullImage(resource, oePacks, allPacks, cePacks, 
								packageFolder, pr_meta,	packMeta, imageDescription, monitor);
					}else{
						log.debug("No oe packages, Creating single one by default.");
						id_url = ImageCreation.createFullImage(resource, new String[]{editor.getProject()
								.getProject().getName()}, allPacks, cePacks, packageFolder, pr_meta,
								packMeta, imageDescription, monitor);		
						imgMeta.addImage(id_url[0], id_url[1], editor.getProject()
							.getProject().getName());
					}
					if (allPacks != null && allPacks.length > 0) {
						for (String p : allPacks) {
							imgMeta.addImage(id_url[0], id_url[1], p);
						}
					}
				}else{
					log.debug("Default Mode");
					//String[] oePacks = pr_meta.getPackagesWithCores();
					if (oePacks != null && oePacks.length > 0) {
						for (String p : oePacks) {
							//Y2 ICS
							//String imageDescription = "OrchestrationElement";
							//Y3 ICS
							String imageDescription = ImageCreation.getImageDescription(pr_meta, packMeta, p, 
									allEls, true, editor.getProject());
							log.debug("Requesting image creation: " +imageDescription);
							String[] id_url;
							id_url = ImageCreation.createFrontEndImage(resource, p, oePacks[0], allPacks, 
									packageFolder, pr_meta, packMeta, imageDescription, monitor);
							imgMeta.addImage(id_url[0], id_url[1], p);
						}
					}else{
						//Y2 ICS
						//String imageDescription = "OrchestrationElement";
						//Y3 ICS
						String projectName = editor.getProject().getProject().getName();
						String imageDescription = ImageCreation.getImageDescription(pr_meta, packMeta, projectName
								,allEls, true,editor.getProject() );
						log.debug("Requesting image creation: " +imageDescription);
						String[] id_url = ImageCreation.createFrontEndImage(resource, projectName, projectName,
								allPacks, packageFolder, pr_meta, packMeta, imageDescription,monitor);
						imgMeta.addImage(id_url[0], id_url[1], editor.getProject().getProject().getName());
					}
					//String[] cePacks = pr_meta.getPackagesWithCores();
					if (cePacks != null && cePacks.length > 0) {
						for (String p : cePacks) {
							// create package image
							//Y2 ICS
							//String packImageDesc = "CoreElement";
							//Y3 ICS
							String packImageDesc = ImageCreation.getImageDescription(pr_meta, packMeta, p ,allEls, 
									false, editor.getProject());
							log.debug("Requesting image creation: " + packImageDesc);
							String[] id_url = ImageCreation.createPackageImage(resource, p, packageFolder, pr_meta, 
									packImageDesc, packMeta, monitor);
							imgMeta.addImage(id_url[0], id_url[1], p);
						}
					}
				}
				imgMeta.toFile(imageMetadataFile);
				deployer.getManifest().setImages(imgMeta.getImageURLPackagesMap());
			}
		}
	}
	
	public ImageMetadata getImageMetadata() throws SAXException, IOException, ParserConfigurationException{
		return new ImageMetadata(imageMetadataFile);
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

	public void setServiceLocation(String icsLocation) {
		icsText.setText(icsLocation);
		
	}

}
