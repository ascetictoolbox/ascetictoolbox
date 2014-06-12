package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.Networks;
import net.atos.ari.seip.CloudManager.db.utils.HibernateUtil;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworksDAO {

	final static Logger logger = LoggerFactory
			.getLogger(NetworksDAO.class.getName());

	public boolean addNetwork(String netwokId, String serviceId, String networkRange, String networkType)
			throws Exception {
		Session session = null;
		Networks net = new Networks();
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			net.setIdnetworks(null);
			net.setNetwokId(netwokId);
			net.setServiceId(serviceId);
			net.setNetworkRange(networkRange);
			net.setNetworkType(networkType);
			session.save(net);
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
	
	public Networks getNetwork(String networkId) throws Exception{
		Session session = null;
		List<Networks> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID & realm ID
			Criteria criteria = session.createCriteria(Networks.class);
			criteria.add(Restrictions.like("networkId", networkId));
			results = (List<Networks>) criteria.list();
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
	
	public boolean deleteNetwork(String networkId) throws Exception{
		Session session = null;
		List<Networks> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID
			Criteria criteria = session.createCriteria(Networks.class);
			criteria.add(Restrictions.like("serviceInfo", networkId));
			results = (List<Networks>) criteria.list();
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
	
	public boolean updateNetworkRange(String networkdId, String networkRange) throws Exception{
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Networks net = getNetwork(networkdId);
			net.setNetworkRange(networkRange);
			session.update(net);
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
	
	public boolean updateNetworkType(String networkdId, String networkType) throws Exception{
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Networks net = getNetwork(networkdId);
			net.setNetworkType(networkType);
			session.update(net);
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
}
		