package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.IntanceInfo;
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

public class InstanceInfoDAO {
	private final static Logger logger = Logger.getLogger(InstanceInfoDAO.class);

	public boolean addInstance(String instanceId, String serviceId)
			throws Exception {
		Session session = null;
		IntanceInfo ii = new IntanceInfo();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			ii.setIdintanceInfo(null);
			ii.setInstanceId(instanceId);
			ii.setServiceInfo(getServiceInfo(serviceId));
			session.save(ii);
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
	public IntanceInfo getInstance(String key) throws Exception {
		Session session = null;
		List<IntanceInfo> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Instance ID
			Criteria criteria2 = session.createCriteria(IntanceInfo.class);
			criteria2.add(Restrictions.like("InstanceId", key));
			results = (List<IntanceInfo>) criteria2.list();
			tx.commit();
			
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}finally {
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

	@SuppressWarnings("unchecked")
	public List<IntanceInfo> getInstances() throws Exception {
		Session session = null;
		List<IntanceInfo> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Criteria criteria = session.createCriteria(IntanceInfo.class);
			results = (List<IntanceInfo>) criteria.list();
			tx.commit();
			
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}finally {
			if (session != null) {
				try {
					session.close();
				} catch (HibernateException e) {
				}
			}
		}
		sf.close();
		return results;
	}

	public boolean deleteInstance(String instance) throws Exception {
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		int result = 0;
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Query query = session
					.createSQLQuery("DELETE FROM intance_Info WHERE instance_id =:instance");
			query.setParameter("instance", instance);
			result = query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}finally {
			if (session != null) {
				try {
					session.close();
				} catch (HibernateException e) {
				}
			}
		}
		sf.close();

		if (result == 0)
			return false;

		return true;
	}
	
	
	
	//FIXME: It works...need to be double-checked
	@SuppressWarnings("unchecked")
	public List<String> getInstancesByServiceID(String serviceID) throws Exception {
		Session session = null;
		List<String> list = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();

			Query query = session.createSQLQuery("SELECT * FROM intance_info WHERE service_id =:serviceID").addScalar("instance_id");
			query.setParameter("serviceID", serviceID);
			list = query.list();
			
			tx.commit();
			
		} catch (Exception e) {
			logger.error("ERROR " + e.getMessage());
			throw new Exception(e.toString());
		}finally {
			if (session != null) {
				try {
					session.close();
				} catch (HibernateException e) {
				}
			}
		}
		sf.close();
		return list;
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
