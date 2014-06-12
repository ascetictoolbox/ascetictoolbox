package net.atos.ari.seip.CloudManager.db.model;
// Generated 05-May-2014 15:45:17 by Hibernate Tools 3.2.2.GA


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class ServiceStatus.
 * @see net.atos.ari.seip.CloudManager.db.model.ServiceStatus
 * @author Hibernate Tools
 */
public class ServiceStatusHome {

    private static final Log log = LogFactory.getLog(ServiceStatusHome.class);

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
    
    public void persist(ServiceStatus transientInstance) {
        log.debug("persisting ServiceStatus instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(ServiceStatus instance) {
        log.debug("attaching dirty ServiceStatus instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(ServiceStatus instance) {
        log.debug("attaching clean ServiceStatus instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(ServiceStatus persistentInstance) {
        log.debug("deleting ServiceStatus instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public ServiceStatus merge(ServiceStatus detachedInstance) {
        log.debug("merging ServiceStatus instance");
        try {
            ServiceStatus result = (ServiceStatus) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public ServiceStatus findById( java.lang.Integer id) {
        log.debug("getting ServiceStatus instance with id: " + id);
        try {
            ServiceStatus instance = (ServiceStatus) sessionFactory.getCurrentSession()
                    .get("net.atos.ari.seip.CloudManager.db.model.ServiceStatus", id);
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
    
    public List findByExample(ServiceStatus instance) {
        log.debug("finding ServiceStatus instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("net.atos.ari.seip.CloudManager.db.model.ServiceStatus")
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

