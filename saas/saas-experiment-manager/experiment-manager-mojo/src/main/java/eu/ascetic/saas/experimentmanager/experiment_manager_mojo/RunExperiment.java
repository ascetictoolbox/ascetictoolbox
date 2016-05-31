package eu.ascetic.saas.experimentmanager.experiment_manager_mojo;

/**
 * @author Dimitri Durieux - CETIC
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import eu.ascetic.saas.experimentmanager.API;
import eu.ascetic.saas.experimentmanager.business.ExperimentAdaptator;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

import java.io.File;



/**
 * Challenges :
 * - get the path to resource file
 * - get properties specific
 */

/**
 * Goal which touches a timestamp file.
 *
 */
@Mojo( name = "run-experiment", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class RunExperiment
    extends AbstractMojo
{

    @Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "experimentDir", required = true )
    private String resourceDirectory;
    
    @Parameter(defaultValue = "${saasknowledgebase_url}", property = "skbUrl", required = true )
    private String skburl;
    
    @Parameter(defaultValue = "${target_depl_id}", property = "deplId", required = true )
    private String deplId;
    
    public void execute()
        throws MojoExecutionException
    {
		String scopesPath      = resourceDirectory + "/scopes.xml";
		String experimentPath  = resourceDirectory + "/experiments.xml";

		this.getLog().info("SaaS knowledge base URL is : "+skburl);
		this.getLog().info("Deployment identifier is : "+deplId);
		this.getLog().info("experiment path is : "+experimentPath);
		this.getLog().info("scope path is : "+scopesPath);
		
		this.getLog().info("Start loading experiment ...");
		Experiment exp = API.loadExperiment(experimentPath);
		this.getLog().info("Computing snapshot ...");
		Snapshot s = API.run(exp, deplId, scopesPath);
		try {
			this.getLog().info("Persisting snapshot ...");
			API.persist(skburl, s);
		} catch (ApiException e) {
			this.getLog().error(e);
			throw new MojoExecutionException("Error during computing experiment",e);
		}
    }
}
