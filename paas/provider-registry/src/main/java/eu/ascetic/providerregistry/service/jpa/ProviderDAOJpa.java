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
 * @email david.garciaperez@atos.net 
 * 
 * Provider DAO Jpa from Provider Registry
 * 
 */
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


