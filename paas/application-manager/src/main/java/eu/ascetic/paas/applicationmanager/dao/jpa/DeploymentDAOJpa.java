package eu.ascetic.paas.applicationmanager.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */

@Service("DeploymentService")
public class DeploymentDAOJpa implements DeploymentDAO {
	private static Logger logger = Logger.getLogger(DeploymentDAOJpa.class);
	private EntityManager entityManager;

	@PersistenceContext (unitName = "applicationManagerDB")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public Deployment getById(int id) {
		return entityManager.find(Deployment.class, id);
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<Deployment> getAll() {
		Query query = entityManager.createNamedQuery("Deployment.findAll");
		@SuppressWarnings("unchecked")
		List<Deployment> deployments = query.getResultList();
		return deployments;
	}
	
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean save(Deployment deployment) {
		entityManager.persist(deployment);
		//entityManager.merge(deployment);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean update(Deployment deployment) {
		entityManager.merge(deployment);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean delete(Deployment deployment) {
		try {
			deployment = entityManager.getReference(Deployment.class, deployment.getId());
			entityManager.remove(deployment);
			entityManager.flush();
			return true;
		} catch(EntityNotFoundException e) {
			logger.debug(e);
			return false;
		} 
	}

	@Override
	public List<Deployment> getDeploymentsForApplicationWithStatus(Application application, String status) {
		Query query = entityManager.createQuery("SELECT d FROM Deployment d WHERE d.application = :application AND d.status = :status");
		query.setParameter("application", application);
		query.setParameter("status", status);
		@SuppressWarnings("unchecked")
		List<Deployment> deployments = query.getResultList();
		
		return deployments;
	}

	@Override
	public Deployment getDeployment(String deploymentName) {
		Query query = entityManager.createQuery("SELECT d FROM Deployment d WHERE d.deploymentName = :deploymentName");
		query.setParameter("deploymentName", deploymentName);
		
		try {
			return (Deployment) query.getSingleResult();
		} catch(NoResultException ex) {
			return null;
		}
	}

	@Override
	public List<Deployment> getDeploymentsWithStatus(String status) {
		Query query = entityManager.createQuery("SELECT d FROM Deployment d WHERE d.status = :status");
		query.setParameter("status", status);
		@SuppressWarnings("unchecked")
		List<Deployment> deployments = query.getResultList();
		
		return deployments;
	}
}



