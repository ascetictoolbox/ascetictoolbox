package eu.ascetic.experimentmanager.plugin.launchers.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import eu.ascetic.experimentmanager.plugin.launchers.Constants;

public class SaveExperimentMainTab  extends AbstractLaunchConfigurationTab {
	
	protected Text skbUrl;
	protected Text experimentPath;
	
	
	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
	    setControl(mainComposite);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 1;
	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	    mainComposite.setLayout(layout);
	    mainComposite.setLayoutData(gridData);
	    mainComposite.setFont(parent.getFont());
	    
	    class Listener implements ModifyListener, SelectionListener {
	        public void modifyText(ModifyEvent e) {
	          entriesChanged();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) {
	          entriesChanged();
	        }

	        public void widgetSelected(SelectionEvent e) {
	          entriesChanged();
	        }
	      }
	      Listener modyfyingListener = new Listener();
	    
	    Label skbLabel = new Label(mainComposite, SWT.NONE);
	    skbLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));
	    skbLabel.setText("Url to SaaS Knowledge Base Service:");
	    
	    this.skbUrl = new Text(mainComposite, SWT.BORDER);
	    this.skbUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
	    this.skbUrl.addModifyListener(modyfyingListener);
	      
	    Label experimentPath = new Label(mainComposite, SWT.NONE);
	    experimentPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));
	    experimentPath.setText("Path to experiment specification file :");
	    
	    this.experimentPath = new Text(mainComposite, SWT.BORDER);
	    this.experimentPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
	    this.experimentPath.addModifyListener(modyfyingListener);
	    
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Main panel";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		String baseDir = getBaseDir();
		try {
			skbUrl.setText(configuration.getAttribute(Constants.SKB_URL, "http://locahost:8080"));
			experimentPath.setText(configuration.getAttribute(Constants.EXPERIMENT_FILEPATH,"/experiment-manager/experiments.xml"));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	
	private String getBaseDir(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        if (firstElement instanceof IAdaptable)
	        {
	            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
	            return project.getLocation().toString();
	        }
	    }
	    return "";
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(Constants.EXPERIMENT_FILEPATH, experimentPath.getText());
		configuration.setAttribute(Constants.SKB_URL, skbUrl.getText());
	}
	
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy arg0) {
		// TODO Auto-generated method stub
		
	}
	
	void entriesChanged() {
	    setDirty(true);
	    updateLaunchConfigurationDialog();
	}

}
