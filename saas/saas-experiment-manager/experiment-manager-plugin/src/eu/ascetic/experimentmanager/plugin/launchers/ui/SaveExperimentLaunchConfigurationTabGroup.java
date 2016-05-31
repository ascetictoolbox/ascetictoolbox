package eu.ascetic.experimentmanager.plugin.launchers.ui;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class SaveExperimentLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
		List<ILaunchConfigurationTab> tabs = new ArrayList<ILaunchConfigurationTab>();

	    tabs.add(new SaveExperimentMainTab());
	    tabs.add(new EnvironmentTab());
	    tabs.add(new CommonTab());

	    setTabs(tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}

}
