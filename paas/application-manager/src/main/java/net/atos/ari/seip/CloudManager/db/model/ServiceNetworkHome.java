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
 * Home object for domain model class ServiceNetwork.
 * @see net.atos.ari.seip.CloudManager.db.model.ServiceNetwork
 * @author Hibernate Tools
 */
public class ServiceNetworkHome {

    private static final Log log = LogFactory.getLog(ServiceNetworkHome.class);

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
    
    public void persist(ServiceNetwork transientInstance) {
        log.debug("persisting ServiceNetwork instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(ServiceNetwork instance) {
        log.debug("attaching dirty ServiceNetwork instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(ServiceNetwork instance) {
        log.debug("attaching clean ServiceNetwork instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(ServiceNetwork persistentInstance) {
        log.debug("deleting ServiceNetwork instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public ServiceNetwork merge(ServiceNetwork detachedInstance) {
        log.debug("merging ServiceNetwork instance");
        try {
            ServiceNetwork result = (ServiceNetwork) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public ServiceNetwork findById( java.lang.Integer id) {
        log.debug("getting ServiceNetwork instance with id: " + id);
        try {
            ServiceNetwork instance = (ServiceNetwork) sessionFactory.getCurrentSession()
                    .get("net.atos.ari.seip.CloudManager.db.model.ServiceNetwork", id);
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
    
    public List findByExample(ServiceNetwork instance) {
        log.debug("finding ServiceNetwork instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("net.atos.ari.seip.CloudManager.db.model.ServiceNetwork")
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

