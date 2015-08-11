package eu.ascetic.saas.applicationpackager.ide.wizards;

import org.eclipse.jface.wizard.Wizard;

public class ApplicationPackagerWizard extends Wizard {

	protected XmlFileSelectorWizardPage one;
	protected XmlFileSelectedViewerWizardPage two;
	protected OvfCodeGeneratedViewerWizardPage three;
	protected OvfResponseVmicViewerWizardPage four;
	protected XmlResponseAppManViewerWizardPage five;

	public ApplicationPackagerWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return "ASCETiC Application Packager";
	}

	@Override
	public void addPages() {
		one = new XmlFileSelectorWizardPage();
		two = new XmlFileSelectedViewerWizardPage();
		three = new OvfCodeGeneratedViewerWizardPage();
		four = new OvfResponseVmicViewerWizardPage();
		five = new XmlResponseAppManViewerWizardPage();
		addPage(one);
		addPage(two);
		addPage(three);
		addPage(four);
		addPage(five);
	}

	
	@Override
	public boolean performFinish() {
		// Print the result to the console
		System.out.println(one.getText1());
		System.out.println(two.getText1());
		return true;
	}
	
}
