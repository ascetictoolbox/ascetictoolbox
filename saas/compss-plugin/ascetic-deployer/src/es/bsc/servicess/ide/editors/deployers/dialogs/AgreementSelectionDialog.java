package es.bsc.servicess.ide.editors.deployers.dialogs;

import static es.bsc.servicess.ide.Constants.METADATA_FILENAME;
import static es.bsc.servicess.ide.Constants.METADATA_FOLDER;
import static es.bsc.servicess.ide.Titles.METHOD_LABEL;
import static es.bsc.servicess.ide.Titles.SELECT_W_BUTTON_TITLE;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import es.bsc.servicess.ide.Activator;
import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.ProjectMetadataUtils;
import es.bsc.servicess.ide.model.Dependency;
import es.bsc.servicess.ide.model.OrchestrationElement;
import es.bsc.servicess.ide.model.Parameter;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.saas.application_uploader.ApplicationUploader;

public class AgreementSelectionDialog extends Dialog {
	private List<Agreement> agreements;
	private Table agreementsTable;
	private String selectedAgreement = null;
	
	private static Logger log = Logger.getLogger(AgreementSelectionDialog.class);
	
	public AgreementSelectionDialog(Shell parent, List<Agreement> agreements) {
		super(parent);
		this.agreements = agreements;
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridData rd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		rd.grabExcessHorizontalSpace = true;
		comp.setLayoutData(rd);
		comp.setLayout(new GridLayout(1, false));
		
		Label methodLabel = new Label(comp, SWT.NONE);
		methodLabel.setText("Available agreements:");
		agreementsTable = new Table(comp, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		agreementsTable.setHeaderVisible(true);
		agreementsTable.setLinesVisible(true);
		TableColumn agreementID = new TableColumn(agreementsTable, SWT.NULL);
		agreementID.setText("Identifier");
		TableColumn agreementPrice = new TableColumn(agreementsTable, SWT.NULL);
		agreementPrice.setText("Description");
		TableColumn agreementDescription = new TableColumn(agreementsTable, SWT.NULL);
		agreementDescription.setText("Description");
		agreementDescription.pack();
		rd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		rd.grabExcessHorizontalSpace = true;
		rd.grabExcessVerticalSpace = true;
		rd.minimumHeight = 90;
		agreementsTable.setLayoutData(rd);
		addAgreements();
		agreementID.pack();
		agreementPrice.pack();
		agreementDescription.pack();
		agreementsTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				selectedAgreement = agreementsTable.getItem(agreementsTable.getSelectionIndex()).getText(0);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selectedAgreement = agreementsTable.getItem(agreementsTable.getSelectionIndex()).getText(0);
			}
		});
		return comp;
	}
	
	private void addAgreements() {
		for(Agreement ag:agreements){
			TableItem it = new TableItem(agreementsTable, SWT.NONE);
			String id = Integer.toString(ag.getId());
			String price = ag.getPrice();
			if (price==null){
				price="N/A";
			}
			String[] text = new String[]{id, price, ag.getSlaAgreement()};
			it.setText(text);
		}
		
	}

	public String getSelectedAgreement() {
		return selectedAgreement;
	}


}
