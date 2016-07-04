package eu.ascetic.paas.applicationmanager.dao.jpa;

import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_DELETED;
import static eu.ascetic.paas.applicationmanager.Dictionary.STATE_VM_ACTIVE;

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
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;

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
 * 
 */

@Service("VMService")
public class VMDAOJpa implements VMDAO {
	private static Logger logger = Logger.getLogger(VMDAOJpa.class);
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
		//entityManager.merge(vm);
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

	@Override
	@SuppressWarnings("unchecked")
	public List<VM> getNotDeletedVMsWithImage(Image image) {
		Query query = entityManager.createQuery("select distinct a from VM a " +
												   "join a.images t where t.id = :id and a.status != :status");
		
		query.setParameter("status", STATE_VM_DELETED);
		query.setParameter("id", image.getId());
		return (List<VM>) query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public VM getVMWithProviderVMId(String proivderVMId, String providerId) {
		Query query = entityManager.createQuery("SELECT v FROM VM v " +
													"WHERE v.providerId = :providerId " + 
													"AND v.providerVmId = :providerVmId ");
		
		query.setParameter("providerId", providerId);
		query.setParameter("providerVmId", proivderVMId);
		
		List<VM> vms = (List<VM>) query.getResultList();
		
		if(vms == null || vms.size() < 1) return null;
		
		return vms.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<VM> getVMsWithOVfIdForDeploymentNotDeleted(String ovfId, int deploymentId) {
		Query query = entityManager.createQuery("SELECT v FROM VM v " +
				   								"WHERE v.status != :status " + 
				   								"AND v.ovfId = :ovfid " +
				   								"AND v.deployment.id = :id");

		query.setParameter("status", STATE_VM_DELETED);
		query.setParameter("id", deploymentId);
		query.setParameter("ovfid", ovfId);
		
		return (List<VM>) query.getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<VM> getVMsWithOvfIdAndActive(int deploymentId, String ovfId) {
		Query query = entityManager.createQuery("SELECT v FROM VM v " +
												"WHERE v.status = :status " + 
												"AND v.ovfId = :ovfid " +
												"AND v.deployment.id = :id");
		
		query.setParameter("status", STATE_VM_ACTIVE);
		query.setParameter("id", deploymentId);
		query.setParameter("ovfid", ovfId);

		return (List<VM>) query.getResultList();
	}
}



