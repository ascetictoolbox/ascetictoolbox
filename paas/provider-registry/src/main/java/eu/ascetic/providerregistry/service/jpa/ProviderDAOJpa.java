package eu.ascetic.providerregistry.service.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.providerregistry.service.ProviderDAO;

@Service("ProviderService")
public class ProviderDAOJpa implements ProviderDAO {
	private static Logger logger = Logger.getLogger(ProviderDAOJpa.class);
	private EntityManager entityManager;

	@PersistenceContext (unitName = "providerRegistryDB")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public Provider getById(int id) {
		return entityManager.find(Provider.class, id);
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<Provider> getAll() {
		Query query = entityManager.createNamedQuery("Provider.findAll");
		List<Provider> providers = null;
		providers = query.getResultList();
		return providers;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean save(Provider rpovider) {
		entityManager.persist(rpovider);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean update(Provider provider) {
		entityManager.merge(provider);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean delete(Provider provider) {
		try {
			provider = entityManager.getReference(Provider.class, provider.getId());
			entityManager.remove(provider);
			entityManager.flush();
			return true;
		} catch(EntityNotFoundException e) {
			logger.debug(e);
			return false;
		} 
	}
}


