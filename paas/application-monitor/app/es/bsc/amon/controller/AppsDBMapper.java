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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import play.Logger;

import java.util.*;

/**
 * Created by mmacias on 08/06/14.
 */
public enum AppsDBMapper {
    INSTANCE;

    private DBCollection colAppDeployments;

    private AppsDBMapper() {
        // default table size to 64 MB
        Logger.info("Creating collection '"+ COLL_NAME +"'...");
        Properties config = DBManager.INSTANCE.getConfig();
        DB database = DBManager.INSTANCE.getDatabase();

        try {


            int collectionSize = Integer.parseInt(config.getProperty("collection.size"));
            colAppDeployments = database.createCollection(COLL_NAME,
                    new BasicDBObject("capped", true) //enable round robin database
                            .append("size", collectionSize));

        } catch(MongoException cfe) {
            if(cfe.getCode() == DBManager.COLLECTION_ALREADY_EXISTS) {
                Logger.info("Collection '"+ COLL_NAME +"' already exists. Continuing normally...");
            }
            colAppDeployments = database.getCollection(COLL_NAME);
        }

        // compound index events by timestamp, appId and nodeId
        BasicDBObject indexInfo = new BasicDBObject();
        indexInfo.put(EventsDBMapper.TIMESTAMP, -1); // 1 for ascending, -1 for descending
        colAppDeployments.createIndex(indexInfo);
    }

    public void addAppDeployment(String appDeploymentJson) {
        BasicDBObject dbo = (BasicDBObject) JSON.parse(appDeploymentJson);
        BasicDBObject data = ((BasicDBObject)dbo.get(FIELD_DATA));
        if( data == null
            || dbo.get(FIELD_APP_ID) == null
            || dbo.get(FIELD_DEPLOYMENT_ID) == null
            || data.get(FIELD_DATA_START) == null
            || data.get(FIELD_DATA_END) == null
            || data.get(FIELD_DATA_POWER) == null){
            throw new AppException("The App document must have the following structure:\n"
                    + "{'appId' : ...\n"
                    + "'deploymentId' : ...\n"
                    + "'data' : {\n"
                    + "\t'start' : ....\n"
                    + "\t'end' : ....\n"
                    + "\t'power' : ....\n"
                    + "}}");
        }
        long timestamp = System.currentTimeMillis();
        dbo.put(FIELD_TIMESTAMP, timestamp);
        colAppDeployments.insert(dbo);
    }

    /**
     *
     * @param start
     * @param end
     * @param showDeployments if true, shows instances information instead of nodes information
     * @return
     */
    public ObjectNode getAllApps(long start, long end, boolean showDeployments) {
        DBObject query = (DBObject) JSON.parse(
            "{ '$or' : ["+
                "{ '$and' : [ { timestamp : { '$gte' : " + start + " }}, { timestamp : {'$lte' : " + end + "}} ] }," +
                "{ '$and' : [ { endtime : { '$gte' : " + start + " }}, { endtime : {'$lte' : " + end + "}} ] }" +
            "]}");
        //DBObject orderby = new BasicDBObject("timestamp":-1);
        BasicDBList ret = DBManager.INSTANCE.find(EventsDBMapper.COLL_NAME, query);

        Map<String,Set<String>> appsInfo = new HashMap<>();

        Iterator<Object> iter = ret.iterator();
        while(iter.hasNext()) {
            DBObject event = (DBObject) iter.next();
            try {
                String appName = event.get(EventsDBMapper.APPID).toString();
                Object node = event.get(showDeployments ? EventsDBMapper.DEPLOYMENT_ID : EventsDBMapper.NODEID);
                String nodeName = node==null?"":node.toString();
                Set<String> appSet = appsInfo.get(appName);
                if (appSet == null) {
                    appSet = new TreeSet<String>();
                    appsInfo.put(appName, appSet);
                }
                appSet.add(nodeName);
            } catch(NullPointerException ex) {
                Logger.warn("This element did not parsed as an application: " + event.toString() + ". Removing it from DB...");
                try {
                    EventsDBMapper.INSTANCE.remove(event);
                } catch(Exception e) {
                    Logger.error("Cannot remove it from database");
                }
            }
        }

        ObjectNode all = new ObjectNode(JsonNodeFactory.instance);
        for(Map.Entry<String,Set<String>> entry : appsInfo.entrySet()) {
            ArrayNode nodes = new ArrayNode(JsonNodeFactory.instance);
            for(String n : entry.getValue()) {
                nodes.add(n);
            }
            all.put(entry.getKey(), nodes);
        }
        return all;
    }

    public String getFinishedAppDeployments(long start, long end, int limit) {
        DBObject query = (DBObject) JSON.parse(
                "{ '$or' : ["+
                "{ '$and' : [ { timestamp : { '$gte' : " + start + " }}, { timestamp : {'$lte' : " + end + "}} ] }," +
                "{ '$and' : [ { endtime : { '$gte' : " + start + " }}, { endtime : {'$lte' : " + end + "}} ] }" +
                "]}");
		DBObject orderBy = (DBObject) JSON.parse("{ timestamp : -1 }");
        BasicDBList ret = DBManager.INSTANCE.find(COLL_NAME, query, orderBy, limit);
        return ret.toString();
    }

    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_APP_ID = "appId";
    public static final String FIELD_DEPLOYMENT_ID = "deploymentId";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_DATA_START = "start";
    public static final String FIELD_DATA_END = "end";
    public static final String FIELD_DATA_POWER = "power";
    public static final String COLL_NAME = "appdeployments";

    public class AppException extends RuntimeException {
        public AppException(String message) {
            super(message);
        }
    }
}
