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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.BasicBSONList;
import org.jongo.Jongo;
import play.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Created by mmacias on 27/05/14.
 */

public enum DBManager {
	INSTANCE;
    public static final int COLLECTION_ALREADY_EXISTS = 17399;

	//private Jongo jongo;
	private MongoClient client;
	private DB database;
    private Properties config;

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
			//jongo = new Jongo(database);

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
        return find(collectionName, query, null, null);
    }

    public BasicDBList find(String collectionName, DBObject query, DBObject orderby) {
        return find(collectionName, query, orderby, null);
    }
    public BasicDBList find(String collectionName, DBObject query, DBObject orderby, Integer limit) {
        BasicDBList result = new BasicDBList();

        DBCursor c = null;
        if(query == null) {
            c = database.getCollection(collectionName).find();
        } else {
            c = database.getCollection(collectionName).find(query);
        }

        if(orderby == null) {
            c.sort(orderby);
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
