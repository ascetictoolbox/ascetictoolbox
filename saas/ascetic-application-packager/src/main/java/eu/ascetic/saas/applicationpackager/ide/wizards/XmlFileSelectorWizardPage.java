package eu.ascetic.saas.applicationpackager.ide.wizards;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs.ProgressBarDialog;

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
 * This class allow to the user to select an XML file as input for Application Packager wizard
 *
 */

public class XmlFileSelectorWizardPage extends WizardPage {
	
	/** The text1. */
	private Text text1;
	
	/** The container. */
	private Composite container;
	
	private ProgressBar progressBar;

	
	/**
	 * Instantiates a new xml file selector wizard page.
	 */
	public XmlFileSelectorWizardPage() {
		super("XML to Import");
		setTitle("XML to Import");
//		setDescription("Select a XML file to import");
		setDescription("Select a XML file source to import");
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

		//testing with jobs
//		Button button = new Button(container, SWT.PUSH);
//		progressBar = new ProgressBar(container, SWT.SMOOTH);
//	    progressBar.setBounds(100, 10, 200, 20);
//	    progressBar.setVisible(false);
//	    progressBar.setSelection(0);
//		progressBar.setMaximum(100);
//			
//		button.setText("job button");
// 
//		//JOB
//		Listener listener = new Listener() {
//			public void handleEvent(Event event) {
//				 
//		    	  Job job = new Job("test") {
//		  			@Override
//		  			protected IStatus run(IProgressMonitor monitor) {
//		  				
//		  				monitor.beginTask("start task", 100);
//		  				//time consuming work here
//		  				doExpensiveWork(monitor);
//		  				//sync with UI				
//		  				syncWithUI();
//
//		  				return Status.OK_STATUS;
//		  			}
//
//		  		};
//		  		job.setUser(true);
//		  		job.schedule();  	  
//		      }
//		    };
// 
//		button.addListener(SWT.Selection, listener);
//		end testing with jobs
		
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

//	private void doExpensiveWork(final IProgressMonitor monitor) {
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				progressBar.setVisible(true);
//				// mimic a long time job here
//				for (int i = 0; i < 10; i++) {
//					try {
//						//give a progress bar to indicate progress
//						monitor.worked(10);
//						progressBar.setSelection(progressBar.getSelection() + 10);
//						
//						Thread.sleep(2000);
//						System.out.println("step: " + i);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//
//	}
// 
//	private void syncWithUI() {
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				MessageDialog.openInformation(XmlFileSelectorWizardPage.this.getShell(), "message",
//						"completed!");
//			}
//		});
//	}
	/**
	 * Gets the text1.
	 *
	 * @return the text1
	 */
	public String getText1() {
		return text1.getText();
	}

}
