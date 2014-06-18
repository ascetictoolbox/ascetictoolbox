package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.ServiceInfo;
import net.atos.ari.seip.CloudManager.db.utils.HibernateUtil;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ServiceInfoDAO {
	private final static Logger logger = Logger.getLogger(ServiceInfoDAO.class);

	public boolean addService(String serviceId, String serviceManifest) throws Exception {
		Session session = null;
		ServiceInfo si = new ServiceInfo();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			si.setServiceId(serviceId);
			si.setServiceManifest(serviceManifest);
			session.save(si);
			tx.commit();
			if (tx.wasCommitted()) {
				logger.info("Transaction commited");
			}
		} catch (Exception e) {
			logger.info("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (HibernateException e) {
				}
			}
		}
		sf.close();
		return true;
	}

	@SuppressWarnings("unchecked")
	public ServiceInfo getService(String key) throws Exception {
		Session session = null;
		List<ServiceInfo> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID
			Criteria criteria2 = session.createCriteria(ServiceInfo.class);
			criteria2.add(Restrictions.like("serviceId", key));
			results = (List<ServiceInfo>) criteria2.list();
			tx.commit();
			return results.get(results.size()-1);
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}

		
	}

	@SuppressWarnings("unchecked")
	public List<ServiceInfo> getServices() throws Exception {
		Session session = null;
		List<ServiceInfo> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Criteria criteria = session.createCriteria(ServiceInfo.class);
			results = (List<ServiceInfo>) criteria.list();
			tx.commit();
			return results;
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}

		
	}

	public boolean deleteService(String service) throws Exception {
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		int result = 0;
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Query query = session
					.createSQLQuery("DELETE FROM service_info WHERE service_id =:service");
			query.setParameter("service", service);
			result = query.executeUpdate();
			tx.commit();
			sf.close();
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}

		if (result == 0)
			return false;

		return true;
	}
}
