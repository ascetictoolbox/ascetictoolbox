package es.bsc.amon;

import play.Application;
import play.Configuration;
import play.GlobalSettings;

import java.io.File;
import java.util.Properties;

/**
 * Created by mmacias on 03/06/14.
 */
public class Global extends GlobalSettings {
	@Override
	public Configuration onLoadConfig(Configuration configuration, File file, ClassLoader classLoader) {
		Configuration c = super.onLoadConfig(configuration, file, classLoader);

		Properties p = new Properties();
		for(String name : new String[] {"mongo.host","mongo.port","mongo.dbname","collection.size"}) {
			p.setProperty(name, configuration.getString(name));
		}

		DBManager.instance.init(p);

		return c;
	}

	@Override
	public void onStop(Application app) {
		super.onStop(app);
		DBManager.instance.close();
	}
}
