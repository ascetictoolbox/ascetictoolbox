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
 * Home object for domain model class Leases.
 * @see net.atos.ari.seip.CloudManager.db.model.Leases
 * @author Hibernate Tools
 */
public class LeasesHome {

    private static final Log log = LogFactory.getLog(LeasesHome.class);

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
    
    public void persist(Leases transientInstance) {
        log.debug("persisting Leases instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(Leases instance) {
        log.debug("attaching dirty Leases instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(Leases instance) {
        log.debug("attaching clean Leases instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(Leases persistentInstance) {
        log.debug("deleting Leases instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public Leases merge(Leases detachedInstance) {
        log.debug("merging Leases instance");
        try {
            Leases result = (Leases) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public Leases findById( java.lang.Integer id) {
        log.debug("getting Leases instance with id: " + id);
        try {
            Leases instance = (Leases) sessionFactory.getCurrentSession()
                    .get("net.atos.ari.seip.CloudManager.db.model.Leases", id);
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
    
    public List findByExample(Leases instance) {
        log.debug("finding Leases instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("net.atos.ari.seip.CloudManager.db.model.Leases")
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

