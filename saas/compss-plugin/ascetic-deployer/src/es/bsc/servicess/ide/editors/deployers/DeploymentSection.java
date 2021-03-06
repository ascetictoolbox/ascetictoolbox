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

import java.io.File;
import java.util.Scanner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestCase.Files;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.editors.ServiceEditorSection;
import es.bsc.servicess.ide.editors.ServiceFormEditor;

public class DeploymentSection extends ServiceEditorSection{
	
	private Text serverText;
	private AsceticDeployer deployer;
	private Text sshPublicKeyPathText;
	private Text sshPrivateKeyPathText;
	private Text monitorText;
	private Button imageCache;
	private Combo optParam;
	private Text maxPower;
	private Text maxPrice;
	private Button autoNegotiation;
	private Text appDuration;
	private Text energy;
	private Text cost;
	private static String[] items = new String[]{"Energy", "Cost", "Performance"};
	private static Logger log = Logger.getLogger(DeploymentSection.class);
	
	public static final String DEPLOYMENT_SEC_TITLE = "Deployment Configuration";
	public static final String DEPLOYMENT_SEC_DESC = "Define the location for the Application Manager and other deployment parameters";
	public DeploymentSection(FormToolkit toolkit, ServiceFormEditor editor, 
			int format, AsceticDeployer deployer) {
		super(toolkit, editor, DEPLOYMENT_SEC_TITLE, DEPLOYMENT_SEC_DESC, format);
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
	protected void createSectionWidgets(Composite comp) {
		
		GridData rd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);//|GridData.FILL_HORIZONTAL
		rd.grabExcessHorizontalSpace = true;
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(rd);
		createCloudPlatformWidgets(comp);
		createApplicationSecurityWidgets(comp);
		createDeploymentPropertiesWidgets(comp);
		
		imageCache = toolkit.createButton(comp, "Enable Image Caching", SWT.CHECK);
		autoNegotiation = toolkit.createButton(comp, "Automatic Negotiation", SWT.CHECK);
		autoNegotiation.setSelection(true);
		
	}
	
	private void createDeploymentPropertiesWidgets(Composite comp) {
		Group secComp = new Group(comp, SWT.BORDER);
		GridData rd = new GridData();
				//| GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		secComp.setLayoutData(rd);
		secComp.setLayout(new GridLayout(3, false));
		secComp.setText("Deployment Properties");
		
		addPowerBoundaries(secComp);
		addPriceBoundaries(secComp);
		addMaxDuration(secComp);
		addMaxEnergy(secComp);
		addMaxCost(secComp);
		addOptimizationParameter(secComp);
		
	}

	private void addOptimizationParameter(Group secComp) {
		toolkit.createLabel(secComp, "Optimization Parameter", SWT.NONE);
		optParam = new Combo(secComp, SWT.NONE);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL);
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		optParam.setLayoutData(rd);
		optParam.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				deployer.getProperties().setOptimizationParameter(optParam.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
		});
		optParam.setItems(items);
		
	}

	private void addMaxCost(Group secComp) {
		toolkit.createLabel(secComp, " Max. Cost", SWT.NONE);
		cost = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		cost.setLayoutData(rd);
		cost.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setMaxCost(Double.parseDouble(cost.getText().trim()));
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "€", SWT.NONE);
		
	}

	private void addMaxEnergy(Group secComp) {
		toolkit.createLabel(secComp, " Max. Energy", SWT.NONE);
		energy = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		energy.setLayoutData(rd);
		energy.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setMaxEnergy(Double.parseDouble(energy.getText().trim()));
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "W.h", SWT.NONE);
		
	}

	private void addMaxDuration(Group secComp) {
		toolkit.createLabel(secComp, "Max. Application Duration", SWT.NONE);
		appDuration = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		appDuration.setLayoutData(rd);
		appDuration.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setMaxDuration(Long.parseLong(appDuration.getText().trim()));
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "secs.", SWT.NONE);
		
	}

	private void addPriceBoundaries(Group secComp) {
		toolkit.createLabel(secComp, "Max. Price Boundary", SWT.NONE);
		maxPrice = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		maxPrice.setLayoutData(rd);
		maxPrice.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setPriceBoundary(Double.parseDouble(maxPrice.getText().trim()));
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "€/h", SWT.NONE);
		
	}

	private void addPowerBoundaries(Group secComp) {
		toolkit.createLabel(secComp, "Max. Power Boundary", SWT.NONE);
		maxPower = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		GridData rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		maxPower.setLayoutData(rd);
		maxPower.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setPowerBoundary(Double.parseDouble(maxPower.getText().trim()));
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "W", SWT.NONE);
		
	}

	private void createApplicationSecurityWidgets(Composite comp) {
		Group secComp = new Group(comp, SWT.BORDER);
		GridData rd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING); //GridData.FILL_HORIZONTAL
			 
		rd.grabExcessHorizontalSpace = true;
		secComp.setLayoutData(rd);
		secComp.setLayout(new GridLayout(2, false));
		secComp.setText("Application SSH security");
		toolkit.createLabel(secComp, "Public Key File", SWT.NONE);
		sshPublicKeyPathText = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		sshPublicKeyPathText.setLayoutData(rd);
		sshPublicKeyPathText.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setApplicationSSHPublicKeyPath(sshPublicKeyPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(secComp, "Private Key File", SWT.NONE);
		sshPrivateKeyPathText = toolkit.createText(secComp, "",
				SWT.SINGLE | SWT.BORDER);
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		sshPrivateKeyPathText.setLayoutData(rd);
		sshPrivateKeyPathText.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setApplicationSSHPrivateKeyPath(sshPrivateKeyPathText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		
	}

	private void createCloudPlatformWidgets(Composite comp) {
		Group options = new Group(comp, SWT.BORDER);
		GridData rd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);//GridData.FILL_HORIZONTAL
		rd.grabExcessHorizontalSpace = true;
		options.setLayout(new GridLayout(2, false));
		options.setLayoutData(rd);
		options.setText("Cloud Platform Specification");
		toolkit.createLabel(options, "Application Manager", SWT.NONE);
		serverText = toolkit.createText(options, "",
				SWT.SINGLE | SWT.BORDER);
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		serverText.setLayoutData(rd);
		serverText.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setDSLocation(serverText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		toolkit.createLabel(options, "Application Monitor", SWT.NONE);
		monitorText = toolkit.createText(options, "",
				SWT.SINGLE | SWT.BORDER);
		rd = new GridData();
		rd.grabExcessHorizontalSpace = true;
		rd.minimumWidth = 400;
		monitorText.setLayoutData(rd);
		monitorText.addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent arg0) {
				deployer.getProperties().setMonitorLocation(monitorText.getText().trim());
				try {
					deployer.getProperties().save();
				} catch (ConfigurationException e) {
					log.error("Exception saving properties", e);
					ErrorDialog.openError(getShell(),
							"Saving ascetic properties", e.getMessage(), 
							new Status(Status.ERROR, Activator.PLUGIN_ID,"Exception saving properties", e));
				}
			}
	
		});
		
	}

	public void init() {
		serverText.setText(deployer.getProperties().getDSLocation());
		monitorText.setText(deployer.getProperties().getMonitorLocation());
		sshPublicKeyPathText.setText(deployer.getProperties().getApplicationSSHPublicKeyPath());
		sshPrivateKeyPathText.setText(deployer.getProperties().getApplicationSSHPrivateKeyPath());
		Double pow = deployer.getProperties().getPowerBoundary();
		if (pow!=null){
			maxPower.setText(Double.toString(pow));
		}
		Double price = deployer.getProperties().getPriceBoundary();
		if (price!=null){
			maxPrice.setText(Double.toString(price));
		}
		optParam.setText(deployer.getProperties().getOptimizationParameter());
		Long duration = deployer.getProperties().getMaxDuration();
		if (duration!=null){
			appDuration.setText(Long.toString(duration));
		}
		Double maxEnergy = deployer.getProperties().getMaxEnergy();
		if (maxEnergy!=null){
			energy.setText(Double.toString(maxEnergy));
		}
		Double maxCost = deployer.getProperties().getMaxCost();
		if (maxCost!=null){
			cost.setText(Double.toString(maxCost));
		}
		
	}
	

	private String readFile(String filePath) throws Exception{
		return new Scanner(new File(filePath)).useDelimiter("\\Z").next();
	}

	public String getServerLocation() {
		return serverText.getText().trim();
	}
	
	public String getMonitorLocation() {
		return monitorText.getText().trim();
	}
	
	public boolean getImageCaching(){
		return imageCache.getSelection();
	}
	
	public String getPowerBoundary(){
		return maxPower.getText().trim();
	}
	
	public String getPriceBoundary(){
		return maxPrice.getText().trim();
	}
	
	public String getOptimizationParameter(){
		return optParam.getText().trim();
	}

	public void setApplicationSecurityInManifest(Manifest manifest) throws Exception {
		if (sshPublicKeyPathText.getText().trim().isEmpty() || 
				sshPrivateKeyPathText.getText().trim().isEmpty()){
			log.debug("A public or private key is not defined");
			manifest.removeApplicationSecurity();
		}else{
			manifest.setApplicationSecurity(readFile(sshPrivateKeyPathText.getText().trim()), 
					readFile(sshPublicKeyPathText.getText().trim()));	
		}
		
	}

	public void setDeploymentOptionsInManifest(Manifest manifest) {
		manifest.setImageCaching(imageCache.getSelection());
		manifest.deleteSlaTerms();
		manifest.setPowerBoundary(maxPower.getText().trim());
		manifest.setPriceBoundary(maxPrice.getText().trim());
		manifest.setAppDuration(appDuration.getText().trim());
		manifest.setAppEnergy(energy.getText().trim());
		manifest.setAppCost(cost.getText().trim());
		manifest.setOptimizationParameter(optParam.getText().trim());
	}
	
	public boolean isManual(){
		return !autoNegotiation.getSelection();
	}

}
