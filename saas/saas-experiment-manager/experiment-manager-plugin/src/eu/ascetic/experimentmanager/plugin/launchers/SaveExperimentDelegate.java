package eu.ascetic.experimentmanager.plugin.launchers;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import eu.ascetic.saas.experimentmanager.API;
import eu.ascetic.saas.experimentmanager.business.ExperimentAdaptator;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;


public class SaveExperimentDelegate extends JavaLaunchDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.JavaLaunchDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		String skbUrl   = configuration.getAttribute(Constants.SKB_URL, "");        
		String experimentPath  = configuration.getAttribute(Constants.EXPERIMENT_FILEPATH, "");
		
		Experiment exp = API.loadExperiment(experimentPath);
		try {
			API.persist(skbUrl,ExperimentAdaptator.getExperiment(exp));
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
