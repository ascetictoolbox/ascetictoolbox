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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private Group options;
	private AsceticDeployer deployer;
	private Text sshPublicKeyPathText;
	private Text sshPrivateKeyPathText;
	private Text monitorText;
	
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
		
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(rd);
		options = new Group(comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
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
		Group secComp = new Group(comp, SWT.BORDER);
		rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
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
	
	public void init() {
		serverText.setText(deployer.getProperties().getDSLocation());
		monitorText.setText(deployer.getProperties().getMonitorLocation());
		sshPublicKeyPathText.setText(deployer.getProperties().getApplicationSSHPublicKeyPath());
		sshPrivateKeyPathText.setText(deployer.getProperties().getApplicationSSHPrivateKeyPath());
		
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

}
