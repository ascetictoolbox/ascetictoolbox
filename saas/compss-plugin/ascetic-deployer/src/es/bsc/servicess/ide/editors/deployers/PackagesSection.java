package es.bsc.servicess.ide.editors.deployers;

import static es.bsc.servicess.ide.Titles.*;
import static es.bsc.servicess.ide.Constants.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xml.sax.SAXException;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.PackageMetadata;
import es.bsc.servicess.ide.PackagingUtils;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.dialogs.PackageDialog;
import es.bsc.servicess.ide.editors.CommonFormPage;
import es.bsc.servicess.ide.editors.ServiceEditorSection;
import es.bsc.servicess.ide.editors.ServiceFormEditor;
import es.bsc.servicess.ide.model.ServiceElement;

public class PackagesSection extends ServiceEditorSection{

	private Button manualButton;
	private Button automaticButton;
	private ExpandableComposite manualPackComp;
	private Combo servicePackagesList;
	private Button deleteButton;
	private Text typeList;
	private List availElemList;
	private Button removeButton;
	private Button addButton;
	private List compElemList;
	private ScrolledForm form;
	private File packageMetadataFile;
	private boolean redoingPackages;
	
	private static Logger log = Logger.getLogger(PackagesSection.class);
	
	/** 
	 * Constructor
	 * @param form Parent's form
	 * @param toolkit Parent's toolkit
	 * @param shell Parent's shell
	 * @param editor Parent's editor
	 */
	public PackagesSection (FormToolkit toolkit, ScrolledForm form,
			ServiceFormEditor editor, int format, File packageMetadataFile) {
		super(toolkit,editor,"Application Packages", "This section provides the interface " +
				"to group Application Elements to build the Application Packages.", format);
		this.packageMetadataFile = packageMetadataFile;
		this.form = form;
	}
	
	public void createSectionWidgets(Composite composite) {
		GridLayout firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 1;
		composite.setLayout(firstRow1Layout);
		Composite firstRow = toolkit
				.createComposite(composite, SWT.NONE);
		firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 3;
		firstRow.setLayout(firstRow1Layout);
		toolkit.createLabel(firstRow, "Packaging Mode",
				SWT.BEGINNING);
		manualButton = toolkit
				.createButton(firstRow, "Manual Mode.", SWT.CHECK);
		manualButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				initManual(servicePackagesList.getText());
				expandManual();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				initManual(servicePackagesList.getText());
				expandManual();
			}
		});
		automaticButton = toolkit.createButton(firstRow, "Automatic Mode.",
				SWT.CHECK);
		// TODO TO be removed, capability is not currently supported
		automaticButton.setEnabled(true);
		automaticButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				compressManual();
				manualPackComp.setEnabled(false);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				compressManual();
				manualPackComp.setEnabled(false);
			}
		});
		manualPackComp = toolkit.createExpandableComposite(composite,
				ExpandableComposite.TREE_NODE
				| ExpandableComposite.CLIENT_INDENT);
		manualPackComp.setText("Manual Package creation");
		firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 1;
		manualPackComp.setLayout(firstRow1Layout);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		manualPackComp.setLayoutData(rd);
		Composite manual = toolkit.createComposite(manualPackComp, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		manual.setLayoutData(rd);
		firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 1;
		manual.setLayout(firstRow1Layout);
		Composite buttonRow = toolkit.createComposite(manual, SWT.NONE);
		rd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		// rd.grabExcessHorizontalSpace = true;
		buttonRow.setLayoutData(rd);
		firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 6;
		buttonRow.setLayout(firstRow1Layout);
		Label packLabel = toolkit.createLabel(buttonRow, "Package",
				SWT.BEGINNING);
		servicePackagesList = new Combo(buttonRow, SWT.READ_ONLY | SWT.BORDER
				| SWT.DEFAULT);
		rd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		servicePackagesList.setLayoutData(rd);
		servicePackagesList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectPackage();
			}
		});
		Button newPackageButton = toolkit.createButton(buttonRow, "New...",
				SWT.NONE);
		newPackageButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				createPackage();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createPackage();
			}
		});
		deleteButton = toolkit.createButton(buttonRow, "Delete", SWT.NONE);
		deleteButton.setEnabled(false);
		deleteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				deletePackage();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				deletePackage();
			}
		});
		Label typeLabel = toolkit.createLabel(buttonRow,"Elements Type",SWT.BEGINNING);

		typeList = new Text(buttonRow, SWT.READ_ONLY | SWT.BORDER |SWT.NONE); 
		typeList.setText("");
		Composite listsRow = toolkit.createComposite(manual, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		listsRow.setLayoutData(rd);
		firstRow1Layout = new GridLayout();
		firstRow1Layout.numColumns = 4;
		listsRow.setLayout(firstRow1Layout);
		availElemList = new List(listsRow, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		availElemList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addButton.setEnabled(true);
			}
		});
		rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		rd.grabExcessHorizontalSpace = true;
		rd.grabExcessVerticalSpace = true;
		rd.heightHint = 120;
		availElemList.setLayoutData(rd);
		removeButton = toolkit.createButton(listsRow, "<", SWT.NONE);
		rd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		removeButton.setLayoutData(rd);
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				removeElementFromPackage();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeElementFromPackage();
			}
		});
		addButton = toolkit.createButton(listsRow, ">", SWT.NONE);
		rd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		addButton.setLayoutData(rd);
		addButton.setEnabled(false);
		addButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				addElementToPackage();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addElementToPackage();
			}
		});
		compElemList = new List(listsRow, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		compElemList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeButton.setEnabled(true);
			}
		});
		rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		rd.grabExcessHorizontalSpace = true;
		rd.grabExcessVerticalSpace = true;
		rd.heightHint = 120;
		compElemList.setLayoutData(rd);
		manualPackComp.setClient(manual);
		manualPackComp.setExpanded(true);
		manualPackComp.setExpanded(false);
		Button generateButton = toolkit.createButton(composite,
				"Generate", SWT.NONE);
		rd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		generateButton.setLayoutData(rd);
		generateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				generate();
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				generate();
			}
		});

	}

	protected void compressManual() {
		manualButton.setSelection(false);
		automaticButton.setSelection(true);
		manualPackComp.setExpanded(false);
		mainComposite.redraw();
		form.reflow(true);
	}

	/**
	 * Initialize manual mode of packaging
	 * @param selectedpack selected package
	 * @return True is no error, false if error
	 */
	public boolean initManual(String selectedpack) {
		try {
			ProjectMetadata pr_meta = new ProjectMetadata(
					editor.getMetadataFile()
					.getRawLocation().toFile());
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			String[] packs = packMeta.getPackages();
			if (packs != null && packs.length > 0) {
				initManual(packMeta, packs, selectedpack);
				return true;
			} else
				return false;
		} catch (Exception e) {
			log.error("Error updating manual composite");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Initialize the manual mode
	 * 
	 * @param packMeta Package metadata
	 * @param packs Existing packages
	 * @param selectedpack Selected packages
	 * @throws Exception 
	 */
	public void initManual(PackageMetadata packMeta, String[] packs,
			String selectedpack) throws Exception {
		servicePackagesList.setItems(packs);
		if (selectedpack != null && selectedpack.length() > 0) {
			servicePackagesList.setText(selectedpack);
			updatePackage(selectedpack, packMeta);
		} else {
			setNoPackage();
		}
	}
	
	/**
	 * Expand the manual part
	 */
	protected void expandManual() {
		manualButton.setSelection(true);
		automaticButton.setSelection(false);
		manualPackComp.setExpanded(true);
		manualPackComp.setEnabled(true);
		//servicePackageSection.getParent().redraw();
		form.reflow(true);
	}
	
	/**
	 * Actions when selecting a package
	 */
	protected void selectPackage() {
		try {
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			String selectedpack = servicePackagesList.getText().trim();
			updatePackage(selectedpack, packMeta);
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite().getShell(),
					"Error selecting package", e.getMessage(), 
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}
	}
	
	/**
	 * Update package
	 * 
	 * @param selectedpack Selected package name
	 * @param pr_meta Project Metadata
	 * @throws Exception 
	 */
	private void updatePackage(String selectedpack, PackageMetadata packMeta)
			throws Exception {
		if (packMeta.existsPackage(selectedpack)) {
			deleteButton.setEnabled(true);
			String type = packMeta.getPackageType(selectedpack);
			typeList.setText(type);
			if (type.equals(SER_CORE_PACK_TYPE)){
				type = SERVICE_TYPE;
			}else if (type.equals(METH_CORE_PACK_TYPE)){
				type = METHOD_TYPE;
			}else if (type.equals(ORCH_PACK_TYPE)){
				type = ORCH_TYPE;
			}else if (type.equals(ALL_PACK_TYPE)){
				type= BOTH_TYPE;
			}else
				type= BOTH_TYPE;
			//TODO change constraints element have to set all elements
			ProjectMetadata pr_meta = editor.getProjectMetadata();
			HashMap<String, ServiceElement> constElements = CommonFormPage.getElements(
				pr_meta.getAllOrchestrationClasses(), type, editor.getProject(), pr_meta);
			if (constElements != null)
				updateLists(packMeta, selectedpack, constElements);
		} else {
			setNoPackage();
		}
	}
	
	/**
	 * Unselect packages
	 */
	private void setNoPackage() {
		servicePackagesList.setText("");
		availElemList.setItems(new String[0]);
		compElemList.setItems(new String[0]);
		addButton.setEnabled(false);
		removeButton.setEnabled(false);
		deleteButton.setEnabled(false);
		typeList.setText("");
		typeList.setEnabled(false);
	}
	
	/** 
	 * Update element lists
	 * @param pr_meta Project Metadata
	 * @param selectedpack Selected Package
	 * @param constElements 
	 */
	private void updateLists(PackageMetadata packMeta, String selectedpack, HashMap<String, ServiceElement> constElements) {
		String[] elementsInPackage = packMeta.getElementsInPackage(selectedpack);
		compElemList.setItems(elementsInPackage);
		availElemList.setItems(getMissingElements(constElements, elementsInPackage));
	}

	/** 
	 * Get missing elements 
	 * @param constElements 
	 * @param elementsInPackage Current elements in package
	 * @return packages not selected
	 */
	private String[] getMissingElements(HashMap<String, ServiceElement> constElements, String[] elementsInPackage) {
		if (constElements != null && !constElements.isEmpty()) {
			Set<String> str = new HashSet<String>();
			str.addAll(constElements.keySet());
			for (String s : elementsInPackage) {
				str.remove(s);
			}
			return str.toArray(new String[str.size()]);
		} else {
			log.error("There are no elements to show");
			return new String[0];
		}
	}
	
	/**
	 * Create a package
	 */
	protected void createPackage() {
		String pName;
		try {
			
			PackageDialog dialog = new PackageDialog(editor.getSite().getShell(), 
					SUPPORTED_PACK_TYPES);
			if (dialog.open() == Window.OK) {
				pName = dialog.getPackageName();
				PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
				packMeta.addPackage(pName, dialog.getType());
				packMeta.toFile(packageMetadataFile);
				servicePackagesList.setText(pName);
				typeList.setText(dialog.getType());
				initManual(packMeta, packMeta.getPackages(), pName);
			}
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite().getShell(),
					"Error creating package", e.getMessage(),new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete a package
	 */
	protected void deletePackage() {
		try {
			String pName = servicePackagesList.getItem(servicePackagesList
					.getSelectionIndex());
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			packMeta.removePackage(pName);
			packMeta.toFile(packageMetadataFile);
			servicePackagesList.setText("");
			initManual(packMeta, packMeta.getPackages(), null);
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite().getShell(),
					"Error deleting package", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}

	}
	
	/**
	 * Add an element in a package
	 */
	protected void addElementToPackage() {
		try {
			String pName = servicePackagesList.getItem(servicePackagesList
					.getSelectionIndex());
			String[] compNames = availElemList.getSelection();
			ProjectMetadata pr_meta = editor.getProjectMetadata();
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			for (String compName : compNames) {
				packMeta.addElementToPackage(pName, compName);
			}
			packMeta.toFile(packageMetadataFile);
			String type = typeList.getText();
			if (type.equals(SER_CORE_PACK_TYPE)){
				type=SERVICE_TYPE;
			}else if (type.equals(METH_CORE_PACK_TYPE)){
				type=METHOD_TYPE;
			}else if (type.equals(ORCH_PACK_TYPE)){
				type=ORCH_TYPE;
			}else if (type.equals(ALL_PACK_TYPE)){
				type=BOTH_TYPE;
			}else
				type=BOTH_TYPE;
			//TODO Check if all Orchestration or only internal
			HashMap<String, ServiceElement> constElements = CommonFormPage.getElements(
				pr_meta.getAllOrchestrationClasses(),type ,editor.getProject(), pr_meta);
			if (constElements != null)
				updateLists(packMeta, pName, constElements);
			else
				throw(new Exception("No elements for this type"));
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite().getShell(),
					"Error adding element to package", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}

	}

	/**
	 * Remove element in a package
	 */
	protected void removeElementFromPackage() {
		try {
			String pName = servicePackagesList.getItem(servicePackagesList
					.getSelectionIndex());
			String[] compNames = compElemList.getSelection();
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			for (String compName : compNames) {
				packMeta.removeElementFromPackage(pName, compName);
			}
			packMeta.toFile(packageMetadataFile);
			String type = typeList.getText();
			if (type.equals(SER_CORE_PACK_TYPE)){
				type=SERVICE_TYPE;
			}else if (type.equals(METH_CORE_PACK_TYPE)){
				type=METHOD_TYPE;
			}else if (type.equals(ORCH_PACK_TYPE)){
				type=ORCH_TYPE;
			}else if (type.equals(ALL_PACK_TYPE)){
				type=BOTH_TYPE;
			}else
				type=BOTH_TYPE;
			//TODO Check if all orchestration or only internal
			ProjectMetadata pr_meta = editor.getProjectMetadata();
			HashMap<String, ServiceElement> constElements = CommonFormPage.getElements(
				pr_meta.getAllOrchestrationClasses(),type ,editor.getProject(), pr_meta);
			if (constElements != null)
				updateLists(packMeta, pName, constElements);
			else
				throw(new Exception("No elements for this type"));
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite().getShell(),
					"Error deleting element from package", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate the service package
	 */
	protected void generate() {
		if (automaticButton.getSelection()) {
			redoingPackages= true;
			definePackagesAutomatically();
		}
		buildPackages();
		redoingPackages=false;

	}
	
	/**
	 * Define packages automatically
	 */
	private void definePackagesAutomatically() {
		try {
			ProjectMetadata pr_meta = editor.getProjectMetadata();
			PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
			packMeta.removeAllPackages();
			servicePackagesList.removeAll();
			boolean packCreated = false;
			//Orchestration Types
			//TODO check 
			HashMap<String, ServiceElement> oeEls = CommonFormPage.getElements(
					pr_meta.getAllOrchestrationClasses(), ORCH_TYPE, editor.getProject(), pr_meta);
			if (oeEls != null && oeEls.size()>0){
				Map<String, ArrayList<String>> oePacks = PackagingUtils.automaticPackagesCalculation(pr_meta, oeEls, 
						editor.getProject().getProject().getName());
				if (oePacks != null && oePacks.size()>0){

					for (Entry<String, ArrayList<String>> e: oePacks.entrySet()){
						log.debug("Creating package " + e.getKey());
						packMeta.addPackage(e.getKey(), ORCH_PACK_TYPE);
						servicePackagesList.add(e.getKey());
						packCreated = true;
						for (String compName: e.getValue()){
							log.debug("Adding element " + compName);
							packMeta.addElementToPackage(e.getKey(), compName);
						}	
					}	
				}else{
					ErrorDialog.openError(editor.getSite().getShell(),
							"Error creating packages","No orchestration packages created and there are method elements", 
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Review service core elements constraints inconsitencies"));
					log.warn(" No orchestration packages created");
				}
			}else
				log.debug(" No method elements to do");

			//Method Core Types	
			//TODO check if all orchestration or only internal
			HashMap<String, ServiceElement> meEls = CommonFormPage.getElements(
					pr_meta.getAllOrchestrationClasses(), METHOD_TYPE, editor.getProject(), pr_meta);
			if (meEls != null && meEls.size()>0){
				Map<String, ArrayList<String>> mePacks = PackagingUtils.automaticPackagesCalculation(pr_meta, meEls, "autoMethod");
				if (mePacks != null && mePacks.size()>0){

					for (Entry<String, ArrayList<String>> e: mePacks.entrySet()){
						log.debug("Creating package " + e.getKey());
						packMeta.addPackage(e.getKey(), METH_CORE_PACK_TYPE);
						servicePackagesList.add(e.getKey());
						packCreated = true;
						for (String compName: e.getValue()){
							log.debug("Adding element " + compName);
							packMeta.addElementToPackage(e.getKey(), compName);
						}	
					}	
				}else{
					ErrorDialog.openError(editor.getSite()
							.getShell(),
							"Error creating packages","No method packages created and there are method elements", 
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Review service core elements constraints inconsitencies"));
					log.warn(" No method package created");
				}
			}else
				log.debug(" No method elements to do");
			//Services
			//TODO Check if all orchestration classes or only internal
			HashMap<String, ServiceElement> seEls = CommonFormPage.getElements(
					pr_meta.getAllOrchestrationClasses(), SERVICE_TYPE, editor.getProject(), pr_meta);
			if (seEls!= null && seEls.size()>0){
				Map<String, ArrayList<String>> sePacks = PackagingUtils.automaticPackagesCalculation(pr_meta, seEls, "autoService");
				if (sePacks != null && sePacks.size()>0){
					for (Entry<String, ArrayList<String>> e: sePacks.entrySet()){
						packMeta.addPackage(e.getKey(), SER_CORE_PACK_TYPE);
						servicePackagesList.add(e.getKey());
						packCreated = true;
						for (String compName: e.getValue()){
							packMeta.addElementToPackage(e.getKey(), compName);
						}
					}
				}else{
					ErrorDialog.openError(editor.getSite().getShell(),
							"Error creating packages","No service packages created and there are service elements", 
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Review service core elements constraints inconsitencies"));
					log.warn(" No service package created");
				}
			}else
				log.debug(" No service elements");
			packMeta.toFile(packageMetadataFile);
			if ((packCreated)){
				initManual(servicePackagesList.getItem(0));
				expandManual();
			}/*else{
					ErrorDialog.openError(this.getServiceEditor().getSite()
							.getShell(),
							"Error creating packages","No packages created", 
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Review core element constraints inconsitencies"));
				}*/
		} catch (Exception e) {
			ErrorDialog.openError(editor.getSite()
					.getShell(),
					"Error creating packages","No packages created", 
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			e.printStackTrace();
		}

	}

	/**
	 * Build the service packages
	 */
	private void buildPackages() {
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				editor.getSite().getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				/* (non-Javadoc)
				 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					 
					try {
						ImageCreation.generateConfigurationFiles("one", "one", new String[]{"packs"}, editor.getProject().getProject().getFolder(OUTPUT_FOLDER), editor.getProjectMetadata(), monitor);
						PackagingUtils.buildPackages(editor.getProject(), editor.getProjectMetadata(),
						new PackageMetadata(packageMetadataFile), monitor);
						
					} catch (Exception e) {
						throw (new InvocationTargetException(e));
					}
				}
				
			});
		} catch (InvocationTargetException e) {
			ErrorDialog.openError(dialog.getShell(),
					"Error creating packages", "Exception during process execution", new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getCause().getMessage(),e.getCause()));
			e.printStackTrace();
		} catch (InterruptedException e) {
			ErrorDialog.openError(dialog.getShell(), "Building interrumped",
					e.getMessage(),new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
			e.printStackTrace();
		}
	}
	
	public void refreshData() throws PartInitException {	
			try{
				ProjectMetadata pr_meta = editor.getProjectMetadata();
				PackageMetadata packMeta = new PackageMetadata(packageMetadataFile);
				String[] orch_cls = pr_meta.getAllOrchestrationClasses();
				HashMap<String, ServiceElement> constraintsElements = CommonFormPage.getElements(orch_cls,
						BOTH_TYPE, editor.getProject(), pr_meta);
					if (hasPackagedElementsChanged(packMeta, constraintsElements)){
						if (!redoingPackages){
							if (!MessageDialog.openQuestion(editor.getSite().getShell(), "Changes in Core Element interfaces", 
									"An update in the Core Element interface has been produced which could " +
									"invalidate current packages. Do you want to keep current packages?")){
								packMeta.removeAllPackages();
							}
						}
					}
			}catch (Exception e){
				throw (new PartInitException(
							"Error loading configuration properties", e));
			}
	}

	/**
	 * Checks if a elements of the packages are in a list of core elements
	 * @param pr_meta Project metadata for extracting the packages and elements per package
	 * @param constraintsElements 
	 * @param packMeta 
	 * @return True if there are elements which does not exists
	 */
	private boolean hasPackagedElementsChanged(PackageMetadata packMeta, 
			HashMap<String, ServiceElement> constraintsElements) {
		String[] packs = packMeta.getPackages();
		if (packs!= null && packs.length>0){
			for (String p:packs){
				String[] els = packMeta.getElementsInPackage(p);
				for(String e:els){
					if (!constraintsElements.containsKey(e)){
						log.debug("Element " + e + "in package "+ p +" not found.");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isBlocking(){
		return redoingPackages;
	}
	
	public PackageMetadata getPackageMetadata() throws SAXException, IOException, ParserConfigurationException{
		return new PackageMetadata(packageMetadataFile);
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
	
	
}
