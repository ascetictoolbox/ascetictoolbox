package eu.ascetic.saas.applicationpackager.ide.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs.VmicCallProgressBarDialog;
import eu.ascetic.saas.applicationpackager.utils.Xml2OvfTranslator;

// TODO: Auto-generated Javadoc
/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class shows the OVF code generated viewer page
 *
 */

public class OvfCodeGeneratedViewerWizardPage extends WizardPage {

	/** The container. */
	private Composite container;
	
	/** The styled text. */
	private StyledText styledText;
	
	/** The called from checkbox. */
	private boolean calledFromCheckbox;

	/**
	 * Instantiates a new ovf code generated viewer wizard page.
	 */
	protected OvfCodeGeneratedViewerWizardPage() {
		super("OVF generated from XML file provided");
		setTitle("OVF generated from XML file provided");
		setDescription("OVF generated from XML file provided. Next page, will send this OVF code to ASCETiC VM Image Constructor");
		setControl(styledText);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;	
		
		Label contentLabel = new Label(container, SWT.NONE);
		contentLabel.setText("OVF");
//		styledText = new StyledText(container, SWT.V_SCROLL | SWT.BORDER);
		styledText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		String ovfContent = "";
	
		styledText.setText(ovfContent);
		styledText.setEditable(false);
		
		Label labelCheck = new Label(container, SWT.NONE);
		labelCheck.setText("Continue?");
		final Button check = new Button(container, SWT.CHECK);
		check.setSelection(false);check.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (check.getSelection() == true) {
					setPageComplete(true);
					calledFromCheckbox = true;
//					vmicClient.testGenerateImageWorkflow();
				}
				else {
					setPageComplete(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
		
	}

	/**
	 * Sets the content.
	 *
	 * @param xmlPath the new content
	 */
	public void setContent(String xmlPath) {
		// TODO Auto-generated method stub
		Xml2OvfTranslator xml2ovf = new Xml2OvfTranslator(xmlPath);
		String ovfCode = xml2ovf.translate();
		styledText.setText(ovfCode);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {			
		IWizardPage nextPage = super.getNextPage();	
		if (calledFromCheckbox){	
			VmicCallProgressBarDialog vcpbd = new VmicCallProgressBarDialog(OvfCodeGeneratedViewerWizardPage.this.getShell(),
					this, 
					(OvfResponseVmicViewerWizardPage) nextPage, styledText.getText());
			vcpbd.open();			
		}
		return nextPage;
	} 
	
	public void setCalledFromCheckBox(boolean called){
		calledFromCheckbox = called;
	}
	
}
