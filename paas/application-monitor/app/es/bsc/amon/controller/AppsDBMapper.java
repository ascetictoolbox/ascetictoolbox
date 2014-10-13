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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import org.bson.BSONObject;
import play.Logger;

import java.util.*;

/**
 * Created by mmacias on 08/06/14.
 */
public class AppsDBMapper {
    private static AppsDBMapper instance;

    public static AppsDBMapper getInstance() {
        if(instance == null) {
            instance = new AppsDBMapper();
        }
        return instance;
    }

    /**
     *
     * @param start
     * @param end
     * @param showInstances if true, shows instances information instead of nodes information
     * @return
     */
    public ObjectNode getAllApps(long start, long end, boolean showInstances) {
        DBObject query = (DBObject) JSON.parse("{'$query' :" +
            "{ '$or' : ["+
                "{ '$and' : [ { timestamp : { '$gte' : " + start + " }}, { timestamp : {'$lte' : " + end + "}} ] }," +
                "{ '$and' : [ { endtime : { '$gte' : " + start + " }}, { endtime : {'$lte' : " + end + "}} ] }" +
            "]}," +
                "" +
                "'$orderby' : {timestamp : -1}}");

        BasicDBList ret = DBManager.instance.find(EventsDBMapper.COLL_NAME, query);

        Map<String,Set<String>> appsInfo = new HashMap<String,Set<String>>();

        Iterator<Object> iter = ret.iterator();
        while(iter.hasNext()) {
            DBObject event = (DBObject) iter.next();
            try {
                String appName = event.get(EventsDBMapper.APPID).toString();
                Object node = event.get(showInstances ? EventsDBMapper.INSTANCEID : EventsDBMapper.NODEID);
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
                    EventsDBMapper.getInstance().remove(event);
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
}
