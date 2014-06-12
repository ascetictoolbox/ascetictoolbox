package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.InstanceNetwork;
import net.atos.ari.seip.CloudManager.db.model.IntanceInfo;
import net.atos.ari.seip.CloudManager.db.model.Leases;
import net.atos.ari.seip.CloudManager.db.model.ServiceInfo;
import net.atos.ari.seip.CloudManager.db.model.ServiceNetwork;
import net.atos.ari.seip.CloudManager.db.utils.HibernateUtil;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceNetworkDAO {

	final static Logger logger = LoggerFactory
			.getLogger(InstanceNetworkDAO.class.getName());
	
	public boolean addInstanceNetwork(String instanceId, String networkType, String networkId, String ip)
			throws Exception {
		Session session = null;
		InstanceNetwork in = new InstanceNetwork();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			in.setIdinstanceNetwork(null);
			in.setIntanceInfo(getInstanceInfo(instanceId));
			in.setNetworkType(networkType);
			in.setLeases(getLeases(ip,networkId));
			in.setServiceNetwork(getServiceNetwork(networkId));
			session.save(in);
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
	public ServiceInfo getInstanceNetwork(String key) throws Exception {
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
	public List<ServiceInfo> getInstanceNetworks() throws Exception {
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

	public boolean deleteInstanceNetwork(String service) throws Exception {
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

	public boolean updateInstanceNetwork(String service, boolean deployed) throws Exception {
		Session session = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		int result = 0;
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			 Query query=
			 session.createSQLQuery("update service_info set deployed=:deploy where service_id=:service");
			query.setParameter("service", service);
			query.setParameter("deploy", deployed);
			result = query.executeUpdate();
			tx.commit();
			sf.close();
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
	
	private Leases getLeases(String ip, String networkId) throws Exception{
		LeasesDAO ldao = new LeasesDAO();
		return ldao.getlease(ip, networkId);
	}
	
	private IntanceInfo getInstanceInfo(String instanceId) throws Exception{
		InstanceInfoDAO iidao = new InstanceInfoDAO();
		return iidao.getInstance(instanceId);
	}

	private ServiceNetwork getServiceNetwork(String networkId) throws Exception{
		ServiceNetworkDAO sndao = new ServiceNetworkDAO();
		return sndao.getServiceNetworkByNetworkId(networkId);
	}	
}
