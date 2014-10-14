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

import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Image;

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
 * @email david.garciaperez@atos.net 
 * 
 * JPA implementation of the ImageDAO interface
 */

@Service("ImageService")
public class ImageDAOJpa implements ImageDAO {
	private static Logger logger = Logger.getLogger(ImageDAOJpa.class);
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
	public Image getById(int id) {
		return entityManager.find(Image.class, id);
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public List<Image> getAll() {
		Query query = entityManager.createNamedQuery("Image.findAll");
		@SuppressWarnings("unchecked")
		List<Image> images = query.getResultList();
		return images;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean save(Image image) {
		entityManager.persist(image);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean update(Image image) {
		entityManager.merge(image);
		entityManager.flush();
		return true;
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public boolean delete(Image image) {
		try {
			image = entityManager.getReference(Image.class, image.getId());
			entityManager.remove(image);
			entityManager.flush();
			return true;
		} catch(EntityNotFoundException e) {
			logger.debug(e);
			return false;
		} 
	}

	@Override
	public Image getLastImageWithOvfId(String ovfId) {
		Query query = entityManager.createQuery("from Image where ovfId = :ovfIdentifier order by id desc").setMaxResults(1);
		query.setParameter("ovfIdentifier", ovfId);
		Image image = (Image) query.getSingleResult();
		return image;
	}

	@Override
	public Image getDemoCacheImage(String ovfId, String ovfHref) {
		try {
			Query query = entityManager.createQuery("from Image where ovfId = :ovfIdentifier and ovfHref = :ovfReference and demo = true order by id desc").setMaxResults(1);
			query.setParameter("ovfIdentifier", ovfId);
			query.setParameter("ovfReference", ovfHref);
			Image image = (Image) query.getSingleResult();
			return image;
		} catch(NoResultException ex) {
			return null;
		}
	}
}



