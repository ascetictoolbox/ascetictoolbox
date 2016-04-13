package eu.ascetic.saas.applicationpackager.ide.wizards;

import org.eclipse.jface.wizard.Wizard;
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
 * This class is the Application Packager wizard
 *
 */

public class ApplicationPackagerWizard extends Wizard {

	/** The one. */
	protected XmlFileSelectorWizardPage one;
	
	/** The two. */
	protected XmlFileSelectedViewerWizardPage two;
	
	/** The three. */
	protected OvfCodeGeneratedViewerWizardPage three;
	
	/** The four. */
	protected OvfResponseVmicViewerWizardPage four;
	
	/** The five. */
	protected XmlResponseAppManViewerWizardPage five;

	/**
	 * Instantiates a new application packager wizard.
	 */
	public ApplicationPackagerWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		return "ASCETiC Application Packager";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
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

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Print the result to the console
//		System.out.println(one.getText1());
//		System.out.println(two.getText1());
		return true;
	}
	
}
