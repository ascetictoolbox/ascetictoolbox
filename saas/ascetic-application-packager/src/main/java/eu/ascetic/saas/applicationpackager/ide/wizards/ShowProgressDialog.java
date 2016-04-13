package eu.ascetic.saas.applicationpackager.ide.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ShowProgressDialog extends Dialog {

	protected ShowProgressDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	public Composite createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(1, false));
		GridData rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		rd.grabExcessHorizontalSpace = true;
		comp.setLayoutData(rd);
		Label textLabel = new Label(comp, SWT.NONE);
		textLabel.setText("Operation in progress");
		
		ProgressBar progressBar = new ProgressBar(comp, SWT.SMOOTH);
	    progressBar.setBounds(100, 10, 200, 20);
	    progressBar.setVisible(false);
	    progressBar.setSelection(0);
		progressBar.setMaximum(100);
		
		
		return comp;
		
	}
}
