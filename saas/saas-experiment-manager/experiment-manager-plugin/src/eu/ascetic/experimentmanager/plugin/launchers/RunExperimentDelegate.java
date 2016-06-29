package eu.ascetic.experimentmanager.plugin.launchers;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import eu.ascetic.saas.experimentmanager.API;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;


public class RunExperimentDelegate extends JavaLaunchDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.JavaLaunchDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		String skburl          = configuration.getAttribute(Constants.SKB_URL, "");
		String deplName          = configuration.getAttribute(Constants.DEPLOYMENT_ID, "");
		String description          = configuration.getAttribute(Constants.SNAPSHOT_DESCRIPTION, "");
		String scopesPath      = configuration.getAttribute(Constants.SCOPE_FILEPATH, "");
		String experimentPath  = configuration.getAttribute(Constants.EXPERIMENT_FILEPATH, "");
		
		try {
			Experiment exp = API.loadExperiment(experimentPath);
			String expId = API.expId(skburl, exp);
			Snapshot s = API.run(expId,exp, deplName, description, scopesPath);
			API.persist(skburl, s);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
