package net.atos.ari.seip.CloudManager.db.utils;

import java.io.File;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	static {
		try {
//			String path;
//        	if (System.getProperty("file.separator").equalsIgnoreCase("/")) {
//        		path = System.getenv("OPTIMIS_HOME")+"/etc/sptf/hibernate-sp.cfg.xml";
//        	} else {
//        		path = System.getenv("OPTIMIS_HOME")+"\\etc\\sptf\\hibernate-sp.cfg.xml";
//        	}
//        	File hibernateConfig = new File(path);
//            sessionFactory = new Configuration().configure(hibernateConfig).buildSessionFactory();
            sessionFactory = new Configuration().configure().buildSessionFactory(); 
		} catch (HibernateException he) {
			System.err
					.println("Error while oppening session in sessionFactory: "
							+ he);
			throw new ExceptionInInitializerError(he);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}