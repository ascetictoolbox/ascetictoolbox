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
 * Home object for domain model class IntanceInfo.
 * @see net.atos.ari.seip.CloudManager.db.model.IntanceInfo
 * @author Hibernate Tools
 */
public class IntanceInfoHome {

    private static final Log log = LogFactory.getLog(IntanceInfoHome.class);

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
    
    public void persist(IntanceInfo transientInstance) {
        log.debug("persisting IntanceInfo instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(IntanceInfo instance) {
        log.debug("attaching dirty IntanceInfo instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(IntanceInfo instance) {
        log.debug("attaching clean IntanceInfo instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(IntanceInfo persistentInstance) {
        log.debug("deleting IntanceInfo instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public IntanceInfo merge(IntanceInfo detachedInstance) {
        log.debug("merging IntanceInfo instance");
        try {
            IntanceInfo result = (IntanceInfo) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public IntanceInfo findById( java.lang.Integer id) {
        log.debug("getting IntanceInfo instance with id: " + id);
        try {
            IntanceInfo instance = (IntanceInfo) sessionFactory.getCurrentSession()
                    .get("net.atos.ari.seip.CloudManager.db.model.IntanceInfo", id);
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
    
    public List findByExample(IntanceInfo instance) {
        log.debug("finding IntanceInfo instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("net.atos.ari.seip.CloudManager.db.model.IntanceInfo")
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

