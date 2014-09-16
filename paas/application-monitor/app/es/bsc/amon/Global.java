/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */

package es.bsc.amon;

import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.SimpleResult;
import scala.concurrent.Future;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader requestHeader, String s) {
        return F.Promise.<Result>pure(Results.internalServerError("BAD REQUEST: " + s));
    }

    @Override
	public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
		if(t.getCause()!=null) t = t.getCause();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return F.Promise.<Result>pure(Results.internalServerError("INTERNAL SERVER ERROR: " + sw.toString()));
	}

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
        return F.Promise.<Result>pure(Results.internalServerError("HANDLER NOT FOUND: " + requestHeader.uri()));

    }
}
