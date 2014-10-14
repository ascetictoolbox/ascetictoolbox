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

import com.mongodb.*;
import play.Logger;
import play.libs.Json;

import java.util.Properties;

/**
 * Created by mmacias on 27/05/14.
 */

public class DBManager {
	public static final DBManager instance = new DBManager();

	private MongoClient client;
	private DB database;
    private Properties config;

    private DBManager() {
    }

	public void init(Properties config) {
		close();
        this.config = config;
		try {
			String host = config.getProperty("mongo.host");
			int port = Integer.parseInt(config.getProperty("mongo.port"));
			String dbName = config.getProperty("mongo.dbname");
			Logger.info("Connecting to mongodb://"+host+":"+port+"/"+dbName);
			client = new MongoClient(host,port);
			database = client.getDB(dbName);

		} catch(Throwable e) {
			Logger.info(e.getMessage());
			throw new RuntimeException(e);
		}
	}


    public void close() {
		if(client != null) {
			Logger.debug("Closing client database connection: " + client.toString() );
			client.close();
		}
	}

    public DB getDatabase() {
        return database;
    }

    public Properties getConfig() {
        return config;
    }

    public BasicDBList find(String collectionName, DBObject query) {
        return find(collectionName, query, null);
    }
    public BasicDBList find(String collectionName, DBObject query, Integer limit) {
        BasicDBList result = new BasicDBList();

        DBCursor c = null;
        if(query == null) {
            c = database.getCollection(collectionName).find();
        } else {
            c = database.getCollection(collectionName).find(query);
        }
        if(limit != null && limit > 0) {
            c = c.limit(limit);
        }
        while(c.hasNext()) {
            result.add(c.next());
        }
        c.close();
        return result;
    }

    public DBObject findOne(String collectionName, DBObject query) {
        DBObject o = null;
        if(query == null) {
            o = database.getCollection(collectionName).findOne();
        } else {
            o = database.getCollection(collectionName).findOne(query);
        }
        return o;
    }

    public void add(String collectionName, DBObject obj) {
        database.getCollection(collectionName).insert(obj);
    }



}
