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

import eu.ascetic.saas.applicationpackager.utils.Xml2OvfTranslator;
import eu.ascetic.saas.applicationpackager.vmic.VmicClient;

public class OvfCodeGeneratedViewerWizardPage extends WizardPage {

	private Composite container;
	private StyledText styledText;
	private boolean calledFromCheckbox;
	
	protected OvfCodeGeneratedViewerWizardPage() {
		super("OVF generated from XML file provided");
		setTitle("OVF generated from XML file provided");
		setDescription("OVF generated from XML file provided. Next page, will send this OVF code to ASCETiC VM Image Constructor");
		setControl(styledText);
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
//		Label label1 = new Label(container, SWT.NONE);
//		label1.setText("File");
//
//		text1 = new Text(container, SWT.BORDER | SWT.SINGLE);
//		text1.setText("");		
//
//		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//		text1.setLayoutData(gd);
//		text1.setEditable(false);		
		
		Label contentLabel = new Label(container, SWT.NONE);
		contentLabel.setText("OVF");
//		styledText = new StyledText(container, SWT.V_SCROLL | SWT.BORDER);
		styledText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
		String ovfContent = "";
	
		styledText.setText(ovfContent);
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

	public void setContent(String xmlPath) {
		// TODO Auto-generated method stub
		Xml2OvfTranslator xml2ovf = new Xml2OvfTranslator(xmlPath);
		String ovfCode = xml2ovf.translate();
		styledText.setText(ovfCode);		
	}
	
	@Override
	public IWizardPage getNextPage() {			
		IWizardPage nextPage = super.getNextPage();	
		if (calledFromCheckbox){	
			VmicClient vmicClient = new VmicClient();
			String ovfVmicResponse = vmicClient.testGenerateImageWorkflow(styledText.getText());
			((OvfResponseVmicViewerWizardPage) nextPage).setContent(ovfVmicResponse);
			calledFromCheckbox = false;
		}
		return nextPage;
	} 

}
