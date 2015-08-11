package eu.ascetic.saas.applicationpackager.ide.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class XmlFileSelectorWizardPage extends WizardPage {
	
	private Text text1;
	private Composite container;

	
	public XmlFileSelectorWizardPage() {
		super("XML to Import");
		setTitle("XML to Import");
//		setDescription("Select a XML file to import");
		setDescription("Select a XML file source to import");
	}


	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("File:");
		
		

//		FileDialog fileDialog = new FileDialog(this.getShell(), SWT.NONE);
//		fileDialog.setText("Select XML file:");
//
//	
//		String[] filterExt = { "*.xml" };
//		fileDialog.setFilterExtensions(filterExt);
//		String directoryName = "C:\\data\\app\\workspace\\ascetic-application-packager\\src\\main\\resources";
//		if (directoryName.length() > 0) {
//			File fpath = new File(directoryName);
//			if (fpath.exists())
//				fileDialog.setFilterPath(directoryName);
//		}
//		final String selectedDirectory = fileDialog.open();
//		if (selectedDirectory != null && selectedDirectory.length()>0) {
//			IFolder extractFolder = secondPage.getJavaProject().getProject()
//					.getFolder(ProjectMetadata.OUTPUT_FOLDER).getFolder(TMP_PATH);
//			loadWarFile(selectedDirectory, extractFolder);			
//		} 
		 //************
		
		text1 = new Text(container, SWT.BORDER | SWT.SINGLE);
		text1.setText("");
		text1.setEnabled(false);
		text1.setText("Choose one XML file");
		text1.addKeyListener(new KeyListener(){			
			@Override
			public void keyPressed(KeyEvent e) {

			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (!text1.getText().isEmpty()) {
					setPageComplete(true);
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text1.setLayoutData(gd);
		
		Button b = new Button(container, SWT.PUSH);
		b.setText("Select");
		b.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				FileDialog fileDialog = new FileDialog(XmlFileSelectorWizardPage.this.getShell(), SWT.NONE);
				fileDialog.setText("Select XML file:");
		
			
				String[] filterExt = { "*.xml" };
				fileDialog.setFilterExtensions(filterExt);
				//String directoryName = "C:\\data\\app\\workspace\\ascetic-application-packager\\src\\main\\resources";
				String directoryName = "//home//ubuntu//ascetic//saas//app-packager//xml";
				if (directoryName.length() > 0) {
					File fpath = new File(directoryName);
					if (fpath.exists())
						fileDialog.setFilterPath(directoryName);
				}
				final String selectedDirectory = fileDialog.open();
				if (selectedDirectory != null && selectedDirectory.length()>0) {
						text1.setText(selectedDirectory);
						text1.setToolTipText(selectedDirectory);
						System.out.println(text1.getText());
//						MyPageTwo.setXmlPath(text1.getText());
						((XmlFileSelectedViewerWizardPage)XmlFileSelectorWizardPage.this.getNextPage()).setContent(text1.getText());						
						setPageComplete(true);
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

}
