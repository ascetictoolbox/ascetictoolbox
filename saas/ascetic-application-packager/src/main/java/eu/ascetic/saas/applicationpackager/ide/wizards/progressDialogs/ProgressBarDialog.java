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
public class ProgressBarDialog extends Dialog {

    private StyledText logConsoleText;
    private Button closeButton;
    private Composite cancelComposite;
    private Label lineLabel;//
    private Composite progressBarComposite;//
    private Label message;
    private ProgressBar progressBar = null; //

    private Object result; //
    private Shell shell; //
    private Display display = null; 
    
    protected volatile boolean isClosed = false;//closed state
    
    protected int executeTime = 50;//process times
    protected String processMessage = "process......";//procress info
    protected String shellTitle = "Progress..."; //
    protected int processBarStyle = SWT.SMOOTH; //process bar style

    public void setExecuteTime(int executeTime) {
        this.executeTime = executeTime;
    }

	public void setProcessMessage(String processInfo) {
		this.processMessage = processInfo;
	}

	public ProgressBarDialog(Shell parent) {
        super(parent);
    }
    
	
    public void initGuage(String title, String processMessage, int executionTime){
//		this.setExecuteTime(100);
//		this.setProcessMessage("please waiting....");
//		this.setShellTitle("Demo");
    	this.setExecuteTime(executionTime);
		this.setProcessMessage(processMessage);
		this.setShellTitle(title);
    }
    
    public Object open() {
        createContents(); //create window
        shell.open();
        shell.layout();
        
  	  	Job job = new Job("test") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doBefore();
				monitor.beginTask("start task", 100);
				//time consuming work here
				doExpensiveWork(monitor);
				//sync with UI				
//				syncWithUI();
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
    
    private void updateProgressWidgets(int progressToAdd){
    	Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				progressBar.setSelection(progressBar.getSelection() + 10);
				if (progressBar.getSelection() == 50) {
					 message.setText(message.getText() + " = 50%");
				}
				addLogMessage("Progress = " + progressBar.getSelection());
				addLogMessage("Progress = " + progressBar.getSelection());
				addLogMessage("Progress = " + progressBar.getSelection());
			}
    	});
    }
    
    private void updateWidgetsProcessFinished(){
    	Display.getDefault().asyncExec(new Runnable() {
    		public void run() {
    			closeButton.setEnabled(true);
    			setProcessMessage("Task completed");
    			message.setText("Task completed");
    		}
    	});
    	
    }
    private void doExpensiveWork(final IProgressMonitor monitor) {
    	boolean shouldStop = false;
		int max = 10;
		clearLog();
		for (int i = 0; i <= max; i++) {
			try {
				monitor.worked(10);
				updateProgressWidgets(10);
				Thread.sleep(2000);
				System.out.println("step: " + i);
				if (i == max || isClosed) {
                    if (isClosed) {
                        shouldStop = true;//
                        cleanUp();//
                    }
                    updateWidgetsProcessFinished();
                }
				if (shouldStop) {
                    break;
                }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    protected void createContents() {
        shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL | SWT.RESIZE);
        display = shell.getDisplay();
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 10;

        shell.setLayout(gridLayout);
        shell.setSize(600, 450);
        shell.setText(shellTitle);
		
        final Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        composite.setLayout(new GridLayout());
        
        message = new Label(composite, SWT.NONE);
        message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        message.setText(processMessage);

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

//    protected abstract String process(int times);


    protected void cleanUp()
    {
    	
    }
    

    protected void doBefore()
    {
    	
    }
    
    protected void doAfter()
    {
    	
    }

//    class ProcessThread extends Thread {
//        private int max = 0;
//        private volatile boolean shouldStop = false;
//
//        ProcessThread(int max) {
//            this.max = max;
//        }
//
//        public void run() {
//        	doBefore();
//            for (final int[] i = new int[] {1}; i[0] <= max; i[0]++) 
//            {
//                //
//                final String info = process(i[0]);
//                if (display.isDisposed()) {
//                    return;
//                }
//                display.asyncExec(new Runnable() {
//                    public void run() {
//                        if (progressBar.isDisposed()) {
//                            return;
//                        }
//                        //
////                        processMessageLabel.setText(info);
//                        logConsoleText.setText(info);
//                        //
//                        progressBar.setSelection(i[0]);
//                        //
//                        if (i[0] == max || isClosed) {
//                            if (isClosed) {
//                                shouldStop = true;//
//                                cleanUp();//
//                            }
//                            shell.close();//
//                        }
//                    }
//                });
//
//                if (shouldStop) {
//                    break;
//                }
//            }
//            doAfter();
//        }
//    }

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
	
	public void addLogMessage(String message){
		logConsoleText.append("\n" + message);
		logConsoleText.setSelection(logConsoleText.getCharCount());
	}
	
}
