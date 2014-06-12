package net.atos.ari.seip.CloudManager.db.DAO;

import net.atos.ari.seip.CloudManager.db.model.ServiceInfo;
import net.atos.ari.seip.CloudManager.db.model.ServiceStatus;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceStatusDAO {

	final static Logger logger = LoggerFactory
			.getLogger(ServiceStatusDAO.class.getName());

	public boolean addServiceStatus(String serviceId, String serviceStatus)
			throws Exception {
		Session session = null;
		ServiceStatus ss = new ServiceStatus();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			ss.setIdserviceStatus(null);
			ss.setServiceInfo(getServiceInfo(serviceId));
			ss.setServiceStatus(serviceStatus);
			ss.setTime(null);			
			session.save(ss);
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
