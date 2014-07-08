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

    public ObjectNode getAllApps(long start, long end) {
        DBObject query = (DBObject) JSON.parse("{'$query' : { '$and' : [ { timestamp : { '$gte' : " + start
                + " }}, { timestamp : {'$lte' : " + end + "}} ] } ,'$orderby' : {timestamp : -1}}");

        BasicDBList ret = DBManager.instance.find(EventsDBMapper.COLL_NAME, query);

        Map<String,Set<String>> appsInfo = new HashMap<String,Set<String>>();

        Iterator<Object> iter = ret.iterator();
        while(iter.hasNext()) {
            DBObject event = (DBObject) iter.next();
            try {
                String appName = event.get(EventsDBMapper.APPID).toString();
                String nodeName = event.get(EventsDBMapper.NODEID).toString();
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
