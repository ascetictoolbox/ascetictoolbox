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

import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.VM;

@Service("VMService")
public class VMDAOJpa implements VMDAO {
	private static Logger logger = Logger.getLogger(VMDAOJpa.class);
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
	public VM getById(int id) {
		return entityManager.find(VM.class, id);
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<VM> getAll() {
		Query query = entityManager.createNamedQuery("VM.findAll");
		@SuppressWarnings("unchecked")
		List<VM> vms = query.getResultList();
		return vms;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean save(VM vm) {
		entityManager.persist(vm);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean update(VM vm) {
		entityManager.merge(vm);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean delete(VM vm) {
		try {
			vm = entityManager.getReference(VM.class, vm.getId());
			entityManager.remove(vm);
			entityManager.flush();
			return true;
		} catch(EntityNotFoundException e) {
			logger.debug(e);
			return false;
		} 
	}
}



