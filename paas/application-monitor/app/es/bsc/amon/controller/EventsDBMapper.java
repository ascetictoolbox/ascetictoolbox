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

package es.bsc.amon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;

import java.util.*;

/**
 * Created by mmacias on 07/06/14.
 */
public enum EventsDBMapper {
	INSTANCE;

    public static final String TIMESTAMP = "timestamp";
	public static final String ENDTIME = "endtime";
    public static final String APPID = "appId";
    public static final String NODEID = "nodeId";
    public static final String DEPLOYMENT_ID = "deploymentId";
    public static final String DATA = "data";
    public static final String SLA_ID = "slaId";
	public static final String _ID = "_id";
	public static final String _ID_OID = "$oid";


    public static final String COLL_NAME = "events";


    private DBCollection colEvents = null;

    private EventsDBMapper() {
        // default table size to 64 MB
        Logger.info("Creating collection '"+ COLL_NAME +"'...");
        Properties config = DBManager.INSTANCE.getConfig();
        DB database = DBManager.INSTANCE.getDatabase();

        try {


            int collectionSize = Integer.parseInt(config.getProperty("collection.size"));
            colEvents = database.createCollection(COLL_NAME,
                    new BasicDBObject("capped", true) //enable round robin database
                            .append("size", collectionSize));
        } catch(MongoException cfe) {
            if(cfe.getCode() == DBManager.COLLECTION_ALREADY_EXISTS) {
                Logger.info("Collection '"+ COLL_NAME +"' already exists. Continuing normally...");
            }
            colEvents = database.getCollection(COLL_NAME);
        }

        // compound index events by timestamp, appId and nodeId
        BasicDBObject indexInfo = new BasicDBObject();
        indexInfo.put(EventsDBMapper.TIMESTAMP, -1); // 1 for ascending, -1 for descending
        indexInfo.put(EventsDBMapper.APPID, 1);
        indexInfo.put(EventsDBMapper.NODEID,1);

        colEvents.createIndex(indexInfo);
    }


    // cuando quiera encontrar métricas ordenándolas por timestamp:
    // {$orderby : {timestamp : -1}, $data : { $and : [ { timestamp : { $gte : 1402162324197 }}, { timestamp : {$lte : 1402162469099}} ] } }
    // el -1 ordena de mayor a menor y un 1 ordenaría de menor a mayor
    public ObjectNode storeEvent(ObjectNode event) {
	    long timestamp = System.currentTimeMillis();

        if(event.get(EventsDBMapper.TIMESTAMP) == null) {
            event.put(EventsDBMapper.TIMESTAMP, timestamp);
        } else {
            timestamp = event.get(EventsDBMapper.TIMESTAMP).asLong();
        }

		DBObject dbo = (DBObject) JSON.parse(event.toString());

        if(dbo.get(ENDTIME) == null) {
            dbo.put(ENDTIME,-1L);
        }
	    colEvents.save(dbo);

	    // return stored id and timestamp
	    ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
		on.put(_ID, dbo.get(_ID).toString());
	    on.put(TIMESTAMP,timestamp);

	    return on;
    }

	public JsonNode getLastEvent(String appId, String nodeId) {
		DBObject query = (DBObject) JSON.parse("{$orderby : {timestamp : -1}, $data : { $and : [ { appId : \"" + appId
				+ "\" }, { nodeId : \"" + nodeId + "\"} ] } }");
		return Json.parse(DBManager.INSTANCE.findOne(COLL_NAME,query).toString());
	}

	public ObjectNode get(String id) {
		try {
			String s = getString(id);
			if( s != null ) {
				return (ObjectNode) new ObjectMapper().readTree(s);
			}
		} catch(Exception e) {
			Logger.error(e.getMessage(),e);
		}
		return null;
	}

	public String getString(String id) {
		BasicDBObjectBuilder b = BasicDBObjectBuilder.start();
		b.add(_ID,new ObjectId(id));

		DBCursor cur = colEvents.find(b.get());
		if(cur.hasNext()) {
			return cur.next().toString();
		}
		return null;
	}

    public void delete(String id) {
        BasicDBObjectBuilder q = BasicDBObjectBuilder.start();
        q.add(_ID, new ObjectId(id));
        colEvents.remove(q.get());
    }

	public ObjectNode markAsFinished(String id) {
		BasicDBObjectBuilder q = BasicDBObjectBuilder.start();
		q.add(_ID,new ObjectId(id));
		long timestamp = Calendar.getInstance().getTimeInMillis();

		BasicDBObjectBuilder m = BasicDBObjectBuilder.start();
		m.add("$set", BasicDBObjectBuilder.start(ENDTIME,timestamp).get());

		colEvents.update(q.get(),m.get(),false,false);

		ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
		on.put(_ID,id);
		on.put(ENDTIME,timestamp);

		return on;
	}

    public void remove(DBObject dbo) {
        colEvents.remove(dbo);
    }

    public void remove(String id) {
        BasicDBObjectBuilder q = BasicDBObjectBuilder.start().add(_ID,new ObjectId(id));
        colEvents.findAndRemove(q.get());
    }

    public BasicDBList find(DBObject query) {
        BasicDBList dbl = new BasicDBList();
        DBCursor dbc = colEvents.find(query);
        while(dbc.hasNext()) {
            dbl.add(dbc.next());
        }
        return dbl;
    }

    public BasicDBList aggregate(BasicDBObject dbo) {
        List<DBObject> query = new ArrayList<DBObject>(1);
        query.add(dbo);
        AggregationOutput aggOut = colEvents.aggregate(query);
        BasicDBList dbl = new BasicDBList();
        Iterator<DBObject> it = aggOut.results().iterator();
        while(it.hasNext()) {
            dbl.add(it.next());
        }
        return dbl;
    }

    public BasicDBList aggregate(BasicDBList query) {
        List<DBObject> ql = new ArrayList<DBObject>(query.size());
        Iterator<Object> itq = query.iterator();
        while(itq.hasNext()) ql.add((DBObject)itq.next());
        AggregationOutput aggOut = colEvents.aggregate(ql);
        BasicDBList dbl = new BasicDBList();
        Iterator<DBObject> it = aggOut.results().iterator();
        while(it.hasNext()) {
            dbl.add(it.next());
        }
        return dbl;
    }



}
