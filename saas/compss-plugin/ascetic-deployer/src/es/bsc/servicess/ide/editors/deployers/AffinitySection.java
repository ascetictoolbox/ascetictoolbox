package es.bsc.servicess.ide.editors.deployers;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import es.bsc.servicess.ide.editors.ISectionSaverCleaner;
import es.bsc.servicess.ide.editors.SaveResetButtonComposite;
import es.bsc.servicess.ide.editors.ScopedListsComposite;
import es.bsc.servicess.ide.editors.ServiceEditorSection;
import es.bsc.servicess.ide.editors.ServiceFormEditor;

public class AffinitySection extends ServiceEditorSection implements ISectionSaverCleaner {

	
	private File metadataFile;
	private AsceticDeployer deployer;
	
	//Affinity
	public final static String AF_SEC_TITLE = "Affinity and Anti-affinity Rules";
	public final static String AF_SEC_DESC = "Define the Affinity and Anti-affinity rules " +
			"between the different components of a service and the different " +
			"instances of the components.";
	
	protected final static String AFFINITY = "AFF";
	protected final static String INTRA_AF = "INTRA_AF";
	protected final static String ANTIAFFINITY = "ANTIAFF";
	protected final static String INTRA_ANTIAF = "INTRA_ANTIAF";
	private static final String AFFINITY_RULE = "Affinity Rule ";
	private static final String ANTI_AFFINITY_RULE = "Anti-affinity Rule ";
	private Combo af_level;
	private Combo af_sec;
	private ScopedListsComposite af_scope;
	private SaveResetButtonComposite af_but;
	private Combo af_component;
	private Combo af_component_level;
	private SaveResetButtonComposite af_component_but;
	private Combo anti_af_component;
	private Combo anti_af_component_level;
	private SaveResetButtonComposite anti_af_component_but;
	private Combo anti_af_sec;
	private Combo anti_af_level;
	private ScopedListsComposite anti_af_scope;
	private SaveResetButtonComposite anti_af_but;
	private CTabFolder af_tab;
	private Composite af_type_composite;
	
	
	
	public AffinitySection(FormToolkit toolkit, ServiceFormEditor editor, int format, File metadataFile,
			AsceticDeployer deployer) {
		super(toolkit, editor, AF_SEC_TITLE, AF_SEC_DESC, format);
		this.metadataFile = metadataFile;
		this.deployer = deployer;
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

	@Override
	protected void createSectionWidgets(Composite com) {
		GridData rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		mainComposite.setLayoutData(rd);
		mainComposite.setLayout(new GridLayout(1, false));
		af_type_composite = toolkit.createComposite(mainComposite,
				SWT.NONE);
		rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.heightHint = 0;
		rd.grabExcessHorizontalSpace = true;
		af_type_composite.setLayoutData(rd);
		af_tab = new CTabFolder(mainComposite, SWT.FLAT | SWT.TOP);
		toolkit.adapt(af_tab, true, true);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		gd.heightHint = 0;
		af_tab.setLayoutData(gd);
		Color selectedColor = toolkit.getColors()
				.getBorderColor();
		af_tab.setSelectionBackground(new Color[] { selectedColor,
				toolkit.getColors().getBackground() },
				new int[] { 50 });
		toolkit.paintBordersFor(af_tab);
		createAFFTabs(af_tab);
		af_tab.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateAFFSelection();
			}
	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				updateAFFSelection();
			}
		});	

	}

	public void initialize(){
		af_tab.setSelection(0);
		af_type_composite = createAffinityComposite(super.mainComposite, super.toolkit);
		initAFparameters();
		super.section.setClient(mainComposite);
		super.section.setExpanded(true);
		super.setExpanded(false);
	}
	
	private Composite createAffinityComposite(Composite comp, FormToolkit toolkit){
		Composite af_details_comp = toolkit.createComposite(comp, SWT.BORDER);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		af_details_comp.setLayoutData(rd);
		af_details_comp.setLayout(new GridLayout(1, false));
		Group intra_af_comp = new Group(af_details_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		intra_af_comp.setLayoutData(rd);
		intra_af_comp.setLayout(new GridLayout(1, false));
		intra_af_comp.setText("Component Instance Affinity Rules");
		Composite af_comp_details = toolkit.createComposite(intra_af_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		af_comp_details.setLayoutData(rd);
		af_comp_details.setLayout(new GridLayout(2, false));
		toolkit.createLabel(af_comp_details, "Component");
		af_component = new Combo(af_comp_details, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		af_component.setLayoutData(rd);
		af_component.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadComponentAffinityRule(af_component.getSelectionIndex());
			}
		});
		toolkit.createLabel(af_comp_details, "Level");
		af_component_level = new Combo(af_comp_details, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		af_component_level.setLayoutData(rd);
		af_component_level.setItems(new String[] { "Low", "Medium", "High" });
		af_component_but = new SectionSaveResetButtonComposite(getShell(), toolkit, INTRA_AF, this);
		af_component_but.createComposite(intra_af_comp);
		
		Group inter_af_comp = new Group(af_details_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		inter_af_comp.setLayoutData(rd);
		inter_af_comp.setLayout(new GridLayout(1, false));
		inter_af_comp.setText("Inter-Component Affinity Rules");
		Composite combo_comp = toolkit.createComposite(inter_af_comp,
				SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		combo_comp.setLayoutData(rd);
		combo_comp.setLayout(new GridLayout(3, false));
		toolkit.createLabel(combo_comp, "Rules");
		af_sec = new Combo(combo_comp, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 300;
		af_sec.setLayoutData(rd);
		af_sec.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadAffinityRule(af_sec.getSelectionIndex());
			}
		});
		Button new_btn = toolkit.createButton(combo_comp, "New",
				SWT.NORMAL);
		new_btn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				newAffinityRule();
			}
	
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				newAffinityRule();
			}
		});
	
		// Details part
		Group details_comp = new Group(
				inter_af_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		details_comp.setLayoutData(rd);
		details_comp.setLayout(new GridLayout(2, false));
		details_comp.setText("Rule Details");
		toolkit.createLabel(details_comp, "Level", SWT.NONE);
		af_level = new Combo(details_comp, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		af_level.setLayoutData(rd);
		af_level.setItems(new String[] { "Low", "Medium", "High" });
	
		// Scope part;
		af_scope = new ScopedListsComposite(toolkit, "Components scope");
		af_scope.createComposite(inter_af_comp);
	
		// Buttons part
		af_but = new SectionSaveResetButtonComposite(getShell(), toolkit, AFFINITY, this);
		af_but.createComposite(inter_af_comp);
		
		return af_details_comp;
	}
	
	private Composite createAntiAffinityComposite(Composite comp, FormToolkit toolkit){
		Composite anti_details_comp = toolkit.createComposite(comp,	SWT.BORDER);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		anti_details_comp.setLayoutData(rd);
		anti_details_comp.setLayout(new GridLayout(1, false));
		Group intra_af_comp = new Group(anti_details_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		intra_af_comp.setLayoutData(rd);
		intra_af_comp.setLayout(new GridLayout(1, false));
		intra_af_comp.setText("Component Instance Anti-affinity Rules");
		Composite af_comp_details = toolkit.createComposite(intra_af_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		af_comp_details.setLayoutData(rd);
		af_comp_details.setLayout(new GridLayout(2, false));
		toolkit.createLabel(af_comp_details, "Component");
		anti_af_component = new Combo(af_comp_details, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 300;
		anti_af_component.setLayoutData(rd);
		anti_af_component.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadComponentAntiAffinityRule(anti_af_component.getSelectionIndex());
			}
		});
		toolkit.createLabel(af_comp_details, "Level");
		anti_af_component_level = new Combo(af_comp_details, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		anti_af_component_level.setLayoutData(rd);
		anti_af_component_level.setItems(new String[] { "Low", "Medium", "High" });
		anti_af_component_but = new SectionSaveResetButtonComposite(getShell(), toolkit, INTRA_ANTIAF, this);
		anti_af_component_but.createComposite(intra_af_comp);
		
		Group inter_af_comp = new Group(anti_details_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		inter_af_comp.setLayoutData(rd);
		inter_af_comp.setLayout(new GridLayout(1, false));
		inter_af_comp.setText("Inter-Component Anti-affinity Rules");
		Composite combo_comp = toolkit.createComposite(inter_af_comp,
				SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		combo_comp.setLayoutData(rd);
		combo_comp.setLayout(new GridLayout(3, false));
		toolkit.createLabel(combo_comp, "Rules");
		anti_af_sec = new Combo(combo_comp, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 300;
		anti_af_sec.setLayoutData(rd);
		anti_af_sec.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadAntiAffinityRule(anti_af_sec.getSelectionIndex());
			}
		});
		Button new_btn = toolkit.createButton(combo_comp, "New", SWT.NORMAL);
		new_btn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				newAntiAffinityRule();
			}
	
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				newAntiAffinityRule();
			}
		});
	
		// Details part
		Group details_comp = new Group(
				inter_af_comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		details_comp.setLayoutData(rd);
		details_comp.setLayout(new GridLayout(2, false));
		details_comp.setText("Rule Details");
		toolkit.createLabel(details_comp, "Level", SWT.NONE);
		anti_af_level = new Combo(details_comp, SWT.NONE);
		rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 350;
		anti_af_level.setLayoutData(rd);
		anti_af_level.setItems(new String[] { "Low", "Medium", "High" });
	
		// Scope part
		//page.getToolkit().createLabel(af_composite, "Components");
		anti_af_scope = new ScopedListsComposite(toolkit, "Components scope");
		anti_af_scope.createComposite(inter_af_comp);
	
		// Buttons part
		anti_af_but = new SectionSaveResetButtonComposite(getShell(), toolkit, ANTIAFFINITY, this);
		anti_af_but.createComposite(inter_af_comp);
		
		return anti_details_comp;
	}
	
	/**
	 * Create the Tab for the different TREC parameters
	 * @param AFF_tab Tab component
	 *
	 */
	private void createAFFTabs(CTabFolder AFF_tab) {
		CTabItem trust_item = new CTabItem(AFF_tab, SWT.NULL);
		trust_item.setText("Affinity");
		trust_item.setData(AFFINITY);
	
		CTabItem risk_item = new CTabItem(AFF_tab, SWT.NULL);
		risk_item.setText("Anti-Affinity");
		risk_item.setData(ANTIAFFINITY);
	
	
	}

	/**
	 * Update the Affinity-AntiAffinity section according the selected type of rules
	 */
	private void updateAFFSelection() {
		CTabItem item = af_tab.getSelection();
		if (af_type_composite != null) {
			af_type_composite.dispose();
		}
		if (((String) item.getData()).equals(AFFINITY)) {
			af_type_composite = createAffinityComposite(mainComposite,toolkit);
			initAFparameters();
		} else if (((String) item.getData()).equals(ANTIAFFINITY)) {
			af_type_composite = createAntiAffinityComposite(mainComposite,toolkit);
			initAntiAFparameters();
		} 
		af_type_composite.layout(true);
		af_type_composite.redraw();
		mainComposite.layout(true);
		mainComposite.redraw();
		// composite.layout(true);
		// composite.redraw();
		deployer.getPage().getForm().reflow(true);
	
	}

	/**
	 * Initialize the Affinity Rules
	 */
	private void initAFparameters() {
		af_component.setItems(deployer.getAllPackages());
		cleanComponentAFRuleDetails();
		enableComponentAFRuleDetails(false);
		af_sec.setItems(AsceticDeployer.generateSectionNames(
				deployer.getManifest().getNumberAffinityRules(), AFFINITY_RULE));
		cleanAFRuleDetails();
		enableAFRuleDetails(false);
	}
	
	/**
	 * Initialize the Affinity Rules
	 */
	private void initAntiAFparameters() {
		anti_af_component.setItems(deployer.getAllPackages());
		cleanComponentAntiAFRuleDetails();
		enableComponentAntiAFRuleDetails(false);
		anti_af_sec.setItems(AsceticDeployer.generateSectionNames(
				deployer.getManifest().getNumberAntiAffinityRules(), ANTI_AFFINITY_RULE));
		cleanAntiAFRuleDetails();
		enableAntiAFRuleDetails(false);
	}

	/** 
	 * Enable/Disable the affinity rules details
	 * 
	 * @param b True for enabling, false for disabling
	 */
	private void enableAFRuleDetails(boolean b) {
		af_scope.setEnabled(b);
		af_level.setEnabled(b);
		af_but.setEnabled(b);
	}
	
	/** 
	 * Enable/Disable the affinity rules details
	 * 
	 * @param b True for enabling, false for disabling
	 */
	private void enableComponentAFRuleDetails(boolean b) {
		af_component_level.setEnabled(b);
		af_component_but.setEnabled(b);
	}
	
	/** 
	 * Enable/Disable the anti-affinity rules details
	 * 
	 * @param b True for enabling, false for disabling
	 */
	private void enableAntiAFRuleDetails(boolean b) {
		anti_af_scope.setEnabled(b);
		anti_af_level.setEnabled(b);
		anti_af_but.setEnabled(b);
	}
	
	/** 
	 * Enable/Disable the affinity intra component rules details
	 * 
	 * @param b True for enabling, false for disabling
	 */
	private void enableComponentAntiAFRuleDetails(boolean b) {
		anti_af_component_level.setEnabled(b);
		anti_af_component_but.setEnabled(b);
	}

	public void cleanDetails(String type){
		
		if (type.equals(AFFINITY)){
			cleanAFRuleDetails();
		}else if (type.equals(ANTIAFFINITY)){
			cleanAntiAFRuleDetails();
		}else if (type.equals(INTRA_AF)) {
			cleanComponentAFRuleDetails();
		} else if (type.equals(INTRA_ANTIAF)) {
			cleanComponentAntiAFRuleDetails();
		}
			
	}
	
	public void saveSection(String type) throws Exception{	
		if (type.equals(AFFINITY)) {
			saveAffinityRule();
		} else if (type.equals(ANTIAFFINITY)) {
			saveAntiAffinityRule();
		} else if (type.equals(INTRA_AF)) {
			saveComponentAffinityRule();
		} else if (type.equals(INTRA_ANTIAF)) {
			saveComponentAntiAffinityRule();
		}	
	}
	
	
	/**
	 * Clean the affinity rule details
	 */
	private void cleanAFRuleDetails() {
		String[] totalEls = deployer.getAllPackages();
		af_scope.reset(totalEls);
		af_level.setText("");
	}
	
	/**
	 * Clean the anti-affinity rule details
	 */
	private void cleanAntiAFRuleDetails() {
		String[] totalEls = deployer.getAllPackages();
		anti_af_scope.reset(totalEls);
		anti_af_level.setText("");
	}
	
	/**
	 * Clean the affinity rule details
	 */
	public void cleanComponentAFRuleDetails() {
		af_level.setText("");
	}
	
	/**
	 * Clean the anti-affinity rule details
	 */
	public void cleanComponentAntiAFRuleDetails() {
		anti_af_level.setText("");
	}

	/** 
	 * Load an affinity rule
	 * 
	 * @param number Number of the affinity rule
	 */
	private void loadAffinityRule(int number) {
		String[] totalEls = deployer.getAllPackages();
		String[] selectedEls = Manifest.getPackageNames(deployer.getManifest().getAffinityRuleScope(number));
		af_scope.setPackagesLists(totalEls, selectedEls);
		
		String level = deployer.getManifest().getAffinityRuleLevel(number);
		if (level!=null)
			af_level.setText(level);
		else
			af_level.setText("");
		enableAFRuleDetails(true);
	}
	
	/** 
	 * Load an affinity rule
	 * 
	 * @param number Number of the affinity rule
	 */
	private void loadAntiAffinityRule(int number) {
		String[] totalEls = deployer.getAllPackages();
		String[] selectedEls = Manifest.getPackageNames(deployer.getManifest().getAntiAffinityRuleScope(number));
				
		anti_af_scope.setPackagesLists(totalEls, selectedEls);
		String level = deployer.getManifest().getAntiAffinityRuleLevel(number);
		if (level != null)
			anti_af_level.setText(level);
		else
			anti_af_level.setText("");
		enableAntiAFRuleDetails(true);
	}
	
	/** 
	 * Load an affinity rule
	 * 
	 * @param number Number of the affinity rule
	 */
	private void loadComponentAffinityRule(int number) {
		String component = Manifest.generateManifestName(
				af_component.getItem(number));
		String level = deployer.getManifest().getAffinityLevel(component);
		if (level != null)
			af_component_level.setText(level);
		else
			af_component_level.setText("");
		enableComponentAFRuleDetails(true);
	}
	
	/** 
	 * Load an affinity rule
	 * 
	 * @param number Number of the affinity rule
	 */
	private void loadComponentAntiAffinityRule(int number) {
		String component = Manifest.generateManifestName(
				anti_af_component.getItem(number));
		String level = deployer.getManifest().getAntiAffinityLevel(component);
		if (level != null)
			anti_af_component_level.setText(level);
		else
			anti_af_component_level.setText("");
		enableComponentAntiAFRuleDetails(true);
	}

	/**
	 * Create a new affinity rule
	 */
	private void newAffinityRule() {
		af_sec.setText(AFFINITY_RULE + (af_sec.getItems().length + 1));
		cleanAFRuleDetails();
		enableAFRuleDetails(true);
	}
	
	/**
	 * Create a new affinity rule
	 */
	private void newAntiAffinityRule() {
		anti_af_sec.setText(ANTI_AFFINITY_RULE + (af_sec.getItems().length + 1));
		cleanAntiAFRuleDetails();
		enableAntiAFRuleDetails(true);
	}

	/**
	 * Save the affinity rule
	 * @throws Exception
	 */
	public void saveAffinityRule() throws Exception {
		int number = AsceticDeployer.getNumberFromName(AFFINITY_RULE, af_sec.getText().trim());
		if (number >= af_sec.getItems().length) {
			af_sec.add(AFFINITY_RULE + (af_sec.getItems().length + 1));
			if (deployer.getManifest() == null) {
				deployer.generateNewManifest();
			}
			deployer.getManifest().addAffinityRule(
					Manifest.generateManifestNames(af_scope.getSelectedPackages()),
					af_level.getText().trim());
		} else {
			deployer.getManifest().setAntiAffinityRule(af_sec.getSelectionIndex(), 
					Manifest.generateManifestNames(af_scope.getSelectedPackages()), 
					af_level.getText().trim());
		}
		deployer.writeManifestToFile();
	}
	
	/**
	 * Save the component affinity rule
	 * @throws Exception
	 */
	public void saveComponentAffinityRule() throws Exception {
		String component = Manifest.generateManifestName(
				af_component.getItem(af_component.getSelectionIndex()));
		if (deployer.getManifest() == null) {
			deployer.generateNewManifest();
		}
		deployer.getManifest().setComponentAffinity(component, af_component_level.getText().trim());
		deployer.writeManifestToFile();
	}
	
	/**
	 * Save the anti-affinity rule
	 * @throws Exception
	 */
	public void saveAntiAffinityRule() throws Exception {
		int number = AsceticDeployer.getNumberFromName(ANTI_AFFINITY_RULE, anti_af_sec.getText().trim());
		if (number >= anti_af_sec.getItems().length) {
			anti_af_sec.add(ANTI_AFFINITY_RULE + (anti_af_sec.getItems().length + 1));
			if (deployer.getManifest() == null) {
				deployer.generateNewManifest();
			}
			deployer.getManifest().addAntiAffinityRule(
					Manifest.generateManifestNames(anti_af_scope.getSelectedPackages()),
					anti_af_level.getText().trim());
		} else {
			deployer.getManifest().setAntiAffinityRule(anti_af_sec.getSelectionIndex(), 
					Manifest.generateManifestNames(anti_af_scope.getSelectedPackages()), 
					anti_af_level.getText().trim());
		}
		deployer.writeManifestToFile();
	}
	
	/**
	 * Save the component anti-affinity rule
	 * @throws Exception
	 */
	public void saveComponentAntiAffinityRule() throws Exception {
		String component = Manifest.generateManifestName(
				anti_af_component.getItem(anti_af_component.getSelectionIndex()));
		if (deployer.getManifest() == null) {
			deployer.generateNewManifest();
		}
		deployer.getManifest().setComponentAntiAffinity(component, anti_af_component_level.getText().trim());
		deployer.writeManifestToFile();
	}
	
	
	
}
