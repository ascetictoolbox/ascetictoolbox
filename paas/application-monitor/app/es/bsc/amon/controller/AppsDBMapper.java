package es.bsc.amon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;

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
        DBObject query = (DBObject) JSON.parse("{$orderby : {timestamp : -1}, $query : { $and : [ { timestamp : { $gte : " + start
                + " }}, { timestamp : {$lte : " + end + "}} ] } }");

        ArrayNode ret = DBManager.instance.find(EventsDBMapper.COLL_NAME, query);

        Map<String,Set<String>> appsInfo = new HashMap<>();

        Iterator<JsonNode> iter = ret.iterator();
        while(iter.hasNext()) {
            JsonNode event = iter.next();
            String appName = event.get(EventsDBMapper.APPID).asText();
            String nodeName = event.get(EventsDBMapper.NODEID).asText();
            Set<String> appSet = appsInfo.get(appName);
            if (appSet == null) {
                appSet = new TreeSet<>();
                appsInfo.put(appName, appSet);
            }
            appSet.add(nodeName);
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
