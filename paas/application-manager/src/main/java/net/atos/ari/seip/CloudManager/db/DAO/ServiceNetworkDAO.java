package net.atos.ari.seip.CloudManager.db.DAO;

import java.util.ArrayList;
import java.util.List;

import net.atos.ari.seip.CloudManager.db.model.Networks;
import net.atos.ari.seip.CloudManager.db.model.ServiceInfo;
import net.atos.ari.seip.CloudManager.db.model.ServiceNetwork;
import net.atos.ari.seip.CloudManager.db.utils.HibernateUtil;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceNetworkDAO {

	final static Logger logger = LoggerFactory
			.getLogger(ServiceNetworkDAO.class.getName());

	public boolean addNetwork(String networkId, String serviceId)
			throws Exception {
		Session session = null;
		ServiceNetwork snet = new ServiceNetwork();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			snet.setIdserviceNetwork(null); 
			snet.setNetworks(getNetworks(networkId));
			snet.setServiceInfo(getServiceInfo(serviceId));
			session.save(snet);
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
	
	public ServiceNetwork getServiceNetworkByNetworkId(String networkId) throws Exception{
		Session session = null;
		ServiceNetwork result = new ServiceNetwork();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Criteria criteria = session.createCriteria(ServiceNetwork.class);
			criteria.add(Restrictions.like("networks",getNetworks(networkId)));
			result = (ServiceNetwork)criteria.uniqueResult();
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
		return result;
	}
	
	
	public List<ServiceNetwork> getServiceNetworks(String networkId) throws Exception{
		Session session = null;
		List<ServiceNetwork> result = new ArrayList<ServiceNetwork>();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Criteria criteria = session.createCriteria(ServiceNetwork.class);
			criteria.add(Restrictions.like("networks",getNetworks(networkId)));
			result = (List<ServiceNetwork>)criteria.list();
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
		return result;
	}
	
	public boolean updateSeriveNetwork(String serviceId, String oldnet, String newNet) throws Exception{
		Session session = null;
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			
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
	
	public boolean deleteServiceNetwork(String networkId) throws Exception{
		Session session = null;
		List<ServiceNetwork> results = null;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			// Search by Service ID
			Criteria criteria = session.createCriteria(ServiceNetwork.class);
			criteria.add(Restrictions.like("networks", getServiceNetworkByNetworkId(networkId)));
			results = (List<ServiceNetwork>) criteria.list();
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
	
	private ServiceInfo getServiceInfo(String serviceId){
		ServiceInfoDAO sidao = new ServiceInfoDAO();
		try {
			return sidao.getService(serviceId);
		} catch (Exception e) {
			logger.error("Service with serviceId: "+serviceId+" does not exist");
			return null;
		}
	} 
	
	private Networks getNetworks(String networkId){
		NetworksDAO netdao = new NetworksDAO();
		try {
			return netdao.getNetwork(networkId);
		} catch (Exception e) {
			logger.error("Network with networkId: "+networkId+" does not exist");
			return null;
		}
	} 
}
