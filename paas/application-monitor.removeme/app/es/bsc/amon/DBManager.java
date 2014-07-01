package es.bsc.amon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

    public ArrayNode find(String collectionName, DBObject query) {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
        DBCursor c = null;
        if(query == null) {
            c = database.getCollection(collectionName).find();
        } else {
            c = database.getCollection(collectionName).find(query);
        }
        while(c.hasNext()) {
            result.add(Json.parse(c.next().toString()));
        }
        c.close();
        return result;
    }

    public JsonNode findOne(String collectionName, DBObject query) {
        DBObject o = null;
        if(query == null) {
            o = database.getCollection(collectionName).findOne();
        } else {
            o = database.getCollection(collectionName).findOne(query);
        }
        if(o == null) {
            return null;
        } else {
            return Json.parse(o.toString());
        }
    }



}
