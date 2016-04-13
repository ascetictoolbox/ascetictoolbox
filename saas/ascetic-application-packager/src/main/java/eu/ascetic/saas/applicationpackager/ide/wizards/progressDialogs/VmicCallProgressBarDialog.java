package eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import eu.ascetic.saas.applicationpackager.ide.wizards.OvfCodeGeneratedViewerWizardPage;
import eu.ascetic.saas.applicationpackager.ide.wizards.OvfResponseVmicViewerWizardPage;
import eu.ascetic.saas.applicationpackager.ovf.OVFUtils;
import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.saas.applicationpackager.vmic.VmicClient;

/**
 * progress bar dialog.
 * the first, you must know your app execute times,
 * you need implement two method: 
 * 
 * process(int times);
 * initGuage();
 * 
 * you can implements method:
 *  
 * cleanUp()
 * doBefore()
 * doAfter() 
 * @author david.rojoa@atos.net
 * 
 */
public class VmicCallProgressBarDialog extends Dialog {

    private StyledText logConsoleText;
    private Button closeButton;
    private Composite cancelComposite;
    private Label lineLabel;//
    private Composite progressBarComposite;//
    private Label message;
    private ProgressBar progressBar = null; //

    private Object result; //
    private Shell shell; //
 
    
    protected volatile boolean isClosed = false;//closed state
    
    protected int executeTime = 50;//process times
    protected String shellTitle = "Progress..."; //
    protected int processBarStyle = SWT.SMOOTH; //process bar style
    
    private OvfCodeGeneratedViewerWizardPage currentWizardPage;
    private OvfResponseVmicViewerWizardPage nextWizardPage;

    private String ovfVmicResponse;
    private String inputOvf;

	public VmicCallProgressBarDialog(Shell parent, 
			OvfCodeGeneratedViewerWizardPage currentWizardPage, 
			OvfResponseVmicViewerWizardPage nextWizardPage,
			String ovfCodeGeneratedFromXml) {
        super(parent);
        this.currentWizardPage = currentWizardPage;
        this.nextWizardPage = nextWizardPage;
        this.inputOvf = ovfCodeGeneratedFromXml;
    }
	
    public void setExecuteTime(int executeTime) {
        this.executeTime = executeTime;
    }    
	
    
    public Object open() {
        createContents(); //create window
        shell.open();
        shell.layout();
        
  	  	Job job = new Job("OVF generation from ASCETiC VMIC") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doBefore();
				monitor.beginTask("start task", 100);
				//time consuming work here
				doExpensiveWork(monitor);
				doAfter();
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();  	  
    

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    private void syncWithUI() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, "message",
						"completed!");
			}
		});
	}
    
    private void clearLog(){
    	Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				logConsoleText.setText("");
			}
    	});
    }
    
    public void updateProgressBar(final double progress){
    	Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				progressBar.setSelection(Utils.getIntValue(progress));
			}
    	});
    }
    
    public void updateWidgetsProcessFinished(){
    	Display.getDefault().asyncExec(new Runnable() {
    		public void run() {
    			closeButton.setEnabled(true);
    			message.setText("Task completed");
    		}
    	});
    	
    }
    private void doExpensiveWork(final IProgressMonitor monitor) {
		clearLog();
		VmicClient vmicClient = new VmicClient();
		ovfVmicResponse = vmicClient.testGenerateImageWorkflow(inputOvf, this);
	}
    
    protected void createContents() {
        shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL | SWT.RESIZE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 10;

    	this.setExecuteTime(100);
		
		this.setShellTitle("Virtual Image Constructor communication");
        shell.setLayout(gridLayout);
        shell.setSize(600, 525);
        shell.setText(shellTitle);
        
		
        final Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        composite.setLayout(new GridLayout());
        
        message = new Label(composite, SWT.NONE);
        message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        message.setText( "Task in progress...");

        progressBarComposite = new Composite(shell, SWT.NONE);
        progressBarComposite.setLayoutData(new GridData(GridData.FILL,
                GridData.CENTER, false, false));
        progressBarComposite.setLayout(new FillLayout());

        progressBar = new ProgressBar(progressBarComposite, processBarStyle);
        progressBar.setMaximum(executeTime);

		logConsoleText = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		logConsoleText.setLayoutData(gd);
		logConsoleText.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		logConsoleText.setEditable(false);
        
        //separator
        lineLabel = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

        cancelComposite = new Composite(shell, SWT.NONE);
        cancelComposite.setLayoutData(new GridData(GridData.END,
                GridData.CENTER, false, false));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 2;
        cancelComposite.setLayout(gridLayout_1);

        closeButton = new Button(cancelComposite, SWT.NONE);
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                isClosed = true;
                //System.out.println(isClosed);
            }
        });
        closeButton.setLayoutData(new GridData(78, SWT.DEFAULT));
        closeButton.setText("Close");
        closeButton.setEnabled(false);
        closeButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		shell.close();//
            }
        	
		});    
        
        setDialogLocation();
      
    }

    protected void cleanUp()
    {
    	
    }
    

    protected void doBefore()
    {
    	
    }
    
    protected void doAfter()
    {
    	boolean error = false;
    	//all has been executed succesfully
		System.out.println("VMIC response: ");
		System.out.println(ovfVmicResponse);
		String msg = null;
		if (ovfVmicResponse != null){
			msg = OVFUtils.removeSoftwareDependencyElements(ovfVmicResponse);
			if (msg == null){
				msg = "Error removing software dependency elements in OVF. Please review de logs. File:\n" + ovfVmicResponse;
				error = true;
			}
		}
		else {
			msg = "Error generating ovf from VMIC component. Please review the logs for more information";
			error = true;
		}
    	updateWidgetsProcessFinished();			
		nextWizardPage.setContent(msg, error);
		currentWizardPage.setCalledFromCheckBox(false);
    }

	public void setShellTitle(String shellTitle) {
		this.shellTitle = shellTitle;
	}

	public void setProcessBarStyle(boolean pStyle) {
		if(pStyle)
			this.processBarStyle = SWT.SMOOTH;
		else
			this.processBarStyle = SWT.NONE;
			
	}
	
	private void setDialogLocation()
	{
		Rectangle monitorArea = shell.getDisplay().getPrimaryMonitor().getBounds();
		Rectangle shellArea = shell.getBounds();
		int x = monitorArea.x + (monitorArea.width - shellArea.width)/2;
		int y = monitorArea.y + (monitorArea.height - shellArea.height)/2;
		shell.setLocation(x,y);
	}
	
	public void addLogMessage(final String message){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				logConsoleText.append("\n" + message);
				logConsoleText.setSelection(logConsoleText.getCharCount());
			}
		});

	}

}
