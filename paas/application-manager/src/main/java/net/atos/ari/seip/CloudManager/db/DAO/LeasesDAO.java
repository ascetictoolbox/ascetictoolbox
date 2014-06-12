package net.atos.ari.seip.CloudManager.db.DAO;

import net.atos.ari.seip.CloudManager.db.model.Leases;
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

public class LeasesDAO {

	final static Logger logger = LoggerFactory.getLogger(LeasesDAO.class
			.getName());

	public boolean addLease(String ip, String mac, String networkId, String type)
			throws Exception {
		Session session = null;
		Leases lea = new Leases();
		boolean added = false;
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			lea.setIdleases(null);
			lea.setIp(ip);
			lea.setMac(mac);
			lea.setNetworks(getNetworks(networkId));
			lea.setType(type);
			session.save(lea);
			tx.commit();
			if (tx.wasCommitted()) {
				logger.info("Transaction commited");
			}
			added = true;
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
		return added;
	}

	public Leases getlease(String ip, String networkId) throws Exception {
		Session session = null;
		Leases lea = new Leases();
		SessionFactory sf = HibernateUtil.getSessionFactory();
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			Criteria criteria = session.createCriteria(Leases.class);
			criteria.add(Restrictions.and(Restrictions.like("ip", ip),
					Restrictions.like("networks", getNetworks(networkId))));
			lea = (Leases) criteria.uniqueResult();
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
		return lea;
	}

	public boolean deleteLease(String ip, String networkId) throws Exception {

		Session session = null;
		Leases lea = new Leases();
		SessionFactory sf = HibernateUtil.getSessionFactory();
		boolean deleted = false;
		lea = getlease(ip, networkId);
		try {
			session = sf.openSession();
			Transaction tx = session.beginTransaction();
			tx.begin();
			session.delete(lea);
			tx.commit();
			if (tx.wasCommitted()) {
				logger.info("Transaction commited");
			}
			deleted = true;
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
		return deleted;
	}

	private Networks getNetworks(String networkId) throws Exception {
		NetworksDAO ndao = new NetworksDAO();
		return ndao.getNetwork(networkId);
	}
}
