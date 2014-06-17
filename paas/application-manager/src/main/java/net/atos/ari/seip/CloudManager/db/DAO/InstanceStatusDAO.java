package net.atos.ari.seip.CloudManager.db.DAO;

import net.atos.ari.seip.CloudManager.db.model.InstanceStatus;
import net.atos.ari.seip.CloudManager.db.model.IntanceInfo;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class InstanceStatusDAO {
	private final static Logger logger = Logger.getLogger(InstanceStatusDAO.class);

	public boolean addInstanceStatus(String instanceId, String instanceStatus)
			throws Exception {
		Session session = null;
		InstanceStatus is = new InstanceStatus();
		SessionFactory sf = net.atos.ari.seip.CloudManager.db.utils.HibernateUtil
				.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			is.setIdinstanceStatus(null);
			is.setIntanceInfo(getIsntanceInfo(instanceId));
			is.setInstanceStatus(instanceStatus);
			is.setTime(null);			
			session.save(is);
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

	private IntanceInfo getIsntanceInfo(String instanceId) {
		InstanceInfoDAO iidao = new InstanceInfoDAO();
		try {
			return iidao.getInstance(instanceId);
		} catch (Exception e) {
			logger.error("Instance instanceId: "+instanceId+" does not exist");
			return null;
		}
		
	}
}
