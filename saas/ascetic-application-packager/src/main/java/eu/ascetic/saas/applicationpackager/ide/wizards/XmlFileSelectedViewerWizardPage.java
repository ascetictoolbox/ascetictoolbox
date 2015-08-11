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

public class XmlFileSelectedViewerWizardPage extends WizardPage {
	
	private Text text1;
	private Composite container;
	private StyledText styledText;
	
	private boolean calledFromCheckbox;

	public XmlFileSelectedViewerWizardPage() {
		super("XML content");
		setTitle("XML content");
		setDescription("File selected XML content. On next page you will see the OVF translation from this XML");
		setControl(text1);
	}

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

	public String getText1() {
		return text1.getText();
	}
	
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
