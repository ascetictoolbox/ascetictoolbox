package eu.ascetic.saas.experimentmanager.experiment_manager_mojo;

/**
 * @author Dimitri Durieux - CETIC
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import eu.ascetic.saas.experimentmanager.API;
import eu.ascetic.saas.experimentmanager.business.ExperimentAdaptator;
import eu.ascetic.saas.experimentmanager.exception.AlreadyExistException;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;



/**
 * Goal which touches a timestamp file.
 *
 */
@Mojo( name = "save-experiment", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class SaveExperiment
    extends AbstractMojo
{
	@Parameter(defaultValue = "${saasknowledgebase_url}", property = "skburl", required = true )
    private String skburl;
	
    @Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "resourceDirectory", required = true )
    private String resourceDirectory;
    
    public void execute()
        throws MojoExecutionException
    {    
		String experimentPath  = resourceDirectory + "/experiments.xml";
		
		this.getLog().info("SaaS knowledge base URL is : "+skburl);
		this.getLog().info("experiment path is : "+experimentPath);
		
		this.getLog().info("Loading experiment ...");
		Experiment exp = API.loadExperiment(experimentPath);
		try {
			this.getLog().info("Persisting experiment ...");
			API.persist(skburl,ExperimentAdaptator.getExperiment(exp));
		} catch (ApiException e) {
			this.getLog().error(e);
			throw new MojoExecutionException("Error during computing experiment",e);
		} catch (AlreadyExistException e) {
			this.getLog().error(e);
			throw new MojoExecutionException("Can't persist the experiment!",e);
		}
    }
}
