package es.bsc.servicess.ide.editors.deployers;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

import es.bsc.servicess.ide.editors.ISectionSaverCleaner;
import es.bsc.servicess.ide.editors.SaveResetButtonComposite;

public class SectionSaveResetButtonComposite extends SaveResetButtonComposite {

	private String type;
	private ISectionSaverCleaner deployer;

	public SectionSaveResetButtonComposite(Shell shell, FormToolkit toolkit, String type, ISectionSaverCleaner deployer) {
		super(shell,toolkit);
		this.type = type;
		this.deployer = deployer;
		
	}
	@Override
	protected void cleanDetails() {
			deployer.cleanDetails(type);
		
	}

	@Override
	protected void saveSection() throws Exception {
			deployer.saveSection(type);
		
	}

}
