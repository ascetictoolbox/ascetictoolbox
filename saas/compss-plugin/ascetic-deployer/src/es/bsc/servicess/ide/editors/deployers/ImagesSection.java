package es.bsc.servicess.ide.editors.deployers;

import static es.bsc.servicess.ide.Constants.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;

public class ImagesSection extends ServiceEditorSection {

	//private File imageMetadataFile;
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
	private static final long CREATION_PULL_INTERVAL = 30000;
	
	
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
		final String location = icsText.getText().trim();
		if (location != null && location.length() > 0) {
			try {
				dialog.run(false, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						try {
							redoingImages=true;
							executeImageCreation( location, monitor);
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
		}else{
			log.error("Empty image creation service");
			ErrorDialog.openError(editor.getSite().getShell(),"Empty Location" , "Image Creation Service loaction is empty",
				 new Status(Status.ERROR, Activator.PLUGIN_ID, "Image Creation Service loaction is empty"));
		}
	}

	/**
	 * Executes the creation of the service images invoking the 
	 * Image Creation service and installing the service packages
	 * 
	 * @param location 
	 * @param monitor Object to monitor the image creation progress
	 * @throws Exception 
	 */
	protected void executeImageCreation(String location, IProgressMonitor monitor)
			throws Exception {
		Manifest manifest = deployer.getManifest();
		if (manifest == null) {
			if (MessageDialog.openQuestion(getShell(), AsceticDeployer.CREATE_PACKS_DEF_TITLE, 
					editor.getProject().getProject().getName() + AsceticDeployer.CREATE_PACKS_DEF_QUESTION)){
				 deployer.packSection.generate();
			}else
				return;
		}
		ProjectMetadata prMeta = new ProjectMetadata(editor
				.getMetadataFile().getRawLocation().toFile());
		PackageMetadata packMeta = deployer.getPackageMetadata();
		HashMap<String, ServiceElement> allEls = CommonFormPage.getElements(
				prMeta.getAllOrchestrationClasses(), BOTH_TYPE, 
				editor.getProject(), prMeta);
		
		//Add VMIC configuration
		GlobalConfiguration gc = new GlobalConfiguration();
		VmicApi vmic = new VmicApi(gc);
		uploadFiles(vmic, manifest, prMeta, packMeta, monitor);
		vmic.generateImage(manifest.getOVFDefinition());
		monitorProgress(vmic, monitor);	
	}

	private void monitorProgress(VmicApi vmic, IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Creating Images", 100);
		AbstractProgressData pd = vmic.progressCallback(editor.getProject().getProject().getName());
		while(!pd.isComplete()){	
			monitor.worked(pd.getCurrentPercentageCompletion().intValue());
			Thread.sleep(CREATION_PULL_INTERVAL);
			pd = vmic.progressCallback(editor.getProject().getProject().getName());
		}
		monitor.done();
	}

	private void uploadFiles(VmicApi vmic, Manifest manifest,
			ProjectMetadata prMeta, PackageMetadata packMeta,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
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
