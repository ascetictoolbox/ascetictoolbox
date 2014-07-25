package eu.ascetic.paas.applicationmanager.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;

/**
 * Common methods for all the rest APIs
 * @author David Garcia Perez - Atos
 *
 */
public abstract class AbstractRest {
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	
	protected Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		return builder.build();
	}
	
	/**
	 * Creates a new deployment for an Application and associated it to an specific ovf file
	 * @param ovf the ovf file associated to this deployment
	 */
	protected Deployment createDeploymentToApplication(String ovf) {
		Deployment deployment = new Deployment();
		deployment.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		deployment.setOvf(ovf);
		
		return deployment;
	}
}
