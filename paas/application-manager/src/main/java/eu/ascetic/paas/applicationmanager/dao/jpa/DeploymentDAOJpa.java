package eu.ascetic.paas.applicationmanager.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;

@Service("DeploymentService")
public class DeploymentDAOJpa implements DeploymentDAO {
	private static Logger logger = Logger.getLogger(DeploymentDAOJpa.class);
	private EntityManager entityManager;

	@PersistenceContext (unitName = "aplicationManagerDB")
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
}



