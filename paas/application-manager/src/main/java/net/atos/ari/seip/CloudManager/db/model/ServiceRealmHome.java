package net.atos.ari.seip.CloudManager.db.model;
// Generated 17-Jun-2014 11:35:53 by Hibernate Tools 3.2.2.GA


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class ServiceRealm.
 * @see net.atos.ari.seip.CloudManager.db.model.ServiceRealm
 * @author Hibernate Tools
 */
public class ServiceRealmHome {

    private static final Log log = LogFactory.getLog(ServiceRealmHome.class);

    private final SessionFactory sessionFactory = getSessionFactory();
    
    protected SessionFactory getSessionFactory() {
        try {
            return (SessionFactory) new InitialContext().lookup("SessionFactory");
        }
        catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }
    
    public void persist(ServiceRealm transientInstance) {
        log.debug("persisting ServiceRealm instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(ServiceRealm instance) {
        log.debug("attaching dirty ServiceRealm instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(ServiceRealm instance) {
        log.debug("attaching clean ServiceRealm instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(ServiceRealm persistentInstance) {
        log.debug("deleting ServiceRealm instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public ServiceRealm merge(ServiceRealm detachedInstance) {
        log.debug("merging ServiceRealm instance");
        try {
            ServiceRealm result = (ServiceRealm) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public ServiceRealm findById( java.lang.Integer id) {
        log.debug("getting ServiceRealm instance with id: " + id);
        try {
            ServiceRealm instance = (ServiceRealm) sessionFactory.getCurrentSession()
                    .get("net.atos.ari.seip.CloudManager.db.model.ServiceRealm", id);
            if (instance==null) {
                log.debug("get successful, no instance found");
            }
            else {
                log.debug("get successful, instance found");
            }
            return instance;
        }
        catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    public List findByExample(ServiceRealm instance) {
        log.debug("finding ServiceRealm instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("net.atos.ari.seip.CloudManager.db.model.ServiceRealm")
                    .add(Example.create(instance))
            .list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        }
    } 
}

