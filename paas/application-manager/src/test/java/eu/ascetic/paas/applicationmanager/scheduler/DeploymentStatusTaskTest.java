package eu.ascetic.paas.applicationmanager.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;

public class DeploymentStatusTaskTest {
	
	/* // TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void checkDeploymentStatusTest() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
		Deployment deployment3 = new Deployment();
		deployment3.setId(3);
		deployment3.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATIED);
		Deployment deployment4 = new Deployment();
		deployment4.setId(4);
		deployment4.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
		
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		deployments.add(deployment1);
		deployments.add(deployment2);
		deployments.add(deployment3);
		deployments.add(deployment4);
		
		// The object will be updated in the database
		when(deploymentDAO.getAll()).thenReturn(deployments);
		when(deploymentDAO.update(deployment1)).thenReturn(true);
		when(deploymentDAO.update(deployment3)).thenReturn(true);
		when(deploymentDAO.update(deployment4)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		
		// We start to iterate over the list of deployments
		task.checkDeploymentStatus();
		
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, deployment1.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATION, deployment2.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, deployment3.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED, deployment4.getStatus());
		
		// We verify the calls to the DAO
		verify(deploymentDAO, times(1)).getAll();
		verify(deploymentDAO, times(1)).update(deployment1);
		verify(deploymentDAO, times(1)).update(deployment3);
		verify(deploymentDAO, times(1)).update(deployment4);
	} */

	// TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void testdeploymentSubmittedActions() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentSubmittedActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, deployment.getStatus());
		// We check that the DAO was called
		verify(deploymentDAO, times(1)).update(deployment);
	}
	
	// TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void testDeploymentAcceptAgreementActions() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentAcceptAgreementActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, deployment.getStatus());
		// We check that the DAO was called
		verify(deploymentDAO, times(1)).update(deployment);
	}
	
	/* // TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void testDeploymentStartContextualizationActions() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentStartContextualizationActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED, deployment.getStatus());
		// We check that the DAO was called
		verify(deploymentDAO, times(1)).update(deployment);
	} */
}
