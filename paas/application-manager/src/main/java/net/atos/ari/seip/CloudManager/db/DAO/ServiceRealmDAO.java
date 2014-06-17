package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.ServiceInfo;
import net.atos.ari.seip.CloudManager.db.model.ServiceRealm;
import net.atos.ari.seip.CloudManager.db.utils.HibernateUtil;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;


public class ServiceRealmDAO {
	private final static Logger logger = Logger.getLogger(ServiceRealmDAO.class);

	public boolean addServiceRealm(String serviceId, String serviceRealm)
			throws Exception {
		Session session = null;
		ServiceRealm sr = new ServiceRealm();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			sr.setIdserviceRealm(null);
			sr.setServiceInfo(getServiceInfo(serviceId));
			sr.setRealmId(serviceRealm);
			session.save(sr);
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
	
	public ServiceRealm getRealm(String serviceId, String realmId) throws Exception{
		Session session = null;
		List<ServiceRealm> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID & realm ID
			Criteria criteria = session.createCriteria(ServiceRealm.class);
			criteria.add(Restrictions.and(Restrictions.like("serviceInfo", getServiceInfo(serviceId)), Restrictions.like("realmId", realmId)));
			results = (List<ServiceRealm>) criteria.list();
			tx.commit();
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
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
		return results.get(results.size()-1);
	}
	
	public boolean deleteServiceRealm(String serviceId, String realmId) throws Exception{
		Session session = null;
		List<ServiceRealm> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID
			Criteria criteria = session.createCriteria(ServiceRealm.class);
			criteria.add(Restrictions.and(Restrictions.like("serviceInfo", getServiceInfo(serviceId)), Restrictions.like("realmId", realmId)));
			results = (List<ServiceRealm>) criteria.list();
			session.delete(results.get(results.size()-1));
			tx.commit();
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
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
	
	public boolean updateServiceRealm(String serviceId, String oldRealm,String newRealm) throws Exception{
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			ServiceRealm sr = getRealm(serviceId, oldRealm);
			sr.setRealmId(newRealm);
			session.update(sr);
			tx.commit();
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
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
	
	private ServiceInfo getServiceInfo(String serviceId){
		ServiceInfoDAO sidao = new ServiceInfoDAO();
		try {
			return sidao.getService(serviceId);
		} catch (Exception e) {
			logger.error("Service with serviceId: "+serviceId+" does not exist");
			return null;
		}
	} 
}
