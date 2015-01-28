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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.model.Application;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 */

@Service("ApplicationService")
public class ApplicationDAOJpa implements ApplicationDAO {
	private static Logger logger = Logger.getLogger(ApplicationDAOJpa.class);
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
	public Application getById(int id) {
		return entityManager.find(Application.class, id);
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<Application> getAll() {
		Query query = entityManager.createNamedQuery("Application.findAll");
		@SuppressWarnings("unchecked")
		List<Application> applications = query.getResultList();
		return applications;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean save(Application application) {
		entityManager.persist(application);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean update(Application application) {
		entityManager.merge(application);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean delete(Application application) {
		try {
			application = entityManager.getReference(Application.class, application.getId());
			entityManager.remove(application);
			entityManager.flush();
			return true;
		} catch(EntityNotFoundException e) {
			logger.debug(e);
			return false;
		} 
	}

	@Override
	public Application getByName(String name) {
		
		Query query = entityManager.createQuery("SELECT a FROM Application a WHERE a.name = :appName");
		query.setParameter("appName", name);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Application> applications = query.getResultList();
		
		if(applications.size() > 0) {
			return applications.get(0);
		} else {
			return null;
		}
	}
}



