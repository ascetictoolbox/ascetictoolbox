package eu.ascetic.experimentmanager.plugin.launchers;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;

public class ExperimentSourceLocator extends AbstractSourceLookupDirector {

	  public void initializeParticipants() {
	    // enabled source lookup participants are calculated dynamically by MavenLaunchDelegate
	  }
}