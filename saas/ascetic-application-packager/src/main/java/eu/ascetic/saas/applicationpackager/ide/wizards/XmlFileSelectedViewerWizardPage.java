package eu.ascetic.saas.applicationpackager.ide.wizards;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Text;

import eu.ascetic.saas.applicationpackager.utils.Utils;

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
 * This class shows the content of the XML file selected by user in the Application Packager wizard
 *
 */
public class XmlFileSelectedViewerWizardPage extends WizardPage {
	
	/** The text1. */
	private Text text1;
	
	/** The container. */
	private Composite container;
	
	/** The styled text. */
	private StyledText styledText;
	
	/** The called from checkbox. */
	private boolean calledFromCheckbox;

	/**
	 * Instantiates a new xml file selected viewer wizard page.
	 */
	public XmlFileSelectedViewerWizardPage() {
		super("XML content");
		setTitle("XML content");
		setDescription("File selected XML content. On next page you will see the OVF translation from this XML");
		setControl(text1);
	}

	/**
	 * Sets the content.
	 *
	 * @param path the new content
	 */
	public void setContent(String path){
		text1.setText(path);
		String xmlContent = "";
		if (!path.equalsIgnoreCase("")){
			try {
				xmlContent = Utils.readFile(path);
			}
			catch(IOException e){
				MessageDialog.openError(this.getShell(), "Error reading XML file", e.getMessage());
				xmlContent = "Error reading XML file";
			}
		}
		styledText.setText(xmlContent);		
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
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("File");

		text1 = new Text(container, SWT.BORDER | SWT.SINGLE);
		text1.setText("");		

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text1.setLayoutData(gd);
		text1.setEditable(false);		
		
		Label contentLabel = new Label(container, SWT.NONE);
		contentLabel.setText("Content");
//		styledText = new StyledText(container, SWT.V_SCROLL | SWT.BORDER);
		styledText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		String xmlContent = "";
	
		styledText.setText(xmlContent);
		styledText.setEditable(false);
//		styledText.setSize(500, 500);
//		styledText.setBounds(10, 10, 500, 500);
		
		Label labelCheck = new Label(container, SWT.NONE);
		labelCheck.setText("Continue?");
		final Button check = new Button(container, SWT.CHECK);
		check.setSelection(false);check.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (check.getSelection() == true) {
					calledFromCheckbox = true;
					setPageComplete(true);
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
	 * Gets the text1.
	 *
	 * @return the text1
	 */
	public String getText1() {
		return text1.getText();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {	
		IWizardPage nextPage = super.getNextPage();	
		if (calledFromCheckbox){			
//			((OvfCodeGeneratedViewerWizardPage) this.getNextPage()).setContent(text1.getText());
			//TODO - do stuff	
			((OvfCodeGeneratedViewerWizardPage) nextPage).setContent(text1.getText());
			calledFromCheckbox = false;			
		}
		return nextPage;
		
	} 
	
	

}
